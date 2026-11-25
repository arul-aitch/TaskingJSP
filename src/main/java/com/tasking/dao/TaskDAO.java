
package com.tasking.dao;

import com.tasking.model.Subtask;
import com.tasking.model.Task;
import com.tasking.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskDAO {

    /** Count helpers for dashboard progress. */
    public int countByUser(Long userId) {
        return count(userId, null);
    }

    public int countCompletedByUser(Long userId) {
        return count(userId, Boolean.TRUE);
    }

    private int count(Long userId, Boolean completed) {
        String sql = "SELECT count(*) FROM tasks WHERE user_id = ?"
                + (completed != null ? " AND is_completed = ?" : "");

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, userId);

            if (completed != null) {
                ps.setBoolean(2, completed);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("TaskDAO.count failed", e);
        }
    }

    /**
     * Dashboard query:
     * - Filter by category
     * - Search task title
     * - Filter active tasks or archived tasks
     * - Sort by pinned status and deadline
     * - Load all subtasks belonging to displayed tasks
     */
    public List<Task> findForDashboard(
            Long userId,
            Long categoryId,
            String search,
            boolean archive,
            boolean newestFirst
    ) {

        StringBuilder sql = new StringBuilder(
                "SELECT t.id, t.user_id, t.category_id, t.title, t.description, t.deadline, "
                + "t.is_completed, t.is_pinned, t.created_at, t.updated_at, "
                + "c.name AS category_name, "
                + "(SELECT count(*) FROM subtasks s WHERE s.task_id = t.id) AS subtask_total, "
                + "(SELECT count(*) FROM subtasks s "
                + "WHERE s.task_id = t.id AND s.is_completed = true) AS subtask_done "
                + "FROM tasks t "
                + "LEFT JOIN categories c ON c.id = t.category_id "
                + "WHERE t.user_id = ? AND t.is_completed = ?"
        );

        List<Object> params = new ArrayList<>();

        params.add(userId);
        params.add(archive);

        if (categoryId != null) {
            sql.append(" AND t.category_id = ?");
            params.add(categoryId);
        }

        if (search != null && !search.isBlank()) {
            sql.append(" AND t.title ILIKE ?");
            params.add("%" + search.trim() + "%");
        }

        sql.append(newestFirst
                ? " ORDER BY t.is_pinned DESC, t.created_at DESC, t.id DESC"
                : " ORDER BY t.is_pinned DESC, t.deadline ASC NULLS LAST, t.created_at DESC, t.id DESC"
        );

        List<Task> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);

                if (p instanceof Long l) {
                    ps.setLong(i + 1, l);
                } else if (p instanceof Boolean b) {
                    ps.setBoolean(i + 1, b);
                } else {
                    ps.setString(i + 1, (String) p);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("TaskDAO.findForDashboard failed", e);
        }

        /*
         * Load actual subtask records.
         *
         * Previously, the dashboard only loaded subtask_total
         * and subtask_done. The task.subtasks list remained empty.
         *
         * This caused the progress indicator to show 0/1,
         * while the subtask title itself was not displayed.
         */

        if (!list.isEmpty()) {

            List<Long> taskIds = list.stream()
                    .map(Task::getId)
                    .toList();

            Map<Long, List<Subtask>> subtasksByTask =
                    new SubtaskDAO().findByTaskIds(taskIds);

            for (Task task : list) {
                task.setSubtasks(
                        subtasksByTask.getOrDefault(
                                task.getId(),
                                List.of()
                        )
                );
            }
        }

        return list;
    }

    /**
     * Find a task belonging to a specific user.
     * Also loads its subtasks.
     */
    public Task findByIdAndUser(Long id, Long userId) {

        String sql =
                "SELECT t.id, t.user_id, t.category_id, t.title, "
                + "t.description, t.deadline, "
                + "t.is_completed, t.is_pinned, "
                + "t.created_at, t.updated_at, "
                + "c.name AS category_name, "
                + "0 AS subtask_total, "
                + "0 AS subtask_done "
                + "FROM tasks t "
                + "LEFT JOIN categories c ON c.id = t.category_id "
                + "WHERE t.id = ? AND t.user_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setLong(2, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                Task t = map(rs);

                SubtaskDAO subDAO = new SubtaskDAO();

                List<Subtask> subs =
                        subDAO.findByTask(t.getId());

                t.setSubtasks(subs);

                t.setSubtaskTotal(subs.size());

                t.setSubtaskCompleted(
                        (int) subs.stream()
                                .filter(Subtask::isCompleted)
                                .count()
                );

                return t;
            }

        } catch (SQLException e) {
            throw new RuntimeException("TaskDAO.findByIdAndUser failed", e);
        }
    }

    public Long create(
            Long userId,
            Long categoryId,
            String title,
            String description,
            LocalDate deadline
    ) {

        String sql =
                "INSERT INTO tasks "
                + "(user_id, category_id, title, description, deadline, "
                + "is_completed, is_pinned, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, false, false, now(), now()) "
                + "RETURNING id";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, userId);

            if (categoryId == null) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, categoryId);
            }

            ps.setString(3, title);

            ps.setString(
                    4,
                    (description == null || description.isBlank())
                            ? null
                            : description
            );

            if (deadline == null) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(deadline));
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("TaskDAO.create failed", e);
        }
    }

    public boolean update(
            Long id,
            Long userId,
            Long categoryId,
            String title,
            String description,
            LocalDate deadline
    ) {

        String sql =
                "UPDATE tasks SET "
                + "category_id = ?, "
                + "title = ?, "
                + "description = ?, "
                + "deadline = ?, "
                + "updated_at = now() "
                + "WHERE id = ? AND user_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (categoryId == null) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, categoryId);
            }

            ps.setString(2, title);

            ps.setString(
                    3,
                    (description == null || description.isBlank())
                            ? null
                            : description
            );

            if (deadline == null) {
                ps.setNull(4, Types.DATE);
            } else {
                ps.setDate(4, Date.valueOf(deadline));
            }

            ps.setLong(5, id);
            ps.setLong(6, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("TaskDAO.update failed", e);
        }
    }

    public boolean toggleCompleted(Long id, Long userId) {

        String sql =
                "UPDATE tasks SET "
                + "is_completed = NOT is_completed, "
                + "updated_at = now() "
                + "WHERE id = ? AND user_id = ?";

        return execToggle(id, userId, sql);
    }

    public boolean togglePinned(Long id, Long userId) {

        String sql =
                "UPDATE tasks SET "
                + "is_pinned = NOT is_pinned, "
                + "updated_at = now() "
                + "WHERE id = ? AND user_id = ?";

        return execToggle(id, userId, sql);
    }

    private boolean execToggle(Long id, Long userId, String sql) {

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("TaskDAO toggle failed", e);
        }
    }

    /**
     * Delete task.
     * Subtasks are deleted automatically by ON DELETE CASCADE.
     */
    public boolean delete(Long id, Long userId) {

        String sql =
                "DELETE FROM tasks WHERE id = ? AND user_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("TaskDAO.delete failed", e);
        }
    }

    /**
     * Convert database ResultSet into Task model.
     */
    private Task map(ResultSet rs) throws SQLException {

        Task t = new Task();

        t.setId(rs.getLong("id"));
        t.setUserId(rs.getLong("user_id"));

        long cid = rs.getLong("category_id");

        t.setCategoryId(
                rs.wasNull() ? null : cid
        );

        String cname = rs.getString("category_name");

        t.setCategoryName(cname);
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));

        Date d = rs.getDate("deadline");

        t.setDeadline(
                d == null ? null : d.toLocalDate()
        );

        t.setCompleted(rs.getBoolean("is_completed"));
        t.setPinned(rs.getBoolean("is_pinned"));

        t.setSubtaskTotal(rs.getInt("subtask_total"));
        t.setSubtaskCompleted(rs.getInt("subtask_done"));

        Timestamp ca = rs.getTimestamp("created_at");

        t.setCreatedAt(
                ca == null ? null : ca.toLocalDateTime()
        );

        Timestamp ua = rs.getTimestamp("updated_at");

        t.setUpdatedAt(
                ua == null ? null : ua.toLocalDateTime()
        );

        return t;
    }
}
