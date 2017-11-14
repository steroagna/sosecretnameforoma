

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
	
	public void addTabuItem(TabuMove item) {
		
		this.list.set((head+currentSize)%size, item);
		currentSize++;
		if(currentSize>size) {
			currentSize--;
			head++;
		}
	}
	
	public List<TabuMove> getTabuList() {
		return new ArrayList<TabuMove>(list);
	}
	
}
