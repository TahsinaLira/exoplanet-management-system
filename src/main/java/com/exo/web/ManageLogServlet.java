package com.exo.web;

import com.exo.config.Db;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/admin/logs")
public class ManageLogServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String userFilter   = trimOrNull(request.getParameter("username"));
        String actionFilter = trimOrNull(request.getParameter("action"));
        String fromDateStr  = trimOrNull(request.getParameter("fromDate"));
        String toDateStr    = trimOrNull(request.getParameter("toDate"));

        List<Map<String,Object>> logs          = new ArrayList<>();
        List<Map<String,Object>> topAdmins     = new ArrayList<>();
        List<Map<String,Object>> uploadsPerDay = new ArrayList<>();

        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            cn = Db.getConnection();

            // ===== MAIN LOG LIST =====
            StringBuilder sql = new StringBuilder(
                    "SELECT l.log_id, l.created_at, " +
                    "       u.username, " +
                    "       l.action, l.entity, l.entity_id, l.notes " +
                    "FROM auditlog l " +
                    "LEFT JOIN appuser u ON u.user_id = l.user_id " +
                    "WHERE 1=1 "
            );

            List<Object> params = new ArrayList<>();

            if (userFilter != null) {
                sql.append(" AND u.username = ? ");
                params.add(userFilter);
            }
            if (actionFilter != null) {
                sql.append(" AND l.action = ? ");
                params.add(actionFilter);
            }
            if (fromDateStr != null) {
                sql.append(" AND l.created_at >= ? ");
                params.add(Timestamp.valueOf(fromDateStr + " 00:00:00"));
            }
            if (toDateStr != null) {
                sql.append(" AND l.created_at < DATE_ADD(?, INTERVAL 1 DAY) ");
                params.add(Timestamp.valueOf(toDateStr + " 00:00:00"));
            }

            sql.append(" ORDER BY l.created_at DESC ");
            sql.append(" LIMIT 300 ");

            ps = cn.prepareStatement(sql.toString());
            int idx = 1;
            for (Object p : params) {
                if (p instanceof Timestamp) {
                    ps.setTimestamp(idx++, (Timestamp) p);
                } else {
                    ps.setObject(idx++, p);
                }
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> row = new HashMap<>();
                row.put("log_id",     rs.getLong("log_id"));
                row.put("created_at", rs.getTimestamp("created_at"));
                row.put("username",   rs.getString("username"));
                row.put("action",     rs.getString("action"));
                row.put("entity",     rs.getString("entity"));
                row.put("entity_id",  rs.getString("entity_id"));
                row.put("notes",      rs.getString("notes"));
                logs.add(row);
            }
            rs.close();
            ps.close();

            // ===== TOP ADMINS (LAST 7 DAYS) =====
            String sqlTopAdmins =
                    "SELECT u.username, COUNT(*) AS action_count " +
                    "FROM auditlog l " +
                    "JOIN appuser u ON u.user_id = l.user_id " +
                    "WHERE l.created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                    "GROUP BY u.username " +
                    "ORDER BY action_count DESC " +
                    "LIMIT 5";

            ps = cn.prepareStatement(sqlTopAdmins);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> row = new HashMap<>();
                row.put("username",     rs.getString("username"));
                row.put("action_count", rs.getInt("action_count"));
                topAdmins.add(row);
            }
            rs.close();
            ps.close();

            // ===== PAPER UPLOADS PER DAY (LAST 10 DAYS) =====
            // Adjust action/entity string if you log uploads differently
            String sqlUploads =
                    "SELECT DATE(l.created_at) AS day, COUNT(*) AS uploads " +
                    "FROM auditlog l " +
                    "WHERE l.action = 'ADD_PAPER' " +
                    "GROUP BY DATE(l.created_at) " +
                    "HAVING day >= DATE_SUB(CURDATE(), INTERVAL 10 DAY) " +
                    "ORDER BY day DESC";

            ps = cn.prepareStatement(sqlUploads);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> row = new HashMap<>();
                row.put("day",     rs.getDate("day"));
                row.put("uploads", rs.getInt("uploads"));
                uploadsPerDay.add(row);
            }

        } catch (SQLException e) {
            throw new ServletException("Error loading logs admin page", e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            try { if (cn != null) cn.close(); } catch (Exception ignored) {}
        }

        request.setAttribute("logs",          logs);
        request.setAttribute("topAdmins",     topAdmins);
        request.setAttribute("uploadsPerDay", uploadsPerDay);

        request.getRequestDispatcher("/WEB-INF/views/admin_logs.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}