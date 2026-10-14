
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JobMatching extends JFrame {

    private JComboBox<StudentItem> studentCombo;
    private JTable resultTable;
    private DefaultTableModel resultModel;
    private JLabel statusLabel;

    public JobMatching() {

        Theme.install();

        setTitle("NEXUS | Job Matching Engine");
        setSize(1450, 820);
        setMinimumSize(new Dimension(1100, 680));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        Theme.applyWindow(this);

        createInterface();
        loadStudents();
    }

    // ================= INTERFACE =================

    private void createInterface() {

        setLayout(new BorderLayout());

        add(Theme.pageHeader(
                "Job Matching Engine",
                "Pick a student to see every job opening ranked by how well "
                        + "their profile matches the requirements.",
                Theme.Glyph.MATCH
        ), BorderLayout.NORTH);

        // Student selection
        studentCombo = new JComboBox<>();
        Theme.styleCombo(studentCombo);
        studentCombo.setPreferredSize(new Dimension(400, 42));

        JButton loadButton = Theme.button(
            "Load Students", Theme.Glyph.REFRESH, "secondary"
        );

        JButton matchButton = Theme.button(
            "Find Matching Jobs", Theme.Glyph.SEARCH, "primary"
        );

        Theme.Card selectionPanel = Theme.card(
            new Theme.HRow(14), 16
        );

        selectionPanel.add(Theme.labeled("Select student", studentCombo));
        selectionPanel.add(Theme.alignedWithFields(loadButton));
        selectionPanel.add(Theme.alignedWithFields(matchButton));

        // Status area
        statusLabel = new JLabel("Loading students...");
        Theme.styleInfoLabel(statusLabel);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        selectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        controls.add(selectionPanel);
        controls.add(Box.createVerticalStrut(14));
        controls.add(statusLabel);

        // Results table
        String[] columns = {
            "Job ID",
            "Company",
            "Job Title",
            "Minimum CGPA",
            "Department",
            "Required Skills",
            "Max Backlogs",
            "Match Score",
            "Eligibility"
        };

        resultModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultTable = new JTable(resultModel);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = Theme.tableScroll(resultTable);

        int[] widths = {
            85, 190, 210, 130, 150,
            300, 120, 125, 155
        };

        for (int i = 0; i < widths.length; i++) {
            resultTable.getColumnModel()
                .getColumn(i)
                .setPreferredWidth(widths[i]);
        }

        // Match score column
        resultTable.getColumnModel().getColumn(7)
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
        resultTable.getColumnModel().getColumn(8)
            .setCellRenderer(Theme.badgeRenderer(
                text -> text.equals("ELIGIBLE") ? Theme.SUCCESS : Theme.DANGER
            ));

        Theme.Card tableCard = Theme.card(new BorderLayout(0, 12), 16);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Center area
        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setBackground(Theme.BG);
        centerPanel.setBorder(
            BorderFactory.createEmptyBorder(22, 28, 24, 28)
        );

        centerPanel.add(controls, BorderLayout.NORTH);
        centerPanel.add(tableCard, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Button actions
        loadButton.addActionListener(e -> loadStudents());
        matchButton.addActionListener(e -> findMatchingJobs());

        studentCombo.addActionListener(e -> {
            resultModel.setRowCount(0);
            statusLabel.setText(
                "Student selected. Click Find Matching Jobs."
            );
        });
    }

    // ================= STUDENT ITEM =================

    private static class StudentItem {

        int id;
        String name;

        StudentItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // ================= JOB ITEM =================

    private static class JobItem {

        int id;
        String company;
        String title;
        double minimumCgpa;
        String department;
        String requiredSkills;
        int maximumBacklogs;

        JobItem(
            int id,
            String company,
            String title,
            double minimumCgpa,
            String department,
            String requiredSkills,
            int maximumBacklogs
        ) {

            this.id = id;
            this.company = company;
            this.title = title;
            this.minimumCgpa = minimumCgpa;
            this.department = department;
            this.requiredSkills = requiredSkills;
            this.maximumBacklogs = maximumBacklogs;
        }
    }

    // ================= LOAD STUDENTS =================

    private void loadStudents() {

        studentCombo.removeAllItems();
        resultModel.setRowCount(0);

        String sql = """
            SELECT student_id, roll_number, full_name
            FROM students
            ORDER BY full_name
            """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                String roll = rs.getString("roll_number");
                String name = rs.getString("full_name");

                studentCombo.addItem(
                    new StudentItem(
                        rs.getInt("student_id"),
                        roll + " - " + name
                    )
                );
            }

            if (studentCombo.getItemCount() == 0) {
                statusLabel.setText(
                    "No students found. Add students first."
                );
            } else {
                statusLabel.setText(
                    studentCombo.getItemCount()
                    + " student(s) loaded. Select one to find matching jobs."
                );
            }

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // ================= FIND MATCHING JOBS =================

    private void findMatchingJobs() {

        StudentItem student =
            (StudentItem) studentCombo.getSelectedItem();

        if (student == null) {
            showMessage("No student selected. Add a student first.");
            return;
        }

        resultModel.setRowCount(0);

        String studentSql = """
            SELECT student_id, full_name, department,
                   cgpa, skills, backlogs
            FROM students
            WHERE student_id = ?
            """;

        String jobsSql = """
            SELECT j.job_id, c.company_name, j.job_title,
                   j.minimum_cgpa, j.eligible_department,
                   j.required_skills, j.maximum_backlogs
            FROM jobs j
            JOIN companies c ON j.company_id = c.company_id
            """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement studentPs =
                con.prepareStatement(studentSql)
        ) {

            studentPs.setInt(1, student.id);

            try (ResultSet studentRs = studentPs.executeQuery()) {

                if (!studentRs.next()) {
                    showMessage("Student record not found.");
                    return;
                }

                String studentName =
                    studentRs.getString("full_name");

                String studentDepartment =
                    studentRs.getString("department");

                double studentCgpa =
                    studentRs.getDouble("cgpa");

                String studentSkills =
                    studentRs.getString("skills");

                int studentBacklogs =
                    studentRs.getInt("backlogs");

                List<Object[]> rows = new ArrayList<>();

                try (
                    PreparedStatement jobsPs =
                        con.prepareStatement(jobsSql);
                    ResultSet jobsRs = jobsPs.executeQuery()
                ) {

                    int totalJobs = 0;
                    int eligibleJobs = 0;

                    while (jobsRs.next()) {

                        totalJobs++;

                        JobItem job = new JobItem(
                            jobsRs.getInt("job_id"),
                            jobsRs.getString("company_name"),
                            jobsRs.getString("job_title"),
                            jobsRs.getDouble("minimum_cgpa"),
                            jobsRs.getString("eligible_department"),
                            jobsRs.getString("required_skills"),
                            jobsRs.getInt("maximum_backlogs")
                        );

                        boolean cgpaPass =
                            studentCgpa >= job.minimumCgpa;

                        boolean departmentPass =
                            departmentMatches(
                                studentDepartment, job.department
                            );

                        boolean backlogsPass =
                            studentBacklogs <= job.maximumBacklogs;

                        boolean eligible =
                            cgpaPass && departmentPass && backlogsPass;

                        int score = calculateMatchScore(
                            studentCgpa,
                            studentDepartment,
                            studentSkills,
                            studentBacklogs,
                            job
                        );

                        if (eligible) {
                            eligibleJobs++;
                        }

                        rows.add(new Object[]{
                            job.id,
                            job.company,
                            job.title,
                            job.minimumCgpa,
                            job.department == null
                                || job.department.trim().isEmpty()
                                ? "Any" : job.department,
                            job.requiredSkills == null
                                ? "" : job.requiredSkills,
                            job.maximumBacklogs,
                            score + "%",
                            eligible ? "ELIGIBLE" : "NOT ELIGIBLE",
                            score
                        });
                    }

                    // Sort by match score from highest to lowest.
                    rows.sort((a, b) ->
                        Integer.compare(
                            (Integer) b[9], (Integer) a[9]
                        )
                    );

                    for (Object[] row : rows) {
                        resultModel.addRow(new Object[]{
                            row[0], row[1], row[2], row[3],
                            row[4], row[5], row[6], row[7], row[8]
                        });
                    }

                    statusLabel.setText(
                        "Student: " + studentName
                        + "   |   Jobs checked: " + totalJobs
                        + "   |   Eligible jobs: " + eligibleJobs
                    );

                    if (totalJobs == 0) {
                        statusLabel.setText(
                            "No job openings found. Add jobs first."
                        );
                    }
                }
            }

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // ================= DEPARTMENT MATCHING =================

    private boolean departmentMatches(
        String studentDepartment,
        String eligibleDepartment
    ) {

        if (eligibleDepartment == null
            || eligibleDepartment.trim().isEmpty()
            || eligibleDepartment.trim().equalsIgnoreCase("ALL")
            || eligibleDepartment.trim().equalsIgnoreCase("ANY")) {
            return true;
        }

        if (studentDepartment == null) {
            return false;
        }

        for (String allowed : eligibleDepartment.split("[,;]")) {

            if (studentDepartment.trim()
                .equalsIgnoreCase(allowed.trim())) {
                return true;
            }
        }

        return false;
    }

    // ================= MATCH SCORE =================

    // CGPA 25%, department 25%, skills 35%, backlogs 15%.
    private int calculateMatchScore(
        double studentCgpa,
        String studentDepartment,
        String studentSkills,
        int studentBacklogs,
        JobItem job
    ) {

        int cgpaScore =
            studentCgpa >= job.minimumCgpa ? 25 : 0;

        int departmentScore =
            departmentMatches(studentDepartment, job.department)
                ? 25 : 0;

        int skillScore = calculateSkillScore(
            studentSkills, job.requiredSkills
        );

        int backlogScore =
            studentBacklogs <= job.maximumBacklogs ? 15 : 0;

        return cgpaScore + departmentScore
            + skillScore + backlogScore;
    }

    private int calculateSkillScore(
        String studentSkills,
        String requiredSkills
    ) {

        if (requiredSkills == null
            || requiredSkills.trim().isEmpty()) {
            return 35;
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

        int matched = 0;
        int total = 0;

        for (String skill : requiredSkills.split("[,;]")) {

            String cleanSkill =
                skill.trim().toLowerCase(Locale.ROOT);

            if (!cleanSkill.isEmpty()) {
                total++;

                if (studentSkillList.contains(cleanSkill)) {
                    matched++;
                }
            }
        }

        if (total == 0) {
            return 35;
        }

        return (int) Math.round(35.0 * matched / total);
    }

    // ================= MESSAGES =================

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
            "Database operation failed:\n" + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // ================= MAIN =================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new JobMatching().setVisible(true);
        });
    }
}