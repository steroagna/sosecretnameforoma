import java.util.ArrayList;
import java.util.List;

public class TabuList {

	private List<TabuMove> list;
	private int size;
	private int currentSize;
	private int head;
	
	public TabuList(int size) {
		this.list = new ArrayList<>();
		this.size  = size;
		this.currentSize = 0;
		this.head = 0;
	}
	
	public void addTabuItem(TabuMove item) {

		if (this.list.size() < size) {
			this.list.add(item);
		} else {
			this.list.set((head + currentSize) % size, item);
		}
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
