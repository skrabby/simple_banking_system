package SQLManager;

import java.sql.*;


public class QueryManager {
    public String url;
    public String log;
    public String pass;

    public QueryManager(String url, String log, String pass) {
        this.url = url;
        this.log = log;
        this.pass = pass;
    }

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, log, pass);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void createNewDatabase() {
        try (Connection conn = this.connect()) {
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createNewTable(String tableName, String var) {
        // SQLite connection string

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (\n"
                + var
                + ");";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void executeWriteQuery(String query) {
        try (Connection connection = this.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }

    }

    // Works fine with Postgres, SQLite need another approach since we can't simply return ResultSet, the connection would be lost
    public ResultSet executeReadQuery(String query) {
        try (Connection connection = this.connect()) {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
        return null;
    }

    // SQLite overload approach
    public String executeReadQuery(String query, String flag) {
        try (Connection connection = this.connect()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            // Different flags handler
            String result = "";
            switch (flag.toUpperCase()) {
                case ("EXISTS"):
                    resultSet.next();
                    return resultSet.getString("exists");
                case ("SELECT"):
                    while (resultSet.next()) {
                        result += String.format("%-5.5s  %-30.30s  %-30.30s  %-30.30s%n", resultSet.getString("id"), resultSet.getString("number"),
                                resultSet.getString("pin"), resultSet.getString("balance"));
                    }
                    return result;
                case ("USER"):
                    resultSet.next();
                    result += resultSet.getString("id") + " " + resultSet.getString("number") + " " +
                            resultSet.getString("pin") + " " + resultSet.getString("balance");
                    return result;
                case ("COUNT(*)"):
                    resultSet.next();
                    return resultSet.getString("COUNT(*)");
                case ("COUNT"):
                    resultSet.next();
                    return resultSet.getString("count");
            }
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
        return "";
    }
}
