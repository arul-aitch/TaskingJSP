package com.tasking.controller;

import com.tasking.dao.CategoryDAO;
import com.tasking.model.User;
import com.tasking.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Category CRUD. Mirrors Laravel CategoryController.
 * All operations are ownership-checked (user_id).
 */
@WebServlet(name = "CategoryServlet", urlPatterns = {"/categories/*"})
public class CategoryServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String override = req.getParameter("_method");
        if (override != null) {
            switch (override.toUpperCase()) {
                case "PUT" -> { doPut(req, resp); return; }
                case "DELETE" -> { doDelete(req, resp); return; }
            }
        }
        if (req.getPathInfo() == null) {
            store(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        update(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        destroy(req, resp);
    }

    private void store(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = DashboardServlet.currentUser(req);
        Map<String, String> errors = new java.util.HashMap<>();
        String name = req.getParameter("name");
        if (!ValidationUtil.required(name)) {
            errors.put("name", "The name field is required.");
        } else if (!ValidationUtil.max(name.trim(), 255)) {
            errors.put("name", "The name may not be greater than 255 characters.");
        }
        if (errors.isEmpty()) {
            categoryDAO.create(user.getId(), name.trim());
        }
        resp.sendRedirect(ValidationUtil.backToDashboard(req));
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        User user = DashboardServlet.currentUser(req);
        Long id = DashboardServlet.parseLong(req.getPathInfo() == null ? null
                : req.getPathInfo().substring(1).split("/")[0]);
        String name = req.getParameter("name");
        boolean valid = ValidationUtil.required(name) && ValidationUtil.max(name.trim(), 255);
        if (id != null && valid) {
            // Ownership-checked update.
            categoryDAO.update(id, user.getId(), name.trim());
        }
        resp.sendRedirect(ValidationUtil.backToDashboard(req));
    }

    private void destroy(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = DashboardServlet.currentUser(req);
        Long id = DashboardServlet.parseLong(req.getPathInfo() == null ? null
                : req.getPathInfo().substring(1).split("/")[0]);
        if (id != null) {
            categoryDAO.delete(id, user.getId());
        }
        resp.sendRedirect(ValidationUtil.backToDashboard(req));
    }
}