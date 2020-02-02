import java.sql.*;
import java.sql.DriverManager;
public class sqlconnection {

        // JDBC driver name and database URL
        static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        static final String DB_URL = "jdbc:mysql://ls-7747fafb702e9b0e95827d986e35040c609dd263.cztwonmsggwh.eu-west-2.rds.amazonaws.com:3306/bardata";
        // Database credentials
        static final String USER = "mehowmeta2";
        static final String PASS = "Once there was a spliff";
        public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try{
//STEP 2: Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
//STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,"mehowmeta2","Once there was a spliff");
//STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM bardata.bars";
           // sql = "CREATE TABLE IF NOT EXISTS bardata2 (\n" +
           //         "barid INT AUTO_INCREMENT PRIMARY KEY,\n" +
           //         "date VARCHAR(50),\n" +
           //         "closePrice VARCHAR(50), rsi VARCHAR(50), ma VARCHAR(50), ac VARCHAR(50), sar VARCHAR(50),\n" +
           //         " momentum VARCHAR(50), vol VARCHAR(50), force VARCHAR(50),\n" +
           //         "ad VARCHAR(50), ama VARCHAR(50), atr VARCHAR(50), ao VARCHAR(50), bears VARCHAR(50), bulls VARCHAR(50), cci VARCHAR(50),\n" +
           //         "chaikin VARCHAR(50), dema VARCHAR(50), demarker VARCHAR(50), frama VARCHAR(50), bwmfi VARCHAR(50), osma VARCHAR(50), obv VARCHAR(50),\n" +
           //         "tema VARCHAR(50), trix VARCHAR(50),wpr VARCHAR(50), vidya VARCHAR(50)\n" +
           //         ");";
            ResultSet rs = stmt.executeQuery(sql);
//STEP 5: Extract data from result set
            while(rs.next()){
//Retrieve by column name
                String time = rs.getString("timeofprice");
                String price = rs.getString("closeprice");
//Display values
                System.out.print(time + ": " + price + "\n");
            }
//STEP 6: Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
//Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
//Handle errors for Class.forName
            e.printStackTrace();
        }finally{
//finally block used to close resources
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){
            }// nothing can be done
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");
    }//end main
} // end Example

