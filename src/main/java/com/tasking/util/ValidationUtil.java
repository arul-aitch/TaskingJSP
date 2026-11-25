package com.tasking.util;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple server-side validation helpers, mirroring the Laravel validation
 * rules used by the original controllers.
 */
public final class ValidationUtil {

    private ValidationUtil() {
    }

    public static boolean required(String v) {
        return v != null && !v.isBlank();
    }

    public static boolean max(String v, int max) {
        return v != null && v.length() <= max;
    }

    /** Loose email format check (name@domain.tld). */
    public static boolean email(String v) {
        if (v == null) return false;
        return v.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") && v.length() <= 255;
    }

    /** ISO date (yyyy-MM-dd) as produced by <input type="date">. */
    public static LocalDate date(String v) {
        if (!required(v)) return null;
        try {
            return LocalDate.parse(v, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Returns the correct dashboard URL preserving search/category/sort/archive filters,
     * used for Post/Redirect/Get responses after task operations.
     */
    public static String backToDashboard(HttpServletRequest request) {
        Map<String, String[]> p = request.getParameterMap();
        StringBuilder sb = new StringBuilder(request.getContextPath() + "/dashboard");
        boolean first = true;
        for (String key : new String[]{"search", "category", "sort", "archive"}) {
            String[] vals = p.get(key);
            if (vals != null && vals.length > 0 && !vals[0].isBlank()) {
                sb.append(first ? '?' : '&').append(key).append('=')
                  .append(java.net.URLEncoder.encode(vals[0], java.nio.charset.StandardCharsets.UTF_8));
                first = false;
            }
        }
        return sb.toString();
    }

    /** Escape a value for safe HTML output (defense-in-depth; JSTL c:out also escapes). */
    public static String escape(String v) {
        if (v == null) return "";
        return v.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /** Small helper for controllers building error maps. */
    public static Map<String, String> errors(String... pairs) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            m.put(pairs[i], pairs[i + 1]);
        }
        return m;
    }
}
