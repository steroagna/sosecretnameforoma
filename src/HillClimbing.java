import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class HillClimbing {

    Timetable bestTimetableG;
    int count = 0, nMoves;
    int costListMaxDim = 50;
    int examNumberMin = 70;
    int step = 10, actualList;

    public Timetable hillClimbing(Timetable timetable, long timer, long startTime) {

        Timetable tempTimetable = new Timetable(timetable);
        Move move;
        Swap swap;
        bestTimetableG = new Timetable(timetable);
        long elapsedTime = 0;
        int kKempe = timetable.timeSlots.size() / 4;
        int iteration = 0, actual, choice;
        actualList = 0;
        int costListSizeMax = examNumberMin * costListMaxDim / timetable.data.examsNumber;
        int[] costListSize = new int[step];
        ArrayList<ArrayList<Double>> penaltyLists = new ArrayList<>();
        
        for (int j = 0; j < step; j++) {
        	costListSize[j] = costListSizeMax;
        	costListSizeMax += costListSizeMax;
        	System.out.println("list " + (j+1) + " size: " + (costListSize[j])); 
        	penaltyLists.add(new ArrayList<Double>());
	        for (int i = 0; i < costListSize[j]; i++)
	            penaltyLists.get(j).add(timetable.objFunc);
        }
        
        ArrayList<Double> actualCostList = penaltyLists.get(actualList);
        while (elapsedTime < timer) {
        	tempTimetable = new Timetable(timetable);
        	choice =ThreadLocalRandom.current().nextInt(5);
        	if (count > 2*costListSizeMax) {
        		choice =ThreadLocalRandom.current().nextInt(12);
        	}
	        switch (choice) {
	            case 0:
	                move = tempTimetable.generatesNeighbourMovingExamWithKempe();
	                if (move != null) {
	                    tempTimetable.moveExamWithoutConflicts(move);
	                    updateBest(tempTimetable, "exam move");
	                } else
	                    updateBest(tempTimetable, "kempe simple");
	                break;
	            case 1:
	                swap = tempTimetable.generatesNeighbourSwappingExam();
	                tempTimetable.doSwap(swap);
	                updateBest(tempTimetable, "exam swap");
	                break;
	            case 2:
	                tempTimetable.swapTimeslot();
	                updateBest(tempTimetable, "timeslot swap");
	                break;
	            case 3:
	                tempTimetable = tempTimetable.kempeChain(kKempe, 7);
	                updateBest(tempTimetable, "kempe hard");
	                break;
	            case 4:
	                tempTimetable.manyMovesWorstExams(0.1);
	                updateBest(tempTimetable, "worsts of random moves");
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
        	
            if (count == 2*costListSize[actualList] && actualList < step - 1) {
            	
//            	for (int i = 0; i < costListSize[actualList]; i++)
//            		System.out.println("element list " +i+": " + actualCostList.get(i) / timetable.data.studentsNumber);
            	
            	count = 0;
            	actualList++;
            	actualCostList = penaltyLists.get(actualList);
            	System.out.println("list: " + (actualList+1));
            }

            actual = iteration % costListSize[actualList];
            if (tempTimetable.objFunc < actualCostList.get(actual) 
            		|| tempTimetable.objFunc <= timetable.objFunc) {
                timetable = new Timetable(tempTimetable);
            } else 
            	count++;

            for (int j = actualList; j < step; j++) {
            	actual = iteration % costListSize[j];
//            	if (tempTimetable.objFunc < penaltyLists.get(j).get(actual))
            		penaltyLists.get(j).set(actual, timetable.objFunc);
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
