package it.polito.oma.util.io;

/* 
 * Interface which describes methods need to
 * transform content file in a data structure. 
 * */
public interface Data {

	public void marshal(String rawData)  throws Exception;
}
