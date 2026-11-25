package com.tasking.controller;

import com.tasking.dao.UserDAO;
import com.tasking.model.User;
import com.tasking.util.PasswordUtil;
import com.tasking.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Password reset flow (forgot password + reset).
 *
 * Email delivery requires a configured SMTP service; tokens are stored in the
 * HTTP session as a documented fallback (see docs/database-compatibility.md).
 * If SMTP is configured in the container (mail resources), integrate a mail
 * sender here — placeholders are provided in .env-style db/mail properties.
 */
@WebServlet(name = "PasswordResetServlet", urlPatterns = {"/forgot-password", "/reset-password"})
public class PasswordResetServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String view = "/forgot-password".equals(req.getServletPath())
                ? "/WEB-INF/views/auth/forgot-password.jsp"
                : "/WEB-INF/views/auth/reset-password.jsp";
        req.getRequestDispatcher(view).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/forgot-password".equals(req.getServletPath())) {
            requestReset(req, resp);
        } else {
            performReset(req, resp);
        }
    }

    private void requestReset(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String email = req.getParameter("email");
        Map<String, String> errors = new HashMap<>();
        if (!ValidationUtil.required(email) || !ValidationUtil.email(email)) {
            errors.put("email", "Please enter a valid email address.");
            AuthServlet.fail(req, resp, "/WEB-INF/views/auth/forgot-password.jsp", errors, email, null);
            return;
        }

        UserDAO dao = new UserDAO();
        User user = dao.findByEmail(email.trim());
        // Always show success to avoid leaking account existence (matching Laravel behaviour).
        if (user != null) {
            String token = UUID.randomUUID().toString().replace("-", "");
            HttpSession session = req.getSession();
            session.setAttribute("resetToken", token);
            session.setAttribute("resetUserId", user.getId());
            session.setAttribute("resetRequestedAt", System.currentTimeMillis());

            // MAIL DELIVERY PLACEHOLDER:
            // Configure the container's mail session (MAIL_HOST etc.) and send
            // a message containing the reset link: /reset-password?token=<token>
            req.setAttribute("resetTokenDemo", token);
        }
        req.setAttribute("status", "reset-link-sent");
        req.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(req, resp);
    }

    private void performReset(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String token = req.getParameter("token");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmation = req.getParameter("password_confirmation");

        HttpSession session = req.getSession(false);
        Map<String, String> errors = new HashMap<>();

        Object storedToken = session == null ? null : session.getAttribute("resetToken");
        Object storedUser = session == null ? null : session.getAttribute("resetUserId");
        Long userId = storedUser instanceof Long l ? l : null;
        Object ts = session == null ? null : session.getAttribute("resetRequestedAt");
        boolean expired = ts instanceof Long t && (System.currentTimeMillis() - t) > 15 * 60 * 1000;

        if (storedToken == null || token == null || !storedToken.equals(token) || expired || userId == null) {
            errors.put("token", "This password reset token is invalid or has expired.");
        }
        if (!ValidationUtil.required(password)) errors.put("password", "The password field is required.");
        else if (password.length() < 8) errors.put("password", "The password must be at least 8 characters.");
        else if (!password.equals(confirmation)) errors.put("password", "The password confirmation does not match.");

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("resetEmail", email);
            req.setAttribute("resetTokenValue", token);
            req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
            return;
        }

        UserDAO dao = new UserDAO();
        User user = dao.findById(userId);
        if (user == null || (email != null && !email.isBlank()
                && !user.getEmail().equalsIgnoreCase(email.trim()))) {
            errors.put("token", "This password reset token is invalid or has expired.");
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
            return;
        }

        dao.updatePassword(userId, PasswordUtil.hash(password));
        session.removeAttribute("resetToken");
        session.removeAttribute("resetUserId");
        session.removeAttribute("resetRequestedAt");
        req.setAttribute("status", "password-reset");
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
    }
}
