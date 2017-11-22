import java.util.ArrayList;
import java.util.Comparator;

public class Exam implements Comparable {
    private int id;
    private int conflictsNumber;
	private int studentsEnrolled;
    private int slot;
    private boolean marked;
    private ArrayList<Exam> conflicts = new ArrayList<>();
    private ArrayList<Integer> possibleSlots = new ArrayList<>();
    
    public Exam(int id, int studentsEnrolled) {
        this.id = id;
        this.studentsEnrolled = studentsEnrolled;
        this.marked = false;
    }

    @Override
    public int compareTo(Object o) throws ClassCastException {
        if (!(o instanceof Exam))
            throw new ClassCastException("A Exam object expected.");
        String a = String.valueOf(((Exam) o).conflictsNumber);
        String b = String.valueOf(this.conflictsNumber);
        String pre = "0";
        if ( a.length() > b.length()) {
            for (int i = 0; i < a.length() - b.length(); i++)
                b = pre + b;
        } else if ( b.length() > a.length()) {
            for (int i = 0; i < b.length() - a.length(); i++)
                a = pre + a;
        }
        String s = a + "_" + ((Exam) o).id;
        String s1 = b + "_" + this.id;
        return s.compareTo(s1);
    }

    public int getConnectedExamsNumber() {
		return conflictsNumber;
	}

	public void setConflictsNumber(int conflictsNumber) {
		this.conflictsNumber = conflictsNumber;
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

    public void mark() {
        this.marked = true;
    }

    public boolean isMarked() {
        return this.marked;
    }
}
