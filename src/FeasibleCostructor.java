import java.util.*;

public class FeasibleCostructor {

	/**
	 * Exam
	 */
	static Exam exam;
	
	/**
	 * Exam Id, Number of Students Enrolled
	 */
	static int examId, examIdSlot;
	
	/**
	 * Flag for conflict in graph coloring
	 */
	static boolean conflictFound;
	
	/**
	 * Array to sort based on degree of Graph coloring 
	 */
	static ArrayList<Integer> colored = new ArrayList<>();
	
	/**
	 * number of Vertex for each node
	 */
	static int degreeCounter = 0;
	
	/**
	 * Ordered Exam List based on Vertex number
	 */
	static TreeSet<Integer> treeMapExams = new TreeSet<>(new Comparator<Integer>() {
		@Override
		public int compare(Integer o1, Integer o2) {
			return o2.compareTo(o1);
		}
	});

	/**
	 * Ordered Exam List based on Vertex number
	 */
	static TreeSet<Integer> treeMapExamsRandom = new TreeSet<>(new Comparator<Integer>() {
		@Override
		public int compare(Integer o1, Integer o2) {
			if (Math.random() < 0.5)
				return -1;
			else
				return 1;
		}
	});

	public static Data makeFeasibleGraphColoring(Data data) {
    	
        int i,j;
        for (i = 1; i < data.examsNumber+1; i++) {
            for (j = 1; j < data.examsNumber+1; j++)
            	if (data.conflictExams[i][j] > 0)
            		degreeCounter++;
            exam = data.examsList.get(i-1);
            exam.setConnectedExamsNumber(degreeCounter);
            treeMapExams.add(i);
            degreeCounter = 0;
        }
        
        i = 0;
        while (!treeMapExams.isEmpty()) {
        	examId = treeMapExams.pollFirst();
        	if (!colored.contains(examId)) {
				while (i >= data.timeSlots.size())
					data.timeSlots.add(new ArrayList<Integer>());
				data.timeSlots.get(i).add(examId);
				colored.add(examId);
				for (j = 1; j < data.examsNumber + 1; j++) {
					if (data.conflictExams[examId][j] == 0) {
						if (!colored.contains(j)) {

							Iterator<Integer> iterator = data.timeSlots.get(i).iterator();
							conflictFound = false;
							while (iterator.hasNext() && !conflictFound) {
								examIdSlot = iterator.next();
								if (data.conflictExams[examIdSlot][j] > 0)
									conflictFound = true;
							}

							if (!conflictFound) {
//            				data.timeSlots.get(i).add(data.examsMap.get(j));
								data.timeSlots.get(i).add(j);
								colored.add(j);
							}
						}
					}
				}
				if (!data.timeSlots.get(i).isEmpty())
					i++;
			}
        }
        
        return data;
    }

	public static Data makeFeasibleGraphGreedy(Data data) {

		int tempSlot, colorCounter, colorCounterMin, i, j;
		boolean conflict;
		for ( i = 0; i < data.examsList.size() ; i++ ) {
			treeMapExamsRandom.add(data.examsList.get(i).getId());
		}

		examId = treeMapExamsRandom.pollFirst();
		data.timeSlots.add(new ArrayList<>());
		data.timeSlots.get(0).add(examId);

		while (!treeMapExamsRandom.isEmpty()) {
			examId = treeMapExamsRandom.pollFirst();
			colorCounterMin = Integer.MAX_VALUE;
			tempSlot = -1;
			for ( i = 0; i < data.timeSlots.size(); i++ ) {
				conflict = false;
				colorCounter = 0;
				for ( j = 0; j < data.timeSlots.get(i).size() && !conflict; j++ ) {
					colorCounter++;
					if ( data.conflictExams[examId][data.timeSlots.get(i).get(j)] > 0 )
						conflict = true;
				}
				if (colorCounter < colorCounterMin && !conflict) {
					colorCounterMin = colorCounter;
					tempSlot = i;
				}
			}
			if (tempSlot == -1 && data.timeSlots.size() < data.timeSlotsNumber) {
				tempSlot = i;
				data.timeSlots.add(new ArrayList<>());
			}
			if (tempSlot == -1)
				tempSlot = (int) Math.floor(Math.random() * (data.timeSlotsNumber));
			data.timeSlots.get(tempSlot).add(examId);
		}

		return data;
	}

	public static Data makeFeasibleStudentBased(Data data) {
		
		int i, j, a, b;
		
		colored.clear();
		
		//Lista provvisoria di esami in conflitto
		ArrayList<Integer> conflictExams = new ArrayList<>();
		
		//Creo gli slot
		for(i = 0; i < data.gettimeSlotsNumber(); i++) {
			data.timeSlots.add(new ArrayList<>());
		}
		
		//Scorro la matrice STUDENTE/ESAMI; per ogni studente scorro la lista dei suoi esami e li metto ognuno in un timeslot diverso.
		//Se un esame è già stato inserito lo salto. Se nello slot in cui sto per mettere un esame ne esiste almeno uno con cui è in conflitto,
		//lo aggiungo alla lista provvisoria conflictExams. Alla fine di ogni iterazione ricontrollo la lista e inserisco nello slot corrente 
		//tutti gli esami in lista che non hanno conflitti in quello slot.
		for(i = 0; i < data.getConflicts().size(); i++)
		{
			a = 0;
			for(j = 0; j < data.getConflicts().get(i).size(); j++)
			{
				if(!colored.contains(data.conflicts.get(i).get(j)))
				{
					Iterator<Integer> iterator = data.timeSlots.get(a).iterator();
					conflictFound = false;
					while (iterator.hasNext() && !conflictFound) {
						examIdSlot = iterator.next();
						if (data.conflictExams[examIdSlot][data.conflicts.get(i).get(j)] > 0)
							conflictFound = true;
					}
					if (!conflictFound) {
         				data.timeSlots.get(a).add(data.conflicts.get(i).get(j));
						colored.add(data.conflicts.get(i).get(j));
						
						//Controllo eventuali esami compatibili tra quelli saltati precedentemente.
						for(b = 0; b < conflictExams.size(); b++)
						{
							Iterator<Integer> iterator1 = data.timeSlots.get(a).iterator();
							conflictFound = false;
							while (iterator1.hasNext() && !conflictFound) {
								examIdSlot = iterator1.next();
								if (data.conflictExams[examIdSlot][conflictExams.get(b)] > 0)
									conflictFound = true;
							}
							//Se non ci sono conflitti, li inserisco e aggiorno la lista.
							if(!conflictFound)
							{
								data.timeSlots.get(a).add(conflictExams.get(b));
								colored.add(conflictExams.get(b));
								conflictExams.remove(b);
							}
						}
						a++;
					}
					else{
						conflictExams.add(data.conflicts.get(i).get(j));
					}	
				}
			}
		}
		
		//Se ci sono ancora esami non assegnati nella lista conflictExams, scorro gli slot e inserisco gli esami rimanenti dove non 
		//ci sono conflitti.
		for(i = 0; i < data.gettimeSlotsNumber() && !conflictExams.isEmpty(); i++)
		{
			for(j = 0; j < conflictExams.size(); j++)
			{
				if(!colored.contains(conflictExams.get(j)))
				{
					Iterator<Integer> iterator = data.timeSlots.get(i).iterator();
					conflictFound = false;
					while (iterator.hasNext() && !conflictFound) {
						examIdSlot = iterator.next();
						if (data.conflictExams[examIdSlot][conflictExams.get(j)] > 0)
							conflictFound = true;
					}
					if (!conflictFound) {
         				data.timeSlots.get(i).add(conflictExams.get(j));
						colored.add(conflictExams.get(j));
						conflictExams.remove(j);
					}
				}
			}
		}
		
		return data;
	}
	
}
