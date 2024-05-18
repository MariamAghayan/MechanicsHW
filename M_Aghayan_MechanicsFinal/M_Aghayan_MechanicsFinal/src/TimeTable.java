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
        this.courses = new CourseArray(150, 13); // Initialize courses
        this.autoassociator = new Autoassociator(this.courses); // Initialize Autoassociator with courses

    }


    public void setTools() {
        String[] capField = new String[]{"Slots:", "Courses:", "Clash File:", "Iters:", "Shift:"};
        this.field = new JTextField[capField.length];
        String[] capButton = new String[]{"Load", "Start", "Step", "Print", "Exit", "Continue"};
        this.tool = new JButton[capButton.length];
        this.tools.setLayout(new GridLayout(2 * capField.length + capButton.length, 1));

        int i;
        for (i = 0; i < this.field.length; ++i) {
            this.tools.add(new JLabel(capField[i]));
            this.field[i] = new JTextField(5);
            this.tools.add(this.field[i]);
        }

        for (i = 0; i < this.tool.length; ++i) {
            this.tool[i] = new JButton(capButton[i]);
            this.tool[i].addActionListener(this);
            this.tools.add(this.tool[i]);
        }

        this.field[0].setText("13");
        this.field[1].setText("150");
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
                this.courses = new CourseArray(Integer.parseInt(this.field[1].getText()) + 1, slots);
                this.courses.readClashes(this.field[2].getText());
                this.draw();
                break;
            case 1:
                runExperiment();
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
        // Get clash-free timeslots from Task 3 (assuming stored in an array named clashFreeTimeslots)
        int[] clashFreeTimeslots = getClashFreeTimeslotsFromTask3();
        // Train Autoassociator with clash-free timeslots
        autoassociator.training(clashFreeTimeslots);
    }

    // Method to get clash-free timeslots from Task 3 (for demonstration purposes)
    private int[] getClashFreeTimeslotsFromTask3() {
        // Simulating clash-free timeslots for demonstration
        // In a real scenario, you would obtain these from Task 3
        int numOfCourses = courses.length();
        Random random = new Random();
        int[] clashFreeTimeslots = new int[numOfCourses];
        for (int i = 0; i < numOfCourses; i++) {
            clashFreeTimeslots[i] = random.nextInt(numOfCourses); // Assuming timeslots are within the range [0, numOfCourses)
        }
        return clashFreeTimeslots;
    }

    // Method to save used timeslots to a log file
    private void saveTimeslotsToLog(int[] timeslots, int shift, int iterationIndex) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("timeslots_log.txt")))) {
            writer.println("Number of Slots: " + timeslots.length);
            writer.println("Shift: " + shift);
            writer.println("Iteration Index: " + iterationIndex);

            writer.println("Timeslots:");
            for (int i = 0; i < timeslots.length; i++) {
                writer.println("Timeslot Index " + i + ": " + timeslots[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void runExperiment() {
        int minClashes = Integer.MAX_VALUE;
        int optimalShift = 0;
        int optimalIter = 0;
        int[] shifts = {1, 5, 10, 15}; // Example shift values
        int[] iterations = {5, 10, 20, 30}; // Example iteration values

        try (PrintWriter log = new PrintWriter(new BufferedWriter(new FileWriter("log4.txt", true)))) {
            log.println("Experiment Log:");

            for (int shift : shifts) {
                for (int iter : iterations) {
                    this.field[4].setText(String.valueOf(shift));
                    this.field[3].setText(String.valueOf(iter));
                    int currentClashes = 0;

                    for (int i = 1; i <= iter; ++i) {
                        this.courses.iterate(shift);
                        this.draw();
                        currentClashes = this.courses.clashesLeft();
                        if (currentClashes < minClashes) {
                            minClashes = currentClashes;
                            optimalShift = shift;
                            optimalIter = iter;
                        }
                        // Perform auto-update of timeslots using the trained autoassociator
                        autoUpdateTimeslots();
// Save each update instance in a log file
                        saveUpdateInstanceToLog(courses.toArray(), shift, iter);
                    }

                    log.printf("Shifts: %d, Iterations: %d, Clashes: %d%n", shift, iter, currentClashes);
                }
            }

            log.printf("Optimal Shifts: %d, Optimal Iterations: %d, Minimum Clashes: %d%n", optimalShift, optimalIter, minClashes);

            // Train Autoassociator with clash-free timeslots
            trainAutoassociator();
        } catch (IOException e) {
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


    // Method to save each update instance in a log file
    // Method to save each update instance in a log file
    private void saveUpdateInstanceToLog(int[] updatedTimeslots, int shiftValue, int iterationIndex) {
        // Call the method to save the timeslots to the log file
        saveTimeslotsToLog(updatedTimeslots, shiftValue, iterationIndex);
    }



}

