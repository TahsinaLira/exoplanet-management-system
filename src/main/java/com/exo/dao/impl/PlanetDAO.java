package com.exo.dao.impl;

import com.exo.config.Db;
import com.exo.model.PlanetDetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PlanetDAO {

    public PlanetDetails getPlanetById(int id) throws Exception {
        String sql = """
            SELECT p.planet_id, p.name AS planet, s.name AS host_star,
                   p.mass_earth, p.radius_earth, p.equilibrium_temp_k,
                   p.disc_year, p.discovery_method, p.disc_facility,
                   p.insolation_flux, p.is_controversial,
                   o.orbital_period_days, o.semimajor_axis, o.eccentricity,
                   s.distance_parsec, s.spectral_type, s.temperature_k
            FROM Planet p
            JOIN Star s ON s.star_id = p.star_id
            LEFT JOIN Orbit o ON o.planet_id = p.planet_id
            WHERE p.planet_id = ?
        """;

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                PlanetDetails d = new PlanetDetails();
                d.setPlanetId(rs.getInt("planet_id"));
                d.setPlanet(rs.getString("planet"));
                d.setHostStar(rs.getString("host_star"));

                d.setMass(getDoubleOrNull(rs, "mass_earth"));
                d.setRadius(getDoubleOrNull(rs, "radius_earth"));
                d.setTemp(getDoubleOrNull(rs, "equilibrium_temp_k"));

                d.setDiscYear(getIntOrNull(rs, "disc_year"));
                d.setMethod(rs.getString("discovery_method"));
                d.setFacility(rs.getString("disc_facility"));

                d.setInsolation(getDoubleOrNull(rs, "insolation_flux"));
                d.setControversial(getBooleanOrNull(rs, "is_controversial"));

                d.setPeriod(getDoubleOrNull(rs, "orbital_period_days"));
                d.setSemimajorAxis(getDoubleOrNull(rs, "semimajor_axis"));
                d.setEccentricity(getDoubleOrNull(rs, "eccentricity"));

                d.setDistance(getDoubleOrNull(rs, "distance_parsec"));
                d.setSpectralType(rs.getString("spectral_type"));
                d.setStarTemp(getDoubleOrNull(rs, "temperature_k"));
                return d;
            }
        }
    }
    
    public void updatePlanetBasics(PlanetDetails d) throws Exception {
    String sql =
        "UPDATE planet SET " +
        " mass_earth = ?, " +
        " radius_earth = ?, " +
        " equilibrium_temp_k = ?, " +
        " disc_year = ?, " +
        " discovery_method = ?, " +
        " disc_facility = ?, " +
        " insolation_flux = ?, " +
        " is_controversial = ? " +
        "WHERE planet_id = ?";

    try (Connection cn = Db.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        // 1 mass
        if (d.getMass() == null) ps.setNull(1, java.sql.Types.DOUBLE);
        else ps.setDouble(1, d.getMass());

        // 2 radius
        if (d.getRadius() == null) ps.setNull(2, java.sql.Types.DOUBLE);
        else ps.setDouble(2, d.getRadius());

        // 3 temp
        if (d.getTemp() == null) ps.setNull(3, java.sql.Types.DOUBLE);
        else ps.setDouble(3, d.getTemp());

        // 4 disc_year
        if (d.getDiscYear() == null) ps.setNull(4, java.sql.Types.INTEGER);
        else ps.setInt(4, d.getDiscYear());

        // 5 method
        ps.setString(5, d.getMethod());

        // 6 facility
        ps.setString(6, d.getFacility());

        // 7 insolation_flux
        if (d.getInsolation() == null) ps.setNull(7, java.sql.Types.DOUBLE);
        else ps.setDouble(7, d.getInsolation());

        // 8 is_controversial (tinyint(1))
        if (d.getControversial() == null) {
            ps.setNull(8, java.sql.Types.TINYINT);
        } else {
            ps.setBoolean(8, d.getControversial());
        }

        // 9 WHERE planet_id = ?
        ps.setInt(9, d.getPlanetId());

        ps.executeUpdate();
    }
}

    private Double getDoubleOrNull(ResultSet rs, String col) throws Exception {
        double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
        }
    private Integer getIntOrNull(ResultSet rs, String col) throws Exception {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }
    private Boolean getBooleanOrNull(ResultSet rs, String col) throws Exception {
        boolean b = rs.getBoolean(col);
        return rs.wasNull() ? null : b;
    }
}