import java.util.Random;

public class TabuSearchConflicts {

    double bestMinPenalty;

    public void TabuSearchConflicts(Timetable timetable, Data data) {

        int i = 0;
        int	T = 7;
        int rep = 10;

        TabuList tabulist = new TabuList(T);
        TabuList tabuListSlot = new TabuList(T);
        bestMinPenalty = timetable.objFunc;
        long startTime = System.currentTimeMillis(), elapsedTime = 0;

        while(timetable.objFunc > 0 && elapsedTime < 120000) {

            switch (i%2) {
                case 0:
                    TabuMove bestMove = generatesBestNeighbourExam(timetable, tabulist, rep, data);

                    if(bestMove==null) {
                        /*
                        ** Statement unreachable (in theory).
                        */
                        System.out.println("No better neighbour found !");
                        break;
                    }

                    timetable.doSwitch2(bestMove.idExam, bestMove.sourceTimeSlot, bestMove.destinationTimeSlot);
                    elapsedTime = System.currentTimeMillis() - startTime;
                    break;
                case 1:
                    TabuSlotMove bestSlot = generatesBestNeighbourTimeslot(timetable, data, rep, tabuListSlot);

                    if(bestSlot==null) {
                        /*
                        ** Statement unreachable (in theory).
                        */
                        System.out.println("No better neighbour found !");
                        break;
                    }

                    timetable.doSwitchTimeslot(bestSlot.sourceTimeSlot, bestSlot.destinationTimeSlot);
                    elapsedTime = System.currentTimeMillis() - startTime;
                    break;
            }
            i=(i++)%2;
        }

        return;
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
		 * Generates and searches best neighbour.
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
     * Method that generates move to best neighbour if exist.
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
		 * Generates and searches best neighbour.
		 * */
        for(int i=0;i<rep;i++) {

            TabuMove move = generatesNeighbour2(data,timetable,tabulist,bestPenalty,randConflict,randExam,randTimeslot);

            double penalty =  timetable.evaluatesSwitch2(data,move.idExam,move.sourceTimeSlot,move.destinationTimeSlot);

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
     * Method that generates a valid neighbour.
     * */
    private TabuMove generatesNeighbour2(Data data, Timetable timetable, TabuList tabulist, double bestPenalty,Random randConflict, Random randExam, Random randTimeslot) {

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
            int examSelected = timetable.timeSlots.get(timeslotSource).get(conflictSelected);

            moving = new TabuMove(examSelected, timeslotSource, timeslotDestination);
            int conflictNumber = timetable.evaluatesSwitch(examSelected,timeslotSource,timeslotDestination);

            if (conflictNumber > 0)
                continue;

            double penalty = timetable.evaluatesSwitch2(data,examSelected,timeslotSource,timeslotDestination);
            /*
			 * Check if moving is already in the tabulist: if true try with another neighbour.
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

}
