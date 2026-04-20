package com.exo.web;

import com.exo.dao.UserDAO;
import com.exo.dao.impl.UserDAOImpl;
import com.exo.model.User;
import com.exo.util.AuditLogUtil;   // <-- NEW IMPORT

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDao = new UserDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String username = trim(req.getParameter("username"));
        String password = req.getParameter("password");
        if (isBlank(username) || isBlank(password)) {
            fail("Username and password are required", req, resp);
            return;
        }

        try {
            User u = userDao.findByUsername(username);
            if (u == null) {
                fail("Invalid username or password", req, resp);
                return;
            }

            // Hash exactly the same way as in RegisterServlet
            String hash = sha256(username + ":" + password);
            if (!hash.equals(u.getPassHash())) {
                fail("Invalid username or password", req, resp);
                return;
            }

            // success → set session, redirect
            HttpSession session = req.getSession(true);
            session.setAttribute("uid", u.getUserId());
            session.setAttribute("username", u.getUsername());
            session.setAttribute("role", u.getRole());

            // 🔹 AUDIT LOG: successful login
            AuditLogUtil.log(
                    u.getUserId(),           // user_id
                    "LOGIN",                 // action
                    "user",                  // entity
                    String.valueOf(u.getUserId()), // entity_id
                    "User logged in"         // notes
            );

            // support ?next=/paper/download?id=...
            String next = req.getParameter("next");
            if (next != null && next.startsWith("/")) {
                resp.sendRedirect(req.getContextPath() + next);
                return;
            }

            // role-based landing
            String role = u.getRole();
            if ("admin".equalsIgnoreCase(role)) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            } else {
                resp.sendRedirect(req.getContextPath() + "/dashboard");
            }
        } catch (Exception e) {
            fail("Login failed: " + e.getMessage(), req, resp);
        }
    }

    private void fail(String msg, HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        req.setAttribute("error", msg);
        doGet(req, resp);
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private String trim(String s) { return s == null ? null : s.trim(); }

    private String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}