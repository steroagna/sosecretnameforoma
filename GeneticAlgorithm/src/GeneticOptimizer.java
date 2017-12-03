import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public class GeneticOptimizer {

	/**
	 * Internal class which rappresent chromosomes population.
	 * */
	static class Population{
		PriorityQueue<Timetable> orderedChromosomes;
		int size;
		
		Population(){
			orderedChromosomes = new PriorityQueue<Timetable>();
		}
		
		/* Now take first best chromosomes of queue (always). */
		List<Timetable> selects(double percentage){
			
			List<Timetable> list = new ArrayList<Timetable>(this.orderedChromosomes);
			List<Timetable> out = new ArrayList<Timetable>();
			Random rand = new Random();
			ArrayList<Integer> selected = new ArrayList<Integer>();
			int i=0;
			for(Iterator<Timetable> it =list.iterator();it.hasNext()&&i<this.orderedChromosomes.size()*percentage;i+=1,it.next()) {
				//out.add(list.get(i));
				//selected.add(i);
				//out.add(list.get(list.size()/2+i));
				//out.add(list.get(list.size()-i-1));
				//for(;;) {
					Integer s = rand.nextInt(list.size());
					if(selected.contains(s)) continue;
					out.add(list.get((int)s));
					selected.add(s);
					i--;
				//	break;
				//}
			}
			
			return out;
		}
		
		/* Get best chromosome */
		Timetable getBestChromosome() {
			return this.orderedChromosomes.peek();
		}
		
		/* Get worst chromosome */
		Timetable getWorstChromosome() {
			return new ArrayList<Timetable>(this.orderedChromosomes).get(this.orderedChromosomes.size()-1);
		}
		
		/* Now take first best chromosomes of queue (always). */
		void replace(List<Timetable> newIndividuals){
			List<Timetable> list = new ArrayList<Timetable>(this.orderedChromosomes);
			for(int j=0;j<newIndividuals.size();j++) {
				for(int i=list.size()-1;i>=0;i--) { // &&j<newIndividuals.size()
					if(list.get(i).penalty>newIndividuals.get(j).penalty) {
						list.set(i, newIndividuals.get(j));
						break;
					}
				}
			}
			this.orderedChromosomes.clear();
			this.orderedChromosomes.addAll(list);
			this.size = this.orderedChromosomes.size();
		}
		
		
		/* Mutates a given chromosome */
		void mutates(Timetable chromosome) {
			Random rand = new Random();
			
			chromosome.data.timeSlots.size();
			for(int i=0;i<chromosome.data.slotsNumber*0.30;i=i+2) {
				int src = rand.nextInt(chromosome.data.slotsNumber);
				int dst = rand.nextInt(chromosome.data.slotsNumber);
				
				ArrayList<Integer> srcDs= chromosome.timeSlots.get(src);
				ArrayList<Integer> dstDs= chromosome.timeSlots.get(dst);
				chromosome.timeSlots.set(dst, srcDs);
				chromosome.timeSlots.set(src, dstDs);
				
				ArrayList<Tuple> srcConfDs= chromosome.timeSlotsConflict.get(src);
				ArrayList<Tuple> dstConfDs= chromosome.timeSlotsConflict.get(dst);
				chromosome.timeSlotsConflict.set(dst, srcConfDs);
				chromosome.timeSlotsConflict.set(src, dstConfDs);
						
				// timeSlotWithConflicts.. is empty...
				
				
				for(Iterator<Timetable.ExamInfo> ite = chromosome.slotInfos.get(src).orderedExamInfo.iterator();ite.hasNext(); ) {
					Timetable.ExamInfo e = ite.next();
					e.timeslot=dst;
				}
				for(Iterator<Timetable.ExamInfo> ite = chromosome.slotInfos.get(dst).orderedExamInfo.iterator();ite.hasNext(); ) {
					Timetable.ExamInfo e = ite.next();
					e.timeslot=src;
				}
			}
			
			
		}
		
		/* Reproducts two parent to get a child ...
		 * */
		static Timetable reproducts(Timetable p1, Timetable p2, double crossOverThreesholdPercentage) throws Exception {
			
			//int numberExamToChange = (int) (p1.data.examsNumber*crossOverThreesholdPercentage);
			
			Timetable best = (p1.penalty>p2.penalty)?p2:p1; // best
			Timetable worst = (best==p1)? p2:p1;			// worst			
			Timetable c = new Timetable(p1.data);

			Timetable.ExamInfo bestExamInfoOfBestOfBestTimeslot = best.orderedSlotInfos.peek().orderedExamInfo.peek();
			
			ArrayList<Integer> addedExams = new ArrayList<Integer>();
			
			// Copio miglior esame(e vicini) del miglior parent nella stessa posizione in child. 
			for(int i=0;i<best.timeSlots.size();i++) {
				if((bestExamInfoOfBestOfBestTimeslot.timeslot-i)>5||(bestExamInfoOfBestOfBestTimeslot.timeslot-i)<-5)
					continue;
				
				List<Timetable.ExamInfo> exams = new ArrayList<Timetable.ExamInfo>(best.slotInfos.get(i).orderedExamInfo);
				
				for(int j=0;j<exams.size()*crossOverThreesholdPercentage;j++) {
					c.addExam(i,exams.get(j).id);
					//c.fixedExams.put(exams.get(j).id, exams.get(j).id);
					addedExams.add(exams.get(j).id);
				}
			}
			
			Random rand = new Random();
			
			// Copio restante parte dal worst
			for(int i=0;i<worst.timeSlots.size();i++) {
				for(int j=0;j<worst.timeSlots.get(i).size();j++) {
					
//					ArrayList<ArrayList<Integer>> src = (rand.nextBoolean())?worst.timeSlots:best.timeSlots;
					
					if(addedExams.contains((Integer)worst.timeSlots.get(i).get(j)))
					//if(addedExams.contains((Integer)src.get(i).get(j)))
							continue;
					c.addExam(i,worst.timeSlots.get(i).get(j));
					addedExams.add(worst.timeSlots.get(i).get(j));
//					c.addExam(i,src.get(i).get(j));
//					addedExams.add(src.get(i).get(j));
				}
					
			}
			
			Timetable prova = FeasibleConstructor.generatesFeasibleTimetable(c);
			//prova.fixedExams = new HashMap<Integer,Integer>();
			return prova;
		}
		
	}
	
	
	
	// Costant parameter fields.
	private double generation_replace_percentage = 0.5;
	private double simple_crossover_threshold_pergentage = 0.7;
	// ... others?
	
	private Population population;
	
	
	/**
	 * Genetic optimizer constructor.
	 * */
	public GeneticOptimizer(List<Timetable> timetables) {
		this.population = new Population();
		System.out.println("[GeneticOptimizer] Population number 0");
		for(Iterator<Timetable> it = timetables.iterator();it.hasNext();) {
			Timetable timetable = it.next();
			timetable.evaluatesPenalty();
			this.population.orderedChromosomes.add(timetable);
			System.out.println("[GeneticOptimizer] (pop=0)Individual penalty :"+ (double)timetable.penalty/timetable.data.studentsNumber);
		}
		this.population.size = timetables.size();
		
	}
	
	
	/**
	 * Start meta-euristic process with a timeout
	 * expressed in maxIterations.
	 * @throws Exception 
	 * */
	public Timetable startOptimizer(int timeout) throws Exception {
		
		int childrenCount = (int) (this.population.size*this.generation_replace_percentage);
		int pi=1;
		Random mutationRand = new Random();
		
		
		double penaltyFirst = this.population.getBestChromosome().penalty/this.population.getBestChromosome().data.studentsNumber;
		
		while(pi<timeout||timeout==-1) {
			
			List<Timetable> parents = this.population.selects(this.generation_replace_percentage);
			List<Timetable> children = new ArrayList<Timetable>();
			
			System.out.println("[GeneticOptimizer] Population number "+ pi);
			
			// Reproduction phase
			for(int i=0,j=parents.size()-1;i<j;i++,j--) {
				
				if(i==j) continue;
					
				Timetable p1 = parents.get(i);
				Timetable p2 = parents.get(j);
					
				Timetable c = Population.reproducts(p1,p2,simple_crossover_threshold_pergentage);
					
				c.evaluatesPenalty();
				//System.out.println("[GeneticOptimizer] (pop="+pi+")Individual penalty :"+ (double)c.penalty/c.data.studentsNumber);
				children.add(c);
			}
			
			this.population.replace(children);
			
			double avg = 0;
			for(Iterator<Timetable> it = this.population.orderedChromosomes.iterator();it.hasNext();) {
				Timetable t = it.next();
				System.out.println("[GeneticOptimizer] (pop="+pi+")Individual penalty :"+ (double)t.penalty/t.data.studentsNumber);		
				avg+=t.penalty;
			}
			
			avg/=this.population.size;
			
			int shold=0;
			for(Iterator<Timetable> it = this.population.orderedChromosomes.iterator();it.hasNext();) {	
				Timetable t = it.next();
				if(Math.abs(avg-t.penalty)<0.1 && shold>this.population.size*this.generation_replace_percentage) {
					shold--;
					List<Timetable> chromosomes = new ArrayList<Timetable>(this.population.orderedChromosomes);
					Random rand = new Random();
					for(int i=0;i<this.population.size*this.generation_replace_percentage;i++) {
						Timetable selected = chromosomes.get(rand.nextInt(chromosomes.size()));
						this.population.mutates(selected);
						int ciao;
						ciao = 1;
					}
					break;
				}
				shold++;
			}			
			
			
//			Timetable best = this.population.getBestChromosome();
//			Timetable worst= this.population.getWorstChromosome();
//			
//			if(best.penalty/worst.penalty>0.999) {
//				List<Timetable> chromosomes = new ArrayList<Timetable>(this.population.orderedChromosomes);
//				Random rand = new Random();
//				for(int i=0;i<this.population.size*this.generation_replace_percentage;i++) {
//					Timetable selected = chromosomes.get(rand.nextInt(chromosomes.size()));
//					this.population.mutates(selected);
//					
//				}
//			}
			
			pi++;
		}
		
        System.out.println("Best of first population : "+ (double)penaltyFirst);
        
        
		return this.population.getBestChromosome();
	}
	
	
}
