import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;

        Data data = new Data();
        ReaderWriter rw = new ReaderWriter();
        
        data = rw.readInputFiles(args[0]);
            
        List<Timetable> timetables = FeasibleCostructor.generatesFeasibleTimetables(data,10);
            
        elapsedTime = (System.currentTimeMillis() - startTime);
           
        //System.out.println(timetable.toString());
        for(Iterator<Timetable> it= timetables.iterator();it.hasNext();)
         	System.out.println("Feasable: "+ Util.feasibilityChecker(it.next(), data));
        
        System.out.println("Elapsed: "+elapsedTime+" ms");

    }
}