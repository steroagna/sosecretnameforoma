

import java.util.ArrayList;
import java.util.List;

public class TabuList {

	private List<TabuItem> list = new ArrayList<TabuItem>();
	private int size;
	private int currentSize;
	private int head;
	
	public TabuList(int size) {
		this.size  = size;
		this.head = 0;
	}
	
	public void addTabuItem(TabuItem item) {
		
		this.list.set((head+currentSize)%size, item);
		currentSize++;
		if(currentSize>size) {
			currentSize--;
			head++;
		}
	}
	
	public List<TabuItem> getTabuList() {
		return new ArrayList<TabuItem>(list);
	}
	
}
