package servlet;

import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import listener.AppContextListener;
import model.User;
import service.UserService;
import util.SessionKeys;

/** GET /login shows the form; POST /login checks credentials and starts a session. */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/login.jsp";
    private static final int SESSION_TIMEOUT_SECONDS = 15 * 60;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession existing = req.getSession(false);
        if (existing != null && existing.getAttribute(SessionKeys.USERNAME) != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");   // already logged in
            return;
        }
        if ("1".equals(req.getParameter("registered"))) {
            req.setAttribute("info", "Registration successful. Please log in.");
        } else if ("1".equals(req.getParameter("loggedout"))) {
            req.setAttribute("info", "You have been logged out.");
        } else if ("auth".equals(req.getParameter("error"))) {
            req.setAttribute("warning", "Please log in to open the dashboard.");
        }
        noCache(resp);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        UserService service = (UserService) getServletContext()
                .getAttribute(AppContextListener.USER_SERVICE);

        String username = req.getParameter("username") == null ? "" : req.getParameter("username").trim();
        String password = req.getParameter("password");

        try {
            User user = service.authenticate(username, password);      // server-side check
            if (user.isAdmin()) {
                req.setAttribute("errors", java.util.List.of("Administrators must use the admin login page."));
                req.setAttribute("username", username);
                noCache(resp);
                req.getRequestDispatcher(VIEW).forward(req, resp);
                return;
            }

            // Session-fixation defence: throw away any old session, start a new one
            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = req.getSession(true);
            session.setAttribute(SessionKeys.USERNAME, user.getUsername());
            session.setAttribute(SessionKeys.FULL_NAME, user.getFullName());
            session.setAttribute(SessionKeys.ROLE, user.getRole());
            session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("username", username);
            noCache(resp);
            req.getRequestDispatcher(VIEW).forward(req, resp);
        }
    }

    private static void noCache(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
    }
}
