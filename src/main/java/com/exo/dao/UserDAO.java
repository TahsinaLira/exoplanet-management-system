package com.exo.dao;

import com.exo.model.User;

public interface UserDAO {

    /**
     * Insert a new user. Returns generated user_id.
     * Expect passHash (already hashed) from the caller.
     */
    int create(String username, String email, String passHash) throws Exception;

    /**
     * Find a user by username. Returns null if not found.
     * Used for login (to get passHash and role) and for uniqueness checks.
     */
    User findByUsername(String username) throws Exception;

    /**
     * Optional convenience: find by id after login.
     */
    User findById(int userId) throws Exception;

    /**
     * Optional (for promoting a user to admin).
     */
    boolean updateRole(int userId, String role) throws Exception;
}