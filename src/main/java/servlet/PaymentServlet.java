package servlet;

import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import listener.AppContextListener;
import model.Payment;
import service.UserService;
import util.SessionKeys;

/** POST /pay: validates the amount, records the payment and sends the parent to the receipt. */
@WebServlet(name = "PaymentServlet", urlPatterns = "/pay")
public class PaymentServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        UserService service = (UserService) getServletContext()
                .getAttribute(AppContextListener.USER_SERVICE);
        HttpSession session = req.getSession(false);   // AuthFilter guarantees it exists
        String username = (String) session.getAttribute(SessionKeys.USERNAME);

        // Form-token check
        String expected = (String) session.getAttribute(SessionKeys.CSRF_TOKEN);
        String sent = req.getParameter("csrf");
        if (expected == null || sent == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), sent.getBytes(StandardCharsets.UTF_8))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid form token");
            return;
        }

        try {
            Payment p = service.makePayment(username, req.getParameter("amount"),
                    req.getParameter("method"));
            resp.sendRedirect(req.getContextPath() + "/receipt?no=" + p.getReceiptNo());
        } catch (ValidationException e) {
            session.setAttribute(SessionKeys.PAY_ERRORS, e.getErrors());
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        }
    }
}