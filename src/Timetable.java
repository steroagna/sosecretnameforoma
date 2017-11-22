import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

public class Timetable implements Cloneable {
	
	public int[][] G;

	/**
	 * position of each exam
	 */
	public HashMap<Integer, Integer> positions;
	/**
     * List of exams for each slot.
     */
    public ArrayList<ArrayList<Integer>> timeSlots;
    
	/**
     * List of tuple conflicting exams.
     */    
    public ArrayList<ArrayList<Tuple>> timeSlotsConflict;
 
	/**
     * Total number of conflicts.
     */ 
    public int conflictNumber;

    /**
     * Objective function value ---> penalty to minimize
     */
    public double objFunc;

	public Timetable(int[][] G,int k) {
		super();
		this.G = G;
		this.timeSlots = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<k;i++)
			this.timeSlots.add(new ArrayList<Integer>());
		this.timeSlotsConflict = new ArrayList<ArrayList<Tuple>>();
		for(int i=0;i<k;i++)
			this.timeSlotsConflict.add(new ArrayList<Tuple>());

		this.positions = new HashMap<>();
		this.conflictNumber = 0;
		this.objFunc = Integer.MAX_VALUE;
	}

	public Timetable(Timetable o) {
		this.G = o.G.clone();
		this.timeSlots = (ArrayList<ArrayList<Integer>>) o.timeSlots.clone();
//		for(int i=0;i<o.timeSlots.size();i++)
//			this.timeSlots.add(new ArrayList<>());
//		for(int i=0;i<o.timeSlots.size();i++)
//			this.timeSlots.set(i, (ArrayList<Integer>)o.timeSlots.get(i).clone());
		this.timeSlotsConflict = (ArrayList<ArrayList<Tuple>>) o.timeSlotsConflict.clone();
//		this.timeSlotsConflict = new ArrayList<>();
//			this.timeSlotsConflict.add(o.timeSlotsConflict.get(i));
		this.positions = (HashMap)o.positions.clone();
		this.conflictNumber = o.conflictNumber;
		this.objFunc = o.objFunc;
	}
	
	/**
	 * Add and exam to a specified timeslot updating tied data structures.
	 * */
	public void addExam(int timeslot, int idExam) {
		timeSlots.get(timeslot).add(idExam);
		List<Integer> slot = timeSlots.get(timeslot);
		positions.put(idExam,timeslot);
		for(int ei=0;ei<slot.size();ei++) {
			
			if(slot.get(ei)==idExam) continue;
			
			if(G[idExam][slot.get(ei)]!=0) {
				this.conflictNumber++;
				
				Tuple conflict = new Tuple(slot.get(ei),idExam);
				this.timeSlotsConflict.get(timeslot).add(conflict);
			}
		}
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
    		if(G[idExam][examSelected]!=0)
    			potentialLocalConflict++;
    	}
    	
    	return this.conflictNumber-currentLocalConflict+potentialLocalConflict;
    }

	public double evaluatesSwitchWithoutConflicts(Data data, int examSelected, int timeslotSource, int timeslotDestination) {

    	double penalty;
    	
    	this.doSwitchExamWithoutConflicts(examSelected,timeslotSource,timeslotDestination);
    	penalty = Util.ofCalculator(this, data);
		this.doSwitchExamWithoutConflicts(examSelected,timeslotDestination,timeslotSource);

		return penalty;
	}

	public double evaluatesSwitchTimeSlots(Data data, int timeslotSource, int timeslotDestination) {

		double penalty;
		this.doSwitchTimeslot(timeslotSource,timeslotDestination);
		penalty = Util.ofCalculator(this, data);
		this.doSwitchTimeslot(timeslotDestination,timeslotSource);

		return penalty;
	}

	/**
	 * Applies the specified move.
	 * */
    public void doSwitch(int examSelected, int timeslotSource, int timeslotDestination, Data data) {
    	
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
    	for(i=0;this.timeSlots.get(timeslotSource).get(i)!=examSelected;i++);

    	this.timeSlots.get(timeslotSource).remove(i);
    	this.timeSlotsConflict.set(timeslotSource,newConflicts);
    	
    	this.conflictNumber = this.conflictNumber-currentLocalConflict;
    	this.addExam(timeslotDestination, examSelected);
    	
    }

	/**
	 * Applies the specified move.
	 * */
	public void doSwitchExamWithoutConflicts(int examSelected, int timeslotSource, int timeslotDestination) {

		timeSlots.get(timeslotSource).remove((Integer) examSelected);
		timeSlots.get(timeslotDestination).add(examSelected);

		return;
	}

	public void doSwitchTimeslot(int timeslotSource, int timeslotDestination) {

		ArrayList<Integer> temp = timeSlots.get(timeslotSource);
		ArrayList<Tuple> temp2 = timeSlotsConflict.get(timeslotSource);

		timeSlots.set(timeslotSource, timeSlots.get(timeslotDestination));
		timeSlotsConflict.set(timeslotSource, timeSlotsConflict.get(timeslotDestination));
		timeSlots.set(timeslotDestination, temp);
		timeSlotsConflict.set(timeslotDestination, temp2);

		return;
	}

    public String toString(String filename) {

    	StringBuffer out = new StringBuffer();
    	int s =0;
    	try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename + "_OMAMZ_group02.sol"))) {
    		for(Iterator<ArrayList<Integer>> its=timeSlots.iterator();its.hasNext();s++) {
				int slotNumber = s+1;
				ArrayList<Integer> slot = its.next();

				out.append("Slot "+s+": ");

				for(Iterator<Integer> ite=slot.iterator();ite.hasNext();) {
					int e = ite.next();
					out.append( e + ", ");
					String content = e + " " + slotNumber + "\n";
					bw.write(content);
				}

				out.append("\n");
			}
		} catch (IOException ex) {

			ex.printStackTrace();

		}
    	
    	out.append("\n");
    	out.append("Conflicts:"+this.conflictNumber);
    	return out.toString();
    }

	public void removeExam(int exam) {
		int timeslot = this.positions.get(exam);
		this.timeSlots.get(timeslot).remove((Integer) exam);
		this.positions.remove(exam);
	}
}
