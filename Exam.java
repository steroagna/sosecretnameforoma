import java.util.ArrayList;

public class Exam {
    public int id;
    public int connectedExamsNumber;
	public int studentsEnrolled;
    public int slot;
    public ArrayList<Exam> conflicts = new ArrayList<>();
    public ArrayList<Integer> possibleSlots = new ArrayList<>();
    
    public Exam(int id, int studentsEnrolled) {
        this.id = id;
        this.studentsEnrolled = studentsEnrolled;
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

    public void setConflicts(ArrayList<Exam> conflicts) {
        this.conflicts = conflicts;
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

    public ArrayList<Exam> getConflicts() {
        return conflicts;
    }

    public ArrayList<Integer> getPossibleSlots() {
        return possibleSlots;
    }
}
