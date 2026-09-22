package com.iwish.server;

import com.iwish.common.CatalogItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CatalogDAO {

    public List<CatalogItem> listAll() throws SQLException {
        List<CatalogItem> list = new ArrayList<>();
        String sql = "SELECT id, name, description, price FROM catalog_items ORDER BY name";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CatalogItem(rs.getInt("id"), rs.getString("name"),
                        rs.getString("description"), rs.getBigDecimal("price")));
            }
        }
        return list;
    }

    public CatalogItem findById(int id) throws SQLException {
        String sql = "SELECT id, name, description, price FROM catalog_items WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CatalogItem(rs.getInt("id"), rs.getString("name"),
                            rs.getString("description"), rs.getBigDecimal("price"));
                }
            }
        }
        return null;
    }
}
