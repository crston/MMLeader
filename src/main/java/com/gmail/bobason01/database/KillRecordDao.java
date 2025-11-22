package com.gmail.bobason01.database;

import com.gmail.bobason01.kill.KillRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public final class KillRecordDao {

    private final HikariConnectionPool pool;

    public KillRecordDao(HikariConnectionPool pool) {
        this.pool = pool;
    }

    public void createTable() throws Exception {
        try (Connection con = pool.getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS mmleader (" +
                    "mobId VARCHAR(64)," +
                    "orderIndex INT," +
                    "player VARCHAR(64)," +
                    "leader VARCHAR(64)" +
                    ");";
            con.prepareStatement(sql).executeUpdate();
        }
    }

    public void insertRecords(String mobId, List<KillRecord> records) throws Exception {
        if (records == null || records.isEmpty()) {
            return;
        }
        try (Connection con = pool.getConnection()) {
            String sql = "INSERT INTO mmleader (mobId, orderIndex, player, leader) VALUES (?,?,?,?);";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (KillRecord r : records) {
                    ps.setString(1, mobId);
                    ps.setInt(2, r.getOrder());
                    ps.setString(3, r.getPlayer());
                    ps.setString(4, r.getLeader());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    public List<KillRecord> loadRecords(String mobId) throws Exception {
        List<KillRecord> list = new ArrayList<>();
        try (Connection con = pool.getConnection()) {
            String sql = "SELECT orderIndex, player, leader FROM mmleader WHERE mobId=? ORDER BY orderIndex ASC;";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, mobId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int order = rs.getInt("orderIndex");
                        String player = rs.getString("player");
                        String leader = rs.getString("leader");
                        list.add(new KillRecord(order, player, leader));
                    }
                }
            }
        }
        return list;
    }
}
