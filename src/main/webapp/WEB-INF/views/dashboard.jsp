<%@ page contentType="text/html;charset=UTF-8" language="java" session="true" %>
<%@ page import="java.util.List, model.User, model.Student, model.Payment, util.HtmlUtil" %>
<%
    // Second line of defence: the filter already blocks anonymous users.
    if (session.getAttribute("username") == null) {
        response.sendRedirect(request.getContextPath() + "/login?error=auth");
        return;
    }
    User user = (User) request.getAttribute("user");
    Student st = user.getStudent();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard - School Fee Management</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="topbar">
    <strong>School Fee &amp; Finance</strong>
    <span>
        <%= HtmlUtil.esc(session.getAttribute("fullName")) %>
        <span class="badge"><%= HtmlUtil.esc(session.getAttribute("role")) %></span>
    </span>
    <form method="post" action="<%= request.getContextPath() %>/logout">
        <button type="submit" class="btn small">Log out</button>
    </form>
</header>

<main class="wrap">
    <h1>Welcome, <%= HtmlUtil.esc(user.getFullName()) %></h1>

    <% if (!user.isAdmin()) { %>
    <section class="card">
        <h2>Fee summary</h2>
        <div class="stats">
            <div class="stat"><span>Total fee</span><strong><%= HtmlUtil.money(st.getTotalFee()) %></strong></div>
            <div class="stat"><span>Amount paid</span><strong><%= HtmlUtil.money(st.getAmountPaid()) %></strong></div>
            <div class="stat"><span>Balance due</span><strong><%= HtmlUtil.money(st.getBalance()) %></strong></div>
        </div>
        <table>
            <tr><th scope="row">Student ID</th><td><%= HtmlUtil.esc(st.getStudentId()) %></td></tr>
            <tr><th scope="row">Student name</th><td><%= HtmlUtil.esc(st.getStudentName()) %></td></tr>
            <tr><th scope="row">Class</th><td><%= HtmlUtil.esc(st.getClassName()) %></td></tr>
            <tr><th scope="row">Parent e-mail</th><td><%= HtmlUtil.esc(user.getEmail()) %></td></tr>
        </table>
    </section>

    <section class="card">
        <h2>Pay fees</h2>
        <% List<String> payErrors = (List<String>) request.getAttribute("payErrors");
           if (payErrors != null) { %>
            <div class="alert error" role="alert">
                <% for (String e : payErrors) { %><div><%= HtmlUtil.esc(e) %></div><% } %>
            </div>
        <% } %>
        <% if (st.getBalance() > 0.004) {
               List<String> methods = (List<String>) request.getAttribute("paymentMethods"); %>
        <form method="post" action="<%= request.getContextPath() %>/pay">
            <input type="hidden" name="csrf" value="<%= HtmlUtil.esc(request.getAttribute("csrfToken")) %>">
            <div class="row">
                <div>
                    <label for="amount">Amount to pay (Rs.)</label>
                    <input id="amount" name="amount" type="number" step="0.01" min="0.01"
                           max="<%= HtmlUtil.plain(st.getBalance()) %>"
                           value="<%= HtmlUtil.plain(st.getBalance()) %>" required>
                </div>
                <div>
                    <label for="method">Payment method</label>
                    <select id="method" name="method" required>
                        <% for (String m : methods) { %>
                            <option value="<%= HtmlUtil.esc(m) %>"><%= HtmlUtil.esc(m) %></option>
                        <% } %>
                    </select>
                </div>
            </div>
            <button type="submit" class="btn">Pay now</button>
        </form>
        <p class="muted">Part payment is allowed. This is a demonstration payment: no real money is
            charged. A receipt is created after every payment.</p>
        <% } else { %>
            <div class="alert ok">All fees are paid. Thank you!</div>
        <% } %>
    </section>

    <section class="card">
        <h2>Payment history</h2>
        <% List<Payment> history = (List<Payment>) request.getAttribute("payments");
           if (history == null || history.isEmpty()) { %>
            <p class="muted">No payments yet.</p>
        <% } else { %>
        <table class="grid">
            <thead><tr><th>Receipt</th><th>Date</th><th>Method</th><th>Amount</th><th>Balance after</th><th></th></tr></thead>
            <tbody>
            <% for (Payment h : history) { %>
                <tr>
                    <td><%= HtmlUtil.esc(h.getReceiptNo()) %></td>
                    <td><%= HtmlUtil.esc(h.getDateText()) %></td>
                    <td><%= HtmlUtil.esc(h.getMethod()) %></td>
                    <td><%= HtmlUtil.money(h.getAmount()) %></td>
                    <td><%= HtmlUtil.money(h.getBalanceAfter()) %></td>
                    <td><a href="<%= request.getContextPath() %>/receipt?no=<%= HtmlUtil.esc(h.getReceiptNo()) %>">View receipt</a></td>
                </tr>
            <% } %>
            </tbody>
        </table>
        <% } %>
    </section>
    <% } else {
        List<User> parents = (List<User>) request.getAttribute("parents");
        double sumTotal = (Double) request.getAttribute("sumTotal");
        double sumPaid = (Double) request.getAttribute("sumPaid");
    %>
    <section class="card">
        <h2>All students</h2>
        <div class="stats">
            <div class="stat"><span>Students</span><strong><%= parents.size() %></strong></div>
            <div class="stat"><span>Fees collected</span><strong><%= HtmlUtil.money(sumPaid) %></strong></div>
            <div class="stat"><span>Outstanding</span><strong><%= HtmlUtil.money(sumTotal - sumPaid) %></strong></div>
        </div>
        <% if (parents.isEmpty()) { %>
            <p class="muted">No parents have registered yet.</p>
        <% } else { %>
        <table class="grid">
            <thead><tr><th>ID</th><th>Student</th><th>Class</th><th>Parent</th>
                <th>Total</th><th>Paid</th><th>Balance</th></tr></thead>
            <tbody>
            <% for (User p : parents) { Student s = p.getStudent(); %>
                <tr>
                    <td><%= HtmlUtil.esc(s.getStudentId()) %></td>
                    <td><%= HtmlUtil.esc(s.getStudentName()) %></td>
                    <td><%= HtmlUtil.esc(s.getClassName()) %></td>
                    <td><%= HtmlUtil.esc(p.getFullName()) %></td>
                    <td><%= HtmlUtil.money(s.getTotalFee()) %></td>
                    <td><%= HtmlUtil.money(s.getAmountPaid()) %></td>
                    <td><%= HtmlUtil.money(s.getBalance()) %></td>
                </tr>
            <% } %>
            </tbody>
        </table>
        <% } %>
    </section>
    <% } %>

    <section class="card">
        <h2>Session information</h2>
        <table>
            <tr><th scope="row">Logged in as</th><td><%= HtmlUtil.esc(session.getAttribute("username")) %></td></tr>
            <tr><th scope="row">Session ID</th><td><code><%= HtmlUtil.esc(request.getAttribute("sessionId")) %></code></td></tr>
            <tr><th scope="row">Session created</th><td><%= HtmlUtil.esc(request.getAttribute("sessionCreated")) %></td></tr>
            <tr><th scope="row">Auto logout after</th><td><%= request.getAttribute("sessionTimeoutMinutes") %> minutes of inactivity</td></tr>
        </table>
    </section>
</main>
</body>
</html>
