package com.exo.web;

import com.exo.config.Db;
import com.exo.dao.PaperDAO;
import com.exo.dao.impl.PaperDAOImpl;
import com.exo.model.Paper;
import com.exo.util.AuditLogUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.*;

@WebServlet("/admin/papers")
@MultipartConfig(maxFileSize = 20 * 1024 * 1024)
public class UploadPaperServlet extends HttpServlet {

    private final PaperDAO paperDao = new PaperDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            // Load planets for dropdown
            List<Map<String, Object>> planets = loadPlanets();
            req.setAttribute("planets", planets);

            // Load all papers
            List<Paper> papers = paperDao.findAll();
            req.setAttribute("papers", papers);

            req.getRequestDispatcher("/WEB-INF/views/admin_papers.jsp")
               .forward(req, resp);

        } catch (Exception e) {
            throw new ServletException("Load admin papers failed", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        Integer uid = (session != null) ? (Integer) session.getAttribute("uid") : null;

        String action = req.getParameter("action");
        String msg;

        try {
            // ----------------- DELETE PAPER -----------------
            if ("delete".equals(action)) {
                int paperId = Integer.parseInt(req.getParameter("paperId"));
                paperDao.deletePaper(paperId);

                // audit log: DELETE_PAPER
                AuditLogUtil.log(
                        uid,
                        "DELETE_PAPER",
                        "paper",
                        String.valueOf(paperId),
                        "Deleted paper id=" + paperId
                );

                msg = "Paper deleted successfully.";
            }

            // ----------------- ADD / UPLOAD PAPER -----------------
            else {
                int planetId = Integer.parseInt(req.getParameter("planetId"));
                String title = req.getParameter("title");
                String authors = nz(req.getParameter("authors"));
                Integer year = parseIntOrNull(req.getParameter("year"));
                String abs = nz(req.getParameter("abstractText"));

                Part pdfPart = req.getPart("pdfFile");
                String fileName = pdfPart.getSubmittedFileName();
                byte[] fileData;
                try (java.io.InputStream in = pdfPart.getInputStream()) {
                    fileData = in.readAllBytes();
                }

                // Insert into paper table (DAO handles details + maybe user id)
                paperDao.addPaperForPlanet(
                        planetId,
                        title,
                        authors,
                        year,
                        fileName,
                        fileData,
                        abs,
                        uid
                );

                // We may not know the exact paper_id here, so we log with planetId
                AuditLogUtil.log(
                        uid,
                        "ADD_PAPER",
                        "paper",
                        String.valueOf(planetId),
                        "Uploaded paper for planetId=" + planetId +
                                ", title=\"" + title + "\", fileName=" + fileName
                );

                msg = "Paper \"" + title + "\" uploaded successfully.";
            }

        } catch (Exception e) {
            msg = "Error: " + e.getMessage();
        }

        req.setAttribute("flashMsg", msg);
        doGet(req, resp);
    }

    // ------------------ HELPERS ------------------

    private List<Map<String, Object>> loadPlanets() throws Exception {
        List<Map<String, Object>> out = new ArrayList<>();

        String sql =
            "SELECT p.planet_id, p.name AS planet, s.name AS star_name " +
            "FROM planet p " +
            "LEFT JOIN star s ON p.star_id = s.star_id " +
            "ORDER BY p.name";

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("planet_id"));

                String planet = rs.getString("planet");
                String star = rs.getString("star_name");

                if (star == null || star.trim().isEmpty()) {
                    row.put("label", planet + " (star: ?)");
                } else {
                    row.put("label", planet + " (star: " + star + ")");
                }

                out.add(row);
            }
        }

        return out;
    }

    private Integer parseIntOrNull(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String nz(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}