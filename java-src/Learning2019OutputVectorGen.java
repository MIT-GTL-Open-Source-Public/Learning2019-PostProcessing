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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

import edu.mit.gtl.masie.analysis.CalcMasie;
import edu.mit.gtl.masie.analysis.MorphologicalMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

public class Learning2019OutputVectorGen {

    static final int BATCH_SIZE = 10; // number of inputs to work on at a time
    Connection conn;
    int wave, compute_id;
    
    public Learning2019OutputVectorGen(Connection conn, int wave, int compute_id) {
        this.conn = conn;
        this.wave = wave;
        this.compute_id = compute_id;
    }

    private MorphologicalMatrix get_morph_matrix(String vec)
    {
        Learning2019InputVector iVec = new Learning2019InputVector(vec);
        assert iVec.isValid(): "Invalid Input Vector";
        List<Boolean> activeBuildings = 
            new ArrayList<Boolean>(
                Collections.nCopies(Learning2019InputVector.NUM_BUILDINGS, false)); 
        List<Integer> numberOfFloors = 
            new ArrayList<Integer>(
                Collections.nCopies(Learning2019InputVector.NUM_BUILDINGS, 0));
        
        List<Integer> redTeamAllocation = 
            new ArrayList<Integer>(
                Collections.nCopies(Learning2019InputVector.NUM_BUILDINGS, 0));
        List<Integer> greenTeamAllocation = 
            new ArrayList<Integer>(
                Collections.nCopies(Learning2019InputVector.NUM_BUILDINGS, 0));
        List<Integer> blueTeamAllocation = 
            new ArrayList<Integer>(
                Collections.nCopies(Learning2019InputVector.NUM_BUILDINGS, 0));

        for (int i = 0; i < Learning2019InputVector.NUM_BUILDINGS; i++) {
            activeBuildings.set(i, iVec.floors[i] > 0);
            numberOfFloors.set(i, iVec.floors[i]);
            redTeamAllocation.set(i, iVec.teamAllocations[0][i]);
            greenTeamAllocation.set(i, iVec.teamAllocations[1][i]);
            blueTeamAllocation.set(i, iVec.teamAllocations[2][i]);
        }    
		
		return new MorphologicalMatrix(activeBuildings, numberOfFloors,
	            redTeamAllocation, greenTeamAllocation, blueTeamAllocation);
    }

    private void udpate_vector(String vec, ResultSet rs)
    throws SQLException
    {
        System.out.print("\rUpdating: " + vec); 
        rs.updateInt("compute_id", compute_id);
        MorphologicalMatrix matrix = get_morph_matrix(vec);
        CalcMasie calc = new CalcMasie(matrix);
        calc.run();
        HashMap<String, Float> results = calc.getResults();
        rs.updateInt("compute_status", 2);
        rs.updateFloat("cost", results.get("Total Cost"));
        rs.updateFloat("team_mix", results.get("Team Mix Index"));
        rs.updateFloat("interaction_score", results.get("Interaction Score"));
        rs.updateFloat("walking_time", results.get("Walking Time"));
        rs.updateRow();
    }

    private void run_batch()
    throws SQLException
    {
        try {
            String sql = 
                "SELECT input_vector, compute_status, compute_id, " +
                "cost, team_mix, interaction_score, walking_time " +
                "FROM Learning2019.ComputeVector " +
                "WHERE input_wave = " + wave + " AND COMPUTE_STATUS = 0 " +
                "LIMIT " + BATCH_SIZE + " FOR UPDATE";
            PreparedStatement ps = conn.prepareStatement(sql,
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String vec = rs.getString("input_vector");
                udpate_vector(vec, rs);
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            // in case of exception, rollback the transaction
            System.out.println("\n\nSQLException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run_iterations(int run_minutes)
    throws SQLException
    {
        final Instant start = Instant.now();
        Duration timeElapsed;
        long num_vectors = 0;
        System.out.println("Running Iterations...");
        do {
            run_batch();
            num_vectors += BATCH_SIZE;
            Instant end = Instant.now();
            timeElapsed = Duration.between(start, end);
            System.out.print("; "  + num_vectors + 
                " vectors processed in: " 
                + timeElapsed.toMillis() + " milliseconds" +
                " (" + timeElapsed.toMinutes() + " minutes)");
        } while (timeElapsed.toMinutes() < run_minutes);
        System.out.println("");
    }

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Usage is: java -cp mysql-connector-java-8.0.19.jar:. Learning2019OutputVectorGen server username password minutes wave compute_id");
            System.out.println("    server, username, password: MySQL database parameters");
            System.out.println("    minutes: how many minutes to run this program");
            System.out.println("    wave: the input wave to use");
            System.out.println("    compute_id: the compute_id to use");
            System.exit(0);
        }
        String server = args[0];
        String uname  = args[1];
        String pass   = args[2];
        int run_minutes = Integer.parseInt(args[3]);
        int wave = Integer.parseInt(args[4]);
        int compute_id = Integer.parseInt(args[5]);
        System.out.println("Generating outputs for " + 
            run_minutes + " minutes [wave " + 
            wave + ", compute_id " + compute_id + "] ...");
        System.out.println("Connecting to " + server + " ...");
        try {
            Connection conn =
                DriverManager.getConnection(
                    server + "?" 
                    + "noAccessToProcedureBodies = true" 
                    + "&rewriteBatchedStatements = true", uname, pass);
            Learning2019OutputVectorGen vGen =
                 new Learning2019OutputVectorGen(conn, wave, compute_id);
            vGen.run_iterations(run_minutes);
            conn.close();
        } catch (Exception ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
        }
       
    }

}