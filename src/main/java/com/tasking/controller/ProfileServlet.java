package com.tasking.controller;

import com.tasking.dao.UserDAO;
import com.tasking.filter.AuthFilter;
import com.tasking.model.User;
import com.tasking.util.PasswordUtil;
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
 * Profile management: view/update info, change password, delete account.
 * Mirrors Laravel ProfileController + PasswordController.
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile", "/password"})
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = DashboardServlet.currentUser(req);
        com.tasking.dao.TaskDAO taskDAO = new com.tasking.dao.TaskDAO();
        int total = taskDAO.countByUser(user.getId());
        int completed = taskDAO.countCompletedByUser(user.getId());
        req.setAttribute("user", userDAO.findById(user.getId()));
        req.setAttribute("categories", new com.tasking.dao.CategoryDAO().findByUser(user.getId()));
        req.setAttribute("globalTotal", total);
        req.setAttribute("globalCompleted", completed);
        req.setAttribute("progress", total > 0 ? Math.round((completed * 100f) / total) : 0);
        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String override = req.getParameter("_method");
        if ("DELETE".equalsIgnoreCase(override)) {
            doDelete(req, resp);
            return;
        }
        if ("PUT".equalsIgnoreCase(override)) {
            doPut(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String path = req.getServletPath();
        if ("/profile".equals(path)) {
            updateProfile(req, resp);
        } else {
            updatePassword(req, resp);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        deleteAccount(req, resp);
    }

    private void updateProfile(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        User user = DashboardServlet.currentUser(req);
        String name = req.getParameter("name");
        String email = req.getParameter("email");

        Map<String, String> errors = new HashMap<>();
        if (!ValidationUtil.required(name)) errors.put("name", "The name field is required.");
        else if (!ValidationUtil.max(name.trim(), 255)) errors.put("name", "The name may not be greater than 255 characters.");
        if (!ValidationUtil.required(email)) errors.put("email", "The email field is required.");
        else if (!ValidationUtil.email(email)) errors.put("email", "The email must be a valid email address.");

        boolean emailChanged = false;
        if (errors.isEmpty()) {
            email = email.trim().toLowerCase();
            User existing = userDAO.findByEmail(email);
            emailChanged = existing == null || !existing.getId().equals(user.getId());
            if (existing != null && !existing.getId().equals(user.getId())) {
                errors.put("email", "The email has already been taken.");
            }
        }

        if (!errors.isEmpty()) {
            req.setAttribute("profileErrors", errors);
            doGet(req, resp);
            return;
        }

        userDAO.updateProfile(user.getId(), name.trim(), email, emailChanged);
        User fresh = userDAO.findById(user.getId());
        req.getSession().setAttribute(AuthFilter.SESSION_USER_KEY, fresh);
        resp.sendRedirect(req.getContextPath() + "/profile?status=profile-updated");
    }

    private void updatePassword(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        User user = DashboardServlet.currentUser(req);
        String current = req.getParameter("current_password");
        String next = req.getParameter("password");
        String confirmation = req.getParameter("password_confirmation");

        Map<String, String> errors = new HashMap<>();
        String hash = userDAO.getPasswordHash(user.getEmail());
        if (!PasswordUtil.verify(current, hash)) {
            errors.put("current_password", "The provided password does not match your current password.");
        }
        if (!ValidationUtil.required(next)) errors.put("password", "The password field is required.");
        else if (next.length() < 8) errors.put("password", "The password must be at least 8 characters.");
        else if (!next.equals(confirmation)) errors.put("password", "The password confirmation does not match.");

        if (!errors.isEmpty()) {
            req.setAttribute("passwordErrors", errors);
            doGet(req, resp);
            return;
        }

        userDAO.updatePassword(user.getId(), PasswordUtil.hash(next));
        resp.sendRedirect(req.getContextPath() + "/profile?status=password-updated");
    }

    private void deleteAccount(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        User user = DashboardServlet.currentUser(req);
        String password = req.getParameter("password");

        String hash = userDAO.getPasswordHash(user.getEmail());
        if (hash == null || !PasswordUtil.verify(password == null ? "" : password, hash)) {
            req.setAttribute("deleteError", "The provided password is incorrect.");
            doGet(req, resp);
            return;
        }

        // Cascades to categories, tasks, subtasks via FK constraints.
        userDAO.delete(user.getId());
        req.getSession().invalidate();
        resp.sendRedirect(req.getContextPath() + "/onboarding");
    }
}