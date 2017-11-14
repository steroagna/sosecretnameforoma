import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

public class Timetable {
	
	public int[][] G;
	
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
		timeSlots = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<k;i++) 
			timeSlots.add(new ArrayList<Integer>());
		timeSlotsConflict = new ArrayList<ArrayList<Tuple>>();
		for(int i=0;i<k;i++) 
			timeSlotsConflict.add(new ArrayList<Tuple>());
		
		this.conflictNumber = 0;
		
	}

	/**
	 * Add and exam to a specified timeslot updating tied data structures.
	 * */
	public void addExam(int timeslot, int idExam) {
		timeSlots.get(timeslot).add(idExam);
		List<Integer> slot = timeSlots.get(timeslot);
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
    	
    	this.conflictNumber = this.conflictNumber-currentLocalConflict;
    	this.addExam(timeslotDestination, examSelected);
    	
    }
    

    public String toString(String filename) {

    	StringBuffer out = new StringBuffer();
    	int s =0;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
			for(Iterator<ArrayList<Integer>> its=timeSlots.iterator();its.hasNext();s++) {
				int slotNumber = s+1;
				ArrayList<Integer> slot = its.next();

				out.append("Slot "+s+": ");

				for(Iterator<Integer> ite=slot.iterator();ite.hasNext();) {
					int e = ite.next();
					out.append( e + ", ");
					String content = e + " " + slotNumber;
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
    
}
