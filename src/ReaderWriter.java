import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class ReaderWriter {
	
	/**
	 * Exam
	 */
	static Exam exam;
	
	/**
	 * Exam Id, Number of Students Enrolled
	 */
	static int examId, numberOfStudentsPerExam;
	
	/**
     * Stack of conflicts (STUD/EXAM)
     */
    static ArrayList<ArrayList<Integer>> conflicts = new ArrayList<>();
	
    /**
     * Slot list of Exams
     */
    static ArrayList<Integer> slot = new ArrayList<>();
    
	public Data readInputFiles(String filename) throws FileNotFoundException 
    {

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
            numberOfStudentsPerExam = scanner.nextInt();
            exam = new Exam(examId, numberOfStudentsPerExam);
            data.addExam(exam);
            data.examsNumber++;
        }
        scanner.close();

        /**
         * Matrix of conflicts (EXAM/EXAM)
         */
        int[][] conflictExamsZero = new int[data.examsNumber+1][data.examsNumber+1];
        
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
        data.studentsNumber = stud;
        scanner.close();
        
        for(i = 0; i < conflicts.size() ; i++) {
        	for(j = 0; j < conflicts.get(i).size(); j++) {
        		int e1 = conflicts.get(i).get(j);
        		for(k = 0; k < conflicts.get(i).size(); k++) {
        			int e2 = conflicts.get(i).get(k);
        			if (e1 != e2) {
        				data.conflictExams[e1][e2]++;
        				if (data.conflictExams[e1][e2] == 1)
        					data.totalConflicts++;
        			}
        		}
        	}
        }

        return data;
    }
}
