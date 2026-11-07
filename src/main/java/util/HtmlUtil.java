package util;

import java.util.Locale;

/** Escapes text before it is written into a JSP page (prevents XSS). */
public final class HtmlUtil {
    private HtmlUtil() { }

    public static String esc(Object value) {
        if (value == null) {
            return "";
        }
        String s = value.toString();
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String money(double amount) {
        return String.format(Locale.ENGLISH, "Rs. %,.2f", amount);
    }

    /** Plain number with two decimals, e.g. 45000.00 (for form fields). */
    public static String plain(double amount) {
        return String.format(Locale.ENGLISH, "%.2f", amount);
    }
}