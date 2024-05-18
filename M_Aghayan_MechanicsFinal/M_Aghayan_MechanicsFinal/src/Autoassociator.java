import java.util.Arrays;

public class Autoassociator {
    private int[][] weights;
    private int trainingCapacity;

    public Autoassociator(CourseArray courses) {
        int numOfCourses = courses.length();
        weights = new int[numOfCourses][numOfCourses];
        trainingCapacity = (int) (numOfCourses / (2 * Math.log(numOfCourses)));
    }

    public int getTrainingCapacity() {
        return trainingCapacity;
    }

    public void training(int[] pattern) {
        if (pattern == null || pattern.length == 0) {
            throw new IllegalArgumentException("Pattern array is empty or null");
        }

        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (i != j) {
                    weights[i][j] += pattern[i] * pattern[j];
                }
            }
        }
    }

    public int[] performUnitUpdatesAndGetTimeslots(int[] initialTimeslots, int steps) {
        int[] timeslots = Arrays.copyOf(initialTimeslots, initialTimeslots.length);

        // Perform unit updates
        for (int i = 0; i < steps; i++) {
            unitUpdate(timeslots);
        }

        return timeslots;
    }

    private void unitUpdate(int[] neurons) {
        int index = (int) (Math.random() * neurons.length);
        int sum = 0;
        for (int i = 0; i < neurons.length; i++) {
            if (i != index) {
                sum += weights[index][i] * neurons[i];
            }
        }
        neurons[index] = sum >= 0 ? 1 : -1;
    }

    public void chainUpdate(int[] neurons, int steps) {
        for (int i = 0; i < steps; i++) {
            unitUpdate(neurons);
        }
    }
}
