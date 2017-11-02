package it.polito.oma.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.polito.oma.application.pojo.ExamTotStudent;
import it.polito.oma.application.pojo.StudentExam;
import it.polito.oma.application.pojo.TimeSlots;
import it.polito.oma.entities.Exam;
import it.polito.oma.entities.Timetable;
import it.polito.oma.util.io.TextFileReader;

// Ipotesi forte: id studenti e id esami partono da 0 e sono incrementali.
public class Application {

	static private int CONFLICT = 1;
	static private int NO_CONFLICT = 0;
	
	static private int SELECTED = 1;
	static private int UNSELECTED = 0;
	
	public static void main(String[] args) throws Exception {
		
		long startTimeFeasableSolutionInitialization = System.currentTimeMillis();
		
		Factory.ExamTotStudentFactory etsFactory = new Factory.ExamTotStudentFactory();
		TextFileReader<ExamTotStudent> exmReader = new TextFileReader<ExamTotStudent>("instance01.exm", etsFactory); 

		Factory.TimeSlotsFactory tsFactory = new Factory.TimeSlotsFactory();
		TextFileReader<TimeSlots> sloReader = new TextFileReader<TimeSlots>("instance01.slo", tsFactory); 
		
		Factory.StudentExamFactory seFactory = new Factory.StudentExamFactory();
		TextFileReader<StudentExam> stuReader = new TextFileReader<StudentExam>("instance01.stu", seFactory); 
		
		/*
		 * 	Reading files.
		 * */
		List<ExamTotStudent> examTotStudentsList =  exmReader.readLines();
		List<TimeSlots> timeSlotsList = sloReader.readLines();
		List<StudentExam> studentExamList =  stuReader.readLines();
		
		/*
		 * 	Initialization basic data structures.
		 * */
		
		int E = examTotStudentsList.size();
		int S = 0;
		int tmax = timeSlotsList.get(0).timeSlots;
		
		Timetable timetable = new Timetable(E, tmax);
	
		int [][] studentExams ;
		int [][] conflictExams ;			// conflict exams considering arch as value cell=1
											// no conflict exams considering arch as value cell=0
		
		// define other helpful data structures..

		for(Iterator<ExamTotStudent> it = examTotStudentsList.iterator(); it.hasNext();) {
			ExamTotStudent ets = it.next();
			S+= ets.totStudent;
			// do something else ..
		}

		studentExams = new int[S+1][E+1];
		conflictExams = new int[E+1][E+1];	
		
		Map<Integer,Exam> examsMap = new HashMap<Integer,Exam>();
		
		for(Iterator<StudentExam> it = studentExamList.iterator(); it.hasNext();) {
			StudentExam se = it.next();
			studentExams[se.intIdStudent][se.idExam] = 1;
			
			if(!examsMap.containsKey(se.idExam)) {
				examsMap.put(se.idExam, new Exam(se.idExam));
			}
			
			examsMap.get(se.idExam).students.put(se.intIdStudent,null);
		}
		
		
		// Initialization of (no)conflicting graph:
		// for each student its exams array is an array of conflicting exams,
		// so for each exam contained in this array, conflicting exams array
		// will be copied in 'or' way.
		for(int si=1;si<=S;si++) { 
			for(int ei1=1;ei1<=E;ei1++) { 
				if(studentExams[si][ei1]==1) {
					for(int ei2=1;ei2<=E;ei2++) {
						if(ei1==ei2) {
							conflictExams[ei1][ei2] = NO_CONFLICT; // An exam is not a conflicting exam for itself
							
						}else {
							conflictExams[ei1][ei2] |= studentExams[si][ei2]; // because exams of a student are in conflict by definition
						}
					}
				}
			}			
		}			

		// (v.2)
		// Initialization of (no)conflicting graph:
		// for each student its exams array is an array of conflicting exams,
		// so for each exam contained in this array, conflicting exams array
		// will be copied in 'or' way.
		for(int si=1;si<=S;si++) { 
			for(int ei1=1;ei1<=E;ei1++) { 
				if(studentExams[si][ei1]==1) {
					for(int ei2=1;ei2<=E;ei2++) {
						if(ei1!=ei2) {
							if(studentExams[si][ei2]==1) {
								// ei1 and ei2 are conclicting exams.
								examsMap.get(ei1).conflictExams.put(ei2, null);
								examsMap.get(ei2).conflictExams.put(ei1, null);
							}
						}
					}
				}			
			}	
		}
		
		for(Iterator<Integer> it = examsMap.keySet().iterator();it.hasNext();) {
			Exam exam = examsMap.get(it.next());
			for(int ei=1;ei<E;ei++)
				if(ei!=exam.id && !exam.conflictExams.containsKey(ei))
					exam.noConflictExams.put(ei, null);
		}
		
		// Initialization of timetable with a feasable solution.
		// [ref. http://i.cs.hku.hk/~chin/paper/Clique%20paper.pdf (pag.4)]
		
		//List<int[]> examsCliques = findCliques(conflictExams, E);
		List<HashSet<Integer>> examsCliques = findCliques(examsMap, E);
		
		// Finding a subset of cliques(#cliquesSelected must be <= tmax)
		// which cover all exams graph.
		// ...
		int ci = 1;
		for(Iterator<HashSet<Integer>> itcs=examsCliques.iterator() ;itcs.hasNext();)
		{
			HashSet<Integer> clique = itcs.next();
			System.out.print("Clique "+ci+ ":");
			for(Iterator<Integer> itc=clique.iterator();itc.hasNext();)
					System.out.print(itc.next()+", ");
			ci++;
			System.out.println();
		}
		//End initialization of timetable with a feasable solution.
		
		long stopTimeFeasableSolutionInitialization = System.currentTimeMillis();
		
		System.out.println("Milliseconds need to initialize feasable solution: "+ (stopTimeFeasableSolutionInitialization - startTimeFeasableSolutionInitialization) );
		//System.out.println(timetable);
		
	
	}

	/* 
	 * Function which find for each vertex not considered yet
	 * the biggest clique to which it belongs.
	 * 
	 * Complexity = O(V^3)
	 * */
	static List<HashSet<Integer>> findCliques(Map<Integer,Exam> examsMap,int E) {
		
		List<HashSet<Integer>> cliques = new ArrayList<HashSet<Integer>>();
		
		int[] consideredExams = new int[E+1]; // initially all exams are NO_SELECTED(=0)
		
		for(Iterator<Integer> ite=examsMap.keySet().iterator();ite.hasNext();) 
		{
			Integer idExam =ite.next();
			if(consideredExams[idExam]==SELECTED) continue;
			
			Set<Integer> V ;
			HashSet<Integer> clique = new HashSet<Integer>();
			
			V = examsMap.get(idExam).noConflictExams.keySet();
			clique.add(idExam);
			
			while(!V.isEmpty()) {
				
				// Search neighbour with highest degree
				Integer highestDegreeNeighbour=-1;
				Integer highestDegree = -1;
				
				for(Iterator<Integer> itn=V.iterator();itn.hasNext();) {
					Integer currentNeighbour = itn.next();
					if(examsMap.get(currentNeighbour).noConflictExams.size()>highestDegree) {
						highestDegreeNeighbour = currentNeighbour;
						highestDegree = examsMap.get(currentNeighbour).noConflictExams.size();
					}
				}
				// End search neighbour
				
				clique.add(highestDegreeNeighbour);
				
				// Intersect neighbours of new highestDegreeNeighbour with accettable vi 
				Set<Integer> _V = new HashSet<Integer>();
				for(Iterator<Integer> itn=V.iterator();itn.hasNext();) {
					Integer potentialNeighbour = itn.next();
					if(examsMap.get(highestDegreeNeighbour).noConflictExams.containsKey(potentialNeighbour)) {
						_V.add(potentialNeighbour);
					}
				}
				V=_V;
				// End intersection
			}
			
			for(Iterator<Integer> it=clique.iterator();it.hasNext();)
				consideredExams[it.next()] = SELECTED;
			
			cliques.add(clique);
		}
		
		return cliques;
	}
	
}
