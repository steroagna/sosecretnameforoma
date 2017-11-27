import java.util.ArrayList;
import java.util.Random;

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
        bestMinPenalty = timetable.objFunc;
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

                Move move = generatesNeighbourSwappingExam(data, timetable);

                if (move.penalty > timetable.objFunc) {
                    double p = Math.exp((timetable.objFunc - move.penalty) / temperature);
                    Random r1 = new Random();
                    double p1 = r1.nextDouble();

                    if ( p1 < p) {
                        timetable.doSwitchExamWithoutConflicts(move.idExam, move.sourceTimeSlot, move.destinationTimeSlot);
                    }
                } else {
                    timetable.doSwitchExamWithoutConflicts(move.idExam, move.sourceTimeSlot, move.destinationTimeSlot);
                }

                timetable.objFunc = Util.ofCalculator(timetable, data);

                updateBest(timetable, data);

                improvementTimer = System.currentTimeMillis() - startTime;
            }
        }

        elapsedTime = System.currentTimeMillis() - startTime;
        if (Main.debug) {
            System.out.println("*** END SIMULATED ANNEALING *** ");
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("OF? " + Util.ofCalculator(bestTimetable, data));
        }

        return bestTimetable;
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
            lastBestPenalty = bestTimetable.objFunc;
            if ( improvementDelta > 0.001)
                startTimetimer = System.currentTimeMillis();
            if (Main.debug) {
                System.out.println("Timer: " + improvementTimer);
                System.out.println("OF? " + Util.ofCalculator(timetable, data));
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
