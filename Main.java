import java.io.FileNotFoundException;

public class Main {

	public static boolean debug = true;
	public static String filename;
    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
            ReaderWriter rw = new ReaderWriter();
            Data data = rw.readInputFiles(args[0]);
            FeasibleConstructor fb = new FeasibleConstructor();
            GreatDeluge gd = new GreatDeluge();
            filename = args[0];

            Timetable timetable = fb.makeFeasibleGraphColoringWithTabu(data, null, 10);
            timetable.setPenality();
            Timetable bestTimetable = gd.greatDeluge(timetable,300000, startTime);
            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Feasable: "+ timetable.feasibilityChecker());
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("OF Last TT after SA: " + bestTimetable.objFunc / data.studentsNumber);
            bestTimetable.setPenality();
            System.out.println("OF with set: " + bestTimetable.objFunc / data.studentsNumber);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}