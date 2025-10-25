package com.remp.jdbc;

import oracle.jdbc.OracleConnection;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class PlatformThread {
    private final static String DB_URL = DBConfig.getDbUrl();
    private final static String DB_USER = DBConfig.getDbUser();
    private final static String DB_PASSWORD = DBConfig.getDbPassword();
    private final static String queryStatement = "SELECT * FROM SH.CUSTOMERS WHERE CUST_ID = 49671";
    private final static Logger LOGGER = Logger.getLogger("com.remp.jdbc.PlatformThread");

    public static void main(String[] args) throws SQLException {

        OracleConnection connection = getOracleConnection();
        DatabaseMetaData dbmd = connection.getMetaData();
        System.out.println(STR."Driver Name: \{dbmd.getDriverName()}");
        System.out.println(STR."Driver Version: \{dbmd.getDriverVersion()}");
        System.out.println();

        Instant start = Instant.now();

        // platform threads
        var threads = IntStream.range(0, 1_500).mapToObj(i -> new Thread(() -> {
            try {
                performWork(connection, queryStatement);
                System.out.println("Query #: " + (i));
            } catch (SQLException ex) {
                LOGGER.severe(ex.getMessage());
            }
        })).toList();

        threads.forEach(Thread::start);

        for (var thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                LOGGER.severe(String.format("InterruptedException has been occurred: %s with cause: %s", e.getMessage(), e.getCause()));
            }
        }

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).getSeconds();
        System.out.println(STR."Elapsed: \{timeElapsed}");

    }

    private static OracleConnection getOracleConnection() throws SQLException {
        Properties props = new Properties();
        props.put(OracleConnection.CONNECTION_PROPERTY_FAN_ENABLED, "false");

        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(DB_URL);
        pds.setUser(DB_USER);
        pds.setPassword(DB_PASSWORD);
        pds.setConnectionPoolName("JDBC_UCP_POOL");
        pds.setConnectionProperties(props);
        return (OracleConnection) pds.getConnection();
    }

    private static void performWork(Connection conn, String queryStatement) throws SQLException {
        conn.setAutoCommit(false);
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(queryStatement)) {
            while (resultSet.next()) {
                System.out.println(new StringBuilder(resultSet.getString(1)).append(" ").append(resultSet.getString(2))
                        .append(" ").append(resultSet.getString(3)).append(" ").append(resultSet.getString(4))
                        .append(" ").append(resultSet.getInt(5)).toString());
            }
        }
    }
}
