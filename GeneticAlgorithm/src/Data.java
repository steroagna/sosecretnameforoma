
import java.util.ArrayList;
import java.util.HashMap;

public class Data {

	/**
     * Number of conflicts among exams
     */
	public int totalConflicts = 0;
	
	/**
     * Number of exams value
     */
    public int examsNumber = 0;
    
    /**
     * Number of students value
     */
    public int studentsNumber = 0;
    
    /**
     * Slot value
     */
    public int slotsNumber;

    /**
     * HashMap of Exams
     */
    public HashMap<Integer, Exam> examsMap;
    
    /**
     * Matrix of conflicts (EXAM/EXAM)
     */
    int[][] conflictExams;
    
    /**
     * Matrix of students (STUD/EXAM)
     */
    int[][] studentExams;
    
    /**
     * List of Exams for each slot ---> final solution
     */
    public ArrayList<ArrayList<Integer>> timeSlots;

    /**
     * Objective function value ---> penalty to minimize
     */
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

	public int getExamsNumber() {
		return examsNumber;
	}

	public void setExamsNumber(int examsNumber) {
		this.examsNumber = examsNumber;
	}

	public int getStudentsNumber() {
		return studentsNumber;
	}

	public void setStudentsNumber(int studentsNumber) {
		this.studentsNumber = studentsNumber;
	}

	public int getSlotsNumber() {
		return slotsNumber;
	}

	public void setSlotsNumber(int slotsNumber) {
		this.slotsNumber = slotsNumber;
	}

	public HashMap<Integer, Exam> getExamsMap() {
		return examsMap;
	}

	public void setExamsMap(HashMap<Integer, Exam> examsMap) {
		this.examsMap = examsMap;
	}


	public int[][] getConflictExams() {
		return conflictExams;
	}

	public void setConflictExams(int[][] conflictExams) {
		this.conflictExams = conflictExams;
	}

	public int[][] getStudentExams() {
		return studentExams;
	}

	public void setStudentExams(int[][] studentExams) {
		this.studentExams = studentExams;
	}

	public ArrayList<ArrayList<Integer>> getTimeSlots() {
		return timeSlots;
	}

	public void setTimeSlots(ArrayList<ArrayList<Integer>> timeSlots) {
		this.timeSlots = timeSlots;
	}

	public int getObjFunc() {
		return objFunc;
	}

	public void setObjFunc(int objFunc) {
		this.objFunc = objFunc;
	}

	public Exam getExam(int examId) {
		if (this.examsMap.containsKey(examId))
			return this.examsMap.get(examId);
		else
			return null;
	}

	public void addExam(Exam exam) {
		this.examsMap.put(exam.getId(), exam);
		return;
	}
	
}
