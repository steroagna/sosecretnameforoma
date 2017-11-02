package it.polito.oma.application.pojo;

import it.polito.oma.util.io.Data;

public class TimeSlots implements Data {

	public Integer timeSlots;
	
	@Override
	public void marshal(String rawData) throws Exception {
		timeSlots = Integer.parseInt(rawData);
		
	}


	@Override
	public String toString() {
		return ""+this.timeSlots;
	}
}
