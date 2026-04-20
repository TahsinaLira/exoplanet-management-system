package com.exo.dao.impl;

import com.exo.config.Db;
import com.exo.model.PlanetSummary;
import java.util.Map;
import java.util.LinkedHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserDashboardDAO {

    public int getTotalPlanets() throws Exception {
        String sql = "SELECT COUNT(*) FROM planet";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public int getTotalStars() throws Exception {
        String sql = "SELECT COUNT(*) FROM star";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public PlanetSummary getLatestDiscovery() throws Exception {
        String sql = """
            SELECT planet_id, planet, host_star, disc_year, method_name,
                   facility_name, distance_parsec, orbital_period_days
            FROM v_planet_summary
            ORDER BY disc_year DESC
            LIMIT 1
        """;

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
        return null;
    }

    // TOP 5 HOTTEST PLANETS (by planet.equilibrium_temp_k)
public List<PlanetSummary> getTopHottestPlanets() throws Exception {
    String sql = """
        SELECT planet_id,
               name AS planet,
               equilibrium_temp_k AS temp
        FROM planet
        WHERE equilibrium_temp_k IS NOT NULL
        ORDER BY equilibrium_temp_k DESC
        LIMIT 5
    """;

    List<PlanetSummary> list = new ArrayList<>();

    try (Connection cn = Db.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            PlanetSummary p = new PlanetSummary();
            p.setPlanetId(rs.getInt("planet_id"));
            p.setPlanet(rs.getString("planet"));

            double t = rs.getDouble("temp");
            p.setMassEarth(t); // temporarily store temp here

            list.add(p);
        }
    }
    return list;
}

// TOP 5 PACKED STARS (stars with most planets)
public List<Map<String, Object>> getTopPackedStars() throws Exception {
    String sql = """
        SELECT s.name AS star,
               COUNT(p.planet_id) AS total
        FROM star s
        LEFT JOIN planet p ON p.star_id = s.star_id
        GROUP BY s.star_id, s.name
        ORDER BY total DESC
        LIMIT 5
    """;

    List<Map<String, Object>> list = new ArrayList<>();

    try (Connection cn = Db.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Map<String, Object> m = new HashMap<>();
            m.put("star", rs.getString("star"));
            m.put("total", rs.getInt("total"));
            list.add(m);
        }
    }
    return list;
}
    public Map<Integer, Integer> getDiscoveryTrend() throws Exception {
    String sql = """
        SELECT disc_year, COUNT(*) AS total
        FROM planet
        WHERE disc_year IS NOT NULL
        GROUP BY disc_year
        ORDER BY disc_year
    """;

    Map<Integer, Integer> map = new LinkedHashMap<>();

    try (Connection cn = Db.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            int year  = rs.getInt("disc_year");
            int count = rs.getInt("total");
            map.put(year, count);
        }
    }
    return map;
}

    public List<PlanetSummary> getNewestPlanets(int limit) throws Exception {

        String sql = """
            SELECT planet_id, planet, host_star, disc_year
            FROM v_planet_summary
            ORDER BY disc_year DESC
            LIMIT ?
        """;

        List<PlanetSummary> list = new ArrayList<>();

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PlanetSummary p = new PlanetSummary();
                    p.setPlanetId(rs.getInt("planet_id"));
                    p.setPlanet(rs.getString("planet"));
                    p.setHostStar(rs.getString("host_star"));
                    p.setDiscYear(rs.getInt("disc_year"));
                    list.add(p);
                }
            }
        }
        return list;
    }
}