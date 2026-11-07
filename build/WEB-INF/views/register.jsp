<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map, util.HtmlUtil" %>
<%
    Map<String, String> form = (Map<String, String>) request.getAttribute("form");
    List<String> classNames = (List<String>) request.getAttribute("classNames");
    List<String> errors = (List<String>) request.getAttribute("errors");
    String selectedClass = form.get("className");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Register - School Fee Management</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="center">
<main class="card">
    <h1>Parent Registration</h1>
    <p class="muted">Create an account to view your child's fee details</p>

    <% if (errors != null) { %>
        <div class="alert error" role="alert">
            <% for (String e : errors) { %><div><%= HtmlUtil.esc(e) %></div><% } %>
        </div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/register">
        <h2>Your details</h2>
        <label for="fullName">Full name</label>
        <input id="fullName" name="fullName" type="text" required maxlength="50"
               value="<%= HtmlUtil.esc(form.get("fullName")) %>">

        <label for="email">E-mail</label>
        <input id="email" name="email" type="email" required
               value="<%= HtmlUtil.esc(form.get("email")) %>">

        <label for="username">Username</label>
        <input id="username" name="username" type="text" required minlength="4" maxlength="20"
               autocomplete="username" value="<%= HtmlUtil.esc(form.get("username")) %>">

        <div class="row">
            <div>
                <label for="password">Password</label>
                <input id="password" name="password" type="password" required minlength="8"
                       autocomplete="new-password">
            </div>
            <div>
                <label for="confirm">Confirm password</label>
                <input id="confirm" name="confirm" type="password" required minlength="8"
                       autocomplete="new-password">
            </div>
        </div>

        <h2>Student details</h2>
        <div class="row">
            <div>
                <label for="studentId">Student ID</label>
                <input id="studentId" name="studentId" type="text" required maxlength="15"
                       value="<%= HtmlUtil.esc(form.get("studentId")) %>">
            </div>
            <div>
                <label for="className">Class</label>
                <select id="className" name="className" required>
                    <option value="">Select</option>
                    <% for (String c : classNames) { %>
                        <option value="<%= HtmlUtil.esc(c) %>"
                            <%= c.equals(selectedClass) ? "selected" : "" %>>Class <%= HtmlUtil.esc(c) %></option>
                    <% } %>
                </select>
            </div>
        </div>

        <label for="studentName">Student name</label>
        <input id="studentName" name="studentName" type="text" required maxlength="50"
               value="<%= HtmlUtil.esc(form.get("studentName")) %>">

        <button type="submit" class="btn">Register</button>
    </form>
    <p class="muted center-text">Already registered?
        <a href="<%= request.getContextPath() %>/login">Log in</a></p>
</main>
</body>
</html>
