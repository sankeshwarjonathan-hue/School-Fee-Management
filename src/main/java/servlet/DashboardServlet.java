package servlet;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import listener.AppContextListener;
import model.User;
import service.UserService;
import util.SessionKeys;

/**
 * GET /dashboard. AuthFilter has already confirmed that a session exists.
 * This servlet loads the data and forwards to the JSP (which sits in WEB-INF,
 * so it cannot be opened by typing its address).
 */
@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/dashboard.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserService service = (UserService) getServletContext()
                .getAttribute(AppContextListener.USER_SERVICE);
        HttpSession session = req.getSession(false);
        String username = (String) session.getAttribute(SessionKeys.USERNAME);

        User user = service.findByUsername(username);
        if (user == null) {                       // account no longer exists
            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login?error=auth");
            return;
        }
        if (user.isAdmin()) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard"); //Tomcat forwards the request internally to dashboard.jsp
            return;
        }

        req.setAttribute("user", user);
        req.setAttribute("sessionId", session.getId());
        req.setAttribute("sessionCreated", new java.util.Date(session.getCreationTime()));
        req.setAttribute("sessionTimeoutMinutes", session.getMaxInactiveInterval() / 60);

                if (!user.isAdmin()) {
            // Form token for the pay button (stops other sites from posting a payment)
            String token = (String) session.getAttribute(SessionKeys.CSRF_TOKEN);
            if (token == null) {
                token = UUID.randomUUID().toString();
                session.setAttribute(SessionKeys.CSRF_TOKEN, token);
            }
            req.setAttribute("csrfToken", token);
            req.setAttribute("paymentMethods", UserService.PAYMENT_METHODS);
            req.setAttribute("payments", service.findPayments(username));

            // One-time error list left by PaymentServlet
            Object payErrors = session.getAttribute(SessionKeys.PAY_ERRORS);
            if (payErrors != null) {
                req.setAttribute("payErrors", payErrors);
                session.removeAttribute(SessionKeys.PAY_ERRORS);
            }
        }

        if (user.isAdmin()) {
            List<User> parents = service.findAllParents();
            double total = 0;
            double paid = 0;
            for (User p : parents) {
                total += p.getStudent().getTotalFee();
                paid += p.getStudent().getAmountPaid();
            }
            req.setAttribute("parents", parents);
            req.setAttribute("sumTotal", total);
            req.setAttribute("sumPaid", paid);
        }
        req.getRequestDispatcher(VIEW).forward(req, resp); //dispacter used  navigate from one resource to another
    }
}
