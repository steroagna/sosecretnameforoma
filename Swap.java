public class Swap {
    public Move m1, m2;
    public double penalty;

    public Swap(Timetable timetable, int origin, int destination, int e1, int e2) {
        this.m1 = new Move(e1, origin, destination);
        this.m2 = new Move(e2, destination, origin);
        this.penalty = timetable.evaluateOFSwap(e1, e2, destination, origin);
    }
}
