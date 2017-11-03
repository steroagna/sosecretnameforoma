import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.*;

public class FeasibleCostructor {

	/**
	 * Exam
	 */
	static Exam exam;
	
	/**
	 * Exam Id, Number of Students Enrolled
	 */
	static int examId, examIdSlot;
	
	/**
	 * Flag for conflict in graph coloring
	 */
	static boolean conflictFound;
	
	/**
	 * Array to sort based on degree of Graph coloring 
	 */
	static ArrayList<Integer> colored = new ArrayList<>();
	
	/**
	 * number of Vertex for each node
	 */
	static int degreeCounter = 0;
	
	/**
	 * Ordered Exam List based on Vertex number
	 */
	static TreeSet<Integer> treeMapExams = new TreeSet<>(new Comparator<Integer>() {
		@Override
		public int compare(Integer o1, Integer o2) {
			return o2.compareTo(o1);
		}
	});

	/**
	 * Ordered Exam List based on Vertex number
	 */
	static TreeSet<Integer> treeMapExamsRandom = new TreeSet<>(new Comparator<Integer>() {
		@Override
		public int compare(Integer o1, Integer o2) {
			if (Math.random() < 0.5)
				return -1;
			else
				return 1;
		}
	});

	public static Data makeFeasibleGraphColoring(Data data) {
    	
        int i,j;
        for (i = 1; i < data.examsNumber+1; i++) {
            for (j = 1; j < data.examsNumber+1; j++)
            	if (data.conflictExams[i][j] > 0)
            		degreeCounter++;
            exam = data.examsList.get(i-1);
            exam.setConnectedExamsNumber(degreeCounter);
            treeMapExams.add(i);
            degreeCounter = 0;
        }
        
        i = 0;
        while (!treeMapExams.isEmpty()) {
        	examId = treeMapExams.pollFirst();
        	if (!colored.contains(examId)) {
				while (i >= data.timeSlots.size())
					data.timeSlots.add(new ArrayList<Integer>());
				data.timeSlots.get(i).add(examId);
				colored.add(examId);
				for (j = 1; j < data.examsNumber + 1; j++) {
					if (data.conflictExams[examId][j] == 0) {
						if (!colored.contains(j)) {

							Iterator<Integer> iterator = data.timeSlots.get(i).iterator();
							conflictFound = false;
							while (iterator.hasNext() && !conflictFound) {
								examIdSlot = iterator.next();
								if (data.conflictExams[examIdSlot][j] > 0)
									conflictFound = true;
							}

							if (!conflictFound) {
//            				data.timeSlots.get(i).add(data.examsMap.get(j));
								data.timeSlots.get(i).add(j);
								colored.add(j);
							}
						}
					}
				}
				if (!data.timeSlots.get(i).isEmpty())
					i++;
			}
        }
        
        return data;
    }

	public static Data makeFeasibleGraphGreedy(Data data) {

		int tempSlot, colorCounter, colorCounterMin, i, j;
		boolean conflict;
		for ( i = 0; i < data.examsList.size() ; i++ ) {
			treeMapExamsRandom.add(data.examsList.get(i).getId());
		}

		examId = treeMapExamsRandom.pollFirst();
		data.timeSlots.add(new ArrayList<>());
		data.timeSlots.get(0).add(examId);

		while (!treeMapExamsRandom.isEmpty()) {
			examId = treeMapExamsRandom.pollFirst();
			colorCounterMin = Integer.MAX_VALUE;
			tempSlot = -1;
			for ( i = 0; i < data.timeSlots.size(); i++ ) {
				conflict = false;
				colorCounter = 0;
				for ( j = 0; j < data.timeSlots.get(i).size() && !conflict; j++ ) {
					colorCounter++;
					if ( data.conflictExams[examId][data.timeSlots.get(i).get(j)] > 0 )
						conflict = true;
				}
				if (colorCounter < colorCounterMin && !conflict) {
					colorCounterMin = colorCounter;
					tempSlot = i;
				}
			}
			if (tempSlot == -1 && data.timeSlots.size() < data.timeSlotsNumber) {
				tempSlot = i;
				data.timeSlots.add(new ArrayList<>());
			}
			if (tempSlot == -1)
				tempSlot = (int) Math.floor(Math.random() * (data.timeSlotsNumber));
			data.timeSlots.get(tempSlot).add(examId);
		}

		return data;
	}

}
