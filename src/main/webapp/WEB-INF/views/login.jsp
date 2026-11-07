<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, util.HtmlUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login - School Fee Management</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="center">
<main class="card narrow">
    <h1>School Fee &amp; Finance</h1>
    <p class="muted">Sign in to your account</p>

    <% if (request.getAttribute("info") != null) { %>
        <div class="alert ok" role="status"><%= HtmlUtil.esc(request.getAttribute("info")) %></div>
    <% } %>
    <% if (request.getAttribute("warning") != null) { %>
        <div class="alert warn" role="alert"><%= HtmlUtil.esc(request.getAttribute("warning")) %></div>
    <% } %>
    <% List<String> errors = (List<String>) request.getAttribute("errors");
       if (errors != null) { %>
        <div class="alert error" role="alert">
            <% for (String e : errors) { %><div><%= HtmlUtil.esc(e) %></div><% } %>
        </div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/login">
        <label for="username">Username</label>
        <input id="username" name="username" type="text" required autofocus
               autocomplete="username" value="<%= HtmlUtil.esc(request.getAttribute("username")) %>">

        <label for="password">Password</label>
        <input id="password" name="password" type="password" required
               autocomplete="current-password">

        <button type="submit" class="btn">Log in</button>
    </form>
    <p class="muted center-text">New parent?
        <a href="<%= request.getContextPath() %>/register">Create an account</a></p>
    <p class="muted center-text"><a href="<%= request.getContextPath() %>/admin/login">Administrator login</a></p>
</main>
</body>
</html>
