
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.Locale;

public class ApplicationTracking extends JFrame {

    private JComboBox<CompanyItem> companyCombo;
    private JComboBox<JobItem> jobCombo;
    private JComboBox<String> statusCombo;

    private JTable table;
    private DefaultTableModel model;

    private JTextField searchField;
    private JLabel countLabel;

    public ApplicationTracking() {

        Theme.install();

        setTitle("NEXUS | Application Tracking");
        setSize(1450, 840);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Theme.applyWindow(this);

        createUI();
        loadCompanies();
    }

    // ================= BUILD INTERFACE =================

    private void createUI() {

        setLayout(new BorderLayout());

        add(Theme.pageHeader(
                "Application Tracking",
                "Record student applications and move them through "
                        + "Applied, Shortlisted, Selected or Rejected.",
                Theme.Glyph.CLIPBOARD
        ), BorderLayout.NORTH);

        // Company and job selectors
        companyCombo = new JComboBox<>();
        jobCombo = new JComboBox<>();

        Theme.styleCombo(companyCombo);
        Theme.styleCombo(jobCombo);

        companyCombo.setPreferredSize(new Dimension(240, 42));
        jobCombo.setPreferredSize(new Dimension(290, 42));

        JButton loadButton = Theme.button(
            "Load Applications", Theme.Glyph.SEARCH, "primary"
        );

        JButton addButton = Theme.button(
            "Add Application", Theme.Glyph.PLUS, "success"
        );

        Theme.Card jobPanel = Theme.card(
            new Theme.HRow(14), 16
        );

        jobPanel.add(Theme.labeled("Company", companyCombo));
        jobPanel.add(Theme.labeled("Job Opening", jobCombo));
        jobPanel.add(Theme.alignedWithFields(loadButton));
        jobPanel.add(Theme.alignedWithFields(addButton));

        // Status controls
        statusCombo = new JComboBox<>(new String[]{
            "Applied", "Shortlisted", "Selected", "Rejected"
        });

        Theme.styleCombo(statusCombo);
        statusCombo.setPreferredSize(new Dimension(170, 42));

        JButton updateButton = Theme.button(
            "Update Status", Theme.Glyph.EDIT, "primary"
        );

        searchField = new JTextField(15);
        Theme.styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(240, 42));

        JButton searchButton = Theme.button(
            "Search", Theme.Glyph.SEARCH, "secondary"
        );

        JButton exportButton = Theme.button(
            "Export CSV", Theme.Glyph.EXPORT, "secondary"
        );

        JButton refreshButton = Theme.button(
            "Refresh", Theme.Glyph.REFRESH, "secondary"
        );

        Theme.Card actionPanel = Theme.card(
            new Theme.HRow(14), 16
        );

        actionPanel.add(Theme.labeled("New status", statusCombo));
        actionPanel.add(Theme.alignedWithFields(updateButton));
        actionPanel.add(Theme.labeled("Search name / roll", searchField));
        actionPanel.add(Theme.alignedWithFields(searchButton));
        actionPanel.add(Theme.alignedWithFields(exportButton));
        actionPanel.add(Theme.alignedWithFields(refreshButton));

        // Application summary
        countLabel = new JLabel("Select a company and job opening.");
        Theme.styleInfoLabel(countLabel);
        countLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        jobPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        controls.add(jobPanel);
        controls.add(Box.createVerticalStrut(12));
        controls.add(actionPanel);
        controls.add(Box.createVerticalStrut(14));
        controls.add(countLabel);

        // Application table
        String[] columns = {
            "Application ID",
            "Student ID",
            "Roll Number",
            "Student Name",
            "Department",
            "CGPA",
            "Job Title",
            "Application Date",
            "Status"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = Theme.tableScroll(table);

        int[] widths = {
            135, 115, 145, 210, 145,
            85, 220, 150, 140
        };

        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel()
                .getColumn(i)
                .setPreferredWidth(widths[i]);
        }

        // Status column with colour-coded badges
        table.getColumnModel().getColumn(8)
            .setCellRenderer(Theme.badgeRenderer(text -> {
                switch (text.toLowerCase(Locale.ROOT)) {
                    case "selected":
                        return Theme.SUCCESS;
                    case "shortlisted":
                        return Theme.BLUE;
                    case "rejected":
                        return Theme.DANGER;
                    default:
                        return Theme.WARNING;
                }
            }));

        Theme.Card tableCard = Theme.card(new BorderLayout(0, 12), 16);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Main panel
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setBackground(Theme.BG);
        center.setBorder(
            BorderFactory.createEmptyBorder(22, 28, 24, 28)
        );

        center.add(controls, BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        // Actions
        companyCombo.addActionListener(e -> loadJobs());

        loadButton.addActionListener(e -> loadApplications());
        addButton.addActionListener(e -> addApplication());
        updateButton.addActionListener(e -> updateStatus());
        searchButton.addActionListener(e -> searchApplications());
        searchField.addActionListener(e -> searchApplications());
        exportButton.addActionListener(e -> exportCSV());

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadApplications();
        });
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
        String title;

        JobItem(int id, String title) {
            this.id = id;
            this.title = title;
        }

        @Override
        public String toString() {
            return title + " (ID: " + id + ")";
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
                    "Add a company before continuing."
                );
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ================= LOAD JOBS =================

    private void loadJobs() {
        jobCombo.removeAllItems();
        model.setRowCount(0);

        CompanyItem company =
            (CompanyItem) companyCombo.getSelectedItem();

        if (company == null) {
            return;
        }

        String sql = """
            SELECT job_id, job_title
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
                            rs.getString("job_title")
                        )
                    );
                }
            }

            countLabel.setText(
                jobCombo.getItemCount() == 0
                    ? "This company has no job openings."
                    : "Select a job and load its applications."
            );

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ================= LOAD APPLICATIONS =================

    private void loadApplications() {
        JobItem job = (JobItem) jobCombo.getSelectedItem();

        if (job == null) {
            showMessage("Please select a job.");
            return;
        }

        model.setRowCount(0);

        String sql = """
            SELECT a.application_id, s.student_id, s.roll_number,
                   s.full_name, s.department, s.cgpa,
                   j.job_title, a.application_date, a.application_status
            FROM applications a
            JOIN students s ON a.student_id = s.student_id
            JOIN jobs j ON a.job_id = j.job_id
            WHERE a.job_id = ?
            ORDER BY a.application_date DESC, s.full_name
            """;

        int applied = 0;
        int shortlisted = 0;
        int selected = 0;
        int rejected = 0;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, job.id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("application_status");

                    if ("Applied".equalsIgnoreCase(status)) {
                        applied++;
                    } else if ("Shortlisted".equalsIgnoreCase(status)) {
                        shortlisted++;
                    } else if ("Selected".equalsIgnoreCase(status)) {
                        selected++;
                    } else if ("Rejected".equalsIgnoreCase(status)) {
                        rejected++;
                    }

                    model.addRow(new Object[]{
                        rs.getInt("application_id"),
                        rs.getInt("student_id"),
                        rs.getString("roll_number"),
                        rs.getString("full_name"),
                        rs.getString("department"),
                        rs.getDouble("cgpa"),
                        rs.getString("job_title"),
                        rs.getDate("application_date"),
                        status
                    });
                }
            }

            countLabel.setText(
                "Applications: " + model.getRowCount()
                + "   |   Applied: " + applied
                + "   |   Shortlisted: " + shortlisted
                + "   |   Selected: " + selected
                + "   |   Rejected: " + rejected
            );

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ================= ADD APPLICATION =================

    private void addApplication() {
        JobItem job = (JobItem) jobCombo.getSelectedItem();

        if (job == null) {
            showMessage("Please select a job.");
            return;
        }

        String roll = JOptionPane.showInputDialog(
            this, "Enter the student's roll number:"
        );

        if (roll == null || roll.trim().isEmpty()) {
            return;
        }

        String sqlStudent = """
            SELECT student_id, full_name, department, cgpa, skills, backlogs
            FROM students
            WHERE roll_number = ?
            """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sqlStudent)
        ) {
            ps.setString(1, roll.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    showMessage("Student not found.");
                    return;
                }

                int studentId = rs.getInt("student_id");
                String studentName = rs.getString("full_name");

                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Add application for " + studentName
                    + "?\nJob: " + job.title
                    + "\n\nPlease verify the student's eligibility first.",
                    "Confirm Application",
                    JOptionPane.YES_NO_OPTION
                );

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                // Prevent duplicate applications.
                String duplicateSQL = """
                    SELECT COUNT(*)
                    FROM applications
                    WHERE student_id = ? AND job_id = ?
                    """;

                try (
                    PreparedStatement duplicate =
                        con.prepareStatement(duplicateSQL)
                ) {
                    duplicate.setInt(1, studentId);
                    duplicate.setInt(2, job.id);

                    try (ResultSet duplicateResult =
                             duplicate.executeQuery()) {

                        duplicateResult.next();

                        if (duplicateResult.getInt(1) > 0) {
                            showMessage(
                                "This student already has an application for this job."
                            );
                            return;
                        }
                    }
                }

                String insertSQL = """
                    INSERT INTO applications
                        (student_id, job_id, application_date, application_status)
                    VALUES (?, ?, ?, ?)
                    """;

                try (PreparedStatement insert =
                         con.prepareStatement(insertSQL)) {

                    insert.setInt(1, studentId);
                    insert.setInt(2, job.id);
                    insert.setDate(3, Date.valueOf(LocalDate.now()));
                    insert.setString(4, "Applied");
                    insert.executeUpdate();
                }
            }

            showMessage("Application added successfully.");
            loadApplications();

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ================= UPDATE STATUS =================

    private void updateStatus() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            showMessage("Select an application row first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        int applicationId = Integer.parseInt(
            model.getValueAt(modelRow, 0).toString()
        );

        String newStatus = (String) statusCombo.getSelectedItem();

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Change application ID " + applicationId
            + " to '" + newStatus + "'?",
            "Confirm Status Update",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = """
            UPDATE applications
            SET application_status = ?
            WHERE application_id = ?
            """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, newStatus);
            ps.setInt(2, applicationId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                showMessage("Status updated successfully.");
                loadApplications();
            } else {
                showMessage("No application was updated.");
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ================= SEARCH APPLICATIONS =================

    private void searchApplications() {
        String search = searchField.getText()
            .trim()
            .toLowerCase(Locale.ROOT);

        if (search.isEmpty()) {
            showMessage("Enter a name or roll number.");
            return;
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            String roll = String.valueOf(
                model.getValueAt(i, 2)
            ).toLowerCase(Locale.ROOT);

            String name = String.valueOf(
                model.getValueAt(i, 3)
            ).toLowerCase(Locale.ROOT);

            if (roll.contains(search) || name.contains(search)) {
                int viewRow = table.convertRowIndexToView(i);

                if (viewRow >= 0) {
                    table.setRowSelectionInterval(viewRow, viewRow);
                    table.scrollRectToVisible(
                        table.getCellRect(viewRow, 0, true)
                    );
                }
                return;
            }
        }

        showMessage(
            "No matching application found for this job."
        );
    }

    // ================= EXPORT CSV =================

    private void exportCSV() {
        if (model.getRowCount() == 0) {
            showMessage("Load applications before exporting.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Applications");
        chooser.setSelectedFile(new File("applications.csv"));

        if (chooser.showSaveDialog(this)
            != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (PrintWriter writer = new PrintWriter(file)) {

            for (int col = 0; col < model.getColumnCount(); col++) {
                if (col > 0) {
                    writer.print(",");
                }

                writer.print(csvEscape(model.getColumnName(col)));
            }
            writer.println();

            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    if (col > 0) {
                        writer.print(",");
                    }

                    Object value = model.getValueAt(row, col);

                    writer.print(
                        csvEscape(value == null ? "" : value.toString())
                    );
                }
                writer.println();
            }

            showMessage(
                "CSV saved successfully:\n" + file.getAbsolutePath()
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Export failed:\n" + ex.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String csvEscape(String value) {
        // Protect spreadsheet exports against formula interpretation.
        if (!value.isEmpty()
            && "=+-@".indexOf(value.charAt(0)) >= 0) {
            value = "'" + value;
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // ================= COMMON METHODS =================

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "NEXUS Placement System",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(SQLException ex) {
        ex.printStackTrace();

        JOptionPane.showMessageDialog(
            this,
            "Database error:\n" + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // ================= MAIN =================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new ApplicationTracking().setVisible(true)
        );
    }
}