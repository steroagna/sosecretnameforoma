import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {

	/**
	 * Problem data
	 **/
	static Data data;
	
	/**
	 * Exam
	 */
	static Exam exam;
	
	/**
	 * Exam Id, Number of Students Enrolled
	 */
	static int examId, examIdSlot, numberOfStudents;
	
	/**
	 * Flag for conflict in graph coloring
	 */
	static boolean conflictFound;
	
	/**
     * Stack of conflicts (STUD/EXAM)
     */
    static ArrayList<ArrayList<Integer>> conflicts = new ArrayList<>();
    
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
	static TreeSet<Exam> treeMapExams = new TreeSet<>(new ExamComparatorDegree());

    /**
     * Slot list of Exams
     */
    static ArrayList<Exam> slot = new ArrayList<>();

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
        	Data data = new Data();
            data = readInputFiles(args[0]);
            makeFeasible(data);
            elapsedTime = (System.currentTimeMillis() - startTime);
            writeOutput(elapsedTime, data);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static Data readInputFiles(String filename) throws FileNotFoundException {

    	int i,j,k;
        File examsFile    	= new File(filename.concat(".exm"));
        File slotsFile    	= new File(filename.concat(".slo"));
        File studentsFile 	= new File(filename.concat(".stu"));
        Data data 			= new Data();

        /**
         * Read slot number
         */
        Scanner scanner  = new Scanner(slotsFile);
        data.slotsNumber = scanner.nextInt();
        scanner.close();
		
        /**
		 * Read exams
		 */
        scanner = new Scanner(examsFile);
        while (scanner.hasNextLine() && scanner.hasNext()) {
            examId = scanner.nextInt();
            numberOfStudents = scanner.nextInt();
            exam = new Exam(examId, numberOfStudents);
            data.addExam(exam);
            data.examsNumber++;
        }
        scanner.close();
        data.examsNumber++;
        /**
         * Matrix of conflicts (EXAM/EXAM)
         */
        int[][] conflictExamsZero = new int[data.examsNumber+1][data.examsNumber+1];
        for (i = 1; i < data.examsNumber; i++)
            for (j = 1; j < data.examsNumber; j++)
                conflictExamsZero[i][j] = 0;
        
        data.conflictExams = conflictExamsZero;
        
        /**
         * Reading students file --> conflicts
         */
        scanner  = new Scanner(studentsFile);
        String[] student;
        int  stud = 0;
        examId = 0;
        while (scanner.hasNextLine() && scanner.hasNext()) {
            student = scanner.next().split("s");
    		stud = Integer.parseInt(student[1]);
            examId = scanner.nextInt();
            while (stud >= conflicts.size()) {
            	conflicts.add(new ArrayList<Integer>());
            }
            conflicts.get(stud).add(examId);
        }
        scanner.close();

//        int[][] studentExams = new int[conflicts.size()][data.examsNumber];
//        for (i = 1; i <= conflicts.size(); i++)
//            for (j = 0; j < data.examsNumber; j++)
//            	studentExams[i][j] = 0;
        
        for(i = 1; i < conflicts.size() ; i++) {
        	for(j = 1; j < conflicts.get(i).size(); j++) {
        		int e1 = conflicts.get(i).get(j);
        		for(k = 1; k < conflicts.get(i).size(); k++) {
        			int e2 = conflicts.get(i).get(k);
        			if (e1 != e2)
        				data.conflictExams[e1][e2]++;
        		}
        	}
        }
        
        return data;
        }
        
        private static void makeFeasible(Data data) {
        	
        int i,j;
        for (i = 1; i < data.examsNumber; i++) {
            for (j = 1; j < data.examsNumber; j++)
            	if (data.conflictExams[i][j] > 0)
            		degreeCounter++;
            exam = data.examsMap.get(i);
            exam.setConnectedExamsNumber(degreeCounter);
            treeMapExams.add(exam);
            degreeCounter = 0;
        }
        
        i = 0;
        while (!treeMapExams.isEmpty()) {
            examId = treeMapExams.pollFirst().getId();
            for (j = 1; j < data.examsNumber; j++)
            	if (data.conflictExams[examId][j] == 0)
            		if(!colored.contains(j)) {
            			while (i >= data.timeSlots.size())
            				data.timeSlots.add(new ArrayList<Exam>());
            			
            			Iterator<Exam> iterator = data.timeSlots.get(i).iterator();
            			conflictFound = false;
            			while(iterator.hasNext() && !conflictFound) {
            				examIdSlot = iterator.next().getId();
            				if (data.conflictExams[examIdSlot][j] > 0)
            					conflictFound = true;
            			}
	            			
            			if (!conflictFound) {
            				data.timeSlots.get(i).add(data.examsMap.get(j));
		        			colored.add(j);
            			}
            		}
            i++;
        }
        
        
//        while (!treeMapExams.isEmpty()) {
//              System.out.print(treeMapExams.pollFirst().getId() + " ");
//                System.out.print(treeMapExams.pollFirst().getId() + " ");
//            System.out.println();
//        }
        return;
    }

    private static void writeOutput(long time, Data data) {
    	int i;
    	/**
		   * Print Graph
		   */
		for (i = 1; i < data.examsNumber; i++) {
			System.out.print("Line " + i + ": ");
			for (int j = 1; j < data.examsNumber; j++) 
				System.out.print(data.conflictExams[i][j] + " ");
			System.out.println();
			
		}
		
		/**
		 * Print Slots
		 */
		i = 0; 
		while(i < data.timeSlots.size()) {
			if (!(slot = data.timeSlots.get(i)).isEmpty()) {
				System.out.print("Slot " + i + ": ");
				slot.stream().forEach(e -> System.out.print(e.getId() + " "));
				System.out.println();
			}
			i++;
		}
		
		System.out.println("Elaborazione dati in " + time + " millisec");
    }
}