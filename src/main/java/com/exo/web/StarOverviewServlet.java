package com.exo.web;

import com.exo.config.Db;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/stars")
public class StarOverviewServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String minPlanetsStr = request.getParameter("minPlanets");
        String searchStar    = trim(request.getParameter("searchStar"));

        Integer minPlanets = null;
        if (minPlanetsStr != null && !minPlanetsStr.trim().isEmpty()) {
            try {
                minPlanets = Integer.parseInt(minPlanetsStr.trim());
            } catch (NumberFormatException ignored) {}
        }
        if (minPlanets == null) {
            minPlanets = 0; // default: show all stars
        }

        List<Map<String,Object>> stars        = new ArrayList<>();
        List<Map<String,Object>> topSystems   = new ArrayList<>();
        List<Map<String,Object>> heavySystems = new ArrayList<>();

        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            cn = Db.getConnection();

            // ---- MAIN STAR SUMMARY (same logic as admin stars page, but read-only) ----
            StringBuilder sqlMain = new StringBuilder(
                "SELECT " +
                "  s.star_id, " +
                "  s.name AS star_name, " +
                "  COUNT(p.planet_id)        AS planet_count, " +
                "  AVG(p.mass_earth)         AS avg_mass_earth, " +
                "  AVG(p.radius_earth)       AS avg_radius_earth " +
                "FROM star s " +
                "LEFT JOIN planet p ON p.star_id = s.star_id " +
                "WHERE 1=1 "
            );

            List<Object> params = new ArrayList<>();

            if (searchStar != null && !searchStar.isEmpty()) {
                sqlMain.append(" AND s.name LIKE ? ");
                params.add("%" + searchStar + "%");
            }

            sqlMain.append(
                "GROUP BY s.star_id, s.name " +
                "HAVING planet_count >= ? " +
                "ORDER BY planet_count DESC, star_name ASC"
            );
            params.add(minPlanets);

            ps = cn.prepareStatement(sqlMain.toString());

            for (int i = 0; i < params.size(); i++) {
                Object v = params.get(i);
                if (v instanceof Integer) ps.setInt(i + 1, (Integer) v);
                else if (v instanceof String) ps.setString(i + 1, (String) v);
                else ps.setObject(i + 1, v);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> row = new HashMap<>();
                row.put("star_id",          rs.getInt("star_id"));
                row.put("star_name",        rs.getString("star_name"));
                row.put("planet_count",     rs.getInt("planet_count"));
                row.put("avg_mass_earth",   rs.getObject("avg_mass_earth"));
                row.put("avg_radius_earth", rs.getObject("avg_radius_earth"));
                stars.add(row);
            }
            rs.close();
            ps.close();

            // ---- TOP 5 MOST PACKED SYSTEMS ----
            String sqlTop =
                "SELECT s.star_id, s.name AS star_name, COUNT(p.planet_id) AS planet_count " +
                "FROM star s " +
                "JOIN planet p ON p.star_id = s.star_id " +
                "GROUP BY s.star_id, s.name " +
                "ORDER BY planet_count DESC, star_name ASC " +
                "LIMIT 5";

            ps = cn.prepareStatement(sqlTop);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> row = new HashMap<>();
                row.put("star_id",      rs.getInt("star_id"));
                row.put("star_name",    rs.getString("star_name"));
                row.put("planet_count", rs.getInt("planet_count"));
                topSystems.add(row);
            }
            rs.close();
            ps.close();

            // ---- TOP 5 HEAVIEST SYSTEMS (by average planet mass) ----
            String sqlHeavy =
                "SELECT s.star_id, s.name AS star_name, " +
                "       COUNT(p.planet_id) AS planet_count, " +
                "       AVG(p.mass_earth)  AS avg_mass_earth " +
                "FROM star s " +
                "JOIN planet p ON p.star_id = s.star_id " +
                "WHERE p.mass_earth IS NOT NULL " +
                "GROUP BY s.star_id, s.name " +
                "HAVING planet_count >= 2 " +
                "ORDER BY avg_mass_earth DESC, star_name ASC " +
                "LIMIT 5";

            ps = cn.prepareStatement(sqlHeavy);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> row = new HashMap<>();
                row.put("star_id",        rs.getInt("star_id"));
                row.put("star_name",      rs.getString("star_name"));
                row.put("planet_count",   rs.getInt("planet_count"));
                row.put("avg_mass_earth", rs.getDouble("avg_mass_earth"));
                heavySystems.add(row);
            }

        } catch (SQLException e) {
            throw new ServletException("Error loading stars overview", e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            try { if (cn != null) cn.close(); } catch (Exception ignored) {}
        }
        
        request.setAttribute("starCount",      stars.size());
        request.setAttribute("stars",      stars);
        request.setAttribute("packed",     topSystems);
        request.setAttribute("heavy",      heavySystems);
        request.setAttribute("searchStar", searchStar);
        request.setAttribute("minPlanets", minPlanetsStr);

        request.getRequestDispatcher("/WEB-INF/views/stars.jsp")
               .forward(request, response);
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}

