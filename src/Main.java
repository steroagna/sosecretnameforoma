import java.io.FileNotFoundException;

public class Main {

	public static boolean debug = false;
    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
            ReaderWriter rw = new ReaderWriter();
            Data data = rw.readInputFiles(args[0]);
            FeasibleConstructor fb = new FeasibleConstructor(data);
            HEA hea = new HEA();
            int populationSize = 10;
            int threadNumber = 10;

            Population population = fb.makeFeasiblePopulation(data, populationSize);

            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Population created in time: " + elapsedTime);

            Timetable timetable = hea.parallelHeuristic(population, data, threadNumber);

            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println(timetable.toString(args[0]));
            System.out.println("Feasable: "+ Util.feasibilityChecker(timetable, data));
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("OF? " + Util.ofCalculator(timetable, data));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}