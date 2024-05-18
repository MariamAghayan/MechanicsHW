import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.io.*;
import java.util.*;

public class TimeTable extends JFrame implements ActionListener {
    private JPanel screen = new JPanel();
    private JPanel tools = new JPanel();
    private JButton[] tool;
    private JTextField[] field;
    private CourseArray courses;
    private Color[] CRScolor;
    private Autoassociator autoassociator; // Instance of Autoassociator

    public TimeTable() {
        super("Dynamic Time Table");
        this.CRScolor = new Color[]{Color.RED, Color.GREEN, Color.BLACK};
        this.setSize(800, 800);
        this.setLayout(new FlowLayout());
        this.screen.setPreferredSize(new Dimension(600, 800));
        this.add(this.screen);
        this.setTools();
        this.add(this.tools);
        this.setVisible(true);
        this.courses = new CourseArray(139, 13); // Initialize courses
        this.autoassociator = new Autoassociator(this.courses); // Initialize Autoassociator with courses
        this.trainAutoassociator(); // Train the autoassociator with clash-free timeslots
    }

    public void setTools() {
        String[] capField = new String[]{"Slots:", "Courses:", "Clash File:", "Iters:", "Shift:"};
        this.field = new JTextField[capField.length];
        String[] capButton = new String[]{"Load", "Start", "Step", "Print", "Exit", "Continue"};
        this.tool = new JButton[capButton.length];
        this.tools.setLayout(new GridLayout(2 * capField.length + capButton.length, 1));

        for (int i = 0; i < this.field.length; ++i) {
            this.tools.add(new JLabel(capField[i]));
            this.field[i] = new JTextField(5);
            this.tools.add(this.field[i]);
        }

        for (int i = 0; i < this.tool.length; ++i) {
            this.tool[i] = new JButton(capButton[i]);
            this.tool[i].addActionListener(this);
            this.tools.add(this.tool[i]);
        }

        this.field[0].setText("13");
        this.field[1].setText("139");
        this.field[2].setText("C:\\Users\\User\\Desktop\\M_Aghayan_MechanicsFinal\\src\\sta-f-83.stu");
        this.field[3].setText("1");
    }

    public void draw() {
        Graphics g = this.screen.getGraphics();
        int width = Integer.parseInt(this.field[0].getText()) * 10;

        for (int courseIndex = 1; courseIndex < this.courses.length(); ++courseIndex) {
            g.setColor(this.CRScolor[this.courses.status(courseIndex) > 0 ? 0 : 1]);
            g.drawLine(0, courseIndex, width, courseIndex);
            g.setColor(this.CRScolor[this.CRScolor.length - 1]);
            g.drawLine(10 * this.courses.slot(courseIndex), courseIndex, 10 * this.courses.slot(courseIndex) + 10, courseIndex);
        }
    }

    private int getButtonIndex(JButton source) {
        int result;
        for (result = 0; source != this.tool[result]; ++result) {
        }
        return result;
    }

    public void actionPerformed(ActionEvent click) {
        int iteration;
        switch (this.getButtonIndex((JButton) click.getSource())) {
            case 0:
                int slots = Integer.parseInt(this.field[0].getText());
                this.courses = new CourseArray(Integer.parseInt(this.field[1].getText()), slots);
                this.courses.readClashes(this.field[2].getText());
                this.draw();
                break;
            case 1:
                try {
                    runExperiment();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                this.courses.iterate(Integer.parseInt(this.field[4].getText()));
                this.draw();
                break;
            case 3:
                System.out.println("Exam\tSlot\tClashes");

                for (iteration = 1; iteration < this.courses.length(); ++iteration) {
                    System.out.println("" + iteration + "\t" + this.courses.slot(iteration) + "\t" + this.courses.status(iteration));
                }

                // Add clash debugging
                this.courses.printClashes();

                return;
            case 4:
                System.exit(0);
                break;
            case 5:
                this.courses.iterate(Integer.parseInt(this.field[4].getText()));
                this.draw();
                break;
        }
    }

    public static void main(String[] args) {
        new TimeTable();
    }

    // Method to train Autoassociator with clash-free timeslots
    private void trainAutoassociator() {
        // Get clash-free timeslots from Task 3
        int[] clashFreeTimeslots = getClashFreeTimeslotsFromTask3(this.courses);
        if (clashFreeTimeslots.length == 0) {
            throw new IllegalStateException("No clash-free timeslots found.");
        }
        // Train Autoassociator with clash-free timeslots
        autoassociator.training(clashFreeTimeslots);
    }

    // Method to get clash-free timeslots from Task 3
    private int[] getClashFreeTimeslotsFromTask3(CourseArray courses) {
        List<Integer> clashFreeTimeslotsList = new ArrayList<>();
        int numOfCourses = courses.length();

        // Iterate through each timeslot
        for (int timeslotIndex = 0; timeslotIndex < numOfCourses; timeslotIndex++) {
            boolean isClashFree = true;

            // Check if any course clashes in this timeslot
            for (int courseIndex = 1; courseIndex < numOfCourses; courseIndex++) {
                if (courses.slot(courseIndex) == timeslotIndex && courses.status(courseIndex) > 0) {
                    isClashFree = false;
                    break;
                }
            }

            // If no clashes found in this timeslot, add it to the clash-free timeslots list
            if (isClashFree) {
                clashFreeTimeslotsList.add(timeslotIndex);
            }
        }

        // Convert clash-free timeslots list to array
        int[] clashFreeTimeslots = new int[clashFreeTimeslotsList.size()];
        for (int i = 0; i < clashFreeTimeslotsList.size(); i++) {
            clashFreeTimeslots[i] = clashFreeTimeslotsList.get(i);
        }

        return clashFreeTimeslots;
    }

    // Method to save used timeslots to a log file
    private void saveTimeslotsToLog(int shift, int iterationIndex, List<Integer> clashFreeTimeslots) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("2nd_timeslots_log_sta-f-83.txt", true)))) {
            writer.println("Number of Slots: " + (this.courses.length() - 1));
            writer.println("Shift: " + shift);
            writer.println("Iteration Index: " + iterationIndex);

            writer.println("Clash-free Timeslots:");
            for (int i = 0; i < clashFreeTimeslots.size(); i++) {
                int timeslotIndex = clashFreeTimeslots.get(i);
                int[] timeslotArray = courses.getTimeSlot(timeslotIndex); // Retrieve the timeslot array for the given index
                writer.print("Timeslot Index " + timeslotIndex + ": ");
                for (int j = 0; j < timeslotArray.length; j++) {
                    writer.print(timeslotArray[j]);
                    if (j < timeslotArray.length - 1) {
                        writer.print(" "); // Add space between timeslot values
                    }
                }
                writer.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update the runExperiment method to handle the NullPointerException and improve logging
    private void runExperiment() {
        int minClashes = Integer.MAX_VALUE;
        int optimalShift = 0;
        int optimalIter = 0;
//        int[] shifts = {1, 5, 10, 15}; // first try iteration values
//        int[] iterations = {5, 10, 20, 30}; // first try iteration values
        int[] shifts = {5, 15, 25, 35, 45}; // second try iteration values
        int[] iterations = {5, 20, 35, 50, 65}; // second try iteration values

        try (PrintWriter log = new PrintWriter(new BufferedWriter(new FileWriter("2nd_log_sta-f-83.txt", true)))) {
            log.println("Experiment Log:");

            for (int shift : shifts) {
                for (int iter : iterations) {
                    this.field[4].setText(String.valueOf(shift));
                    this.field[3].setText(String.valueOf(iter));
                    int currentClashes = 0;

                    // Something goes wrong around here, I'm not sure what, but the minClashes doesn't always match the lowest currentClashes, sometimes it's a lower number
                    try {
                        for (int i = 1; i <= iter; ++i) {
                            this.courses.iterate(shift);
                            this.draw();
                            currentClashes = this.courses.clashesLeft();
                            autoUpdateTimeslots();
                            if (currentClashes < minClashes) {
                                minClashes = currentClashes;
                                optimalShift = shift;
                                optimalIter = iter;
                            }
                        }
                    } catch (NullPointerException e) {
                        // Log the error and continue to the next iteration
                        log.println("NullPointerException occurred during experiment:");
                        log.println("Shifts: " + shift + ", Iterations: " + iter);
                        log.println("Error message: " + e.getMessage());
                        e.printStackTrace(log);
                        continue; // Continue to the next iteration
                    }

                    // Reset the list of clash-free timeslots
                    List<Integer> clashFreeTimeslots = new ArrayList<>();

                    // Save clash-free timeslots to the list
                    for (int j = 1; j < this.courses.length(); j++) {
                        if (this.courses.status(j) == 0) { // Assuming status 0 indicates clash-free
                            clashFreeTimeslots.add(j);
                        }
                    }

                    // Save the clash-free timeslots to the log file
                    saveTimeslotsToLog(shift, iter, clashFreeTimeslots);

                    log.printf("Shifts: %d, Iterations: %d, Clashes: %d%n", shift, iter, currentClashes);
                }
            }

            log.printf("Optimal Shifts: %d, Optimal Iterations: %d, Minimum Clashes: %d%n", optimalShift, optimalIter, minClashes);
            // Train Autoassociator with clash-free timeslots
            trainAutoassociator();
        } catch (IOException e) {
            // Handle IOException
            e.printStackTrace();
        }
    }

    // Method to perform auto-update of timeslots using the trained autoassociator
    private void autoUpdateTimeslots() {
        // Get the current timeslots
        int[] currentTimeslots = courses.toArray();

        // Perform unit updates using the trained autoassociator
        int[] updatedTimeslots = autoassociator.performUnitUpdatesAndGetTimeslots(currentTimeslots, 1);

        // Update the timeslots in the CourseArray
        courses.setTimeslots(updatedTimeslots);
    }
}

