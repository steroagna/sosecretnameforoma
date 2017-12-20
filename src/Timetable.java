import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.concurrent.ThreadLocalRandom;

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

	public void doSwap(Swap swap) {
		int exam1 = swap.m1.idExam;
		int exam2 = swap.m2.idExam;
		int timeslotDestination1 = swap.m1.destinationTimeSlot;
		int timeslotDestination2 = swap.m2.destinationTimeSlot;

		this.removeExam(exam1);
		this.removeExam(exam2);
		this.addExam(timeslotDestination1,exam1);
		this.addExam(timeslotDestination2,exam2);
		this.objFunc = swap.penalty;
		this.examMoved.remove(exam1);
		this.examMoved.remove(exam2);
	}

	public int perturbation() {
		int numberOfMove = data.examsNumber/10, count = 0;
		Move move[] = new Move[numberOfMove], bestMove[] = new Move[numberOfMove];
		int examSelected, timeslotSource, timeslotDestination, i;
		boolean moved = false;

		for (i = 0; i < numberOfMove; i++) {
			bestMove[i] = new Move(0,0,0);
			bestMove[i].penalty = Double.MAX_VALUE;
		}

		Map sortedMap = sortByValues(examMoved);
		Set set = sortedMap.entrySet();
		Iterator it = set.iterator();

		i = 0;
		while (it.hasNext() && i < numberOfMove) {
			if (moved)
				i++;
			Map.Entry me = (Map.Entry) it.next();
			examSelected = (int) me.getKey();
			timeslotSource = positions.get(examSelected);
			moved = false;
			for (timeslotDestination = 0; timeslotDestination < timeSlots.size() && i < numberOfMove; timeslotDestination++) {
				if (timeslotDestination == timeslotSource)
					continue;
				move[i] = new Move(examSelected, timeslotSource, timeslotDestination);
				int conflictNumber = evaluatesSwitch(examSelected, timeslotSource, timeslotDestination);
				if (conflictNumber > 0) {
					continue;
				} else {
					move[i].penalty = evaluateOF(examSelected, timeslotDestination);
					if (move[i].penalty < bestMove[i].penalty && ThreadLocalRandom.current().nextDouble() < 0.85) {
						bestMove[i].idExam = move[i].idExam;
						bestMove[i].destinationTimeSlot = move[i].destinationTimeSlot;
						bestMove[i].sourceTimeSlot = move[i].sourceTimeSlot;
						bestMove[i].penalty = move[i].penalty;
						moved = true;
					}
				}
				if (!moved)
					move[i] = null;
			}
		}
		for (i = 0; i < numberOfMove; i++) {
			if (move[i] != null) {
				int conflictNumber = evaluatesSwitch(bestMove[i].idExam, bestMove[i].sourceTimeSlot, bestMove[i].destinationTimeSlot);
				if (conflictNumber > 0) {
					continue;
				}
				bestMove[i].penalty = evaluateOF(bestMove[i].idExam, bestMove[i].destinationTimeSlot);
				doSwitchExamWithoutConflicts(bestMove[i]);
				count++;
			} else
				break;
		}
		return count;
	}

	public void perturbation2() {
		int t1 = ThreadLocalRandom.current().nextInt(timeSlots.size());
		int t2 = ThreadLocalRandom.current().nextInt(timeSlots.size());
		ArrayList<Integer> slot1 = timeSlots.get(t1), slot2 = timeSlots.get(t2),
				temp1 = new ArrayList<>(), temp2 = new ArrayList<>();

		while (slot1.size() > 0) {
			temp1.add(slot1.get(0));
			removeExam(slot1.get(0));
		}
		while (slot2.size() > 0) {
			temp2.add(slot2.get(0));
			removeExam(slot2.get(0));
		}

		for (int i = 0; i < temp1.size(); i++) {
			addExam(t2, temp1.get(i));
		}
		for (int i = 0; i < temp2.size(); i++) {
			addExam(t1, temp2.get(i));
		}

		setPenality();
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

	/**
	 * evaluate OF adding destination penalty and subtracting origin penalty
	 * @param e1
	 * @param timeslotDest
	 * @return
	 */
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

	public double evaluateOFSwap(int e1, int e2, int timeslotDestExam1, int timeslotDestExam2) {
		int timeslotStart, timeslotEnd, timeslotStart2, timeslotEnd2, size = timeSlots.size(), pow,
				timeslotSource = positions.get(e1), timeslotStartSource, timeslotEndSource,
				timeslotSource2 = positions.get(e2), timeslotStartSource2, timeslotEndSource2;
		double penaltyE1E2 = 0;

		if (timeslotDestExam1 < 5) {
			timeslotStart = 0;
		} else {
			timeslotStart = timeslotDestExam1 - 5;
		}

		if (timeslotDestExam1 + 5 > size - 1) {
			timeslotEnd = size - 1;
		} else {
			timeslotEnd = timeslotDestExam1 + 5;
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

		if (timeslotDestExam2 < 5) {
			timeslotStart2 = 0;
		} else {
			timeslotStart2 = timeslotDestExam2 - 5;
		}

		if (timeslotDestExam2 + 5 > size - 1) {
			timeslotEnd2 = size - 1;
		} else {
			timeslotEnd2 = timeslotDestExam2 + 5;
		}

		if (timeslotSource2 < 5) {
			timeslotStartSource2 = 0;
		} else {
			timeslotStartSource2 = timeslotSource2 - 5;
		}

		if (timeslotSource2 + 5 > size - 1) {
			timeslotEndSource2 = size - 1;
		} else {
			timeslotEndSource2 = timeslotSource2 + 5;
		}

		double objectiveFunctionExamAdd = calculatePenalty(e1, timeslotDestExam1, timeslotStart, timeslotEnd, false);
		double objectiveFunctionExamRemove = calculatePenalty(e1, timeslotSource, timeslotStartSource, timeslotEndSource, false);
		double objectiveFunctionExamAdd2 = calculatePenalty(e2, timeslotDestExam2, timeslotStart2, timeslotEnd2, false);
		double objectiveFunctionExamRemove2 = calculatePenalty(e2, timeslotSource2, timeslotStartSource2, timeslotEndSource2, false);

		if (timeslotDestExam1 > timeslotDestExam2)
			pow = timeslotDestExam1 - timeslotDestExam2;
		else
			pow = timeslotDestExam2 - timeslotDestExam1;

		if (pow <= 5)
			penaltyE1E2 = Math.pow(2, (5 - pow)) * data.conflictExams[e1][e2];

		return objFunc - objectiveFunctionExamRemove + objectiveFunctionExamAdd - objectiveFunctionExamRemove2 + objectiveFunctionExamAdd2 + 2*penaltyE1E2;
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

	public boolean feasibilityChecker() {

		int examCounter = 0, e1, e2;
		boolean feasible = true;
		ArrayList<Integer> slot;

		if (this.timeSlots.size() > data.slotsNumber) {
			feasible = false;
			System.out.println("Too many timeslots used");
		}

		int[] coveredExams = new int[data.examsNumber+1];

		for (int i = 0 ; i < this.timeSlots.size() && feasible ; i++) {
			slot = this.timeSlots.get(i);
			for (int j = 0 ; j < slot.size() && feasible; j++) {
				e1 = slot.get(j);
				coveredExams[e1]=+1;
				examCounter++;
				for (int k = j+1 ; k < slot.size() && feasible; k++) {
					e2 = slot.get(k);
					if (e1 != e2) {
						if (data.conflictExams[e1][e2] > 0) {
							feasible = false;
							System.out.println("Conflict: " + e1 + " - " + e2);
						}
					}
				}
			}
		}

		if ( feasible == true && examCounter < data.examsNumber) {
			feasible = false;
			System.out.println("Exam Inserted: " + examCounter + " Exam Number form Data: " + data.examsNumber);
		}

		for(int i =1; i<=data.examsNumber;i++)
			if(coveredExams[i]==0||coveredExams[i]>1) {
				if(feasible==true)
					System.out.println("Uncovered/Duplicated exams: ");
				feasible= false;
				System.out.println(""+i+", ");
			}

		if (feasible == false)
			return false;
		else
			return true;
	}
}
