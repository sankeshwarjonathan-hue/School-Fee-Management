package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** POST /logout destroys the session. (GET is accepted too, so a plain link also works.) */
@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);   // do not create a new one
        boolean admin = session != null
                && "ADMIN".equals(session.getAttribute("role"));
        if (session != null) {
            session.invalidate();                      // removes all attributes
        }
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.sendRedirect(req.getContextPath()
                + (admin ? "/admin/login?loggedout=1" : "/login?loggedout=1"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }
}
