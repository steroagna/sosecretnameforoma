import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class FeasibleCostructor {
	/**
	 * Search and set a feasible timetable solution for current problem 
	 * described by data object.
	 * @throws Exception 
	 * */
	public Timetable makeFeasibleHEA(Data data) throws Exception {
		
		double density = (double)data.totalConflicts / (double)(data.examsNumber*data.examsNumber);
		int 		numberOfConflict = Integer.MAX_VALUE;
		int[][] 	G = data.conflictExams;
		int 		k = data.slotsNumber;
		int			T = 7;
		int 		pop = 10;
		int 		rep = 20;
		int 		i;
		Timetable timetable, parent1, parent2, newGen = new Timetable(G,k);
		ArrayList<Timetable> population = new ArrayList<>();
		
		// Random population
		for (i = 0; i < pop; i++) {
			timetable = randomSolution(new Timetable(G,k), new ArrayList<>(data.examsMap.keySet()));
			timetable = LS(timetable, T, rep);
			if (timetable.conflictNumber < numberOfConflict)
				numberOfConflict = timetable.conflictNumber;
			if (numberOfConflict == 0) {
				newGen = timetable;
				break;
			}
			population.add(timetable);
		}

		for (i = 0; i < pop; i++) {
			population.get(i).toString();
		}

		while (numberOfConflict > 0) {
			Random randParent = new Random();
			int rand1 = 0, rand2 = 0;
			while (rand1 == rand2) {
				rand1 = randParent.nextInt(population.size());
				rand2 = randParent.nextInt(population.size());
			}
			parent1 = population.get(rand1);
			parent2 = population.get(rand2);
			newGen 	= crossover(parent1, parent2, newGen);
			newGen 	= LS(newGen, T, rep);
			if (newGen.conflictNumber > 0)
				population.set(randParent.nextInt(population.size()), newGen);
			else
				break;
		}

		return newGen;
	}

	/**
	 * Setting of unfeasible random solution.
	 * */
	private Timetable randomSolution(Timetable timetable , List<Integer> exams) {

		Random randTimeslot = new Random();

		for(Iterator<Integer> itExam=exams.iterator();itExam.hasNext();)
			timetable.addExam(randTimeslot.nextInt(timetable.timeSlots.size()),itExam.next());

		return timetable;
	}


	private Timetable LS(Timetable timetable, int sizeTabu, int rep) {

		int i;
		for (i = 0; i < rep && timetable.conflictNumber > 0; i++) {
			TabuList tabulist = new TabuList(sizeTabu);
			TabuMove bestMove = generatesBestNeighbour(timetable, timetable.G, tabulist, rep);
			if (bestMove == null) {
				/*
				** Statement unreachable (in theory).
				*/
				System.out.println("No better neighbour found !");
				break;
			}
			timetable.doSwitch(bestMove.idExam, bestMove.sourceTimeSlot, bestMove.destinationTimeSlot);
		}
		return timetable;
	}

	private Timetable crossover(Timetable parent1, Timetable parent2, Timetable newGen) {

		Random randTimeslot = new Random();
		Timetable A = null, B, C = new Timetable(parent1), D = new Timetable(parent2);
		int cardinalityMax;
		int i, j, k, l, m, columnMax = 0;
		for (i = 0; i < parent1.timeSlots.size(); i++) {
			cardinalityMax = 0;

			if ( i%2 == 0) {
				A = C;
				B = D;
			} else {
				A = D;
				B = C;
			}

			for (j = 0; j < A.timeSlots.size(); j++) {
				if (A.timeSlots.get(j).size() > cardinalityMax) {
					cardinalityMax = A.timeSlots.get(j).size();
					columnMax = j;
				}
			}

			for (k = 0; k < A.timeSlots.get(columnMax).size(); k++) {
				int e = A.timeSlots.get(columnMax).get(k);
				A.timeSlots.get(columnMax).remove(k);
				newGen.addExam(i,e);
				boolean deleted = false;
				for (l = 0; l < B.timeSlots.size() && !deleted; l++) {
					for (m = 0; m < B.timeSlots.get(l).size() && !deleted; m++) {
						if (B.timeSlots.get(l).get(m) == e) {
							B.timeSlots.get(l).remove(m);
							deleted = true;
						}
					}
				}
			}
		}

		for (i = 0; i < A.timeSlots.size(); i++) {
			for (m = 0; m < A.timeSlots.get(i).size(); m++)
				newGen.addExam(randTimeslot.nextInt(newGen.timeSlots.size()), A.timeSlots.get(i).get(m));
		}

			return newGen;
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
		 * Generates and searches best neighbour.
		 * */
		for(int i=0;i<rep;i++) {

			TabuMove move = generatesNeighbour(timetable,tabulist,bestConflictNumber,randConflict,randExam,randTimeslot);

			int conflictNumber =  timetable.evaluatesSwitch(move.idExam,move.sourceTimeSlot,move.destinationTimeSlot);

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

		TabuMove moving = null;

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
			int examSelected = -1;

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
			if((
					tabulist.getTabuList().contains(moving) &&
							conflictNumber >=(bestConflictsNumber-1)
			)) {
				continue;
			}

			/*
			 * Check for aspiration level.
			 * */
			if(
					tabulist.getTabuList().contains(moving) &&
							conflictNumber<(bestConflictsNumber-1)
			) {
				tabulist.getTabuList().remove(moving);
			}

			break;
		}

		return moving;

	}

}
