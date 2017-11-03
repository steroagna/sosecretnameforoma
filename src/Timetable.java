import java.util.ArrayList;

public class Timetable {
	
	/**
     * List of Exams for each slot ---> final solution
     */
    public ArrayList<ArrayList<Integer>> timeSlots;

    /**
     * Objective function value ---> penalty to minimize
     */
    public double objFunc;

	public Timetable(ArrayList<ArrayList<Integer>> timeSlots, double objFunc) {
		super();
		this.timeSlots = timeSlots;
		this.objFunc = objFunc;
	}
    
    
}
