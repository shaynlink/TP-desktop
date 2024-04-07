package com.example.financemanager.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.example.financemanager.FinanceTrackerApplication.findAndCreateOSFolder;

public class Database {
    private static final Logger log = LoggerFactory.getLogger(Database.class);

    /**
     * Location of database
     */
    private static final String location = "database.db";

    public static boolean isOK() {
        if (!checkDrivers()) return false; //driver errors

        if (!checkConnection()) return false; //can't connect to db

        return createTableIfNotExists(); //tables didn't exist
    }

    private static boolean checkDrivers() {
        try {
            Class.forName("org.sqlite.JDBC");
            DriverManager.registerDriver(new JDBC());
            return true;
        } catch (ClassNotFoundException | SQLException classNotFoundException) {
            log.error("Could not start SQLite Drivers", classNotFoundException);
            return false;
        }
    }

    private static boolean checkConnection() {
        try (Connection connection = connect()) {
            return connection != null;
        } catch (SQLException e) {
            log.error("Could not connect to SQLite DB");
            return false;
        }
    }

    private static boolean createTableIfNotExists() {
        String createExpenseTables =
                """
                        CREATE TABLE IF NOT EXISTS expense(
                             date TEXT NOT NULL,
                             housing REAL NOT NULL,
                             food REAL NOT NULL,
                             goingOut REAL NOT NULL,
                             transportation REAL NOT NULL,
                             travel REAL NOT NULL,
                             tax REAL NOT NULL,
                             other REAL NOT NULL
                     );
                   """;
        String createIncomeTables =
                """
                        CREATE TABLE IF NOT EXISTS income(
                             date TEXT NOT NULL,
                             salary REAL NOT NULL,
                             help REAL NOT NULL,
                             autoBusiness REAL NOT NULL,
                             passiveIncome REAL NOT NULL,
                             other REAL NOT NULL
                     );
                   """;

        try (Connection connection = Database.connect()) {
            PreparedStatement expenseStatement = connection.prepareStatement(createExpenseTables);
            expenseStatement.executeUpdate();

            PreparedStatement incomeStatement = connection.prepareStatement(createIncomeTables);
            incomeStatement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            log.error("Could not create tables in database", exception);
            return false;
        }
    }

    protected static Connection connect() {
        String osFolder = findAndCreateOSFolder();

        String dbPrefix = "jdbc:sqlite:";
        Connection connection;
        try {
            connection = DriverManager.getConnection(dbPrefix + osFolder + "/" + location);
        } catch (SQLException exception) {
            log.error("Could not connect to SQLite DB at " + osFolder + "/" + location, exception);

            return null;
        }
        return connection;
    }

}
