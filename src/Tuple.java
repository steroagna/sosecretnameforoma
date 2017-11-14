

public class Tuple {

	public Integer e1;
	public Integer e2;
	
	public Tuple(Integer e1, Integer e2) {
		this.e1 = e1;
		this.e2 = e2;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Tuple) {
			Tuple t = (Tuple)o;
			if((t.e1==this.e1 && t.e2==this.e2)||(t.e2==this.e1 && t.e1==this.e2))
				return true;
		}
		return false;
	}
}
