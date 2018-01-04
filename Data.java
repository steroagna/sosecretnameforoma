import java.util.ArrayList;
import java.util.HashMap;

public class Data {
	public int totalConflicts = 0;
    public int examsNumber = 0;
    public int studentsNumber = 0;
    public int slotsNumber;
    public HashMap<Integer, Exam> examsMap;
    int[][] conflictExams;
    int[][] studentExams;
    public ArrayList<ArrayList<Integer>> timeSlots;
    public int objFunc;

	public Data() {
		super();
		this.examsNumber 	= 0;
		this.studentsNumber = 0;
		this.slotsNumber 	= 0;
		this.examsMap  		= new HashMap<>();
		this.conflictExams 	= null;
		this.studentExams	= null;
		this.timeSlots 		= new ArrayList<ArrayList<Integer>>();
		this.objFunc		= Integer.MAX_VALUE;
	}

	public void addExam(Exam exam) {
		this.examsMap.put(exam.id, exam);
		return;
	}
}
