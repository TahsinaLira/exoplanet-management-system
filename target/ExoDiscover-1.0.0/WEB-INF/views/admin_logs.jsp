<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*, java.util.Map, java.text.SimpleDateFormat" %>

<!DOCTYPE html>
<html>
<head>
    <title>ExoDiscover | View Logs</title>

    <style>
        :root {
            --panel: #150028;
            --edge: #2a0f45;
            --accent: #6a0dad;
            --accent-hover: #9b5de5;
            --ink: #fff;
            --muted: #c8b0ff;
        }

        body.page-with-stars {
            margin: 0;
            padding: 0;
            color: var(--ink);
            background: radial-gradient(circle at top, #1a0333 0%, #0d011d 90%);
            font-family: "Segoe UI", sans-serif;
            min-height: 100vh;
        }

        /* starfield */
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

        .filter-box {
            background: var(--panel);
            border: 1px solid var(--edge);
            padding: 14px 16px;
            border-radius: 14px;
            margin-bottom: 22px;
        }

        .filter-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 10px;
        }

        label {
            font-size: 12px;
            color: var(--muted);
            display: block;
            margin-bottom: 4px;
        }

        input, select {
            width: 100%;
            padding: 6px 8px;
            background: #100022;
            border: 1px solid var(--accent);
            border-radius: 8px;
            color: var(--ink);
            font-size: 13px;
        }

        .btn {
            background: var(--accent);
            color: #fff;
            padding: 8px 16px;
            border-radius: 8px;
            border: none;
            cursor: pointer;
            font-size: 13px;
            margin-top: 18px;
        }
        .btn:hover { background: var(--accent-hover); }

        .summary {
            text-align: center;
            color: var(--muted);
            font-size: 13px;
            margin-bottom: 12px;
        }

        .main-grid {
            display: grid;
            grid-template-columns: 2.2fr 1fr;
            gap: 18px;
        }
        @media (max-width: 1000px) {
            .main-grid { grid-template-columns: 1fr; }
        }

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
            padding: 8px 10px;
            border-bottom: 1px solid var(--edge);
            font-size: 12px;
        }
        th {
            background: rgba(255,255,255,0.05);
            color: var(--muted);
            text-align: left;
        }

        .side-card {
            background: var(--panel);
            border-radius: 14px;
            border: 1px solid var(--edge);
            padding: 12px 14px;
            margin-bottom: 12px;
        }
        .side-card h3 {
            margin: 0 0 8px;
            font-size: 14px;
            color: var(--muted);
        }
        .side-card ul {
            margin: 0;
            padding-left: 18px;
            font-size: 12px;
        }
        .side-card li { margin-bottom: 4px; }
        .empty { color: var(--muted); font-size: 12px; }
    </style>
</head>

<body class="page-with-stars">
<div id="space-bg"><canvas id="starsCanvas"></canvas></div>

<%@ include file="_header.jspf" %>

<div class="wrap">
    <h1>View Logs</h1>

    <!-- FILTERS -->
    <form method="get" action="<%=request.getContextPath()%>/admin/logs">
        <div class="filter-box">
            <div class="filter-grid">
                <div>
                    <label>Username</label>
                    <input type="text" name="username"
                           value="<%= request.getParameter("username")==null? "" : request.getParameter("username") %>">
                </div>

                <div>
                    <label>Action</label>
                    <select name="action">
                        <%
                            String act = request.getParameter("action");
                            if (act == null) act = "";
                        %>
                        <option value="">-- Any --</option>
                        <option value="LOGIN"        <%= "LOGIN".equals(act) ? "selected" : "" %>>LOGIN</option>
                        <option value="LOGOUT"       <%= "LOGOUT".equals(act) ? "selected" : "" %>>LOGOUT</option>
                        <option value="ADD_PAPER"    <%= "ADD_PAPER".equals(act) ? "selected" : "" %>>ADD_PAPER</option>
                        <option value="DELETE_PAPER" <%= "DELETE_PAPER".equals(act) ? "selected" : "" %>>DELETE_PAPER</option>
                        <option value="DELETE_PLANET"<%= "DELETE_PLANET".equals(act) ? "selected" : "" %>>DELETE_PLANET</option>
                        <option value="ADD_PLANET"<%= "ADD_PLANET".equals(act) ? "selected" : "" %>>ADD_PLANET</option>
                    </select>
                </div>

                <div>
                    <label>From date</label>
                    <input type="date" name="fromDate"
                           value="<%= request.getParameter("fromDate")==null? "" : request.getParameter("fromDate") %>">
                </div>

                <div>
                    <label>To date</label>
                    <input type="date" name="toDate"
                           value="<%= request.getParameter("toDate")==null? "" : request.getParameter("toDate") %>">
                </div>
            </div>

            <button class="btn" type="submit">Apply Filters</button>
        </div>
    </form>

    <!-- SUMMARY -->
    <%
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> logs =
            (List<Map<String,Object>>) request.getAttribute("logs");
        int logCount = (logs == null) ? 0 : logs.size();
    %>
    <p class="summary">Showing <b><%= logCount %></b> log entries (max 300).</p>

    <div class="main-grid">
        <!-- LEFT: table -->
        <div class="table-card">
            <table>
                <tr>
                    <th>Time</th>
                    <th>User</th>
                    <th>Action</th>
                    <th>Entity</th>
                    <th>Entity ID</th>
                    <th>Notes</th>
                </tr>
                <%
                    if (logs == null || logs.isEmpty()) {
                %>
                <tr>
                    <td colspan="6" style="text-align:center; color:var(--muted); padding:16px;">
                        No logs found for this filter.
                    </td>
                </tr>
                <%
                    } else {
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        for (Map<String,Object> row : logs) {
                            java.sql.Timestamp ts = (java.sql.Timestamp) row.get("created_at");
                            String tsStr = (ts == null) ? "" : df.format(ts);
                %>
                <tr>
                    <td><%= tsStr %></td>
                    <td><%= row.get("username") == null ? "—" : row.get("username") %></td>
                    <td><%= row.get("action") %></td>
                    <td><%= row.get("entity") == null ? "" : row.get("entity") %></td>
                    <td><%= row.get("entity_id") == null ? "" : row.get("entity_id") %></td>
                    <td><%= row.get("notes") == null ? "" : row.get("notes") %></td>
                </tr>
                <%
                        }
                    }
                %>
            </table>
        </div>

        <!-- RIGHT: summaries -->
        <div>
            <div class="side-card">
                <h3>Top 5 admins (last 7 days)</h3>
                <%
                    @SuppressWarnings("unchecked")
                    List<Map<String,Object>> topAdmins =
                        (List<Map<String,Object>>) request.getAttribute("topAdmins");
                    if (topAdmins == null || topAdmins.isEmpty()) {
                %>
                <p class="empty">No data.</p>
                <%
                    } else {
                %>
                <ul>
                    <%
                        for (Map<String,Object> row : topAdmins) {
                            String u = String.valueOf(row.get("username"));
                            int c = (row.get("action_count") == null)
                                    ? 0 : ((Number)row.get("action_count")).intValue();
                    %>
                    <li><%= u %> — <%= c %> actions</li>
                    <%
                        }
                    %>
                </ul>
                <%
                    }
                %>
            </div>

            <div class="side-card">
                <h3>Paper uploads per day (last 10 days)</h3>
                <%
                    @SuppressWarnings("unchecked")
                    List<Map<String,Object>> uploadsPerDay =
                        (List<Map<String,Object>>) request.getAttribute("uploadsPerDay");
                    if (uploadsPerDay == null || uploadsPerDay.isEmpty()) {
                %>
                <p class="empty">No data.</p>
                <%
                    } else {
                        SimpleDateFormat ddf = new SimpleDateFormat("MMM dd");
                %>
                <ul>
                    <%
                        for (Map<String,Object> row : uploadsPerDay) {
                            java.sql.Date d = (java.sql.Date) row.get("day");
                            String dayStr = (d == null) ? "" : ddf.format(d);
                            int uploads = (row.get("uploads") == null)
                                          ? 0 : ((Number)row.get("uploads")).intValue();
                    %>
                    <li><%= dayStr %> — <%= uploads %> uploads</li>
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