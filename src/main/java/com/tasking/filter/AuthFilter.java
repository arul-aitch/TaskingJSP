package com.tasking.filter;

import com.tasking.util.CSRFUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authentication + CSRF filter.
 *
 * - Exposes the session CSRF token to views as request attribute "csrfToken".
 * - Rejects POST requests without a valid CSRF token.
 * - Enforces guest-only (/login, /register, /onboarding) and
 *   authenticated-only (everything under /dashboard, /tasks, /categories,
 *   /subtasks, /profile, /logout, /password) access rules.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = "/*")
public class AuthFilter implements Filter {

    private static final String SESSION_USER = "authUser";
    public static final String SESSION_USER_KEY = SESSION_USER;

    private static final String[] GUEST_ONLY = {"/login", "/register", "/onboarding"};
    private static final String[] AUTH_PAGES = {"/dashboard", "/tasks", "/categories", "/subtasks",
            "/profile", "/logout", "/password"};

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.isEmpty()) path = "/";

        // Static assets pass through untouched.
        if (path.startsWith("/assets/") || path.equals("/favicon.ico")) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(true);
        Object user = session.getAttribute(SESSION_USER);
        boolean loggedIn = user != null;

        // Expose CSRF token to all views.
        request.setAttribute(CSRFUtil.REQUEST_KEY, CSRFUtil.token(session));

        // CSRF check for state-changing requests (login/register included).
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            if (!CSRFUtil.isValid(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Invalid or missing CSRF token.");
                return;
            }
        }

        boolean guestOnly = matches(path, GUEST_ONLY);
        boolean authOnly = matches(path, AUTH_PAGES);

        // Laravel: "/" redirects authenticated users to the dashboard.
        if (loggedIn && (path.equals("/") || path.equals("/index.jsp"))) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        if (authOnly && !loggedIn) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (guestOnly && loggedIn && !"/onboarding".equals(path)) {
            // Logged-in users are redirected to the dashboard, matching Laravel's guest middleware + '/' route.
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean matches(String path, String[] prefixes) {
        for (String p : prefixes) {
            if (path.equals(p) || path.startsWith(p + "/")) return true;
        }
        return false;
    }
}
