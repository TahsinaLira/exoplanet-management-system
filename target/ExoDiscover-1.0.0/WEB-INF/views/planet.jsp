<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.*,com.exo.model.PlanetSummary, com.exo.model.Paper" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>ExoDiscover | Planet Details</title>
  <style>
    :root{ --bg:#0d011d; --panel:#150028; --edge:#2a0f45; --accent:#6a0dad; --ink:#fff; --muted:#c599ff; }
    *{ box-sizing:border-box }
    body{ margin:0; font-family:"Segoe UI",Arial,sans-serif; color:var(--ink);
          background:radial-gradient(1200px 600px at 50% -10%, #1a0333 0, #0d011d 55%) fixed; }
    .topbar{ display:flex; align-items:center; justify-content:space-between; gap:8px; padding:10px 14px; border-bottom:1px solid var(--edge); background:rgba(0,0,0,.25) }
    .brand{ font-weight:700; letter-spacing:.5px; color:#fff; }
    .brand b{ color:var(--muted) }
    .btn{ background:var(--accent); color:#fff; padding:8px 14px; border:0; border-radius:10px; cursor:pointer; text-decoration:none; display:inline-block; }
    .btn:hover{ filter:brightness(1.1) }
    .wrap{ max-width:1200px; margin:18px auto; padding:0 14px; }
    .grid{ display:grid; grid-template-columns: 280px 1fr 320px; gap:14px; }
    .card{ background:var(--panel); border:1px solid var(--edge); border-radius:14px; padding:14px; min-height:140px; }
    h1{ margin:0 0 8px; font-size:24px; color:#fff }
    h2{ margin:0 0 10px; font-size:16px; color:var(--muted); font-weight:600 }
    .papers a{ color:#d8b6ff; text-decoration:none }
    .papers a:hover{ text-decoration:underline }
    .paper{ padding:8px 10px; border:1px solid var(--edge); border-radius:10px; margin-bottom:8px; background:rgba(255,255,255,.02) }
    .paper small{ color:#c9b6f5 }
    .desc{ line-height:1.55; color:#eae1ff }
    .facts dt{ color:#c9b6f5; margin-top:8px; font-weight:600 }
    .facts dd{ margin:4px 0 10px 0; color:#eee }
    .muted{ color:#bfa7e8 }

    /* Overview media layout */
    .overview-grid{ display:grid; grid-template-columns: 190px 1fr; gap:14px; align-items:start; }
    .planet-img{ width:180px; height:180px; object-fit:cover; border-radius:12px;
                 border:1px solid var(--accent); box-shadow:0 0 10px rgba(106,13,173,.35); }
    @media (max-width: 1024px){ .grid{ grid-template-columns:1fr; } .overview-grid{ grid-template-columns:1fr; } .planet-img{ width:100%; height:220px; } }
  </style>
</head>
<body>

<%
  // ---- Read data from request safely ----
  PlanetSummary p = (PlanetSummary) request.getAttribute("planet");
  if (p == null) {
%>
  <div class="wrap">
    <div class="card">
      <h1>Planet not found</h1>
      <p class="muted">No planet data was provided to the page.</p>
      <a class="btn" href="<%=request.getContextPath()%>/search">Back to Search</a>
    </div>
  </div>
</body>
</html>
<%
    return;
  }

  // Optional text description from servlet (short)
  String description = (String) request.getAttribute("description");
  if (description == null || description.trim().isEmpty()) {
      description = "";}

  // Rich HTML paragraph from servlet
  String descriptionHtml = (String) request.getAttribute("descriptionHtml");
  if (descriptionHtml == null) descriptionHtml = "";

  // Papers list (optional)
  List papers = null;
  Object papersAttr = request.getAttribute("papers");
  if (papersAttr instanceof List) { papers = (List) papersAttr; }

  // Picture URL chosen in the servlet; fallback to unknown.png
  String imgUrl = (String) request.getAttribute("imgUrl");
  if (imgUrl == null || imgUrl.isEmpty()) {
      imgUrl = request.getContextPath() + "/assets/img/planets/unknown.png";
  }
%>

<div class="topbar">
  <div class="brand">Exo<b>Discover</b></div>
  <div>
    <a class="btn" href="<%=request.getContextPath()%>/search">Back to Search</a>
  </div>
</div>

<div class="wrap">
  <h1><%= p.getPlanet() == null ? "Unknown Planet" : p.getPlanet() %></h1>
  <p class="muted">Host star: <b><%= p.getHostStar()==null?"—":p.getHostStar() %></b></p>

  <div class="grid">

   <!-- LEFT: Papers -->
<section class="card papers">
  <h2>Research Papers</h2>

  <%
    Integer uid = (Integer) session.getAttribute("uid");
    boolean loggedIn = (uid != null);

    List<com.exo.model.Paper> papersList =
        (List<com.exo.model.Paper>) request.getAttribute("papers");
  %>

  <% if (!loggedIn) { %>

      <div style="text-align:center; padding:20px;">
        <p class="muted" style="font-size:14px;">🔒 Please log in to view research papers.</p>
        <a href="<%=request.getContextPath()%>/login" class="btn">Login</a>
      </div>

  <% } else if (papersList == null || papersList.isEmpty()) { %>

      <p class="muted">No papers linked yet.</p>
      <p style="font-size:12px;color:#d0c4ff">
        
      </p>

  <% } else { %>

      <% for (com.exo.model.Paper paper : papersList) { %>
        <div class="paper">
          <div>
            <a href="<%=request.getContextPath()%>/paper/download?id=<%= paper.getPaperId() %>"
               target="_blank">
              <%= paper.getTitle() %>
            </a>
          </div>
          <small><%= (paper.getYear() == null ? "" : paper.getYear()) %></small>
        </div>
      <% } %>

  <% } %>

</section>
    <!-- MIDDLE: Overview with image + rich paragraph -->
    <section class="card">
      <h2>Overview</h2>
      <div class="overview-grid">
        <img class="planet-img" src="<%= imgUrl %>" alt="Planet image">
        <div class="desc">
          <p><%= description %></p>
          <div><%= descriptionHtml %></div>
        </div>
      </div>
    </section>

    <!-- RIGHT: Key facts -->
    <aside class="card">
      <h2>Key Facts</h2>
      <dl class="facts">
        <dt>Planet</dt>
        <dd><%= p.getPlanet()==null?"—":p.getPlanet() %></dd>

        <dt>Host Star</dt>
        <dd><%= p.getHostStar()==null?"—":p.getHostStar() %></dd>

        <dt>Discovery Year</dt>
        <dd><%= p.getDiscYear()==null?"—":p.getDiscYear() %></dd>

        <dt>Discovery Method</dt>
        <dd><%= p.getMethodName()==null?"—":p.getMethodName() %></dd>

        <dt>Facility</dt>
        <dd><%= p.getFacilityName()==null?"—":p.getFacilityName() %></dd>

        <dt>Distance (pc)</dt>
        <dd><%= p.getDistanceParsec()==null?"—":p.getDistanceParsec() %></dd>

        <dt>Orbital Period (days)</dt>
        <dd><%= p.getOrbitalPeriodDays()==null?"—":p.getOrbitalPeriodDays() %></dd>
      </dl>
    </aside>

  </div>
</div>

</body>
</html>