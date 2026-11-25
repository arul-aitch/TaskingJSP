package com.tasking.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * CSRF protection for state-changing (POST) requests.
 *
 * A token is generated per session. Forms embed it as the "csrf_token" hidden
 * field; every POST handler validates it via {@link #isValid(HttpServletRequest)}.
 * Views read it from the request attribute "csrfToken" (set by AuthFilter).
 */
public final class CSRFUtil {

    public static final String SESSION_KEY = "csrfToken";
    public static final String PARAM_NAME = "csrf_token";
    public static final String REQUEST_KEY = "csrfToken";

    private CSRFUtil() {
    }

    /** Get (or lazily create) the token for this session. */
    public static String token(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_KEY);
        if (token == null || token.isBlank()) {
            token = java.util.UUID.randomUUID().toString().replace("-", "");
            session.setAttribute(SESSION_KEY, token);
        }
        return token;
    }

    /** Validate the submitted token for a POST request. */
    public static boolean isValid(HttpServletRequest request) {
        String submitted = request.getParameter(PARAM_NAME);
        HttpSession session = request.getSession(false);
        if (submitted == null || session == null) {
            return false;
        }
        String expected = (String) session.getAttribute(SESSION_KEY);
        if (expected == null) {
            return false;
        }
        return java.security.MessageDigest.isEqual(
                submitted.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                expected.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
