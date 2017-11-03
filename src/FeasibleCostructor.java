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
}
