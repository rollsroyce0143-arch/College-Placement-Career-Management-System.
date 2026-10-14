import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Locale;

public class PlacementAnalytics extends JFrame {

    private JLabel studentsLabel;
    private JLabel companiesLabel;
    private JLabel jobsLabel;
    private JLabel applicationsLabel;
    private JLabel selectedLabel;
    private JLabel placedLabel;
    private JLabel statusMessageLabel;

    private JTable departmentTable;
    private DefaultTableModel departmentModel;

    private JTable statusTable;
    private DefaultTableModel statusModel;

    public PlacementAnalytics() {

        Theme.install();

        setTitle("NEXUS | Placement Analytics");
        setSize(1280, 820);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Theme.applyWindow(this);

        createInterface();
        loadAnalytics();
    }

    // =============== MAIN INTERFACE ===============

    private void createInterface() {

        setLayout(new BorderLayout());

        add(Theme.pageHeader(
                "Placement Analytics",
                "A live summary of students, companies, applications and "
                        + "department-wise placement results.",
                Theme.Glyph.CHART
        ), BorderLayout.NORTH);

        // Dashboard cards
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 16, 16));
        cardsPanel.setOpaque(false);

        studentsLabel = createCard(
                cardsPanel, "TOTAL STUDENTS",
                "Registered students", Theme.BLUE, Theme.Glyph.STUDENT
        );

        companiesLabel = createCard(
                cardsPanel, "TOTAL COMPANIES",
                "Recruiting companies", Theme.TEAL, Theme.Glyph.BUILDING
        );

        jobsLabel = createCard(
                cardsPanel, "JOB OPENINGS",
                "Available positions", Theme.VIOLET, Theme.Glyph.BRIEFCASE
        );

        applicationsLabel = createCard(
                cardsPanel, "APPLICATIONS",
                "Total applications received", Theme.WARNING, Theme.Glyph.CLIPBOARD
        );

        selectedLabel = createCard(
                cardsPanel, "SELECTED APPLICATIONS",
                "Applications marked as selected", Theme.SUCCESS, Theme.Glyph.CHECK
        );

        placedLabel = createCard(
                cardsPanel, "UNIQUE STUDENTS SELECTED",
                "Different students who got selected", Theme.PRIMARY, Theme.Glyph.CAP
        );

        // Department report
        JPanel departmentPanel = createReportPanel(
                "Department-wise Statistics"
        );

        String[] departmentColumns = {
                "Department",
                "Total Students",
                "Selected Students",
                "Placement Percentage"
        };

        departmentModel = new DefaultTableModel(
                departmentColumns, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        departmentTable = createStyledTable(departmentModel);

        int[] departmentWidths = {130, 145, 160, 190};

        for (int i = 0; i < departmentWidths.length; i++) {
            departmentTable.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(departmentWidths[i]);
        }

        JScrollPane departmentScroll = createScrollPane(departmentTable);
        departmentPanel.add(departmentScroll, BorderLayout.CENTER);

        // Application status report
        JPanel statusPanel = createReportPanel(
                "Application Status Summary"
        );

        String[] statusColumns = {
                "Application Status",
                "Total Applications"
        };

        statusModel = new DefaultTableModel(statusColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        statusTable = createStyledTable(statusModel);

        statusTable.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(220);

        statusTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(180);

        statusTable.getColumnModel().getColumn(0)
                .setCellRenderer(Theme.badgeRendererLeft(text -> {
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

        JScrollPane statusScroll = createScrollPane(statusTable);
        statusPanel.add(statusScroll, BorderLayout.CENTER);

        // Report layout
        JPanel reportsPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        reportsPanel.setOpaque(false);
        reportsPanel.add(departmentPanel);
        reportsPanel.add(statusPanel);

        // Action buttons
        JButton refreshButton = Theme.button(
                "Refresh Analytics", Theme.Glyph.REFRESH, "primary"
        );

        JButton exportDepartmentButton = Theme.button(
                "Export Department CSV", Theme.Glyph.EXPORT, "secondary"
        );

        JButton exportStatusButton = Theme.button(
                "Export Status CSV", Theme.Glyph.EXPORT, "secondary"
        );

        JPanel buttonsPanel = new JPanel(
                new Theme.HRow(10)
        );
        buttonsPanel.setOpaque(false);

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(exportDepartmentButton);
        buttonsPanel.add(exportStatusButton);

        // Status message
        statusMessageLabel = new JLabel(" ");
        statusMessageLabel.setFont(Theme.font(Font.PLAIN, 12));
        statusMessageLabel.setForeground(Theme.SUCCESS);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.add(buttonsPanel, BorderLayout.CENTER);
        bottomPanel.add(statusMessageLabel, BorderLayout.SOUTH);

        // Main content
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setBackground(Theme.BG);
        centerPanel.setBorder(
                BorderFactory.createEmptyBorder(22, 28, 22, 28)
        );

        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(reportsPanel, BorderLayout.CENTER);
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Button actions
        refreshButton.addActionListener(e -> loadAnalytics());

        exportDepartmentButton.addActionListener(
                e -> exportTable(
                        departmentTable,
                        "department_analytics.csv"
                )
        );

        exportStatusButton.addActionListener(
                e -> exportTable(
                        statusTable,
                        "application_status.csv"
                )
        );
    }

    // =============== ANALYTICS CARDS ===============

    private JLabel createCard(
            JPanel parent,
            String title,
            String description,
            Color accent,
            Theme.Glyph glyph
    ) {

        JLabel valueLabel = new JLabel("0");
        parent.add(Theme.statCard(title, valueLabel, description, accent, glyph));
        return valueLabel;
    }

    // =============== REPORT PANELS ===============

    private JPanel createReportPanel(String title) {

        Theme.Card panel = Theme.card(new BorderLayout(0, 14), 18);
        panel.add(Theme.sectionTitle(title), BorderLayout.NORTH);
        return panel;
    }

    // =============== TABLE STYLING ===============

    private JTable createStyledTable(DefaultTableModel model) {

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        Theme.styleTable(table);
        return table;
    }

    private JScrollPane createScrollPane(JTable table) {

        JScrollPane scrollPane = new JScrollPane(table);
        Theme.styleScroll(scrollPane);
        return scrollPane;
    }

    // =============== LOAD ANALYTICS ===============

    private void loadAnalytics() {

        try (Connection con = DBConnection.getConnection()) {

            studentsLabel.setText(
                    String.valueOf(
                            getCount(con, "SELECT COUNT(*) FROM students")
                    )
            );

            companiesLabel.setText(
                    String.valueOf(
                            getCount(con, "SELECT COUNT(*) FROM companies")
                    )
            );

            jobsLabel.setText(
                    String.valueOf(
                            getCount(con, "SELECT COUNT(*) FROM jobs")
                    )
            );

            applicationsLabel.setText(
                    String.valueOf(
                            getCount(con, "SELECT COUNT(*) FROM applications")
                    )
            );

            selectedLabel.setText(
                    String.valueOf(
                            getCount(
                                    con,
                                    "SELECT COUNT(*) FROM applications " +
                                    "WHERE application_status = 'Selected'"
                            )
                    )
            );

            placedLabel.setText(
                    String.valueOf(
                            getCount(
                                    con,
                                    "SELECT COUNT(DISTINCT student_id) " +
                                    "FROM applications " +
                                    "WHERE application_status = 'Selected'"
                            )
                    )
            );

            loadDepartmentStatistics(con);
            loadStatusStatistics(con);

            statusMessageLabel.setText(
                    "Analytics refreshed successfully."
            );

        } catch (SQLException ex) {

            ex.printStackTrace();

            statusMessageLabel.setText(
                    "Could not refresh analytics."
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load analytics:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private int getCount(
            Connection con,
            String sql
    ) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        }
    }

    // =============== DEPARTMENT STATISTICS ===============

    private void loadDepartmentStatistics(Connection con)
            throws SQLException {

        departmentModel.setRowCount(0);

        String sql = """
                SELECT
                    s.department,
                    COUNT(DISTINCT s.student_id) AS total_students,
                    COUNT(DISTINCT CASE
                        WHEN a.application_status = 'Selected'
                        THEN s.student_id
                    END) AS selected_students
                FROM students s
                LEFT JOIN applications a
                    ON s.student_id = a.student_id
                GROUP BY s.department
                ORDER BY s.department
                """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int total = rs.getInt("total_students");
                int selected = rs.getInt("selected_students");

                double percentage = total == 0
                        ? 0.0
                        : selected * 100.0 / total;

                departmentModel.addRow(new Object[]{
                        rs.getString("department"),
                        total,
                        selected,
                        String.format(Locale.US, "%.2f%%", percentage)
                });
            }
        }
    }

    // =============== APPLICATION STATUS STATISTICS ===============

    private void loadStatusStatistics(Connection con)
            throws SQLException {

        statusModel.setRowCount(0);

        String sql = """
                SELECT application_status, COUNT(*) AS total
                FROM applications
                GROUP BY application_status
                ORDER BY application_status
                """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                statusModel.addRow(new Object[]{
                        rs.getString("application_status"),
                        rs.getInt("total")
                });
            }
        }
    }

    // =============== CSV EXPORT ===============

    private void exportTable(
            JTable table,
            String defaultFileName
    ) {

        if (table.getRowCount() == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "There is no data to export.",
                    "NEXUS Placement System",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Analytics Report");
        chooser.setSelectedFile(new File(defaultFileName));

        if (chooser.showSaveDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        if (!file.getName()
                .toLowerCase(Locale.ROOT)
                .endsWith(".csv")) {

            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (PrintWriter writer = new PrintWriter(file)) {

            // Export the visible table order, including sorting.
            for (int col = 0; col < table.getColumnCount(); col++) {

                if (col > 0) {
                    writer.print(",");
                }

                writer.print(
                        csvEscape(
                                table.getColumnName(col)
                        )
                );
            }

            writer.println();

            for (int viewRow = 0;
                 viewRow < table.getRowCount();
                 viewRow++) {

                for (int col = 0;
                     col < table.getColumnCount();
                     col++) {

                    if (col > 0) {
                        writer.print(",");
                    }

                    Object value = table.getValueAt(viewRow, col);

                    writer.print(
                            csvEscape(
                                    value == null ? "" : value.toString()
                            )
                    );
                }

                writer.println();
            }

            statusMessageLabel.setText(
                    "Report exported successfully."
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Report exported successfully:\n"
                            + file.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
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

        // Reduce spreadsheet formula injection risk.
        if (!value.isEmpty()
                && "=+-@".indexOf(value.charAt(0)) >= 0) {

            value = "'" + value;
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // =============== MAIN METHOD ===============

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new PlacementAnalytics().setVisible(true);
        });
    }
}
