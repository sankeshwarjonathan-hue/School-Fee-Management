<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, util.HtmlUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin Login - School Fee Management</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="center">
<main class="card narrow">
    <h1>Administrator login</h1>
    <p class="muted">Sign in with an administrator account to manage student fee records.</p>

    <% if (request.getAttribute("info") != null) { %>
        <div class="alert ok" role="status"><%= HtmlUtil.esc(request.getAttribute("info")) %></div>
    <% } %>
    <% List<String> errors = (List<String>) request.getAttribute("errors");
       if (errors != null) { %>
        <div class="alert error" role="alert">
            <% for (String error : errors) { %><div><%= HtmlUtil.esc(error) %></div><% } %>
        </div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/admin/login">
        <label for="username">Admin username</label>
        <input id="username" name="username" type="text" required autofocus
               autocomplete="username" value="<%= HtmlUtil.esc(request.getAttribute("username")) %>">
        <label for="password">Password</label>
        <input id="password" name="password" type="password" required autocomplete="current-password">
        <button type="submit" class="btn">Log in</button>
    </form>
    <p class="muted center-text"><a href="<%= request.getContextPath() %>/login">Parent login</a></p>
</main>
</body>
</html>
