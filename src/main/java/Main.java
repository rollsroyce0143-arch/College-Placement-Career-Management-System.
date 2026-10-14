import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Main {

    // ---------- COMPONENTS ----------
    private static JFrame frame;

    private static JTextField rollField;
    private static JTextField nameField;
    private static JTextField departmentField;
    private static JTextField emailField;
    private static JTextField cgpaField;
    private static JTextField skillsField;
    private static JTextField backlogsField;

    private static JTable studentTable;
    private static DefaultTableModel tableModel;
    private static JLabel countLabel;

    // ---------- OPEN WINDOW ----------
    public static void main(String[] args) {
        openWindow();
    }

    // Dashboard can use this method to open Student Management.
    public static void openWindow() {
        if (SwingUtilities.isEventDispatchThread()) {
            createWindow();
        } else {
            SwingUtilities.invokeLater(Main::createWindow);
        }
    }

    private static void createWindow() {
        Theme.install();

        frame = new JFrame("NEXUS | Student Management");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1180, 790);
        frame.setMinimumSize(new Dimension(1000, 680));
        frame.setLocationRelativeTo(null);
        Theme.applyWindow(frame);
        frame.setLayout(new BorderLayout(0, 0));

        frame.add(
                Theme.pageHeader(
                        "Student Management",
                        "Register students, keep academic records up to date and "
                                + "list the skills that matter for placements.",
                        Theme.Glyph.STUDENT
                ),
                BorderLayout.NORTH
        );

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(Theme.BG);
        content.setBorder(new EmptyBorder(22, 28, 24, 28));

        content.add(createFormPanel(), BorderLayout.NORTH);
        content.add(createTablePanel(), BorderLayout.CENTER);

        frame.add(content, BorderLayout.CENTER);

        frame.setVisible(true);
        loadStudents();
    }

    // ---------- FORM PANEL ----------
    private static JPanel createFormPanel() {

        Theme.Card card = Theme.card(new BorderLayout(0, 16), 20);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel heading = Theme.sectionTitle("Student Details");
        JLabel help = Theme.hint(
                "Fill in the form and press Add Student. "
                        + "Select a row in the table below to edit or delete it."
        );
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(heading);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(help);

        rollField = Theme.textField();
        nameField = Theme.textField();
        departmentField = Theme.textField();
        emailField = Theme.textField();
        cgpaField = Theme.textField();
        skillsField = Theme.textField();
        backlogsField = Theme.textField();

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        addField(fields, "Roll Number", rollField, 0, 0, 1);
        addField(fields, "Full Name", nameField, 1, 0, 1);
        addField(fields, "Department", departmentField, 2, 0, 1);
        addField(fields, "Email Address", emailField, 3, 0, 1);
        addField(fields, "CGPA (0 - 10)", cgpaField, 0, 1, 1);
        addField(fields, "Skills (comma separated)", skillsField, 1, 1, 2);
        addField(fields, "Backlogs", backlogsField, 3, 1, 1);

        JButton addButton = Theme.button("Add Student", Theme.Glyph.PLUS, "success");
        JButton updateButton = Theme.button("Update", Theme.Glyph.EDIT, "primary");
        JButton deleteButton = Theme.button("Delete", Theme.Glyph.TRASH, "danger");
        JButton clearButton = Theme.button("Clear Form", Theme.Glyph.CLEAR, "secondary");
        JButton refreshButton = Theme.button("Refresh", Theme.Glyph.REFRESH, "secondary");

        addButton.addActionListener(e -> addStudent());
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> loadStudents());

        JPanel buttonPanel = new JPanel(new Theme.HRow(10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(titleBlock, BorderLayout.NORTH);
        top.add(fields, BorderLayout.CENTER);

        card.add(top, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private static void addField(
            JPanel grid, String caption, JTextField field,
            int column, int row, int width
    ) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = column;
        c.gridy = row;
        c.gridwidth = width;
        c.weightx = width;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(row == 0 ? 0 : 14, column == 0 ? 0 : 8, 0, 8);
        grid.add(Theme.labeled(caption, field), c);
    }

    // ---------- TABLE PANEL ----------
    private static JPanel createTablePanel() {

        Theme.Card panel = Theme.card(new BorderLayout(0, 14), 20);

        JLabel heading = Theme.sectionTitle("Registered Students");

        countLabel = new JLabel("0 students");
        countLabel.setFont(Theme.font(Font.BOLD, 12));
        countLabel.setForeground(Theme.PRIMARY);
        countLabel.setOpaque(true);
        countLabel.setBackground(Theme.PRIMARY_SOFT);
        countLabel.setBorder(new EmptyBorder(5, 12, 5, 12));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(heading, BorderLayout.WEST);
        titleRow.add(countLabel, BorderLayout.EAST);

        String[] columns = {
                "ID", "Roll Number", "Full Name", "Department",
                "Email", "CGPA", "Skills", "Backlogs"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableModel.addTableModelListener(e -> {
            int n = tableModel.getRowCount();
            countLabel.setText(n + (n == 1 ? " student" : " students"));
        });

        studentTable = new JTable(tableModel);
        studentTable.setAutoCreateRowSorter(true);

        int[] widths = {50, 110, 160, 110, 200, 65, 190, 80};
        for (int i = 0; i < widths.length; i++) {
            studentTable.getColumnModel().getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        JScrollPane scrollPane = Theme.tableScroll(studentTable);

        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });

        panel.add(titleRow, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ---------- ADD STUDENT ----------
    private static void addStudent() {
        if (!validateForm()) {
            return;
        }

        String sql = "INSERT INTO students " +
                "(roll_number, full_name, department, email, " +
                "cgpa, skills, backlogs) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            fillStudentStatement(ps);

            int result = ps.executeUpdate();

            if (result > 0) {
                showMessage("Student added successfully.", "Success");
                loadStudents();
                clearForm();
            }

        } catch (SQLIntegrityConstraintViolationException ex) {
            showError(
                    "This roll number may already exist. " +
                    "Check the entered details."
            );
        } catch (SQLException ex) {
            showError("Unable to add student.\n" + ex.getMessage());
        }
    }

    // ---------- UPDATE STUDENT ----------
    private static void updateStudent() {
        int selectedRow = studentTable.getSelectedRow();

        if (selectedRow == -1) {
            showError("Please select a student from the table to update.");
            return;
        }

        if (!validateForm()) {
            return;
        }

        int modelRow = studentTable.convertRowIndexToModel(selectedRow);
        int studentId = Integer.parseInt(
                tableModel.getValueAt(modelRow, 0).toString()
        );

        String sql = "UPDATE students SET roll_number=?, full_name=?, " +
                "department=?, email=?, cgpa=?, skills=?, backlogs=? " +
                "WHERE student_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            fillStudentStatement(ps);
            ps.setInt(8, studentId);

            int result = ps.executeUpdate();

            if (result > 0) {
                showMessage("Student updated successfully.", "Success");
                loadStudents();
                clearForm();
            } else {
                showError("No student record was updated.");
            }

        } catch (SQLIntegrityConstraintViolationException ex) {
            showError(
                    "The roll number may already belong to another student."
            );
        } catch (SQLException ex) {
            showError("Unable to update student.\n" + ex.getMessage());
        }
    }

    // ---------- DELETE STUDENT ----------
    private static void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();

        if (selectedRow == -1) {
            showError("Please select a student from the table to delete.");
            return;
        }

        int modelRow = studentTable.convertRowIndexToModel(selectedRow);
        int studentId = Integer.parseInt(
                tableModel.getValueAt(modelRow, 0).toString()
        );

        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to delete this student?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM students WHERE student_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            int result = ps.executeUpdate();

            if (result > 0) {
                showMessage("Student deleted successfully.", "Success");
                loadStudents();
                clearForm();
            } else {
                showError("Student record not found.");
            }

        } catch (SQLIntegrityConstraintViolationException ex) {
            showError(
                    "This student is linked to other placement records. " +
                    "Remove or update those linked records first."
            );
        } catch (SQLException ex) {
            showError("Unable to delete student.\n" + ex.getMessage());
        }
    }

    // ---------- LOAD STUDENTS ----------
    private static void loadStudents() {
        String sql = "SELECT student_id, roll_number, full_name, " +
                "department, email, cgpa, skills, backlogs " +
                "FROM students ORDER BY student_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            tableModel.setRowCount(0);

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("student_id"),
                        rs.getString("roll_number"),
                        rs.getString("full_name"),
                        rs.getString("department"),
                        rs.getString("email"),
                        rs.getBigDecimal("cgpa"),
                        rs.getString("skills"),
                        rs.getInt("backlogs")
                };

                tableModel.addRow(row);
            }

        } catch (SQLException ex) {
            showError("Unable to load students.\n" + ex.getMessage());
        }
    }

    // ---------- FORM HELPERS ----------
    private static void fillStudentStatement(PreparedStatement ps)
            throws SQLException {

        String email = emailField.getText().trim();
        String skills = skillsField.getText().trim();

        ps.setString(1, rollField.getText().trim());
        ps.setString(2, nameField.getText().trim());
        ps.setString(3, departmentField.getText().trim());
        ps.setString(4, email.isEmpty() ? null : email);
        ps.setDouble(5, Double.parseDouble(cgpaField.getText().trim()));
        ps.setString(6, skills.isEmpty() ? null : skills);
        ps.setInt(7, Integer.parseInt(backlogsField.getText().trim()));
    }

    private static boolean validateForm() {
        String roll = rollField.getText().trim();
        String name = nameField.getText().trim();
        String department = departmentField.getText().trim();
        String cgpaText = cgpaField.getText().trim();
        String backlogsText = backlogsField.getText().trim();

        if (roll.isEmpty() || name.isEmpty()
                || department.isEmpty()
                || cgpaText.isEmpty() || backlogsText.isEmpty()) {
            showError(
                    "Please fill in Roll Number, Full Name, Department, " +
                    "CGPA and Backlogs."
            );
            return false;
        }

        try {
            double cgpa = Double.parseDouble(cgpaText);
            if (cgpa < 0 || cgpa > 10) {
                showError("CGPA must be between 0 and 10.");
                return false;
            }
        } catch (NumberFormatException ex) {
            showError("Please enter a valid numeric CGPA.");
            return false;
        }

        try {
            int backlogs = Integer.parseInt(backlogsText);
            if (backlogs < 0) {
                showError("Backlogs cannot be negative.");
                return false;
            }
        } catch (NumberFormatException ex) {
            showError("Please enter a valid whole number for Backlogs.");
            return false;
        }

        return true;
    }

    private static void fillFormFromSelection() {
        int selectedRow = studentTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow = studentTable.convertRowIndexToModel(selectedRow);

        rollField.setText(valueAt(modelRow, 1));
        nameField.setText(valueAt(modelRow, 2));
        departmentField.setText(valueAt(modelRow, 3));
        emailField.setText(valueAt(modelRow, 4));
        cgpaField.setText(valueAt(modelRow, 5));
        skillsField.setText(valueAt(modelRow, 6));
        backlogsField.setText(valueAt(modelRow, 7));
    }

    private static String valueAt(int row, int column) {
        Object value = tableModel.getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private static void clearForm() {
        rollField.setText("");
        nameField.setText("");
        departmentField.setText("");
        emailField.setText("");
        cgpaField.setText("");
        skillsField.setText("");
        backlogsField.setText("");

        studentTable.clearSelection();
        rollField.requestFocusInWindow();
    }

    // ---------- MESSAGE HELPERS ----------
    private static void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(
                frame,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static void showError(String message) {
        JOptionPane.showMessageDialog(
                frame,
                message,
                "NEXUS | Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}