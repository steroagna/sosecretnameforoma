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
//            
//            for(int i=1;i<=data.examsNumber;i++) {
//            	System.out.print("Exam "+i+": ");
//            	for(int j=1;j<=data.examsNumber;j++) {
//            		System.out.print(data.conflictExams[i][j]+"| ");
//            	}
//            	System.out.println();
//            }
            
            Timetable timetable = fb.makeFeasibleGraphColoringWithTabu(data);
            elapsedTime = (System.currentTimeMillis() - startTime);
            
            System.out.println(timetable.toString());
            System.out.println("Feasable: "+ Util.feasibilityChecker(timetable, data));
            //rw.writeOutput(elapsedTime, data, args[0]);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}