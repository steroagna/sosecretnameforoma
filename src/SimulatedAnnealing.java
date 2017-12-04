import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SimulatedAnnealing {

    Timetable bestTimetable;
    long startTimetimer;
    double lastBestPenalty;
    double improvementDelta;
    int i = 0, count = 0, countw = 0, countb = 0;

    public Timetable simulatedAnnealing(Timetable timetable, Data data, int rep, long timer) {

        double alfa = 0.0001;
        double a = 10;
        Timetable tempTimetable;
        bestTimetable = new Timetable(timetable);
        startTimetimer = System.currentTimeMillis();
        lastBestPenalty = bestTimetable.objFunc;
        int plateau;
        long startTime = System.currentTimeMillis(), elapsedTime = 0;

        while (elapsedTime < timer) {
            tempTimetable = kempeChain(timetable, data, (int) Math.floor(data.slotsNumber/3));
            double temperature = setTemperature(timetable, tempTimetable, rep, data);
            plateau = (int) (data.examsNumber*a);
            while (true) {
                a = a*1.1;
                i++;
                if (i == 3)
                    break;
                temperature = 1/(1/temperature + alfa);
                System.out.println("temp: " + temperature);
                for (int j = 0; j < plateau; j++) {
                    tempTimetable = kempeChain(timetable, data, (int) Math.floor(data.slotsNumber / 3));
                    if (tempTimetable.objFunc > timetable.objFunc) {
                        double penalty = timetable.objFunc - tempTimetable.objFunc;
                        double p = Math.exp((penalty) / temperature);
                        double p1 = ThreadLocalRandom.current().nextDouble();

                        if (p1 < p) {
                            countw++;
                            timetable = tempTimetable;
                        }
                    } else {
                        countb++;
                        timetable = tempTimetable;
                    }
                    updateBest(timetable, data);
                }
            }

            i=0;
            Move move = generatesNeighbourSwappingExam(data, timetable);
            if (move.penalty > timetable.objFunc) {
                double penalty = timetable.objFunc - move.penalty;
                double p = Math.exp((penalty) / temperature);
                double p1 = ThreadLocalRandom.current().nextDouble();

                if (p1 < p) {
//                    System.out.println("worst");
                    timetable.doSwitchExamWithoutConflicts(move.idExam, move.destinationTimeSlot);
                }
            } else {
//                System.out.println("best");
                timetable.doSwitchExamWithoutConflicts(move.idExam, move.destinationTimeSlot);
            }
            timetable.objFunc = Util.ofCalculator(timetable, data);
            updateBest(timetable, data);

            elapsedTime = System.currentTimeMillis() - startTime;
        }

        System.out.println("*** END SIMULATED ANNEALING *** ");
        System.out.println("Elapsed time: " + elapsedTime);
        System.out.println("OF? " + Util.ofCalculator(bestTimetable, data));
        System.out.println("Count W? " + countw);
        System.out.println("Count B? " + countb);
        System.out.println("Count equal min? " + count);

        return bestTimetable;
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
                while (randomSlot1 == randomSlot2 || visited[randomSlot1] || visited[randomSlot2]) { //todo ottimizzare se ho già visitato solo uno dei due riscelgo solo l'altro
                    randomSlot1 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
                    randomSlot2 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
                }
                visited[randomSlot1] = true;
                visited[randomSlot2] = true;
                int randomExam = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.get(randomSlot1).size());
                int exam = tempTimetable.timeSlots.get(randomSlot1).get(randomExam);
                tempTimetable = kempeMove(randomSlot1, randomSlot2, exam, tempTimetable);
                tempTimetable.objFunc = Util.ofCalculator(tempTimetable, data);
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

    private double setTemperature(Timetable timetable, Timetable startingTimetable, int rep, Data data) {

        Timetable tempTimetable;
//        Move move;
        double temperature;
        double totPenalty = 0;
        int numberWorstSol = 0;

        for (int i = 0; i<rep ; i++) {
            tempTimetable = kempeChain(timetable, data, (int) Math.floor(data.slotsNumber/3));
            if (tempTimetable.objFunc > startingTimetable.objFunc) {
                totPenalty += tempTimetable.objFunc;
                numberWorstSol++;
            }
        }

        if (numberWorstSol > 0) {
            totPenalty /= numberWorstSol;
            temperature = -((totPenalty - startingTimetable.objFunc)) / Math.log(0.5);
        } else
            temperature = 1;

        return temperature;
    }

    private void updateBest(Timetable timetable, Data data) {
        if (timetable.objFunc < bestTimetable.objFunc) {
            bestTimetable = new Timetable(timetable);
            improvementDelta = lastBestPenalty - bestTimetable.objFunc;
            lastBestPenalty = new Double(bestTimetable.objFunc);
            if ( improvementDelta > 0.001)
                startTimetimer = System.currentTimeMillis();
            if (Main.debug) {
//                System.out.println("Timer: " + improvementTimer);
                System.out.println("OF? " + Util.ofCalculator(timetable, data));
//                System.out.println(Util.feasibilityChecker( bestTimetable, data));
            }
        } else if (timetable.objFunc == bestTimetable.objFunc)
            count++;
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

            moving.penalty = timetable.evaluatesSwitchWithoutConflicts(data,examSelected,timeslotSource,timeslotDestination);

            break;
        }

        return moving;
    }
}
