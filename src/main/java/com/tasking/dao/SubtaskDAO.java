package com.tasking.dao;

import com.tasking.model.Subtask;
import com.tasking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubtaskDAO {

    /**
     * Get all subtasks belonging to one task.
     */
    public List<Subtask> findByTask(Long taskId) {

        String sql =
                "SELECT id, task_id, title, is_completed, created_at, updated_at "
                + "FROM subtasks "
                + "WHERE task_id = ? "
                + "ORDER BY id ASC";

        List<Subtask> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, taskId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("SubtaskDAO.findByTask failed", e);
        }

        return list;
    }

    /**
     * Get subtasks belonging to multiple tasks.
     */
    public Map<Long, List<Subtask>> findByTaskIds(List<Long> taskIds) {

        Map<Long, List<Subtask>> grouped = new HashMap<>();

        if (taskIds == null || taskIds.isEmpty()) {
            return grouped;
        }

        for (Long taskId : taskIds) {
            grouped.put(taskId, new ArrayList<>());
        }

        String placeholders = String.join(
                ",",
                Collections.nCopies(taskIds.size(), "?")
        );

        String sql =
                "SELECT id, task_id, title, is_completed, created_at, updated_at "
                + "FROM subtasks "
                + "WHERE task_id IN (" + placeholders + ") "
                + "ORDER BY task_id ASC, id ASC";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            for (int i = 0; i < taskIds.size(); i++) {
                ps.setLong(i + 1, taskIds.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Subtask subtask = map(rs);

                    grouped.computeIfAbsent(
                            subtask.getTaskId(),
                            id -> new ArrayList<>()
                    ).add(subtask);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("SubtaskDAO.findByTaskIds failed", e);
        }

        return grouped;
    }

    /**
     * Find the parent task ID only if the subtask belongs to the current user's task.
     */
    public Long findOwnedTaskId(Long subtaskId, Long userId) {

        String sql =
                "SELECT s.task_id "
                + "FROM subtasks s "
                + "JOIN tasks t ON t.id = s.task_id "
                + "WHERE s.id = ? AND t.user_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, subtaskId);
            ps.setLong(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("SubtaskDAO.findOwnedTaskId failed", e);
        }
    }

    /**
     * Create a new subtask.
     */
    public Long create(Long taskId, String title) {

        String sql =
                "INSERT INTO subtasks "
                + "(task_id, title, is_completed, created_at, updated_at) "
                + "VALUES (?, ?, false, now(), now()) "
                + "RETURNING id";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, taskId);
            ps.setString(2, title);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("SubtaskDAO.create failed", e);
        }
    }

    /**
     * Toggle completion status of a subtask.
     * Diperbaiki: Menggunakan subquery untuk menghindari error "ambiguous column"
     * karena tabel tasks dan subtasks sama-sama punya kolom 'is_completed' dan 'id'.
     */
    public boolean toggleCompleted(Long subtaskId, Long userId) {

        String sql =
                "UPDATE subtasks "
                + "SET is_completed = NOT is_completed, updated_at = now() "
                + "WHERE id = ? "
                + "AND task_id IN (SELECT id FROM tasks WHERE user_id = ?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, subtaskId);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("SubtaskDAO.toggleCompleted failed", e);
        }
    }

    /**
     * Delete a subtask belonging to the current user's task.
     */
    public boolean delete(Long subtaskId, Long userId) {

        String sql =
                "DELETE FROM subtasks s "
                + "USING tasks t "
                + "WHERE s.id = ? "
                + "AND t.id = s.task_id "
                + "AND t.user_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, subtaskId);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("SubtaskDAO.delete failed", e);
        }
    }

    /**
     * Convert database ResultSet into Subtask model.
     */
    private Subtask map(ResultSet rs) throws SQLException {

        Subtask s = new Subtask();

        s.setId(rs.getLong("id"));
        s.setTaskId(rs.getLong("task_id"));
        s.setTitle(rs.getString("title"));
        s.setCompleted(rs.getBoolean("is_completed"));

        Timestamp ca = rs.getTimestamp("created_at");

        s.setCreatedAt(
                ca == null ? null : ca.toLocalDateTime()
        );

        Timestamp ua = rs.getTimestamp("updated_at");

        s.setUpdatedAt(
                ua == null ? null : ua.toLocalDateTime()
        );

        return s;
    }
}