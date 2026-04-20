<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>🌌 ExoDiscover | Explore the Universe</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
  <style>
    /* starfield layer */
    #space-bg{position:fixed; inset:0; z-index:0; pointer-events:none;}
    #starsCanvas{width:100%; height:100%; display:block;}
    /* centered hero */
    .wrap{
      position:relative; z-index:1;
      min-height:100vh; display:flex; flex-direction:column;
      align-items:center; justify-content:center; text-align:center; gap:16px;
    }
  </style>
</head>
<body class="page-with-stars">
  <!-- background stars -->
  <div id="space-bg"><canvas id="starsCanvas"></canvas></div>

  <!-- centered content -->
  <div class="wrap">
    <h1>🌌 Welcome to <span style="color:#c599ff;">ExoDiscover</span></h1>
    <p style="font-size:16px; color:#d8b6ff;">
      Explore exoplanets, their host stars, and the mysteries of our universe.
    </p>
    <div>
      <a class="btn" href="${pageContext.request.contextPath}/register">Register</a>
      <a class="btn" href="${pageContext.request.contextPath}/login">Login</a>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/assets/js/space-bg.js"></script>
  <script>
    document.addEventListener('DOMContentLoaded', function () {
      if (window.SpaceBG) { window.SpaceBG.init('#starsCanvas'); }
    });
  </script>
</body>
</html>