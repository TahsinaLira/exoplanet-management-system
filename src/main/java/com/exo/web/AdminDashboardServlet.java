package com.exo.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("uid") == null) {
            resp.sendRedirect(req.getContextPath() + "/login?next=/admin/dashboard");
            return;
        }
        Object role = s.getAttribute("role");
        if (!"admin".equals(role)) {
            // logged in but not admin → send to user dashboard (or 403 page)
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/views/admin_dashboard.jsp").forward(req, resp);
    }
}