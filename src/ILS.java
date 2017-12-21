import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public class ILS {

    Timetable bestTimetableG;
    int countbk = 0, countbm = 0, countbs = 0, countReset = 0;

    public Timetable ILST(Timetable timetable, Data data, long timer, long startTime) throws Exception {

        ArrayList<ILSMoveThread> ilsmt = new ArrayList<>();
        ArrayList<ILSSwapThread> ilsst = new ArrayList<>();
        ArrayList<ILSKempeThread> ilskt = new ArrayList<>();
        ILS.ILSMoveThread ilsm = new ILS.ILSMoveThread(timetable,0,0,0);
        ILS.ILSSwapThread ilss = new ILS.ILSSwapThread(timetable,0,0,0);
        ILS.ILSKempeThread ilsk = new ILS.ILSKempeThread(timetable,0,0);
        Timetable tempTimetable;
        bestTimetableG      = new Timetable(timetable);
        Move move, bestMove = new Move(0,0,0);
        Swap swap, bestSwap = new Swap();
        int plateau         = data.examsNumber/20;
        int threadsMove     = 50;
        int threadsSwap     = 50;
        int threadsKempe    = 15;
        long elapsedTime    = 0;

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
                updateBest(timetable, "exam move");
            } else {
                countbm++;
            }

            ilsmt.clear();

            for (int i = 0; i < threadsSwap; i++)
                ilsst.add(new ILS.ILSSwapThread(timetable, plateau, timer, startTime));

            for (int i = 0; i < threadsSwap; i++)
                ilsst.get(i).start();

            bestSwap.penalty = Double.MAX_VALUE;
            for (int i = 0; i < threadsSwap; i++) {
                ilsst.get(i).join();
                swap = ilsst.get(i).swap;
                if (swap.penalty < bestSwap.penalty){
                    bestSwap.m1 = swap.m1;
                    bestSwap.m2 = swap.m2;
                    bestSwap.penalty = swap.penalty;
                }
            }

            if (bestSwap.penalty < timetable.objFunc) {
                timetable.doSwap(bestSwap);
                updateBest(timetable, "exam swap");
            } else
                countbs++;
            ilsst.clear();

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
            updateBest(timetable, "kempe");
            ilskt.clear();


            if (countReset == 5) {
                timetable = new Timetable(bestTimetableG);
                countReset = 0;
                countbm = 0;
                countbs = 0;
                countbk = 0;
                System.out.println("Timetable Reset!");
            }
            if (countbm > 10 && countbs > 20 && countbk > 100) {
                if (!timetable.examMoved.isEmpty()) {
                    int moved = timetable.perturbation();
                    System.out.println("Perturbation moved " + moved + " exams");
                    updateBest(timetable, "perturbation");
                    countbm = 0;
                    countbs = 0;
                    countbk = 0;
                    countReset++;
                } else {
                    timetable.repopulateMovedExam();
                    timetable.setPenality();
                    System.out.println("All exams moved!");
                }
            }
            move = ilsm.generatesNeighbourMovingExam(timetable);
            timetable.doSwitchExamWithoutConflicts(move);
            timetable = ilsk.kempeChain(timetable, 2, 5);
            updateBest(timetable, "kempe");
            elapsedTime = System.currentTimeMillis() - startTime;
        }

        System.out.println("*** ILS *** ");
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
                move = new Move(0, 0, 0);
                move.penalty = Double.MAX_VALUE;
                for (int j = 0; j < iter && elapsedTime < timer && counterStop < 20; j++) {
                    temp = generatesNeighbourMovingExam(timetable);
                    if (temp.penalty < move.penalty) {
                        move.idExam = temp.idExam;
                        move.destinationTimeSlot = temp.destinationTimeSlot;
                        move.sourceTimeSlot = temp.sourceTimeSlot;
                        move.penalty = temp.penalty;
                    } else
                        counterStop++;
                    elapsedTime = System.currentTimeMillis() - startTimer;
                }
            } catch (Exception e) {
                System.out.println("Move Thread error");
            }
        }

        /**
         * Method that generates a move
         * */
        private Move generatesNeighbourMovingExam(Timetable timetable) {

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

    static public class ILSSwapThread extends Thread {

        public Timetable timetable;
        public int iter;
        public long timer;
        public long startTimer;
        public Swap swap;

        public ILSSwapThread(Timetable timetable, int iter, long timer, long startTimer) {
            this.timetable = timetable;
            this.iter = iter;
            this.timer = timer;
            this.startTimer = startTimer;
        }

        public void run() {
            try {
                Swap temp;
                int counterStop = 0;
                long elapsedTime = 0;

                swap = new Swap();
                for (int j = 0; j < iter && elapsedTime < timer && counterStop < 20; j++) {
                    temp = generatesNeighbourSwappingExam(timetable);
                    if (temp.penalty < swap.penalty) {
                        swap.m1 = temp.m1;
                        swap.m2 = temp.m2;
                        swap.penalty = temp.penalty;
                    } else
                        counterStop++;
                    elapsedTime = System.currentTimeMillis() - startTimer;
                }
            } catch (Exception e) {
                System.out.println("Swap Thread error: " + e.getCause().toString());
            }
        }

        /**
         * Method that generates a move
         * */
        private Swap generatesNeighbourSwappingExam(Timetable timetable) {

            Swap swap;
            int examSelected2 = 0;

            for(;;) {
                int timeslotSource = ThreadLocalRandom.current().nextInt(timetable.timeSlots.size());
                int timeslotDestination = ThreadLocalRandom.current().nextInt(timetable.timeSlots.size());
                if(timeslotSource==timeslotDestination ||
                        timetable.timeSlots.get(timeslotSource).size()==0)
                    continue;
                int conflictSelected = ThreadLocalRandom.current().nextInt(timetable.timeSlots.get(timeslotSource).size());
                int examSelected = timetable.timeSlots.get(timeslotSource).get(conflictSelected);
                int conflictNumber = timetable.evaluatesSwitch(examSelected,timeslotSource,timeslotDestination);
                if (conflictNumber == 1) {
                    for(Iterator<Integer> it = timetable.timeSlots.get(timeslotDestination).iterator(); it.hasNext();) {
                        examSelected2 = it.next();
                        if(timetable.data.conflictExams[examSelected][examSelected2]!=0)
                            break;
                    }
                    conflictNumber = timetable.evaluatesSwitch(examSelected2, timeslotDestination, timeslotSource);
                    if (conflictNumber == 1) {
                        swap = new Swap(timetable, timeslotSource, timeslotDestination, examSelected, examSelected2);
                        break;
                    }
                } else
                    continue;
            }
            return swap;
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
                System.out.println("Kempe Thread error");
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
    }

    private boolean updateBest(Timetable timetable, String flag) {
        if (timetable.objFunc < bestTimetableG.objFunc) {
            bestTimetableG = new Timetable(timetable);
            bestTimetableG.toString(Main.filename);
            if (Main.debug) {
                System.out.println("OF --> " + timetable.objFunc / timetable.data.studentsNumber + " " + flag);
            }
            countbs = 0;
            countbk = 0;
            return true;
        }
        return false;
    }
}
