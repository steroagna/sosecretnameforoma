package it.polito.oma.application.pojo;

import it.polito.oma.util.io.Data;

public class ExamTotStudent implements Data {

	public Integer idExam ;
	public Integer totStudent;
	
	@Override
	public void marshal(String rawData) throws Exception {
		String[] ss = rawData.split(" ");
		
		if(ss.length<2) 
			throw new Exception("Wrong format!");
		
		this.idExam = Integer.parseInt(ss[0]);
		this.totStudent = Integer.parseInt(ss[1]);
		
	}

	@Override
	public String toString() {
		return this.idExam+" "+this.totStudent;
	}
	
}
