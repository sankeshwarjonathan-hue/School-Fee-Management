package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;
import util.SessionKeys;

/**
 * Guards every protected URL. A request without a logged-in session is
 * redirected to the login page before the servlet or JSP can run.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {
    "/dashboard", "/dashboard/*", "/pay", "/receipt",
    "/admin/dashboard", "/admin/student/save", "/admin/student/delete"
})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Do not create a session just to check it: getSession(false)
        HttpSession session = req.getSession(false);
        String path = req.getServletPath();
        boolean adminPath = path.startsWith("/admin/");
        boolean correctRole = session != null && (adminPath
                ? User.ROLE_ADMIN.equals(session.getAttribute(SessionKeys.ROLE))
                : !"/pay".equals(path) || User.ROLE_PARENT.equals(session.getAttribute(SessionKeys.ROLE)));
        boolean loggedIn = session != null
                && session.getAttribute(SessionKeys.USERNAME) != null && correctRole;

        // Stop the browser caching protected pages (Back button after logout)
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        if (!loggedIn) {
            resp.sendRedirect(req.getContextPath() + (adminPath ? "/admin/login" : "/login?error=auth"));
            return;
        }
        chain.doFilter(request, response);
    }
    
}
