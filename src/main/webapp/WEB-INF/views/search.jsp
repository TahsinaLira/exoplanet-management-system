<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.*,com.exo.model.PlanetSummary" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>ExoDiscover | Search</title>
  
  <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/style.css">
  
  <style>
  /* Compact layout just for search.jsp */
  body.search-page {
    background:#0d011d;
    color:#fff;
    font-family:"Segoe UI",Arial,sans-serif;
    font-size:13px;
    margin:0;
  }

  .container {
    width:96%;
    max-width:1080px;
    margin:0 auto;
    padding:6px 4px;
  }

  .page-title {
    font-size:18px;
    margin:4px 0;
    color:#c599ff;
  }

  .search-form {
    display:flex;
    flex-wrap:wrap;
    gap:4px;
    margin:6px 0;
  }

  .search-input {
    flex:1 1 180px;
    min-width:130px;
    padding:4px 6px;
    background:#16002e;
    border:1px solid #6a0dad;
    border-radius:4px;
    color:#fff;
    font-size:12px;
  }

  .search-input.small {
    flex:0 0 100px;
  }

  .btn {
    padding:4px 8px;
    border-radius:4px;
    background:#6a0dad;
    color:#fff;
    border:none;
    cursor:pointer;
    font-size:12px;
  }

  .btn:hover {
    background:#8a3ed1;
  }

  .table-wrap {
    overflow-x:auto;
    -webkit-overflow-scrolling:touch;
  }

  .data-table {
    width:100%;
    border-collapse:collapse;
    margin-top:6px;
    font-size:12px;
  }

  .data-table th,
  .data-table td {
    padding:12px 14px;
    border-bottom:1px solid #2a0f45;
    text-align:left;
    line-height:1.6;
  }

  .data-table th {
    color:#c599ff;
    font-weight:500;
  }

  tr:hover td {
    background:rgba(106,13,173,0.12);
  }

  .muted {
    color:#b7a7d9;
    font-size:12px;
  }
  .thumb{width:40px;height:40px;object-fit:cover;border-radius:6px;border:1px solid #2a0f45}
</style>
  
  
</head>
<body class=""search-page">
    <body class="page-with-stars">
  <%@ include file="/WEB-INF/views/_stars.jspf" %>
 
<div class="wrap">
  <h1>🔭 Explore Exoplanets</h1>

  <form method="get" action="<%=request.getContextPath()%>/search" class="filters">
    <input type="text"   name="q"        placeholder="Planet or star" value="<%=request.getParameter("q")==null?"":request.getParameter("q")%>">
    <input type="number" name="yFrom"    placeholder="Year from"      value="<%=request.getParameter("yFrom")==null?"":request.getParameter("yFrom")%>">
    <input type="number" name="yTo"      placeholder="Year to"        value="<%=request.getParameter("yTo")==null?"":request.getParameter("yTo")%>">
    <input type="text"   name="method"   placeholder="Method"         value="<%=request.getParameter("method")==null?"":request.getParameter("method")%>">
    <input type="text"   name="facility" placeholder="Facility"       value="<%=request.getParameter("facility")==null?"":request.getParameter("facility")%>">
    <button class="btn">Search</button>
  </form>

  <%
    List<PlanetSummary> rows = (List<PlanetSummary>) request.getAttribute("rows");
    if (rows == null) rows = Collections.emptyList();
  %>

  <table>
    <thead>
      <tr>
         
        <th>Planet</th><th>Host Star</th><th>Year</th><th>Method</th>
        <th>Facility</th><th>Distance (pc)</th><th>Period (days)</th>
        <th>Details</th> <!-- NEW COLUMN -->
      </tr>
    </thead>
    <tbody>
    <%
      if (rows.isEmpty()) {
    %>
        <tr><td colspan="8" class="muted">No results</td></tr>
    <%
      } else {
        for (PlanetSummary r : rows) {
    %>
      <tr>
        <td><%= r.getPlanet() %></td>
        <td><%= r.getHostStar() %></td>
        <td><%= r.getDiscYear()==null?"":r.getDiscYear() %></td>
        <td><%= r.getMethodName()==null?"":r.getMethodName() %></td>
        <td><%= r.getFacilityName()==null?"":r.getFacilityName() %></td>
        <td><%= r.getDistanceParsec()==null?"":r.getDistanceParsec() %></td>
        <td><%= r.getOrbitalPeriodDays()==null?"":r.getOrbitalPeriodDays() %></td>

        <!-- NEW: clickable link -->
        <td>
          <a class="btn" 
             href="<%=request.getContextPath()%>/planet?id=<%= r.getPlanetId() %>">
             View
          </a>
        </td>
      </tr>
    <%
        }
      }
    %>
    </tbody>
  </table>
<%= request.getAttribute("pagination") == null ? "" : request.getAttribute("pagination") %>
</div>
</body>
</html>