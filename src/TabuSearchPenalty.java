import java.util.Random;
import java.util.ArrayList;

public class TabuSearchPenalty {

    double bestMinPenalty;
    Timetable bestTimetable;

    public Timetable TabuSearch(Timetable timetable, Data data, long timer) {

        int	T = 7;
        int rep = 10;
        
        bestTimetable = new Timetable(timetable);
        TabuList tabulist = new TabuList(T);
        TabuList tabuListSlot = new TabuList(T);
        bestMinPenalty = timetable.objFunc;
        long startTime = System.currentTimeMillis(), startTimetimer = System.currentTimeMillis(), elapsedTime = 0;
        double lastBestPenalty = bestMinPenalty;
        double improvementDelta;
        long improvementTimer = 0;

        while (improvementTimer < timer) {
//        for (i = 0; i < iterations; i ++) {
            TabuMove bestMove = generatesBestNeighbourExam(timetable, tabulist, rep, data);

            if (bestMove == null) {
                /*
                ** Statement unreachable (in theory).
                */
                System.out.println("No better neighbour found !");
                break;
            }

            timetable.doSwitchExamWithoutConflicts(bestMove.idExam, bestMove.sourceTimeSlot, bestMove.destinationTimeSlot);

            timetable.objFunc = Tools.ofCalculator(timetable, data);
            if (timetable.objFunc < bestTimetable.objFunc) {
                bestTimetable = new Timetable(timetable);
                improvementDelta = lastBestPenalty - bestTimetable.objFunc;
                lastBestPenalty = bestTimetable.objFunc;
                if ( improvementDelta > 0.001)
                	startTimetimer = System.currentTimeMillis();
                if (Main.debug) {
	                System.out.println("Timer: " + improvementTimer);
	                System.out.println("OF? " + Tools.ofCalculator(timetable, data));
                }
            }
            
            improvementTimer = System.currentTimeMillis() - startTimetimer;
        }
        
        elapsedTime = System.currentTimeMillis() - startTime;
        if (Main.debug) {
            System.out.println("*** Second Part *** ");
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("OF? " + Tools.ofCalculator(bestTimetable, data));
        }

        improvementTimer = 0;
        while (improvementTimer < timer) {
//        for (i = 0; i < iterations; i ++) {
            TabuMove bestMove = worstMove(timetable, data);

            if (bestMove == null) {
                /*
                ** Statement unreachable (in theory).
                */
                System.out.println("No better neighbour found !");
                break;
            }

            timetable.doSwitchExamWithoutConflicts(bestMove.idExam, bestMove.sourceTimeSlot, bestMove.destinationTimeSlot);

            timetable.objFunc = Tools.ofCalculator(timetable, data);
            if (timetable.objFunc < bestTimetable.objFunc) {
                bestTimetable = new Timetable(timetable);
                improvementDelta = lastBestPenalty - bestTimetable.objFunc;
                lastBestPenalty = bestTimetable.objFunc;
                if ( improvementDelta > 0.001)
                    startTimetimer = System.currentTimeMillis();
                if (Main.debug) {
                    System.out.println("Timer: " + improvementTimer);
                    System.out.println("OF? " + Tools.ofCalculator(timetable, data));
                }
            }

            improvementTimer = System.currentTimeMillis() - startTimetimer;
        }

        elapsedTime = System.currentTimeMillis() - startTime;
        if (Main.debug) {
            System.out.println("*** Second Part *** ");
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("OF? " + Tools.ofCalculator(bestTimetable, data));
        }

        improvementTimer = 0;
        while (improvementTimer < timer) {
//        for (i = 0; i < iterations; i ++) {
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

            timetable.objFunc = Tools.ofCalculator(timetable, data);
            if (timetable.objFunc < bestTimetable.objFunc) {
                bestTimetable = new Timetable(timetable);
                improvementDelta = lastBestPenalty - bestTimetable.objFunc;
                lastBestPenalty = bestTimetable.objFunc;
                if ( improvementDelta > 0.001)
                    startTimetimer = System.currentTimeMillis();
                if (Main.debug) {
		            System.out.println("Elapsed time: " + elapsedTime);
		            System.out.println("OF? " + Tools.ofCalculator(timetable, data));
                }
            }

            improvementTimer = System.currentTimeMillis() - startTimetimer;
        }

        return bestTimetable;
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

    private TabuMove worstMove(Timetable timetable, Data data ) {

        TabuMove moving = null;

        selectWorstTimeslot(timetable.timeSlots, data);

        System.out.println("OF: " + Tools.ofCalculator(timetable, data));
        return moving;
    }

    private int selectWorstTimeslot(ArrayList<ArrayList<Integer>> timeSlots, Data data) {

        int timeslot = 0, exam1, exam2, distance;
        double penalty, worstPenality = 0, sum = 0;

        // ciclo su tutti i timeslot
        for (int timeslotSource = 0; timeslotSource < timeSlots.size() ; timeslotSource++) {
            penalty = 0;
            int columnStart = timeslotSource - 6;
            int columnEnd = timeslotSource + 6;
            if (columnStart < 0)
                columnStart = 0;
            if (columnEnd > timeSlots.size())
                columnEnd = timeSlots.size();
            // seleziono gli esami per il timeslot attuale e calcolo per ogni esame le penalità
            for (int i = 0; i < timeSlots.get(timeslotSource).size(); i++) {
                exam1 = timeSlots.get(timeslotSource).get(i);
                // seleziono il range di +/- 5 timeslot
                for (int j = columnStart; j < columnEnd; j++) {
                    if (j == timeslotSource) continue;
                    if (j > timeslotSource)
                        distance = j - timeslotSource;
                    else
                        distance = timeslotSource - j;
                    ArrayList<Integer> slot1 = timeSlots.get(j);
                    for (int k = 0; k < slot1.size(); k++) {
                        exam2 = slot1.get(k);
                        if (data.conflictExams[exam1][exam2] > 0)
                            penalty += Math.pow(2, (5 - distance)) * data.conflictExams[exam1][exam2];
                    }
                }
            }
            penalty = penalty / data.studentsNumber;
            if (penalty > worstPenality) {
                timeslot = timeslotSource;
                worstPenality = penalty;
            }
            sum += penalty;
        }

        System.out.println("sum: " + sum);
        return timeslot;
    }


}
