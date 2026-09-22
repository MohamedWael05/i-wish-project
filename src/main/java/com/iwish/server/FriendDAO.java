package com.iwish.server;

import com.iwish.common.FriendInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendDAO {

    public void sendRequest(int userId, int friendId) throws SQLException {
        String sql = "INSERT INTO friendships (user_a, user_b, status, requested_by) VALUES (?, ?, 'PENDING', ?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Math.min(userId, friendId));
            ps.setInt(2, Math.max(userId, friendId));
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public boolean friendshipExists(int userId, int friendId) throws SQLException {
        String sql = "SELECT id FROM friendships WHERE user_a = ? AND user_b = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Math.min(userId, friendId));
            ps.setInt(2, Math.max(userId, friendId));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void respondToRequest(int friendshipId, boolean accept) throws SQLException {
        if (accept) {
            String sql = "UPDATE friendships SET status = 'ACCEPTED' WHERE id = ?";
            try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, friendshipId);
                ps.executeUpdate();
            }
        } else {
            String sql = "DELETE FROM friendships WHERE id = ?";
            try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, friendshipId);
                ps.executeUpdate();
            }
        }
    }

    public void removeFriend(int friendshipId) throws SQLException {
        String sql = "DELETE FROM friendships WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, friendshipId);
            ps.executeUpdate();
        }
    }

    public Integer getOtherUser(int friendshipId, int currentUserId) throws SQLException {
        String sql = "SELECT user_a, user_b FROM friendships WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, friendshipId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int a = rs.getInt("user_a");
                    int b = rs.getInt("user_b");
                    return a == currentUserId ? b : a;
                }
            }
        }
        return null;
    }

    public List<FriendInfo> listFriends(int userId) throws SQLException {
        List<FriendInfo> list = new ArrayList<>();
        String sql = "SELECT f.id AS friendship_id, f.status, f.requested_by, u.id AS uid, u.username, u.full_name " +
                "FROM friendships f " +
                "JOIN users u ON u.id = (CASE WHEN f.user_a = ? THEN f.user_b ELSE f.user_a END) " +
                "WHERE (f.user_a = ? OR f.user_b = ?) AND f.status = 'ACCEPTED'";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<FriendInfo> listPendingIncoming(int userId) throws SQLException {
        List<FriendInfo> list = new ArrayList<>();
        String sql = "SELECT f.id AS friendship_id, f.status, f.requested_by, u.id AS uid, u.username, u.full_name " +
                "FROM friendships f " +
                "JOIN users u ON u.id = (CASE WHEN f.user_a = ? THEN f.user_b ELSE f.user_a END) " +
                "WHERE (f.user_a = ? OR f.user_b = ?) AND f.status = 'PENDING' AND f.requested_by != ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private FriendInfo mapRow(ResultSet rs) throws SQLException {
        FriendInfo fi = new FriendInfo();
        fi.setFriendshipId(rs.getInt("friendship_id"));
        fi.setStatus(rs.getString("status"));
        fi.setRequestedBy(rs.getInt("requested_by"));
        fi.setUserId(rs.getInt("uid"));
        fi.setUsername(rs.getString("username"));
        fi.setFullName(rs.getString("full_name"));
        return fi;
    }
}
