import java.util.ArrayList;
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
			//Random rand = new Random();
			
			int i=0;
			for(Iterator<Timetable> it =list.iterator();it.hasNext()&&i<this.orderedChromosomes.size()*percentage;i+=2,it.next()) {
				out.add(list.get(i));
				//out.add(list.get(list.size()/2+i));
				out.add(list.get(list.size()-i-1));
			}
			
			return out;
		}
		
		/* Get best chromosome */
		Timetable getBestChromosome() {
			return this.orderedChromosomes.peek();
		}
		
		/* Now take first best chromosomes of queue (always). */
		void replace(List<Timetable> newIndividuals){
			List<Timetable> list = new ArrayList<Timetable>(this.orderedChromosomes);
			
			for(int i=list.size()-1,j=0;i>=0&&j<newIndividuals.size();i--,j++) {
				list.set(i, newIndividuals.get(j));
			}
			this.orderedChromosomes.clear();
			this.orderedChromosomes.addAll(list);
			this.size = this.orderedChromosomes.size();
		}
		
		
		/* Mutates a given chromosome */
		static void mutates(Timetable chromosome) {
			Random slotRand = new Random();
			for(;;) {
				int t1 = slotRand.nextInt(chromosome.timeSlots.size());
				int t2 = slotRand.nextInt(chromosome.timeSlots.size());
				if(t1 == t2) continue;
				chromosome.switchSlots(t1,t2);
				break;
			}
		}
		
		/* Reproducts two parent to get a child ...
		 * */
		static Timetable reproducts(Timetable p1, Timetable p2, double crossOverThreesholdPercentage) throws Exception {
			
			//int numberExamToChange = (int) (p1.data.examsNumber*crossOverThreesholdPercentage);
			
			Timetable best = (p1.penalty>p2.penalty)?p2:p1;
			Timetable worst = (best==p1)? p2:p1;
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
					addedExams.add(exams.get(j).id);
				}
			}
			
			// Copio restante parte dal worst
			for(int i=0;i<worst.timeSlots.size();i++) {
				for(int j=0;j<worst.timeSlots.get(i).size();j++) {
					if(addedExams.contains((Integer)worst.timeSlots.get(i).get(j)))
							continue;
					c.addExam(i,worst.timeSlots.get(i).get(j));
					addedExams.add(worst.timeSlots.get(i).get(j));
				}
					
			}
			
			Timetable prova = FeasibleConstructor.generatesFeasibleTimetable(c);
			return prova;
		}
		
	}
	
	
	
	// Costant parameter fields.
	private double generation_replace_percentage = 0.7;
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
		
		while(pi<timeout||timeout==-1) {
			
			List<Timetable> parents = this.population.selects(this.generation_replace_percentage);
			List<Timetable> children = new ArrayList<Timetable>();
			
			System.out.println("[GeneticOptimizer] Population number "+ pi);
			
			// Reproduction phase
			for(int i=0;i<parents.size();i++) {
				for(int j=0;children.size()<childrenCount&&j<parents.size();j++) {
					if(i==j) continue;
					
					Timetable p1 = parents.get(i);
					Timetable p2 = parents.get(j);
					
					Timetable c = Population.reproducts(p1,p2,simple_crossover_threshold_pergentage);
					
					c.evaluatesPenalty();
					System.out.println("[GeneticOptimizer] (pop="+pi+")Individual penalty :"+ (double)c.penalty/c.data.studentsNumber);
					children.add(c);
				}
			}
			
			this.population.replace(children);

			pi++;
		}
		
		return this.population.getBestChromosome();
	}
	
	
}
