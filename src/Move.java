/*
 * Class which rapresents a forbidden moving.
 * */
public class Move {

	public int sourceTimeSlot;
	public int destinationTimeSlot;
	public int idExam;
	public double penalty;

	public Move(int idExam, int sourceTimeSlot, int destinationTimeSlot) {
		this.destinationTimeSlot = destinationTimeSlot;
		this.sourceTimeSlot = sourceTimeSlot;
		this.idExam = idExam;
		this.penalty = 0;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o instanceof Move) {
			Move ti = (Move)o;
			if(ti.idExam==this.idExam && this.penalty==ti.penalty)
				return true;
		}
		return false;
	}
}
