import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;

        Data data = new Data();
        ReaderWriter rw = new ReaderWriter();
        
        data = rw.readInputFiles(args[0]);
            
        List<Timetable> timetables = FeasibleConstructor.generatesFeasibleTimetables(data,50);
            
        elapsedTime = (System.currentTimeMillis() - startTime);
        System.out.println("Elapsed: "+elapsedTime+" ms");
        
        //System.out.println(timetable.toString());
        for(Iterator<Timetable> it= timetables.iterator();it.hasNext();) {
        	Timetable t = it.next();
        	System.out.println("Feasable: "+ Util.feasibilityChecker(t, data));
         }
         
        GeneticOptimizer optimizer  = new GeneticOptimizer(timetables);
        Timetable best = optimizer.startOptimizer(1000);
        
        System.out.println("Best: "+ (double)best.penalty/best.data.studentsNumber);
    }
}