import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SimulatedAnnealing {

    double bestMinPenalty;
    Timetable bestTimetable;
    long startTimetimer;
    double lastBestPenalty;
    double improvementDelta;
    long improvementTimer;

    public Timetable simulatedAnnealing(Timetable timetable, Data data, int rep, long timer) {

        double alfa = 0.99;
        bestTimetable = new Timetable(timetable);
        bestMinPenalty = new Double(timetable.objFunc);
        startTimetimer = System.currentTimeMillis();
        lastBestPenalty = bestMinPenalty;
        improvementTimer = 0;
        int plateau = timetable.timeSlots.size()*data.examsNumber*20/8;

        long startTime = System.currentTimeMillis(), elapsedTime;

        double temperature = setTemperature(timetable, rep, data);
        temperature *= alfa;
        while (improvementTimer < timer) {

            temperature = temperature*alfa;
            for (int i = 0; i < plateau; i++) {

//                Move move = generatesNeighbourSwappingExam(data, timetable);
//
//                if (move.penalty > timetable.objFunc) {
//                    double p = Math.exp((timetable.objFunc - move.penalty) / temperature);
//                    Random r1 = new Random();
//                    double p1 = r1.nextDouble();
//
//                    if ( p1 < p) {
//                        timetable.doSwitchExamWithoutConflicts(move.idExam, move.sourceTimeSlot, move.destinationTimeSlot);
//                    }
//                } else {
//                    timetable.doSwitchExamWithoutConflicts(move.idExam, move.sourceTimeSlot, move.destinationTimeSlot);
//                }
//
//                timetable.objFunc = Util.ofCalculator(timetable, data);
//
//                updateBest(timetable, data);

                timetable = kempeChain(timetable, data);

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

    private Timetable kempeChain(Timetable timetable, Data data) {

        int randomSlot1, randomSlot2;
        int iter = 10;
        int k = 5;
        boolean[] visited;
        Timetable temp;
        Timetable bestTimetable = new Timetable(timetable);

//        for (int i = 0; i < iter; i++) {
            temp = new Timetable(timetable);
//            System.out.println(temp.toString("ciao"));

            visited = new boolean[timetable.timeSlots.size()]; // todo creare lista invece di array in cui inserire solo i non visited
            randomSlot1 = 0;
            randomSlot2 = 0;
//            for (int j = 0; j < k; j++) {
                while (randomSlot1 == randomSlot2 || visited[randomSlot1] || visited[randomSlot2]) { //todo ottimizzare se ho già visitato solo uno dei due riscelgo solo l'altro
                    randomSlot1 = ThreadLocalRandom.current().nextInt(timetable.timeSlots.size());
                    randomSlot2 = ThreadLocalRandom.current().nextInt(timetable.timeSlots.size());
                }
                visited[randomSlot1] = true;
                visited[randomSlot2] = true;
                int randomExam = ThreadLocalRandom.current().nextInt(timetable.timeSlots.get(randomSlot1).size());
                int exam = timetable.timeSlots.get(randomSlot1).get(randomExam);
                temp = kempeMove(randomSlot1, randomSlot2, exam, temp);
//            }

            temp.objFunc = Util.ofCalculator(temp, data);
            if (temp.objFunc < timetable.objFunc) {
                bestTimetable = new Timetable(temp);
//                break;
            } else {
                temp = kempeMove(randomSlot2, randomSlot1, exam, temp);
            }
//        }
        return bestTimetable;
    }

    private Timetable kempeMove(int slot1, int slot2, int exam, Timetable temp) {

        int departureSlot, arrivalSlot, i = 0, exam2;
        ArrayList<Integer> examsMoved = new ArrayList<>();

        temp.removeExam(exam);
        temp.addExam(slot2,exam);
        examsMoved.add(exam);
//        System.out.println("Exam " + exam + " da " + slot1 + " a " + slot2);

        while (temp.conflictNumber > 0) {
            if ( i%2 == 0 ) {
                departureSlot = slot2;
                arrivalSlot = slot1;
            }
            else {
                departureSlot = slot1;
                arrivalSlot = slot2;
            }

            while (temp.timeSlotsConflict.get(departureSlot).size() != 0) {
                Tuple tupla = temp.timeSlotsConflict.get(departureSlot).get(0);
                if (examsMoved.contains(tupla.e1)) {
                    exam2 = tupla.e2;
                }
                else
                    exam2 = tupla.e1;

                examsMoved.add(exam2);
                temp.removeExam(exam2);
                temp.addExam(arrivalSlot,exam2);
//                System.out.println("Exam " + exam2 + " da " + departureSlot + " a " + arrivalSlot);
            }
            i++;
        }

        return temp;
    }

    private double setTemperature(Timetable timetable, int rep, Data data) {

        double temperature;
        double penalty;
        double totPenalty = 0;
        int numberWorstSol = 0;

        for (int i = 0; i<rep ; i++) {
            penalty = generatesBestNeighbourExam(timetable, data);
            if (penalty > timetable.objFunc) {
                totPenalty += penalty;
                numberWorstSol++;
            }
        }

        totPenalty /= numberWorstSol;

        temperature = -((totPenalty - timetable.objFunc))/Math.log(0.5);

        return temperature;
    }

    private double generatesBestNeighbourExam(Timetable timetable, Data data) {

        Random randTimeslot = new Random();
        Random randConflict = new Random();
        double penalty;

        for(; ; ) {

            int timeslotSource = randTimeslot.nextInt(timetable.timeSlots.size());
            int timeslotDestination = randTimeslot.nextInt(timetable.timeSlots.size());

			/*
			 * If timeslot source doesn't contain conflicts or timeslots source is also the destination
			 * try to generate another neighbour.
			 * */
            //timetable.timeSlotsConflict.get(timeslotSource).size()==0 ||
            if(
                    timeslotSource==timeslotDestination
                            || timetable.timeSlots.get(timeslotSource).size()==0
                    )
            {
                continue;
            }

            int conflictSelected = randConflict.nextInt(timetable.timeSlots.get(timeslotSource).size());

            int examSelected = timetable.timeSlots.get(timeslotSource).get(conflictSelected);

            int conflictNumber = timetable.evaluatesSwitch(examSelected,timeslotSource,timeslotDestination);

            if (conflictNumber > 0)
                continue;

            penalty = timetable.evaluatesSwitchWithoutConflicts(data,examSelected,timeslotSource,timeslotDestination);


            break;
        }

        return penalty;

    }

    private double evaluatePermutationExam(Timetable timetable, Data data) {
        double penalty  = 0;



        return penalty;
    }

    private void updateBest(Timetable timetable, Data data) {
        if (timetable.objFunc < bestTimetable.objFunc) {
            bestTimetable = new Timetable(timetable);
            improvementDelta = lastBestPenalty - bestTimetable.objFunc;
            lastBestPenalty = new Double(bestTimetable.objFunc);
            if ( improvementDelta > 0.001)
                startTimetimer = System.currentTimeMillis();
            if (Main.debug) {
                System.out.println("Timer: " + improvementTimer);
                System.out.println("OF? " + Util.ofCalculator(timetable, data));
                System.out.println(bestTimetable.toString("MastrototaroChain"));
                System.out.println(Util.feasibilityChecker( bestTimetable, data));
            }
        }
    }

    /**
     * Method that generates a move
     * */
    private Move generatesNeighbourSwappingExam(Data data, Timetable timetable) {

        Random randTimeslot = new Random();
        Random randConflict = new Random();
        Move moving;

        for(; ; ) {

            int timeslotSource = randTimeslot.nextInt(timetable.timeSlots.size());
            int timeslotDestination = randTimeslot.nextInt(timetable.timeSlots.size());

            if(timeslotSource==timeslotDestination ||
                    timetable.timeSlots.get(timeslotSource).size()==0)
                continue;

            int conflictSelected = randConflict.nextInt(timetable.timeSlots.get(timeslotSource).size());

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
