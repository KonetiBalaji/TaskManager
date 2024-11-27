import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Task Manager Application
 * Provides functionality to manage tasks, track their progress, and mark them as completed.
 * Includes features like task sorting, saving/loading tasks, and clearing completed tasks.
 */
public class ExtendedTaskManager extends JFrame {
    private DefaultListModel<Task> taskListModel;
    private DefaultListModel<Task> inProgressListModel;
    private DefaultListModel<Task> completedListModel;

    public ExtendedTaskManager() {
        // Set up the main window
        setTitle("Extended Task Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        // Create panels for different sections
        JPanel taskListPanel = createTaskListPanel();
        JPanel inProgressPanel = createInProgressPanel();
        JPanel completedPanel = createCompletedPanel();

        // Create a main split pane for better layout
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taskListPanel, inProgressPanel);
        JSplitPane finalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainSplitPane, completedPanel);

        mainSplitPane.setDividerLocation(300);
        finalSplitPane.setDividerLocation(600);

        // Add split pane to the main frame
        add(finalSplitPane);

        // Add menu bar with additional options
        setJMenuBar(createMenuBar());

        setVisible(true);
    }

    /**
     * Creates the Task List panel.
     */
    private JPanel createTaskListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Task List"));

        taskListModel = new DefaultListModel<>();
        JList<Task> taskList = new JList<>(taskListModel);

        // Button to add a new task
        JButton addTaskButton = new JButton("Add Task");
        addTaskButton.addActionListener(e -> {
            String taskName = JOptionPane.showInputDialog("Enter Task Name:");
            if (taskName != null && !taskName.trim().isEmpty()) {
                taskListModel.addElement(new Task(taskName));
            }
        });

        // Button to sort tasks alphabetically
        JButton sortTasksButton = new JButton("Sort Tasks");
        sortTasksButton.addActionListener(e -> {
            ArrayList<Task> tasks = Collections.list(taskListModel.elements());
            tasks.sort(Comparator.comparing(Task::getName));
            taskListModel.clear();
            tasks.forEach(taskListModel::addElement);
        });

        // Button to move a task to In Progress
        JButton moveToProgressButton = new JButton("Move to In Progress");
        moveToProgressButton.addActionListener(e -> {
            int selectedIndex = taskList.getSelectedIndex();
            if (selectedIndex != -1) {
                Task task = taskListModel.get(selectedIndex);
                taskListModel.remove(selectedIndex);
                inProgressListModel.addElement(task);
            }
        });

        // Arrange buttons in a panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addTaskButton);
        buttonPanel.add(sortTasksButton);
        buttonPanel.add(moveToProgressButton);

        panel.add(new JScrollPane(taskList), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the In Progress panel.
     */
    private JPanel createInProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("In Progress"));

        inProgressListModel = new DefaultListModel<>();
        JList<Task> inProgressList = new JList<>(inProgressListModel);

        // Button to update task progress
        JButton updateProgressButton = new JButton("Update Progress");
        updateProgressButton.addActionListener(e -> {
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

        // Button to mark task as completed
        JButton markAsCompletedButton = new JButton("Mark as Completed");
        markAsCompletedButton.addActionListener(e -> {
            int selectedIndex = inProgressList.getSelectedIndex();
            if (selectedIndex != -1) {
                Task task = inProgressListModel.get(selectedIndex);
                inProgressListModel.remove(selectedIndex);
                task.setProgress(100);
                completedListModel.addElement(task);
            }
        });

        // Arrange buttons in a panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(updateProgressButton);
        buttonPanel.add(markAsCompletedButton);

        panel.add(new JScrollPane(inProgressList), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the Completed Tasks panel.
     */
    private JPanel createCompletedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Completed Tasks"));

        completedListModel = new DefaultListModel<>();
        JList<Task> completedList = new JList<>(completedListModel);

        // Button to clear all completed tasks
        JButton clearCompletedButton = new JButton("Clear All");
        clearCompletedButton.addActionListener(e -> completedListModel.clear());

        panel.add(new JScrollPane(completedList), BorderLayout.CENTER);
        panel.add(clearCompletedButton, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the menu bar with Save and Load options.
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
            oos.writeObject(Collections.list(taskListModel.elements()));
            oos.writeObject(Collections.list(inProgressListModel.elements()));
            oos.writeObject(Collections.list(completedListModel.elements()));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExtendedTaskManager::new);
    }
}

/**
 * Represents a Task with a name and progress percentage.
 */
class Task implements Serializable {
    private String name;
    private int progress;

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
