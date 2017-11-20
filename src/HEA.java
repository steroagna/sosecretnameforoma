public class HEA {

	public Timetable heuristic(Population population, Data data) throws Exception {
		
		FeasibleCostructor fb = new FeasibleCostructor(data);
		Timetable bestTimetable = null, newGen;
		TabuSearchPenalty localSearch = new TabuSearchPenalty();
		long startTime = System.currentTimeMillis(), elapsedTime = 0;
		int iterations = 100;
		
		while (elapsedTime < 120000) {
			newGen = population.copulate();
			fb.makeFeasibleGraphColoringWithTabu(data, newGen);
			newGen.objFunc = Tools.ofCalculator(newGen, data);
			newGen = localSearch.TabuSearch(newGen, data, iterations);
			if(newGen.objFunc < population.bestOF)
				bestTimetable = newGen;
			population.updatePopulation(newGen);
	        elapsedTime = System.currentTimeMillis() - startTime;
		}
		
		return bestTimetable;
	}

}
