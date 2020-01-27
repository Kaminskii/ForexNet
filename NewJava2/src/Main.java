import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {


    public static void main(String[] args) {

        // String url = "ls-7747fafb702e9b0e95827d986e35040c609dd263.cztwonmsggwh.eu-west-2.rds.amazonaws.com";
        //String url = "dbc:sqlserver://ls-7747fafb702e9b0e95827d986e35040c609dd263.cztwonmsggwh.eu-west-2.rds.amazonaws.com:3306;DatabaseName=bardata";
        String user = "mehowmeta2";
        String password = "Once there was a spliff";
        connectJDBCToAWSEC2();

        Network net = new Network(1,10,10,1);
        float[] input = new float[1];
        float[] output = new float[1];

        input[0] = 1;
        output[0] = -1;


    }

    public static void connectJDBCToAWSEC2() {

        System.out.println("----MySQL JDBC Connection Testing -------");

        try {
            Class cls = Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
            return;
        }

        System.out.println("MySQL JDBC Driver Registered!");
        Connection conn = null;

        try {
            String url = "jdbc:mysql://ls-\\7747fafb702e9b0e95827d986e35040c609dd263.cztwonmsggwh.eu-west-2.rds.amazonaws.com:3306/bardata";
            conn = DriverManager.getConnection (url,"mehowmeta2","Once there was a spliff");
            //conn = DriverManager.getConnection("jdbc:mysql://ls-\\7747fafb702e9b0e95827d986e35040c609dd263.cztwonmsggwh.eu-west-2.rds.amazonaws.com" + "port=3306&user=mehowmeta2&password=Once there was a spliff");
            //connection = DriverManager.
              //      getConnection("jdbc:mysql:ls-\\7747fafb702e9b0e95827d986e35040c609dd263.cztwonmsggwh.eu-west-2.rds.amazonaws.com:3306/bardata", "mehowmeta2", "Once there was a spliff");
        } catch (SQLException e) {
            System.out.println("Connection Failed!:\n" + e.getMessage() + "\n" + e.getErrorCode());
        }

        if (conn != null) {
            System.out.println("SUCCESS!!!! You made it, take control     your database now!");
        } else {
            System.out.println("FAILURE! Failed to make connection!");
        }

    }
}
