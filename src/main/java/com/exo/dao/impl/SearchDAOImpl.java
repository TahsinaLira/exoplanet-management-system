package com.exo.dao.impl;

import com.exo.config.Db;                 // your existing connection helper
import com.exo.dao.SearchDAO;
import com.exo.model.PlanetSummary;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchDAOImpl implements SearchDAO {

    private static final String BASE_SELECT =
        "SELECT planet_id, planet, host_star, disc_year, method_name, " +
        "       facility_name, distance_parsec, orbital_period_days, image_path " +
        "FROM v_planet_summary " +
        "WHERE (? IS NULL OR planet LIKE CONCAT('%', ?, '%') OR host_star LIKE CONCAT('%', ?, '%')) " +
        "  AND (? IS NULL OR disc_year >= ?) " +
        "  AND (? IS NULL OR disc_year <= ?) " +
        "  AND (? IS NULL OR method_name   LIKE CONCAT('%', ?, '%')) " +
        "  AND (? IS NULL OR facility_name LIKE CONCAT('%', ?, '%')) " +
        "ORDER BY disc_year DESC, planet " +
        "LIMIT ? OFFSET ?";

    private static final String BASE_COUNT =
        "SELECT COUNT(*) " +
        "FROM v_planet_summary " +
        "WHERE (? IS NULL OR planet LIKE CONCAT('%', ?, '%') OR host_star LIKE CONCAT('%', ?, '%')) " +
        "  AND (? IS NULL OR disc_year >= ?) " +
        "  AND (? IS NULL OR disc_year <= ?) " +
        "  AND (? IS NULL OR method_name   LIKE CONCAT('%', ?, '%')) " +
        "  AND (? IS NULL OR facility_name LIKE CONCAT('%', ?, '%'))";

    // helper: convert "" to null
    private String nz(String s) { return (s == null || s.trim().isEmpty()) ? null : s.trim(); }

    @Override
    public List<PlanetSummary> search(String q, Integer yearFrom, Integer yearTo,
                                      String method, String facility,
                                      int limit, int offset) throws Exception {

        List<PlanetSummary> out = new ArrayList<>();
        String Q = nz(q);
        String M = nz(method);
        String F = nz(facility);

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(BASE_SELECT)) {

            int i = 1;
            // (? IS NULL OR planet LIKE ... OR host_star LIKE ...)
            ps.setObject(i++, Q); ps.setObject(i++, Q); ps.setObject(i++, Q);
            // yearFrom
            ps.setObject(i++, yearFrom); ps.setObject(i++, yearFrom);
            // yearTo
            ps.setObject(i++, yearTo);   ps.setObject(i++, yearTo);
            // method
            ps.setObject(i++, M);        ps.setObject(i++, M);
            // facility
            ps.setObject(i++, F);        ps.setObject(i++, F);
            // paging
            ps.setInt(i++, limit);
            ps.setInt(i++, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PlanetSummary row = new PlanetSummary();
                    row.setPlanetId(rs.getInt("planet_id"));
                    row.setPlanet(rs.getString("planet"));
                    row.setHostStar(rs.getString("host_star"));

                    int y = rs.getInt("disc_year");
                    row.setDiscYear(rs.wasNull() ? null : y);

                    row.setMethodName(rs.getString("method_name"));
                    row.setFacilityName(rs.getString("facility_name"));

                    double d = rs.getDouble("distance_parsec");
                    row.setDistanceParsec(rs.wasNull() ? null : d);

                    double p = rs.getDouble("orbital_period_days");
                    row.setOrbitalPeriodDays(rs.wasNull() ? null : p);
                    row.setImagePath(rs.getString("image_path"));

                    out.add(row);
                }
            }
        }
        return out;
    }

    @Override
    public int count(String q, Integer yearFrom, Integer yearTo,
                     String method, String facility) throws Exception {

        String Q = nz(q);
        String M = nz(method);
        String F = nz(facility);

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(BASE_COUNT)) {

            int i = 1;
            ps.setObject(i++, Q); ps.setObject(i++, Q); ps.setObject(i++, Q);
            ps.setObject(i++, yearFrom); ps.setObject(i++, yearFrom);
            ps.setObject(i++, yearTo);   ps.setObject(i++, yearTo);
            ps.setObject(i++, M);        ps.setObject(i++, M);
            ps.setObject(i++, F);        ps.setObject(i++, F);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
    
    
public PlanetSummary findById(int planetId) throws Exception {
    String sql = "SELECT planet_id, planet, host_star, disc_year, method_name, " +
                 "facility_name, distance_parsec, orbital_period_days " +
                 "FROM v_planet_summary WHERE planet_id = ?";
    try (var cn = com.exo.config.Db.getConnection();
         var ps = cn.prepareStatement(sql)) {
        ps.setInt(1, planetId);
        try (var rs = ps.executeQuery()) {
            if (rs.next()) {
                PlanetSummary p = new PlanetSummary();
                p.setPlanetId(rs.getInt("planet_id"));
                p.setPlanet(rs.getString("planet"));
                p.setHostStar(rs.getString("host_star"));
                p.setDiscYear(rs.getInt("disc_year"));
                p.setMethodName(rs.getString("method_name"));
                p.setFacilityName(rs.getString("facility_name"));
                p.setDistanceParsec(rs.getDouble("distance_parsec"));
                p.setOrbitalPeriodDays(rs.getDouble("orbital_period_days"));
                return p;
            }
        }
    }
    return null;
}
}
