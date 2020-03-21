import java.text.DecimalFormat; 
import java.util.Random;

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
    public static void main(String[] args) {
        Learning2019InputVector vI = Learning2019InputVector.getRandomValid();
        // Prints "Hello, World" to the terminal window.
        System.out.println("Vector: " + vI.getUniqueString() + " " + vI.isValid());
    }

}