import java.util.*;

public class FeasibleCostructor {
	/**
	 * Ordered Exam List based on Vertex number
	 */
	static TreeSet<Exam> treeMapExams = new TreeSet<>();

	/**
	 * Search and set a feasible timetable solution for current problem 
	 * described by data object.
	 * @throws Exception 
	 * */
	public Timetable makeFeasibleGraphColoringWithTabu(Data data) throws Exception {

		double density = (double)data.totalConflicts / (double)(data.examsNumber*data.examsNumber);
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
		
		Timetable timetable = new Timetable(G,k);
		TabuList tabulist = new TabuList(T);
		
		// Random coloring.
		randomSolution(timetable, new ArrayList<>(data.examsMap.keySet()));
//		notRandomSolution(timetable, data);

		while(timetable.conflictNumber>0) {

//			int rep = r * timetable.conflictNumber;
			TabuMove bestMove = generatesBestNeighbour(timetable,G,tabulist,rep, data);

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
	 * Setting of unfeasible random solution based on number of student enrolled.
	 * */
	private void notRandomSolution(Timetable timetable, Data data) {

		Random randTimeslot = new Random();
		int i, j, index = 0;
		Exam exam;
		int examId;
		ArrayList<Integer> slot;
		boolean conflict, found;

		for (i = 1; i <= data.examsNumber; i++) {
			exam = data.getExam(i);
			treeMapExams.add(exam);
		}

		i = 0;
		for(Iterator<Exam> itExam=treeMapExams.iterator();itExam.hasNext();) {
			exam = itExam.next();
//			if (i / data.examsNumber < 0.02)
//				exam.mark();
			i++;
			examId = exam.getId();
			found = false;
			for(i = 0; i < timetable.timeSlots.size() && !found; i++) {
				index = i;
				slot = timetable.timeSlots.get(i);
				conflict = false;
				for (j = 0; j < slot.size() && !conflict; j++) {
					if (data.conflictExams[examId][slot.get(j)] > 0)
						conflict = true;
				}
				if (!conflict)
					found = true;
			}
			if (i == timetable.timeSlots.size() && !found)
				index = randTimeslot.nextInt(timetable.timeSlots.size());
			timetable.addExam(index, examId);
		}

	}

	/**
	 * Method that generates move to best neighbour if exist.
	 * */
	private TabuMove generatesBestNeighbour(Timetable timetable, int [][] G, TabuList tabulist,int rep, Data data){

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

			TabuMove move = generatesNeighbour(timetable,tabulist,bestConflictNumber,randConflict,randExam,randTimeslot, data);

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
	private TabuMove generatesNeighbour(Timetable timetable, TabuList tabulist, int bestConflictsNumber,Random randConflict, Random randExam, Random randTimeslot, Data data) {

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

			if (data.getExam(examSelected).isMarked())
				continue;

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
