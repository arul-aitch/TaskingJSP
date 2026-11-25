package com.tasking.controller;

import com.tasking.dao.SubtaskDAO;
import com.tasking.model.User;
import com.tasking.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "SubtaskServlet", urlPatterns = {"/subtasks/*"})
@MultipartConfig
public class SubtaskServlet extends HttpServlet {

    private final SubtaskDAO subtaskDAO = new SubtaskDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String override = req.getParameter("_method");
        if ("DELETE".equalsIgnoreCase(override)) {
            doDelete(req, resp);
            return;
        }
        if ("PATCH".equalsIgnoreCase(override)) {
            doPatch(req, resp);
            return;
        }
        
        Long taskId = DashboardServlet.parseLong(req.getPathInfo() == null ? null
                : req.getPathInfo().substring(1));
        User user = DashboardServlet.currentUser(req);
        String title = req.getParameter("title");

        boolean ownsTask = taskId != null && new com.tasking.dao.TaskDAO()
                .findByIdAndUser(taskId, user.getId()) != null;
        boolean valid = ValidationUtil.required(title) && ValidationUtil.max(title.trim(), 255);

        if (ownsTask && valid) {
            subtaskDAO.create(taskId, title.trim());
        }
        resp.sendRedirect(ValidationUtil.backToDashboard(req));
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String pathInfo = req.getPathInfo();
        String[] parts = pathInfo == null ? new String[0] : pathInfo.substring(1).split("/");
        if (parts.length == 2 && "toggle".equals(parts[1])) {
            Long subtaskId = DashboardServlet.parseLong(parts[0]);
            User user = DashboardServlet.currentUser(req);
            if (subtaskId != null && subtaskDAO.findOwnedTaskId(subtaskId, user.getId()) != null) {
                subtaskDAO.toggleCompleted(subtaskId, user.getId());
            }
            resp.sendRedirect(ValidationUtil.backToDashboard(req));
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Long subtaskId = DashboardServlet.parseLong(req.getPathInfo() == null ? null
                : req.getPathInfo().substring(1));
        User user = DashboardServlet.currentUser(req);
        if (subtaskId != null && subtaskDAO.findOwnedTaskId(subtaskId, user.getId()) != null) {
            subtaskDAO.delete(subtaskId, user.getId());
        }
        resp.sendRedirect(ValidationUtil.backToDashboard(req));
    }
}