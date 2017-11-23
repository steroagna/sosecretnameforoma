import java.util.*;

public class HEA {

	static private class HEAThread extends Thread{

		public Population population;
		public Data data;
		public Timetable newGen;
		public long timerNewGenLS;
		public int neighborNumberFeasibleConstructor;
		public int neighborLS;

		public HEAThread(Population population, Data data, int neighborNumberFeasibleConstructor, 
				int neighborLS, long timerNewGenLS) {
			super();
			this.population = population;
			this.data = data;
			this.timerNewGenLS = timerNewGenLS;
			this.neighborNumberFeasibleConstructor = neighborNumberFeasibleConstructor;
			this.neighborLS = neighborLS;
		}

		public void run() {
			try {
				newGen = this.heuristic(this.population, this.data, this.neighborNumberFeasibleConstructor, this.neighborLS, this.timerNewGenLS);
			} catch (Exception e) {
				System.out.println("[FeasibleConstructor::FeasibleConstructorThread::run()] Some problem occurred.");
			}
		}

		public Timetable heuristic(Population population, Data data, int neighborNumberFeasibleConstructor, int neighborLS, long timerNewGenLS) throws Exception {

			FeasibleConstructor.FeasibleConstructorThread fb = new FeasibleConstructor.FeasibleConstructorThread(data, 0, neighborNumberFeasibleConstructor, neighborLS);
			Timetable newGen;
			TabuSearchPenalty localSearch = new TabuSearchPenalty();

			newGen = population.copulate(data);
			fb.makeFeasibleGraphColoringWithTabu(data, newGen, neighborNumberFeasibleConstructor);
			newGen = localSearch.TabuSearch(newGen, data, neighborLS, timerNewGenLS);
			newGen.objFunc = Util.ofCalculator(newGen, data);

			return newGen;
		}
	}

	public Timetable parallelHeuristic(Population population, Data data, long timerHEADuration, long timerNewGenLS,int neighborNumberFeasibleConstructor, int neighborLS, int threadsNumber) throws Exception {

		List<HEAThread> heatr = new ArrayList<>();
		long startTime = System.currentTimeMillis(), elapsedTime = 0;
		Timetable newGen;

		while (elapsedTime < 120000) {

			for (int i = 0; i < threadsNumber; i++)
				heatr.add(new HEA.HEAThread(population, data, neighborNumberFeasibleConstructor, neighborLS, timerNewGenLS));

			for (int i = 0; i < threadsNumber; i++)
				heatr.get(i).start();

			for (int i = 0; i < threadsNumber; i++) {
				heatr.get(i).join();

				newGen = heatr.get(i).newGen;

				System.out.println("OF New Generation: " + newGen.objFunc);

				if(newGen.objFunc < population.bestOF) {
					population.bestTimetable = newGen;
					population.bestOF = newGen.objFunc;
				}

				population.updatePopulation(newGen);
			}

			heatr.clear();

			elapsedTime = System.currentTimeMillis() - startTime;

		}

		System.out.println("OF Best TT before LS: " + population.bestTimetable.objFunc);

		return population.bestTimetable;
	}

}
