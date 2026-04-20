package com.exo.util;

import com.exo.config.Db;   // same Db helper you use in other servlets

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

public class AuditLogUtil {

    public static void log(Integer userId,
                           String action,
                           String entity,
                           String entityId,
                           String notes) {

        String sql = "INSERT INTO auditlog " +
                     "(user_id, action, entity, entity_id, notes) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            if (userId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }
            ps.setString(2, action);
            ps.setString(3, entity);
            ps.setString(4, entityId);
            ps.setString(5, notes);

            ps.executeUpdate();
        } catch (Exception e) {
            // Don't break login if logging fails — just print it
            e.printStackTrace();
        }
    }
}