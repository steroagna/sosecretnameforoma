import java.io.FileNotFoundException;

public class Main {

	public static boolean debug = true;
    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
            ReaderWriter rw = new ReaderWriter();
            Data data = rw.readInputFiles(args[0]);
            ILS ils = new ILS();
            int neighborNumberFeasibleConstructor = 10;
            int neighborLS = 120;

            FeasibleConstructor.FeasibleConstructorThread fb = new FeasibleConstructor.FeasibleConstructorThread(data, 0, neighborNumberFeasibleConstructor, neighborLS);
            Timetable timetable = fb.makeFeasibleGraphColoringWithTabu(data, null, neighborNumberFeasibleConstructor);
            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Feasible created in time: " + elapsedTime);
            Util.setPenality(timetable, data);
            System.out.println("OF with set: " + timetable.objFunc);
            timetable.objFunc = Util.ofCalculator(timetable, data);
            System.out.println("OF with ofCalc: " + timetable.objFunc);
            Timetable bestTimetable = ils.iteratedLocalSearch(timetable, data, 30, 30000);
            elapsedTime = System.currentTimeMillis() - startTime;
//            System.out.println(timetable.toString(args[0]));
            System.out.println("Feasable: "+ Util.feasibilityChecker(timetable, data));
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("OF Last TT after SA: " + bestTimetable.objFunc);
//            System.out.println("OF Last TT after SA: " + timetable.objFunc);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}