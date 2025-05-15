package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class MYSQL {

    private static Connection connection;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "QWERTY4321pa!";
    private static final String DATABASE = "salespot_test";

    private static Statement createConnection() throws Exception {

        if (getConnection() == null) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + DATABASE, USERNAME, PASSWORD);

        }
        Statement statement = getConnection().createStatement();
        return statement;

    }

    public static void iud(String query) {
        try {
            createConnection().executeUpdate(query);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ResultSet search(String query) throws Exception {

        return createConnection().executeQuery(query);

    }

    public static Connection getConnection() {

        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + DATABASE, USERNAME, PASSWORD);

            } catch (Exception e) {
            }

        }
        return connection;
    }
}