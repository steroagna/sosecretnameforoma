import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {

    /**
     * Slot value
     */
    static int examsNumber = 0;

    /**
     * Slot value
     */
    static int slotsNumber;

    /**
     * List of Exams for each slot
     */
    ArrayList<ArrayList<Exam>> slots = new ArrayList<>();

    /**
     * Matrix of conflicts (STUD/EXAM)
     */
    static List<Integer> conflicts = new ArrayList<>();

    /**
     * Matrix of conflicts (STUD/EXAM)
     */
    static HashMap<Integer, Exam> Exams = new HashMap<>();

    /**
     * Objective function value
     */
    static int objFunc = Integer.MAX_VALUE;


    public static void main(String[] args) {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
            readInputFiles(args[0]);
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
    private static void readInputFiles(String filename) throws FileNotFoundException {

        File examsFile    = new File(filename.concat(".exm"));
        File slotsFile    = new File(filename.concat(".slo"));
        File studentsFile = new File(filename.concat(".stu"));

        Scanner scanner  = new Scanner(slotsFile);
        slotsNumber = scanner.nextInt();
        scanner.close();

        scanner  = new Scanner(examsFile);
        while (scanner.hasNextLine() && scanner.hasNext()) {
            int examId = scanner.nextInt();
            int numberOfStudents = scanner.nextInt();
            Exam exam = new Exam(examId, numberOfStudents);
            Exams.put(examId, exam);
            examsNumber++;
        }
        scanner.close();
        System.out.println(examsNumber);
        /**
         * Matrix of conflicts (EXAM/EXAM)
         */
        int[][] examsGraph = new int[examsNumber][examsNumber];

        scanner  = new Scanner(studentsFile);
        String  stud, actualStud = null;
        int exam = 0;
        while (scanner.hasNextLine() && scanner.hasNext()) {
            stud = scanner.next();
            if (actualStud != null && !conflicts.isEmpty()) {
                if (!actualStud.equals(stud)) {
                    System.out.println(exam);
                    examsGraph[exam-1][exam-1] = 0;
                }
            }
            actualStud = stud;
            exam = scanner.nextInt();
            conflicts.add(exam);
        }
        scanner.close();
        return;
    }

    private static void writeOutput(long time) {

        System.out.println("Elaborazione dati in " + time + " millisec");

    }
}