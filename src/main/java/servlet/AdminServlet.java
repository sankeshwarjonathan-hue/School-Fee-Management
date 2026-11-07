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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import listener.AppContextListener;
import model.Student;
import model.User;
import service.UserService;
import util.SessionKeys;

@WebServlet(name = "AdminServlet", urlPatterns = {
    "/admin/login", "/admin/dashboard", "/admin/student/save", "/admin/student/delete"
})
public class AdminServlet extends HttpServlet {
    private static final String LOGIN_VIEW = "/WEB-INF/views/admin-login.jsp";
    private static final String DASHBOARD_VIEW = "/WEB-INF/views/admin-dashboard.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        noCache(resp);
        switch (req.getServletPath()) {
            case "/admin/login":
                HttpSession session = req.getSession(false);
                if (isAdmin(session)) {
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                    return;
                }
                if ("1".equals(req.getParameter("loggedout"))) {
                    req.setAttribute("info", "You have been logged out.");
                }
                req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
                break;
            case "/admin/dashboard":
                renderDashboard(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        noCache(resp);
        switch (req.getServletPath()) {
            case "/admin/login":
                login(req, resp);
                break;
            case "/admin/student/save":
                saveStudent(req, resp);
                break;
            case "/admin/student/delete":
                deleteStudent(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = trim(req.getParameter("username"));
        String password = req.getParameter("password");
        UserService service = userService();
        try {
            User user = service.authenticate(username, password);
            if (!user.isAdmin()) {
                req.setAttribute("errors", List.of("Invalid admin username or password."));
                req.setAttribute("username", username);
                req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
                return;
            }

            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = req.getSession(true);
            session.setAttribute(SessionKeys.USERNAME, user.getUsername());
            session.setAttribute(SessionKeys.FULL_NAME, user.getFullName());
            session.setAttribute(SessionKeys.ROLE, user.getRole());
            session.setAttribute(SessionKeys.ADMIN_CSRF_TOKEN, UUID.randomUUID().toString());
            session.setMaxInactiveInterval(15 * 60);
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } catch (ValidationException e) {
            req.setAttribute("errors", List.of("Invalid admin username or password."));
            req.setAttribute("username", username);
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
        }
    }

    private void saveStudent(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!validCsrf(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid form token");
            return;
        }
        Map<String, String> form = new LinkedHashMap<>();
        form.put("username", trim(req.getParameter("username")));
        form.put("parentFullName", trim(req.getParameter("parentFullName")));
        form.put("parentEmail", trim(req.getParameter("parentEmail")));
        form.put("studentName", trim(req.getParameter("studentName")));
        form.put("className", trim(req.getParameter("className")));
        form.put("totalFee", trim(req.getParameter("totalFee")));
        form.put("amountPaid", trim(req.getParameter("amountPaid")));
        String existingUsername = trim(req.getParameter("existingUsername"));

        try {
            userService().saveAdminStudentRecord(form.get("username"), form.get("parentFullName"),
                    form.get("parentEmail"), req.getParameter("parentPassword"),
                    form.get("studentName"), form.get("className"), form.get("totalFee"),
                    form.get("amountPaid"), existingUsername);
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?saved=1");
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("form", form);
            req.setAttribute("editingUsername", existingUsername);
            renderDashboard(req, resp);
        }
    }

    private void deleteStudent(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!validCsrf(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid form token");
            return;
        }
        try {
            userService().deleteAdminStudentRecord(req.getParameter("username"));
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?deleted=1");
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            renderDashboard(req, resp);
        }
    }

    private void renderDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!isAdmin(session)) {
            resp.sendRedirect(req.getContextPath() + "/admin/login");
            return;
        }

        UserService service = userService();
        List<User> parents = service.findAllParents();
        double total = 0;
        double paid = 0;
        Map<String, Integer> paymentCounts = new LinkedHashMap<>();
        for (User parent : parents) {
            Student student = parent.getStudent();
            total += student.getTotalFee();
            paid += student.getAmountPaid();
            paymentCounts.put(parent.getUsername(), service.findPayments(parent.getUsername()).size());
        }
        req.setAttribute("parents", parents);
        req.setAttribute("sumTotal", total);
        req.setAttribute("sumPaid", paid);
        req.setAttribute("paymentCounts", paymentCounts);
        req.setAttribute("csrfToken", session.getAttribute(SessionKeys.ADMIN_CSRF_TOKEN));
        req.setAttribute("classNames", service.getFeeService().classNames());

        String editUsername = trim(req.getParameter("edit"));
        if (req.getAttribute("editingUsername") != null) {
            editUsername = (String) req.getAttribute("editingUsername");
        }
        Object formValue = req.getAttribute("form");
        if (formValue instanceof Map<?, ?>) {
            req.setAttribute("editingUsername", editUsername);
        } else if (!editUsername.isEmpty()) {
            User editing = service.findByUsername(editUsername);
            if (editing == null || editing.isAdmin() || editing.getStudent() == null) {
                req.setAttribute("errors", List.of("The student record no longer exists."));
                editUsername = "";
            } else {
                Student student = editing.getStudent();
                Map<String, String> form = new LinkedHashMap<>();
                form.put("username", editing.getUsername());
                form.put("parentFullName", editing.getFullName());
                form.put("parentEmail", editing.getEmail());
                form.put("studentName", student.getStudentName());
                form.put("className", student.getClassName());
                form.put("totalFee", Double.toString(student.getTotalFee()));
                form.put("amountPaid", Double.toString(student.getAmountPaid()));
                req.setAttribute("form", form);
                req.setAttribute("editingUsername", editing.getUsername());
            }
        }
        if ("1".equals(req.getParameter("saved"))) {
            req.setAttribute("success", "Student fee record saved.");
        } else if ("1".equals(req.getParameter("deleted"))) {
            req.setAttribute("success", "Student record deleted. Historical payments and receipts were retained.");
        }
        req.getRequestDispatcher(DASHBOARD_VIEW).forward(req, resp);
    }

    private boolean validCsrf(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        String expected = session == null ? null
                : (String) session.getAttribute(SessionKeys.ADMIN_CSRF_TOKEN);
        String sent = req.getParameter("csrf");
        return expected != null && sent != null && MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), sent.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isAdmin(HttpSession session) {
        return session != null && User.ROLE_ADMIN.equals(session.getAttribute(SessionKeys.ROLE));
    }

    private UserService userService() {
        return (UserService) getServletContext().getAttribute(AppContextListener.USER_SERVICE);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static void noCache(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
    }
}
