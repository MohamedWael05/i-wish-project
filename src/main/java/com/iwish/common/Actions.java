package com.iwish.common;

public final class Actions {
    private Actions() {}

    public static final String REGISTER = "REGISTER";
    public static final String LOGIN = "LOGIN";

    public static final String SEARCH_USERS = "SEARCH_USERS";
    public static final String SEND_FRIEND_REQUEST = "SEND_FRIEND_REQUEST";
    public static final String RESPOND_FRIEND_REQUEST = "RESPOND_FRIEND_REQUEST";
    public static final String REMOVE_FRIEND = "REMOVE_FRIEND";
    public static final String GET_FRIENDS = "GET_FRIENDS";
    public static final String GET_PENDING_REQUESTS = "GET_PENDING_REQUESTS";

    public static final String GET_CATALOG_ITEMS = "GET_CATALOG_ITEMS";
    public static final String ADD_WISHLIST_ITEM = "ADD_WISHLIST_ITEM";
    public static final String UPDATE_WISHLIST_ITEM_NOTE = "UPDATE_WISHLIST_ITEM_NOTE";
    public static final String DELETE_WISHLIST_ITEM = "DELETE_WISHLIST_ITEM";
    public static final String GET_MY_WISHLIST = "GET_MY_WISHLIST";
    public static final String GET_FRIEND_WISHLIST = "GET_FRIEND_WISHLIST";

    public static final String CONTRIBUTE = "CONTRIBUTE";

    public static final String GET_NOTIFICATIONS = "GET_NOTIFICATIONS";
    public static final String MARK_NOTIFICATIONS_READ = "MARK_NOTIFICATIONS_READ";

    public static final String LOGOUT = "LOGOUT";
}
