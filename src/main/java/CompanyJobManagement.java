import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CompanyJobManagement extends JFrame {

    // Company form fields
    private JTextField companyNameField;
    private JTextField locationField;
    private JTable companyTable;
    private DefaultTableModel companyModel;
    private Integer selectedCompanyId = null;

    // Job form fields
    private JComboBox<CompanyItem> companyCombo;
    private JTextField jobTitleField;
    private JTextField minimumCgpaField;
    private JTextField departmentField;
    private JTextField requiredSkillsField;
    private JTextField maximumBacklogsField;
    private JTable jobTable;
    private DefaultTableModel jobModel;
    private Integer selectedJobId = null;

    public CompanyJobManagement() {
        Theme.install();

        setTitle("NEXUS | Company & Job Management");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1050, 680));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        Theme.applyWindow(this);
        setLayout(new BorderLayout());

        add(Theme.pageHeader(
                "Company & Job Management",
                "Add recruiting companies and publish the job openings "
                        + "students can apply for.",
                Theme.Glyph.BUILDING
        ), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Company Management", createCompanyPanel());
        tabs.addTab("Job Management", createJobPanel());
        Theme.styleTabs(tabs);
        add(tabs, BorderLayout.CENTER);

        loadCompanies();
        loadCompanyChoices();
        loadJobs();
    }

    // ================= COMPANY MANAGEMENT =================

    private JPanel createCompanyPanel() {
        JPanel panel = basePanel();

        Theme.Card form = Theme.card(new BorderLayout(0, 16), 20);

        companyNameField = Theme.textField();
        locationField = Theme.textField();

        JPanel fields = new JPanel(new GridLayout(1, 2, 16, 0));
        fields.setOpaque(false);
        fields.add(Theme.labeled("Company Name *", companyNameField));
        fields.add(Theme.labeled("Location", locationField));

        JButton addButton = Theme.button("Add Company", Theme.Glyph.PLUS, "success");
        JButton updateButton = Theme.button("Update Company", Theme.Glyph.EDIT, "primary");
        JButton deleteButton = Theme.button("Delete Company", Theme.Glyph.TRASH, "danger");
        JButton clearButton = Theme.button("Clear Form", Theme.Glyph.CLEAR, "secondary");
        JButton refreshButton = Theme.button("Refresh", Theme.Glyph.REFRESH, "secondary");

        JPanel buttons = createButtonBar();
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);
        buttons.add(refreshButton);

        form.add(formTitle(
                "Company Details",
                "Enter the company name and location, then press Add Company. "
                        + "Select a row below to edit or delete it."
        ), BorderLayout.NORTH);
        form.add(fields, BorderLayout.CENTER);
        form.add(buttons, BorderLayout.SOUTH);

        companyModel = new DefaultTableModel(
                new String[]{"Company ID", "Company Name", "Location"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        companyTable = createTable(companyModel);
        companyTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        companyTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        companyTable.getColumnModel().getColumn(2).setPreferredWidth(250);

        panel.add(form, BorderLayout.NORTH);
        panel.add(createTablePanel("Registered Companies", companyTable), BorderLayout.CENTER);

        addButton.addActionListener(e -> addCompany());
        updateButton.addActionListener(e -> updateCompany());
        deleteButton.addActionListener(e -> deleteCompany());
        clearButton.addActionListener(e -> clearCompanyForm());
        refreshButton.addActionListener(e -> {
            loadCompanies();
            loadCompanyChoices();
        });

        companyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = companyTable.getSelectedRow();
                if (row >= 0) {
                    selectedCompanyId = Integer.parseInt(
                            companyModel.getValueAt(row, 0).toString());
                    companyNameField.setText(valueAt(companyModel, row, 1));
                    locationField.setText(valueAt(companyModel, row, 2));
                }
            }
        });

        return panel;
    }

    private void addCompany() {
        String name = companyNameField.getText().trim();
        String location = locationField.getText().trim();

        if (name.isEmpty()) {
            showMessage("Please enter a company name.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO companies (company_name, location) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, location.isEmpty() ? null : location);
            ps.executeUpdate();

            showMessage("Company added successfully!", JOptionPane.INFORMATION_MESSAGE);
            clearCompanyForm();
            loadCompanies();
            loadCompanyChoices();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void updateCompany() {
        if (selectedCompanyId == null) {
            showMessage("Select a company from the table first.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = companyNameField.getText().trim();
        String location = locationField.getText().trim();
        if (name.isEmpty()) {
            showMessage("Company name cannot be empty.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE companies SET company_name = ?, location = ? WHERE company_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, location.isEmpty() ? null : location);
            ps.setInt(3, selectedCompanyId);

            int rows = ps.executeUpdate();
            showMessage(rows > 0 ? "Company updated successfully!" : "Company was not found.",
                    rows > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

            clearCompanyForm();
            loadCompanies();
            loadCompanyChoices();
            loadJobs();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void deleteCompany() {
        if (selectedCompanyId == null) {
            showMessage("Select a company from the table first.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete this company?\nCompanies linked to job openings cannot be deleted.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM companies WHERE company_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, selectedCompanyId);
            int rows = ps.executeUpdate();

            showMessage(rows > 0 ? "Company deleted successfully!" : "Company was not found.",
                    rows > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

            clearCompanyForm();
            loadCompanies();
            loadCompanyChoices();
        } catch (SQLException ex) {
            if ("23000".equals(ex.getSQLState())) {
                showMessage("Cannot delete this company because job openings are linked to it.\nDelete or reassign its jobs first.",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                showDatabaseError(ex);
            }
        }
    }

    private void loadCompanies() {
        if (companyModel == null) return;
        companyModel.setRowCount(0);

        String sql = "SELECT company_id, company_name, location FROM companies ORDER BY company_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                companyModel.addRow(new Object[]{
                        rs.getInt("company_id"),
                        rs.getString("company_name"),
                        rs.getString("location")
                });
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void clearCompanyForm() {
        companyNameField.setText("");
        locationField.setText("");
        selectedCompanyId = null;
        companyTable.clearSelection();
    }

    // ================= JOB MANAGEMENT =================

    private JPanel createJobPanel() {
        JPanel panel = basePanel();

        Theme.Card form = Theme.card(new BorderLayout(0, 16), 20);

        companyCombo = new JComboBox<>();
        Theme.styleCombo(companyCombo);
        jobTitleField = Theme.textField();
        minimumCgpaField = Theme.textField();
        departmentField = Theme.textField();
        requiredSkillsField = Theme.textField();
        maximumBacklogsField = Theme.textField();
        maximumBacklogsField.setText("0");

        JPanel fields = new JPanel(new GridLayout(2, 3, 16, 14));
        fields.setOpaque(false);
        fields.add(Theme.labeled("Company *", companyCombo));
        fields.add(Theme.labeled("Job Title *", jobTitleField));
        fields.add(Theme.labeled("Minimum CGPA (0 - 10) *", minimumCgpaField));
        fields.add(Theme.labeled("Eligible Department *", departmentField));
        fields.add(Theme.labeled("Required Skills", requiredSkillsField));
        fields.add(Theme.labeled("Maximum Backlogs", maximumBacklogsField));

        JButton addButton = Theme.button("Add Job", Theme.Glyph.PLUS, "success");
        JButton updateButton = Theme.button("Update Job", Theme.Glyph.EDIT, "primary");
        JButton deleteButton = Theme.button("Delete Job", Theme.Glyph.TRASH, "danger");
        JButton clearButton = Theme.button("Clear Form", Theme.Glyph.CLEAR, "secondary");
        JButton refreshButton = Theme.button("Refresh Jobs", Theme.Glyph.REFRESH, "secondary");
        JButton reloadCompaniesButton = Theme.button("Reload Companies", Theme.Glyph.BUILDING, "secondary");

        JPanel buttons = createButtonBar();
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);
        buttons.add(refreshButton);
        buttons.add(reloadCompaniesButton);

        form.add(formTitle(
                "Job Opening Details",
                "Pick the company, then set the eligibility rules students "
                        + "must meet to apply for this role."
        ), BorderLayout.NORTH);
        form.add(fields, BorderLayout.CENTER);
        form.add(buttons, BorderLayout.SOUTH);

        jobModel = new DefaultTableModel(
                new String[]{"Job ID", "Company ID", "Company", "Job Title",
                        "Minimum CGPA", "Department", "Required Skills", "Max Backlogs"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        jobTable = createTable(jobModel);
        jobTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        jobTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        jobTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        jobTable.getColumnModel().getColumn(3).setPreferredWidth(190);
        jobTable.getColumnModel().getColumn(4).setPreferredWidth(145);
        jobTable.getColumnModel().getColumn(5).setPreferredWidth(135);
        jobTable.getColumnModel().getColumn(6).setPreferredWidth(210);
        jobTable.getColumnModel().getColumn(7).setPreferredWidth(135);

        panel.add(form, BorderLayout.NORTH);
        panel.add(createTablePanel("Available Job Openings", jobTable), BorderLayout.CENTER);

        addButton.addActionListener(e -> addJob());
        updateButton.addActionListener(e -> updateJob());
        deleteButton.addActionListener(e -> deleteJob());
        clearButton.addActionListener(e -> clearJobForm());
        refreshButton.addActionListener(e -> loadJobs());
        reloadCompaniesButton.addActionListener(e -> loadCompanyChoices());

        jobTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = jobTable.getSelectedRow();
                if (row >= 0) {
                    selectedJobId = Integer.parseInt(valueAt(jobModel, row, 0));
                    int companyId = Integer.parseInt(valueAt(jobModel, row, 1));

                    jobTitleField.setText(valueAt(jobModel, row, 3));
                    minimumCgpaField.setText(valueAt(jobModel, row, 4));
                    departmentField.setText(valueAt(jobModel, row, 5));
                    requiredSkillsField.setText(valueAt(jobModel, row, 6));
                    maximumBacklogsField.setText(valueAt(jobModel, row, 7));
                    selectCompany(companyId);
                }
            }
        });

        return panel;
    }

    private void addJob() {
        CompanyItem company = (CompanyItem) companyCombo.getSelectedItem();
        if (company == null) {
            showMessage("Please add a company first.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateJobForm()) return;

        String sql = """
                INSERT INTO jobs
                (company_id, job_title, minimum_cgpa,
                 eligible_department, required_skills, maximum_backlogs)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            fillJobStatement(ps, company.id);
            ps.executeUpdate();

            showMessage("Job opening added successfully!", JOptionPane.INFORMATION_MESSAGE);
            clearJobForm();
            loadJobs();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void updateJob() {
        if (selectedJobId == null) {
            showMessage("Select a job from the table first.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CompanyItem company = (CompanyItem) companyCombo.getSelectedItem();
        if (company == null) {
            showMessage("Please select a company.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateJobForm()) return;

        String sql = """
                UPDATE jobs
                SET company_id = ?, job_title = ?, minimum_cgpa = ?,
                    eligible_department = ?, required_skills = ?,
                    maximum_backlogs = ?
                WHERE job_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            fillJobStatement(ps, company.id);
            ps.setInt(7, selectedJobId);

            int rows = ps.executeUpdate();
            showMessage(rows > 0 ? "Job updated successfully!" : "Job was not found.",
                    rows > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

            clearJobForm();
            loadJobs();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void deleteJob() {
        if (selectedJobId == null) {
            showMessage("Select a job from the table first.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this job opening?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM jobs WHERE job_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, selectedJobId);
            int rows = ps.executeUpdate();

            showMessage(rows > 0 ? "Job deleted successfully!" : "Job was not found.",
                    rows > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

            clearJobForm();
            loadJobs();
        } catch (SQLException ex) {
            if ("23000".equals(ex.getSQLState())) {
                showMessage("Cannot delete this job because student applications are linked to it.\nHandle those applications first.",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                showDatabaseError(ex);
            }
        }
    }

    private boolean validateJobForm() {
        if (jobTitleField.getText().trim().isEmpty()
                || minimumCgpaField.getText().trim().isEmpty()
                || departmentField.getText().trim().isEmpty()) {
            showMessage("Enter the job title, minimum CGPA and department.",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            double cgpa = Double.parseDouble(minimumCgpaField.getText().trim());
            int backlogs = Integer.parseInt(maximumBacklogsField.getText().trim());

            if (cgpa < 0 || cgpa > 10) {
                showMessage("Minimum CGPA must be between 0 and 10.", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (backlogs < 0) {
                showMessage("Maximum backlogs cannot be negative.", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            showMessage("Enter valid numbers for CGPA and backlogs.", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void fillJobStatement(PreparedStatement ps, int companyId) throws SQLException {
        ps.setInt(1, companyId);
        ps.setString(2, jobTitleField.getText().trim());
        ps.setDouble(3, Double.parseDouble(minimumCgpaField.getText().trim()));
        ps.setString(4, departmentField.getText().trim());

        String skills = requiredSkillsField.getText().trim();
        ps.setString(5, skills.isEmpty() ? null : skills);
        ps.setInt(6, Integer.parseInt(maximumBacklogsField.getText().trim()));
    }

    private void loadCompanyChoices() {
        if (companyCombo == null) return;
        companyCombo.removeAllItems();

        String sql = "SELECT company_id, company_name FROM companies ORDER BY company_name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                companyCombo.addItem(new CompanyItem(
                        rs.getInt("company_id"), rs.getString("company_name")));
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void selectCompany(int companyId) {
        for (int i = 0; i < companyCombo.getItemCount(); i++) {
            CompanyItem item = companyCombo.getItemAt(i);
            if (item.id == companyId) {
                companyCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadJobs() {
        if (jobModel == null) return;
        jobModel.setRowCount(0);

        String sql = """
                SELECT j.job_id, j.company_id, c.company_name,
                       j.job_title, j.minimum_cgpa,
                       j.eligible_department, j.required_skills,
                       j.maximum_backlogs
                FROM jobs j
                JOIN companies c ON j.company_id = c.company_id
                ORDER BY j.job_id DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                jobModel.addRow(new Object[]{
                        rs.getInt("job_id"),
                        rs.getInt("company_id"),
                        rs.getString("company_name"),
                        rs.getString("job_title"),
                        rs.getBigDecimal("minimum_cgpa"),
                        rs.getString("eligible_department"),
                        rs.getString("required_skills"),
                        rs.getInt("maximum_backlogs")
                });
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void clearJobForm() {
        companyCombo.setSelectedIndex(-1);
        jobTitleField.setText("");
        minimumCgpaField.setText("");
        departmentField.setText("");
        requiredSkillsField.setText("");
        maximumBacklogsField.setText("0");
        selectedJobId = null;
        jobTable.clearSelection();
    }

    // ================= REUSABLE UI HELPERS =================

    private JPanel basePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(Theme.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 28, 24, 28));
        return panel;
    }

    private JPanel formTitle(String title, String help) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        JLabel heading = Theme.sectionTitle(title);
        JLabel hint = Theme.hint(help);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(heading);
        block.add(Box.createVerticalStrut(3));
        block.add(hint);
        return block;
    }

    private JPanel createButtonBar() {
        JPanel panel = new JPanel(new Theme.HRow(10));
        panel.setOpaque(false);
        return panel;
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        Theme.styleTable(table);
        return table;
    }

    private JPanel createTablePanel(String title, JTable table) {
        Theme.Card panel = Theme.card(new BorderLayout(0, 14), 20);
        panel.add(Theme.sectionTitle(title), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        Theme.styleScroll(scroll);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private String valueAt(DefaultTableModel model, int row, int column) {
        Object value = model.getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    // ================= COMMON METHODS =================

    private void showMessage(String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, "NEXUS Placement System", messageType);
    }

    private void showDatabaseError(SQLException ex) {
        ex.printStackTrace();
        showMessage("Database operation failed:\n" + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
    }

    private static class CompanyItem {
        private final int id;
        private final String name;

        CompanyItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            new CompanyJobManagement().setVisible(true);
        });
    }
}
