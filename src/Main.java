import java.io.FileNotFoundException;

public class Main {

	public static boolean debug = true;
    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
            ReaderWriter rw = new ReaderWriter();
            Data data = rw.readInputFiles(args[0]);
            TabuSearchPenalty localSearch = new TabuSearchPenalty();
            HEA hea = new HEA();
            SimulatedAnnealing sa = new SimulatedAnnealing();

            //Parametri prima parte
            int populationSize = 20; //generati ognuno con un thread
            long timerFeasibleConstructorLS = 500;
            int neighborNumberFeasibleConstructor = 10;

            //Parametri seconda parte
            long timerHEADuration = 30000;
            long timerNewGenLS = 5000;
            int neighborLS = 120;
            int threadNumber = 30; // Generano ognuno una nuova generazione

            FeasibleConstructor.FeasibleConstructorThread fb = new FeasibleConstructor.FeasibleConstructorThread(data, 0, neighborNumberFeasibleConstructor, neighborLS);
            Timetable timetable = fb.makeFeasibleGraphColoringWithTabu(data, null, neighborNumberFeasibleConstructor) ;

//            FeasibleConstructor fb = new FeasibleConstructor();
//            Population population = fb.makeFeasiblePopulation(data, populationSize, timerFeasibleConstructorLS, neighborNumberFeasibleConstructor, neighborLS);

            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Population created in time: " + elapsedTime);

//            Timetable timetable = hea.parallelHeuristic(population, data, timerHEADuration, timerNewGenLS, neighborNumberFeasibleConstructor, neighborLS, threadNumber);

            timetable.objFunc = Util.ofCalculator(timetable, data);
            Timetable bestTimetable = sa.simulatedAnnealing(timetable, data, 30, 60000);

            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println(bestTimetable.toString(args[0]));
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