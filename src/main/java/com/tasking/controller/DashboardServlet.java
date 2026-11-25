package com.tasking.controller;

import com.tasking.dao.CategoryDAO;
import com.tasking.dao.TaskDAO;
import com.tasking.filter.AuthFilter;
import com.tasking.model.User;
import com.tasking.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard: task list with search / category filter / archive / sorting,
 * plus progress counters. Mirrors the Laravel /dashboard closure.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        Long userId = user.getId();

        CategoryDAO categoryDAO = new CategoryDAO();
        TaskDAO taskDAO = new TaskDAO();

        req.setAttribute("user", user);
        req.setAttribute("categories", categoryDAO.findByUser(userId));

        int globalTotal = taskDAO.countByUser(userId);
        int globalCompleted = taskDAO.countCompletedByUser(userId);
        int progress = globalTotal > 0 ? Math.round((globalCompleted * 100f) / globalTotal) : 0;
        req.setAttribute("globalTotal", globalTotal);
        req.setAttribute("globalCompleted", globalCompleted);
        req.setAttribute("progress", progress);

        Long categoryId = parseLong(req.getParameter("category"));
        if (categoryId != null) {
            // Only accept a category that belongs to this user (authorization).
            if (!categoryDAO.existsForUser(categoryId, userId)) {
                categoryId = null;
            }
        }
        String search = req.getParameter("search");
        boolean archive = req.getParameter("archive") != null && !req.getParameter("archive").isBlank();
        boolean newest = "newest".equals(req.getParameter("sort"));

        req.setAttribute("tasks", taskDAO.findForDashboard(userId, categoryId, search, archive, newest));
        req.setAttribute("isArchive", archive);
        req.setAttribute("selectedCategory", categoryId);
        req.setAttribute("searchValue", search == null ? "" : search);
        req.setAttribute("sortValue", req.getParameter("sort") == null ? "deadline" : req.getParameter("sort"));

        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }

    static User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute(AuthFilter.SESSION_USER_KEY);
    }

    static Long parseLong(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static Map<String, String> singleError(String key, String message) {
        Map<String, String> m = new HashMap<>();
        m.put(key, message);
        return m;
    }
}