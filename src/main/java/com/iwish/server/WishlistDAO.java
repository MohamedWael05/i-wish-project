package com.iwish.server;

import com.iwish.common.Contribution;
import com.iwish.common.WishlistItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAO {

    public int addItem(int ownerId, int catalogItemId, String note) throws SQLException {
        String sql = "INSERT INTO wishlist_items (owner_id, catalog_item_id, note, status) VALUES (?, ?, ?, 'OPEN')";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ownerId);
            ps.setInt(2, catalogItemId);
            ps.setString(3, note);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void updateNote(int wishlistItemId, int ownerId, String note) throws SQLException {
        String sql = "UPDATE wishlist_items SET note = ? WHERE id = ? AND owner_id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, note);
            ps.setInt(2, wishlistItemId);
            ps.setInt(3, ownerId);
            ps.executeUpdate();
        }
    }

    public boolean deleteItem(int wishlistItemId, int ownerId) throws SQLException {
        String sql = "DELETE FROM wishlist_items WHERE id = ? AND owner_id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, wishlistItemId);
            ps.setInt(2, ownerId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<WishlistItem> listForOwner(int ownerId) throws SQLException {
        List<WishlistItem> list = new ArrayList<>();
        String sql = "SELECT w.id, w.owner_id, u.username AS owner_username, w.catalog_item_id, " +
                "ci.name AS item_name, ci.price AS price, w.note, w.status, w.created_at, " +
                "COALESCE((SELECT SUM(amount) FROM contributions ct WHERE ct.wishlist_item_id = w.id), 0) AS funded " +
                "FROM wishlist_items w " +
                "JOIN catalog_items ci ON ci.id = w.catalog_item_id " +
                "JOIN users u ON u.id = w.owner_id " +
                "WHERE w.owner_id = ? ORDER BY w.created_at DESC";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public WishlistItem findById(int wishlistItemId) throws SQLException {
        String sql = "SELECT w.id, w.owner_id, u.username AS owner_username, w.catalog_item_id, " +
                "ci.name AS item_name, ci.price AS price, w.note, w.status, w.created_at, " +
                "COALESCE((SELECT SUM(amount) FROM contributions ct WHERE ct.wishlist_item_id = w.id), 0) AS funded " +
                "FROM wishlist_items w " +
                "JOIN catalog_items ci ON ci.id = w.catalog_item_id " +
                "JOIN users u ON u.id = w.owner_id " +
                "WHERE w.id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, wishlistItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private WishlistItem mapRow(ResultSet rs) throws SQLException {
        WishlistItem wi = new WishlistItem();
        wi.setId(rs.getInt("id"));
        wi.setOwnerId(rs.getInt("owner_id"));
        wi.setOwnerUsername(rs.getString("owner_username"));
        wi.setCatalogItemId(rs.getInt("catalog_item_id"));
        wi.setItemName(rs.getString("item_name"));
        wi.setPrice(rs.getBigDecimal("price"));
        wi.setNote(rs.getString("note"));
        wi.setStatus(rs.getString("status"));
        wi.setCreatedAt(rs.getTimestamp("created_at"));
        wi.setFundedAmount(rs.getBigDecimal("funded"));
        return wi;
    }

    public List<Integer> contribute(int wishlistItemId, int contributorId, BigDecimal amount) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                String insert = "INSERT INTO contributions (wishlist_item_id, contributor_id, amount) VALUES (?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(insert)) {
                    ps.setInt(1, wishlistItemId);
                    ps.setInt(2, contributorId);
                    ps.setBigDecimal(3, amount);
                    ps.executeUpdate();
                }

                BigDecimal price;
                BigDecimal funded;
                String statusNow;
                String sel = "SELECT ci.price, w.status, " +
                        "COALESCE((SELECT SUM(amount) FROM contributions ct WHERE ct.wishlist_item_id = w.id), 0) AS funded " +
                        "FROM wishlist_items w JOIN catalog_items ci ON ci.id = w.catalog_item_id WHERE w.id = ?";
                try (PreparedStatement ps = c.prepareStatement(sel)) {
                    ps.setInt(1, wishlistItemId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        price = rs.getBigDecimal("price");
                        funded = rs.getBigDecimal("funded");
                        statusNow = rs.getString("status");
                    }
                }

                List<Integer> contributors = null;
                if (funded.compareTo(price) >= 0 && !"COMPLETED".equals(statusNow)) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE wishlist_items SET status = 'COMPLETED' WHERE id = ?")) {
                        ps.setInt(1, wishlistItemId);
                        ps.executeUpdate();
                    }
                    contributors = new ArrayList<>();
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT DISTINCT contributor_id FROM contributions WHERE wishlist_item_id = ?")) {
                        ps.setInt(1, wishlistItemId);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) contributors.add(rs.getInt("contributor_id"));
                        }
                    }
                }

                c.commit();
                return contributors;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public List<Contribution> listContributions(int wishlistItemId) throws SQLException {
        List<Contribution> list = new ArrayList<>();
        String sql = "SELECT c.id, c.wishlist_item_id, c.contributor_id, u.username, c.amount, c.created_at " +
                "FROM contributions c JOIN users u ON u.id = c.contributor_id " +
                "WHERE c.wishlist_item_id = ? ORDER BY c.created_at";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wishlistItemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Contribution ct = new Contribution();
                    ct.setId(rs.getInt("id"));
                    ct.setWishlistItemId(rs.getInt("wishlist_item_id"));
                    ct.setContributorId(rs.getInt("contributor_id"));
                    ct.setContributorUsername(rs.getString("username"));
                    ct.setAmount(rs.getBigDecimal("amount"));
                    ct.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(ct);
                }
            }
        }
        return list;
    }
}
