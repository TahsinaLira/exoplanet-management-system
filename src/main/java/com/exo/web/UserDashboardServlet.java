package com.exo.web;

import com.exo.dao.impl.UserDashboardDAO;
import com.exo.model.PlanetSummary;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/dashboard")
public class UserDashboardServlet extends HttpServlet {

    private final UserDashboardDAO dao = new UserDashboardDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("uid") == null) {
            resp.sendRedirect(req.getContextPath() + "/login?next=/dashboard");
            return;
        }

        try {
            // left column
            req.setAttribute("totalPlanets", dao.getTotalPlanets());
            req.setAttribute("totalStars", dao.getTotalStars());
            req.setAttribute("latestDiscovery", dao.getLatestDiscovery());

            // right column
            List<PlanetSummary> hottestList = dao.getTopHottestPlanets();
            List<Map<String, Object>> packedStars = dao.getTopPackedStars();
            req.setAttribute("hottestList", hottestList);
            req.setAttribute("packedStars", packedStars);

            // center chart uses separate servlet /data/discovery-trend (already set up)

            req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp")
               .forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}