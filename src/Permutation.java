public class Permutation {
    public Swap s1, s2;
    public double penalty;
    int caso;

    public Permutation() {
        this.s1 = null;
        this.s2 = null;
        this.penalty = Double.MAX_VALUE;
    }

    public Permutation(Timetable timetable, int timeslot1, int timeslot2, int timeslot3, int e1, int e2, int e3, int caso) {
        int pow;
        double penaltyE1E2 = 0;
        double penaltyE1E3 = 0;
        double penaltyE2E3 = 0;

        if (timeslot1 > timeslot2)
            pow = timeslot1 - timeslot2;
        else
            pow = timeslot2 - timeslot1;
        if (pow <= 5)
            penaltyE1E2 = Math.pow(2, (5 - pow)) * timetable.data.conflictExams[e1][e2];

        if (timeslot1 > timeslot3)
            pow = timeslot1 - timeslot3;
        else
            pow = timeslot3 - timeslot1;
        if (pow <= 5)
            penaltyE1E3 = Math.pow(2, (5 - pow)) * timetable.data.conflictExams[e1][e3];

        if (timeslot3 > timeslot2)
            pow = timeslot3 - timeslot2;
        else
            pow = timeslot2 - timeslot3;
        if (pow <= 5)
            penaltyE2E3 = Math.pow(2, (5 - pow)) * timetable.data.conflictExams[e2][e3];

        double swap1 = timetable.evaluateOF(e1, timeslot2) +
                timetable.evaluateOF(e2, timeslot3) +
                timetable.evaluateOF(e3, timeslot1) +
                2*penaltyE1E2 + 2*penaltyE1E3 + 2*penaltyE2E3 - 2*timetable.objFunc;

        double swap2 = timetable.evaluateOF(e1, timeslot3) +
                timetable.evaluateOF(e2, timeslot1) +
                timetable.evaluateOF(e3, timeslot2) +
                2*penaltyE1E2 + 2*penaltyE1E3 + 2*penaltyE2E3 - 2*timetable.objFunc;

        if (swap1 < swap2) {
            this.s1 = new Swap(timetable, timeslot1, timeslot2, e1, e2, true);
            this.s2 = new Swap(timetable, timeslot1, timeslot3, e2, e3, true);
            this.penalty = swap1;
            this.caso = caso;
        } else {
            this.s1 = new Swap(timetable, timeslot1, timeslot3, e1, e3, true);
            this.s2 = new Swap(timetable, timeslot1, timeslot2, e3, e2, true);
            this.penalty = swap1;
            this.caso = caso;
        }
    }
}
