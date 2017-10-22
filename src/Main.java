import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
public class Main {

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
	 * Array to sort based on degree of Graph coloring 
	 */
	static ArrayList<Integer> colored = new ArrayList<>();
	
	/**
	 * number of Vertex for each node
	 */
	static int degreeCounter = 0;
	
	/**
	 * 
	 */
	static TreeSet<Exam> treeMapExams = new TreeSet<>(new 1111111111paratorDegree());
	
    /**
     * Slot value
     */
    static int examsNumber = 0;

    /**
     * Slot list of Exams
     */
    static ArrayList<Exam> slot = new ArrayList<>();
    
    /**
     * Slot value
     */
    static int slotsNumber;

    /**
     * List of Exams for each slot
     */
    static ArrayList<ArrayList<Exam>> slots = new ArrayList<ArrayList<Exam>>();

    /**
     * Matrix of conflicts (STUD/EXAM)
     */
    static Stack<Integer> conflicts = new Stack<>();

    /**
     * Matrix of conflicts (STUD/EXAM)
     */
    static HashMap<Integer, Exam> exams = new HashMap<>();

    /**
     * Objective function value
     */
    static int objFunc = Integer.MAX_VALUE;


    public static void main(String[] args) {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
            readInputFilesAndCreateFeasible(args[0]);
            elapsedTime = (System.currentTimeMillis() - startTime);
            writeOutput(elapsedTime);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static void readInputFilesAndCreateFeasible(String filename) throws FileNotFoundException {

        File examsFile    = new File(filename.concat(".exm"));
        File slotsFile    = new File(filename.concat(".slo"));
        File studentsFile = new File(filename.concat(".stu"));

        Scanner scanner  = new Scanner(slotsFile);
        slotsNumber = scanner.nextInt();
        scanner.close();

        scanner  = new Scanner(examsFile);
        while (scanner.hasNextLine() && scanner.hasNext()) {
            examId = scanner.nextInt();
            numberOfStudents = scanner.nextInt();
            exam = new Exam(examId, numberOfStudents);
            exams.put(examId, exam);
            examsNumber++;
        }
        scanner.close();

        /**
         * Matrix of conflicts (EXAM/EXAM)
         */
        int[][] examsGraph = new int[examsNumber][examsNumber];
        int i,j;
        for (i = 0; i < examsNumber; i++)
            for (j = 0; j < examsNumber; j++)
                examsGraph[i][j] = 0;

        /**
         * Reading students file
         */
        scanner  = new Scanner(studentsFile);
        String  stud, actualStud = null;
        examId = 0;
        while (scanner.hasNextLine() && scanner.hasNext()) {
            stud = scanner.next();
            if (actualStud != null && !conflicts.isEmpty()) {
                if (!actualStud.equals(stud)) {
                    while (!conflicts.empty()) {
                        examId = conflicts.pop();
                        for(Integer examTemp : conflicts) {
                            examsGraph[examId-1][examTemp-1]++;
                            examsGraph[examTemp-1][examId-1]++;
                        }
                    }
                }
            }
            actualStud = stud;
            examId = scanner.nextInt();
            conflicts.push(examId);
        }
        scanner.close();

        /**
         * Print Graph
         */
        for (i = 0; i < examsNumber; i++) {
            for (j = 0; j < examsNumber; j++)
                System.out.print(examsGraph[i][j] + " ");
            System.out.println();
        }
        
        
        for (i = 0; i < examsNumber; i++) {
            for (j = 0; j < examsNumber; j++)
            	if (examsGraph[i][j] > 0)
            		degreeCounter++;
            exam = exams.get(i+1);
            exam.setConnectedExamsNumber(degreeCounter);
            treeMapExams.add(exam);
            degreeCounter = 0;
        }
        
        i = 1;
        while (!treeMapExams.isEmpty()) {
            examId = treeMapExams.pollFirst().getId();
            for (j = 0; j < examsNumber; j++)
            	if (examsGraph[examId-1][j] > 0)
            		if(!colored.contains(j+1)) {
            			while (i >= slots.size())
            				slots.add(new ArrayList<Exam>());
            			
            			Iterator<Exam> iterator = slots.get(i-1).iterator();
            			conflictFound = false;
            			while(iterator.hasNext() && !conflictFound) {
            				examIdSlot = iterator.next().getId();
            				if (examsGraph[examIdSlot+1][j] > 0)
            					conflictFound = true;
            			}
	            			
            			if (!conflictFound) {
		        			slots.get(i-1).add(exams.get(j+1));
		        			colored.add(j+1);
            			}
            		}
            i++;
        }
        
        i = 0; 
        while(!(slot = slots.get(i)).isEmpty()) {
        	i++;
        	System.out.print("Slot " + i + ": ");
        	slot.stream().forEach(e -> System.out.print(e.getId() + " "));
        	System.out.println();
        }
        
//        while (!treeMapExams.isEmpty()) {
//              System.out.print(treeMapExams.pollFirst().getId() + " ");
//                System.out.print(treeMapExams.pollFirst().getId() + " ");
//            System.out.println();
//        }
        return;
    }

    private static void writeOutput(long time) {
        System.out.println("Elaborazione dati in " + time + " millisec");
    }
}