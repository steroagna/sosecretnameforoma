import java.util.*;

public class HEA {

	static private class HEAThread extends Thread{

		public Population population;
		public Data data;
		public Timetable newGen;

		public HEAThread(Population population, Data data) {
			this.population = population;
			this.data = data;
		}

		public void run() {
			try {
				newGen = this.heuristic(this.population, this.data);
			} catch (Exception e) {
				System.out.println("[FeasibleConstructor::FeasibleConstructorThread::run()] Some problem occurred.");
			}
		}

		public Timetable heuristic(Population population, Data data) throws Exception {

			FeasibleConstructor fb = new FeasibleConstructor(data);
			Timetable newGen;
			TabuSearchPenalty localSearch = new TabuSearchPenalty();
			long timer = 1000;

			newGen = population.copulate(data);
			fb.makeFeasibleGraphColoringWithTabu(data, newGen);
			newGen = localSearch.TabuSearch(newGen, data, timer);
			newGen.objFunc = Util.ofCalculator(newGen, data);

			return newGen;
		}
	}

	public Timetable parallelHeuristic(Population population, Data data, int threadsNumber) throws Exception {

		List<HEAThread> heatr = new ArrayList<>();
		long startTime = System.currentTimeMillis(), elapsedTime = 0;
		TabuSearchPenalty localSearch = new TabuSearchPenalty();
		Timetable newGen;

		while (elapsedTime < 120000) {

			for (int i = 0; i < threadsNumber; i++)
				heatr.add(new HEA.HEAThread(population, data));

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
		Timetable lastbestTimetable = localSearch.TabuSearch(population.bestTimetable, data, 10000);
		System.out.println("OF Last TT after LS: " + lastbestTimetable.objFunc);

		return lastbestTimetable;
	}

}
