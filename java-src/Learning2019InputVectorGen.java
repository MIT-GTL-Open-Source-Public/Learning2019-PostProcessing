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

import java.text.DecimalFormat; 
import java.util.Random;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.sql.CallableStatement;

class Learning2019InputVector {
    static final char MAX_BUILDINGS = 6;
    static final char NUM_TEAMS  = 3;
    static final char MAX_FLOORS = 20;
    static final char MAX_RATIOS = 9;

    int floors[] = new int[MAX_BUILDINGS];
    int teamRatios[][] = new int[NUM_TEAMS][MAX_BUILDINGS];

    private static int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

    public static Learning2019InputVector getRandom() {
        Learning2019InputVector vI = new Learning2019InputVector();
        for (int i = 0; i < MAX_BUILDINGS; i++) {
            vI.floors[i] = getRandomNumberInRange(0, MAX_FLOORS); 
        }
        for (int j = 0; j < NUM_TEAMS; j++) {
            for (int i = 0; i < MAX_BUILDINGS; i++) {
                if(vI.floors[i] != 0) {
                    vI.teamRatios[j][i] = getRandomNumberInRange(1, MAX_RATIOS);
                }
            }
        }
        return vI;
    }

    public static Learning2019InputVector getRandomValid() {
        Learning2019InputVector vI = Learning2019InputVector.getRandom();
        while ( ! vI.isValid() ) {
            vI = Learning2019InputVector.getRandom();
        }
        return vI;
    }

    /**
     * Checks that:
     *  1. At least one of the buildings are enabled 
           and have floors less than equal to MAX_FLOORS. 
     *  2. Teams are assigned only to buildings that are enabled
     *  3. A team is assigned to at least one building
     */
    boolean isValid() {
        char numBuildings = 0;
        for (int i = 0; i < MAX_BUILDINGS; i++) {
            if (floors[i] > MAX_FLOORS) break;
            if (floors[i] != 0) {
                numBuildings++;
                break;
            } 
        }
        boolean validFloors =  (numBuildings > 0) && (numBuildings <= MAX_BUILDINGS);
        char numValidTeams = 0;
        if (validFloors) {
            for (int j = 0; j < NUM_TEAMS; j++) { 
                boolean validTeam = true;
                numBuildings = 0;
                for (int i = 0; i < MAX_BUILDINGS; i++) {
                    if ((floors[i] == 0) && (teamRatios[j][i] > 0)) {
                        validTeam = false;
                        break;
                    }
                    if ((floors[i] > 0) && (teamRatios[j][i]) > 0) {
                        numBuildings++;
                    }
                }
                if ((!validTeam) || (numBuildings == 0)) break;
                else numValidTeams++;            }
        }
        return validFloors && (numValidTeams == NUM_TEAMS);
    }

    String getUniqueString() {
        String out = "";
        DecimalFormat df = new DecimalFormat("00"); 
        for (int i = 0; i < MAX_BUILDINGS; i++) {
            out += df.format(floors[i]);
        }
        df = new DecimalFormat("0");    
        for (int j = 0; j < NUM_TEAMS; j++) {
            out += "-";
            for (int i = 0; i < MAX_BUILDINGS; i++) {
                out += df.format(teamRatios[j][i]);
            }
        }
        return out;
    }
}

public class Learning2019InputVectorGen {

    public static void run_iterations(Connection conn, int run_minutes, int wave)
    throws SQLException
    {
        final Instant start = Instant.now();
        Duration timeElapsed;
        
        System.out.println("");
        int num_vectors = 0;
        do {
            Learning2019InputVector vI = Learning2019InputVector.getRandomValid();
            String vS = vI.getUniqueString();
            System.out.print("\rVector: " + vS);
            CallableStatement cStmt = conn.prepareCall(
            "{call Learning2019.AddComputeInputVector(?, ?, ?)}");
            cStmt.setString(1, vS);
            cStmt.setInt(2, wave);
            cStmt.registerOutParameter(3, Types.BOOLEAN);
            cStmt.execute();
            boolean exists = cStmt.getBoolean(3);
            if (exists) num_vectors++;
            Instant end = Instant.now();
            timeElapsed = Duration.between(start, end);
            System.out.print("; "  + num_vectors + 
                " vectors generated in: " 
                + timeElapsed.toMillis() + " milliseconds" +
                " (" + timeElapsed.toMinutes() + " minutes)");
        } while (timeElapsed.toMinutes() < run_minutes);
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
                    server + "?" + "noAccessToProcedureBodies = true", uname, pass);
            run_iterations(conn, run_minutes, wave);
        } catch (Exception ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
        }
       
    }

}