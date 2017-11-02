package it.polito.oma.application.pojo;

import it.polito.oma.util.io.Data;

public class StudentExam implements Data{

	public String idStudent;
	public Integer intIdStudent;
	public Integer idExam;
	
	@Override
	public void marshal(String rawData) throws Exception {
		
		String[] ss = rawData.split(" ");
		
		if(ss.length<2) 
			throw new Exception();
			
		this.idStudent = ss[0];
		String[] ssIdStudent = this.idStudent.split("s");
		
		this.intIdStudent = Integer.parseInt(ssIdStudent[1]);
		
		this.idExam = Integer.parseInt(ss[1]);
		
	}
	

	@Override
	public String toString() {
		return this.idStudent+" "+this.idExam;
	}
}
