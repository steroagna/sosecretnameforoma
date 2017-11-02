package it.polito.oma.entities;

public class Timetable {

	/* Timetable as int matrix where rows are exams and columns are timeslots. */
	public int[][] timetable; // per ora come matrice.
	
	public int E ;
	public int tmax;
	
	
	public Timetable(int E, int tmax) {
		this.timetable = new int[tmax][E+1];
		this.E  = E;
		this.tmax = tmax;
	}
	
	public void add(int slot, int examId) {
		this.timetable[slot][examId] = 1;
		
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		for(int i =0; i<tmax;i++) {
			sb.append("t="+i+" | ");
			for(int j=0;j<E;j++)
					sb.append(timetable[i][j] + " | ");
			sb.append('\n');
		}
		return sb.toString();
		
	}
}
