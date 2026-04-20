package com.exo.dao.impl;

import com.exo.config.Db;
import com.exo.dao.UserDAO;
import com.exo.model.User;

import java.sql.*;

public class UserDAOImpl implements UserDAO {

    // --- SQL statements (keep them in one place) ---
    private static final String SQL_INSERT =
        "INSERT INTO AppUser(username, email, pass_hash) VALUES (?,?,?)";

    private static final String SQL_FIND_BY_USERNAME =
        "SELECT user_id, username, email, pass_hash, role " +
        "FROM AppUser WHERE username = ?";

    private static final String SQL_FIND_BY_ID =
        "SELECT user_id, username, email, pass_hash, role " +
        "FROM AppUser WHERE user_id = ?";

    private static final String SQL_UPDATE_ROLE =
        "UPDATE AppUser SET role = ? WHERE user_id = ?";

    // --- create ---
    @Override
    public int create(String username, String email, String passHash) throws Exception {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username.trim());
            ps.setString(2, email.trim());
            ps.setString(3, passHash);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Insert failed, no rows affected.");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("Insert failed, no ID obtained.");
            }

        } catch (SQLIntegrityConstraintViolationException dup) {
            // thrown on duplicate username/email (UNIQUE constraints)
            throw new IllegalArgumentException("Username or email already exists");
        }
    }

    // --- findByUsername ---
    @Override
    public User findByUsername(String username) throws Exception {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_FIND_BY_USERNAME)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        }
    }

    // --- findById (optional convenience) ---
    @Override
    public User findById(int userId) throws Exception {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        }
    }

    // --- updateRole (optional) ---
    @Override
    public boolean updateRole(int userId, String role) throws Exception {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_UPDATE_ROLE)) {

            ps.setString(1, role);   // "user" or "admin"
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    // --- helper ---
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassHash(rs.getString("pass_hash"));
        u.setRole(rs.getString("role"));
        return u;
    }
}