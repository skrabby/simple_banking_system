package SQLManager;

import org.w3c.dom.xpath.XPathResult;

import java.sql.*;


public class QueryManager {
    private String url;
    private String log;
    private String pass;

    public QueryManager(String url, String log, String pass) {
        this.url = url;
        this.log = log;
        this.pass = pass;
    }

    private Connection connect(String url, String log, String pass) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, log, pass);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void executeWriteQuery(String query) {
        try (Connection connection = this.connect(url, log, pass)) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }

    }

    public ResultSet executeReadQuery(String query) {
        try (Connection connection = this.connect(url, log, pass)) {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
        return null;
    }
}
