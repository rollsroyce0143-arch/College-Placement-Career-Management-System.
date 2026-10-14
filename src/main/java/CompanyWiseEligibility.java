
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompanyWiseEligibility extends JFrame {

    private JComboBox<CompanyItem> companyCombo;
    private JComboBox<JobItem> jobCombo;

    private JTable studentTable;
    private DefaultTableModel tableModel;

    private JTextField searchField;
    private JLabel countLabel;

    public CompanyWiseEligibility() {
        Theme.install();

        setTitle("NEXUS | Company-Wise Eligibility");
        setSize(1450, 820);
        setMinimumSize(new Dimension(1100, 680));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        Theme.applyWindow(this);

        createInterface();
        loadCompanies();
    }

    // ================= BUILD INTERFACE =================

    private void createInterface() {
        setLayout(new BorderLayout(0, 0));

        add(Theme.pageHeader(
                "Company-Wise Eligibility",
                "Choose a company and job opening to see which students "
                        + "meet its CGPA, department and backlog requirements.",
                Theme.Glyph.ELIGIBLE
        ), BorderLayout.NORTH);

        // Dropdowns
        companyCombo = new JComboBox<>();
        jobCombo = new JComboBox<>();

        Theme.styleCombo(companyCombo);
        Theme.styleCombo(jobCombo);

        companyCombo.setPreferredSize(new Dimension(240, 42));
        jobCombo.setPreferredSize(new Dimension(300, 42));

        // Buttons
        JButton findButton = Theme.button(
                "Find Eligible Students", Theme.Glyph.SEARCH, "primary"
        );
        JButton refreshButton = Theme.button(
                "Refresh", Theme.Glyph.REFRESH, "secondary"
        );
        JButton exportButton = Theme.button(
                "Export CSV", Theme.Glyph.EXPORT, "secondary"
        );

        // Selection card
        Theme.Card selectionPanel = Theme.card(
                new Theme.HRow(14), 16
        );

        selectionPanel.add(Theme.labeled("Company", companyCombo));
        selectionPanel.add(Theme.labeled("Job Opening", jobCombo));
        selectionPanel.add(Theme.alignedWithFields(findButton));
        selectionPanel.add(Theme.alignedWithFields(refreshButton));
        selectionPanel.add(Theme.alignedWithFields(exportButton));

        // Search card
        searchField = new JTextField(20);
        Theme.styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(320, 42));

        JButton searchButton = Theme.button(
                "Search Student", Theme.Glyph.SEARCH, "secondary"
        );

        Theme.Card searchPanel = Theme.card(
                new Theme.HRow(14), 16
        );

        searchPanel.add(Theme.labeled("Search by name or roll number", searchField));
        searchPanel.add(Theme.alignedWithFields(searchButton));

        // Status label
        countLabel = new JLabel("Select a company and job opening.");
        Theme.styleInfoLabel(countLabel);
        countLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        selectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        controls.add(selectionPanel);
        controls.add(Box.createVerticalStrut(12));
        controls.add(searchPanel);
        controls.add(Box.createVerticalStrut(14));
        controls.add(countLabel);

        // Student table
        String[] columns = {
            "Student ID", "Roll Number", "Student Name",
            "Department", "CGPA", "Skills", "Backlogs",
            "Skill Match", "Eligibility"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {
            115, 140, 210, 145, 85, 300, 100, 120, 140
        };

        JScrollPane scrollPane = Theme.tableScroll(studentTable);

        for (int i = 0; i < widths.length; i++) {
            studentTable.getColumnModel()
                .getColumn(i)
                .setPreferredWidth(widths[i]);
        }

        // Skill match column (percentage badge)
        studentTable.getColumnModel().getColumn(7)
            .setCellRenderer(Theme.badgeRenderer(text -> {
                int score = 0;
                try {
                    score = Integer.parseInt(text.replace("%", "").trim());
                } catch (Exception ignored) {
                }
                if (score >= 75) {
                    return Theme.SUCCESS;
                } else if (score >= 50) {
                    return Theme.BLUE;
                }
                return Theme.MUTED;
            }));

        // Eligibility column
        studentTable.getColumnModel().getColumn(8)
            .setCellRenderer(Theme.badgeRenderer(text -> Theme.SUCCESS));

        Theme.Card tableCard = Theme.card(new BorderLayout(0, 12), 16);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(Theme.BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(22, 28, 24, 28));
        mainPanel.add(controls, BorderLayout.NORTH);
        mainPanel.add(tableCard, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // ================= BUTTON ACTIONS =================

        companyCombo.addActionListener(e -> loadJobsForCompany());

        findButton.addActionListener(e -> findEligibleStudents());

        refreshButton.addActionListener(e -> {
            loadCompanies();
            tableModel.setRowCount(0);
            searchField.setText("");
            countLabel.setText(
                "Select a company and job opening."
            );
        });

        searchButton.addActionListener(e -> searchStudents());
        searchField.addActionListener(e -> searchStudents());
        exportButton.addActionListener(e -> exportCsv());
    }

    // ================= DATA ITEMS =================

    private static class CompanyItem {
        int id;
        String name;

        CompanyItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class JobItem {
        int id;
        int companyId;
        String title;
        double minimumCgpa;
        String department;
        String requiredSkills;
        int maximumBacklogs;

        JobItem(
            int id, int companyId, String title,
            double minimumCgpa, String department,
            String requiredSkills, int maximumBacklogs
        ) {
            this.id = id;
            this.title = title;
            this.minimumCgpa = minimumCgpa;
            this.department = department;
            this.requiredSkills = requiredSkills;
            this.maximumBacklogs = maximumBacklogs;
        }

        @Override
        public String toString() {
            return title + " (Job ID: " + id + ")";
        }
    }

    // ================= LOAD COMPANIES =================

    private void loadCompanies() {
        companyCombo.removeAllItems();

        String sql = """
            SELECT company_id, company_name
            FROM companies
            ORDER BY company_name
            """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                companyCombo.addItem(
                    new CompanyItem(
                        rs.getInt("company_id"),
                        rs.getString("company_name")
                    )
                );
            }

            if (companyCombo.getItemCount() == 0) {
                countLabel.setText(
                    "No companies found. Add a company first."
                );
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // ================= LOAD JOBS =================

    private void loadJobsForCompany() {
        jobCombo.removeAllItems();
        tableModel.setRowCount(0);

        CompanyItem company =
            (CompanyItem) companyCombo.getSelectedItem();

        if (company == null) {
            return;
        }

        String sql = """
            SELECT job_id, company_id, job_title,
                   minimum_cgpa, eligible_department,
                   required_skills, maximum_backlogs
            FROM jobs
            WHERE company_id = ?
            ORDER BY job_title
            """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, company.id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jobCombo.addItem(
                        new JobItem(
                            rs.getInt("job_id"),
                            rs.getInt("company_id"),
                            rs.getString("job_title"),
                            rs.getDouble("minimum_cgpa"),
                            rs.getString("eligible_department"),
                            rs.getString("required_skills"),
                            rs.getInt("maximum_backlogs")
                        )
                    );
                }
            }

            if (jobCombo.getItemCount() == 0) {
                countLabel.setText(
                    "This company has no job openings."
                );
            } else {
                countLabel.setText(
                    "Choose a job and click Find Eligible Students."
                );
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // ================= ELIGIBILITY CHECK =================

    private void findEligibleStudents() {
        JobItem job = (JobItem) jobCombo.getSelectedItem();

        if (job == null) {
            showMessage(
                "Select a company with a job opening first."
            );
            return;
        }

        tableModel.setRowCount(0);

        String sql = """
            SELECT student_id, roll_number, full_name,
                   department, cgpa, skills, backlogs
            FROM students
            ORDER BY full_name
            """;

        int totalStudents = 0;
        int eligibleCount = 0;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                totalStudents++;

                int studentId = rs.getInt("student_id");
                String rollNumber = rs.getString("roll_number");
                String name = rs.getString("full_name");
                String department = rs.getString("department");
                double cgpa = rs.getDouble("cgpa");
                String skills = rs.getString("skills");
                int backlogs = rs.getInt("backlogs");

                boolean cgpaPass = cgpa >= job.minimumCgpa;
                boolean departmentPass =
                    departmentMatches(department, job.department);
                boolean backlogsPass =
                    backlogs <= job.maximumBacklogs;

                boolean eligible =
                    cgpaPass && departmentPass && backlogsPass;

                if (eligible) {
                    eligibleCount++;

                    int skillScore = calculateSkillScore(
                        skills, job.requiredSkills
                    );

                    tableModel.addRow(new Object[]{
                        studentId,
                        rollNumber,
                        name,
                        department,
                        cgpa,
                        skills == null ? "" : skills,
                        backlogs,
                        skillScore + "%",
                        "ELIGIBLE"
                    });
                }
            }

            countLabel.setText(
                "Company: " + getSelectedCompanyName()
                + "   |   Job: " + job.title
                + "   |   Students checked: " + totalStudents
                + "   |   Eligible students: " + eligibleCount
            );

            if (eligibleCount == 0) {
                showMessage(
                    "No students satisfy this job's mandatory criteria."
                );
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // ================= DEPARTMENT MATCHING =================

    private boolean departmentMatches(
        String studentDepartment, String jobDepartment
    ) {
        if (jobDepartment == null
            || jobDepartment.trim().isEmpty()
            || jobDepartment.trim().equalsIgnoreCase("ALL")
            || jobDepartment.trim().equalsIgnoreCase("ANY")) {
            return true;
        }

        if (studentDepartment == null) {
            return false;
        }

        for (String allowed : jobDepartment.split("[,;]")) {
            if (studentDepartment.trim()
                .equalsIgnoreCase(allowed.trim())) {
                return true;
            }
        }

        return false;
    }

    // ================= SKILL MATCHING =================

    private int calculateSkillScore(
        String studentSkills, String requiredSkills
    ) {
        if (requiredSkills == null
            || requiredSkills.trim().isEmpty()) {
            return 100;
        }

        if (studentSkills == null
            || studentSkills.trim().isEmpty()) {
            return 0;
        }

        List<String> studentSkillList = new ArrayList<>();

        for (String skill : studentSkills.split("[,;]")) {
            studentSkillList.add(
                skill.trim().toLowerCase(Locale.ROOT)
            );
        }

        int requiredCount = 0;
        int matchedCount = 0;

        for (String required : requiredSkills.split("[,;]")) {
            String skill = required.trim().toLowerCase(Locale.ROOT);

            if (skill.isEmpty()) {
                continue;
            }

            requiredCount++;

            if (studentSkillList.contains(skill)) {
                matchedCount++;
            }
        }

        if (requiredCount == 0) {
            return 100;
        }

        return (int) Math.round(
            100.0 * matchedCount / requiredCount
        );
    }

    // ================= SEARCH STUDENTS =================

    private void searchStudents() {
        String search = searchField.getText()
            .trim()
            .toLowerCase(Locale.ROOT);

        if (search.isEmpty()) {
            showMessage(
                "Enter a roll number or student name to search."
            );
            return;
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String roll = String.valueOf(
                tableModel.getValueAt(i, 1)
            ).toLowerCase(Locale.ROOT);

            String name = String.valueOf(
                tableModel.getValueAt(i, 2)
            ).toLowerCase(Locale.ROOT);

            if (roll.contains(search) || name.contains(search)) {
                studentTable.setRowSelectionInterval(i, i);
                studentTable.scrollRectToVisible(
                    studentTable.getCellRect(i, 0, true)
                );
                return;
            }
        }

        showMessage(
            "No matching student found in the eligible list."
        );
    }

    // ================= CSV EXPORT =================

    private void exportCsv() {
        if (tableModel.getRowCount() == 0) {
            showMessage(
                "Find eligible students before exporting."
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Eligible Students List");
        chooser.setSelectedFile(new File("eligible_students.csv"));

        int result = chooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                if (col > 0) {
                    writer.print(",");
                }
                writer.print(
                    csvEscape(tableModel.getColumnName(col))
                );
            }
            writer.println();

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    if (col > 0) {
                        writer.print(",");
                    }

                    Object value = tableModel.getValueAt(row, col);

                    writer.print(
                        csvEscape(value == null ? "" : value.toString())
                    );
                }
                writer.println();
            }

            showMessage(
                "Eligible students exported successfully:\n"
                + file.getAbsolutePath()
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not export CSV:\n" + ex.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String csvEscape(String value) {
        // Prevent spreadsheet cells from being treated as formulas.
        if (!value.isEmpty()
            && "=+-@".indexOf(value.charAt(0)) >= 0) {
            value = "'" + value;
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // ================= COMMON METHODS =================

    private String getSelectedCompanyName() {
        CompanyItem company =
            (CompanyItem) companyCombo.getSelectedItem();

        return company == null ? "" : company.name;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "NEXUS Placement System",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showDatabaseError(SQLException ex) {
        ex.printStackTrace();

        JOptionPane.showMessageDialog(
            this,
            "Database error:\n" + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // ================= MAIN METHOD =================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CompanyWiseEligibility().setVisible(true);
        });
    }
}