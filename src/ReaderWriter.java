import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
    
	public static Data readInputFiles(String filename) throws FileNotFoundException
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
        data.timeSlotsNumber = scanner.nextInt();
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
        
        data.setConflicts(conflicts);
        
     // Matrix of students/exams
//        int[][] studentExams = new int[conflicts.size()][data.examsNumber];
//        	for (i = 0; i < conflicts.size(); i++)
//        		for (j = 0; j < data.examsNumber; j++)
//        			studentExams[i][j] = 0;
//          
//        data.setStudentExams(studentExams);
        
        for(i = 0; i < conflicts.size() ; i++) {
        	for(j = 0; j < conflicts.get(i).size(); j++) {
        		int e1 = conflicts.get(i).get(j);
        		for(k = 0; k < conflicts.get(i).size(); k++) {
        			int e2 = conflicts.get(i).get(k);
        			if (e1 != e2)
        				data.conflictExams[e1][e2]++;
        		}
        	}
        }
        
        return data;
    }

    public static void writeOutput(long time, Data data) {
    	Tools tools = new Tools();
    	int i, j, e;
    	
    	/**
		   * Print Graph
		   */
		for (i = 1; i <= data.examsNumber; i++) {
			System.out.print("Line " + i + ": ");
			for (j = 1; j <= data.examsNumber; j++)
				System.out.print(data.conflictExams[i][j] + " ");
			System.out.println();

		}

		/**
		 * Print Slots
		 */
		i = 0;
		while(i < data.timeSlots.size()) {
//			if (!(slot = data.timeSlots.get(i)).isEmpty()) {
			slot = data.timeSlots.get(i);
			System.out.print("Slot " + i + ": ");
			for (j = 0 ; j < slot.size() ; j ++) {
				e = slot.get(j);
				System.out.print(e + " ");
			}
			System.out.println();
//			}
			i++;
		}

		System.out.println("Feasible? " + tools.feasibilityChecker(data));
		System.out.println("Used timeslots: " + data.timeSlots.size());
		System.out.println("OF? " + data.objFunc);

		System.out.println("Elaborazione dati in " + time + " millisec");
	}

	public static void writeOutputToFile(Data data, String filename) {

		int i = 0, j, e;
		/**
		 * Print sol file
		 */
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename + "_OMAMZ_group02.sol"))) {
			while(i < data.timeSlots.size()) {
				slot = data.timeSlots.get(i);
				for (j = 0 ; j < slot.size() ; j ++) {
					e = slot.get(j);
					String content = e + " " + i + "\n";
					bw.write(content);
				}
				i++;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
