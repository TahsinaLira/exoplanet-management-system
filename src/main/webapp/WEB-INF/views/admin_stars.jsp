<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <title>ExoDiscover | Manage Stars</title>

    <style>
        :root {
            --panel: #150028;
            --edge: #2a0f45;
            --accent: #6a0dad;
            --accent-hover: #9b5de5;
            --ink: #fff;
            --muted: #c8b0ff;
        }

        /* full-page starfield background */
        body.page-with-stars {
            margin: 0;
            padding: 0;
            color: var(--ink);
            background: radial-gradient(circle at top, #1a0333 0%, #0d011d 90%);
            font-family: "Segoe UI", sans-serif;
            min-height: 100vh;
        }

        #space-bg {
            position: fixed;
            inset: 0;
            z-index: 0;
            pointer-events: none;
        }

        #starsCanvas {
            width: 100%;
            height: 100%;
            display: block;
        }

        /* content wrapper sits above stars */
        .wrap {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 16px;
            position: relative;
            z-index: 1;
        }

        h1 {
            text-align: center;
            color: var(--muted);
            margin-bottom: 20px;
        }

        /* FILTER BOX */
        .filter-box {
            background: var(--panel);
            border: 1px solid var(--edge);
            padding: 16px;
            border-radius: 14px;
            margin-bottom: 25px;
            max-width: 600px;
            margin-left: auto;
            margin-right: auto;
        }

        .filter-row {
            display: flex;
            align-items: center;
            gap: 18px;
            justify-content: center;
        }

        .filter-row label {
            font-size: 13px;
            color: var(--muted);
        }

        .filter-row input {
            width: 140px;
            padding: 8px;
            background: #100022;
            border: 1px solid var(--accent);
            border-radius: 8px;
            color: var(--ink);
        }

        .btn {
            background: var(--accent);
            color: #fff;
            padding: 8px 18px;
            border-radius: 8px;
            border: none;
            cursor: pointer;
            font-size: 13px;
            white-space: nowrap;
        }

        .btn:hover {
            background: var(--accent-hover);
        }

        .summary-text {
            text-align: center;
            color: var(--muted);
            margin-bottom: 12px;
            font-size: 13px;
        }

        /* MAIN LAYOUT: table left, cards right */
        .main-grid {
            display: grid;
            grid-template-columns: 2.1fr 1fr;
            gap: 18px;
        }

        @media (max-width: 1000px) {
            .main-grid {
                grid-template-columns: 1fr;
            }
        }

        /* TABLE STYLES */
        .table-card {
            background: var(--panel);
            border-radius: 14px;
            border: 1px solid var(--edge);
            overflow: hidden;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            padding: 8px 12px;
            border-bottom: 1px solid var(--edge);
            font-size: 13px;
        }

        th {
            color: var(--muted);
            background: rgba(255,255,255,0.04);
            text-align: left;
        }

        /* RIGHT-SIDE CARDS */
        .side-card {
            background: var(--panel);
            border-radius: 14px;
            border: 1px solid var(--edge);
            padding: 14px 16px;
            margin-bottom: 14px;
        }

        .side-card h3 {
            margin: 0 0 8px;
            font-size: 15px;
            color: var(--muted);
        }

        .side-card ul {
            margin: 0;
            padding-left: 18px;
            color: var(--ink);
            font-size: 13px;
        }

        .side-card li {
            margin-bottom: 4px;
        }

        .side-card .empty {
            color: var(--muted);
            font-size: 13px;
        }
    </style>
</head>

<body class="page-with-stars">
<div id="space-bg"><canvas id="starsCanvas"></canvas></div>

<%@ include file="_header.jspf" %>

<div class="wrap">
    <div class="wrap">  
    <h1>Manage Stars</h1>  

    <% 
        String flash = (String) request.getAttribute("flash");
        if (flash != null) {
    %>
        <div style="margin-bottom:10px; padding:8px 12px; border-radius:8px;
                    background:rgba(0,0,0,0.4); color:#ffdcb3; font-size:13px;">
            <%= flash %>
        </div>
    <%
        }
    %>

    <!-- ADD NEW STAR BUTTON -->  
    <button class="btn" style="margin-bottom:15px;"
        onclick="document.getElementById('addStarModal').style.display='block'">  
        + Add New Star  
    </button>

<!-- SEARCH STAR BY NAME -->
<form method="get"
      action="<%=request.getContextPath()%>/admin/stars"
      style="display:inline-block; margin-left:15px;">
    <input type="text" name="searchStar" placeholder="Search star name"
           style="padding:8px; width:200px; border-radius:8px;">
    <button class="btn">Search</button>
</form>

<!-- ADD STAR MODAL -->
<div id="addStarModal"
     style="display:none; position:fixed; top:0; left:0; width:100%; height:100%;
            background:rgba(0,0,0,0.65); backdrop-filter: blur(4px);
            justify-content:center; align-items:center;">

    <div style="background:#150028; padding:25px; width:420px;
                border-radius:16px; border:1px solid #6a0dad;">

        <h3 style="color:#c8b0ff; margin-top:0;">Add New Star</h3>

        <form method="post" action="<%=request.getContextPath()%>/admin/stars">

            <input type="hidden" name="action" value="add">

            <label style="color:#c8b0ff;">Star Name</label>
            <input type="text" name="name" required
                   style="width:100%; padding:8px; margin-bottom:12px;">
            
            <label style="color:#c8b0ff;">System ID (existing)</label>
<input type="number" name="system_id" required
       placeholder="e.g., 1"
       style="width:100%; padding:8px; margin-bottom:12px;">

            <label style="color:#c8b0ff;">Star Type</label>
            <input type="text" name="type"
                   placeholder="G-type, K-type, Red Dwarf, etc."
                   style="width:100%; padding:8px; margin-bottom:12px;">

            <label style="color:#c8b0ff;">Mass (Solar)</label>
            <input type="number" step="0.0001" name="mass"
                   style="width:100%; padding:8px; margin-bottom:12px;">

            <label style="color:#c8b0ff;">Radius (Solar)</label>
            <input type="number" step="0.0001" name="radius"
                   style="width:100%; padding:8px; margin-bottom:12px;">

            <button class="btn" style="width:100%; margin-top:10px;">
                Add Star
            </button>

            <button type="button"
                    onclick="document.getElementById('addStarModal').style.display='none'"
                    style="width:100%; margin-top:10px; padding:8px;
                           background:#333; color:#fff; border-radius:8px;">
                Cancel
            </button>
        </form>
    </div>
</div>
    <!-- FILTERS -->
    <form method="get" action="<%=request.getContextPath()%>/admin/stars">
        <div class="filter-box">
            <div class="filter-row">
                <label for="minPlanets">Minimum number of planets</label>
                <input type="number" id="minPlanets" name="minPlanets"
                       placeholder="e.g., 2"
                       value="<%= request.getParameter("minPlanets") == null ? "" : request.getParameter("minPlanets") %>">
                <button class="btn" type="submit">Apply Filter</button>
            </div>
        </div>
    </form>

    <!-- SUMMARY TEXT -->
    <%
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> stars =
            (List<Map<String,Object>>) request.getAttribute("stars");
        int starCount = (stars == null) ? 0 : stars.size();
    %>
    <p class="summary-text">Showing <b><%= starCount %></b> star systems.</p>

    <!-- MAIN CONTENT: TABLE + SIDE PANELS -->
    <div class="main-grid">
        <!-- LEFT: STAR TABLE -->
        <div class="table-card">
            <table>
                <tr>
                    <th>ID</th>
                    <th>Star</th>
                    <th>Planet Count</th>
                    <th>Avg Mass (⊕)</th>
                    <th>Avg Radius (⊕)</th>
                </tr>
                <%
                    if (stars == null || stars.isEmpty()) {
                %>
                <tr>
                    <td colspan="5" style="text-align:center; color:var(--muted); padding:18px;">
                        No star systems found for this filter.
                    </td>
                </tr>
                <%
                    } else {
                        for (Map<String,Object> row : stars) {
                %>
                <tr>
                    <td><%= row.get("star_id") %></td>
                    <td><%= row.get("star_name") %></td>
                    <td><%= row.get("planet_count") %></td>
                    <td><%= row.get("avg_mass_earth") %></td>
                    <td><%= row.get("avg_radius_earth") %></td>
                </tr>
                <%
                        }
                    }
                %>
            </table>
        </div>

        <!-- RIGHT: TOP SYSTEM PANELS -->
        <div>
            <!-- TOP 5 MOST PACKED -->
            <div class="side-card">
                <h3>Top 5 Most Packed Systems</h3>
                <%
                    @SuppressWarnings("unchecked")
                    List<Map<String,Object>> topSystems =
                        (List<Map<String,Object>>) request.getAttribute("packed");
                    if (topSystems == null || topSystems.isEmpty()) {
                %>
                <p class="empty">No data.</p>
                <%
                    } else {
                %>
                <ul>
                    <%
                        for (Map<String,Object> row : topSystems) {
                            String name = String.valueOf(row.get("star_name"));
                            int pc      = (row.get("planet_count") == null)
                                          ? 0 : ((Number) row.get("planet_count")).intValue();
                    %>
                    <li><%= name %> — <%= pc %> planets</li>
                    <%
                        }
                    %>
                </ul>
                <%
                    }
                %>
            </div>

            <!-- TOP 5 HEAVIEST -->
            <div class="side-card">
                <h3>Top 5 Heaviest Systems (avg planet mass)</h3>
                <%
                    @SuppressWarnings("unchecked")
                    List<Map<String,Object>> heavySystems =
                        (List<Map<String,Object>>) request.getAttribute("heavy");
                    if (heavySystems == null || heavySystems.isEmpty()) {
                %>
                <p class="empty">No data.</p>
                <%
                    } else {
                %>
                <ul>
                    <%
                        for (Map<String,Object> row : heavySystems) {
                            String name = String.valueOf(row.get("star_name"));
                            int pc      = (row.get("planet_count") == null)
                                          ? 0 : ((Number) row.get("planet_count")).intValue();
                            double avgM = (row.get("avg_mass_earth") == null)
                                          ? 0.0 : ((Number) row.get("avg_mass_earth")).doubleValue();
                    %>
                    <li><%= name %> — <%= pc %> planets, avg mass
                        <%= String.format("%.3f", avgM) %> ⊕</li>
                    <%
                        }
                    %>
                </ul>
                <%
                    }
                %>
            </div>
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