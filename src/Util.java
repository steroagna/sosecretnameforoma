import java.util.ArrayList;

public class Util {

	public static boolean feasibilityChecker (Timetable timetable, Data data) {
			
			int examCounter = 0, e1 = 0, e2 = 0;
			boolean feasible = true;
			ArrayList<Integer> slot = new ArrayList<Integer>();
	
			if (timetable.timeSlots.size() > data.slotsNumber) {
				feasible = false;
				System.out.println("Too many timeslots used");
			}
	
			for (int i = 0 ; i < timetable.timeSlots.size() && feasible ; i++) {
				slot = timetable.timeSlots.get(i);
				for (int j = 0 ; j < slot.size() && feasible; j++) {
					e1 = slot.get(j);
					examCounter++;
					for (int k = j+1 ; k < slot.size() && feasible; k++) {
						e2 = slot.get(k);
						if (e1 != e2) {
							if (data.conflictExams[e1][e2] > 0) {
								feasible = false;
								System.out.println("Conflict: " + e1 + " - " + e2);
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
