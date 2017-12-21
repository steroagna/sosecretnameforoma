import java.sql.Time;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class Population {

    public ArrayList<Timetable> population;
    public double bestOF;
    Timetable parent1, parent2, bestTimetable;

    public Population(ArrayList<Timetable> population, Timetable best, double bestOF) {
        super();
        this.population = population;
        this.bestTimetable = best;
        this.bestOF = bestOF;
        this.parent1 = null;
        this.parent2 = null;
    }

    public void chooseParents() {

        Random r1 = new Random();
        Random r2 = new Random();
        int p1 = 0, p2= 0;

        while (p1 == p2) {
            p1 = r1.nextInt(this.population.size());
            p2 = r2.nextInt(this.population.size());
        }

        this.parent1 = new Timetable(this.population.get(p1));
        this.parent2 = new Timetable(this.population.get(p2));

        return;
    }

    public Timetable chooseParent() {

        chooseParents();
        if (this.parent1.objFunc < this.parent2.objFunc)
            return this.parent1;
        else
            return this.parent2;
    }

    public Timetable copulate() {

        Timetable A,timetable1 = null,timetable2 = null;
        double x;

        while (timetable1 == timetable2) {
            timetable1 = this.chooseParent();
            timetable2 = this.chooseParent();
        }
        Timetable newGen = new Timetable(this.parent1);
        ArrayList exams = new ArrayList<>(parent1.data.examsMap.keySet());
        int exam, timeslot, index = 0;

        if (timetable1.objFunc > timetable2.objFunc)
            x = 0.25;
        else
            x = 0.75;

        for(Iterator<Integer> itExam=exams.iterator();itExam.hasNext();) {
            exam = itExam.next();
            if (ThreadLocalRandom.current().nextDouble() < x) {
                A = timetable1;
            } else {
                A = timetable2;
            }

            timeslot = A.positions.get(exam);
            newGen.addExam(timeslot, exam);
        }

        return newGen;
    }

    public void updatePopulation(Timetable newGen) {

        int i, indexMax = 0;
        double OJMax = 0;
        for (i = 0; i < this.population.size(); i++) {
            if (this.population.get(i).objFunc > OJMax) {
                OJMax = this.population.get(i).objFunc;
                indexMax = i;
            }
        }

        if (newGen.objFunc < this.population.get(indexMax).objFunc)
            this.population.set(indexMax, newGen);
    }
}
