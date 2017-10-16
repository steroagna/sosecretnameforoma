import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

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
    static int[][] conflicts;

    /**
     * Matrix of conflicts (EXAM/EXAM)
     */
    static int[][] examsGraph;

    /**
     * Objective function value
     */
    static int objFunc = Integer.MAX_VALUE;


    public static void main(String[] args) {

        long startTime = System.currentTimeMillis(), elapsedTime;
        FileWriter writer = null;

        try {
            readInputFiles(args[1]);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        elapsedTime = (System.currentTimeMillis() - startTime);
        try {
			writeOutput(writer, elapsedTime);
			writer.close();
        } catch (IOException e) {
			e.printStackTrace();
		}
    }

    @SuppressWarnings("unused")
    private static void readInputFiles(String filename) throws FileNotFoundException {

        File examsFile    = new File(filename.concat(".exm"));
        File slotsFile    = new File(filename.concat(".slo"));
        File studentsFile = new File(filename.concat(".stu"));

        Scanner scanner  = new Scanner(slotsFile);
        slotsNumber = scanner.nextInt();

        scanner  = new Scanner(studentsFile);
        while (scanner.hasNextLine()) {
            int stud = scanner.nextInt();
            int exam = scanner.nextInt();
            conflicts[stud][exam] = 1;
        }

        scanner  = new Scanner(examsFile);

    }

    private static void writeOutput(FileWriter writer, long time) {

        System.out.println(time);

    }
}