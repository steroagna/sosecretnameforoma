public class HEA {

	public Timetable heuristic(Population population, Data data) throws Exception {
		
		FeasibleConstructor fb = new FeasibleConstructor(data);
		Timetable bestTimetable = null, newGen;
		TabuSearchPenalty localSearch = new TabuSearchPenalty();
		long startTime = System.currentTimeMillis(), elapsedTime = 0;
		long timer = 5000;
		
		while (elapsedTime < 120000) {
			newGen = population.copulate(data);
			fb.makeFeasibleGraphColoringWithTabu(data, newGen);
			newGen.objFunc = Util.ofCalculator(newGen, data);
			newGen = localSearch.TabuSearch(newGen, data, timer);

			System.out.println("OF New Generation? " + newGen.objFunc);
			System.out.println("Feasible " + Util.feasibilityChecker(newGen,data));

			if(newGen.objFunc < population.bestOF)
				bestTimetable = newGen;
			population.updatePopulation(newGen);
	        elapsedTime = System.currentTimeMillis() - startTime;
		}

		bestTimetable = localSearch.TabuSearch(bestTimetable, data, 10000);
		
		return bestTimetable;
	}

}
