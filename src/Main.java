import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
        	Data data = new Data();
        	FeasibleCostructor fb = new FeasibleCostructor();
        	Tools tools = new Tools();
            data = ReaderWriter.readInputFiles(args[0]);
            data = fb.makeInitialRandom(data);
//            data = fb.makeFeasibleStudentBased(data);
            
        	Timetable timetable = new Timetable(data.timeSlots, tools.ofCalculator(data));
            elapsedTime = (System.currentTimeMillis() - startTime);
            ReaderWriter.writeOutput(elapsedTime, data);
            ReaderWriter.writeOutputToFile(data, args[0]);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}