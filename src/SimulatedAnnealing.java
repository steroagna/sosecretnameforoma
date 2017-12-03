import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SimulatedAnnealing {

    Timetable bestTimetable;
    long startTimetimer;
    double lastBestPenalty;
    double improvementDelta;
    long improvementTimer;

    public Timetable simulatedAnnealing(Timetable timetable, Data data, int rep, long timer) {

        double alfa = 0.99;
        Timetable tempTimetable;
        bestTimetable = new Timetable(timetable);
        startTimetimer = System.currentTimeMillis();
        lastBestPenalty = bestTimetable.objFunc;
        improvementTimer = 0;
        int plateau = timetable.timeSlots.size()*data.examsNumber/8;

        long startTime = System.currentTimeMillis(), elapsedTime;

        tempTimetable = kempeChain(timetable, data, (int) Math.floor(data.slotsNumber/2));
        double temperature = setTemperature(timetable, tempTimetable, rep, data);
        temperature *= alfa;
        while (improvementTimer < timer) {

            temperature = temperature*alfa;
            System.out.println("temp: " + temperature);
            for (int i = 0; i < plateau; i++) {

                tempTimetable = kempeChain(timetable, data, (int) Math.floor(data.slotsNumber/2));

                if (tempTimetable.objFunc > timetable.objFunc) {
                    double p = Math.exp((timetable.objFunc - tempTimetable.objFunc) / temperature);
                    double p1 = ThreadLocalRandom.current().nextDouble();

                    if ( p1 < p) {
                        timetable = tempTimetable;
                    }
                } else {
                    timetable = tempTimetable;
                }
                updateBest(timetable, data);

                improvementTimer = 0;
            }
        }

        elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("*** END SIMULATED ANNEALING *** ");
        System.out.println("Elapsed time: " + elapsedTime);
        System.out.println("OF? " + Util.ofCalculator(bestTimetable, data));

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
        double temperature;
        double totPenalty = 0;
        int numberWorstSol = 0;

        for (int i = 0; i<rep ; i++) {
            tempTimetable = kempeChain(timetable, data, (int) Math.floor(data.slotsNumber/2));
            if (tempTimetable.objFunc > startingTimetable.objFunc) {
                totPenalty += tempTimetable.objFunc;
                numberWorstSol++;
            }
        }

        totPenalty /= numberWorstSol;

        temperature = -((totPenalty - startingTimetable.objFunc))/Math.log(0.2);

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
        }
    }
}
