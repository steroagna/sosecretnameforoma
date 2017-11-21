import java.io.FileNotFoundException;

public class Main {

    public static boolean debug = true;
    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
            ReaderWriter rw = new ReaderWriter();
        	FeasibleCostructor fb = new FeasibleCostructor();
            TabuSearchPenalty ts = new TabuSearchPenalty();
        	Data data = rw.readInputFiles(args[0]);

            Timetable timetable = fb.makeFeasibleGraphColoringWithTabu(data);
            timetable.objFunc = Tools.ofCalculator(timetable, data);

            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Feasable: "+ Util.feasibilityChecker(timetable, data));
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("OF? " + Tools.ofCalculator(timetable, data));

            timetable = ts.TabuSearch(timetable, data);

            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println(timetable.toString(args[0]));
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("OF? " + Tools.ofCalculator(timetable, data));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}