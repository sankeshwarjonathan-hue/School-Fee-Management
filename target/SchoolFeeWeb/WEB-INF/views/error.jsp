<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Something went wrong</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="center">
<main class="card narrow center-text">
    <h1>Oops</h1>
    <p class="muted">The page you asked for is not available, or something went wrong on the server.</p>
    <a class="btn" href="<%= request.getContextPath() %>/login">Back to login</a>
</main>
</body>
</html>
