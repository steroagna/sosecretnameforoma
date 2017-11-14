

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class FeasibleCostructor {	
	/**
	 * @throws Exception 
	 * 
	 * */
	public Timetable makeFeasibleGraphColoringWithTabu(Data data) throws Exception {
		
		int[][] 	G = data.conflictExams;
		int 		k = data.slotsNumber;
		int			T = 7;
		int			rep;
	
		Timetable timetable = new Timetable(G,k);
		TabuList tabulist = new TabuList(T);
		
		// Random coloring.
		randomSolution(timetable, new ArrayList<Integer>(data.examsMap.keySet()));
		
		while(timetable.conflictNumber>0) {

			rep = timetable.conflictNumber*2;
			
			if (rep > data.slotsNumber)
				rep = data.slotsNumber;
			
			TabuItem bestMove = generatesBestNeighbour(timetable,G,tabulist,rep);
			
			if(bestMove==null) {
				// Statement unreachable (in theory).
				System.out.println("No better neighbour found !");
				break;
			}
			
			timetable.doSwitch(bestMove.idExam, bestMove.sourceTimeSlot, bestMove.destinationTimeSlot);
		}
		
		return timetable;
	}
	
	/**
	 * Setting of infeasible random solution.
	 * */
	private void randomSolution(Timetable timetable , List<Integer> exams) {
		
		Random randTimeslot = new Random();
	
		for(Iterator<Integer> itExam=exams.iterator();itExam.hasNext();)
			timetable.addExam(randTimeslot.nextInt(timetable.timeSlots.size()),itExam.next());
		
	}

	/**
	 * Method that generates move to best neighbour if exist. 
	 * */
	private TabuItem generatesBestNeighbour(Timetable timetable, int [][] G, TabuList tabulist,int rep){
		
		Random rand = new Random();
		Random rand01 = new Random();
		Random randConflict = new Random();
		
		boolean foundBetterTimetable = false;
		
		int bestConflictNumber = 0xFFFFFF;
		int bestTimeslotSource = -1;
		int bestTimeslotDestination = -1;
		int bestExamSelected = -1;
		
		for(int i=0;i<rep;i++) {

			int timeslotSource = rand.nextInt(timetable.timeSlots.size());
			int timeslotDestination = rand.nextInt(timetable.timeSlots.size());
			
			// Checking valid moving.
			if(timetable.timeSlotsConflict.get(timeslotSource).size()==0
					|| timetable.timeSlotsConflict.get(timeslotSource).size()==0
					|| timeslotSource==timeslotDestination) {
				i--;
				continue;
			}
			
			int conflictSelected = randConflict.nextInt(timetable.timeSlotsConflict.get(timeslotSource).size());
			int examSelected = -1;
			
			if(rand01.nextBoolean()) {
				examSelected = timetable.timeSlotsConflict.get(timeslotSource).get(conflictSelected).e1;
			}else {
				examSelected = timetable.timeSlotsConflict.get(timeslotSource).get(conflictSelected).e2;
			}
			
			TabuItem moving = new TabuItem(examSelected, timeslotSource, timeslotDestination);
			int conflictNumber = timetable.evaluatesSwitch(examSelected,timeslotSource,timeslotDestination);
			
			if((
					tabulist.getTabuList().contains(moving) &&
					conflictNumber >=(bestConflictNumber-1) 
			)) {
				i--;
				continue;
			}
			
			if(
				tabulist.getTabuList().contains(moving) && 
				conflictNumber < (bestConflictNumber-1)
			) {	
				tabulist.getTabuList().remove(moving);
				bestConflictNumber = conflictNumber;
				bestTimeslotSource = timeslotSource;
				bestExamSelected = examSelected;
				bestTimeslotDestination = timeslotDestination;
				foundBetterTimetable = true;
				break;
			}
			
			if(conflictNumber < bestConflictNumber) {
				bestConflictNumber = conflictNumber;
				bestTimeslotSource = timeslotSource;
				bestExamSelected = examSelected;
				bestTimeslotDestination = timeslotDestination;
				foundBetterTimetable = true;
			}
		}
		
		if(foundBetterTimetable) {
			TabuItem tabuItem = new TabuItem(bestExamSelected, bestTimeslotSource, bestTimeslotDestination);
			return tabuItem;
		}
		return null;
	}
}
