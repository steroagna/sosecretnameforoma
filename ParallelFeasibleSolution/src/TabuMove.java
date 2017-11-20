

/*
 * Class which rapresents a forbidden moving.
 * */
public class TabuMove {
	
	public int sourceTimeSlot;
	public int destinationTimeSlot;
	public int idExam;
	
	public TabuMove(int idExam,int sourceTimeSlot, int destinationTimeSlot) {
		this.destinationTimeSlot = destinationTimeSlot;
		this.sourceTimeSlot = sourceTimeSlot;
		this.idExam = idExam;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o instanceof TabuMove) {
			TabuMove ti = (TabuMove)o;
			if(ti.idExam==this.idExam && this.destinationTimeSlot==ti.sourceTimeSlot)
				return true;
		}
		return false;
	}
}
