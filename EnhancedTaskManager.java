import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

/**
 * A task manager application with a graphical user interface (GUI).
 * Features include task management, progress tracking, and task completion.
 */
public class EnhancedTaskManager extends JFrame {
    private DefaultListModel<Task> taskListModel;
    private DefaultListModel<Task> inProgressListModel;
    private DefaultListModel<Task> completedListModel;

    public EnhancedTaskManager() {
        setTitle("Enhanced Task Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLayout(new BorderLayout());

        // Panels for task sections
        JPanel taskListPanel = createTaskListPanel();
        JPanel inProgressPanel = createInProgressPanel();
        JPanel completedPanel = createCompletedPanel();

        // Main layout with split panes
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taskListPanel, inProgressPanel);
        JSplitPane finalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainSplitPane, completedPanel);

        mainSplitPane.setDividerLocation(250);
        finalSplitPane.setDividerLocation(550);

        add(finalSplitPane);

        // Menu bar with save/load functionality
        setJMenuBar(createMenuBar());

        setVisible(true);
    }

    /**
     * Creates the task list panel.
     */
    private JPanel createTaskListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Task List"));

        taskListModel = new DefaultListModel<>();
        JList<Task> taskList = new JList<>(taskListModel);

        JButton addTaskButton = new JButton("Add Task");
        addTaskButton.addActionListener(e -> {
            String taskName = JOptionPane.showInputDialog("Enter Task Name:");
            if (taskName != null && !taskName.trim().isEmpty()) {
                taskListModel.addElement(new Task(taskName));
            }
        });

        JButton editTaskButton = new JButton("Edit Task");
        editTaskButton.addActionListener(e -> {
            int selectedIndex = taskList.getSelectedIndex();
            if (selectedIndex != -1) {
                String newName = JOptionPane.showInputDialog("Edit Task Name:", taskListModel.get(selectedIndex).getName());
                if (newName != null && !newName.trim().isEmpty()) {
                    taskListModel.get(selectedIndex).setName(newName);
                    taskList.repaint();
                }
            }
        });

        JButton moveToProgressButton = new JButton("Move to In Progress");
        moveToProgressButton.addActionListener(e -> {
            int selectedIndex = taskList.getSelectedIndex();
            if (selectedIndex != -1) {
                Task task = taskListModel.get(selectedIndex);
                taskListModel.remove(selectedIndex);
                inProgressListModel.addElement(task);
            }
        });

        panel.add(new JScrollPane(taskList), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addTaskButton);
        buttonPanel.add(editTaskButton);
        buttonPanel.add(moveToProgressButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the in-progress panel.
     */
    private JPanel createInProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("In Progress"));

        inProgressListModel = new DefaultListModel<>();
        JList<Task> inProgressList = new JList<>(inProgressListModel);

        JButton markAsCompletedButton = new JButton("Mark as Completed");
        markAsCompletedButton.addActionListener(e -> {
            int selectedIndex = inProgressList.getSelectedIndex();
            if (selectedIndex != -1) {
                Task task = inProgressListModel.get(selectedIndex);
                inProgressListModel.remove(selectedIndex);
                task.setProgress(100); // Automatically set progress to 100% when completed
                completedListModel.addElement(task);
            }
        });

        JButton setProgressButton = new JButton("Set Progress");
        setProgressButton.addActionListener(e -> {
            int selectedIndex = inProgressList.getSelectedIndex();
            if (selectedIndex != -1) {
                String input = JOptionPane.showInputDialog("Enter progress (0-100):");
                try {
                    int progress = Integer.parseInt(input);
                    if (progress >= 0 && progress <= 100) {
                        inProgressListModel.get(selectedIndex).setProgress(progress);
                        inProgressList.repaint();
                    } else {
                        JOptionPane.showMessageDialog(this, "Progress must be between 0 and 100.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter a number.");
                }
            }
        });

        panel.add(new JScrollPane(inProgressList), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(setProgressButton);
        buttonPanel.add(markAsCompletedButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the completed panel.
     */
    private JPanel createCompletedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Completed Tasks"));

        completedListModel = new DefaultListModel<>();
        JList<Task> completedList = new JList<>(completedListModel);

        panel.add(new JScrollPane(completedList), BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the menu bar for saving and loading tasks.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem saveMenuItem = new JMenuItem("Save Tasks");
        saveMenuItem.addActionListener(e -> saveTasksToFile());
        fileMenu.add(saveMenuItem);

        JMenuItem loadMenuItem = new JMenuItem("Load Tasks");
        loadMenuItem.addActionListener(e -> loadTasksFromFile());
        fileMenu.add(loadMenuItem);

        menuBar.add(fileMenu);

        return menuBar;
    }

    /**
     * Saves tasks to a file.
     */
    private void saveTasksToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("tasks.dat"))) {
            oos.writeObject(new ArrayList<>(taskListModel.elements()));
            oos.writeObject(new ArrayList<>(inProgressListModel.elements()));
            oos.writeObject(new ArrayList<>(completedListModel.elements()));
            JOptionPane.showMessageDialog(this, "Tasks saved successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving tasks: " + e.getMessage());
        }
    }

    /**
     * Loads tasks from a file.
     */
    private void loadTasksFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("tasks.dat"))) {
            taskListModel.clear();
            inProgressListModel.clear();
            completedListModel.clear();

            ((ArrayList<Task>) ois.readObject()).forEach(taskListModel::addElement);
            ((ArrayList<Task>) ois.readObject()).forEach(inProgressListModel::addElement);
            ((ArrayList<Task>) ois.readObject()).forEach(completedListModel::addElement);

            JOptionPane.showMessageDialog(this, "Tasks loaded successfully!");
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage());
        }
    }

    /**
     * The main method to run the application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnhancedTaskManager::new);
    }
}

/**
 * Represents a task with a name and progress.
 */
class Task implements Serializable {
    private String name;
    private int progress; // Progress percentage (0-100)

    public Task(String name) {
        this.name = name;
        this.progress = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public String toString() {
        return name + " (" + progress + "%)";
    }
}
