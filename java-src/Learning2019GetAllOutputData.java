/*
Copyright (c) 2020 Prakash Manandhar

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;

public class Learning2019GetAllOutputData {

    static final int BATCH_SIZE = 10; // number of inputs to work on at a time
    Connection conn;
    String outfile;
    
    public Learning2019GetAllOutputData(Connection conn, String outfile) {
        this.conn = conn;
        this.outfile = outfile;
    }

    private void save_all()
    throws SQLException, IOException
    {
        final Instant start = Instant.now();
        Duration timeElapsed;
        long num_vectors = 0;
        System.out.println("Fetching data...");
        FileWriter fw = new FileWriter(outfile);
        final String [] columns = new String[] {
            "input_vector", "compute_status", "input_wave", "compute_id", 
            "cost", "team_mix", "interaction_score", "walking_time"};
        final int num_cols = columns.length;
        for (int i = 0; i < num_cols; i++)
            fw.write("" + columns[i] + ",");
        fw.write("\n");
        try {
            conn.setAutoCommit(false);
            String sql = 
                "SELECT * " +
                "FROM Learning2019.ComputeVector " +
                "WHERE COMPUTE_STATUS = 2 ";
            Statement stmt = conn.createStatement();
            stmt.setFetchSize(200);
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                for (int i = 0; i < num_cols; i++)
                    fw.write(rs.getObject(columns[i]).toString() + ",");
                fw.write("\n");
                num_vectors++;
                if ( (num_vectors % 100) == 0) {
                    Instant end = Instant.now();
                    timeElapsed = Duration.between(start, end);        
                    System.out.println(" " + num_vectors + 
                        " vectors processed in: " 
                        + timeElapsed.toMillis() + " milliseconds" +
                        " (" + timeElapsed.toMinutes() + " minutes)");
                }
            }
            rs.close();
            stmt.close();
        } catch(SQLException e) {
            // in case of exception, rollback the transaction
            System.out.println("\n\nSQLException: " + e.getMessage());
            e.printStackTrace();
        }
        fw.flush();
        fw.close();
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage is: java -cp mysql-connector-java-8.0.19.jar:. Learning2019GetAllOutputData server username password outfile");
            System.out.println("   server, username, password: MySQL database parameters");
            System.out.println("   out_file: output csv file");
            System.exit(0);
        }
        String server = args[0];
        String uname  = args[1];
        String pass   = args[2];
        String outfile = args[3];
        System.out.println("Connecting to " + server + " ...");
        try {
            Connection conn =
                DriverManager.getConnection(
                    server + "?" 
                    + "noAccessToProcedureBodies = true" 
                    + "&rewriteBatchedStatements = true", uname, pass);
            Learning2019GetAllOutputData vOut =
                 new Learning2019GetAllOutputData(conn, outfile);
            vOut.save_all();
            conn.close();
        } catch (Exception ex) {
            // handle any errors
            System.out.println("Exception: " + ex.getMessage());
        }
       
    }

}