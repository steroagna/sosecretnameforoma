import java.io.FileNotFoundException;
import java.io.File;

public class Main {

    public static String filename;
    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis(), elapsedTime;
        try {
            if (args.length < 3) {
                ReaderWriter rw = new ReaderWriter();
                Data data = rw.readInputFiles(args[0]);
                FeasibleConstructor fb = new FeasibleConstructor();
                GreatDeluge gd = new GreatDeluge();
                String option = args[1];
                filename = args[0];
                long timer;

                File f = new File(filename);
                if (f.exists() && !f.isDirectory()) {
                    if (option.equals("-t")) {
                        timer = Long.parseLong(args[2]);
                        timer *= 60000;
                        Timetable timetable = fb.makeFeasibleGraphColoringWithTabu(data, null, 10);
                        timetable.setPenality();
                        Timetable bestTimetable = gd.greatDeluge(timetable, timer, startTime);
                        elapsedTime = System.currentTimeMillis() - startTime;
                        System.out.println("Feasable: " + timetable.feasibilityChecker());
                        System.out.println("Elapsed time: " + elapsedTime);
                        System.out.println("OF Last TT after SA: " + bestTimetable.objFunc / data.studentsNumber);
                        bestTimetable.setPenality();
                        System.out.println("OF with set: " + bestTimetable.objFunc / data.studentsNumber);
                    } else {
                        System.out.println("Usage: [instancename] -t [timelimit]");
                        System.out.println("    -timelimit must be in seconds");
                    }
                } else {
                    System.out.println("Usage: [instancename] -t [timelimit]");
                    System.out.println("    -timelimit must be in seconds");
                }
            } else {
                System.out.println("Usage: [instancename] -t [timelimit]");
                System.out.println("    -timelimit must be in seconds");
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}