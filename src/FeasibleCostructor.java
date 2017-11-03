import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

public class FeasibleCostructor {

	/**
	 * Exam
	 */
	private Exam exam;
	
	/**
	 * Exam Id, Number of Students Enrolled
	 */
	private int examId, examIdSlot;
	
	/**
	 * Flag for conflict in graph coloring
	 */
	private boolean conflictFound;
	
	/**
	 * Array to sort based on degree of Graph coloring 
	 */
	private ArrayList<Integer> colored = new ArrayList<>();
	
	/**
	 * number of Vertex for each node
	 */
	private int degreeCounter = 0;
	
	/**
	 * Ordered Exam List based on Vertex number
	 */
	private TreeSet<Integer> treeMapExams = new TreeSet<>();

	public Data makeFeasibleGraphColoring(Data data) {
    	
        int i,j;
        for (i = 1; i < data.examsNumber+1; i++) {
            for (j = 1; j < data.examsNumber+1; j++)
            	if (data.conflictExams[i][j] > 0)
            		degreeCounter++;
            exam = data.examsMap.get(i);
            exam.setConnectedExamsNumber(degreeCounter);
            treeMapExams.add(i);
            degreeCounter = 0;
        }
        
        i = 0;
        while (!treeMapExams.isEmpty()) {
        	examId = treeMapExams.pollFirst();
        	while (i >= data.timeSlots.size() && !treeMapExams.isEmpty())
				data.timeSlots.add(new ArrayList<Integer>());
            for (j = 1; j < data.examsNumber+1; j++) {
            	if (data.conflictExams[examId][j] == 0) {
            		if(!colored.contains(j)) {
            			
            			Iterator<Integer> iterator = data.timeSlots.get(i).iterator();
            			conflictFound = false;
            			while(iterator.hasNext() && !conflictFound) {
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
        
        return data;
    }
}
