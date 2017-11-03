import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
        	Data data = new Data();
        	ReaderWriter rw = new ReaderWriter();
        	FeasibleCostructor fb = new FeasibleCostructor();
        	Tools tools = new Tools();
            data = rw.readInputFiles(args[0]);
            data = fb.makeFeasibleGraphColoring(data);
            
//        	Timetable timetable = new Timetable(data.timeSlots, tools.ofCalculator(data));
            elapsedTime = (System.currentTimeMillis() - startTime);
            rw.writeOutput(elapsedTime, data, args[0]);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}