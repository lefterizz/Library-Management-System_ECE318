package com.example.ece318_librarymanagementsys_uc1069790.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();

        // connection information
        config.setJdbcUrl("jdbc:mysql://localhost:3306/librarydb");
        config.setUsername("libraryuser");
        config.setPassword("password123");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // pool optimization
        config.setMaximumPoolSize(10);        // Perfect for desktop apps
        config.setMinimumIdle(2);
        config.setIdleTimeout(60000);         // 1 minute
        config.setConnectionTimeout(30000);   // 30 seconds
        config.setMaxLifetime(600000);        // 10 minutes (avoid MySQL server timeout)
        config.setLeakDetectionThreshold(2000); // Detect hanging DatabaseConnection calls

        // performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // Allow ID = 0 inserts
        config.setConnectionInitSql(
                "SET SESSION sql_mode = " +
                        "CONCAT(IF(@@sql_mode = '', 'NO_AUTO_VALUE_ON_ZERO', " +
                        "CONCAT(@@sql_mode, ',NO_AUTO_VALUE_ON_ZERO')));"
        );

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
