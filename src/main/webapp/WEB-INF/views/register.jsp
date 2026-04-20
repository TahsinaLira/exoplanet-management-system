<%@ include file= "_header.jspf" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Register</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>

<body>
    <body class="page-with-stars">
  <%@ include file="/WEB-INF/views/_stars.jspf" %>
  <div class="guest-top-right">
  <a href="<%=request.getContextPath()%>/search" class="guest-btn">Continue as Guest</a>
</div>
  <h2>Create Account</h2>

  <form method="post" action="${pageContext.request.contextPath}/register">
    <label>Username</label><br/>
    <input type="text" name="username" required maxlength="50"><br/><br/>

    <label>Email</label><br/>
    <input type="email" name="email" required maxlength="120"><br/><br/>

    <label>Password</label><br/>
    <input type="password" name="password" required minlength="6"><br/><br/>

    <label>Confirm Password</label><br/>
    <input type="password" name="confirm" required minlength="6"><br/><br/>

    <button type="submit">Sign Up</button>
     <p>
  <a href="${pageContext.request.contextPath}/" class="btn back-btn"> Back to Home</a>
</p>
  </form>

  <p>Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in</a></p>

  <p style="color:red;">
    ${requestScope.error != null ? requestScope.error : ""}
  </p>
</body>
</html>