<%--<%@ include file= "_header.jspf" %>--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login | ExoDiscover</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
    <body class="page-with-stars">
  <%@ include file="/WEB-INF/views/_stars.jspf" %>
  <div class="guest-top-right">
  <a href="<%=request.getContextPath()%>/search" class="guest-btn">Continue as Guest</a>
</div>
  
    <div class="login-box">
        <h2>🔭 User Login</h2>

        <form action="${pageContext.request.contextPath}/login" method="post">
            <% String error = (String) request.getAttribute("error");
               if (error != null) { %>
                <div class="error"><%= error %></div>
            <% } %>

            <input type="text" name="username" placeholder="Enter Username" required>
            <input type="password" name="password" placeholder="Enter Password" required>
            <button type="submit" class="btn">Login</button>
        </form>

        <p style="margin-top: 10px;">Don't have an account?
           <a href="${pageContext.request.contextPath}/register" style="color:#9b5de5;">Register</a>
        </p>

        <a href="${pageContext.request.contextPath}/" class="back-btn">⬅ Back to Home</a>
    </div>
</body>
</html>