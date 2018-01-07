import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.concurrent.ThreadLocalRandom;

public class Timetable implements Cloneable {

	public Data data;
	public HashMap<Integer, Integer> positions;
	public ArrayList<ArrayList<Integer>> timeSlots;
	public ArrayList<Double> penaltyTimeSlots;
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
	public Double objFunc;

	public Timetable(Data data) {
		super();
		int i,k = data.slotsNumber;
		this.data = data;
		this.timeSlots = new ArrayList<>();
		for(i=0;i<k;i++)
			this.timeSlots.add(new ArrayList<Integer>());
		this.timeSlotsConflict = new ArrayList<>();
		for(i=0;i<k;i++)
			this.timeSlotsConflict.add(new ArrayList<Tuple>());

		this.penaltyTimeSlots = new ArrayList<>();
		this.examMoved = new TreeMap<>();
		this.positions = new HashMap<>();
		this.conflictNumber = 0;
		this.objFunc = Double.MAX_VALUE;
	}

	public Timetable(Timetable o) {
		int i,j;
		this.data = o.data;
		this.timeSlots = new ArrayList<>();
		this.timeSlotsConflict = new ArrayList<>();
		for(i=0;i<o.timeSlots.size();i++) {
			this.timeSlots.add(new ArrayList<Integer>());
			for(j= 0; j < o.timeSlots.get(i).size(); j++)
				this.timeSlots.get(i).add(o.timeSlots.get(i).get(j));
		}
		this.timeSlotsConflict = new ArrayList<>();
		for(i=0;i<o.timeSlotsConflict.size();i++) {
			this.timeSlotsConflict.add(new ArrayList<Tuple>());
			for(j= 0; j < o.timeSlotsConflict.get(i).size(); j++)
				this.timeSlotsConflict.get(i).add(o.timeSlotsConflict.get(i).get(j));
		}
		this.penaltyTimeSlots = new ArrayList<>();
		this.penaltyTimeSlots.addAll(o.penaltyTimeSlots);
		this.positions = new HashMap<>();
		this.positions.putAll(o.positions);
		this.examMoved = new TreeMap<>();
		this.examMoved.putAll(o.examMoved);
		this.conflictNumber = o.conflictNumber;
		this.objFunc = new Double(o.objFunc);
	}

	public void addExam(int timeslot, int idExam) {

		int ei;
		Tuple conflict;
		timeSlots.get(timeslot).add(idExam);
		positions.put(idExam, timeslot);

		List<Integer> slot = timeSlots.get(timeslot);
		for (ei = 0; ei < slot.size(); ei++) {

			if (slot.get(ei) == idExam) continue;

			if (data.conflictExams[idExam][slot.get(ei)] != 0) {
				conflict = new Tuple(slot.get(ei), idExam);
				this.timeSlotsConflict.get(timeslot).add(conflict);
				this.conflictNumber++;
			}
		}
	}

	public void removeExam(int exam) {
		int timeslot = this.positions.get(exam), i = 0;
		if (this.timeSlots.get(timeslot).contains(exam)) {
			this.timeSlots.get(timeslot).remove((Integer) exam);
			this.positions.remove(exam);
		}
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

    public int evaluatesSwitch(int examSelected, int timeslotSource, int timeslotDestination) {
		Tuple conflict;
		Integer idExam;
		int currentLocalConflict=0;
    	int potentialLocalConflict =0;
		for(Iterator<Tuple> it=this.timeSlotsConflict.get(timeslotSource).iterator();it.hasNext();) {
    		conflict = it.next();
    		if(conflict.e1==examSelected || conflict.e2==examSelected)
    			currentLocalConflict++;
    	}
		for(Iterator<Integer> it=this.timeSlots.get(timeslotDestination).iterator();it.hasNext();) {
    		idExam = it.next();
    		if(data.conflictExams[idExam][examSelected]!=0)
    			potentialLocalConflict++;
    	}
		return this.conflictNumber-currentLocalConflict+potentialLocalConflict;
    }

    public void doSwitch(int examSelected, int timeslotSource, int timeslotDestination) {
		int currentLocalConflict=0, i;
		Tuple conflict;
		ArrayList<Tuple> newConflicts = new ArrayList<Tuple>();
		for(Iterator<Tuple> it=this.timeSlotsConflict.get(timeslotSource).iterator();it.hasNext();) {
    		conflict = it.next();
    		if(conflict.e1==examSelected || conflict.e2==examSelected)
    			currentLocalConflict++;
    		else
    			newConflicts.add(conflict);
    	}
		for(i=0;this.timeSlots.get(timeslotSource).get(i)!=examSelected;i++);
		this.timeSlots.get(timeslotSource).remove(i);
    	this.timeSlotsConflict.set(timeslotSource,newConflicts);
		this.conflictNumber = this.conflictNumber-currentLocalConflict;
    	this.addExam(timeslotDestination, examSelected);
    }

	public void moveExamWithoutConflicts(Move move) {
		int examSelected = move.idExam, timeslotDestination = move.destinationTimeSlot;
		this.removeExam(examSelected);
		this.addExam(timeslotDestination,examSelected);
		this.objFunc = move.penalty;
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
	}

	public Move generatesNeighbourMovingExamWithKempe() {
		int timeslotSource, timeslotDestination, conflictSelected, examSelected, conflictNumber;
		Move moving = null;

		for(;;) {
			timeslotSource = ThreadLocalRandom.current().nextInt(timeSlots.size());
			timeslotDestination = ThreadLocalRandom.current().nextInt(timeSlots.size());
			if(timeslotSource==timeslotDestination ||
					timeSlots.get(timeslotSource).size()==0)
				continue;
			conflictSelected = ThreadLocalRandom.current().nextInt(timeSlots.get(timeslotSource).size());
			examSelected = timeSlots.get(timeslotSource).get(conflictSelected);
			conflictNumber = evaluatesSwitch(examSelected,timeslotSource,timeslotDestination);
			if (conflictNumber == 0) {
				moving = new Move(examSelected, timeslotSource, timeslotDestination);
				moving.penalty = objFunc + evaluateOF(examSelected, timeslotDestination);
			}
			else
				kempeMove(timeslotSource, timeslotDestination, examSelected);
			break;
		}
		return moving;
	}

	public Swap generatesNeighbourSwappingExam() {
		int timeslotSource, timeslotDestination, conflictSelected, examSelected, conflictNumber;
		Swap swap;
		int examSelected2 = 0;

		for(;;) {
			timeslotSource = ThreadLocalRandom.current().nextInt(timeSlots.size());
			timeslotDestination = ThreadLocalRandom.current().nextInt(timeSlots.size());
			if(timeslotSource==timeslotDestination ||
					timeSlots.get(timeslotSource).size()==0)
				continue;
			conflictSelected = ThreadLocalRandom.current().nextInt(timeSlots.get(timeslotSource).size());
			examSelected = timeSlots.get(timeslotSource).get(conflictSelected);
			conflictNumber = evaluatesSwitch(examSelected,timeslotSource,timeslotDestination);
			if (conflictNumber == 1) {
				for(Iterator<Integer> it = timeSlots.get(timeslotDestination).iterator(); it.hasNext();) {
					examSelected2 = it.next();
					if(data.conflictExams[examSelected][examSelected2]!=0)
						break;
				}
				conflictNumber = evaluatesSwitch(examSelected2, timeslotDestination, timeslotSource);
				if (conflictNumber == 1) {
					swap = new Swap(this, timeslotSource, timeslotDestination, examSelected, examSelected2);
					break;
				}
			} else
				continue;
		}
		return swap;
	}

	public Timetable kempeChain(int k) {
		int randomSlot1, randomSlot2, j, exam, randomExam;
		boolean[] visited;
		Timetable tempTimetable;
		Timetable bestTimetable = new Timetable(this);
		bestTimetable.objFunc = Double.MAX_VALUE;

		tempTimetable = new Timetable(this);
		visited = new boolean[timeSlots.size()]; // todo creare lista invece di array in cui inserire solo i non visited
		randomSlot1 = 0;
		randomSlot2 = 0;
		for (j = 0; j < k; j++) {
			while (randomSlot1 == randomSlot2 ) {
				if (visited[randomSlot1] && !visited[randomSlot2]) {
					randomSlot1 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
				} else if (visited[randomSlot2] && !visited[randomSlot1]) {
					randomSlot2 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
				} else if (visited[randomSlot2] && !visited[randomSlot1]) {
					randomSlot2 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
				} else {
					randomSlot1 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
					randomSlot2 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
				}
			}
			visited[randomSlot1] = true;
			visited[randomSlot2] = true;
			if (tempTimetable.timeSlots.get(randomSlot1).size() == 0 )
				continue;
			randomExam = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.get(randomSlot1).size());
			exam = tempTimetable.timeSlots.get(randomSlot1).get(randomExam);
			tempTimetable.kempeMove(randomSlot1, randomSlot2, exam);
			if (tempTimetable.objFunc < bestTimetable.objFunc)
				bestTimetable = new Timetable(tempTimetable);
		}

		return bestTimetable;
	}

	public void kempeMove(int slot1, int slot2, int exam) {

		int departureSlot, arrivalSlot, i = 0, exam2;
		ArrayList<Integer> examsMoved = new ArrayList<>();

		updateOF(exam, positions.get(exam), false);
		removeExam(exam);
		addExam(slot2,exam);
		updateOF(exam, positions.get(exam), true);
		examsMoved.add(exam);

		while (conflictNumber > 0) {
			if ( i%2 == 0 ) {
				departureSlot = slot2;
				arrivalSlot = slot1;
			}
			else {
				departureSlot = slot1;
				arrivalSlot = slot2;
			}

			while (timeSlotsConflict.get(departureSlot).size() != 0) {
				Tuple tupla = timeSlotsConflict.get(departureSlot).get(0);
				if (examsMoved.contains(tupla.e1)) {
					exam2 = tupla.e2;
				}
				else
					exam2 = tupla.e1;

				examsMoved.add(exam2);
				updateOF(exam2, positions.get(exam2), false);
				removeExam(exam2);
				addExam(arrivalSlot,exam2);
				updateOF(exam2, arrivalSlot, true);
			}
			i++;
		}

	}

	public int manyMovesWorstExams(double percentage) {
		int numberOfMove = (int) (data.examsNumber * percentage), count = 0;
		Move move;
		int examSelected, timeslotSource, timeslotDestination, i;
		TreeMap<Integer, Double> miniExamMoved = new TreeMap<>();

		System.out.println("Tryng to move: " + numberOfMove + " exams" );
		for (i = 0; i < numberOfMove; i++) {
			examSelected = 1 + ThreadLocalRandom.current().nextInt(data.examsNumber);
			miniExamMoved.put(
					examSelected,
					examMoved.get(examSelected)
			);
		}

		@SuppressWarnings("rawtypes")
		Map sortedMap = sortByValues(miniExamMoved);
		Set set = sortedMap.entrySet();
		Iterator it = set.iterator();

		while (it.hasNext()) {
			Map.Entry me = (Map.Entry) it.next();
			examSelected = (int) me.getKey();
			timeslotSource = positions.get(examSelected);
			timeslotDestination = ThreadLocalRandom.current().nextInt(timeSlots.size());
			if (timeslotDestination == timeslotSource)
				continue;
			int conflictNumber = evaluatesSwitch(examSelected, timeslotSource, timeslotDestination);
			if (conflictNumber == 0) {
				move = new Move(examSelected, timeslotSource, timeslotDestination);
				move.penalty = objFunc + evaluateOF(examSelected, timeslotDestination);
				moveExamWithoutConflicts(move);
				count++;
			}
//			else
//				kempeMove(timeslotSource, timeslotDestination, examSelected);
		}
		return count;
	}

	public void swapTimeslot() {
		int t1 = ThreadLocalRandom.current().nextInt(timeSlots.size()),
				t2 = ThreadLocalRandom.current().nextInt(timeSlots.size()), pow, size = timeSlots.size(),
				max, min, e1, timeslotStart, timeslotEnd, i;
		while (t1 == t2) {
			t1 = ThreadLocalRandom.current().nextInt(timeSlots.size());
			t2 = ThreadLocalRandom.current().nextInt(timeSlots.size());
		}
		ArrayList<Integer> slot1 = timeSlots.get(t1), slot2 = timeSlots.get(t2),
				temp = new ArrayList<>();

		while (slot1.size() > 0) {
			e1 = slot1.get(0);
			temp.add(e1);
			removeExam(e1);
		}
		while (slot2.size() > 0) {
			e1 = slot2.get(0);
			removeExam(e1);
			addExam(t1, e1);
		}
		for (i = 0; i < temp.size(); i++) {
			e1 = temp.get(i);
			addExam(t2, e1);
		}

		if ( t2 > t1 ) {
			max = t2;
			min = t1;
		} else {
			max = t1;
			min = t2;
		}

		pow = max - min;
		if (min < 5) {
			timeslotStart = 0;
		}
		else {
			timeslotStart = min - 5;
		}
		if (max + 5 > size - 1) {
			timeslotEnd = size - 1;
		}
		else {
			timeslotEnd = max + 5;
		}

		if (pow <= 5 || min + 5 >= max - 5 ) {
			for(i = timeslotStart; i < timeslotEnd ; i++)
				setPenaltyTimeslot(i);
		} else {
			for(i = timeslotStart; i < min + 5 ; i++)
				setPenaltyTimeslot(i);
			for(i = max - 5 ; i < timeslotEnd ; i++)
				setPenaltyTimeslot(i);
		}
	}

    public String toString(String filename) {
		StringBuffer out = new StringBuffer();
    	int s = 0, slotNumber, e;

    	try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename + "_OMAMZ_group02.sol"))) {
    		for(Iterator<ArrayList<Integer>> its=timeSlots.iterator();its.hasNext();s++) {
				slotNumber = s+1;
				ArrayList<Integer> slot = its.next();

				out.append("Slot "+s+": ");

				for(Iterator<Integer> ite=slot.iterator();ite.hasNext();) {
					e = ite.next();
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
		double objectiveFunctionExam, objectiveFunctionSlot = 0;
		ArrayList<Integer> slot;
		int timeslotStart, timeslotEnd, size = this.timeSlots.size(),e1,i,j;
		this.objFunc = 0.0;

		for (i = 0 ; i < size; i++) {
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
			for (j = 0 ; j < slot.size(); j++) {
				e1 = slot.get(j);
				objectiveFunctionExam = calculatePenalty(e1, i, timeslotStart, timeslotEnd);
				this.objFunc += objectiveFunctionExam;
				objectiveFunctionSlot += objectiveFunctionExam;
			}
			penaltyTimeSlots.add(objectiveFunctionSlot/2);
			objectiveFunctionSlot = 0;
		}
		repopulateMovedExam();
		this.objFunc = this.objFunc / 2;
	}

	public void setPenaltyTimeslot(int timeslot) {
		int timeslotStart, timeslotEnd, size = timeSlots.size(), e1,j;
		ArrayList<Integer> slot;
		double objectiveFunctionSlot = 0;
		double penalty;

		if (timeslot < 5) {
			timeslotStart = 0;
		}
		else {
			timeslotStart = timeslot - 5;
		}
		if (timeslot + 5 > size - 1) {
			timeslotEnd = size - 1;
		}
		else {
			timeslotEnd = timeslot + 5;
		}

		slot = this.timeSlots.get(timeslot);

		for (j = 0 ; j < slot.size(); j++) {
			e1 = slot.get(j);
			penalty = calculatePenalty(e1, timeslot, timeslotStart, timeslotEnd);
			objectiveFunctionSlot += penalty;
		}
		objectiveFunctionSlot = objectiveFunctionSlot/2;
		penaltyTimeSlots.set(timeslot, objectiveFunctionSlot);
		objFunc += objectiveFunctionSlot;
	}

    public double evaluateOF(int e1, int timeslotDest) {
    	int timeslotStart, timeslotEnd, size = timeSlots.size(),
				timeslotSource = positions.get(e1), timeslotStartSource, timeslotEndSource;

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

		double objectiveFunctionExamAdd = calculatePenalty(e1, timeslotDest, timeslotStart, timeslotEnd);

		if (timeslotSource == -1)
			return objectiveFunctionExamAdd;

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

		double objectiveFunctionExamRemove = calculatePenalty(e1, timeslotSource, timeslotStartSource, timeslotEndSource);
		return objectiveFunctionExamAdd - objectiveFunctionExamRemove;
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

		double objectiveFunctionExamAdd = calculatePenalty(e1, timeslotDestExam1, timeslotStart, timeslotEnd);
		double objectiveFunctionExamRemove = calculatePenalty(e1, timeslotSource, timeslotStartSource, timeslotEndSource);
		double objectiveFunctionExamAdd2 = calculatePenalty(e2, timeslotDestExam2, timeslotStart2, timeslotEnd2);
		double objectiveFunctionExamRemove2 = calculatePenalty(e2, timeslotSource2, timeslotStartSource2, timeslotEndSource2);

		if (timeslotDestExam1 > timeslotDestExam2)
			pow = timeslotDestExam1 - timeslotDestExam2;
		else
			pow = timeslotDestExam2 - timeslotDestExam1;

		if (pow <= 5)
			penaltyE1E2 = Math.pow(2, (5 - pow)) * data.conflictExams[e1][e2];

		return objFunc - objectiveFunctionExamRemove + objectiveFunctionExamAdd - objectiveFunctionExamRemove2 + objectiveFunctionExamAdd2 + 2*penaltyE1E2;
	}

	private double calculatePenalty(int e1, int timeslotDest, int timeslotStart, int timeslotEnd) {

		ArrayList<Integer> slot;
		double objectiveFunctionExam = 0;
		int e2, pow, l;

		for (int k = timeslotStart; k <= timeslotEnd; k++) {
			if (k == timeslotDest) {
				continue;
			}
			slot = timeSlots.get(k);
			for (l = 0; l < slot.size(); l++) {
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
		examMoved.replace(e1, objectiveFunctionExam);

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

		objectiveFunctionExam = calculatePenalty(e1, timeslotDest, timeslotStart, timeslotEnd);

		if (insert)
			objFunc += objectiveFunctionExam;
		else
			objFunc -= objectiveFunctionExam;
	}

	public boolean feasibilityChecker() {
		int examCounter = 0, e1, e2, i, j;
		boolean feasible = true;
		ArrayList<Integer> slot;

		if (this.timeSlots.size() > data.slotsNumber) {
			feasible = false;
			System.out.println("Too many timeslots used");
		}

		int[] coveredExams = new int[data.examsNumber+1];

		for (i = 0 ; i < this.timeSlots.size() && feasible ; i++) {
			slot = this.timeSlots.get(i);
			for (j = 0 ; j < slot.size() && feasible; j++) {
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

		if (feasible == true && examCounter < data.examsNumber) {
			feasible = false;
			System.out.println("Exam Inserted: " + examCounter + " Exam Number form Data: " + data.examsNumber);
		}

		for(i = 1; i<=data.examsNumber;i++)
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
