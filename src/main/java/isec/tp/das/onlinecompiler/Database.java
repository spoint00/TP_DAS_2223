package isec.tp.das.onlinecompiler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    static String url = "jdbc:sqlite:./bd/DAS.db";
    static String username = "";
    static String password = "";
    static Connection connection = null;

    public Database() {}

    public static Connection initiliazeDatabase () {
        try {
            connection = DriverManager.getConnection(url, username, password);
            createTable(connection);

            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void closeConnection(){
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createTable(Connection connection) throws SQLException {
        // SQL statement to create a table named "users"
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL,"
                + "email TEXT NOT NULL"
                + ");";

        // Execute the SQL statement
        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        }

        System.out.println("Table created successfully.");
    }
}
