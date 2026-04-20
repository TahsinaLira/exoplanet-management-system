package com.exo.web;

import com.exo.dao.UserDAO;
import com.exo.dao.impl.UserDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserDAO userDao = new UserDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String username = trim(req.getParameter("username"));
        String email    = trim(req.getParameter("email"));
        String pass     = req.getParameter("password");
        String confirm  = req.getParameter("confirm");

        // Basic validation
        if (isBlank(username) || isBlank(email) || isBlank(pass) || isBlank(confirm)) {
            fail("All fields are required", req, resp); return;
        }
        if (!pass.equals(confirm)) {
            fail("Passwords do not match", req, resp); return;
        }

        try {
            // Hash password (simple SHA-256). You can swap to BCrypt later.
            String passHash = sha256(username + ":" + pass);

            // Check username uniqueness (email check optional if you add DAO method)
            if (userDao.findByUsername(username) != null) {
                fail("Username already exists", req, resp); return;
            }

            userDao.create(username, email, passHash);
            resp.sendRedirect(req.getContextPath() + "/login?registered=1");
        } catch (IllegalArgumentException dup) {
            fail("Username or email already exists", req, resp);
        } catch (Exception e) {
            fail("Registration failed: " + e.getMessage(), req, resp);
        }
    }

    // --- helpers ---
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