import java.sql.Time;

public class HEA {

	public Timetable heuristic(Population population, Data data) throws Exception {
		
		FeasibleConstructor fb = new FeasibleConstructor(data);
		Timetable newGen;
		TabuSearchPenalty localSearch = new TabuSearchPenalty();
		long startTime = System.currentTimeMillis(), elapsedTime = 0;
		long timer = 2000;
		
		while (elapsedTime < 120000) {
			newGen = population.copulate(data);
			fb.makeFeasibleGraphColoringWithTabu(data, newGen);
			newGen = localSearch.TabuSearch(newGen, data, timer);
			newGen.objFunc = Util.ofCalculator(newGen, data);

			System.out.println("OF New Generation: " + newGen.objFunc);

			if(newGen.objFunc < population.bestOF) {
				population.bestTimetable = newGen;
				population.bestOF = newGen.objFunc;
			}
			population.updatePopulation(newGen);
	        elapsedTime = System.currentTimeMillis() - startTime;
		}

		System.out.println("OF Best TT before LS: " + population.bestTimetable.objFunc);
		Timetable lastbestTimetable = localSearch.TabuSearch(population.bestTimetable, data, 10000);
		System.out.println("OF Last TT after LS: " + lastbestTimetable.objFunc);

		return lastbestTimetable;
	}

}
