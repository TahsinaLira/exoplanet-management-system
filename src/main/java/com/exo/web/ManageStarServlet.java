package com.exo.web;

import com.exo.config.Db;
import com.exo.util.AuditLogUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/admin/stars")
public class ManageStarServlet extends HttpServlet {

    // ---------------------- GET: LIST + SEARCH + STATS ----------------------
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

            // ---------- MAIN STAR SUMMARY (with optional search by name) ----------
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

            // bind parameters
            for (int i = 0; i < params.size(); i++) {
                Object v = params.get(i);
                if (v instanceof Integer) {
                    ps.setInt(i + 1, (Integer) v);
                } else if (v instanceof String) {
                    ps.setString(i + 1, (String) v);
                } else {
                    ps.setObject(i + 1, v);
                }
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

            // ---------- TOP 5 MOST PACKED SYSTEMS ----------
            String sqlTop =
                "SELECT " +
                "  s.star_id, " +
                "  s.name AS star_name, " +
                "  COUNT(p.planet_id) AS planet_count " +
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

            // ---------- TOP 5 HEAVIEST SYSTEMS (highest avg planet mass) ----------
            String sqlHeavy =
                "SELECT " +
                "  s.star_id, " +
                "  s.name AS star_name, " +
                "  COUNT(p.planet_id)      AS planet_count, " +
                "  AVG(p.mass_earth)       AS avg_mass_earth " +
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
            throw new ServletException("Error loading stars admin page", e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            try { if (cn != null) cn.close(); } catch (Exception ignored) {}
        }

        // send data to JSP
        request.setAttribute("stars",      stars);
        request.setAttribute("packed",     topSystems);
        request.setAttribute("heavy",      heavySystems);
        request.setAttribute("searchStar", searchStar);
        request.setAttribute("minPlanets", minPlanetsStr);

        request.getRequestDispatcher("/WEB-INF/views/admin_stars.jsp")
               .forward(request, response);
    }

    // ------------------------ POST: ADD NEW STAR ------------------------
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if ("add".equalsIgnoreCase(action)) {
            handleAddStar(request, response);
        } else {
            // default: just reload page
            doGet(request, response);
        }
    }

    private void handleAddStar(HttpServletRequest request,
                               HttpServletResponse response)
            throws ServletException, IOException {

        String name       = trim(request.getParameter("name"));
        String type       = trim(request.getParameter("type"));         // maps to spectral_type
        String massStr    = trim(request.getParameter("mass_solar"));
        String radiusStr  = trim(request.getParameter("radius_solar"));

        if (name == null || name.isEmpty()) {
            request.setAttribute("flashError", "Star name is required.");
            doGet(request, response);
            return;
        }

        Double massSolar   = parseDoubleOrNull(massStr);
        Double radiusSolar = parseDoubleOrNull(radiusStr);

        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int newId = 0;

        try {
            cn = Db.getConnection();

            // 1) find some existing system_id (we'll attach new star to it)
            Integer systemId = null;
            try (PreparedStatement psSys =
                     cn.prepareStatement("SELECT MIN(system_id) AS sid FROM planetary_system")) {
                try (ResultSet rsSys = psSys.executeQuery()) {
                    if (rsSys.next()) {
                        int sid = rsSys.getInt("sid");
                        if (!rsSys.wasNull()) {
                            systemId = sid;
                        }
                    }
                }
            }

            if (systemId == null) {
                request.setAttribute("flashError",
                        "Cannot add star: no planetary system found in table 'planetary_system'.");
                doGet(request, response);
                return;
            }

            // 2) insert into star with system_id
            String sqlInsert =
                "INSERT INTO star " +
                " (system_id, name, spectral_type, mass_solar, radius_solar) " +
                "VALUES (?, ?, ?, ?, ?)";

            ps = cn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, systemId);
            ps.setString(2, name);
            if (type == null || type.isEmpty()) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, type);
            }
            if (massSolar == null) {
                ps.setNull(4, Types.DOUBLE);
            } else {
                ps.setDouble(4, massSolar);
            }
            if (radiusSolar == null) {
                ps.setNull(5, Types.DOUBLE);
            } else {
                ps.setDouble(5, radiusSolar);
            }

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        newId = gk.getInt(1);
                    }
                }

                // log in auditlog (same pattern as LoginServlet)
                try {
                    HttpSession session = request.getSession(false);
                    Integer uid = (session != null)
                            ? (Integer) session.getAttribute("uid")
                            : null;
                    if (uid != null) {
                        AuditLogUtil.log(
                                uid,
                                "ADD_STAR",
                                "star",
                                (newId > 0 ? String.valueOf(newId) : null),
                                "Added star: " + name
                        );
                    }
                } catch (Exception logEx) {
                    logEx.printStackTrace();
                }

                request.setAttribute("flashMsg",
                        "Star added successfully (ID " + newId + ").");
            } else {
                request.setAttribute("flashError", "Insert failed, no rows affected.");
            }

        } catch (SQLException e) {
            request.setAttribute("flashError", "Error adding star: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            try { if (cn != null) cn.close(); } catch (Exception ignored) {}
        }

        // reload list (you'll see the new star)
        doGet(request, response);
    }

    // ------------------------ helpers ------------------------
    private String trim(String s) {
        return (s == null) ? null : s.trim();
    }

    private Double parseDoubleOrNull(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return Double.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}