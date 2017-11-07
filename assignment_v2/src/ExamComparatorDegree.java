import java.util.Comparator;

public class ExamComparatorDegree implements Comparator<Exam> {
	@Override
	public int compare(Exam e1, Exam e2) {
		if (e2.getConnectedExamsNumber() > e1.getConnectedExamsNumber()) {
            return 1;
        } else {
            return -1;
        }
	}
}
