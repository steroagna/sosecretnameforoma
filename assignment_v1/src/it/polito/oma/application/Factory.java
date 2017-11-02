package it.polito.oma.application;

import it.polito.oma.application.pojo.ExamTotStudent;
import it.polito.oma.application.pojo.StudentExam;
import it.polito.oma.application.pojo.TimeSlots;
import it.polito.oma.util.io.DataFactory;

public class Factory  {

	
	static public class ExamTotStudentFactory extends DataFactory<ExamTotStudent>{
		@Override
		public ExamTotStudent newInstance() {
			// TODO Auto-generated method stub
			return new ExamTotStudent();
		}
	}
	
	static public class TimeSlotsFactory extends DataFactory<TimeSlots>{
		@Override
		public TimeSlots newInstance() {
			// TODO Auto-generated method stub
			return new TimeSlots();
		}
	}
	
	static public class StudentExamFactory extends DataFactory<StudentExam>{
		@Override
		public StudentExam newInstance() {
			// TODO Auto-generated method stub
			return new StudentExam();
		}
	}
}
