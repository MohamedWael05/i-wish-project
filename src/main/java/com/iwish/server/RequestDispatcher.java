package com.iwish.server;

import com.iwish.common.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RequestDispatcher {

    private final UserDAO userDAO = new UserDAO();
    private final FriendDAO friendDAO = new FriendDAO();
    private final CatalogDAO catalogDAO = new CatalogDAO();
    private final WishlistDAO wishlistDAO = new WishlistDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public Response handle(Request req, ClientHandler session) {
        try {
            switch (req.getAction()) {
                case Actions.REGISTER: return doRegister(req);
                case Actions.LOGIN: return doLogin(req, session);
                case Actions.LOGOUT: session.setCurrentUser(null); return Response.ok("Logged out.");

                default:
                    // Every action below requires an authenticated session.
                    if (session.getCurrentUser() == null) {
                        return Response.fail("You must be logged in.");
                    }
                    return handleAuthenticated(req, session);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.fail("Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("Server error: " + e.getMessage());
        }
    }

    private Response handleAuthenticated(Request req, ClientHandler session) throws SQLException {
        User me = session.getCurrentUser();
        switch (req.getAction()) {
            case Actions.SEARCH_USERS: {
                List<User> results = userDAO.search(req.getString("query"), me.getId());
                return Response.ok("ok", new ArrayList<>(results));
            }
            case Actions.SEND_FRIEND_REQUEST: {
                String targetUsername = req.getString("username");
                User target = null;
                for (User u : userDAO.search(targetUsername, me.getId())) {
                    if (u.getUsername().equalsIgnoreCase(targetUsername)) { target = u; break; }
                }
                if (target == null) return Response.fail("No user found with that username.");
                if (target.getId() == me.getId()) return Response.fail("You can't friend yourself!");
                if (friendDAO.friendshipExists(me.getId(), target.getId())) {
                    return Response.fail("A friendship or pending request already exists.");
                }
                friendDAO.sendRequest(me.getId(), target.getId());
                notificationDAO.create(target.getId(), me.getFullName() + " (@" + me.getUsername() + ") sent you a friend request.");
                return Response.ok("Friend request sent to " + target.getUsername() + ".");
            }
            case Actions.RESPOND_FRIEND_REQUEST: {
                int friendshipId = req.getInt("friendshipId");
                boolean accept = Boolean.parseBoolean(req.getString("accept"));
                Integer other = friendDAO.getOtherUser(friendshipId, me.getId());
                friendDAO.respondToRequest(friendshipId, accept);
                if (accept && other != null) {
                    notificationDAO.create(other, me.getFullName() + " accepted your friend request!");
                }
                return Response.ok(accept ? "Friend request accepted." : "Friend request declined.");
            }
            case Actions.REMOVE_FRIEND: {
                friendDAO.removeFriend(req.getInt("friendshipId"));
                return Response.ok("Friend removed.");
            }
            case Actions.GET_FRIENDS: {
                return Response.ok("ok", new ArrayList<>(friendDAO.listFriends(me.getId())));
            }
            case Actions.GET_PENDING_REQUESTS: {
                return Response.ok("ok", new ArrayList<>(friendDAO.listPendingIncoming(me.getId())));
            }
            case Actions.GET_CATALOG_ITEMS: {
                return Response.ok("ok", new ArrayList<>(catalogDAO.listAll()));
            }
            case Actions.ADD_WISHLIST_ITEM: {
                int catalogItemId = req.getInt("catalogItemId");
                String note = req.getString("note");
                int id = wishlistDAO.addItem(me.getId(), catalogItemId, note == null ? "" : note);
                if (id < 0) return Response.fail("Could not add item.");
                return Response.ok("Item added to your wish list.");
            }
            case Actions.UPDATE_WISHLIST_ITEM_NOTE: {
                wishlistDAO.updateNote(req.getInt("wishlistItemId"), me.getId(), req.getString("note"));
                return Response.ok("Note updated.");
            }
            case Actions.DELETE_WISHLIST_ITEM: {
                boolean ok = wishlistDAO.deleteItem(req.getInt("wishlistItemId"), me.getId());
                return ok ? Response.ok("Item removed from your wish list.") : Response.fail("Could not remove item.");
            }
            case Actions.GET_MY_WISHLIST: {
                return Response.ok("ok", new ArrayList<>(wishlistDAO.listForOwner(me.getId())));
            }
            case Actions.GET_FRIEND_WISHLIST: {
                int friendId = req.getInt("friendId");
                if (!friendDAO.friendshipExists(me.getId(), friendId)) {
                    return Response.fail("You are not friends with this user.");
                }
                return Response.ok("ok", new ArrayList<>(wishlistDAO.listForOwner(friendId)));
            }
            case Actions.CONTRIBUTE: {
                int wishlistItemId = req.getInt("wishlistItemId");
                BigDecimal amount = new BigDecimal(req.getString("amount"));
                if (amount.compareTo(BigDecimal.ZERO) <= 0) return Response.fail("Amount must be positive.");

                WishlistItem item = wishlistDAO.findById(wishlistItemId);
                if (item == null) return Response.fail("Item not found.");
                if (item.getOwnerId() == me.getId()) return Response.fail("You can't contribute to your own wish list item!");
                if ("COMPLETED".equals(item.getStatus())) return Response.fail("This item is already fully funded.");
                if (!friendDAO.friendshipExists(me.getId(), item.getOwnerId())) {
                    return Response.fail("You are not friends with this user.");
                }

                List<Integer> completedContributors = wishlistDAO.contribute(wishlistItemId, me.getId(), amount);

                if (completedContributors != null) {
                    // Fully funded now: notify receiver + all contributors (buyers).
                    notificationDAO.create(item.getOwnerId(),
                            "Great news! Your friends have fully funded your gift: " + item.getItemName() + " \uD83C\uDF81");
                    for (Integer contributorId : completedContributors) {
                        notificationDAO.create(contributorId,
                                "The gift \"" + item.getItemName() + "\" for " + item.getOwnerUsername() + " is now fully funded! \uD83C\uDF89");
                    }
                    return Response.ok("Contribution recorded. This gift is now fully funded!");
                }
                return Response.ok("Contribution of $" + amount + " recorded. Thank you!");
            }
            case Actions.GET_NOTIFICATIONS: {
                return Response.ok("ok", new ArrayList<>(notificationDAO.listForUser(me.getId())));
            }
            case Actions.MARK_NOTIFICATIONS_READ: {
                notificationDAO.markAllRead(me.getId());
                return Response.ok("ok");
            }
            default:
                return Response.fail("Unknown action: " + req.getAction());
        }
    }

    private Response doRegister(Request req) throws SQLException {
        String username = req.getString("username");
        String password = req.getString("password");
        String fullName = req.getString("fullName");
        String email = req.getString("email");

        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return Response.fail("Username and password are required.");
        }
        if (userDAO.usernameExists(username)) {
            return Response.fail("That username is already taken.");
        }
        User user = userDAO.register(username.trim(), password, fullName, email);
        return Response.ok("Account created! You can now sign in.", user);
    }

    private Response doLogin(Request req, ClientHandler session) throws SQLException {
        String username = req.getString("username");
        String password = req.getString("password");
        User user = userDAO.login(username, password);
        if (user == null) {
            return Response.fail("Invalid username or password.");
        }
        session.setCurrentUser(user);
        return Response.ok("Welcome back, " + user.getFullName() + "!", (Serializable) user);
    }
}
