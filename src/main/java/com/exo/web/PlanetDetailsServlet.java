 package com.exo.web;

import com.exo.dao.SearchDAO;
import com.exo.dao.impl.SearchDAOImpl;
import com.exo.model.PlanetSummary;
import com.exo.config.Db;
import com.exo.dao.PaperDAO;
import com.exo.dao.impl.PaperDAOImpl;
import com.exo.model.Paper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

@WebServlet("/planet")
public class PlanetDetailsServlet extends HttpServlet {

    private final SearchDAO dao = new SearchDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/search");
            return;
        }

        try {
            int planetId = Integer.parseInt(idStr);

            // 1) Load the basic row from your view (SearchDAO.findById already implemented)
            PlanetSummary planet = dao.findById(planetId);
            if (planet == null) {
                // Not found → simple message page
                req.setAttribute("error", "Planet not found (id=" + planetId + ")");
                req.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(req, resp);
                return;
            }
            req.setAttribute("planet", planet);
            // --- choose a picture for the Overview card ---
String imgKey = "unknown";
if (planet != null) {
    String n = planet.getPlanet() == null ? "" : planet.getPlanet().toLowerCase();
    String m = planet.getMethodName() == null ? "" : planet.getMethodName().toLowerCase();

    // simple heuristics; tweak as you like
    if (planet.getOrbitalPeriodDays() != null && planet.getOrbitalPeriodDays() < 5) {
        imgKey = "lava_world";
    } else if (m.contains("transit")) {
        imgKey = "water_world";
    } else if (m.contains("radial")) {
        imgKey = "neptune_like";
    } else if (n.contains("c") || n.contains("d")) {
        imgKey = "mini_neptune";
    } else if (n.contains("b")) {
        imgKey = "rocky";
    } else {
        imgKey = "ice_world";
    }
}
// Build a web URL that works behind the app’s context path
String imgUrl = req.getContextPath() + "/assets/img/planets/" + imgKey + ".png";
req.setAttribute("imgUrl", imgUrl);

            // 2) Fetch extra fields for the middle description panel
            String extraSql =
                "SELECT p.mass_earth, p.radius_earth, p.equilibrium_temp_k, " +
                "       o.semimajor_axis, o.eccentricity, " +
                "       s.spectral_type, s.temperature_k, s.radius_solar, s.mass_solar, " +
                "       s.metallicity_dex, s.surface_gravity, " +
                "       s.right_ascension_degree, s.declination_degree " +
                "FROM Planet p " +
                "JOIN Star s ON s.star_id = p.star_id " +
                "LEFT JOIN Orbit o ON o.planet_id = p.planet_id " +
                "WHERE p.planet_id = ?";

            Double massE=null, radiusE=null, eqT=null, sma=null, ecc=null,
                   sTemp=null, sRad=null, sMass=null, metal=null, logg=null, ra=null, dec=null;
            String sType=null;

            try (Connection cn = Db.getConnection();
                 PreparedStatement ps = cn.prepareStatement(extraSql)) {
                ps.setInt(1, planetId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        massE   = getD(rs, "mass_earth");
                        radiusE = getD(rs, "radius_earth");
                        eqT     = getD(rs, "equilibrium_temp_k");
                        sma     = getD(rs, "semimajor_axis");
                        ecc     = getD(rs, "eccentricity");
                        sType   = rs.getString("spectral_type");
                        sTemp   = getD(rs, "temperature_k");
                        sRad    = getD(rs, "radius_solar");
                        sMass   = getD(rs, "mass_solar");
                        metal   = getD(rs, "metallicity_dex");
                        logg    = getD(rs, "surface_gravity");
                        ra      = getD(rs, "right_ascension_degree");
                        dec     = getD(rs, "declination_degree");
                    }
                }
            }

            // expose raw values to JSP (for right-column bullets if you want)
            req.setAttribute("massE", massE);
            req.setAttribute("radiusE", radiusE);
            req.setAttribute("eqT", eqT);
            req.setAttribute("sma", sma);
            req.setAttribute("ecc", ecc);
            req.setAttribute("sType", sType);
            req.setAttribute("sTemp", sTemp);
            req.setAttribute("sRad", sRad);
            req.setAttribute("sMass", sMass);
            req.setAttribute("metal", metal);
            req.setAttribute("logg", logg);
            req.setAttribute("ra", ra);
            req.setAttribute("dec", dec);
            
         PaperDAO paperDao = new PaperDAOImpl();
List<Paper> papers = paperDao.findByPlanet(planetId);
req.setAttribute("papers", papers);
            // 3) Build a readable summary paragraph for the middle column
            String planetName = nz(planet.getPlanet(), "This exoplanet");
            String host       = nz(planet.getHostStar(), "its host star");
            String method     = planet.getMethodName();
            String facility   = planet.getFacilityName();
            Integer year      = planet.getDiscYear();
            Double dist       = planet.getDistanceParsec();
            Double period     = planet.getOrbitalPeriodDays();

            java.util.function.Function<Double,String> f0 = d -> d==null? null : String.format("%.0f", d);
            java.util.function.Function<Double,String> f1 = d -> d==null? null : String.format("%.1f", d);
            java.util.function.Function<Double,String> f2 = d -> d==null? null : String.format("%.2f", d);

            StringBuilder html = new StringBuilder();
            html.append("<p><b>").append(escape(planetName)).append("</b>");
            if (method!=null || year!=null || facility!=null) {
                html.append(" was discovered");
                if (year!=null) html.append(" in ").append(year);
                if (method!=null) html.append(" using the ").append(escape(method)).append(" method");
                if (facility!=null) html.append(" at ").append(escape(facility));
                html.append(".");
            } else {
                html.append(" is a confirmed exoplanet.");
            }
            html.append(" It orbits the star <b>").append(escape(host)).append("</b>");
            if (period!=null) html.append(" with an orbital period of <b>").append(f1.apply(period)).append(" days</b>");
            if (sma!=null)    html.append(" at a semi-major axis of <b>").append(f2.apply(sma)).append(" AU</b>");
            if (ecc!=null)    html.append(" and orbital eccentricity <b>").append(f2.apply(ecc)).append("</b>");
            html.append(".");

            if (massE!=null || radiusE!=null || eqT!=null) {
                html.append(" Physically, <b>").append(escape(planetName)).append("</b>");
                boolean first = true;
                if (massE!=null)   { html.append(" has a mass of <b>").append(f2.apply(massE)).append(" Earths</b>"); first=false; }
                if (radiusE!=null) { html.append(first? " has " : ", ").append("a radius of <b>").append(f2.apply(radiusE)).append(" Earth radii</b>"); first=false; }
                if (eqT!=null)     { html.append(first? " has " : ", and ").append("an equilibrium temperature of <b>").append(f0.apply(eqT)).append(" K</b>"); }
                html.append(".");
            }

            if (dist!=null) {
                html.append(" The system is approximately <b>").append(f1.apply(dist)).append(" parsecs</b> from Earth.");
            }

            if (sType!=null || sTemp!=null || sRad!=null || sMass!=null || metal!=null || logg!=null) {
                html.append(" The host star");
                boolean first = true;
                if (sType!=null) { html.append(" is <b>").append(escape(sType)).append("</b>"); first=false; }
                if (sTemp!=null) { html.append(first? " has " : ", ").append("T<sub>eff</sub> <b>").append(f0.apply(sTemp)).append(" K</b>"); first=false; }
                if (sRad!=null)  { html.append(first? " has " : ", ").append("radius <b>").append(f2.apply(sRad)).append(" R&odot;</b>"); first=false; }
                if (sMass!=null) { html.append(first? " has " : ", ").append("mass <b>").append(f2.apply(sMass)).append(" M&odot;</b>"); first=false; }
                if (metal!=null) { html.append(first? " has " : ", ").append("metallicity <b>").append(f2.apply(metal)).append(" dex</b>"); first=false; }
                if (logg!=null)  { html.append(first? " has " : ", ").append("log g <b>").append(f2.apply(logg)).append("</b>"); }
                html.append(".");
            }

            if (ra!=null || dec!=null) {
                html.append(" Sky position:");
                if (ra!=null)  html.append(" RA <b>").append(f2.apply(ra)).append("&deg;</b>");
                if (dec!=null) html.append(", Dec <b>").append(f2.apply(dec)).append("&deg;</b>");
                html.append(".");
            }
            

            req.setAttribute("descriptionHtml", html.toString());

            // 4) Forward to JSP
            req.getRequestDispatcher("/WEB-INF/views/planet.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException("Planet details load failed", e);
        }
    }

    // ---- helpers ----
    private static Double getD(ResultSet rs, String col) throws Exception {
        double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
    }

    private static String nz(String s, String defVal) {
        return (s == null || s.trim().isEmpty()) ? defVal : s;
    }

    // very small HTML-escape for safety
    private static String escape(String s) {
        if (s == null) return null;
        return s.replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\"","&quot;");
    }
}