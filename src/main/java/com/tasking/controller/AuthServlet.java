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
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Registration, login, logout and onboarding view.
 * Mirrors Laravel Breeze: RegisteredUserController, AuthenticatedSessionController.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/onboarding", "/register", "/login", "/logout"})
public class AuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/onboarding" -> forward(req, resp, "/WEB-INF/views/onboarding.jsp");
            case "/register" -> forward(req, resp, "/WEB-INF/views/auth/register.jsp");
            case "/login" -> forward(req, resp, "/WEB-INF/views/auth/login.jsp");
            default -> resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/register" -> register(req, resp);
            case "/login" -> login(req, resp);
            case "/logout" -> logout(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void register(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String name = trim(req.getParameter("name"));
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password");
        String confirmation = req.getParameter("password_confirmation");

        Map<String, String> errors = new HashMap<>();
        if (!ValidationUtil.required(name)) errors.put("name", "The name field is required.");
        else if (!ValidationUtil.max(name, 255)) errors.put("name", "The name may not be greater than 255 characters.");
        if (!ValidationUtil.required(email)) errors.put("email", "The email field is required.");
        else if (!ValidationUtil.email(email)) errors.put("email", "The email must be a valid email address.");
        if (!ValidationUtil.required(password)) errors.put("password", "The password field is required.");
        else if (password.length() < 8) errors.put("password", "The password must be at least 8 characters.");
        else if (!password.equals(confirmation)) errors.put("password", "The password confirmation does not match.");

        UserDAO dao = new UserDAO();
        if (errors.isEmpty() && dao.emailExists(email, null)) {
            errors.put("email", "The email has already been taken.");
        }

        if (!errors.isEmpty()) {
            fail(req, resp, "/WEB-INF/views/auth/register.jsp", errors, name, email);
            return;
        }

        Long id = dao.create(name, email.toLowerCase(), PasswordUtil.hash(password));
        User user = dao.findById(id);
        loginSession(req, user);
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }

    private void login(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password");
        boolean remember = req.getParameter("remember") != null;

        Map<String, String> errors = new HashMap<>();
        if (!ValidationUtil.required(email)) errors.put("email", "The email field is required.");
        if (!ValidationUtil.required(password)) errors.put("password", "The password field is required.");

        // Simple in-session login throttle (rate limiting), mirroring Laravel's LoginRequest throttle.
        HttpSession session = req.getSession();
        int attempts = 0;
        Object a = session.getAttribute("loginAttempts");
        if (a instanceof Integer i) attempts = i;

        UserDAO dao = new UserDAO();
        if (errors.isEmpty()) {
            if (attempts >= 5) {
                errors.put("email", "Too many login attempts. Please try again in one minute.");
            } else {
                String hash = dao.getPasswordHash(email);
                if (hash != null && PasswordUtil.verify(password, hash)) {
                    // Session fixation protection: regenerate id after successful login.
                    req.changeSessionId();
                    session.invalidate();
                    session = req.getSession(true);
                    User user = dao.findById(dao.findByEmail(email).getId());
                    session.setAttribute(AuthFilter.SESSION_USER_KEY, user);
                    session.setAttribute("loginAttempts", 0);
                    if (remember) {
                        // Remember-me is honored for the browser session lifetime via a cookie hint.
                        jakarta.servlet.http.Cookie c = new jakarta.servlet.http.Cookie("tasking_remember", "1");
                        c.setMaxAge(60 * 60 * 24 * 14);
                        c.setHttpOnly(true);
                        c.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
                        resp.addCookie(c);
                    }
                    resp.sendRedirect(req.getContextPath() + "/dashboard");
                    return;
                }
                session.setAttribute("loginAttempts", attempts + 1);
                errors.put("email", "These credentials do not match our records.");
            }
        }

        fail(req, resp, "/WEB-INF/views/auth/login.jsp", errors, email, null);
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        jakarta.servlet.http.Cookie c = new jakarta.servlet.http.Cookie("tasking_remember", "");
        c.setMaxAge(0);
        c.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        resp.addCookie(c);
        resp.sendRedirect(req.getContextPath() + "/onboarding");
    }

    static void loginSession(HttpServletRequest req, User user) {
        HttpSession session = req.getSession(true);
        session.setAttribute(AuthFilter.SESSION_USER_KEY, user);
        session.setAttribute("loginAttempts", 0);
    }

    static void fail(HttpServletRequest req, HttpServletResponse resp, String view,
                     Map<String, String> errors, String oldName, String oldEmail)
            throws ServletException, IOException {
        req.setAttribute("errors", errors);
        if (oldName != null) req.setAttribute("oldName", oldName);
        if (oldEmail != null) req.setAttribute("oldEmail", oldEmail);
        req.getRequestDispatcher(view).forward(req, resp);
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher(view).forward(req, resp);
    }
}
