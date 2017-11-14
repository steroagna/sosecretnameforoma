import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
        	Data data = new Data();
        	ReaderWriter rw = new ReaderWriter();
        	FeasibleCostructor fb = new FeasibleCostructor();
        	Tools tools = new Tools();
            data = rw.readInputFiles(args[0]);
            Timetable timetable = fb.makeFeasibleGraphColoringWithTabu(data);
            elapsedTime = (System.currentTimeMillis() - startTime);
            
//            System.out.println(timetable.printOutput(args[0]));
            System.out.println("Elaborazione dati in " + elapsedTime + " millisec");
            System.out.println("Feasable: "+ Util.feasibilityChecker(timetable, data));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}