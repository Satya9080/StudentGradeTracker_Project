
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CodeAlpha Task 1 - Professional Student Grade Tracker
 *
 * Features:
 * - Login screen
 * - Dashboard
 * - Add / Edit / Delete students
 * - Search students
 * - Automatic grade letter and Pass/Fail
 * - Average / highest / lowest statistics
 * - Persistent CSV file storage
 * - Logout
 *
 * Demo login:
 * Username: admin
 * Password: admin123
 */
public class StudentGradeTracker extends JFrame {

    private static final String DATA_FILE = "students.csv";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123";

    private final List<Student> students = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    private JLabel totalLabel;
    private JLabel averageLabel;
    private JLabel highestLabel;
    private JLabel lowestLabel;
    private JLabel passedLabel;
    private JLabel failedLabel;

   public static void main(String[] args) {
    try {
        System.out.println("Starting application...");

        SwingUtilities.invokeAndWait(() -> {
            StudentGradeTracker app = new StudentGradeTracker();
            app.showLogin();
        });

    } catch (Exception e) {
        e.printStackTrace();

        JOptionPane.showMessageDialog(
            null,
            e.toString(),
            "Application Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}

    public StudentGradeTracker() {
        setTitle("Student Grade Tracker");
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------------- LOGIN ----------------

    private void showLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Student Grade Tracker ", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel subtitle = new JLabel("Admin Login", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton exitButton = new JButton("Exit");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridy++;
        panel.add(subtitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(loginButton, gbc);
        gbc.gridx = 1;
        panel.add(exitButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JLabel hint = new JLabel("Demo: admin / admin123", SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);
        panel.add(hint, gbc);

        setContentPane(panel);
        setTitle("Login - Student Grade Tracker Pro");
        revalidate();
        repaint();
        setLocationRelativeTo(null);
        setVisible(true);

        loginButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());

            if (USERNAME.equals(user) && PASSWORD.equals(pass)) {
                loadStudents();
                showDashboard();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
                passwordField.setText("");
            }
        });

        passwordField.addActionListener(e -> loginButton.doClick());
        exitButton.addActionListener(e -> System.exit(0));

        usernameField.requestFocusInWindow();
    }

    // ---------------- DASHBOARD ----------------

    private void showDashboard() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JPanel header = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Student Grade Tracker");
        title.setFont(new Font("Arial", Font.BOLD, 25));

        JButton logoutButton = new JButton("Logout");
        header.add(title, BorderLayout.WEST);
        header.add(logoutButton, BorderLayout.EAST);

        // Search
        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Student"));

        searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchButtons.add(searchButton);
        searchButtons.add(showAllButton);
        searchPanel.add(searchButtons, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.add(header, BorderLayout.NORTH);
        top.add(searchPanel, BorderLayout.CENTER);

        // Table
        String[] columns = {
                "ID", "Student Name", "Grade", "Letter", "Result"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Student Records"));

        // Buttons
        JButton addButton = new JButton("Add Student");
        JButton editButton = new JButton("Edit Student");
        JButton deleteButton = new JButton("Delete Student");
        JButton refreshButton = new JButton("Refresh");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(refreshButton);

        // Statistics
        totalLabel = createStatLabel("Total: 0");
        averageLabel = createStatLabel("Average: 0.00");
        highestLabel = createStatLabel("Highest: -");
        lowestLabel = createStatLabel("Lowest: -");
        passedLabel = createStatLabel("Passed: 0");
        failedLabel = createStatLabel("Failed: 0");

        JPanel stats = new JPanel(new GridLayout(2, 3, 8, 8));
        stats.setBorder(BorderFactory.createTitledBorder("Dashboard Summary"));
        stats.add(totalLabel);
        stats.add(averageLabel);
        stats.add(highestLabel);
        stats.add(lowestLabel);
        stats.add(passedLabel);
        stats.add(failedLabel);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(actions, BorderLayout.NORTH);
        bottom.add(stats, BorderLayout.CENTER);

        main.add(top, BorderLayout.NORTH);
        main.add(scrollPane, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
        setTitle("Dashboard - Student Grade Tracker Pro");
        revalidate();
        repaint();

        refreshTable(students);
        updateStatistics(students);

        addButton.addActionListener(e -> showStudentDialog(null));
        editButton.addActionListener(e -> editSelectedStudent());
        deleteButton.addActionListener(e -> deleteSelectedStudent());
        refreshButton.addActionListener(e -> {
            loadStudents();
            refreshTable(students);
            updateStatistics(students);
        });

        searchButton.addActionListener(e -> searchStudents());
        searchField.addActionListener(e -> searchStudents());
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable(students);
            updateStatistics(students);
        });

        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Do you want to logout?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                showLogin();
            }
        });
    }

    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 15));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 5, 10, 5)
        ));
        return label;
    }

    // ---------------- STUDENT CRUD ----------------

    private void showStudentDialog(Student student) {
        boolean editing = student != null;

        JTextField nameField = new JTextField(editing ? student.name : "");
        JTextField gradeField = new JTextField(
                editing ? String.valueOf(student.grade) : ""
        );

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Student Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Grade (0-100):"));
        panel.add(gradeField);

        String title = editing ? "Edit Student" : "Add Student";

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String gradeText = gradeField.getText().trim();

        if (name.isEmpty()) {
            showError("Student name cannot be empty.");
            return;
        }

        double grade;

        try {
            grade = Double.parseDouble(gradeText);
        } catch (NumberFormatException ex) {
            showError("Grade must be a valid number.");
            return;
        }

        if (grade < 0 || grade > 100) {
            showError("Grade must be between 0 and 100.");
            return;
        }

        if (editing) {
            student.name = name;
            student.grade = grade;
        } else {
            students.add(new Student(nextId(), name, grade));
        }

        saveStudents();
        refreshTable(students);
        updateStatistics(students);
    }

    private void editSelectedStudent() {
        int row = table.getSelectedRow();

        if (row == -1) {
            showWarning("Please select a student to edit.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        int id = Integer.parseInt(
                tableModel.getValueAt(modelRow, 0).toString()
        );

        Student student = findById(id);

        if (student != null) {
            showStudentDialog(student);
        }
    }

    private void deleteSelectedStudent() {
        int row = table.getSelectedRow();

        if (row == -1) {
            showWarning("Please select a student to delete.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        int id = Integer.parseInt(
                tableModel.getValueAt(modelRow, 0).toString()
        );

        Student student = findById(id);

        if (student == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete " + student.name + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            students.remove(student);
            saveStudents();
            refreshTable(students);
            updateStatistics(students);
        }
    }

    private int nextId() {
        int max = 0;
        for (Student s : students) {
            max = Math.max(max, s.id);
        }
        return max + 1;
    }

    private Student findById(int id) {
        for (Student s : students) {
            if (s.id == id) {
                return s;
            }
        }
        return null;
    }

    // ---------------- SEARCH ----------------

    private void searchStudents() {
        String query = searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            refreshTable(students);
            updateStatistics(students);
            return;
        }

        List<Student> filtered = students.stream()
                .filter(s ->
                        s.name.toLowerCase().contains(query)
                                || String.valueOf(s.id).equals(query)
                                || gradeLetter(s.grade).toLowerCase().equals(query)
                                || result(s.grade).toLowerCase().equals(query)
                )
                .collect(Collectors.toList());

        refreshTable(filtered);
        updateStatistics(filtered);
    }

    // ---------------- TABLE & STATS ----------------

    private void refreshTable(List<Student> list) {
        if (tableModel == null) {
            return;
        }

        tableModel.setRowCount(0);

        for (Student s : list) {
            tableModel.addRow(new Object[]{
                    s.id,
                    s.name,
                    String.format("%.2f", s.grade),
                    gradeLetter(s.grade),
                    result(s.grade)
            });
        }
    }

    private void updateStatistics(List<Student> list) {
        if (list == null || list.isEmpty()) {
            totalLabel.setText("Total: 0");
            averageLabel.setText("Average: 0.00");
            highestLabel.setText("Highest: -");
            lowestLabel.setText("Lowest: -");
            passedLabel.setText("Passed: 0");
            failedLabel.setText("Failed: 0");
            return;
        }

        double sum = 0;
        Student highest = list.get(0);
        Student lowest = list.get(0);
        int passed = 0;
        int failed = 0;

        for (Student s : list) {
            sum += s.grade;

            if (s.grade > highest.grade) {
                highest = s;
            }

            if (s.grade < lowest.grade) {
                lowest = s;
            }

            if (s.grade >= 40) {
                passed++;
            } else {
                failed++;
            }
        }

        double average = sum / list.size();

        totalLabel.setText("Total: " + list.size());
        averageLabel.setText(String.format("Average: %.2f", average));
        highestLabel.setText(
                String.format("Highest: %.2f (%s)", highest.grade, highest.name)
        );
        lowestLabel.setText(
                String.format("Lowest: %.2f (%s)", lowest.grade, lowest.name)
        );
        passedLabel.setText("Passed: " + passed);
        failedLabel.setText("Failed: " + failed);
    }

    private String gradeLetter(double grade) {
        if (grade >= 90) return "A+";
        if (grade >= 80) return "A";
        if (grade >= 70) return "B";
        if (grade >= 60) return "C";
        if (grade >= 50) return "D";
        if (grade >= 40) return "E";
        return "F";
    }

    private String result(double grade) {
        return grade >= 40 ? "PASS" : "FAIL";
    }

    // ---------------- FILE STORAGE ----------------

    private void saveStudents() {
        Path path = Paths.get(DATA_FILE);

        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8
        )) {
            writer.write("id,name,grade");
            writer.newLine();

            for (Student s : students) {
                writer.write(
                        s.id + "," +
                        escapeCsv(s.name) + "," +
                        s.grade
                );
                writer.newLine();
            }

        } catch (IOException ex) {
            showError("Could not save student data:\n" + ex.getMessage());
        }
    }

    private void loadStudents() {
        students.clear();

        Path path = Paths.get(DATA_FILE);

        if (!Files.exists(path)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(
                path,
                StandardCharsets.UTF_8
        )) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                List<String> fields = parseCsv(line);

                if (fields.size() != 3) {
                    continue;
                }

                try {
                    int id = Integer.parseInt(fields.get(0));
                    String name = fields.get(1);
                    double grade = Double.parseDouble(fields.get(2));

                    students.add(new Student(id, name, grade));
                } catch (NumberFormatException ignored) {
                    // Skip malformed rows.
                }
            }

        } catch (IOException ex) {
            showError("Could not load student data:\n" + ex.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private List<String> parseCsv(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (quoted && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        fields.add(current.toString());
        return fields;
    }

    // ---------------- HELPERS ----------------

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Warning",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private static class Student {
        int id;
        String name;
        double grade;

        Student(int id, String name, double grade) {
            this.id = id;
            this.name = name;
            this.grade = grade;
        }
    }
}
