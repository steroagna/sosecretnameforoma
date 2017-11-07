import java.util.*;

public class FeasibleCostructor {

	/**
	 * Exam
	 */
	static Exam exam;
	
	/**
	 * Exam Id, Number of Students Enrolled
	 */
	static int examId, examIdSlot, conflictsNumber;
	
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

	public static Data makeInitialGraphColoring(Data data) {

		/**
		 * Conto quanti conflitti hanno gli esami e ordino in modo decrescente
		 */
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

		/**
		 * Estraggo in ordine gli esami partendo da quello con più conflitti fin quando non li ho considerati tutti
		 * Coloro l'estratto se non lo è già (--> inserimento in colored e timeslot) altrimenti passo al prossimo
		 * Per ogni esame che non è in conflitto con il selezionato controllo:
		 * - che non sia già colorato
		 * - che non abbia conflitti con quelli del colore attuale (iterator su timeslot)
		 * se passa questi test lo inserisco
		 *
		 * Passo al prossimo esame
		 */
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

	public static Data makeInitialGraphGreedy(Data data) {

		int tempSlot, colorCounter, colorCounterMin, i, j;
		boolean conflict, notFeasible = false;
		conflictsNumber = 0;

		/**
		 * creo treemap ordinata casualmente
		 */
		for (i = 0; i < data.examsList.size(); i++) {
			treeMapExamsRandom.add(data.examsList.get(i).getId());
		}

		/**
		 * inserimento primo elemento in primo timeslot
		 */
		examId = treeMapExamsRandom.pollFirst();
		data.timeSlots.add(new ArrayList<>());
		data.timeSlots.get(0).add(examId);

		/**
		 * inserimento altri elementi
		 */
		while (!treeMapExamsRandom.isEmpty()) {
			examId = treeMapExamsRandom.pollFirst();
			colorCounterMin = Integer.MAX_VALUE;
			tempSlot = -1;
			/**
			 * Conto tra i timeslot quello con minore numero di esami
			 * e che non abbia conflitti con l'attuale esame in considerazione
			 */
			for (i = 0; i < data.timeSlots.size(); i++) {
				conflict = false;
				colorCounter = 0;
				for (j = 0; j < data.timeSlots.get(i).size() && !conflict; j++) {
					colorCounter++;
					int examId2 = data.timeSlots.get(i).get(j);
					if (data.conflictExams[examId][examId2] > 0) {
						conflict = true;
					}
				}
				if (colorCounter < colorCounterMin && !conflict) {
					colorCounterMin = colorCounter;
					tempSlot = i;
				}
			}
			/**
			 * se non sono riuscito a trovare uno slot senza conflitti
			 * ma ho ancora slot disponibili lo inserisco in un nuovo slot
			 */
			if (tempSlot == -1 && data.timeSlots.size() < data.timeSlotsNumber) {
				tempSlot = i;
				data.timeSlots.add(new ArrayList<>());
			}

			/**
			 * se non sono riuscito a trovare uno slot senza conflitti
			 * ma ho non ho più slot disponibili inserisco l'esame a caso in uno dei timeslot
			 * e aggiungo l'esame a una hashmap di esami che hanno conflitti
			 * per il timeslot selezionato aggiorno i conflitti per ogni esame
			 * e aggiorno il numero di conflitti totale
			 * che servirà dopo per rendere feasible la sol
			 */
			if (tempSlot == -1) {
				tempSlot = (int) Math.floor(Math.random() * (data.timeSlotsNumber));
				data.getExam(examId).setSlot(tempSlot);
				data.conflictList.add(examId);
				for (j = 0; j < data.timeSlots.get(tempSlot).size(); j++) {
					int examId2 = data.timeSlots.get(tempSlot).get(j);
					//conflitto
					if (data.conflictExams[examId][examId2] > 0) {
						if (!data.conflictList.contains((examId2)))
							data.conflictList.add(examId2);
						data.getExam(examId).conflicts++;
						data.getExam(examId2).conflicts++;
						conflictsNumber++;
					}
				}
				notFeasible = true;
			}
			data.getExam(examId).setSlot(tempSlot);
			data.timeSlots.get(tempSlot).add(examId);
		}

		return data;
	}

	public static Data makeInitialRandom(Data data) {

		int tempSlot, colorCounter, colorCounterMin, i, j;
		boolean conflict, notFeasible = false;
		conflictsNumber = 0;

		/**
		 * creo treemap ordinata casualmente
		 */
		for ( i = 0; i < data.examsList.size() ; i++ ) {
			treeMapExamsRandom.add(data.examsList.get(i).getId());
		}

		/**
		 * creo timeslots
		 */
		for ( i = 0; i < data.timeSlotsNumber; i++ )
			data.timeSlots.add(new ArrayList<>());

		/**
		 * inserimento altri elementi
		 */
		while (!treeMapExamsRandom.isEmpty()) {
			examId = treeMapExamsRandom.pollFirst();
			tempSlot = (int) Math.floor(Math.random() * (data.timeSlotsNumber));
			data.getExam(examId).setSlot(tempSlot);
			for ( j = 0; j < data.timeSlots.get(tempSlot).size(); j++ ) {
				int examId2 = data.timeSlots.get(tempSlot).get(j);
				//conflitto
				if (data.conflictExams[examId][examId2] > 0) {
					data.getExam(examId).conflicts++;
					data.getExam(examId2).conflicts++;
					conflictsNumber++;
				}
			}
			data.timeSlots.get(tempSlot).add(examId);
		}

//		/**
//		 * Print timeslot of Exams
//		 */
//		 ArrayList<Integer> slot = new ArrayList<>();
//
//		i = 0;
//		while(i < data.timeSlots.size()) {
//			if (!(slot = data.timeSlots.get(i)).isEmpty()) {
//			slot = data.timeSlots.get(i);
//			System.out.print("Slot " + i + ": \n");
//			for (j = 0 ; j < slot.size() ; j ++) {
//				int e = slot.get(j);
//				System.out.print("Esame:" + e + " - conflitti: " + data.getExam(e).conflicts + "\n");
//			}
//			System.out.println();
//			}
//			i++;
//		}

		data = makeFeasibleTabu(data);

		return data;
	}

	/**
	 * Sposta esami con conflitti finchè non trova la feasible
	 * @param data
	 * @return data
	 */
	public static Data makeFeasibleTabu(Data data) {

		int timeslot, i, conflictsNumberMin = conflictsNumber, cyclecounter = 0, cyclechecker;
		Move moveTemp, actualMove = new Move(0,0, Integer.MAX_VALUE);
		ArrayList<Move> tabuList = new ArrayList<>();

		while ( conflictsNumber > 0 ) {
			actualMove.conflicts = Integer.MAX_VALUE;
			/**
			 * creo vicinato
			 */
			for (i = 0; i < data.examsNumber/2 && conflictsNumber > 0; i++ ) {
				/**
				 * se non ho trovato cicli prendo esame a caso da quelli che hanno un conflitto
				 * altrimenti provo a spostare uno che non ne ha per movimentare la soluzione
				 */
//				if (cyclecounter < 3 ) {
//					int random = (int) Math.floor((Math.random() * data.conflictList.size()));
//					int id = data.conflictList.get((random));
//					exam = data.getExam(id);
//				} else {
					int random = (int) Math.floor((Math.random() * data.examsNumber)) + 1;
					exam = data.getExam(random);
//				}

				/**
				 * timeslot a caso per inserire
				 */
				timeslot = (int) Math.floor(Math.random() * data.timeSlotsNumber);

				if (timeslot != exam.slot) {
					/**
					 * provo se la mossa migliora la soluzione
					 */
					moveTemp = evaluateMove(data, exam, timeslot);
					/**
					 * se applico la tabusearch
					 * controllo che la mossa non sia nella tabuList
					 * e valuto altre mosse
					 */
					if (!tabuList.contains(moveTemp) || (moveTemp.conflicts < conflictsNumberMin - 1)) {
						if (moveTemp.conflicts < conflictsNumberMin - 1) {
							conflictsNumberMin = moveTemp.conflicts;
						}
						if (moveTemp.conflicts <= actualMove.conflicts) {
							actualMove = moveTemp;
						}
					}
				} else {
					i--;
				}
			}

			if (tabuList.size() >= 7)
				tabuList.remove(0);

			cyclechecker = conflictsNumber;

			if (!tabuList.contains(actualMove))
				tabuList.add(new Move(actualMove.exam, exam.slot, 0));
			moveExam(data, actualMove);

			if (cyclechecker != conflictsNumber)
				cyclecounter = 0;
			else
				cyclecounter++;

//			//stampa tabulist
//			for (i = 0; i < tabuList.size(); i++)
//				System.out.println("tabumove exam " + tabuList.get(i).exam + " to timeslot --> " + tabuList.get(i).timeslot);
		}

		return data;
	}

	public static Move evaluateMove(Data data, Exam exam, int timeslot) {

		int conflictsNumberTemp = conflictsNumber, j, examId2;
		int examId = exam.getId();

		conflictsNumberTemp = conflictsNumberTemp - exam.conflicts;

		for ( j = 0; j < data.timeSlots.get(timeslot).size(); j++ ) {
			examId2 = data.timeSlots.get(timeslot).get(j);
			//conflitto
			if (data.conflictExams[examId][examId2] > 0) {
				conflictsNumberTemp++;
			}
		}
//		System.out.println("Conflitti mosse provv: " + conflictsNumberTemp);

		return new Move(examId, timeslot, conflictsNumberTemp);

	}

	public static void moveExam(Data data, Move move) {

		int j;
		Integer examId, examId2;
		int slotPartenza = data.getExam(move.exam.intValue()).getSlot();
		boolean foundConflict = false;
		conflictsNumber = move.conflicts;

//		System.out.println("esame da spostare: " + move.exam);

		/*svuoto conflict list dell'esame che sto spostando*/
		data.timeSlots.get(slotPartenza).remove(move.exam);
		data.getExam(move.exam.intValue()).conflicts = 0;
		data.conflictList.remove(move.exam);

		for ( j = 0; j < data.timeSlots.get(slotPartenza).size(); j++ ) {
			examId = data.timeSlots.get(slotPartenza).get(j);
			//conflitto --> rimuovo
			if (data.conflictExams[move.exam.intValue()][examId] > 0) {
				data.getExam(examId).conflicts--;
				if (data.getExam(examId).conflicts == 0 )
					data.conflictList.remove(examId);
//				System.out.println("esame in conflitto: " + examId + " slot di partenza: " + slotPartenza);
			}
		}

		for ( j = 0; j < data.timeSlots.get(move.timeslot).size(); j++ ) {
			examId2 = data.timeSlots.get(move.timeslot).get(j);
			//conflitto
			if (data.conflictExams[move.exam.intValue()][examId2] > 0) {
				data.getExam(move.exam.intValue()).conflicts++;
				data.getExam(examId2).conflicts++;
				if (!data.conflictList.contains(examId2))
					data.conflictList.add(examId2);
				foundConflict = true;
//				System.out.println("esame in conflitto: " + examId2 + " slot di arrivo: " + move.timeslot);
			}
		}
		if (foundConflict)
			data.conflictList.add(move.exam);

//		System.out.println("Move: " + move.exam + " ---> " + move.timeslot);

		data.timeSlots.get(move.timeslot).add(move.exam.intValue());
		data.getExam(move.exam.intValue()).setSlot(move.timeslot);
//		System.out.println("Conflitti: " + conflictsNumber);

//		for ( j = 0; j < data.timeSlots.get(slotPartenza).size(); j++ ) {
//			examId = data.timeSlots.get(slotPartenza).get(j);
//			//conflitto --> rimuovo
//			if (move.exam == examId) {
//				System.out.println("Errore in timeslot: due esami con stesso id" + examId + " - " + move.exam);
//			}
//		}

		return;
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
		//Se un esame � gi� stato inserito lo salto. Se nello slot in cui sto per mettere un esame ne esiste almeno uno con cui � in conflitto,
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
