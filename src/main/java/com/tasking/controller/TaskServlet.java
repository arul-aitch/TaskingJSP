package com.tasking.controller;

import com.tasking.dao.CategoryDAO;
import com.tasking.dao.TaskDAO;
import com.tasking.model.User;
import com.tasking.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "TaskServlet", urlPatterns = {"/tasks/*"})
@MultipartConfig
public class TaskServlet extends HttpServlet {

    private final TaskDAO taskDAO = new TaskDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String pathInfo = req.getPathInfo();
        String override = req.getParameter("_method");
        if (override != null) {
            switch (override.toUpperCase()) {
                case "PATCH" -> { doPatch(req, resp); return; }
                case "PUT" -> { doPut(req, resp); return; }
                case "DELETE" -> { doDelete(req, resp); return; }
            }
        }
        if ("/tasks".equals(req.getServletPath()) && pathInfo == null) {
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

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String[] parts = pathInfo.substring(1).split("/");
        if (parts.length == 2) {
            Long id = DashboardServlet.parseLong(parts[0]);
            if (id == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if ("toggle".equals(parts[1])) {
                toggle(req, id, resp);
            } else if ("pin".equals(parts[1])) {
                pin(req, id, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        destroy(req, resp);
    }

    private void store(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        User user = DashboardServlet.currentUser(req);
        Map<String, String> errors = validate(req);
        Long categoryId = resolveCategory(req, user, errors);

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            resp.sendRedirect(ValidationUtil.backToDashboard(req));
            return;
        }

        taskDAO.create(user.getId(), categoryId,
                req.getParameter("title").trim(),
                req.getParameter("description"),
                ValidationUtil.date(req.getParameter("deadline")));
        resp.sendRedirect(ValidationUtil.backToDashboard(req));
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        User user = DashboardServlet.currentUser(req);
        Long id = idFrom(req);
        if (id == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }

        Map<String, String> errors = validate(req);
        Long categoryId = resolveCategory(req, user, errors);
        if (errors.isEmpty() && taskDAO.findByIdAndUser(id, user.getId()) == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!errors.isEmpty()) {
            resp.sendRedirect(ValidationUtil.backToDashboard(req));
            return;
        }
        taskDAO.update(id, user.getId(), categoryId,
                req.getParameter("title").trim(),
                req.getParameter("description"),
                ValidationUtil.date(req.getParameter("deadline")));
        resp.sendRedirect(ValidationUtil.backToDashboard(req));
    }

    private void toggle(HttpServletRequest req, Long id, HttpServletResponse resp)
            throws IOException, ServletException {
        User user = DashboardServlet.currentUser(req);
        if (taskDAO.findByIdAndUser(id, user.getId()) == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        taskDAO.toggleCompleted(id, user.getId());
        resp.sendRedirect(ValidationUtil.backToDashboard(req));
    }

    private void pin(HttpServletRequest req, Long id, HttpServletResponse resp)
            throws IOException, ServletException {
        User user = DashboardServlet.currentUser(req);
        if (taskDAO.findByIdAndUser(id, user.getId()) == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        taskDAO.togglePinned(id, user.getId());
        resp.sendRedirect(ValidationUtil.backToDashboard(req));
    }

    private void destroy(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = DashboardServlet.currentUser(req);
        Long id = idFrom(req);
        if (id == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        taskDAO.delete(id, user.getId());
        resp.sendRedirect(ValidationUtil.backToDashboard(req));
    }

    private Long idFrom(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) return null;
        String idStr = pathInfo.substring(1).split("/")[0];
        return DashboardServlet.parseLong(idStr);
    }

    private Map<String, String> validate(HttpServletRequest req) {
        Map<String, String> errors = new HashMap<>();
        String title = req.getParameter("title");
        if (!ValidationUtil.required(title)) {
            errors.put("title", "The title field is required.");
        } else if (!ValidationUtil.max(title.trim(), 255)) {
            errors.put("title", "The title may not be greater than 255 characters.");
        }
        String deadline = req.getParameter("deadline");
        if (ValidationUtil.required(deadline) && ValidationUtil.date(deadline) == null) {
            errors.put("deadline", "The deadline must be a valid date.");
        }
        return errors;
    }

    private Long resolveCategory(HttpServletRequest req, User user, Map<String, String> errors) {
        Long categoryId = DashboardServlet.parseLong(req.getParameter("category_id"));
        if (categoryId != null && !categoryDAO.existsForUser(categoryId, user.getId())) {
            errors.put("category_id", "The selected category is invalid.");
            return null;
        }
        return categoryId;
    }
}