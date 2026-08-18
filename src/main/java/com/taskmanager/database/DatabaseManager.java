package com.taskmanager.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * يدير الاتصال الوحيد (Singleton) بقاعدة بيانات SQLite المحلية
 * وينشئ الجداول اللازمة عند أول تشغيل.
 */
public class DatabaseManager {

    private static final String DB_FOLDER = System.getProperty("user.home") + File.separator + ".taskmanager";
    private static final String DB_FILE = DB_FOLDER + File.separator + "taskmanager.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            File folder = new File(DB_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON;");
            }
            createSchema();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("فشل الاتصال بقاعدة البيانات: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            throw new RuntimeException("تعذّر إعادة فتح الاتصال بقاعدة البيانات", e);
        }
        return connection;
    }

    private void createSchema() throws SQLException {
        String tasksTable = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                priority TEXT NOT NULL,
                category TEXT NOT NULL,
                deadline TEXT,
                scheduled_start TEXT,
                estimated_minutes INTEGER NOT NULL DEFAULT 30,
                actual_seconds INTEGER NOT NULL DEFAULT 0,
                status TEXT NOT NULL DEFAULT 'PENDING',
                postpone_count INTEGER NOT NULL DEFAULT 0,
                points INTEGER NOT NULL DEFAULT 0,
                created_at TEXT NOT NULL,
                completed_at TEXT
            );
            """;

        String timeEntriesTable = """
            CREATE TABLE IF NOT EXISTS time_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                task_id INTEGER NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT,
                is_pomodoro_break INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
            );
            """;

        String userSettingsTable = """
            CREATE TABLE IF NOT EXISTS user_settings (
                key TEXT PRIMARY KEY,
                value TEXT
            );
            """;

        String gamificationTable = """
            CREATE TABLE IF NOT EXISTS gamification (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                total_points INTEGER NOT NULL DEFAULT 0,
                level INTEGER NOT NULL DEFAULT 1,
                current_streak INTEGER NOT NULL DEFAULT 0
            );
            """;

        try (Statement st = connection.createStatement()) {
            st.execute(tasksTable);
            st.execute(timeEntriesTable);
            st.execute(userSettingsTable);
            st.execute(gamificationTable);
            st.execute("INSERT OR IGNORE INTO gamification (id, total_points, level, current_streak) VALUES (1, 0, 1, 0);");
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("خطأ أثناء إغلاق قاعدة البيانات: " + e.getMessage());
        }
    }
}
