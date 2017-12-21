import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class HillClimbing {

    Timetable bestTimetableG;
    int count = 0;

    public Timetable hillClimbing(Timetable timetable, long timer, long startTime) {

        Timetable tempTimetable;
        Move move;
        Swap swap;
        bestTimetableG = new Timetable(timetable);
        long elapsedTime = 0;
        int kKempe = timetable.timeSlots.size() / 2;
        int iteration = 0, actual;
        int costListSize = 5000;
        Double[] penaltyList = new Double[costListSize];

        for (int i = 0; i < costListSize; i++)
            penaltyList[i] = timetable.objFunc;

        while (elapsedTime < timer) {
            tempTimetable = new Timetable(timetable);
            switch (ThreadLocalRandom.current().nextInt(4)) {
                case 0:
                    move = tempTimetable.generatesNeighbourMovingExamWithKempe();
                    if (move != null) {
                        tempTimetable.moveExamWithoutConflicts(move);
                        updateBest(tempTimetable, "exam move");
                    } else
                        updateBest(tempTimetable, "kempe simple");
                    break;
                case 1:
                    swap = tempTimetable.generatesNeighbourSwappingExam();
                    tempTimetable.doSwap(swap);
                    updateBest(tempTimetable, "exam swap");
                    break;
                case 2:
                    tempTimetable.perturbation2();
                    updateBest(tempTimetable, "timeslot swap");
                    break;
                case 3:
                    tempTimetable = tempTimetable.kempeChain(kKempe, 2);
                    updateBest(tempTimetable, "kempe hard");
                    break;
            }
            actual = iteration % costListSize;
            if (tempTimetable.objFunc < penaltyList[actual]
                    || tempTimetable.objFunc <= timetable.objFunc)
                timetable = new Timetable(tempTimetable);
            if (tempTimetable.objFunc < penaltyList[actual]) {
                penaltyList[actual] = timetable.objFunc;
            }
            iteration++;
            elapsedTime = System.currentTimeMillis() - startTime;
        }

        return bestTimetableG;
    }

    public boolean updateBest(Timetable timetable, String flag) {
        if (timetable.objFunc < bestTimetableG.objFunc) {
            bestTimetableG = new Timetable(timetable);
            bestTimetableG.toString(Main.filename);
            if (Main.debug) {
                System.out.println("OF --> " + timetable.objFunc / timetable.data.studentsNumber + " " + flag);
            }
            return true;
        }
        return false;
    }
}
