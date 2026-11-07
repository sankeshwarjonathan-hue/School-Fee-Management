package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import listener.AppContextListener;
import model.Payment;
import model.User;
import service.UserService;
import util.SessionKeys;

/** GET /receipt?no=RCPT-... shows a printable receipt. Parents see only their own. */
@WebServlet(name = "ReceiptServlet", urlPatterns = "/receipt")
public class ReceiptServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/receipt.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserService service = (UserService) getServletContext()
                .getAttribute(AppContextListener.USER_SERVICE);
        HttpSession session = req.getSession(false);
        String username = (String) session.getAttribute(SessionKeys.USERNAME);
        boolean admin = User.ROLE_ADMIN.equals(session.getAttribute(SessionKeys.ROLE));

        Payment payment = service.findPayment(req.getParameter("no"));
        // Same 404 for "does not exist" and "belongs to someone else"
        if (payment == null || (!admin && !payment.getUsername().equalsIgnoreCase(username))) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("payment", payment);
        req.setAttribute("parent", service.findByUsername(payment.getUsername()));
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}