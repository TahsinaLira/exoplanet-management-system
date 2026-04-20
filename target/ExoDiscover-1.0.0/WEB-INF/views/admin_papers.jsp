<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.*,com.exo.model.Paper" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>ExoDiscover | Upload Research Paper</title>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/style.css">

  <style>
    .admin-wrap{
      max-width:1100px;
      margin:40px auto;
      padding:0 16px;
      position:relative;
      z-index:1;
    }
    .admin-title{
      font-size:24px;
      margin-bottom:8px;
      color:#c599ff;
      text-align:center;
    }
    .flash{
      margin:10px auto 20px auto;
      max-width:600px;
      padding:10px 14px;
      border-radius:8px;
      background:rgba(20,180,120,0.12);
      border:1px solid #1dd19a;
      color:#e9fff7;
      text-align:center;
      font-size:13px;
    }
    .grid-2{
      display:grid;
      grid-template-columns: 1fr 1.4fr;
      gap:18px;
    }
    .card{
      background:rgba(15,0,35,0.9);
      border-radius:14px;
      border:1px solid #2a0f45;
      padding:16px 18px;
    }
    .card h2{
      margin:0 0 10px;
      font-size:16px;
      color:#d0b8ff;
    }
    .card label{
      display:block;
      font-size:13px;
      margin-top:8px;
      margin-bottom:2px;
      color:#d9cafc;
    }
    .card input[type=text],
    .card input[type=number],
    .card textarea,
    .card select{
      width:100%;
      padding:8px 10px;
      border-radius:8px;
      border:1px solid #6a0dad;
      background:#110022;
      color:#fff;
      font-size:13px;
    }
    .card textarea{
      min-height:80px;
      resize:vertical;
    }
    .btn-primary{
      margin-top:10px;
      padding:8px 16px;
      border-radius:8px;
      background:#6a0dad;
      border:0;
      color:#fff;
      cursor:pointer;
      font-size:13px;
    }
    .btn-primary:hover{ background:#9b5de5; }

    /* ==== table ==== */
    .paper-table{
      width:100%;
      border-collapse:collapse;
      font-size:12px;
      margin-top:4px;
    }
    .paper-table th,
    .paper-table td{
      padding:6px 8px;
      border-bottom:1px solid #2a0f45;
      text-align:left;
      color:#e8dcff;
      background:transparent !important;   /* kill purple block */
      box-shadow:none !important;
    }
    .paper-table th{ color:#c599ff; }
    .paper-table tr:hover{
      background:rgba(255,255,255,0.03);   /* subtle row hover, no box */
    }

    /* delete button */
    .btn-small{
      padding:4px 8px;
      border-radius:6px;
      background:#c0392b;
      border:0;
      color:#fff;
      cursor:pointer;
      font-size:11px;
      box-shadow:none;
      outline:none;
    }
    .btn-small:hover{
      background:#e74c3c;
      box-shadow:none;
    }

    .muted{ color:#b7a7d9; font-size:12px; }

    
    .paper-link{
      color:#d8b6ff;
      text-decoration:none;
    }
    .paper-link:hover{
      text-decoration:underline;
    }
    .paper-table a {
    background: none !important;
    box-shadow: none !important;
    padding: 0 !important;
    border-radius: 0 !important;
    color: #d8b6ff !important;
}

/* On hover, only underline */
.paper-table a:hover {
    text-decoration: underline !important;
    background: none !important;
}
  </style>
</head>

<body class="page-with-stars">
<div id="space-bg"><canvas id="starsCanvas"></canvas></div>

<div class="admin-wrap">
  <h1 class="admin-title">Upload Research Paper</h1>

  <%
    String flash = (String) request.getAttribute("flashMsg");
    if (flash != null) {
  %>
    <div class="flash"><%= flash %></div>
  <%
    }
  %>

  <div class="grid-2">

    <!-- LEFT: upload form -->
    <div class="card">
      <h2>New Paper</h2>

      <!-- multipart is required for PDF upload -->
      <form method="post"
            action="<%=request.getContextPath()%>/admin/papers"
            enctype="multipart/form-data">

        <label>Planet</label>
        <select name="planetId" required>
          <option value="">-- Select planet --</option>
          <%
            List<Map<String,Object>> planets =
                (List<Map<String,Object>>) request.getAttribute("planets");
            if (planets != null) {
              for (Map<String,Object> row : planets) {
                int id = (Integer) row.get("id");
                String label = (String) row.get("label");
          %>
                <option value="<%= id %>"><%= label %></option>
          <%
              }
            }
          %>
        </select>

        <label>Title</label>
        <input type="text" name="title" required>

        <label>Authors</label>
        <input type="text" name="authors">

        <label>Year</label>
        <input type="number" name="year" min="1900" max="2100">

        <label>PDF File</label>
        <input type="file" name="pdfFile" accept="application/pdf" required>

        <label>Short Abstract / Notes</label>
        <textarea name="abstractText"></textarea>

        <button class="btn-primary" type="submit">Upload Paper</button>
      </form>
    </div>

    <!-- RIGHT: list of uploaded papers -->
    <div class="card">
      <h2>Uploaded Papers</h2>

      <%
        List<Paper> papers = (List<Paper>) request.getAttribute("papers");
        if (papers == null || papers.isEmpty()) {
      %>
        <p class="muted">No papers uploaded yet.</p>
      <%
        } else {
      %>
        <table class="paper-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>Planet</th>
              <th>Year</th>
              <th>Link</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
          <%
            for (Paper p : papers) {
              String downloadUrl = request.getContextPath()
                                   + "/paper/download?id=" + p.getPaperId();
          %>
            <tr>
              <td><%= p.getPaperId() %></td>

              <!-- Title clickable for download -->
              <td>
                <a class="paper-link"
                   href="<%= downloadUrl %>"
                   target="_blank">
                  <%= p.getTitle() %>
                </a>
              </td>

              <td><%= p.getPlanetName()==null?"—":p.getPlanetName() %></td>
              <td><%= p.getYear()==null?"—":p.getYear() %></td>

              <td>
                <a class="paper-link"
                   href="<%= downloadUrl %>"
                   target="_blank">
                  Download
                </a>
              </td>

              <td>
                <form method="post"
                      action="<%=request.getContextPath()%>/admin/papers"
                      style="display:inline;">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="paperId" value="<%= p.getPaperId() %>">
                  <button class="btn-small"
                          onclick="return confirm('Delete this paper?');">
                    Delete
                  </button>
                </form>
              </td>
            </tr>
          <%
            }
          %>
          </tbody>
        </table>
      <%
        }
      %>
    </div>

  </div>
</div>

<script src="<%=request.getContextPath()%>/assets/js/space-bg.js"></script>
<script>
  document.addEventListener('DOMContentLoaded', () => {
    if (window.SpaceBG) {
      window.SpaceBG.init('#starsCanvas');
    }
  });
</script>

</body>
</html>