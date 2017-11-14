import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
        	
        	Data data = new Data();
        	ReaderWriter rw = new ReaderWriter();
        	FeasibleCostructor fb = new FeasibleCostructor();
        	
        	data = rw.readInputFiles(args[0]);
        	
            Timetable timetable = fb.makeFeasibleGraphColoringWithTabu(data);
            
            elapsedTime = (System.currentTimeMillis() - startTime);

            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println(timetable.toString());
            System.out.println("Feasable: "+ Util.feasibilityChecker(timetable, data));
            System.out.println("Elapsed time: " + elapsedTime);
            //rw.writeOutput(elapsedTime, data, args[0]);
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}