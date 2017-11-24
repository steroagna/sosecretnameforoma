

public class TabuSlotMove extends TabuMove {
    /*
     * Class which rapresents a forbidden moving.
     * */
    public int source;
    public int destination;

    public TabuSlotMove(int sourceTimeSlot, int destinationTimeSlot) {
        super(0,sourceTimeSlot,destinationTimeSlot);
    }

    @Override
    public boolean equals(Object o) {

        if(o instanceof TabuSlotMove) {
            TabuSlotMove ti = (TabuSlotMove) o;
            if(ti.source==this.source && ti.destination==this.destination)
                return true;
        }
        return false;
    }
}