import edu.mit.gtl.masie.analysis.CalcMasie;
import edu.mit.gtl.masie.analysis.MorphologicalMatrix;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class TestCalcMasie {
    
    public static void main(String[] args) {
		int popSize = 10000;                 // %number of people on the site 
		double hFloor = 15;                 // %height of one floor/storey, in ft
		int popRed = 5000;                  // %number of people on Red team
		int popBlue = 1500;                  // %number of people on Blue team
		int popGreen = 3500;                 // %number of people on Green team
		
        List<Boolean> activeBuildings = List.of(true, true, true, true, true, true);
		List<Integer> numberOfFloors  = List.of(5, 3, 3, 3, 3, 3);

		//% Define team organization matrix (3x6) - team population allocated to each building
		//% Check to do: sum of elements in each row (across columns) = number of units in each team
		//% Rows: [Red: ...
		//%        Blue: ...
		//%        Green: ... ]
		List<Integer> redTeamAllocation = List.of(665, 167, 167, 167, 167, 167);
		List<Integer> greenTeamAllocation = List.of(1000,  500,  500, 500, 500, 500);
		List<Integer> blueTeamAllocation = List.of(835, 833, 833, 833, 833, 833);
		
		MorphologicalMatrix seTting = new MorphologicalMatrix(activeBuildings, numberOfFloors,
	            redTeamAllocation, greenTeamAllocation, blueTeamAllocation);
		
		//System.out.println(seTting.getSettingPackage());
		
		CalcMasie calc = new CalcMasie(seTting);
		//calc.setSettings(seTting);
		
		//calc.centerZones(zoneSize);
		calc.run();
		calc.displayResults();
		// calc.dispCenterZones();
		System.out.println(calc.getResults());
    }

}