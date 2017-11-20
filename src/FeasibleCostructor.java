import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class FeasibleCostructor {

	public Data data;
	/**
	 * Private costructor.
	 * */
	public FeasibleCostructor(Data data) {
		this.data = data;
	}

	/**
	 * Internal class useful to generate more
	 * than one feasible solutions in parallel
	 * way.
	 * */
	static private class FeasibleCostructorThread extends Thread{

		public Timetable timetable;
		private FeasibleCostructor feasibleCostructor;

		public FeasibleCostructorThread(FeasibleCostructor fc) {
			this.feasibleCostructor = fc;
		}

		public void run() {
			try {
				this.timetable = this.feasibleCostructor.makeFeasibleGraphColoringWithTabu(feasibleCostructor.data, timetable);
			} catch (Exception e) {
				System.out.println("[FeasibleConstructor::FeasibleConstructorThread::run()] Some problem occurred.");
			}
		}
	}

	public Population makeFeasiblePopulation(Data data, int populationSize) throws Exception{

		long startTime = System.currentTimeMillis(), elapsedTime;
		double bestOF = Integer.MAX_VALUE;
		Timetable t;
		ArrayList<Timetable> population = new ArrayList<>();
		List<FeasibleCostructorThread> fcts = new ArrayList<FeasibleCostructorThread>();

		for (int i = 0; i < populationSize; i++)
			fcts.add(new FeasibleCostructor.FeasibleCostructorThread(new FeasibleCostructor(data)));

		for (int i = 0; i < populationSize; i++)
			fcts.get(i).start();

		for (int i = 0; i < populationSize; i++) {
			fcts.get(i).join();
			t = fcts.get(i).timetable;
			t.objFunc = Tools.ofCalculator(t, data);
			if ( t.objFunc < bestOF)
				bestOF = t.objFunc;
			population.add(t);
			if (Main.debug) {
				elapsedTime = System.currentTimeMillis() - startTime;
				System.out.println("Feasable: "+ Util.feasibilityChecker(t, data));
				System.out.println("Elapsed time: " + elapsedTime);
				System.out.println("OF? " + Tools.ofCalculator(t, data));
			}
		}

		return new Population(population, bestOF);
	}
	
	/**
	 * Search and set a feasible timetable solution for current problem 
	 * described by data object.
	 * @throws Exception 
	 * */
	public Timetable makeFeasibleGraphColoringWithTabu(Data data, Timetable timetable) throws Exception {

//		double density = (double)data.totalConflicts / (double)(data.examsNumber*data.examsNumber);
		int[][] 	G = data.conflictExams;
		int 		k = data.slotsNumber;
		int			T = 7;
//		int 		r = (int) (1/density);
		int rep = 10;
		//int	rep = (int) ((1/density)*(data.examsNumber)/7);
//		int	rep = (int) ((1/density)*(Math.sqrt(data.examsNumber)));
		//int rep = (int) (data.examsNumber/2);
		//int rep = (int) (k*(2/density));
		//int rep = data.examsNumber/k;
		
		if (timetable == null) {
			timetable = new Timetable(G, k);
			// Random coloring.
			randomSolution(timetable, new ArrayList<Integer>(data.examsMap.keySet()));
		}
		TabuList tabulist = new TabuList(T);

		while(timetable.conflictNumber>0) {

//			int rep = r * timetable.conflictNumber;
			TabuMove bestMove = generatesBestNeighbour(timetable,G,tabulist,rep);

			if(bestMove==null) {
				/*
				** Statement unreachable (in theory).
				*/
				System.out.println("No better neighbour found !");
				break;
			}

			timetable.doSwitch(bestMove.idExam, bestMove.sourceTimeSlot, bestMove.destinationTimeSlot);
			tabulist.addTabuItem(bestMove);
		}

		return timetable;
	}

	/**
	 * Setting of unfeasible random solution.
	 * */
	private void randomSolution(Timetable timetable , List<Integer> exams) {

		Random randTimeslot = new Random();

		for(Iterator<Integer> itExam=exams.iterator();itExam.hasNext();)
			timetable.addExam(randTimeslot.nextInt(timetable.timeSlots.size()),itExam.next());

	}

	/**
	 * Method that generates move to best neighbour if exist.
	 * */
	private TabuMove generatesBestNeighbour(Timetable timetable, int [][] G, TabuList tabulist,int rep){

		Random randTimeslot = new Random();
		Random randExam = new Random();
		Random randConflict = new Random();

		boolean foundBetterTimetable = false;

		int bestConflictNumber = 0xFFFFFF;
		int bestTimeslotSource = -1;
		int bestTimeslotDestination = -1;
		int bestExamSelected = -1;

		/*
		 * Generates and searches best neighbor.
		 * */
		for(int i=0;i<rep;i++) {

			TabuMove move = generatesNeighbour(timetable,tabulist,bestConflictNumber,randConflict,randExam,randTimeslot);

			int conflictNumber = timetable.evaluatesSwitch(move.idExam,move.sourceTimeSlot,move.destinationTimeSlot);

			/*
			 * Check for aspiration level.
			 * */
			if(tabulist.getTabuList().contains(move) && conflictNumber < (bestConflictNumber-1)) {
				bestTimeslotSource = move.sourceTimeSlot;
				bestExamSelected = move.idExam;
				bestTimeslotDestination = move.destinationTimeSlot;
				foundBetterTimetable = true;
				break;
			}

			if(conflictNumber < bestConflictNumber) {
				bestConflictNumber = conflictNumber;
				bestTimeslotSource = move.sourceTimeSlot;
				bestExamSelected = move.idExam;
				bestTimeslotDestination = move.destinationTimeSlot;
				foundBetterTimetable = true;
			}

		}

		if(foundBetterTimetable) {
			TabuMove tabuItem = new TabuMove(bestExamSelected, bestTimeslotSource, bestTimeslotDestination);
			tabulist.addTabuItem(tabuItem);
			return tabuItem;
		}

		/*
		 * Statement unreachable (in theory).
		 * */
		return null;
	}

	/**
	 * Method that generates a valid neighbour.
	 * */
	private TabuMove generatesNeighbour(Timetable timetable, TabuList tabulist, int bestConflictsNumber,Random randConflict, Random randExam, Random randTimeslot) {

		TabuMove moving;

		for(;;) {

			int timeslotSource = randTimeslot.nextInt(timetable.timeSlots.size());
			int timeslotDestination = randTimeslot.nextInt(timetable.timeSlots.size());

			/*
			 * If timeslot source doesn't contain conflicts or timeslots source is also the destination
			 * try to generate another neighbour.
			 * */
			//timetable.timeSlotsConflict.get(timeslotSource).size()==0 ||
			if(
				timeslotSource==timeslotDestination 						||
				timetable.timeSlots.get(timeslotSource).size()==0 			||
				timetable.timeSlotsConflict.get(timeslotSource).size()==0
			)
			{
				continue;
			}

			int conflictSelected = randConflict.nextInt(timetable.timeSlotsConflict.get(timeslotSource).size());
			//int examToSwitch = randConflict.nextInt(timetable.timeSlots.get(timeslotSource).size());
			int examSelected;

			if(randExam.nextBoolean()) {
				examSelected = timetable.timeSlotsConflict.get(timeslotSource).get(conflictSelected).e1;
			}else {
				examSelected = timetable.timeSlotsConflict.get(timeslotSource).get(conflictSelected).e2;
			}
			//examSelected = timetable.timeSlots.get(timeslotSource).get(examToSwitch);
			//		.timeSlotsConflict.get(timeslotSource).get(conflictSelected).e1;

			moving = new TabuMove(examSelected, timeslotSource, timeslotDestination);
			int conflictNumber = timetable.evaluatesSwitch(examSelected,timeslotSource,timeslotDestination);
			/*
			 * Check if moving is already in the tabulist: if true try with another neighbour.
			 * */
			if((tabulist.getTabuList().contains(moving) &&
					conflictNumber >=(bestConflictsNumber-1)
			)) {
				continue;
			}

			break;
		}

		return moving;

	}

}
