import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FeasibleConstructor {

	/**
	 * Internal class useful to generate more
	 * than one feasible solutions in parallel
	 * way.
	 * */
	static private class FeasibleConstructorThread extends Thread{
		
		public Timetable timetable;
        private FeasibleConstructor feasibleCostructor;
        
        public FeasibleConstructorThread(FeasibleConstructor fc) {
            this.feasibleCostructor = fc; 
        }

        public void run() {
        	try {
				this.timetable = this.feasibleCostructor.makeFeasibleGraphColoringWithTabu(null);
		        TabuSearchPenalty tsp = new TabuSearchPenalty();
		        this.timetable = tsp.TabuSearch(this.timetable, this.timetable.data, 150, 5000);
			} catch (Exception e) {
				System.out.println("[FeasibleConstructor::FeasibleConstructorThread::run()] Some problem occurred.");
			}
        }
	}
	
	/**
	 * Generates, in parallel way, n feasible timetables.
	 * @throws InterruptedException 
	 * */
	public static List<Timetable> generatesFeasibleTimetables(Data data,int n) throws InterruptedException{
		
		List<Timetable> timetables = new ArrayList<Timetable>();
		List<FeasibleConstructorThread> fcts = new ArrayList<FeasibleConstructorThread>();
		
		for(int i=0;i<n;i++)
			fcts.add(new FeasibleConstructor.FeasibleConstructorThread(new FeasibleConstructor(data)));
		
		for(int i=0;i<n;i++)
			fcts.get(i).start();
		for(int i=0;i<n;i++) {
			fcts.get(i).join();
			timetables.add(fcts.get(i).timetable);
		}
		
		return timetables;
	}
	
	/**
	 * Temporany static method. 
	 * @throws Exception 
	 * */
	public static Timetable generatesFeasibleTimetable(Timetable timetable) throws Exception{
		FeasibleConstructor feasibleConstructor = new FeasibleConstructor(timetable.data);
		return feasibleConstructor.makeFeasibleGraphColoringWithTabu(timetable);
	}
	
	/**
	 * Data input object.
	 * */
	private Data data;
	
	/**
	 * Best cost found during searching.
	 * */
	private Integer bestConflictsNumberFromStarting = Integer.MAX_VALUE;
	
	/**
	 * Private constructor.
	 * */
	private FeasibleConstructor(Data data) {
		this.data = data;
	}
	
	
	/**
	 * Search and set a feasible timetable solution for current problem 
	 * described by data object.
	 * @throws Exception 
	 * */
	public Timetable makeFeasibleGraphColoringWithTabu(Timetable timetableStart) throws Exception {
		
		int[][] 	G = data.conflictExams;
		int			T = 7;

		int rep = -1;
		int minRep = 10;
		
		Timetable timetable = null;
		if(timetableStart==null)
			timetable = new Timetable(data);
		else
			timetable = timetableStart;
		
		TabuList tabulist = new TabuList(T);
		
		if(timetableStart==null)
			// Random coloring.
			randomSolution(timetable, new ArrayList<Integer>(data.examsMap.keySet()));
		
		while(timetable.conflictNumber>0) {
		
			// Selecting number of neighbours to generate
			if(timetable.conflictNumber>minRep)
				rep = timetable.conflictNumber ;
			else
				rep = minRep;
			
			TabuMove bestMove = generatesBestNeighbour(timetable,G,tabulist,rep);
			
			if(bestMove==null) {
				/*
				** Statement unreachable (in theory).
				*/
				System.out.println("No better neighbour found !");
				break;
			}
			
			timetable.doSwitch(bestMove.idExam, bestMove.sourceTimeSlot, bestMove.destinationTimeSlot);
			TabuMove moving = new TabuMove(bestMove.idExam, bestMove.sourceTimeSlot, bestMove.destinationTimeSlot);
			
			tabulist.addTabuMove(moving);
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
		
		int bestConflictNumber = Integer.MAX_VALUE;
		int bestTimeslotSource = -1;
		int bestTimeslotDestination = -1;
		int bestExamSelected = -1;
		
		/*
		 * Generates and searches best neighbour.
		 * */
		for(int i=0;i<rep;i++) {
			
			TabuMove move = generatesNeighbour(timetable,tabulist,bestConflictNumber,bestConflictsNumberFromStarting,randConflict,randExam,randTimeslot);

			
			int conflictNumber =  timetable.evaluatesSwitch(move.idExam,move.sourceTimeSlot,move.destinationTimeSlot);
			
			if(conflictNumber < bestConflictNumber) {
				bestConflictNumber = conflictNumber;
				bestTimeslotSource = move.sourceTimeSlot;
				bestExamSelected = move.idExam;
				bestTimeslotDestination = move.destinationTimeSlot;
				foundBetterTimetable = true;
			}
			if(conflictNumber< bestConflictsNumberFromStarting)
				bestConflictsNumberFromStarting = conflictNumber;
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
	private TabuMove generatesNeighbour(Timetable timetable, TabuList tabulist, int bestConflictsNumber, int bestConflictsNumberFromStart,Random randConflict, Random randExam, Random randTimeslot) {
		
		TabuMove moving = null;

		for(;;) {
			
			
			/* Per evitare di tirare sempre a caso il time source*/
			int timeslotSource = (new ArrayList<Integer>(timetable.timeSlotWithConflicts.values())).get(randTimeslot.nextInt(timetable.timeSlotWithConflicts.size()));
			
			//int timeslotSource = randTimeslot.nextInt(timetable.timeSlots.size());
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
			int examSelected = -1;
			
			if(randExam.nextBoolean()) {
				examSelected = timetable.timeSlotsConflict.get(timeslotSource).get(conflictSelected).e1;
			}else {
				examSelected = timetable.timeSlotsConflict.get(timeslotSource).get(conflictSelected).e2;
			}
			
			//###########
			//if(fixed.containsKey(examSelected)==true) continue;
			//###########
			moving = new TabuMove(examSelected, timeslotSource, timeslotDestination);
			
			/*
			 * Check if moving is already in the tabulist: if true try with another neighbour.
			 * */
			if((
					tabulist.getTabuList().contains(moving) &&
					timetable.evaluatesSwitch(examSelected,timeslotSource,timeslotDestination)>(bestConflictsNumberFromStart-1) 
			)) {
				continue;
			}

			/*
			 * Check for aspiration.
			 * */
			if(
					tabulist.getTabuList().contains(moving) && 
					timetable.evaluatesSwitch(examSelected,timeslotSource,timeslotDestination)<=(bestConflictsNumberFromStart-1)
			) {	
				tabulist.removeTabuMove(moving);
			}

			break;
		}
		
		return moving;
		
	}
	
}
