<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <title>ExoDiscover | Manage Planets</title>

    <style>
        :root {
            --panel: #150028;
            --edge: #2a0f45;
            --accent: #6a0dad;
            --accent-hover: #9b5de5;
            --ink: #fff;
            --muted: #c8b0ff;
        }

        /* FULLSCREEN STARFIELD */
        #space-bg {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: -1;
            pointer-events: none;
        }
        #starsCanvas {
            width: 100% !important;
            height: 100% !important;
            display: block;
        }

        body {
            margin: 0;
            padding: 0;
            color: var(--ink);
            background: radial-gradient(circle at top, #1a0333 0%, #0d011d 90%);
            font-family: "Segoe UI", sans-serif;
            min-height: 100vh;
        }

        .wrap {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 16px;
        }

        .toolbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            margin-bottom: 14px;
        }

        h1 {
            margin: 0;
            color: var(--muted);
        }

        .btn {
            background: var(--accent);
            color: #fff;
            padding: 8px 18px;
            border-radius: 8px;
            border: none;
            cursor: pointer;
            font-size: 13px;
        }

        .btn:hover { background: var(--accent-hover); }

        .btn-secondary {
            background: transparent;
            border: 1px solid var(--accent);
            color: var(--muted);
            padding: 7px 16px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 13px;
        }

        .btn-secondary:hover {
            background: var(--accent);
            color: #fff;
        }

        .results-info {
            font-size: 12px;
            color: var(--muted);
            margin: 6px 0 10px;
        }

        /* TABLE */
        .table-wrap {
            background: var(--panel);
            border-radius: 14px;
            border: 1px solid var(--edge);
            overflow: hidden;
            max-height: 520px;
            overflow-y: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            padding: 10px 14px;
            border-bottom: 1px solid #2a0f45;
            font-size: 13px;
        }

        th {
            color: var(--muted);
            background: rgba(10, 0, 30, 0.95);
            text-align: left;
            position: sticky;
            top: 0;
            z-index: 2;
        }

        tr:nth-child(even) td {
            background: rgba(255, 255, 255, 0.01);
        }

        .del-btn {
            background: #c0392b;
            border: none;
            padding: 6px 12px;
            color: white;
            border-radius: 6px;
            cursor: pointer;
            font-size: 12px;
        }

        .del-btn:hover {
            background: #e74c3c;
        }

        .flash {
            margin-bottom: 10px;
            padding: 8px 12px;
            border-radius: 10px;
            background: rgba(20, 180, 120, 0.12);
            border: 1px solid #1dd19a;
            color: #e9fff7;
            font-size: 13px;
            text-align: center;
        }

        /* PAGINATION BAR (your old logic kept) */
        .pager {
            margin-top: 16px;
            display: flex;
            justify-content: center;
            gap: 12px;
            align-items: center;
        }

        /* MODAL */
        .modal-overlay {
            position: fixed;
            inset: 0;
            background: rgba(0,0,0,0.6);
            display: none;
            align-items: center;
            justify-content: center;
            z-index: 999;
        }

        .modal-overlay.show {
            display: flex;
        }

        .modal {
            background: var(--panel);
            border-radius: 14px;
            border: 1px solid var(--edge);
            padding: 18px 20px;
            width: 420px;
            max-width: 95vw;
            box-shadow: 0 0 20px rgba(0,0,0,0.6);
        }

        .modal h2 {
            margin: 0 0 10px;
            font-size: 18px;
            color: var(--muted);
        }

        .modal label {
            display: block;
            font-size: 13px;
            color: var(--muted);
            margin-top: 8px;
            margin-bottom: 2px;
        }

        .modal input {
            width: 100%;
            padding: 8px;
            background: #100022;
            border: 1px solid var(--accent);
            border-radius: 8px;
            color: var(--ink);
            font-size: 13px;
        }

        .modal-actions {
            margin-top: 12px;
            display: flex;
            justify-content: flex-end;
            gap: 8px;
        }
    </style>
</head>

<body class="page-with-stars">
<%@ include file= "_header.jspf" %>
<%@ include file="/WEB-INF/views/_stars.jspf" %>

<div class="wrap">
    <div class="toolbar">
        <h1>Manage Planets</h1>
        
        <!-- SIMPLE SEARCH BAR -->
<form method="get" action="<%=request.getContextPath()%>/admin/planets" 
      style="margin-bottom:18px; display:flex; gap:10px;">

    <input type="text" 
           name="q" 
           placeholder="Search planet by name..." 
           value="<%= request.getParameter("q")==null? "" : request.getParameter("q") %>"
           style="flex:1; padding:8px; border-radius:8px; border:1px solid var(--accent);
                  background:#100022; color:white;">

    <button class="btn">Search</button>
</form>
        <button type="button" class="btn" onclick="openPlanetModal()">+ Add New Planet</button>
    </div>
    

    <%
        String flash = (String) request.getAttribute("flashMsg");
        if (flash != null) {
    %>
        <div class="flash"><%= flash %></div>
    <%
        }
    %>

    <%
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> planets =
                (List<Map<String,Object>>) request.getAttribute("planets");
        int totalRows = (planets == null) ? 0 : planets.size();
    %>

    <p class="results-info">
        Showing <strong><%= totalRows %></strong> planet<%= totalRows == 1 ? "" : "s" %> in this view.
    </p>

    <!-- PLANET TABLE (no inline update, only Delete) -->
    <div class="table-wrap">
        <table>
            <tr>
                <th>ID</th>
                <th>Planet</th>
                <th>Host Star</th>
                <th>Year</th>
                <th>Method</th>
                <th>Facility</th>
                <th>Action</th>
            </tr>

            <%
                if (planets == null || planets.isEmpty()) {
            %>
            <tr>
                <td colspan="7" style="text-align:center; color:var(--muted); padding:20px;">
                    No planets found.
                </td>
            </tr>

            <%
                } else {
                    for (Map<String,Object> row : planets) {

                        String id       = String.valueOf(row.get("planet_id"));
                        String planetName = String.valueOf(row.get("planet"));
                        String star     = String.valueOf(row.get("star_name"));

                        Object yearObj = row.get("disc_year");
                        String year    = (yearObj == null) ? "" : String.valueOf(yearObj);

                        String method   = String.valueOf(row.get("discovery_method"));
                        String facility = String.valueOf(row.get("disc_facility"));
            %>

            <tr>
                <td><%= id %></td>
                <td><%= planetName %></td>
                <td><%= star %></td>
                <td><%= year %></td>
                <td><%= method %></td>
                <td><%= facility %></td>
                <td>
                    <form method="post"
                          action="<%=request.getContextPath()%>/admin/planets"
                          style="display:inline;">
                        <input type="hidden" name="planetId" value="<%= id %>">
                        <button class="del-btn"
                                type="submit"
                                name="action"
                                value="delete"
                                onclick="return confirm('Delete this planet?');">
                            Delete
                        </button>
                    </form>
                </td>
            </tr>

            <%
                    }
                }
            %>
        </table>
    </div>

    <!-- PAGINATION: this is your OLD working logic, unchanged -->
    <%
        int currentPage = 1;
        boolean hasNextPage = false;

        try {
            Object pObj = request.getAttribute("page");
            if (pObj != null) {
                currentPage = Integer.parseInt(pObj.toString());
            }
        } catch (Exception ignore) {}

        try {
            Object hObj = request.getAttribute("hasNext");
            if (hObj != null) {
                hasNextPage = Boolean.parseBoolean(hObj.toString());
            }
        } catch (Exception ignore) {}
    %>

    <div class="pager">
        <!-- Previous -->
        <form method="get"
              action="<%=request.getContextPath()%>/admin/planets"
              style="margin:0;">
            <input type="hidden" name="page"
                   value="<%= (currentPage > 1 ? currentPage - 1 : 1) %>">
            <button class="btn" <%= (currentPage <= 1 ? "disabled" : "") %>>
                Previous
            </button>
        </form>

        <span style="color:var(--muted); font-size:13px;">
            Page <%= currentPage %>
        </span>

        <!-- Next -->
        <form method="get"
              action="<%=request.getContextPath()%>/admin/planets"
              style="margin:0;">
            <input type="hidden" name="page"
                   value="<%= currentPage + 1 %>">
            <button class="btn" <%= (!hasNextPage ? "disabled" : "") %>>
                Next
            </button>
        </form>
    </div>
</div>

<!-- ADD PLANET MODAL -->
<div id="planetModal" class="modal-overlay">
    <div class="modal">
        <h2>Add New Planet</h2>
        <form method="post" action="<%=request.getContextPath()%>/admin/planets">
            <input type="hidden" name="action" value="add">

            <label>Planet name *</label>
            <input type="text" name="name" required>

            <label>Star ID </label>
            <input type="number" name="starId" >

            <label>Discovery year</label>
            <input type="number" name="discYear" min="1800" max="2100">

            <label>Discovery method</label>
            <input type="text" name="discoveryMethod">

            <label>Discovery facility</label>
            <input type="text" name="discFacility">

            <label>Mass (Earth units)</label>
            <input type="number" step="0.01" name="massEarth">

            <div class="modal-actions">
                <button type="button" class="btn-secondary" onclick="closePlanetModal()">Cancel</button>
                <button type="submit" class="btn">Save Planet</button>
            </div>
        </form>
    </div>
</div>

<script src="<%=request.getContextPath()%>/assets/js/space-bg.js"></script>
<script>
  document.addEventListener('DOMContentLoaded', () => {
    if (window.SpaceBG) {
      window.SpaceBG.init('#starsCanvas');
    }
  });

  const modal = document.getElementById('planetModal');

  function openPlanetModal() {
      modal.classList.add('show');
  }
  function closePlanetModal() {
      modal.classList.remove('show');
  }
  modal.addEventListener('click', function (e) {
      if (e.target === modal) {
          closePlanetModal();
      }
  });
</script>

</body>
</html>