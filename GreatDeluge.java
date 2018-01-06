import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GreatDeluge {

    Timetable bestTimetableG;
    double deltaLevel;

    public Timetable greatDeluge(Timetable timetable, long timer, long startTime) throws Exception {
        ArrayList<GDKempeThread> ilskt = new ArrayList<>();
        Timetable tempTimetable, tempTimetableKempe;
        Move move;
        Swap swap;
        bestTimetableG = new Timetable(timetable);
        deltaLevel = 0.0001 * bestTimetableG.objFunc;
        long elapsedTime = 0;
        int iteration = 0, kKempe = timetable.timeSlots.size() / 4, iterNoMovement = 0,
                nMoves, nMovesMax = 18, nExamsMax = 600,
                nSwap, constSwap = 5, i, threadsKempe = 30;
        nMoves = timetable.data.examsNumber * nMovesMax / nExamsMax;
        if (nMoves < 5)
            nMoves = 5;
        if (nMoves > 18)
            nMoves = 18;
        System.out.println("nMoves: " + nMoves);
        double level, initialLevel = timetable.objFunc, p;
        double reductionConstInitial = timetable.data.examsNumber * 0.0000006513 - 0.00004432;
        if (reductionConstInitial < 0.00005)
            reductionConstInitial = 0.00005;
//        double reductionConstInitial = 0.001;
        double reductionConst = reductionConstInitial;

        System.out.println("Reduction Const: " + reductionConst);
        level = initialLevel;

        while (elapsedTime < timer) {
            tempTimetable = new Timetable(timetable);
            switch (ThreadLocalRandom.current().nextInt(nMoves)) {
                case 0:
                    move = tempTimetable.generatesNeighbourMovingExamWithKempe();
                    if (move != null) {
                        tempTimetable.moveExamWithoutConflicts(move);
                        if (updateBest(tempTimetable, "exam move 1")){
                            reductionConst *= 1.001;
                        }
                    } else
                    if (updateBest(tempTimetable, "kempe simple 1")){
                        reductionConst *= 1.001;
                    }
                    break;
                case 1:
                    for (i = 0; i < threadsKempe; i++)
                        ilskt.add(new GreatDeluge.GDKempeThread(tempTimetable, kKempe));

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
                        reductionConst *= 1.001;
                    }
                    break;
                case 2:
                    swap = tempTimetable.generatesNeighbourSwappingExam();
                    tempTimetable.doSwap(swap);
                    if (updateBest(tempTimetable, "exam swap 1")) {
                        reductionConst *= 1.001;
                    }
                    break;
                case 3:
                    for (i = 0; i < 2; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 2")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 4:
                    for (i = 0; i < 3; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 3")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 5:
                    for (i = 0; i < 4; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 4")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 6:
                    for (i = 0; i < 5; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 5")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 7:
                    for (i = 0; i < 6; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 6")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 8:
                    for (i = 0; i < 7; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 7")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 9:
                    for (i = 0; i < 8; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 8")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 10:
                    for (i = 0; i < 9; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 9")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 11:
                    for (i = 0; i < 10; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 10")) {
                            reductionConst *= 1.01;
                        }
                    }
                    break;
                case 12:
                    for (i = 0; i < 11; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 11")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 13:
                    for (i = 0; i < 12; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 12")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 14:
                    for (i = 0; i < 13; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 13")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 15:
                    for (i = 0; i < 14; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 14")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 16:
                    for (i = 0; i < 15; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap 15")) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
                case 17:
                    nSwap = ThreadLocalRandom.current().nextInt(constSwap) + 16;
                    for (i = 0; i < nSwap; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        if (updateBest(tempTimetable, "exam swap " + nSwap)) {
                            reductionConst *= 1.001;
                        }
                    }
                    break;
            }

            if (timetable.objFunc > level && tempTimetable.objFunc > level) {
                iterNoMovement++;
            } else if (tempTimetable.objFunc < level || tempTimetable.objFunc < timetable.objFunc){
                timetable = new Timetable(tempTimetable);
                iterNoMovement = 0;
            } else
                iterNoMovement++;

            if(iterNoMovement >= 3000){
                timetable = new Timetable(bestTimetableG);
//                countReset++;
                reductionConst = reductionConstInitial;
                iterNoMovement = 0;
                System.out.println("Reset");
                System.out.println(" |-> Level: " + level / timetable.data.studentsNumber);
                System.out.println(" |-> Actual OF temp: " + tempTimetable.objFunc / timetable.data.studentsNumber);
                System.out.println(" |-> Actual OF: " + timetable.objFunc / timetable.data.studentsNumber);
                System.out.println();
//                if (countReset == 2) {
//                    reductionConst /= 2;
//                    countReset = 0;
//                    System.out.println("Slowly Baby!!1!1!");
//                    System.out.println(" |-> Reduction Const: " + reductionConst);
//                }
            }

            level -= (level - bestTimetableG.objFunc) * reductionConst;
            if (level - bestTimetableG.objFunc < deltaLevel) {
//                reductionConst *= 1.05;
                p = ThreadLocalRandom.current().nextDouble();
                level = bestTimetableG.objFunc + (initialLevel - level) * p;
                while (level < timetable.objFunc) {
                    p = ThreadLocalRandom.current().nextDouble();
                    level = bestTimetableG.objFunc + (initialLevel - level) * p;
                }
                System.out.println();
                System.out.println("New Level: " + level / timetable.data.studentsNumber);
            }

            // Just for print
            if (iteration % 5000 == 0){
                System.out.println();
                System.out.println("Level: " + level / timetable.data.studentsNumber);
                System.out.println("Actual OF: " + timetable.objFunc / timetable.data.studentsNumber);
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
                System.out.println("Kempe Thread error");
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