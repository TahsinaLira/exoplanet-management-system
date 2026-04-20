package com.exo.web;

import com.exo.dao.impl.UserDashboardDAO;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/data/discovery-trend")
public class DiscoveryTrendServlet extends HttpServlet {

    private final UserDashboardDAO dao = new UserDashboardDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            Map<Integer, Integer> trend = dao.getDiscoveryTrend();

            StringBuilder json = new StringBuilder();
            json.append("{\"years\":[");
            boolean first = true;

            for (Map.Entry<Integer, Integer> e : trend.entrySet()) {
                if (!first) json.append(",");
                json.append(e.getKey());
                first = false;
            }

            json.append("],\"counts\":[");
            first = true;

            for (Map.Entry<Integer, Integer> e : trend.entrySet()) {
                if (!first) json.append(",");
                json.append(e.getValue());
                first = false;
            }

            json.append("]}");

            resp.getWriter().write(json.toString());

        } catch (Exception e) {
            resp.getWriter().write("{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}