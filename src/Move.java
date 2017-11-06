public class Move {
    public Integer exam;
    public int timeslot;
    public int conflicts;

    public Move(int exam, int timeslot, int conflicts) {
        this.exam = exam;
        this.timeslot = timeslot;
        this.conflicts = conflicts;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Move) {
            Move move = (Move) o;
            if (move.exam.intValue() == this.exam && move.timeslot == this.timeslot)
                return true;
        }
        return false;
    }
}
