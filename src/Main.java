import java.io.FileNotFoundException;

public class Main {

	public static boolean debug = true;
    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
            ReaderWriter rw = new ReaderWriter();
            Data data = rw.readInputFiles(args[0]);
            FeasibleCostructor fb = new FeasibleCostructor(data);
            HEA hea = new HEA();
            int populationSize = 20;
        	
            Population population = fb.makeFeasiblePopulation(data, populationSize);

            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Population created in time: " + elapsedTime);
            
            Timetable timetable = hea.heuristic(population, data);

            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println(timetable.toString(args[0]));
            System.out.println("Feasable: "+ Util.feasibilityChecker(timetable, data));
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("OF? " + Tools.ofCalculator(timetable, data));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}