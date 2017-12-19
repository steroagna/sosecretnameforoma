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
		this.objFunc		= Integer.MAX_VALUE;
	}

	public void addExam(Exam exam) {
		this.examsMap.put(exam.getId(), exam);
		return;
	}

}
