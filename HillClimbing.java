import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class HillClimbing {

    Timetable bestTimetableG;
    int count = 0, nMoves;

    public Timetable hillClimbing(Timetable timetable, long timer, long startTime) {

        Timetable tempTimetable;
        Move move;
        Swap swap;
        bestTimetableG = new Timetable(timetable);
        long elapsedTime = 0;
        int kKempe = timetable.timeSlots.size() / 4;
        int iteration = 0, iterNoMovement = 0;
        double level, initialLevel;
        double reductionLevel = timetable.data.examsNumber * 0.0000006513 - 0.00004432;
        if (reductionLevel < 0.00005)
        	reductionLevel = 0.00005;
//        double reductionLevel = 0.00005;
        System.out.println(reductionLevel);
        double p;
        initialLevel = timetable.objFunc;
        level = initialLevel;
        boolean improve = true;

        while (elapsedTime < timer) {
            tempTimetable = new Timetable(timetable);
            switch (ThreadLocalRandom.current().nextInt(5)) {
                case 0:
                    move = tempTimetable.generatesNeighbourMovingExamWithKempe();
                    if (move != null) {
                        tempTimetable.moveExamWithoutConflicts(move);
                        improve = updateBest(tempTimetable, "exam move");
                    } else
                    	improve = updateBest(tempTimetable, "kempe simple");
                    break;
                case 1:
                    swap = tempTimetable.generatesNeighbourSwappingExam();
                    tempTimetable.doSwap(swap);
                    improve = updateBest(tempTimetable, "exam swap");
                    break;
                case 2:
                    tempTimetable = tempTimetable.kempeChain(kKempe, 7);
                    improve = updateBest(tempTimetable, "kempe hard");
                    break;                    
                case 3:
                    tempTimetable.swapTimeslot();
                    improve = updateBest(tempTimetable, "timeslot swap");
                    break;
                case 4:
                    tempTimetable.manyMovesWorstExams(0.1);
                    improve = updateBest(tempTimetable, "worsts of random moves");
                    break;
                case 5:
	                for (int i = 0; i < 2; i++) {
		            	move = tempTimetable.generatesNeighbourMovingExamWithKempe();
		                if (move != null) {
		                    tempTimetable.moveExamWithoutConflicts(move);
		                    updateBest(tempTimetable, "exam move");
		                } else
		                    updateBest(tempTimetable, "kempe simple");
	                }
	                break;
	            case 6:
	            	for (int i = 0; i < 2; i++) {
		                swap = tempTimetable.generatesNeighbourSwappingExam();
		                tempTimetable.doSwap(swap);
		                updateBest(tempTimetable, "exam swap");
	            	}
	            	break;
	            case 7:
	            	for (int i = 0; i < 2; i++) {
		                tempTimetable.swapTimeslot();
		                updateBest(tempTimetable, "timeslot swap");
	            	}
	                break;
	            case 8:
	                for (int i = 0; i < 3; i++) {
		            	move = tempTimetable.generatesNeighbourMovingExamWithKempe();
		                if (move != null) {
		                    tempTimetable.moveExamWithoutConflicts(move);
		                    updateBest(tempTimetable, "exam move");
		                } else
		                    updateBest(tempTimetable, "kempe simple");
	                }
	                break;
	            case 9:
	            	for (int i = 0; i < 3; i++) {
		                swap = tempTimetable.generatesNeighbourSwappingExam();
		                tempTimetable.doSwap(swap);
		                updateBest(tempTimetable, "exam swap");
	            	}
	            	break;
	            case 10:
	            	for (int i = 0; i < 3; i++) {
		                tempTimetable.swapTimeslot();
		                updateBest(tempTimetable, "timeslot swap");
	            	}
	                break;
	            case 11:
	            	for (int i = 0; i < 2; i++) {
		                tempTimetable.manyMovesWorstExams(0.7);
		                updateBest(tempTimetable, "worsts of random moves");
	            	}
	                break;
            }           

            
            if (tempTimetable.objFunc < level || tempTimetable.objFunc < timetable.objFunc){// 
            	timetable = new Timetable(tempTimetable);
            	iterNoMovement = 0;
            }else
            	iterNoMovement++;
            
            if(iterNoMovement >= 3000){//
            	timetable = new Timetable(bestTimetableG);
            	System.out.println("----- Reset from best -----");
            	iterNoMovement = 0;
            }
                

            level -= (level - bestTimetableG.objFunc) * reductionLevel;
           	
            if (level - bestTimetableG.objFunc < 0.0001 * bestTimetableG.objFunc){
        		p = ThreadLocalRandom.current().nextDouble();
        		level += (initialLevel - level) * p;
        		System.out.println("New Level: " + level / timetable.data.studentsNumber);
        		if (reductionLevel < 0.001)
        			reductionLevel *= 1.01;
        	}

               
            if (iteration % 5000 == 0){
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
            bestTimetableG.setPenality();
            if (Main.debug) {
                System.out.println("OF --> " + timetable.objFunc / timetable.data.studentsNumber + " " + flag + " <=======");
            }
            return true;
        }
        return false;
    }
}
