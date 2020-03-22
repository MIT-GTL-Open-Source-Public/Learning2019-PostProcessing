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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

class Learning2019InputVector {
    static final char NUM_BUILDINGS = 6;
    static final char NUM_TEAMS  = 3;
    static final char MAX_FLOORS = 20;
    static final char MAX_RATIOS = 9;
    static final List<Integer> TEAM_POPULATION = List.of(1500, 3500, 5000);
    static final float MIN_SPACE = 100.0f;

    int floors[] = new int[NUM_BUILDINGS];
    int teamRatios[][]      = new int[NUM_TEAMS][NUM_BUILDINGS];
    int teamAllocations[][] = new int[NUM_TEAMS][NUM_BUILDINGS];

    // empty constructor needed for random generator
    public Learning2019InputVector() { 
        for (int i = 0; i < NUM_BUILDINGS; i++) {
            floors[i] = getRandomNumberInRange(0, MAX_FLOORS); 
        }
        for (int j = 0; j < NUM_TEAMS; j++) {
            for (int i = 0; i < NUM_BUILDINGS; i++) {
                if( floors[i] != 0 ) {
                    teamRatios[j][i] = getRandomNumberInRange(0, MAX_RATIOS);
                }
            }
        }
    }

    // creates object from string vector representation
    // reverse of getUniqueString
    public Learning2019InputVector(String vector) {
        int index = 0;
        for(int i = 0; i < NUM_BUILDINGS; i++) {
            floors[i] = Integer.parseInt(vector.substring(index, index+2));
            index += 2;
        }
        for (int j = 0; j < NUM_TEAMS; j++) {
            index++; // skip -
            for(int i = 0; i < NUM_BUILDINGS; i++) {
                teamRatios[j][i] = Integer.parseInt(vector.substring(index, index+1));
                index++;
            }
        }
        calculateAllocations();
    }

    private void calculateAllocations() {
        for(int j = 0; j < NUM_TEAMS; j++) {
            int total_ratio = 0;
            for(int i = 0; i < NUM_BUILDINGS; i++) {
                total_ratio += teamRatios[j][i];
            }
            for(int i = 0; i < NUM_BUILDINGS; i++) {
                int building_allocation = 
                    (int) ((
                           ((float)teamRatios[j][i]) / 
                           (total_ratio))*TEAM_POPULATION.get(j));
                teamAllocations[j][i] = building_allocation;
            }
        }
    }

    private static int getRandomNumberInRange(int min, int max) {
		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

    public static Learning2019InputVector getRandomValid() {
        Learning2019InputVector vI = new Learning2019InputVector();
        while ( ! vI.isValid() ) {
            vI = new Learning2019InputVector();
        }
        return vI;
    }

    public List<Double> getGrossSqFtPerPerson()
    {
        final float SQFT_PER_FLOOR = 50e3f;
        List<Double> space = new ArrayList<Double>
            (Collections.nCopies(NUM_BUILDINGS, -1.0));
        calculateAllocations();    
        for (int i = 0; i < NUM_BUILDINGS; i++)
        {
            if ( floors[i] > 0 ) {
                double area = SQFT_PER_FLOOR*floors[i];
                int population = 0;
                for(int j = 0; j < NUM_TEAMS; j++) {
                    population += teamAllocations[j][i];
                }
                space.set(i, area/population);
            }
        }
        return space;
    }

    /**
     * Checks that:
     *  1. At least one of the buildings are enabled 
           and have floors less than equal to MAX_FLOORS. 
     *  2. Teams are assigned only to buildings that are enabled
     *  3. A team is assigned to at least one building
     *  4. The calculated gross_sqft_per_person for each building is greater than equal to 100.0
     */
    boolean isValid() {
        char numBuildings = 0;
        for (int i = 0; i < NUM_BUILDINGS; i++) {
            if (floors[i] > MAX_FLOORS) break;
            if (floors[i] != 0) {
                numBuildings++;
                break;
            } 
        }
        boolean validFloors =  (numBuildings > 0) && (numBuildings <= NUM_BUILDINGS);
        char numValidTeams = 0;
        if (validFloors) {
            for (int j = 0; j < NUM_TEAMS; j++) { 
                boolean validTeam = true;
                numBuildings = 0;
                for (int i = 0; i < NUM_BUILDINGS; i++) {
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
        boolean validAll =  validFloors && (numValidTeams == NUM_TEAMS);
        
        // check densities
        if ( validAll ) {
            List<Double> space = getGrossSqFtPerPerson();
            for (int i = 0; i < NUM_BUILDINGS; i++) {
                if (floors[i] > 0) {
                    if (space.get(i) < MIN_SPACE) {
                        validAll = false;
                        break;
                    }
                }
            }
        }
        return validAll;
    }

    String getUniqueString() {
        String out = "";
        DecimalFormat df = new DecimalFormat("00"); 
        for (int i = 0; i < NUM_BUILDINGS; i++) {
            out += df.format(floors[i]);
        }
        df = new DecimalFormat("0");    
        for (int j = 0; j < NUM_TEAMS; j++) {
            out += "-";
            for (int i = 0; i < NUM_BUILDINGS; i++) {
                out += df.format(teamRatios[j][i]);
            }
        }
        return out;
    }
}