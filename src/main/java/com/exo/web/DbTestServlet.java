package com.exo.web;

import com.exo.config.Db;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

@WebServlet("/dbtest")
public class DbTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("🔍 Starting DB connection test...");
            Db.getConnection().close();
            out.println("✅ DB OK — Connection successful!");
        } catch (Exception e) {
            e.printStackTrace(); // logs to Tomcat console
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.println("❌ DB FAIL: " + e.getMessage());
            }
        }
    }
}