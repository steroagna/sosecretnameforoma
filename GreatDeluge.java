import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GreatDeluge {

    Timetable bestTimetableG;

    public Timetable greatDeluge(Timetable timetable, long timer, long startTime) {
        Timetable tempTimetable;
        Move move;
        Swap swap;
        bestTimetableG = new Timetable(timetable);
        long elapsedTime = 0;
        int iteration = 0, kKempe = timetable.timeSlots.size() / 4, iterNoMovement = 0, nMoves = 8;
        double level, initialLevel = timetable.objFunc, p;
        double reductionLevel = timetable.data.examsNumber * 0.0000006513 - 0.00004432;
        if (reductionLevel < 0.00005)
        	reductionLevel = 0.00005;
        level = initialLevel;

        while (elapsedTime < timer) {
            tempTimetable = new Timetable(timetable);
            switch (ThreadLocalRandom.current().nextInt(nMoves)) {
                case 0:
                    swap = tempTimetable.generatesNeighbourSwappingExam();
                    tempTimetable.doSwap(swap);
                    updateBest(tempTimetable, "exam swap 1");
                    break;
                case 1:
                    tempTimetable = tempTimetable.kempeChain(kKempe, 7);
                    updateBest(tempTimetable, "kempe hard 1");
                    break;
                case 2:
	            	for (int i = 0; i < 2; i++) {
		                swap = tempTimetable.generatesNeighbourSwappingExam();
		                tempTimetable.doSwap(swap);
		                updateBest(tempTimetable, "exam swap 2");
	            	}
	            	break;
	            case 3:
	            	for (int i = 0; i < 3; i++) {
		                swap = tempTimetable.generatesNeighbourSwappingExam();
		                tempTimetable.doSwap(swap);
		                updateBest(tempTimetable, "exam swap 3");
	            	}
	            	break;
                case 4:
                    for (int i = 0; i < 4; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        updateBest(tempTimetable, "exam swap 4");
                    }
                    break;
                case 5:
                    for (int i = 0; i < 5; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        updateBest(tempTimetable, "exam swap 5");
                    }
                    break;
                case 6:
                    for (int i = 0; i < 6; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        updateBest(tempTimetable, "exam swap 6");
                    }
                    break;
                case 7:
                    for (int i = 0; i < 7; i++) {
                        swap = tempTimetable.generatesNeighbourSwappingExam();
                        tempTimetable.doSwap(swap);
                        updateBest(tempTimetable, "exam swap 7");
                    }
                    break;
            }

            if (tempTimetable.objFunc < level || tempTimetable.objFunc < timetable.objFunc){//
            	timetable = new Timetable(tempTimetable);
            	iterNoMovement = 0;
            } else
            	iterNoMovement++;

            if(iterNoMovement >= 1000){
                timetable = new Timetable(bestTimetableG);
            	iterNoMovement = 0;
                System.out.println("Reset");
            }

            if (iteration % 2 == 0)
                level -= (level - bestTimetableG.objFunc) * reductionLevel;
            if (level - bestTimetableG.objFunc < 0.0001 * bestTimetableG.objFunc){
        		p = ThreadLocalRandom.current().nextDouble();
        		level += (initialLevel - level) * p;
                System.out.println("New Level: " + level / timetable.data.studentsNumber);
                if (reductionLevel < 0.001)
        			reductionLevel *= 1.01;
        	}

        	// Just for print
            if (iteration % 50000 == 0){
                System.out.println("Level: " + level / timetable.data.studentsNumber);
                System.out.println("Actual OF: " + timetable.objFunc / timetable.data.studentsNumber);
            }

            iteration++;
            elapsedTime = System.currentTimeMillis() - startTime;
        }

        return bestTimetableG;
    }

    public boolean updateBest(Timetable timetable, String flag) {
        if (timetable.objFunc < bestTimetableG.objFunc) {
            bestTimetableG = new Timetable(timetable);
            bestTimetableG.toString(Main.filename);
            if (Main.debug) {
                System.out.println("OF --> " + timetable.objFunc / timetable.data.studentsNumber + " " + flag);
            }
            return true;
        }
        return false;
    }
}
