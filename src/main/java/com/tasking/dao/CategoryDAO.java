package com.tasking.dao;

import com.tasking.model.Category;
import com.tasking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> findByUser(Long userId) {
        String sql = "SELECT id, user_id, name, created_at, updated_at FROM categories "
                + "WHERE user_id = ? ORDER BY name ASC";
        List<Category> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findByUser failed", e);
        }
        return list;
    }

    public Category findByIdAndUser(Long id, Long userId) {
        String sql = "SELECT id, user_id, name, created_at, updated_at FROM categories "
                + "WHERE id = ? AND user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findByIdAndUser failed", e);
        }
    }

    /** True if the id belongs to this user (ownership check for task category_id). */
    public boolean existsForUser(Long id, Long userId) {
        return id != null && findByIdAndUser(id, userId) != null;
    }

    public Long create(Long userId, String name) {
        String sql = "INSERT INTO categories (user_id, name, created_at, updated_at) "
                + "VALUES (?, ?, now(), now()) RETURNING id";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.create failed", e);
        }
    }

    public boolean update(Long id, Long userId, String name) {
        String sql = "UPDATE categories SET name = ?, updated_at = now() WHERE id = ? AND user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setLong(2, id);
            ps.setLong(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.update failed", e);
        }
    }

    /** Delete; tasks keep their data and their category_id is set to NULL by the FK (ON DELETE SET NULL). */
    public boolean delete(Long id, Long userId) {
        String sql = "DELETE FROM categories WHERE id = ? AND user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.delete failed", e);
        }
    }

    private Category map(ResultSet rs) throws SQLException {
        Category cat = new Category();
        cat.setId(rs.getLong("id"));
        cat.setUserId(rs.getLong("user_id"));
        cat.setName(rs.getString("name"));
        Timestamp ca = rs.getTimestamp("created_at");
        cat.setCreatedAt(ca == null ? null : ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        cat.setUpdatedAt(ua == null ? null : ua.toLocalDateTime());
        return cat;
    }
}
