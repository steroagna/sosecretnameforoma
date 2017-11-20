public class HEA {

	public Timetable heuristic(Population population, Data data) throws Exception {
		
		FeasibleConstructor fb = new FeasibleConstructor(data);
		Timetable bestTimetable = null, newGen;
		TabuSearchPenalty localSearch = new TabuSearchPenalty();
		long startTime = System.currentTimeMillis(), elapsedTime = 0;
		int iterations = 10000;
		
		while (elapsedTime < 300000) {
			newGen = population.generateSon();
			fb.makeFeasibleGraphColoringWithTabu(data, newGen);
			newGen.objFunc = Tools.ofCalculator(newGen, data);
			newGen = localSearch.TabuSearch(newGen, data, iterations);

			System.out.println("OF New Generation? " + newGen.objFunc);

			if(newGen.objFunc < population.bestOF)
				bestTimetable = newGen;
			population.updatePopulation(newGen);
	        elapsedTime = System.currentTimeMillis() - startTime;
		}
		
		return bestTimetable;
	}

}
