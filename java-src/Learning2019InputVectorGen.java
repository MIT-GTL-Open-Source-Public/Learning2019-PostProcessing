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
import java.sql.CallableStatement;

public class Learning2019InputVectorGen {

    public static void run_iterations(Connection conn, int run_minutes, int wave)
    throws SQLException
    {
        final Instant start = Instant.now();
        Duration timeElapsed;
        
        System.out.println("Running Iterations...");
        int num_vectors = 0;

        CallableStatement cStmt = conn.prepareCall(
            "INSERT INTO Learning2019.ComputeVector" +
            "(input_vector, compute_status, input_wave) VALUES (?, 0, " + wave + ")");
        int batch_since_last = 0;
        do {
            Learning2019InputVector vI = Learning2019InputVector.getRandomValid();
            String vS = vI.getUniqueString();
            System.out.print("\rVector: " + vS);
            cStmt.setString(1, vS);
            cStmt.addBatch();
            num_vectors++;
            batch_since_last++;
            if(batch_since_last > 249) {
                cStmt.executeLargeBatch();
                batch_since_last = 0;
            }
            Instant end = Instant.now();
            timeElapsed = Duration.between(start, end);
            System.out.print("; "  + num_vectors + 
                " vectors generated in: " 
                + timeElapsed.toMillis() + " milliseconds" +
                " (" + timeElapsed.toMinutes() + " minutes)");
        } while (timeElapsed.toMinutes() < run_minutes);
        cStmt.executeLargeBatch();
        cStmt.close();
        System.out.println("");
    }

    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Usage is: java -cp mysql-connector-java-8.0.19.jar:. Learning2019InputVectorGen minutes wave server username password");
            System.out.println("    minutes: how many minutes to run this program");
            System.out.println("    wave: the input generation wave");
            System.out.println("    server, username, password: MySQL database parameters");
            System.exit(0);
        }
        int run_minutes = Integer.parseInt(args[0]);
        int wave = Integer.parseInt(args[1]);
        String server = args[2];
        String uname  = args[3];
        String pass   = args[4];
        System.out.println("Generating inputs for " + run_minutes + " minutes [wave " + wave + "] ...");
        System.out.println("Connecting to " + server + " ...");
        try {
            Connection conn =
                DriverManager.getConnection(
                    server + "?" 
                    + "noAccessToProcedureBodies = true" 
                    + "&rewriteBatchedStatements = true", uname, pass);
            run_iterations(conn, run_minutes, wave);
            conn.close();
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
        }
       
    }

}