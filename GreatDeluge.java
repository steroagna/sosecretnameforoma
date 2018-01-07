import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GreatDeluge {

    Timetable bestTimetableG;
    double deltaLevel;

    public Timetable greatDeluge(Timetable timetable, long timer, long startTime) throws Exception {
        ArrayList<GDMoveThread> ilsmt = new ArrayList<>();
        ArrayList<GDSwapThread> ilsst = new ArrayList<>();
        ArrayList<GDKempeThread> ilskt = new ArrayList<>();
        Timetable tempTimetable, tempTimetableKempe;
        Move move, bestMove = new Move(0,0,0);
        Swap swap, bestSwap = new Swap();
        bestTimetableG = new Timetable(timetable);
        deltaLevel = 0.0001 * bestTimetableG.objFunc;
        long elapsedTime = 0;
        int iteration = 0, kKempe = timetable.timeSlots.size() / 4, iterNoMovement = 0,
                nMoves, nMovesMax = 8, nExamsMax = 600, moved,
                i, countManyMoves = 1, plateau = 50;
        int threadsMove  = 50;
        int threadsSwap  = 50;
        int threadsKempe = 8;
//        nMoves = timetable.data.examsNumber * nMovesMax / nExamsMax;
//        if (nMoves < 5)
//            nMoves = 5;
//        if (nMoves > 8)
            nMoves = 8;
        System.out.println("nMoves: " + nMoves);
        double level, initialLevel = timetable.objFunc, p;
        double reductionConstInitial = timetable.data.examsNumber * 0.0000006513 - 0.00004432;
        if (reductionConstInitial < 0.00005)
            reductionConstInitial = 0.00005;
        double reductionConst = reductionConstInitial;

        System.out.println("Reduction Const: " + reductionConst);
        level = initialLevel;

        while (elapsedTime < timer) {
            tempTimetable = new Timetable(timetable);
            switch (ThreadLocalRandom.current().nextInt(nMoves)) {
                case 0:
                    for (i = 0; i < threadsKempe; i++)
                        ilskt.add(new GDKempeThread(tempTimetable, kKempe));

                    for (i = 0; i < threadsKempe; i++)
                        ilskt.get(i).start();

                    for (i = 0; i < threadsKempe; i++) {
                        ilskt.get(i).join();
                        tempTimetableKempe = ilskt.get(i).timetable;
                        if (tempTimetableKempe.objFunc < tempTimetable.objFunc)
                            tempTimetable = new Timetable(tempTimetableKempe);
                    }
                    ilskt.clear();

                    if (updateBest(tempTimetable, "kempe hard 1")) {
                        reductionConst *= 1.002;
                    }
                    break;
                case 1:
                    swap = tempTimetable.generatesNeighbourSwappingExam();
                    tempTimetable.doSwap(swap);
                    if (updateBest(tempTimetable, "exam swap 1")) {
                        reductionConst *= 1.002;
                    }
                    break;
                case 2:
                    for (i = 0; i < 2; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 2")) {
                            reductionConst *= 1.002;
                        }
                    }
                    break;
                case 3:
                    for (i = 0; i < 3; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 3")) {
                            reductionConst *= 1.002;
                        }
                    }
                    break;
                case 4:
                    for (i = 0; i < 4; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 4")) {
                            reductionConst *= 1.002;
                        }
                    }
                    break;
                case 5:
                    for (i = 0; i < 5; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 5")) {
                            reductionConst *= 1.002;
                        }
                    }
                    break;
                case 6:
                    for (i = 0; i < 6; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 6")) {
                            reductionConst *= 1.002;
                        }
                    }
                    break;
                case 7:
                    for (i = 0; i < 7; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 7")) {
                            reductionConst *= 1.002;
                        }
                    }
                    break;
            }

            if (tempTimetable.objFunc > level && timetable.objFunc > level) {
                iterNoMovement++;
            } else if (tempTimetable.objFunc < level || tempTimetable.objFunc < timetable.objFunc){
                timetable = new Timetable(tempTimetable);
                iterNoMovement = 0;
            } else
                iterNoMovement++;

            if(iterNoMovement >= 1000){
                System.out.println();
                System.out.println("Before Reset");
                System.out.println(" |-> Level: " + level / timetable.data.studentsNumber);
                System.out.println(" |-> Actual OF: " + timetable.objFunc / timetable.data.studentsNumber);
                System.out.println(" |-> Actual OF temp: " + tempTimetable.objFunc / timetable.data.studentsNumber);
                timetable = new Timetable(bestTimetableG);
                if (countManyMoves % 3 == 1) {
                    System.out.println("Trying Bests with Thread");
                    for (i = 0; i < threadsMove; i++)
                        ilsmt.add(new GDMoveThread(timetable, plateau, timer, startTime));

                    for (i = 0; i < threadsMove; i++)
                        ilsmt.get(i).start();

                    bestMove.penalty = Double.MAX_VALUE;
                    for (i = 0; i < threadsMove; i++) {
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
                        timetable.moveExamWithoutConflicts(bestMove);
                        updateBest(timetable, "exam move");
                    }
                    ilsmt.clear();

                    for (i = 0; i < threadsSwap; i++)
                        ilsst.add(new GDSwapThread(timetable, plateau, timer, startTime));

                    for (i = 0; i < threadsSwap; i++)
                        ilsst.get(i).start();

                    bestSwap.penalty = Double.MAX_VALUE;
                    for (i = 0; i < threadsSwap; i++) {
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
                    }
                    ilsst.clear();

                    for (i = 0; i < threadsKempe; i++)
                        ilskt.add(new GDKempeThread(tempTimetable, kKempe));

                    for (i = 0; i < threadsKempe; i++)
                        ilskt.get(i).start();

                    for (i = 0; i < threadsKempe; i++) {
                        ilskt.get(i).join();
                        tempTimetableKempe = ilskt.get(i).timetable;
                        if (tempTimetableKempe.objFunc < tempTimetable.objFunc)
                            tempTimetable = new Timetable(tempTimetableKempe);
                    }
                    ilskt.clear();
                } else if (countManyMoves % 3 == 2) {
                    moved = timetable.manyMovesWorstExams(0.35);
                    System.out.println("Moved: " + moved + " exams");
                    moved = timetable.manyMovesWorstExams(0.35);
                    System.out.println("Moved: " + moved + " exams");
                    if (level < timetable.objFunc) {
                        level = timetable.objFunc;
                        if (initialLevel < level) {
                            initialLevel = level;
                        }
                    }
                }
                reductionConst = reductionConstInitial;
                countManyMoves++;
                iterNoMovement = 0;
                System.out.println("After Reset");
                System.out.println(" |-> Level: " + level / timetable.data.studentsNumber);
                System.out.println(" |-> Actual OF: " + timetable.objFunc / timetable.data.studentsNumber);
                System.out.println(" |-> Actual OF temp: " + tempTimetable.objFunc / timetable.data.studentsNumber);
                System.out.println();
            }

            if (iteration % 2 == 0)
                level -= (level - bestTimetableG.objFunc) * reductionConst;
            if (level - bestTimetableG.objFunc < deltaLevel) {
                reductionConst = reductionConstInitial;
                p = ThreadLocalRandom.current().nextDouble();
                level += (initialLevel - level) * p;
                System.out.println();
                System.out.println("New Level: " + level / timetable.data.studentsNumber);
                System.out.println("Actual OF: " + timetable.objFunc / timetable.data.studentsNumber);
                while (level < timetable.objFunc) {
                    p = ThreadLocalRandom.current().nextDouble();
                    level += (initialLevel - level) * p;
                }
            }

            // Just for print
            if (iteration % 10000 == 0){
                System.out.println();
                System.out.println("Level: " + level / timetable.data.studentsNumber);
                System.out.println("Actual OF: " + timetable.objFunc / timetable.data.studentsNumber);
                System.out.println("Actual OF temp: " + tempTimetable.objFunc / timetable.data.studentsNumber);
            }

            iteration++;
            elapsedTime = System.currentTimeMillis() - startTime;
        }

        return bestTimetableG;
    }

    static public class GDKempeThread extends Thread {

        public Timetable timetable;
        public int n;

        public GDKempeThread(Timetable timetable, int n) {
            this.timetable = timetable;
            this.n = n;
        }

        public void run() {
            try {
                timetable = timetable.kempeChain(n);
            } catch (Exception e) {
                System.out.println("Kempe Thread Error");
            }
        }
    }

    static public class GDMoveThread extends Thread {

        public Timetable timetable;
        public int iter;
        public long timer;
        public long startTimer;
        public Move move;

        public GDMoveThread(Timetable timetable, int iter, long timer, long startTimer) {
            this.timetable = timetable;
            this.iter = iter;
            this.timer = timer;
            this.startTimer = startTimer;
        }

        public void run() {
            try {
                Move temp;
                int counterStop = 0, j;
                long elapsedTime = 0;
                move = new Move(0, 0, 0);
                move.penalty = Double.MAX_VALUE;
                for (j = 0; j < iter && elapsedTime < timer && counterStop < 20; j++) {
                    temp = timetable.generatesNeighbourMovingExam();
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
    }

    static public class GDSwapThread extends Thread {

        public Timetable timetable;
        public int iter;
        public long timer;
        public long startTimer;
        public Swap swap;

        public GDSwapThread(Timetable timetable, int iter, long timer, long startTimer) {
            this.timetable = timetable;
            this.iter = iter;
            this.timer = timer;
            this.startTimer = startTimer;
        }

        public void run() {
            try {
                Swap temp;
                int counterStop = 0, j;
                long elapsedTime = 0;

                swap = new Swap();
                for (j = 0; j < iter && elapsedTime < timer && counterStop < 20; j++) {
                    temp = timetable.generatesNeighbourSwappingExam();
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
    }

    public boolean updateBest(Timetable timetable, String flag) {
        if (timetable.objFunc < bestTimetableG.objFunc) {
            bestTimetableG = new Timetable(timetable);
            deltaLevel = 0.0001 * bestTimetableG.objFunc;
            bestTimetableG.toString(Main.filename);
            if (Main.debug) {
                System.out.println("OF --> " + timetable.objFunc / timetable.data.studentsNumber + " " + flag  + " <--");
            }
            return true;
        }
        return false;
    }
}