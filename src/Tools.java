import java.util.ArrayList;

public class Tools {
	
	/**
	 * Objective Function Calculator
	 */
	public static double ofCalculator(Data data) {
		
		double val, objectiveFunction = 0;
		int e1, e2;
		ArrayList<Integer> slot1, slot2;
		
		for (int i = 0 ; i < data.timeSlots.size()-1; i++) {
			slot1 = data.timeSlots.get(i);
			for (int j = 0 ; j < slot1.size(); j++) {
				e1 = slot1.get(j);
				for (int k = i+1 ; k < data.timeSlots.size() && k < i+6; k++) {
					slot2 = data.timeSlots.get(k);
					for (int l = 0 ; l < slot2.size(); l++) {
						e2 = slot2.get(l);
						if (e1 != e2) {
							if (data.conflictExams[e1][e2] > 0) {
								val = Math.pow(2, (5 - (k - i))) * data.conflictExams[e1][e2];
								objectiveFunction += val;
							}
						}
					}
				}
			}
		}

		objectiveFunction = objectiveFunction / data.studentsNumber;
		data.objFunc = objectiveFunction;
//		System.out.println("Students :" + data.studentsNumber);
		return objectiveFunction;
	}
	
	public static boolean feasibilityChecker (Data data) {
		
		int examCounter = 0, e1 = 0, e2 = 0;
		boolean feasible = true;
		ArrayList<Integer> slot = new ArrayList<>();

		if (data.timeSlots.size() > data.timeSlotsNumber) {
			feasible = false;
			System.out.println("Too many timeslots used");
		}

		for (int i = 0 ; i < data.timeSlots.size() && feasible ; i++) {
			slot = data.timeSlots.get(i);
			for (int j = 0 ; j < slot.size() && feasible; j++) {
				e1 = slot.get(j);
				examCounter++;
				for (int k = 0 ; k < slot.size() && feasible; k++) {
					e2 = slot.get(k);
					if (e1 != e2) {
						if (data.conflictExams[e1][e2] > 0) {
							feasible = false;
							System.out.println("Conflict: " + e1 + " - " + e2);
//							System.out.println("Conflict in graph: " + data.conflictExams[e1][e2]);
						}
					}
				}
			}
		}

		if ( feasible == true && examCounter < data.examsNumber) {
			feasible = false;
			System.out.println("Exam Inserted: " + examCounter + " Exam Number form Data: " + data.examsNumber);
		}

		if (feasible == false)
			return false;
		else
			return true;
	}
	
}
