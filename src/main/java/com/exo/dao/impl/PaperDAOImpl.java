package com.exo.dao.impl;

import com.exo.config.Db;
import com.exo.dao.PaperDAO;
import com.exo.model.Paper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaperDAOImpl implements PaperDAO {

    @Override
    public List<Paper> findByPlanet(int planetId) throws Exception {
        List<Paper> out = new ArrayList<>();

        String sql =
            "SELECT p.paper_id, p.title, p.authors, p.year, " +
            "       p.file_name, p.abstract, p.uploaded_by, p.uploaded_at " +
            "FROM paper p " +
            "JOIN planetpaper pp ON pp.paper_id = p.paper_id " +
            "WHERE pp.planet_id = ? " +
            "ORDER BY p.year DESC, p.title";

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, planetId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Paper p = map(rs);
                    p.setPlanetId(planetId);
                    out.add(p);
                }
            }
        }

        return out;
    }

    @Override
    public List<Paper> findAll() throws Exception {
        List<Paper> out = new ArrayList<>();

        String sql =
            "SELECT p.paper_id, p.title, p.authors, p.year, " +
            "       p.file_name, p.abstract, p.uploaded_by, p.uploaded_at, " +
            "       pl.planet_id, pl.name AS planet_name " +
            "FROM paper p " +
            "LEFT JOIN planetpaper pp ON pp.paper_id = p.paper_id " +
            "LEFT JOIN planet pl ON pl.planet_id = pp.planet_id " +
            "ORDER BY p.uploaded_at DESC, p.paper_id DESC";

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Paper p = map(rs);

                int planetId = rs.getInt("planet_id");
                if (!rs.wasNull()) {
                    p.setPlanetId(planetId);
                }
                p.setPlanetName(rs.getString("planet_name"));

                out.add(p);
            }
        }

        return out;
    }

    @Override
    public void addPaperForPlanet(int planetId,
                                  String title,
                                  String authors,
                                  Integer year,
                                  String fileName,
                                  byte[] fileData,
                                  String abstractText,
                                  Integer uploadedBy) throws Exception {

        String insertPaper =
            "INSERT INTO paper(title, authors, year, file_name, file_data, abstract, uploaded_by, uploaded_at) " +
            "VALUES(?,?,?,?,?,?,?, NOW())";

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(insertPaper, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, title);
            ps.setString(2, authors);

            if (year == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, year);
            ps.setString(4, fileName);

            if (fileData == null) {
                ps.setNull(5, Types.LONGVARBINARY);
            } else {
                ps.setBytes(5, fileData);
            }

            ps.setString(6, abstractText);

            if (uploadedBy == null) {
                ps.setNull(7, Types.INTEGER);
            } else {
                ps.setInt(7, uploadedBy);
            }

            ps.executeUpdate();

            int paperId;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated key for paper");
                }
                paperId = keys.getInt(1);
            }

            String linkSql = "INSERT INTO planetpaper(planet_id, paper_id) VALUES(?,?)";
            try (PreparedStatement ps2 = cn.prepareStatement(linkSql)) {
                ps2.setInt(1, planetId);
                ps2.setInt(2, paperId);
                ps2.executeUpdate();
            }
        }
    }

    @Override
    public void deletePaper(int paperId) throws Exception {
        try (Connection cn = Db.getConnection()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement(
                        "DELETE FROM planetpaper WHERE paper_id = ?")) {
                    ps.setInt(1, paperId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = cn.prepareStatement(
                        "DELETE FROM paper WHERE paper_id = ?")) {
                    ps.setInt(1, paperId);
                    ps.executeUpdate();
                }

                cn.commit();
            } catch (Exception e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    @Override
    public Paper findFileById(int paperId) throws Exception {
        String sql =
            "SELECT paper_id, title, file_name, file_data " +
            "FROM paper WHERE paper_id = ?";

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, paperId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Paper p = new Paper();
                p.setPaperId(rs.getInt("paper_id"));
                p.setTitle(rs.getString("title"));
                p.setFileName(rs.getString("file_name"));
                p.setFileData(rs.getBytes("file_data"));
                return p;
            }
        }
    }

    // ---------- PRIVATE HELPER ----------

    private Paper map(ResultSet rs) throws Exception {
        Paper p = new Paper();

        p.setPaperId(rs.getInt("paper_id"));
        p.setTitle(rs.getString("title"));
        p.setAuthors(rs.getString("authors"));

        int y = rs.getInt("year");
        p.setYear(rs.wasNull() ? null : y);

        p.setFileName(rs.getString("file_name"));
        p.setAbstractText(rs.getString("abstract"));

        int u = rs.getInt("uploaded_by");
        p.setUploadedBy(rs.wasNull() ? null : u);

        Timestamp ts = rs.getTimestamp("uploaded_at");
        p.setUploadedAt(ts);

        // we do NOT fetch file_data here (too heavy for list view)
        return p;
    }
}