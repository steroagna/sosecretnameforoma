import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public class ILS {

    Timetable bestTimetableG;
    long startTimetimer;
    double lastBestPenalty;
    double improvementDelta;
    int countbk = 0, countbs = 0;

    public Timetable ILST(Timetable timetable, Data data, long timer, long startTime) throws Exception {
        
        Move move, bestMove = new Move(0,0,0);
        Timetable tempTimetable;
        bestTimetableG = new Timetable(timetable);
        startTimetimer = System.currentTimeMillis();
        lastBestPenalty = bestTimetableG.objFunc;
        int plateau = data.examsNumber/20;
        int threadsMove = 20;
        int threadsKempe = 40;
        long elapsedTime = 0;
        ArrayList<ILSMoveThread> ilsmt = new ArrayList<>();
        ArrayList<ILSKempeThread> ilskt = new ArrayList<>();

        while (elapsedTime < timer) {

            for (int i = 0; i < threadsMove; i++)
                ilsmt.add(new ILS.ILSMoveThread(timetable, plateau, timer, startTime));

            for (int i = 0; i < threadsMove; i++)
                ilsmt.get(i).start();

            bestMove.penalty = Double.MAX_VALUE;
            for (int i = 0; i < threadsMove; i++) {
                ilsmt.get(i).join();
                move = ilsmt.get(i).move;
                if (move.penalty < bestMove.penalty){
                    bestMove.idExam = move.idExam;
                    bestMove.destinationTimeSlot = move.destinationTimeSlot;
                    bestMove.sourceTimeSlot = move.sourceTimeSlot;
                    bestMove.penalty = move.penalty;
                }
            }

            if (bestMove.penalty < timetable.objFunc) {
                timetable.doSwitchExamWithoutConflicts(bestMove);
                updateBest(timetable, true);
            } else
                countbs++;

            ilsmt.clear();

            for (int i = 0; i < threadsKempe; i++)
                ilskt.add(new ILS.ILSKempeThread(timetable, timer, startTime));

            for (int i = 0; i < threadsKempe; i++)
                ilskt.get(i).start();

            for (int i = 0; i < threadsKempe; i++) {
                ilskt.get(i).join();
                tempTimetable = ilskt.get(i).timetable;
                if (tempTimetable.objFunc < timetable.objFunc)
                    timetable = new Timetable(tempTimetable);
                else
                    countbk++;
            }
            updateBest(timetable, false);
            ilskt.clear();

            ILS.ILSMoveThread ilsm = new ILS.ILSMoveThread(timetable,0,0,0);
            ILS.ILSKempeThread ilsk = new ILS.ILSKempeThread(timetable,0,0);

            if (countbs > 15 && countbk > 15) {
                if (!timetable.examMoved.isEmpty()) {
                    timetable.perturbation();
                    System.out.println("Perturbation!");
                    countbs = 0;
                    countbk = 0;
                } else {
                    timetable.repopulateMovedExam();
                    System.out.println("All exams moved!");
                }
            }
            timetable = ilsk.kempeChain(timetable, 2, 5);
            updateBest(timetable, false);
            move = ilsm.generatesNeighbourSwappingExam(timetable);
            timetable.doSwitchExamWithoutConflicts(move);
            elapsedTime = System.currentTimeMillis() - startTime;
        }

        System.out.println("*** END SIMULATED ANNEALING *** ");
        System.out.println("Elapsed time: " + elapsedTime);
        System.out.println("OF: " + bestTimetableG.objFunc / data.studentsNumber);
        System.out.println("move improvement: " + countbs);
        System.out.println("kempe improvement: " + countbk);

        return bestTimetableG;
    }

    static public class ILSMoveThread extends Thread {

        public Timetable timetable;
        public int iter;
        public long timer;
        public long startTimer;
        public Move move;

        public ILSMoveThread(Timetable timetable, int iter, long timer, long startTimer) {
            this.timetable = timetable;
            this.iter = iter;
            this.timer = timer;
            this.startTimer = startTimer;
        }

        public void run() {
            try {
                Move temp;
                int counterStop = 0;
                long elapsedTime = 0;
                double penaltyMin = Double.MAX_VALUE;

                move = new Move(0,0,0);
                for (int j = 0; j < iter && elapsedTime < timer && counterStop < 20; j++) {
                    temp = generatesNeighbourSwappingExam(timetable);
                    if (temp.penalty < penaltyMin) {
                        move.idExam = temp.idExam;
                        move.destinationTimeSlot = temp.destinationTimeSlot;
                        move.sourceTimeSlot = temp.sourceTimeSlot;
                        move.penalty = temp.penalty;
                    } else
                        counterStop++;
                    elapsedTime = System.currentTimeMillis() - startTimer;
                }
            } catch (Exception e) {
                System.out.println("[FeasibleConstructor::FeasibleConstructorThread::run()] Some problem occurred.");
            }
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

    static public class ILSKempeThread extends Thread {

        public Timetable timetable;
        public long timer;
        public long startTimer;

        public ILSKempeThread(Timetable timetable, long timer, long startTimer) {
            this.timetable = timetable;
            this.timer = timer;
            this.startTimer = startTimer;
        }

        public void run() {
            try {
                timetable = kempeChain(timetable, (int) Math.floor(timetable.data.slotsNumber / 3), 7);
            } catch (Exception e) {
                System.out.println("[FeasibleConstructor::FeasibleConstructorThread::run()] Some problem occurred.");
            }
        }

        private Timetable kempeChain(Timetable timetable, int k, int iter) {

            int randomSlot1, randomSlot2;
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
                        } else if (visited[randomSlot2] && !visited[randomSlot1]) {
                            randomSlot2 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
                        }else {
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
//                        break;
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

//        private Timetable kempeChain3(Timetable timetable, int k, int iter) {
//
//            int randomSlot1, randomSlot2, randomSlot3;
//            boolean[] visited;
//            Timetable tempTimetable;
//            Timetable bestTimetable = new Timetable(timetable);
//            bestTimetable.objFunc = Double.MAX_VALUE;
//
//            for (int i = 0; i < iter; i++) {
//                tempTimetable = new Timetable(timetable);
//                visited = new boolean[timetable.timeSlots.size()]; // todo creare lista invece di array in cui inserire solo i non visited
//                randomSlot1 = 0;
//                randomSlot2 = 0;
//                randomSlot3 = 0;
//                for (int j = 0; j < k; j++) {
//                    while (randomSlot1 == randomSlot2 || randomSlot1 == randomSlot3 || randomSlot2 == randomSlot3
//                            || visited[randomSlot1]
//                            || visited[randomSlot2]
//                            || visited[randomSlot3]) {
////                    if (visited[randomSlot1] && !visited[randomSlot2]) {
////                        randomSlot1 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
////                    } else if (visited[randomSlot2] && !visited[randomSlot1]) {
////                        randomSlot2 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
////                    } else if (visited[randomSlot2] && !visited[randomSlot1]) {
////                        randomSlot2 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
////                    }else {
//                        randomSlot1 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
//                        randomSlot2 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
//                        randomSlot3 = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.size());
//                    }
////                }
//                    visited[randomSlot1] = true;
//                    visited[randomSlot2] = true;
//                    visited[randomSlot3] = true;
//                    int randomExam = ThreadLocalRandom.current().nextInt(tempTimetable.timeSlots.get(randomSlot1).size());
//                    int exam = tempTimetable.timeSlots.get(randomSlot1).get(randomExam);
//                    tempTimetable = kempeMove3(randomSlot1, randomSlot2, randomSlot3, exam, tempTimetable);
//                    if (tempTimetable.objFunc < bestTimetable.objFunc) {
//                        bestTimetable = new Timetable(tempTimetable);
////                        break;
//                    }
//                }
//            }
//
//            return bestTimetable;
//        }
//
//        private Timetable kempeMove3(int slot1, int slot2, int slot3, int exam, Timetable tempTimetable) {
//
//            int departureSlot, arrivalSlot, i = 0, exam2;
//            ArrayList<Integer> examsMoved = new ArrayList<>();
//
//            tempTimetable.updateOF(exam, tempTimetable.positions.get(exam), false);
//            tempTimetable.removeExam(exam);
//            tempTimetable.addExam(slot2,exam);
//            tempTimetable.updateOF(exam, tempTimetable.positions.get(exam), true);
//            examsMoved.add(exam);
//
//            while (tempTimetable.conflictNumber > 0) {
//                if ( i%3 == 1 ) {
//                    departureSlot = slot2;
//                    arrivalSlot = slot3;
//                }
//                else if ( i%3 == 2 ) {
//                    departureSlot = slot3;
//                    arrivalSlot = slot1;
//                }
//                else {
//                    departureSlot = slot1;
//                    arrivalSlot = slot2;
//                }
//
//                while (tempTimetable.timeSlotsConflict.get(departureSlot).size() != 0) {
//                    Tuple tupla = tempTimetable.timeSlotsConflict.get(departureSlot).get(0);
//                    if (examsMoved.contains(tupla.e1)) {
//                        exam2 = tupla.e2;
//                    }
//                    else
//                        exam2 = tupla.e1;
//
//                    examsMoved.add(exam2);
//                    tempTimetable.updateOF(exam2, tempTimetable.positions.get(exam2), false);
//                    tempTimetable.removeExam(exam2);
//                    tempTimetable.addExam(arrivalSlot,exam2);
//                    tempTimetable.updateOF(exam2, arrivalSlot, true);
//                }
//                i++;
//            }
//
//            return tempTimetable;
//        }
    }

    private boolean updateBest(Timetable timetable, boolean flag) {
        if (timetable.objFunc < bestTimetableG.objFunc) {
            bestTimetableG = new Timetable(timetable);
            improvementDelta = lastBestPenalty - bestTimetableG.objFunc;
            lastBestPenalty = bestTimetableG.objFunc;
            bestTimetableG.toString(Main.filename);
            if (improvementDelta > 0.001)
                startTimetimer = System.currentTimeMillis();
            if (Main.debug) {
//                System.out.println("Timer: " + improvementTimer);
                System.out.println("OF --> " + timetable.objFunc / timetable.data.studentsNumber + " " + flag);
//                System.out.println(Util.feasibilityChecker( bestTimetable, data));
            }
            countbs = 0;
            countbk = 0;
            return true;
        }
        return false;
    }
}
