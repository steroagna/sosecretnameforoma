import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


public class Population {

	public ArrayList<Timetable> population;
	public double bestOF;
	Timetable parent1, parent2;
	
	public Population(ArrayList<Timetable> population, double bestOF) {
		super();
		this.population = population;
		this.bestOF = bestOF;
		this.parent1 = null;
		this.parent2 = null;
	}

	public void chooseParents() {
		
		Random r1 = new Random();
		Random r2 = new Random();
		int p1 = 0, p2= 0;
		
		while (p1 == p2) {
			p1 = r1.nextInt(this.population.size());
			p2 = r2.nextInt(this.population.size());
		}
		try {
			this.parent1 = (Timetable) this.population.get(p1).clone();
			this.parent2 = (Timetable) this.population.get(p2).clone();
		}catch(CloneNotSupportedException c){
			System.out.println("Clonation failed");
		}
		
		return;
	}
	
	public Timetable copulate() {
		
		this.chooseParents();
		Timetable newGen = new Timetable(this.parent1.G, this.parent1.timeSlots.size());
		Timetable A, B;
		int examCounterPerColumn, i, j, index = 0, examCounterMax = 0;
		ArrayList<Integer> slot, selectedColumn = null;
		Iterator<ArrayList<Integer>> it;
		Iterator<Integer> itSlot, itSlotSelected;
		Integer exam;
		boolean found;
		Random r1 = new Random();
		int p1;
		
		for (i = 0; i < this.parent1.timeSlots.size(); i++) {
			if (i%2 == 0) {
				A = this.parent1;
				B = this.parent2;
			} else {
				B = this.parent1;
				A = this.parent2;
			}
			
			it = A.timeSlots.iterator();
			j = 0;
			while ( it.hasNext() ) {
				slot = it.next();
				itSlot = slot.iterator();
				examCounterPerColumn = 0;
				while ( itSlot.hasNext() ) {
					itSlot.next();
					examCounterPerColumn++;
				}
				if (examCounterPerColumn > examCounterMax) {
					examCounterMax = examCounterPerColumn;
					index = j;
					selectedColumn = A.timeSlots.get(j);
				}
				j++;
			}
			
			found = false;
			itSlotSelected = selectedColumn.iterator();
			while (itSlotSelected.hasNext()) {
				exam = itSlotSelected.next();
				it = B.timeSlots.iterator();
				while ( it.hasNext() && !found ) {
					slot = it.next();
					itSlot = slot.iterator();
					while ( itSlot.hasNext() && !found ) {
						if (itSlot.next() == exam) {
							itSlot.remove();
							found = true;
						}
					}
				}
			}
			
			A.timeSlots.set(index, new ArrayList<Integer>());
			newGen.timeSlots.add(selectedColumn);
			newGen.timeSlotsConflict.add(new ArrayList<Tuple>());
		}
		
		for (i = 0; i < this.parent1.timeSlots.size(); i++) {
			slot = this.parent1.timeSlots.get(i);
			itSlot = slot.iterator();
			while ( itSlot.hasNext() ) {
				p1 = r1.nextInt(this.parent1.timeSlots.size());
				newGen.addExam(p1,itSlot.next());
			}
		}
		
		return newGen;
	}

	public void updatePopulation(Timetable newGen) {
		
		Random r = new Random();
		int t;
		
		t = r.nextInt(this.population.size());
		this.population.set(t, newGen);
	}

}
