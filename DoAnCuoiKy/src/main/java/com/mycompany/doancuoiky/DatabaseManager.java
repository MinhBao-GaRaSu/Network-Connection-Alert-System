/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.doancuoiky;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager — Singleton quản lý kết nối SQLite.
 * Tác giả: Trần Bảo Minh (MSSV: 2280601960)
 */
public class DatabaseManager {
    private static DatabaseManager instance;

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    private static final String DB_FILE = "network_monitoring.db";
    private static final String DB_URL  = "jdbc:sqlite:" + DB_FILE;
    private Connection connection;

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL;");
            }
            initDatabase();
            System.out.println("[DB] Ket noi SQLite thanh cong.");
        } catch (Exception e) {
            System.err.println("[DB] Loi ket noi: " + e.getMessage());
        }
    }

    private void initDatabase() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS servers (\n" +
                       "    id   INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                       "    name TEXT NOT NULL,\n" +
                       "    host TEXT NOT NULL,\n" +
                       "    port INTEGER NOT NULL DEFAULT 0,\n" +
                       "    type TEXT NOT NULL DEFAULT 'PING'\n" +
                       ")");
            st.execute("CREATE TABLE IF NOT EXISTS status_history (\n" +
                       "    id            INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                       "    server_name   TEXT NOT NULL,\n" +
                       "    host          TEXT NOT NULL,\n" +
                       "    status        TEXT NOT NULL,\n" +
                       "    response_time INTEGER NOT NULL DEFAULT 0,\n" +
                       "    checked_at    TEXT NOT NULL\n" +
                       ")");
            st.execute("CREATE TABLE IF NOT EXISTS app_config (\n" +
                       "    key TEXT PRIMARY KEY, value TEXT\n" +
                       ")");
        }
    }

    public List<ServerInfo> loadServers() {
        List<ServerInfo> list = new ArrayList<>();
        String sql = "SELECT id, name, host, port, type FROM servers ORDER BY id ASC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ServerInfo(
                    rs.getInt("id"), rs.getString("name"),
                    rs.getString("host"), rs.getInt("port"), rs.getString("type")
));
            }
        } catch (SQLException e) {
            System.err.println("[DB] loadServers error: " + e.getMessage());
        }
        return list;
    }

    public int saveServer(ServerInfo server) {
        String sql = "INSERT INTO servers (name, host, port, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, server.getName());
            ps.setString(2, server.getHost());
            ps.setInt(3, server.getPort());
            ps.setString(4, server.getType());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DB] saveServer error: " + e.getMessage());
        }
        return -1;
    }

    public void deleteServer(int id) {
        String sql = "DELETE FROM servers WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] deleteServer error: " + e.getMessage());
        }
    }

    public void insertStatusHistory(String serverName, String host, String status, long latency) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "INSERT INTO status_history (server_name, host, status, response_time, checked_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, serverName);
            ps.setString(2, host);
            ps.setString(3, status);
            ps.setLong(4, latency);
            ps.setString(5, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] insertStatusHistory error: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {}
    }

    public String getConfig(String key, String defaultValue) {
        String sql = "SELECT value FROM app_config WHERE key = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("value");
            }
        } catch (SQLException e) {
            System.err.println("[DB] getConfig error: " + e.getMessage());
        }
        return defaultValue;
    }

    public void setConfig(String key, String value) {
        String sql = "INSERT OR REPLACE INTO app_config (key, value) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] setConfig error: " + e.getMessage());
        }
    }

    public static class StatusHistoryRecord {
        private final int id;
        private final String serverName;
        private final String host;
        private final String status;
        private final long responseTime;
        private final String checkedAt;

        public StatusHistoryRecord(int id, String serverName, String host, String status, long responseTime, String checkedAt) {
            this.id = id;
            this.serverName = serverName;
            this.host = host;
            this.status = status;
            this.responseTime = responseTime;
            this.checkedAt = checkedAt;
        }

        public int getId() { return id; }
        public String getServerName() { return serverName; }
        public String getHost() { return host; }
        public String getStatus() { return status; }
        public long getResponseTime() { return responseTime; }
        public String getCheckedAt() { return checkedAt; }
    }

    public List<StatusHistoryRecord> loadRecentHistory(int limit) {
        List<StatusHistoryRecord> list = new ArrayList<>();
        String sql = "SELECT id, server_name, host, status, response_time, checked_at FROM status_history ORDER BY id DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StatusHistoryRecord(
                        rs.getInt("id"),
                        rs.getString("server_name"),
                        rs.getString("host"),
                        rs.getString("status"),
                        rs.getLong("response_time"),
                        rs.getString("checked_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] loadRecentHistory error: " + e.getMessage());
        }
        return list;
    }

    public void clearHistory() {
        String sql = "DELETE FROM status_history";
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("[DB] clearHistory error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        DatabaseManager db = DatabaseManager.getInstance();
        db.saveServer(new ServerInfo("Google DNS", "8.8.8.8", 0, "PING"));
        List<ServerInfo> list = db.loadServers();
        System.out.println("Test SQLite thanh cong! Tong so server: " + list.size());
    }
}
