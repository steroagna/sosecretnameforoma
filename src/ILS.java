import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ILS {

    Timetable bestTimetableG;
    long startTimetimer;
    double lastBestPenalty;
    double improvementDelta;
    int countbk = 0, countbs = 0;

    public Timetable iteratedLocalSearch(Timetable timetable, Data data, int rep, long timer) {

        Move move = null;
        Timetable tempTimetable;
        bestTimetableG = new Timetable(timetable);
        startTimetimer = System.currentTimeMillis();
        lastBestPenalty = bestTimetableG.objFunc;
        int plateau = data.examsNumber/10;
        int counterStop;
        long startTime = System.currentTimeMillis(), elapsedTime = 0;

        while (elapsedTime < timer) {
            counterStop = 0;
            for (int j = 0; j < plateau && elapsedTime < timer && counterStop < 20; j++) {
                move = generatesNeighbourSwappingExam(data, timetable);
                System.out.println("COMPARISON 0");
                System.out.println(move.penalty);            
                System.out.println(move.destinationTimeSlot);            
                System.out.println(timetable.objFunc);
                System.out.println(Util.ofCalculator(timetable));
                if ( timetable.objFunc != Util.ofCalculator(timetable))
                	 System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAH");
                if (move.penalty < timetable.objFunc) {
                    counterStop = 0;
                    timetable.doSwitchExamWithoutConflicts(move.idExam, move.destinationTimeSlot);
                    updateBest(timetable, data, true);
                } else
                    counterStop++;
                elapsedTime = System.currentTimeMillis() - startTime;
            }
            
//            counterStop = 0;
//            for (int j = 0; j < plateau/5 - 1 && elapsedTime < timer && counterStop < 10; j++) {
//                tempTimetable = kempeChain(timetable, data, (int) Math.floor(data.slotsNumber / 3));
//                if (tempTimetable.objFunc < timetable.objFunc) {
//                    counterStop = 0;
//                    timetable = tempTimetable;
//                    updateBest(timetable, data, false);
//                } else
//                    counterStop++;
//                elapsedTime = System.currentTimeMillis() - startTime;
//            }
//            timetable = kempeChain(timetable, data, (int) Math.floor(data.slotsNumber / 3));
//            updateBest(timetable, data, false);
////            while (!updateBest(timetable, data, false))
////                timetable = kempeChain(timetable, data, (int) Math.floor(data.slotsNumber / 3));
//            move = generatesNeighbourSwappingExam(data, timetable);
//            timetable.doSwitchExamWithoutConflicts(move.idExam, move.destinationTimeSlot);
//            elapsedTime = System.currentTimeMillis() - startTime;
        }

        System.out.println("*** END SIMULATED ANNEALING *** ");
        System.out.println("Elapsed time: " + elapsedTime);
        System.out.println("OF? " + bestTimetableG.objFunc);
        System.out.println("move improvement: " + countbs);
        System.out.println("kempe improvement: " + countbk);

        return bestTimetableG;
    }

    private Timetable kempeChain(Timetable timetable, Data data, int k) {

        int randomSlot1, randomSlot2;
        int iter = 10;
        boolean[] visited;
        Timetable tempTimetable;
        Timetable bestTimetable = new Timetable(timetable);
        bestTimetable.objFunc = Double.MAX_VALUE;

        for (int i = 0; i < iter; i++) {
            tempTimetable = new Timetable(timetable);
            visited = new boolean[timetable.timeSlots.size()]; // todo creare lista invece di array in cui inserire solo i non visited
            randomSlot1 = 0;
            randomSlot2 = 0;
            for (int j = 0; j < k; j++) {
                while (randomSlot1 == randomSlot2 ) {
                    if (visited[randomSlot1] && !visited[randomSlot2]) {
                        randomSlot1 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
                    } else if (visited[randomSlot2] && !visited[randomSlot1]) {
                        randomSlot2 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
                    } else {
                        randomSlot1 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
                        randomSlot2 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
                    }
                }
                visited[randomSlot1] = true;
                visited[randomSlot2] = true;
                int randomExam = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.get(randomSlot1).size());
                int exam = tempTimetable.timeSlots.get(randomSlot1).get(randomExam);
                tempTimetable = kempeMove(randomSlot1, randomSlot2, exam, tempTimetable);
                if (tempTimetable.objFunc < bestTimetable.objFunc)
                    bestTimetable = new Timetable(tempTimetable);
            }
        }

        return bestTimetable;
    }

    private Timetable kempeMove(int slot1, int slot2, int exam, Timetable tempTimetable) {

        int departureSlot, arrivalSlot, i = 0, exam2;
        ArrayList<Integer> examsMoved = new ArrayList<>();

        tempTimetable.removeExam(exam);
        tempTimetable.addExam(slot2,exam);
        examsMoved.add(exam);

        while (tempTimetable.conflictNumber > 0) {
            if ( i%2 == 0 ) {
                departureSlot = slot2;
                arrivalSlot = slot1;
            }
            else {
                departureSlot = slot1;
                arrivalSlot = slot2;
            }

            while (tempTimetable.timeSlotsConflict.get(departureSlot).size() != 0) {
                Tuple tupla = tempTimetable.timeSlotsConflict.get(departureSlot).get(0);
                if (examsMoved.contains(tupla.e1)) {
                    exam2 = tupla.e2;
                }
                else
                    exam2 = tupla.e1;

                examsMoved.add(exam2);
                tempTimetable.removeExam(exam2);
                tempTimetable.addExam(arrivalSlot,exam2);
            }
            i++;
        }

        return tempTimetable;
    }

    private boolean updateBest(Timetable timetable, Data data, boolean flag) {
        if (timetable.objFunc < bestTimetableG.objFunc) {
            bestTimetableG = new Timetable(timetable);
            improvementDelta = lastBestPenalty - bestTimetableG.objFunc;
            lastBestPenalty = new Double(bestTimetableG.objFunc);
            if ( improvementDelta > 0.001)
                startTimetimer = System.currentTimeMillis();
            if (Main.debug) {
//                System.out.println("Timer: " + improvementTimer);
                System.out.println("OF? " + timetable.objFunc + " " + flag);
//                System.out.println(Util.feasibilityChecker( bestTimetable, data));
                timetable.toString("instance02");
                
            }
            if (flag)
                countbs++;
            else
                countbk++;
            return true;
        }
        return false;
    }

    /**
     * Method that generates a move
     * */
    private Move generatesNeighbourSwappingExam(Data data, Timetable timetable) {

        Move moving;

        for(; ; ) {

            int timeslotSource = ThreadLocalRandom.current().nextInt(timetable.timeSlots.size());
            int timeslotDestination = ThreadLocalRandom.current().nextInt(timetable.timeSlots.size());

            if(timeslotSource==timeslotDestination ||
                    timetable.timeSlots.get(timeslotSource).size()==0)
                continue;

            int conflictSelected = ThreadLocalRandom.current().nextInt(timetable.timeSlots.get(timeslotSource).size());

            int examSelected = timetable.timeSlots.get(timeslotSource).get(conflictSelected);

            moving = new Move(examSelected, timeslotSource, timeslotDestination);
            int conflictNumber = timetable.evaluatesSwitch(examSelected,timeslotSource,timeslotDestination);

            if (conflictNumber > 0)
                continue;

            moving.penalty = timetable.evaluateOF(examSelected, timeslotDestination);
            
            break;
        }

        return moving;
    }
}
