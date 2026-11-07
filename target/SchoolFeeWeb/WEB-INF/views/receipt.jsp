<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Payment, model.User, util.HtmlUtil" %>
<%
    Payment pay = (Payment) request.getAttribute("payment");
    User parent = (User) request.getAttribute("parent");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Receipt <%= HtmlUtil.esc(pay.getReceiptNo()) %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="receipt-page">
<main class="receipt">
    <header class="receipt-head">
        <h1>School Fee &amp; Finance</h1>
        <p class="muted">Fee Payment Receipt</p>
    </header>

    <table class="receipt-meta">
        <tr><th scope="row">Receipt No.</th><td><strong><%= HtmlUtil.esc(pay.getReceiptNo()) %></strong></td></tr>
        <tr><th scope="row">Date and time</th><td><%= HtmlUtil.esc(pay.getDateText()) %></td></tr>
        <tr><th scope="row">Transaction ref.</th><td><%= HtmlUtil.esc(pay.getTxnRef()) %></td></tr>
        <tr><th scope="row">Payment method</th><td><%= HtmlUtil.esc(pay.getMethod()) %></td></tr>
    </table>

    <h2>Student</h2>
    <table class="receipt-meta">
        <tr><th scope="row">Student ID</th><td><%= HtmlUtil.esc(pay.getStudentId()) %></td></tr>
        <tr><th scope="row">Student name</th><td><%= HtmlUtil.esc(pay.getStudentName()) %></td></tr>
        <tr><th scope="row">Class</th><td><%= HtmlUtil.esc(pay.getClassName()) %></td></tr>
        <tr><th scope="row">Paid by (parent)</th><td><%= parent == null ? "" : HtmlUtil.esc(parent.getFullName()) %></td></tr>
    </table>

    <h2>Fee details</h2>
    <table class="receipt-meta amounts">
        <tr><th scope="row">Total fee</th><td><%= HtmlUtil.money(pay.getTotalFee()) %></td></tr>
        <tr class="paid-now"><th scope="row">Amount paid now</th><td><%= HtmlUtil.money(pay.getAmount()) %></td></tr>
        <tr><th scope="row">Total paid so far</th><td><%= HtmlUtil.money(pay.getPaidAfter()) %></td></tr>
        <tr><th scope="row">Balance due</th><td><%= HtmlUtil.money(pay.getBalanceAfter()) %></td></tr>
    </table>

    <p class="muted center-text small">This is a computer-generated receipt and needs no signature.
        (Demonstration payment: no real money was charged.)</p>

    <div class="no-print actions">
        <button type="button" class="btn" onclick="window.print()">Print / Save as PDF</button>
        <a class="btn alt" href="<%= request.getContextPath() %>/dashboard">Back to dashboard</a>
    </div>
</main>
</body>
</html>