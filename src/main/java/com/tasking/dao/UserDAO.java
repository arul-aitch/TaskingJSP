
package com.tasking.dao;

import com.tasking.model.User;
import com.tasking.util.DBConnection;

import java.sql.*;

public class UserDAO {

    public User findByEmail(String email) {
        String sql = "SELECT id, name, email, created_at, updated_at, password "
                + "FROM users WHERE lower(email) = lower(?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findByEmail failed", e);
        }
    }

    public String getPasswordHash(String email) {
        String sql = "SELECT password FROM users WHERE lower(email) = lower(?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("password") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.getPasswordHash failed", e);
        }
    }

    public User findById(Long id) {
        String sql = "SELECT id, name, email, created_at, updated_at "
                + "FROM users WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findById failed", e);
        }
    }

    public boolean emailExists(String email, Long excludeId) {
        String sql = "SELECT 1 FROM users WHERE lower(email) = lower(?) "
                + "AND (?::bigint IS NULL OR id <> ?::bigint)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);

            if (excludeId == null) {
                ps.setNull(2, Types.BIGINT);
                ps.setNull(3, Types.BIGINT);
            } else {
                ps.setLong(2, excludeId);
                ps.setLong(3, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.emailExists failed", e);
        }
    }

    public Long create(String name, String email, String passwordHash) {
        String sql = "INSERT INTO users (name, email, password, created_at, updated_at) "
                + "VALUES (?, ?, ?, now(), now()) RETURNING id";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.create failed", e);
        }
    }

    public boolean updateProfile(Long id, String name, String email, boolean emailChanged) {
        String sql = "UPDATE users SET name = ?, email = ?, updated_at = now() "
                + "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setLong(3, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateProfile failed", e);
        }
    }

    public boolean updatePassword(Long id, String passwordHash) {
        String sql = "UPDATE users SET password = ?, updated_at = now() WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, passwordHash);
            ps.setLong(2, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updatePassword failed", e);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.delete failed", e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();

        u.setId(rs.getLong("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));

        Timestamp ca = rs.getTimestamp("created_at");
        u.setCreatedAt(ca == null ? null : ca.toLocalDateTime());

        Timestamp ua = rs.getTimestamp("updated_at");
        u.setUpdatedAt(ua == null ? null : ua.toLocalDateTime());

        return u;
    }
}
