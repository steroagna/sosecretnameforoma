import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FeasibleConstructor {
	/**
	 * Search and set a feasible timetable solution for current problem
	 * described by data object.
	 * @throws Exception
	 * */
	public Timetable makeFeasibleGraphColoringWithTabu(Data data, Timetable timetable, int rep) throws Exception {
		TabuList tabulist = new TabuList(7);
		if (timetable == null) {
			timetable = new Timetable(data);
			randomSolution(timetable, new ArrayList<>(data.examsMap.keySet()));
		}

		while(timetable.conflictNumber>0) {
			TabuMove bestMove = generatesBestNeighbour(timetable, tabulist, rep);
			timetable.doSwitch(bestMove.idExam, bestMove.sourceTimeSlot, bestMove.destinationTimeSlot);
			tabulist.addTabuItem(bestMove);
		}
		return timetable;
	}

	/**
	 * Setting of infeasible random solution.
	 * */
	private void randomSolution(Timetable timetable , List<Integer> exams) {
		for(Iterator<Integer> itExam=exams.iterator();itExam.hasNext();)
			timetable.addExam(ThreadLocalRandom.current().nextInt(timetable.timeSlots.size()),itExam.next());
	}

	/**
	 * Method that generates move to best neighbor if exist.
	 * */
	private TabuMove generatesBestNeighbour(Timetable timetable, TabuList tabulist,int rep){
		TabuMove tabuItem, move;
		int bestConflictNumber = 0xFFFFFF,
				bestTimeslotSource = -1,
				bestTimeslotDestination = -1,
				bestExamSelected = -1,
				conflictNumber;

		for(int i=0;i<rep;i++) {
			move = generatesNeighbour(timetable,tabulist,bestConflictNumber);
			conflictNumber = timetable.evaluatesSwitch(move.idExam,move.sourceTimeSlot,move.destinationTimeSlot);
			if(tabulist.getTabuList().contains(move) && conflictNumber < (bestConflictNumber-1)) {
				bestTimeslotSource = move.sourceTimeSlot;
				bestExamSelected = move.idExam;
				bestTimeslotDestination = move.destinationTimeSlot;
				break;
			}
			if(conflictNumber < bestConflictNumber) {
				bestConflictNumber = conflictNumber;
				bestTimeslotSource = move.sourceTimeSlot;
				bestExamSelected = move.idExam;
				bestTimeslotDestination = move.destinationTimeSlot;
			}

		}
		tabuItem = new TabuMove(bestExamSelected, bestTimeslotSource, bestTimeslotDestination);
		tabulist.addTabuItem(tabuItem);
		return tabuItem;
	}

	/**
	 * Method that generates a valid neighbour.
	 * */
	private TabuMove generatesNeighbour(Timetable timetable, TabuList tabulist, int bestConflictsNumber) {
		TabuMove moving;
		int timeslotSource, timeslotDestination, conflictSelected, examSelected, conflictNumber;

		for(;;) {
			timeslotSource = ThreadLocalRandom.current().nextInt(timetable.timeSlots.size());
			timeslotDestination = ThreadLocalRandom.current().nextInt(timetable.timeSlots.size());
			if(timeslotSource==timeslotDestination
					|| timetable.timeSlots.get(timeslotSource).size()==0
					|| timetable.timeSlotsConflict.get(timeslotSource).size()==0
					)
				continue;
			conflictSelected = ThreadLocalRandom.current().nextInt(timetable.timeSlotsConflict.get(timeslotSource).size());
			if(ThreadLocalRandom.current().nextBoolean()) {
				examSelected = timetable.timeSlotsConflict.get(timeslotSource).get(conflictSelected).e1;
			}else {
				examSelected = timetable.timeSlotsConflict.get(timeslotSource).get(conflictSelected).e2;
			}
			moving = new TabuMove(examSelected, timeslotSource, timeslotDestination);
			conflictNumber = timetable.evaluatesSwitch(examSelected,timeslotSource,timeslotDestination);
			if((tabulist.getTabuList().contains(moving) && conflictNumber >=(bestConflictsNumber-1)))
				continue;
			break;
		}
		return moving;
	}
}
