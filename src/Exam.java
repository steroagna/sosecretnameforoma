import java.util.ArrayList;

public class Exam {
    public int id;
    public int connectedExamsNumber;
	public int studentsEnrolled;
    public int slot;
    public int conflicts;
    public ArrayList<Integer> conflictList = new ArrayList<>();
    public ArrayList<Integer> possibleSlots = new ArrayList<>();
    
    public Exam(int id, int studentsEnrolled) {
        this.id = id;
        this.studentsEnrolled = studentsEnrolled;
        this.conflicts = 0;
    }

    public int getConnectedExamsNumber() {
		return connectedExamsNumber;
	}

	public void setConnectedExamsNumber(int connectedExamsNumber) {
		this.connectedExamsNumber = connectedExamsNumber;
	}

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void setConflicts(ArrayList<Integer> conflictList) {
        this.conflictList = conflictList;
    }

    public void setPossibleSlots(ArrayList<Integer> possibleSlots) {
        this.possibleSlots = possibleSlots;
    }

    public int getId() {
        return id;
    }

    public int getStudentsEnrolled() {
        return studentsEnrolled;
    }

    public int getSlot() { return slot; }

    public ArrayList<Integer> getConflicts() {
        return conflictList;
    }

    public ArrayList<Integer> getPossibleSlots() {
        return possibleSlots;
    }
}
