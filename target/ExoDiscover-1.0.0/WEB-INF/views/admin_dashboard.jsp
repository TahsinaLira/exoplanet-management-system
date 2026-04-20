<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Dashboard | ExoDiscover</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<!-- LOGOUT BUTTON (TOP RIGHT) -->
    <div style="position:absolute; top:20px; right:20px;">
        <a href="${pageContext.request.contextPath}/logout" class="btn"
           style="background:#6a0dad; padding:8px 16px; border-radius:8px;">
            Logout
        </a>
    </div>

<%@ include file="_header.jspf" %>
<body class="page-with-stars">
  <%@ include file="/WEB-INF/views/_stars.jspf" %>
  
<section class="dashboard">
    <h1> Welcome, Admin ${sessionScope.username}</h1>
    <p class="subtitle">Manage the ExoDiscover database and research content.</p>

    <div class="admin-actions">
        <a class="btn" href="${pageContext.request.contextPath}/admin/papers"> Upload Research Paper</a>
        <a class="btn" href="${pageContext.request.contextPath}/admin/planets"> Manage Planets</a>
        <a class="btn" href="${pageContext.request.contextPath}/admin/stars"> Manage Stars</a>
        <a class="btn" href="${pageContext.request.contextPath}/admin/logs"> View Logs</a>
    </div>
</section>

<footer style="margin-top: 40px; text-align: center; color: #aaa;">
    <p>© 2025 ExoDiscover Control Center | Powered by NASA Exoplanet Archive</p>
</footer>

</body>
</html>