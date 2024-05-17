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

public class TimeTable extends JFrame implements ActionListener {
    private JPanel screen = new JPanel();
    private JPanel tools = new JPanel();
    private JButton[] tool;
    private JTextField[] field;
    private CourseArray courses;
    private Color[] CRScolor;
    private JButton continueButton; // New JButton for "Continue"

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
    }

    public void setTools() {
        String[] capField = new String[]{"Slots:", "Courses:", "Clash File:", "Iters:", "Shift:"};
        this.field = new JTextField[capField.length];
        String[] capButton = new String[]{"Load", "Start", "Step", "Print", "Exit", "Continue"}; // Added "Continue" button
        this.tool = new JButton[capButton.length];
        this.tools.setLayout(new GridLayout(2 * capField.length + capButton.length, 1));

        int i;
        for(i = 0; i < this.field.length; ++i) {
            this.tools.add(new JLabel(capField[i]));
            this.field[i] = new JTextField(5);
            this.tools.add(this.field[i]);
        }

        for(i = 0; i < this.tool.length; ++i) {
            this.tool[i] = new JButton(capButton[i]);
            this.tool[i].addActionListener(this);
            this.tools.add(this.tool[i]);
        }

        this.field[0].setText("13");
        this.field[1].setText("30");
        this.field[2].setText("sta-f-83.stu");
        this.field[3].setText("30");
    }

    public void draw() {
        Graphics g = this.screen.getGraphics();
        int width = Integer.parseInt(this.field[0].getText()) * 10;

        for(int courseIndex = 1; courseIndex < this.courses.length(); ++courseIndex) {
            g.setColor(this.CRScolor[this.courses.status(courseIndex) > 0 ? 0 : 1]);
            g.drawLine(0, courseIndex, width, courseIndex);
            g.setColor(this.CRScolor[this.CRScolor.length - 1]);
            g.drawLine(10 * this.courses.slot(courseIndex), courseIndex, 10 * this.courses.slot(courseIndex) + 10, courseIndex);
        }
    }

    private int getButtonIndex(JButton source) {
        int result;
        for(result = 0; source != this.tool[result]; ++result) {
        }

        return result;
    }

    public void actionPerformed(ActionEvent click) {
        int iteration;
        switch (this.getButtonIndex((JButton)click.getSource())) {
            case 0:
                int slots = Integer.parseInt(this.field[0].getText());
                this.courses = new CourseArray(Integer.parseInt(this.field[1].getText()) + 1, slots);
                this.courses.readClashes(this.field[2].getText());
                this.draw();
                break;
            case 1:
                int min = Integer.MAX_VALUE;
                int step = 0;

                for(iteration = 1; iteration < this.courses.length(); ++iteration) {
                    this.courses.setSlot(iteration, 0);
                }

                for(iteration = 1; iteration <= Integer.parseInt(this.field[3].getText()); ++iteration) {
                    this.courses.iterate(Integer.parseInt(this.field[4].getText()));
                    this.draw();
                    int clashes = this.courses.clashesLeft();
                    if (clashes < min) {
                        min = clashes;
                        step = iteration;
                    }
                }

                System.out.println("Shift = " + this.field[4].getText() + "\tMin clashes = " + min + "\tat step " + step);
                this.setVisible(true);
                break;
            case 2:
                this.courses.iterate(Integer.parseInt(this.field[4].getText()));
                this.draw();
                break;
            case 3:
                System.out.println("Exam\tSlot\tClashes");

                for(iteration = 1; iteration < this.courses.length(); ++iteration) {
                    System.out.println("" + iteration + "\t" + this.courses.slot(iteration) + "\t" + this.courses.status(iteration));
                }

                return;
            case 4:
                System.exit(0);
                break;
            case 5: // New case for "Continue" button
                // Continue the scheduling algorithm from the current state
                if (this.courses != null) {
                    int minn = Integer.MAX_VALUE;
                    int stepp = 0;

                    for (iteration = 1; iteration <= Integer.parseInt(this.field[3].getText()); ++iteration) {
                        this.courses.iterate(Integer.parseInt(this.field[4].getText()));
                        this.draw();
                        int clashes = this.courses.clashesLeft();
                        if (clashes < minn) {
                            minn = clashes;
                            stepp = iteration;
                        }
                    }

                    System.out.println("Shift = " + this.field[4].getText() + "\tMin clashes = " + minn + "\tat step " + stepp);
                    this.setVisible(true);
                } else {
                    System.out.println("Please load courses first!");
                }
                break;
        }
    }

    public static void main(String[] args) {
        new TimeTable();
    }
}
