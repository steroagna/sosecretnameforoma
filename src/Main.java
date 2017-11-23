import java.io.FileNotFoundException;

public class Main {

	public static boolean debug = false;
    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
            ReaderWriter rw = new ReaderWriter();
            Data data = rw.readInputFiles(args[0]);
            FeasibleConstructor fb = new FeasibleConstructor();
            TabuSearchPenalty localSearch = new TabuSearchPenalty();
            HEA hea = new HEA();
            
            //Parametri prima parte
            int populationSize = 10; //generati ognuno con un thread
            long timerFeasibleConstructorLS = 10000;
            int neighborNumberFeasibleConstructor = 10;
            
            //Parametri seconda parte
            long timerHEADuration = 120000;
            long timerNewGenLS = 2000;
            int neighborLS = 120;
            int threadNumber = 15; // Generano ognuno una nuova generazione
            
            Population population = fb.makeFeasiblePopulation(data, populationSize, timerFeasibleConstructorLS, neighborNumberFeasibleConstructor, neighborLS);

            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Population created in time: " + elapsedTime);

            Timetable timetable = hea.parallelHeuristic(population, data, timerHEADuration, timerNewGenLS, neighborNumberFeasibleConstructor, neighborLS, threadNumber);
            Timetable lastbestTimetable = localSearch.TabuSearch(timetable, data, neighborLS, 10000);
    		
            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println(timetable.toString(args[0]));
            System.out.println("Feasable: "+ Util.feasibilityChecker(timetable, data));
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("OF Last TT after LS: " + lastbestTimetable.objFunc);
       
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}