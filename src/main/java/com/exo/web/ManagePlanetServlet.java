package com.exo.web;

import com.exo.config.Db;
import com.exo.util.AuditLogUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/admin/planets")
public class ManagePlanetServlet extends HttpServlet {

    // ===================== GET: LIST + FILTER + PAGINATION =====================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // --------- 1. READ FILTERS ----------
        String yearMinStr = req.getParameter("yearMin");
        String yearMaxStr = req.getParameter("yearMax");
        String method     = req.getParameter("method");
        String facility   = req.getParameter("facility");
        String massMinStr = req.getParameter("massMin");
        String massMaxStr = req.getParameter("massMax");

        Integer yearMin = parseIntOrNull(yearMinStr);
        Integer yearMax = parseIntOrNull(yearMaxStr);
        Double  massMin = parseDoubleOrNull(massMinStr);
        Double  massMax = parseDoubleOrNull(massMaxStr);

        // --------- 2. PAGINATION ----------
        int pageSize = 100;  // how many planets per page
        int page = 1;
        try {
            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.isBlank()) {
                page = Integer.parseInt(pageStr.trim());
                if (page < 1) page = 1;
            }
        } catch (NumberFormatException ignored) {}
        int offset = (page - 1) * pageSize;

        List<Map<String,Object>> planets = new ArrayList<>();
        boolean hasNext = false;

        // --------- 3. BUILD SQL WITH FILTERS + PAGINATION ----------
        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "  p.planet_id, " +
            "  p.name              AS planet, " +
            "  s.name              AS star_name, " +
            "  p.disc_year, " +
            "  p.discovery_method, " +
            "  p.disc_facility " +
            "FROM planet p " +
            "LEFT JOIN star s ON s.star_id = p.star_id " +
            "WHERE 1=1 "
        );
        // --- SEARCH BY NAME ---

        List<Object> params = new ArrayList<>();
        String q = req.getParameter("q");
if (q != null && !q.isBlank()) {
    sql.append(" AND p.name LIKE ? ");
    params.add("%" + q.trim() + "%");
}


        if (yearMin != null) {
            sql.append(" AND p.disc_year >= ? ");
            params.add(yearMin);
        }
        if (yearMax != null) {
            sql.append(" AND p.disc_year <= ? ");
            params.add(yearMax);
        }
        if (method != null && !method.isBlank()) {
            sql.append(" AND p.discovery_method = ? ");
            params.add(method);
        }
        if (facility != null && !facility.isBlank()) {
            sql.append(" AND p.disc_facility = ? ");
            params.add(facility);
        }
        if (massMin != null) {
            sql.append(" AND p.mass_earth >= ? ");
            params.add(massMin);
        }
        if (massMax != null) {
            sql.append(" AND p.mass_earth <= ? ");
            params.add(massMax);
        }

        sql.append(" ORDER BY p.disc_year DESC, p.name ");
        sql.append(" LIMIT ? OFFSET ? ");
        params.add(pageSize);
        params.add(offset);

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            // bind parameters in order
            for (int i = 0; i < params.size(); i++) {
                Object val = params.get(i);
                if (val instanceof Integer) {
                    ps.setInt(i + 1, (Integer) val);
                } else if (val instanceof Double) {
                    ps.setDouble(i + 1, (Double) val);
                } else if (val instanceof String) {
                    ps.setString(i + 1, (String) val);
                } else {
                    ps.setObject(i + 1, val);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> row = new HashMap<>();

                    row.put("planet_id",   rs.getInt("planet_id"));
                    row.put("planet",      rs.getString("planet"));
                    row.put("star_name",   rs.getString("star_name"));

                    int y = rs.getInt("disc_year");
                    row.put("disc_year", rs.wasNull() ? null : y);

                    row.put("discovery_method", rs.getString("discovery_method"));
                    row.put("disc_facility",    rs.getString("disc_facility"));

                    planets.add(row);
                }
            }

            hasNext = (planets.size() == pageSize);

        } catch (Exception e) {
            throw new ServletException("Error loading planets admin page", e);
        }
        List<Map<String,Object>> stars = new ArrayList<>();

try (Connection cn2 = Db.getConnection();
     PreparedStatement ps2 = cn2.prepareStatement(
             "SELECT star_id, name FROM star ORDER BY name ASC"
     );
     ResultSet rs2 = ps2.executeQuery()) {

    while (rs2.next()) {
        Map<String,Object> s = new HashMap<>();
        s.put("star_id", rs2.getInt("star_id"));
        s.put("name", rs2.getString("name"));
        stars.add(s);
    }

} catch (Exception ex) {
    throw new ServletException("Could not load stars list", ex);
}

       req.setAttribute("stars",stars);

        // --------- 4. SEND TO JSP ----------
        req.setAttribute("planets", planets);
        req.setAttribute("page", page);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("hasNext", hasNext);

        req.getRequestDispatcher("/WEB-INF/views/admin_planets.jsp")
           .forward(req, resp);
    }

    // ===================== POST: ADD / DELETE =====================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        String msg;

        // current logged-in user (for audit)
        HttpSession session = req.getSession(false);
        Integer uid = (session != null) ? (Integer) session.getAttribute("uid") : null;

        if ("delete".equalsIgnoreCase(action)) {
            msg = handleDelete(req, uid);
        } else if ("add".equalsIgnoreCase(action)) {
            msg = handleAdd(req, uid);
        } else {
            msg = "Unknown action.";
        }

        req.setAttribute("flashMsg", msg);
        doGet(req, resp);   // reload list
    }

    // --------- DELETE PLANET (safe with child rows) ----------
    private String handleDelete(HttpServletRequest req, Integer uid) {
        Integer planetId = parseIntOrNull(req.getParameter("planetId"));
        if (planetId == null) {
            return "Invalid planet id.";
        }

        try (Connection cn = Db.getConnection()) {
            cn.setAutoCommit(false);

            // 1) delete child rows first (known FK: discovery → planet)
            try (PreparedStatement psChild =
                         cn.prepareStatement("DELETE FROM discovery WHERE planet_id = ?")) {
                psChild.setInt(1, planetId);
                psChild.executeUpdate();
            }

            // TODO: if you later add other tables with planet_id FK,
            // delete from them here (e.g., planetpaper, observation, etc.)

            // 2) delete the planet itself
            int rows;
            try (PreparedStatement ps =
                         cn.prepareStatement("DELETE FROM planet WHERE planet_id = ?")) {
                ps.setInt(1, planetId);
                rows = ps.executeUpdate();
            }

            cn.commit();
            cn.setAutoCommit(true);

            if (rows > 0) {
                // log delete if something was actually removed
                AuditLogUtil.log(
                        uid,
                        "DELETE_PLANET",
                        "planet",
                        String.valueOf(planetId),
                        "Planet deleted from admin panel"
                );
                return "Planet deleted (ID " + planetId + ").";
            } else {
                return "No planet found with ID " + planetId + ".";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error deleting planet: " + e.getMessage();
        }
    }

    // --------- ADD NEW PLANET ----------
    private String handleAdd(HttpServletRequest req, Integer uid) {

        String name      = trim(req.getParameter("name"));
        Integer starId   = parseIntOrNull(req.getParameter("starId"));
        Integer discYear = parseIntOrNull(req.getParameter("discYear"));
        String method    = trim(req.getParameter("discoveryMethod"));
        String facility  = trim(req.getParameter("discFacility"));
        Double massEarth = parseDoubleOrNull(req.getParameter("massEarth")); // optional

        if (name == null || name.isEmpty()) {
            return "Planet name is required.";
        }

        String sql =
            "INSERT INTO planet " +
            " (name, star_id, disc_year, discovery_method, disc_facility, mass_earth) " +
            "VALUES (?,?,?,?,?,?)";

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);

            if (starId == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, starId);

            if (discYear == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, discYear);

            ps.setString(4, method);
            ps.setString(5, facility);

            if (massEarth == null) ps.setNull(6, Types.DOUBLE);
            else ps.setDouble(6, massEarth);

            ps.executeUpdate();

            Integer newId = null;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    newId = keys.getInt(1);
                }
            }

            // audit log
            AuditLogUtil.log(
                    uid,
                    "ADD_PLANET",
                    "planet",
                    (newId == null ? "?" : String.valueOf(newId)),
                    "Planet added via admin panel: " + name
            );

            return "Planet \"" + name + "\" added successfully (ID " + newId + ").";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error adding planet: " + e.getMessage();
        }
    }

    // ===================== HELPERS =====================
    private Integer parseIntOrNull(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDoubleOrNull(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return Double.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String trim(String s) {
        return (s == null) ? null : s.trim();
    }
}