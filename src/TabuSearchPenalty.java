import java.util.Random;
import java.util.ArrayList;

public class TabuSearchPenalty {

    double bestMinPenalty;
    Timetable bestTimetable;
    long startTimetimer;
    double lastBestPenalty;
    double improvementDelta;
    long improvementTimer;

    public Timetable TabuSearch(Timetable timetable, Data data) {

        int	T = 7;
        int rep = 120;

        bestTimetable = new Timetable(timetable);
        bestMinPenalty = timetable.objFunc;
        startTimetimer = System.currentTimeMillis();
        lastBestPenalty = bestMinPenalty;
        improvementTimer = 0;
        TabuList tabulist = new TabuList(T);
        TabuList tabuListSlot = new TabuList(T);
        long startTime = System.currentTimeMillis(), elapsedTime = 0;

        while (improvementTimer < 10000) {
            TabuMove bestMove = generatesBestNeighbourExam(timetable, tabulist, rep, data);

            if (bestMove == null) {
                /*
                ** Statement unreachable (in theory).
                */
                System.out.println("No better neighbour found !");
                break;
            }

            timetable.doSwitchExamWithoutConflicts(bestMove.idExam, bestMove.sourceTimeSlot, bestMove.destinationTimeSlot);

            timetable.objFunc = Util.ofCalculator(timetable, data);

            updateBest(timetable, data);

            improvementTimer = System.currentTimeMillis() - startTimetimer;

        }

        elapsedTime = System.currentTimeMillis() - startTime;
        if (Main.debug) {
            System.out.println("*** Second Part *** ");
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("OF? " + Util.ofCalculator(bestTimetable, data));
        }
        startTimetimer = System.currentTimeMillis();
        improvementTimer = 0;

        while (improvementTimer < 10000) {
            TabuSlotMove bestSlot = generatesBestNeighbourTimeslot(timetable, data, rep, tabuListSlot);

            if (bestSlot == null) {
                /*
                ** Statement unreachable (in theory).
                */
                System.out.println("No better neighbour found !");
                break;
            }

            timetable.doSwitchTimeslot(bestSlot.sourceTimeSlot, bestSlot.destinationTimeSlot);

            elapsedTime = System.currentTimeMillis() - startTime;

            timetable.objFunc = Util.ofCalculator(timetable, data);

            updateBest(timetable, data);

            improvementTimer = System.currentTimeMillis() - startTimetimer;
        }

            if (Main.debug) {
            System.out.println("*** End Tabu Search *** ");
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("OF? " + Util.ofCalculator(bestTimetable, data));
        }

        return bestTimetable;
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

    private TabuSlotMove generatesBestNeighbourTimeslot(Timetable timetable, Data data, int rep, TabuList tabulist)
    {
        Random randTimeslot1 = new Random();
        Random randTimeslot2 = new Random();

        boolean foundBetterTimetable = false;

        double bestPenalty = 0xFFFFFF;
        int bestTimeslotSource = -1;
        int bestTimeslotDestination = -1;
        int t1 = 0 , t2 = 0;

        /*
		 * Generates and searches best neighbor.
		 * */
        for(int i=0;i<rep;i++) {

            while (t1 == t2) {
                t1 = randTimeslot1.nextInt(timetable.timeSlots.size());
                t2 = randTimeslot2.nextInt(timetable.timeSlots.size());
            }

            TabuSlotMove move = new TabuSlotMove(t1, t2);
            double penalty = timetable.evaluatesSwitchTimeSlots(data,move.sourceTimeSlot,move.destinationTimeSlot);

            if(penalty < bestPenalty) {
                bestPenalty = penalty;
                bestTimeslotSource = move.sourceTimeSlot;
                bestTimeslotDestination = move.destinationTimeSlot;
                foundBetterTimetable = true;
            }
        }

        if(foundBetterTimetable) {
            TabuSlotMove tabuSlot = new TabuSlotMove(bestTimeslotSource, bestTimeslotDestination);
            return tabuSlot;
        }

		/*
		 * Statement unreachable (in theory).
		 * */
        return null;

    }

    /**
     * Method that generates move to best neighbor if exist.
     * */
    private TabuMove generatesBestNeighbourExam(Timetable timetable, TabuList tabulist,int rep, Data data){

        Random randTimeslot = new Random();
        Random randExam = new Random();
        Random randConflict = new Random();

        boolean foundBetterTimetable = false;

        double bestPenalty = 0xFFFFFF;
        int bestTimeslotSource = -1;
        int bestTimeslotDestination = -1;
        int bestExamSelected = -1;

		/*
		 * Generates and searches best neighbor.
		 * */
        for(int i=0;i<rep;i++) {

            TabuMove move = generatesNeighbourSwappingExam(data,timetable,tabulist,bestPenalty,randConflict,randExam,randTimeslot);

            double penalty =  timetable.evaluatesSwitchWithoutConflicts(data,move.idExam,move.sourceTimeSlot,move.destinationTimeSlot);

            if(penalty < bestPenalty) {
                bestPenalty = penalty;
                bestTimeslotSource = move.sourceTimeSlot;
                bestExamSelected = move.idExam;
                bestTimeslotDestination = move.destinationTimeSlot;
                foundBetterTimetable = true;
            }
        }

        if(foundBetterTimetable) {
            TabuMove tabuItem = new TabuMove(bestExamSelected, bestTimeslotSource, bestTimeslotDestination);
            return tabuItem;
        }

		/*
		 * Statement unreachable (in theory).
		 * */
        return null;
    }

    /**
     * Method that generates a valid neighbor.
     * */
    private TabuMove generatesNeighbourSwappingExam(Data data, Timetable timetable, TabuList tabulist, double bestPenalty,Random randConflict, Random randExam, Random randTimeslot) {

        TabuMove moving;

        for(;;) {

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

//            int conflictSelected = selectWorst(timetable.timeSlots , timeslotSource, data);

            int examSelected = timetable.timeSlots.get(timeslotSource).get(conflictSelected);

//            if (data.getExam(examSelected).isMarked())
//                continue;

            moving = new TabuMove(examSelected, timeslotSource, timeslotDestination);
            int conflictNumber = timetable.evaluatesSwitch(examSelected,timeslotSource,timeslotDestination);

            if (conflictNumber > 0)
                continue;

            double penalty = timetable.evaluatesSwitchWithoutConflicts(data,examSelected,timeslotSource,timeslotDestination);
            /*
			 * Check if moving is already in the tabulist: if true try with another neighbor.
			 * */
            if((tabulist.getTabuList().contains(moving) &&
                    penalty > (bestPenalty)
            )) {
                continue;
            }

			/*
			 * Check for aspiration level.
			 * */
            if(tabulist.getTabuList().contains(moving) &&
                    penalty<(bestMinPenalty-0.01)
                    ) {
                tabulist.getTabuList().remove(moving);
                bestMinPenalty = penalty;
            }

            break;
        }

        return moving;
    }

    private int selectWorst(ArrayList<ArrayList<Integer>> timeSlots, int timeslotSource, Data data) {

        int exam = 0, exam1, exam2;
        double penality, worstPenality = 0;
        int columnStart = timeslotSource - 5;
        int columnEnd = timeslotSource + 5;

        if (columnStart < 0)
            columnStart = 0;
        if (columnEnd > timeSlots.size())
            columnEnd = timeSlots.size();

        for (int i = 0; i < timeSlots.get(timeslotSource).size() ; i++) {
            exam1 = timeSlots.get(timeslotSource).get(i);
            penality = 0;
            for (int j = columnStart; j < columnEnd ; j++) {
                if ( j == timeslotSource) continue;
                ArrayList<Integer> slot1 = timeSlots.get(j);
                for (int k = 0; k < slot1.size(); k++) {
                    exam2 = slot1.get(k);
                    penality += Math.pow(2, (5 - (k - i))) * data.conflictExams[exam1][exam2];
                }
                if (penality > worstPenality)
                    exam = i;
            }
        }

        return exam;
    }

}
