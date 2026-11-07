<%@ page contentType="text/html;charset=UTF-8" language="java" session="true" %>
<%@ page import="java.util.List, java.util.Map, model.User, model.Student, util.HtmlUtil" %>
<%
    Map<String, String> form = (Map<String, String>) request.getAttribute("form");
    if (form == null) {
        form = java.util.Collections.emptyMap();
    }
    String editingUsername = (String) request.getAttribute("editingUsername");
    boolean editing = editingUsername != null && !editingUsername.isEmpty();
    List<User> parents = (List<User>) request.getAttribute("parents");
    List<String> errors = (List<String>) request.getAttribute("errors");
    Map<String, Integer> paymentCounts = (Map<String, Integer>) request.getAttribute("paymentCounts");
    List<String> classes = (List<String>) request.getAttribute("classNames");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin Dashboard - School Fee Management</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="topbar">
    <strong>School Fee &amp; Finance - Admin</strong>
    <span><%= HtmlUtil.esc(session.getAttribute("fullName")) %>
        <span class="badge">ADMIN</span>
    </span>
    <form method="post" action="<%= request.getContextPath() %>/logout">
        <button type="submit" class="btn small">Log out</button>
    </form>
</header>

<main class="wrap admin-wrap">
    <div class="admin-heading">
        <div>
            <h1>Student fee records</h1>
            <p class="muted">Add, edit and manage student fee balances.</p>
        </div>
        <a class="btn admin-add-link" href="<%= request.getContextPath() %>/admin/dashboard#student-form">Add student record</a>
    </div>

    <% if (request.getAttribute("success") != null) { %>
        <div class="alert ok" role="status"><%= HtmlUtil.esc(request.getAttribute("success")) %></div>
    <% } %>
    <% if (errors != null) { %>
        <div class="alert error" role="alert">
            <% for (String error : errors) { %><div><%= HtmlUtil.esc(error) %></div><% } %>
        </div>
    <% } %>

    <section class="stats">
        <div class="stat"><span>Student records</span><strong><%= parents.size() %></strong></div>
        <div class="stat"><span>Total fees</span><strong><%= HtmlUtil.money((Double) request.getAttribute("sumTotal")) %></strong></div>
        <div class="stat"><span>Collected</span><strong><%= HtmlUtil.money((Double) request.getAttribute("sumPaid")) %></strong></div>
        <div class="stat"><span>Outstanding</span><strong><%= HtmlUtil.money((Double) request.getAttribute("sumTotal") - (Double) request.getAttribute("sumPaid")) %></strong></div>
    </section>

    <section class="card admin-table-card">
        <h2>All student records</h2>
        <% if (parents.isEmpty()) { %>
            <p class="muted">No student records yet. Add the first record using the form below.</p>
        <% } else { %>
        <div class="table-scroll">
            <table class="grid admin-table">
                <thead>
                <tr>
                    <th>Student</th><th>Class</th><th>Parent username</th>
                    <th>Total fee</th><th>Paid</th><th>Balance</th><th>Payments</th><th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <% for (User parent : parents) {
                       Student student = parent.getStudent();
                       int paymentCount = paymentCounts.getOrDefault(parent.getUsername(), 0);
                %>
                    <tr>
                        <td><strong><%= HtmlUtil.esc(student.getStudentName()) %></strong>
                            <span class="subline">ID: <%= HtmlUtil.esc(student.getStudentId()) %></span>
                        </td>
                        <td><%= HtmlUtil.esc(student.getClassName()) %></td>
                        <td><%= HtmlUtil.esc(parent.getUsername()) %>
                            <span class="subline"><%= HtmlUtil.esc(parent.getFullName()) %></span>
                        </td>
                        <td><%= HtmlUtil.money(student.getTotalFee()) %></td>
                        <td><%= HtmlUtil.money(student.getAmountPaid()) %></td>
                        <td><%= HtmlUtil.money(student.getBalance()) %></td>
                        <td><%= paymentCount %></td>
                        <td>
                            <div class="admin-actions">
                                <a class="btn small" href="<%= request.getContextPath() %>/admin/dashboard?edit=<%= HtmlUtil.esc(parent.getUsername()) %>#student-form">Edit</a>
                                <form method="post" action="<%= request.getContextPath() %>/admin/student/delete"
                                      onsubmit="return confirm('Delete this student and parent login? Historical payments and receipts will be retained.');">
                                    <input type="hidden" name="csrf" value="<%= HtmlUtil.esc(request.getAttribute("csrfToken")) %>">
                                    <input type="hidden" name="username" value="<%= HtmlUtil.esc(parent.getUsername()) %>">
                                    <button type="submit" class="btn small danger">Delete</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
        <% } %>
    </section>

    <section class="card admin-form-card" id="student-form">
        <h2><%= editing ? "Edit student fee record" : "Add student fee record" %></h2>
        <p class="muted">Student IDs are generated automatically. Existing payment receipts are kept when a record is edited.</p>
        <form method="post" action="<%= request.getContextPath() %>/admin/student/save">
            <input type="hidden" name="csrf" value="<%= HtmlUtil.esc(request.getAttribute("csrfToken")) %>">
            <input type="hidden" name="existingUsername" value="<%= editing ? HtmlUtil.esc(editingUsername) : "" %>">

            <label for="studentName">Student name</label>
            <input id="studentName" name="studentName" type="text" minlength="3" maxlength="50"
                   value="<%= HtmlUtil.esc(form.get("studentName")) %>" required>

            <div class="row">
                <div>
                    <label for="className">Class / Grade</label>
                    <select id="className" name="className" required>
                        <option value="">Select class</option>
                        <% for (String className : classes) { %>
                            <option value="<%= HtmlUtil.esc(className) %>"
                                <%= className.equals(form.get("className")) ? "selected" : "" %>>
                                <%= HtmlUtil.esc(className) %>
                            </option>
                        <% } %>
                    </select>
                </div>
                <div>
                    <label for="totalFee">Total fee (Rs.)</label>
                    <input id="totalFee" name="totalFee" type="number" min="0.01" step="0.01"
                           value="<%= HtmlUtil.esc(form.get("totalFee")) %>" required>
                </div>
            </div>

            <label for="amountPaid">Amount paid so far (Rs.)</label>
            <input id="amountPaid" name="amountPaid" type="number" min="0" step="0.01"
                   value="<%= form.containsKey("amountPaid") ? HtmlUtil.esc(form.get("amountPaid")) : "0" %>" required>

            <% if (editing) { %>
                <label for="username">Parent username</label>
                <input id="username" value="<%= HtmlUtil.esc(editingUsername) %>" readonly>
                <p class="muted form-help">Parent login details are unchanged by editing a fee record.</p>
            <% } else { %>
                <label for="username">Parent username</label>
                <input id="username" name="username" type="text" minlength="4" maxlength="20"
                       pattern="[A-Za-z0-9_]+" value="<%= HtmlUtil.esc(form.get("username")) %>" required>
                <p class="muted form-help">If this parent account exists without a student record, it will be linked. Otherwise a new parent login is created.</p>

                <div class="row">
                    <div>
                        <label for="parentFullName">Parent full name</label>
                        <input id="parentFullName" name="parentFullName" type="text" minlength="3" maxlength="50"
                               value="<%= HtmlUtil.esc(form.get("parentFullName")) %>">
                    </div>
                    <div>
                        <label for="parentEmail">Parent e-mail (optional)</label>
                        <input id="parentEmail" name="parentEmail" type="email"
                               value="<%= HtmlUtil.esc(form.get("parentEmail")) %>">
                    </div>
                </div>

                <label for="parentPassword">Parent password</label>
                <input id="parentPassword" name="parentPassword" type="password" minlength="8" maxlength="100"
                       autocomplete="new-password">
                <p class="muted form-help">Required only when creating a new parent account; use at least 8 characters with a letter and a digit.</p>
            <% } %>

            <div class="admin-form-actions">
                <% if (editing) { %>
                    <a class="btn alt" href="<%= request.getContextPath() %>/admin/dashboard">Cancel</a>
                <% } else { %>
                    <button class="btn alt" type="reset">Clear</button>
                <% } %>
                <button class="btn" type="submit"><%= editing ? "Save changes" : "Save record" %></button>
            </div>
        </form>
    </section>
</main>
</body>
</html>
