package it.polito.oma.entities;

public class Student {

	public int intId;
	public String id;
	
	public int[] exams;
	
	public Student(String sid, int maxExams){
		this.intId = Integer.parseInt(sid.split("s")[0]);
		this.exams =  new int[maxExams];
	}
	
	public void addExam(int id) {
		this.exams[id] = 1;
	}
}
