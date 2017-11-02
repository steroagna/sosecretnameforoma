package it.polito.oma.util.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextFileReader <T extends Data> {

	private String filename;
	private DataFactory<T> dataFactory;
	
	public TextFileReader(String filename, DataFactory<T> dataFactory) {
		this.filename = filename;
		this.dataFactory = dataFactory;
	}
	
	public List<T> readLines() throws Exception {
		
		BufferedReader br = null;
		FileReader fr = null;
		List<T> outDataList = new ArrayList<T>();
		
		try {

			fr = new FileReader(filename);
			br = new BufferedReader(fr);

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				
				if(sCurrentLine.equals("\n")) continue;
				if(sCurrentLine.equals("")) continue;
				
				T data = dataFactory.newInstance();
				data.marshal(sCurrentLine);
				outDataList.add(data);
			}

		} catch (IOException e) {

			e.printStackTrace();

		} finally {
				if (br != null)
					br.close();

				if (fr != null)
					fr.close();
		}

		return outDataList;
	}
	
}
