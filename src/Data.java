import java.util.ArrayList;
import java.util.HashMap;

public class Data {

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
    public int timeSlotsNumber;

    /**
     * HashMap of Exams
     */
    public ArrayList<Exam> examsList;
    
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
     *  List of exams for each student
     */
    public ArrayList<ArrayList<Integer>> conflicts;
    
    /**
     * Objective function value ---> penalty to minimize
     */
    public double objFunc;

	public Data() {
		super();
		this.examsNumber 	= 0;
		this.studentsNumber = 0;
		this.timeSlotsNumber 	= 0;
		this.examsList  	= new ArrayList<>();
		this.conflictExams 	= null;
		this.studentExams	= null;
		this.timeSlots 		= new ArrayList<ArrayList<Integer>>();
		this.objFunc		= Integer.MAX_VALUE;
		this.conflicts		= new ArrayList<>();
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

	public int gettimeSlotsNumber() {
		return timeSlotsNumber;
	}

	public void settimeSlotsNumber(int timeSlotsNumber) {
		this.timeSlotsNumber = timeSlotsNumber;
	}

	public ArrayList<Exam> getExamsList() {
		return examsList;
	}

	public void setExamsMap(ArrayList<Exam> examsList) {
		this.examsList = examsList;
	}

	public ArrayList<ArrayList<Integer>> getConflicts() {
		return conflicts;
	}

	public void setConflicts(ArrayList<ArrayList<Integer>> conflicts) {
		this.conflicts = conflicts;
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

	public double getObjFunc() {
		return objFunc;
	}

	public void setObjFunc(int objFunc) {
		this.objFunc = objFunc;
	}

	public Exam getExam(int examId) {
		return this.examsList.get(examId);
	}

	public void addExam(Exam exam) {
		this.examsList.add(exam);
		return;
	}

}
