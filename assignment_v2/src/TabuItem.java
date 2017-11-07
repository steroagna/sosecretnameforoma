

/*
 * Class which rapresents a forbidden moving.
 * */
public class TabuItem {
	
	public int sourceTimeSlot;
	public int destinationTimeSlot;
	public int idExam;
	
	public TabuItem(int idExam,int sourceTimeSlot, int destinationTimeSlot) {
		this.destinationTimeSlot = destinationTimeSlot;
		this.sourceTimeSlot = sourceTimeSlot;
		this.idExam = idExam;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o instanceof TabuItem) {
			TabuItem ti = (TabuItem)o;
			if(ti.idExam==this.idExam && ti.destinationTimeSlot==this.destinationTimeSlot)
				return true;
		}
		return false;
	}
}
