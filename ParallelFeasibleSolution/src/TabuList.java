import java.util.ArrayList;
import java.util.List;

public class TabuList {

	private List<TabuMove> list = new ArrayList<TabuMove>();
	private int size;
	private int currentSize;
	private int head;
	
	
	public TabuList(int size) {
		this.size  = size;
		this.head = 0;
	}
	
	public void addTabuMove(TabuMove move) {

		
		if(currentSize<size)
			this.list.add(move);
		else
			this.list.set((head+currentSize)%size, move);
		currentSize++;
		if(currentSize>size) {
			currentSize--;
			head = (head+1)%size;
		}
	}
	
	public boolean removeTabuMove(TabuMove move) {
		
		if(this.list.contains(move)) {
			boolean isFirst = list.get(0).equals(move);	
				
			this.list.remove(move);
			currentSize--;
			
			if(isFirst)
				head = (head+1)%size;
			return true;
		}
		return false;
	}
	
	public List<TabuMove> getTabuList() {
		return list;
	}
	
}
