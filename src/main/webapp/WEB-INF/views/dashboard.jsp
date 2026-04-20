<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.*,com.exo.model.PlanetSummary" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>User Dashboard | ExoDiscover</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">

    <style>
        .topbar {
            position: relative;
            z-index: 1;
            width: 95%;
            max-width: 1250px;
            margin: 25px auto 5px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .topbar-left {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .topbar-title {
            font-size: 18px;
            color: #c599ff;
        }

        .btn-search {
            padding: 8px 16px;
            background: var(--accent);
            color: #fff;
            border-radius: 8px;
            text-decoration: none;
            font-size: 14px;
        }

        .dashboard-layout {
            display: grid;
            grid-template-columns: 260px 1fr 260px;
            gap: 20px;
            width: 95%;
            max-width: 1250px;
            margin: 10px auto 40px;
            position: relative;
            z-index: 1;
        }

        .card {
            background: rgba(20, 0, 40, 0.7);
            border: 1px solid var(--accent);
            border-radius: 12px;
            padding: 18px;
            box-shadow: 0 0 12px rgba(106, 13, 173, 0.25);
        }

        .card h2 {
            margin-top: 0;
            font-size: 18px;
            color: #c599ff;
            margin-bottom: 10px;
        }

        .stat {
            font-size: 26px;
            font-weight: 700;
            margin: 5px 0 10px;
        }

        .list {
            list-style: none;
            padding: 0;
            margin: 0;
        }

        .list li {
            margin-bottom: 4px;
        }

        .muted {
            font-size: 12px;
            color: var(--text-muted);
        }
    </style>
</head>

<body class="page-with-stars">
    
  
    <!-- LOGOUT BUTTON (TOP RIGHT) -->
    <div style="position:absolute; top:20px; right:20px;">
        <a href="${pageContext.request.contextPath}/logout" class="btn"
           style="background:#6a0dad; padding:8px 16px; border-radius:8px;">
            Logout
        </a>
    </div>

    
    <%@ include file="/WEB-INF/views/_stars.jspf" %>

    <%
        PlanetSummary latest = (PlanetSummary) request.getAttribute("latestDiscovery");
        List<PlanetSummary> hottestList =
            (List<PlanetSummary>) request.getAttribute("hottestList");
        List<Map<String, Object>> packedStars =
            (List<Map<String, Object>>) request.getAttribute("packedStars");
        if (hottestList == null) hottestList = Collections.emptyList();
        if (packedStars == null) packedStars = Collections.emptyList();
    %>

   <div class="topbar">
    <div class="topbar-left">
        <a href="${pageContext.request.contextPath}/search" class="btn-search">
             Search all planets
        </a>
        <a href="${pageContext.request.contextPath}/stars" class="btn-search">
            Star systems
        </a>
    </div>

    <div class="topbar-title">
        Welcome, ${sessionScope.username} 
    </div>
</div>
    <!-- MAIN 3-COLUMN LAYOUT -->
    <div class="dashboard-layout">

        <!-- LEFT COLUMN: overview -->
        <div class="card">
            <h2>Overview</h2>

            <p>Total Planets</p>
            <div class="stat">${totalPlanets}</div>

            <p>Total Stars</p>
            <div class="stat">${totalStars}</div>

            <hr/>

            <h2>Latest Discovery</h2>
            <c:if test="${latestDiscovery != null}">
                <strong>${latestDiscovery.planet}</strong><br/>
                <span class="muted">(${latestDiscovery.discYear})</span><br/>
                <span class="muted">Star: ${latestDiscovery.hostStar}</span>
            </c:if>
            <c:if test="${latestDiscovery == null}">
                <p class="muted">No discovery data yet.</p>
            </c:if>
        </div>

        <!-- MIDDLE COLUMN: discovery trend chart -->
        <div class="card">
            <h2> Discovery Trend</h2>
            <canvas id="trendChart" height="150"></canvas>
        </div>

        <!-- RIGHT COLUMN: hottest planets + packed stars -->
        <div class="card">
            <h2> Top 5 Hottest Planets</h2>
            <c:choose>
                <c:when test="${not empty hottestList}">
                    <ul class="list">
                        <c:forEach var="p" items="${hottestList}">
                            <li>
                                <strong>${p.planet}</strong>
                                <span class="muted">(${p.massEarth} K)</span>
                            </li>
                        </c:forEach>
                    </ul>
                </c:when>
                <c:otherwise>
                    <p class="muted">No temperature data available.</p>
                </c:otherwise>
            </c:choose>

            <hr/>

            <h2> Top 5 Packed Stars</h2>
            <c:choose>
                <c:when test="${not empty packedStars}">
                    <ul class="list">
                        <c:forEach var="s" items="${packedStars}">
                            <li>
                                <strong>${s.star}</strong>
                                <span class="muted">(${s.total} planets)</span>
                            </li>
                        </c:forEach>
                    </ul>
                </c:when>
                <c:otherwise>
                    <p class="muted">No star/planet count data available.</p>
                </c:otherwise>
            </c:choose>
        </div>

    </div>

    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <!-- Fetch discovery trend -->
    <script>
    fetch('${pageContext.request.contextPath}/data/discovery-trend')
        .then(r => r.json())
        .then(data => {
            const ctx = document.getElementById('trendChart').getContext('2d');
            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.years,
                    datasets: [{
                        label: 'Planets Discovered',
                        data: data.counts,
                        borderColor: '#a56bff',
                        backgroundColor: 'rgba(122,77,255,0.3)',
                        borderWidth: 2,
                        fill: true,
                        tension: 0.3
                    }]
                },
                options: {
                    scales: {
                        y: { beginAtZero: true }
                    }
                }
            });
        });
    </script>

</body>
</html>