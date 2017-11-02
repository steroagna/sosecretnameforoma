package it.polito.oma.entities;

import java.util.HashMap;
import java.util.Map;

public class Exam {

	public int id;
	public Map<Integer,Object> students;
	public Map<Integer,Object> conflictExams; 
	public Map<Integer,Object> noConflictExams; 

	
	public Exam(int id) {
		this.id = id;
		this.students = new HashMap<Integer,Object>();
		this.conflictExams = new HashMap<Integer,Object>();
		this.noConflictExams = new HashMap<Integer,Object>();
	}

}
