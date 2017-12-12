import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ILS {

    Timetable bestTimetableG;
    long startTimetimer;
    double lastBestPenalty;
    double improvementDelta;
    int countbk = 0, countbs = 0;

    public Timetable iteratedLocalSearch(Timetable timetable, Data data, int rep, long timer) {

        Move move;
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
                move = generatesNeighbourSwappingExam(timetable);
                if (move.penalty < timetable.objFunc) {
                    counterStop = 0;
                    timetable.doSwitchExamWithoutConflicts(move);
                    if ( timetable.objFunc != Util.ofCalculator(timetable) && false) {
                        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAH");
                        System.out.println("evaluated: " + move.penalty);
                        System.out.println("dest timeslot: " + move.destinationTimeSlot);
                        System.out.println("wrong: " + timetable.objFunc);
                        System.out.println("right (with util): " + Util.ofCalculator(timetable));
                    }
                    updateBest(timetable, true);
                } else
                    counterStop++;
                elapsedTime = System.currentTimeMillis() - startTime;
            }
            
            counterStop = 0;
            for (int j = 0; j < plateau/5 - 1 && elapsedTime < timer /*&& counterStop < 5*/; j++) {
                tempTimetable = kempeChain(timetable, (int) Math.floor(data.slotsNumber / 2));
                if (tempTimetable.objFunc < timetable.objFunc) {
                    counterStop = 0;
                    timetable = tempTimetable;
                    updateBest(timetable, false);
                } else
                    counterStop++;
                elapsedTime = System.currentTimeMillis() - startTime;
            }
            timetable = kempeChain(timetable, (int) Math.floor(data.slotsNumber / 4));
            updateBest(timetable, false);
//            while (!updateBest(timetable, data, false))
//                timetable = kempeChain(timetable, data, (int) Math.floor(data.slotsNumber / 3));
            move = generatesNeighbourSwappingExam(timetable);
            timetable.doSwitchExamWithoutConflicts(move);
            elapsedTime = System.currentTimeMillis() - startTime;
        }

        System.out.println("*** END SIMULATED ANNEALING *** ");
        System.out.println("Elapsed time: " + elapsedTime);
        System.out.println("OF? " + bestTimetableG.objFunc);
        System.out.println("move improvement: " + countbs);
        System.out.println("kempe improvement: " + countbk);

        return bestTimetableG;
    }

    private Timetable kempeChain(Timetable timetable, int k) {

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
                if (tempTimetable.objFunc < bestTimetable.objFunc) {
                    bestTimetable = new Timetable(tempTimetable);
                    break;
                }
            }
        }

        return bestTimetable;
    }

    private Timetable kempeMove(int slot1, int slot2, int exam, Timetable tempTimetable) {

        int departureSlot, arrivalSlot, i = 0, exam2;
        ArrayList<Integer> examsMoved = new ArrayList<>();

        tempTimetable.updateOF(exam, tempTimetable.positions.get(exam), false);
        tempTimetable.removeExam(exam);
        tempTimetable.addExam(slot2,exam);
        tempTimetable.updateOF(exam, tempTimetable.positions.get(exam), true);
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
                tempTimetable.updateOF(exam2, tempTimetable.positions.get(exam2), false);
                tempTimetable.removeExam(exam2);
                tempTimetable.addExam(arrivalSlot,exam2);
                tempTimetable.updateOF(exam2, arrivalSlot, true);
            }
            i++;
        }

        return tempTimetable;
    }

    private boolean updateBest(Timetable timetable, boolean flag) {
        if (timetable.objFunc < bestTimetableG.objFunc) {
            bestTimetableG = new Timetable(timetable);
            improvementDelta = lastBestPenalty - bestTimetableG.objFunc;
            lastBestPenalty = bestTimetableG.objFunc;
            if ( improvementDelta > 0.001)
                startTimetimer = System.currentTimeMillis();
            if (Main.debug) {
//                System.out.println("Timer: " + improvementTimer);
                System.out.println("OF? " + timetable.objFunc + " " + flag);
//                System.out.println(Util.feasibilityChecker( bestTimetable, data));
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
    private Move generatesNeighbourSwappingExam(Timetable timetable) {

        Move moving;

        for(;;) {

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
