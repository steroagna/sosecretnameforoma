import java.util.ArrayList;

public class Tools {
	
	/**
	 * Objective Function Calculator
	 */
	public static double ofCalculator(Timetable timetable, Data data) {
		
		double objectiveFunction = 0;
		int e1, e2;
		ArrayList<Integer> slot1, slot2 = new ArrayList<>();
		
		for (int i = 0 ; i < timetable.timeSlots.size()-1; i++) {
			slot1 = timetable.timeSlots.get(i);
			for (int j = 0 ; j < slot1.size(); j++) {
				e1 = slot1.get(j);
				for (int k = i+1 ; k < timetable.timeSlots.size() && k < i+6; k++) {
					slot2 = timetable.timeSlots.get(k);
					for (int l = 0 ; l < slot2.size(); l++) {
						e2 = slot2.get(l);
						if (e1 != e2) {
							if (timetable.G[e1][e2] > 0)
								objectiveFunction += Math.pow(2, (5 - (k-i))) * timetable.G[e1][e2];
						}
					}
				}
			}
		}
		
		return objectiveFunction / data.studentsNumber;
	}
	
	public boolean feasibilityChecker (Timetable timetable, Data data) {
		
		int examCounter = 0, e1 = 0, e2 = 0;
		boolean feasible = true;
		ArrayList<Integer> slot = new ArrayList<>();
		
		for (int i = 0 ; i < timetable.timeSlots.size() && feasible ; i++) {
			slot = timetable.timeSlots.get(i);
			for (int j = 0 ; j < slot.size() && feasible; j++) {
				e1 = slot.get(j);
				examCounter++;
				for (int k = 0 ; k < slot.size() && feasible; k++) {
					e2 = slot.get(k);
					if (e1 != e2) {
						if (timetable.G[e1][e2] > 0)
							feasible = false;
					}
				}
			}
		}
		
//		System.out.println("Conflict: " + e1 + " - " + e2);
//		System.out.println("Conflict in graph: " + timetable.conflictExams[e1][e2]);
//
//		System.out.println("Exam Number: " + examCounter);
//		System.out.println("Exam Number timetable: " + timetable.examsNumber);

		if (examCounter < data.examsNumber || feasible == false)
			return false;
		else
			return true;
	}
	
}
