import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

public class Timetable implements Cloneable {
	
	public Data data;

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
	 * HashSet of exams not already moved by swap or kempe
	 */
	public TreeMap<Integer, Double> examMoved;

	public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
		Comparator<K> valueComparator = new Comparator<K>() {
			public int compare(K k1, K k2) {
				return map.get(k2).compareTo(map.get(k1));
			}
		};

		Map<K, V> sortedByValues =
				new TreeMap<K, V>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}

	/**
     * Total number of conflicts.
     */ 
    public int conflictNumber;

	/**
     * Objective function value ---> penalty to minimize
     */
    public Double objFunc;

	public Timetable(Data data) {
		super();
		int k = data.slotsNumber;
		this.data = data;
		this.timeSlots = new ArrayList<>();
		for(int i=0;i<k;i++)
			this.timeSlots.add(new ArrayList<Integer>());
		this.timeSlotsConflict = new ArrayList<>();
		for(int i=0;i<k;i++)
			this.timeSlotsConflict.add(new ArrayList<Tuple>());

		this.examMoved = new TreeMap<>();
		this.positions = new HashMap<>();
		this.conflictNumber = 0;
		this.objFunc = Double.MAX_VALUE;
	}

	public Timetable(Timetable o) {
		this.data = o.data;
		this.timeSlots = new ArrayList<>();
		this.timeSlotsConflict = new ArrayList<>();
		for(int i=0;i<o.timeSlots.size();i++) {
			this.timeSlots.add(new ArrayList<Integer>());
			for(int j= 0; j < o.timeSlots.get(i).size(); j++)
				this.timeSlots.get(i).add(o.timeSlots.get(i).get(j));
		}
		this.timeSlotsConflict = new ArrayList<>();
		for(int i=0;i<o.timeSlotsConflict.size();i++) {
			this.timeSlotsConflict.add(new ArrayList<Tuple>());
			for(int j= 0; j < o.timeSlotsConflict.get(i).size(); j++)
				this.timeSlotsConflict.get(i).add(o.timeSlotsConflict.get(i).get(j));
		}
		this.positions = new HashMap<>();
		this.positions.putAll(o.positions);
		this.examMoved = new TreeMap<>();
		this.examMoved.putAll(o.examMoved);
		this.conflictNumber = o.conflictNumber;
		this.objFunc = new Double(o.objFunc);
	}

	/**
	 * Add an exam to a specified timeslot updating tied data structures.
	 * */
	public void addExam(int timeslot, int idExam) {
		
		timeSlots.get(timeslot).add(idExam);
		positions.put(idExam, timeslot);

		List<Integer> slot = timeSlots.get(timeslot);
		for (int ei = 0; ei < slot.size(); ei++) {

			if (slot.get(ei) == idExam) continue;

			if (data.conflictExams[idExam][slot.get(ei)] != 0) {
				Tuple conflict = new Tuple(slot.get(ei), idExam);
				this.timeSlotsConflict.get(timeslot).add(conflict);
				this.conflictNumber++;
			}
		}
	}
	
	/**
	 * Remove an exam from its timeslot updating tied data structures.
	 * */
	public void removeExam(int exam) {
		
		int timeslot = this.positions.get(exam);

		if (this.timeSlots.get(timeslot).contains(exam)) {
			this.timeSlots.get(timeslot).remove((Integer) exam);
			this.positions.remove(exam);
		}

		int i = 0;
		while (i < this.timeSlotsConflict.get(timeslot).size()) {
			Tuple tupla = this.timeSlotsConflict.get(timeslot).get(i);
			if (tupla.e1 == exam || tupla.e2 == exam) {
				this.timeSlotsConflict.get(timeslot).remove(tupla);
				this.conflictNumber--;
			}
			else
				i++;
		}
	}

	public void repopulateMovedExam() {
		for(int i = 1; i <= data.examsNumber; i++)
			examMoved.put(i, 0.0);
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
    		if(data.conflictExams[idExam][examSelected]!=0)
    			potentialLocalConflict++;
    	}
    	
    	return this.conflictNumber-currentLocalConflict+potentialLocalConflict;
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
	public void doSwitchExamWithoutConflicts(Move move) {
		int examSelected = move.idExam, timeslotDestination = move.destinationTimeSlot;

		this.removeExam(examSelected);
		this.addExam(timeslotDestination,examSelected);
		this.objFunc = move.penalty;
		this.examMoved.remove(examSelected);
	}

	public void perturbation() {
		Move move = null, bestMove = new Move(0,0,0);
		int examSelected, timeslotSource, timeslotDestination;
		boolean moved = false;
		bestMove.penalty = Double.MAX_VALUE;

		Map sortedMap = sortByValues(examMoved);
		Set set = sortedMap.entrySet();
		Iterator it = set.iterator();

		while (it.hasNext() && !moved) {
			Map.Entry me = (Map.Entry) it.next();
			examSelected = (int) me.getKey();
			timeslotSource = positions.get(examSelected);
			for (timeslotDestination = 0; timeslotDestination < timeSlots.size(); timeslotDestination++) {
				move = new Move(examSelected, timeslotSource, timeslotDestination);
				int conflictNumber = evaluatesSwitch(examSelected, timeslotSource, timeslotDestination);
				if (conflictNumber > 0) {
					continue;
				} else {
					move.penalty = evaluateOF(examSelected, timeslotDestination);
					if (move.penalty < bestMove.penalty){
						bestMove.idExam = move.idExam;
						bestMove.destinationTimeSlot = move.destinationTimeSlot;
						bestMove.sourceTimeSlot = move.sourceTimeSlot;
						bestMove.penalty = move.penalty;
					}
					moved = true;
				}
			}
		}
		if (move != null) {
			doSwitchExamWithoutConflicts(bestMove);
		}
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
    
    public void setPenality() {

		double objectiveFunctionExam;
		int e1;
		ArrayList<Integer> slot;
		int timeslotStart, timeslotEnd, size = this.timeSlots.size();
		this.objFunc = 0.0;

		for (int i = 0 ; i < size; i++) {
			if (i < 5) {
				timeslotStart = 0;
			}
			else {
				timeslotStart = i - 5;
			}
			if (i + 5 > size - 1) {
				timeslotEnd = size - 1;
			}
			else {
				timeslotEnd = i + 5;
			}

			slot = this.timeSlots.get(i);
			for (int j = 0 ; j < slot.size(); j++) {
				e1 = slot.get(j);
				objectiveFunctionExam = calculatePenalty(e1, i, timeslotStart, timeslotEnd, true);
				this.objFunc += objectiveFunctionExam;
			}
		}
		this.objFunc = this.objFunc / 2;
	}
	
    public double evaluateOF(int e1, int timeslotDest) {
    	int timeslotStart, timeslotEnd, size = timeSlots.size();
		int timeslotSource = positions.get(e1), timeslotStartSource, timeslotEndSource;

		if (timeslotDest < 5) {
			timeslotStart = 0;
		} else {
			timeslotStart = timeslotDest - 5;
		}
		
		if (timeslotDest + 5 > size - 1) {
			timeslotEnd = size - 1;
		} else {
			timeslotEnd = timeslotDest + 5;
		}

		if (timeslotSource < 5) {
			timeslotStartSource = 0;
		} else {
			timeslotStartSource = timeslotSource - 5;
		}

		if (timeslotSource + 5 > size - 1) {
			timeslotEndSource = size - 1;
		} else {
			timeslotEndSource = timeslotSource + 5;
		}

		double objectiveFunctionExamAdd = calculatePenalty(e1, timeslotDest, timeslotStart, timeslotEnd, false);
		double objectiveFunctionExamRemove = calculatePenalty(e1, timeslotSource, timeslotStartSource, timeslotEndSource, false);

		return objFunc - objectiveFunctionExamRemove + objectiveFunctionExamAdd;
    }

	private double calculatePenalty(int e1, int timeslotDest, int timeslotStart, int timeslotEnd, boolean updateMoves) {

		ArrayList<Integer> slot;
		double objectiveFunctionExam = 0;
		int e2, pow;

		for (int k = timeslotStart; k <= timeslotEnd; k++) {
			if (k == timeslotDest) {
				continue;
			}
			slot = timeSlots.get(k);
			for (int l = 0; l < slot.size(); l++) {
				e2 = slot.get(l);
				if (e1 != e2) {
					if (data.conflictExams[e1][e2] > 0) {
						if (k < timeslotDest)
							pow = timeslotDest - k;
						else
							pow = k - timeslotDest;
						objectiveFunctionExam += Math.pow(2, (5 - pow)) * data.conflictExams[e1][e2];
					}
				}
			}
		}
		if (updateMoves)
			examMoved.put(e1, objectiveFunctionExam);

		return objectiveFunctionExam;
	}

	public void updateOF(int e1, int timeslotDest, boolean insert) {
		
		int timeslotStart, timeslotEnd, size = timeSlots.size();
		double objectiveFunctionExam;

		if (timeslotDest < 5) {
			timeslotStart = 0;
		} else {
			timeslotStart = timeslotDest - 5;
		}
		if (timeslotDest + 5 > size - 1) {
			timeslotEnd = size - 1;
		} else {
			timeslotEnd = timeslotDest + 5;
		}

		objectiveFunctionExam = calculatePenalty(e1, timeslotDest, timeslotStart, timeslotEnd, true);

		if (insert)
			objFunc += objectiveFunctionExam;
		else
			objFunc -= objectiveFunctionExam;
	}
}
