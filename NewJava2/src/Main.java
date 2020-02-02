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

        Network net = new Network(2,10,1,10,1);
        net.PARAM_Gradient = (float) 0.01;
        net.PARAM_LearnRate = (float) 0.01;
        float[] input = new float[2];
        float[] output = new float[1];
        float[] predicted;
        input[0] = -3;
        input[0] = 3;
        output[0] = (float) 6;

        float error = 0;
        for (int i = 0; i < 1000000; i++) {
            predicted = net.calculate(input);
            error = error + ((output[0] - predicted[0]) * (output[0] - predicted[0]));
            net.train(input,output);
            if ((i % 100) == 0){
                System.out.println("Average squared error over last 100 : " + error/100);

                error = 0;
            }
        }


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
