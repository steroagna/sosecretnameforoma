import com.sun.org.apache.bcel.internal.generic.POP;

import java.io.FileNotFoundException;

public class Main {

	public static boolean debug = true;
	public static String filename;
    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
            ReaderWriter rw = new ReaderWriter();
            Data data = rw.readInputFiles(args[0]);
            ILS ils = new ILS();
            filename = args[0];
            FeasibleConstructor fb = new FeasibleConstructor();
            //Parametri prima parte
            int populationSize = 10; //generati ognuno con un thread
            int neighborNumberFeasibleConstructor = 10;
            int neighborLS = 120;


            Population population = fb.makeFeasiblePopulation(data, populationSize, neighborNumberFeasibleConstructor, neighborLS);

            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Population created in time: " + elapsedTime);
            System.out.println("Feasible created in time: " + elapsedTime);

            Timetable bestTimetable = ils.ILSWithThread(population, data, 120000, startTime);

            elapsedTime = System.currentTimeMillis() - startTime;
//            System.out.println(bestTimetable.toString(args[0]));
            System.out.println("Feasable: "+ bestTimetable.feasibilityChecker());
            System.out.println("OF Last TT after ILS: " + bestTimetable.objFunc / data.studentsNumber);
            bestTimetable.setPenality();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}