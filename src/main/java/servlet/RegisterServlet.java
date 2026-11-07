package servlet;

import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import listener.AppContextListener;
import service.UserService;

/** GET /register shows the form; POST /register validates and creates the account. */
@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/register.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        showForm(req, resp, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        UserService service = (UserService) getServletContext()
                .getAttribute(AppContextListener.USER_SERVICE);

        Map<String, String> form = new LinkedHashMap<>();
        form.put("fullName", trim(req.getParameter("fullName")));
        form.put("email", trim(req.getParameter("email")));
        form.put("username", trim(req.getParameter("username")));
        form.put("studentId", trim(req.getParameter("studentId")));
        form.put("studentName", trim(req.getParameter("studentName")));
        form.put("className", trim(req.getParameter("className")));
        String password = req.getParameter("password");   // never trimmed, never echoed
        String confirm = req.getParameter("confirm");

        try {
            service.register(form.get("fullName"), form.get("email"), form.get("username"),
                    password, confirm, form.get("studentId"), form.get("studentName"),
                    form.get("className"));
            // Post/Redirect/Get: refreshing the page will not re-submit the form
            resp.sendRedirect(req.getContextPath() + "/login?registered=1");
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            showForm(req, resp, form);
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp,
                          Map<String, String> form) throws ServletException, IOException {
        UserService service = (UserService) getServletContext()
                .getAttribute(AppContextListener.USER_SERVICE);
        req.setAttribute("classNames", service.getFeeService().classNames());
        req.setAttribute("form", form == null ? new LinkedHashMap<String, String>() : form);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
