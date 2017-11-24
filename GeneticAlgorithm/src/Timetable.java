import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class Timetable implements Comparable<Timetable> {
	
	// Internal classes
	class SlotInfo implements Comparable<SlotInfo>{
		int id;
		Double penalty;
		PriorityQueue<ExamInfo> orderedExamInfo;
		
		public SlotInfo(int id) {
			this.id = id;
			this.orderedExamInfo = new PriorityQueue<ExamInfo>();
			this.penalty = (double) 0;
		}		
		
		public void addExamInfo(ExamInfo examInfo) {
			this.orderedExamInfo.add(examInfo);
			this.penalty+=examInfo.penalty;
		}
		
		@Override 
		public int compareTo(SlotInfo slotInfo){
			
			return -this.penalty.compareTo(slotInfo.penalty);
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof SlotInfo) {
				if(((SlotInfo)o).id==this.id)
					return true;
			}
			return false;
			
		}
	}
	
	
	class ExamInfo implements Comparable<ExamInfo>{
		int id;
		Double penalty;
		int timeslot;
		
		public ExamInfo(int id, double penalty, int timeslot) {
			this.id = id;
			this.penalty = penalty;
			this.timeslot = timeslot;
		}
		
		@Override 
		public int compareTo(ExamInfo ep){
			
			return -this.penalty.compareTo(ep.penalty);
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof ExamInfo) {
				if(((ExamInfo)o).id==this.id)
					return true;
			}
			return false;
			
		}
	}
	
	public Data data;
	
	/**
     * List of exams for each slot.
     */
    public ArrayList<ArrayList<Integer>> timeSlots;
    
    // Data structures need to find feasible solution(s)
	/**
     * List of tuple conflicting exams.
     */    
    public ArrayList<ArrayList<Tuple>> timeSlotsConflict;
 
	/**
     * List of timeslot (id) which contain at least a conflict.
     */  
    public Map<Integer, Integer> timeSlotWithConflicts;
    
	/**
     * Total number of conflicts.
     */ 
    public int conflictNumber;
    // End data structures need to find feasible solution(s)
    
    
    // Data structures need optimality initialized
    // when evaluatePenalty() method is called.
    /**
     * Total penalty calculated according to definition
     * already given.
     */
    public int penalty;
    
    /**
     * Penalty associated to exams.
     */
    public Map<Integer,ExamInfo> examInfos;
    public Map<Integer,SlotInfo> slotInfos;
    public PriorityQueue<ExamInfo> orderedExamInfos;
    public PriorityQueue<SlotInfo> orderedSlotInfos;
    // End data structures need optimality initialized

    /**
     * Timetable constructor.
     * */
	public Timetable(Data data) {
		super();
		
		this.data = data;
		timeSlots = new ArrayList<ArrayList<Integer>>();
		
		for(int i=0;i<data.slotsNumber;i++) 
			timeSlots.add(new ArrayList<Integer>());
		
		timeSlotsConflict = new ArrayList<ArrayList<Tuple>>();
		
		for(int i=0;i<data.slotsNumber;i++) 
			timeSlotsConflict.add(new ArrayList<Tuple>());
		
		timeSlotWithConflicts = new TreeMap<Integer,Integer>();
		conflictNumber = 0;
		
		// For second part
		examInfos = new HashMap<Integer,ExamInfo>();
		slotInfos = new HashMap<Integer,SlotInfo>();
		orderedExamInfos = new PriorityQueue<ExamInfo>();
		orderedSlotInfos = new PriorityQueue<SlotInfo>();
		
		penalty = 0;
	}

	/**
	 * Add an exam to a specified timeslot updating tied data structures.
	 * */
	public void addExam(int timeslot, int idExam) {
		timeSlots.get(timeslot).add(idExam);
		List<Integer> slot = timeSlots.get(timeslot);
		for(int ei=0;ei<slot.size();ei++) {
			
			if(slot.get(ei)==idExam) continue;
			
			if(this.data.conflictExams[idExam][slot.get(ei)]!=0) {
				this.conflictNumber++;
				
				Tuple conflict = new Tuple(slot.get(ei),idExam);
				this.timeSlotsConflict.get(timeslot).add(conflict);
				
				this.timeSlotWithConflicts.put(timeslot, timeslot);
			}
		}
	}
	
	/**
	 * Switch two timeslots of timetable.
	 * To call only and only if timetable is feasible. 
	 * */
	public void switchSlots(int s1, int s2) {
		ArrayList<Integer> temp = this.timeSlots.get(s1);
		this.timeSlots.set(s1, this.timeSlots.get(s2));
		this.timeSlots.set(s2, temp);
		
		this.evaluatesPenalty();
	}
	
	/**
	 * Evaluates total number of conflicts after applying specified move.
	 * */
    public int evaluatesSwitch(int examSelected, int timeslotSource, int timeslotDestination) {
    	
    	int currentLocalConflict=0;
    	int potentialLocalConflict =0;
    	
    	for(Iterator<Tuple> it=this.timeSlotsConflict.get(timeslotSource).iterator();it.hasNext();) {
    		Tuple conflict = it.next();
    		if(conflict.e1==examSelected || conflict.e2==examSelected)
    			currentLocalConflict++;
    	}
    	
    	for(Iterator<Integer> it=this.timeSlots.get(timeslotDestination).iterator();it.hasNext();) {
    		Integer idExam = it.next();
    		if(this.data.conflictExams[idExam][examSelected]!=0)
    			potentialLocalConflict++;
    	}
    	
    	return this.conflictNumber-currentLocalConflict+potentialLocalConflict;
    }
    
	/**
	 * Applies the specified move.
	 * */
    public void doSwitch(int examSelected, int timeslotSource, int timeslotDestination) {
    	
    	int currentLocalConflict=0;
    	
    	ArrayList<Tuple> newConflicts = new ArrayList<Tuple>();
    	
    	for(Iterator<Tuple> it=this.timeSlotsConflict.get(timeslotSource).iterator();it.hasNext();) {
    		Tuple conflict = it.next();
    		if(conflict.e1==examSelected || conflict.e2==examSelected)
    			currentLocalConflict++;
    		else
    			newConflicts.add(conflict);
    	}
    	
    	int i;
    	for(i=0;this.timeSlots.get(timeslotSource).get(i)!=examSelected;i++) ;

    	this.timeSlots.get(timeslotSource).remove(i);
    	this.timeSlotsConflict.set(timeslotSource,newConflicts);
    	
    	if(this.timeSlotsConflict.get(timeslotSource).size()>0)
    		this.timeSlotWithConflicts.put(timeslotSource, timeslotSource);
    	else
    		this.timeSlotWithConflicts.remove(timeslotSource);
    	
    	this.conflictNumber = this.conflictNumber-currentLocalConflict;
    	this.addExam(timeslotDestination, examSelected);
    	
    }

    /** 
     * Evaluates penalty value of current timetable 
     * according to the given definition.
     * */
	public void evaluatesPenalty() {

		//double objectiveFunction = 0;
		int e1, e2;
		ArrayList<Integer> slot1, slot2 = new ArrayList<>();

		for (int i = 0 ; i < timeSlots.size()-1; i++) {
			slot1 = timeSlots.get(i);
			for (int j = 0 ; j < slot1.size(); j++) {
				e1 = slot1.get(j);
				for (int k = i+1 ; k < timeSlots.size() && k < i+6; k++) {
					slot2 = timeSlots.get(k);
					for (int l = 0 ; l < slot2.size(); l++) {
						e2 = slot2.get(l);
						if (e1 != e2) {
							if (data.conflictExams[e1][e2] > 0) {
								
								double partialPenalty = Math.pow(2, (5 - (k-i))) * data.conflictExams[e1][e2];
								
								this.penalty += partialPenalty;
								
								ExamInfo ei1 = null;
								ExamInfo ei2 = null;
								SlotInfo si1 = null;
								SlotInfo si2 = null;
								
								if((ei1=this.examInfos.get(e1))==null) {
									ei1 = new ExamInfo(e1,0,i);
									this.examInfos.put(e1, ei1);
									this.orderedExamInfos.add(ei1);
								}
								if((ei2=this.examInfos.get(e2))==null) {
									ei2 = new ExamInfo(e2,0,k);
									this.examInfos.put(e2, ei2);
									this.orderedExamInfos.add(ei2);
								}
								
								ei1.penalty += partialPenalty;
								ei2.penalty += partialPenalty;								
							
								if((si1=this.slotInfos.get(i))==null) {
									si1 = new SlotInfo(i);
									this.slotInfos.put(i, si1);
									this.orderedSlotInfos.add(si1);
								}
								if((si2=this.slotInfos.get(k))==null) {
									si2 = new SlotInfo(k);
									this.slotInfos.put(k, si2);
									this.orderedSlotInfos.add(si2);
								}
								
								if(!si1.orderedExamInfo.contains(ei1))
									si1.addExamInfo(ei1);
								if(!si2.orderedExamInfo.contains(ei2))
									si2.addExamInfo(ei2);
							}
						}
					}
				}
			}
		}

		//return objectiveFunction / data.studentsNumber;
	}
	
    @Override
    public String toString() {
    	StringBuffer out = new StringBuffer();
    	int s =0;
    	for(Iterator<ArrayList<Integer>> its=timeSlots.iterator();its.hasNext();s++) {
    		ArrayList<Integer> slot = its.next();
    		out.append("Slot "+s+": ");
    		
    		for(Iterator<Integer> ite=slot.iterator();ite.hasNext();)
    			out.append(ite.next()+", ");
    		
    		out.append("\n");
    		
    	}
    	
    	out.append("\n");
    	out.append("Conflicts:"+this.conflictNumber);
    	return out.toString();
    }

	@Override
	public int compareTo(Timetable timetable) {
		
		if(this.penalty>timetable.penalty)
			return 1;
		if(this.penalty<timetable.penalty)
			return -1;
		return 0;
	}
    
}
