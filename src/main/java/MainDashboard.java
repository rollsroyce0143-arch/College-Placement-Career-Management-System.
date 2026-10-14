
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainDashboard extends JFrame {

    // ==================== NEXUS THEME COLOURS ====================

    private final Color bg = Theme.BG;
    private final Color panel = Theme.SURFACE;
    private final Color panelHover = Theme.SURFACE_ALT;

    private final Color cyan = Theme.PRIMARY;
    private final Color blue = Theme.BLUE;
    private final Color violet = Theme.VIOLET;
    private final Color orange = new Color(0xF59E0B);
    private final Color green = new Color(0x22C55E);
    private final Color red = new Color(0xEF4444);

    private final Color textColor = Theme.TEXT;
    private final Color muted = Theme.MUTED;
    private final Color borderColor = Theme.BORDER;

    // ==================== DASHBOARD COMPONENTS ====================

    private JPanel content;
    private JPanel departmentChart;
    private JPanel statusChart;
    private JPanel activityPanel;

    private JLabel studentValue;
    private JLabel companyValue;
    private JLabel jobValue;
    private JLabel applicationValue;
    private JLabel selectedValue;

    private JLabel dbStatus;
    private JLabel refreshStatus;

    private boolean refreshing = false;

    // ==================== CONSTRUCTOR ====================

    public MainDashboard() {

        Theme.install();

        setTitle("NEXUS | College Placement & Career Management");
        setSize(1450, 860);
        setMinimumSize(new Dimension(1280, 740));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        Theme.applyWindow(this);

        createSidebar();

        content = new JPanel(new BorderLayout());
        content.setBackground(bg);
        content.setBorder(new EmptyBorder(24, 30, 20, 30));

        add(content, BorderLayout.CENTER);

        showOverview();
        refreshData();
    }

    // Page panel that always matches the width of the scroll viewport.
    private static class PagePanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(
                Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(
                Rectangle visibleRect, int orientation, int direction) {
            return 120;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // ==================== SIDEBAR ====================

    private void createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(268, 0));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setLayout(new BorderLayout());

        // Brand
        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setBackground(Theme.SIDEBAR);
        brand.setBorder(new EmptyBorder(26, 22, 24, 16));

        JLabel logoIcon = new JLabel(new ImageIcon(Theme.appIcon(44)));

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("NEXUS");
        logo.setFont(Theme.font(Font.BOLD, 22));
        logo.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Placement Management");
        subtitle.setFont(Theme.font(Font.PLAIN, 12));
        subtitle.setForeground(Theme.SIDEBAR_TEXT);

        brandText.add(logo);
        brandText.add(subtitle);

        brand.add(logoIcon, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);

        sidebar.add(brand, BorderLayout.NORTH);

        // Menu
        JPanel menu = new JPanel();
        menu.setBackground(Theme.SIDEBAR);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(6, 14, 15, 14));

        JLabel menuTitle = new JLabel("WORKSPACE");
        menuTitle.setFont(Theme.font(Font.BOLD, 11));
        menuTitle.setForeground(new Color(0x64748B));
        menuTitle.setBorder(new EmptyBorder(0, 8, 0, 0));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        menu.add(menuTitle);
        menu.add(Box.createVerticalStrut(12));

        addMenu(menu, Theme.Glyph.HOME, "Overview", null, true);
        addMenu(menu, Theme.Glyph.STUDENT, "Student Management", "Main", false);
        addMenu(menu, Theme.Glyph.BUILDING, "Companies & Jobs",
                "CompanyJobManagement", false);
        addMenu(menu, Theme.Glyph.ELIGIBLE, "Company-wise Eligibility",
                "CompanyWiseEligibility", false);
        addMenu(menu, Theme.Glyph.MATCH, "Job Matching", "JobMatching", false);
        addMenu(menu, Theme.Glyph.CLIPBOARD, "Application Tracking",
                "ApplicationTracking", false);
        addMenu(menu, Theme.Glyph.CHART, "Placement Analytics",
                "PlacementAnalytics", false);

        sidebar.add(menu, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel();
        footer.setBackground(Theme.SIDEBAR);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(0x1E293B)),
                new EmptyBorder(16, 22, 20, 20)
        ));

        JLabel systemLabel = new JLabel("SYSTEM STATUS");
        systemLabel.setFont(Theme.font(Font.BOLD, 11));
        systemLabel.setForeground(new Color(0x64748B));

        dbStatus = new JLabel("\u25CF Connecting to database");
        dbStatus.setFont(Theme.font(Font.PLAIN, 12));
        dbStatus.setForeground(orange);

        JLabel version = new JLabel("NEXUS \u2022 Version 1.0");
        version.setFont(Theme.font(Font.PLAIN, 11));
        version.setForeground(new Color(0x64748B));

        systemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        version.setAlignmentX(Component.LEFT_ALIGNMENT);

        footer.add(systemLabel);
        footer.add(Box.createVerticalStrut(8));
        footer.add(dbStatus);
        footer.add(Box.createVerticalStrut(10));
        footer.add(version);

        sidebar.add(footer, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
    }

    private void addMenu(
            JPanel menu,
            Theme.Glyph icon,
            String text,
            String className,
            boolean active) {

        JButton button = Theme.button(
                text, null, active ? "navActive" : "nav"
        );

        button.setIcon(active
                ? Theme.icon(icon, 19, Color.WHITE)
                : Theme.icon(icon, 19, Theme.SIDEBAR_TEXT, Color.WHITE));
        button.setIconTextGap(14);
        button.setFont(Theme.font(Font.BOLD, 13));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(0, 16, 0, 8));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setPreferredSize(new Dimension(235, 46));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.addActionListener(e -> {
            if (className == null) {
                showOverview();
                refreshData();
            } else {
                openModule(className);
            }
        });

        menu.add(button);
        menu.add(Box.createVerticalStrut(4));
    }

    // ==================== OVERVIEW SCREEN ====================

    private void showOverview() {

        content.removeAll();
        content.setLayout(new BorderLayout(0, 20));

        JPanel page = new PagePanel();
        page.setBackground(bg);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Placement Overview");
        title.setFont(Theme.font(Font.BOLD, 28));
        title.setForeground(textColor);

        JLabel description = new JLabel(
            "A quick look at students, companies, applications and placement progress."
        );
        description.setFont(Theme.font(Font.PLAIN, 14));
        description.setForeground(muted);

        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(description);

        JButton refreshButton = Theme.button(
                "Refresh Data", Theme.Glyph.REFRESH, "secondary"
        );
        refreshButton.addActionListener(e -> refreshData());

        JPanel refreshWrap = new JPanel(new GridBagLayout());
        refreshWrap.setOpaque(false);
        refreshWrap.add(refreshButton);

        header.add(heading, BorderLayout.WEST);
        header.add(refreshWrap, BorderLayout.EAST);

        page.add(header);
        page.add(Box.createVerticalStrut(22));

        // Statistics
        JPanel stats = new JPanel(new GridLayout(1, 5, 16, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);

        studentValue = new JLabel("0");
        companyValue = new JLabel("0");
        jobValue = new JLabel("0");
        applicationValue = new JLabel("0");
        selectedValue = new JLabel("0");

        stats.add(statCard("TOTAL STUDENTS", studentValue, cyan,
                "Registered", Theme.Glyph.STUDENT));
        stats.add(statCard("COMPANIES", companyValue, blue,
                "Partners", Theme.Glyph.BUILDING));
        stats.add(statCard("JOB OPENINGS", jobValue, violet,
                "Open positions", Theme.Glyph.BRIEFCASE));
        stats.add(statCard("APPLICATIONS", applicationValue, orange,
                "Submitted", Theme.Glyph.CLIPBOARD));
        stats.add(statCard("SELECTED", selectedValue, green,
                "Selected / placed", Theme.Glyph.CHECK));

        page.add(stats);
        page.add(Box.createVerticalStrut(26));

        // Charts
        page.add(sectionHeading("Placement Insights"));
        page.add(Box.createVerticalStrut(12));

        JPanel charts = new JPanel(new GridLayout(1, 2, 18, 0));
        charts.setOpaque(false);
        charts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        charts.setAlignmentX(Component.LEFT_ALIGNMENT);

        departmentChart = createChartPanel("Department-wise Overview");
        statusChart = createChartPanel("Application Status");

        charts.add(departmentChart);
        charts.add(statusChart);

        page.add(charts);
        page.add(Box.createVerticalStrut(26));

        // Quick actions
        page.add(sectionHeading("Quick Actions"));
        page.add(Box.createVerticalStrut(12));

        JPanel actions = new JPanel(new GridLayout(2, 2, 16, 16));
        actions.setOpaque(false);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        actions.add(actionCard(
            Theme.Glyph.STUDENT, "Manage Students",
            "Register students and update academic details",
            "Main", cyan
        ));

        actions.add(actionCard(
            Theme.Glyph.BUILDING, "Manage Companies & Jobs",
            "Add companies and create job openings",
            "CompanyJobManagement", blue
        ));

        actions.add(actionCard(
            Theme.Glyph.ELIGIBLE, "Check Eligibility",
            "Find students eligible for each company",
            "CompanyWiseEligibility", violet
        ));

        actions.add(actionCard(
            Theme.Glyph.CLIPBOARD, "Track Applications",
            "View and update application progress",
            "ApplicationTracking", orange
        ));

        page.add(actions);
        page.add(Box.createVerticalStrut(14));

        refreshStatus = new JLabel("Ready");
        refreshStatus.setFont(Theme.font(Font.PLAIN, 12));
        refreshStatus.setForeground(muted);
        refreshStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        page.add(refreshStatus);

        JScrollPane scrollPane = new JScrollPane(page);
        Theme.styleScrollPage(scrollPane);

        content.add(scrollPane, BorderLayout.CENTER);

        content.revalidate();
        content.repaint();
    }

    private JLabel sectionHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(Font.BOLD, 17));
        label.setForeground(textColor);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // ==================== STATISTICS CARDS ====================

    private JPanel statCard(
            String title,
            JLabel value,
            Color accent,
            String caption,
            Theme.Glyph glyph) {

        return Theme.statCard(title, value, caption, accent, glyph);
    }

    // ==================== QUICK ACTION CARDS ====================

    private JPanel actionCard(
            Theme.Glyph icon,
            String title,
            String description,
            String className,
            Color accent) {

        final boolean[] hover = {false};

        Theme.Card card = new Theme.Card(new BorderLayout(16, 0), 18) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (hover[0]) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.alpha(accent, 150));
                    g2.setStroke(new BasicStroke(1.6f));
                    g2.draw(new RoundRectangle2D.Double(
                            0.8, 0.8, getWidth() - 1.6, getHeight() - 3.6, 16, 16
                    ));
                    g2.dispose();
                }
            }
        };

        JPanel chipWrap = new JPanel(new GridBagLayout());
        chipWrap.setOpaque(false);
        chipWrap.add(Theme.chip(icon, accent, 52));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.font(Font.BOLD, 15));
        titleLabel.setForeground(textColor);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(Theme.font(Font.PLAIN, 12));
        descriptionLabel.setForeground(muted);

        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textCenter = new JPanel(new GridBagLayout());
        textCenter.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(descriptionLabel);
        textCenter.add(textPanel, new GridBagConstraints(
                0, 0, 1, 1, 1, 1, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0
        ));

        JLabel arrow = new JLabel(Theme.icon(Theme.Glyph.ARROW, 20, accent));

        JPanel arrowWrap = new JPanel(new GridBagLayout());
        arrowWrap.setOpaque(false);
        arrowWrap.add(arrow);

        card.add(chipWrap, BorderLayout.WEST);
        card.add(textCenter, BorderLayout.CENTER);
        card.add(arrowWrap, BorderLayout.EAST);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openModule(className);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hover[0] = true;
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover[0] = false;
                card.repaint();
            }
        });

        return card;
    }

    // ==================== CHART PANELS ====================

    private JPanel createChartPanel(String title) {

        Theme.Card chart = Theme.card(new BorderLayout(0, 14), 18);

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.font(Font.BOLD, 15));
        heading.setForeground(textColor);

        JPanel rows = new JPanel();
        rows.setBackground(panel);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));

        chart.add(heading, BorderLayout.NORTH);
        chart.add(rows, BorderLayout.CENTER);

        return chart;
    }

    private JPanel getChartRows(JPanel chart) {
        Component[] components = chart.getComponents();

        for (Component component : components) {
            if (component instanceof JPanel) {
                return (JPanel) component;
            }
        }

        JPanel rows = new JPanel();
        rows.setBackground(panel);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        chart.add(rows, BorderLayout.CENTER);

        return rows;
    }

    private void addBarRow(
            JPanel rows,
            String name,
            int value,
            int maxValue,
            Color accent) {

        JPanel row = new JPanel(new BorderLayout(0, 6));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(muted);
        nameLabel.setFont(Theme.font(Font.PLAIN, 12));

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setForeground(textColor);
        valueLabel.setFont(Theme.font(Font.BOLD, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(nameLabel, BorderLayout.WEST);
        top.add(valueLabel, BorderLayout.EAST);

        final double fraction = Math.max(0, Math.min(value, maxValue))
                / (double) Math.max(1, maxValue);

        JComponent bar = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xEEF2F7));
                g2.fill(new RoundRectangle2D.Double(
                        0, 0, getWidth(), getHeight(), getHeight(), getHeight()
                ));
                if (fraction > 0) {
                    double w = Math.max(getHeight(), getWidth() * fraction);
                    g2.setColor(accent);
                    g2.fill(new RoundRectangle2D.Double(
                            0, 0, w, getHeight(), getHeight(), getHeight()
                    ));
                }
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(100, 8));

        row.add(top, BorderLayout.NORTH);
        row.add(bar, BorderLayout.CENTER);

        rows.add(row);
        rows.add(Box.createVerticalStrut(10));
    }

    // ==================== LOAD DEPARTMENT CHART ====================

    private void loadDepartmentChart() {

        if (departmentChart == null) {
            return;
        }

        departmentChart.removeAll();
        departmentChart.setLayout(new BorderLayout(0, 12));

        JLabel heading = new JLabel("Department-wise Overview");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(textColor);

        JPanel rows = new JPanel();
        rows.setBackground(panel);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));

        departmentChart.add(heading, BorderLayout.NORTH);
        departmentChart.add(rows, BorderLayout.CENTER);

        String sql =
            "SELECT s.department, " +
            "COUNT(DISTINCT s.student_id) AS total_students, " +
            "COUNT(DISTINCT CASE WHEN " +
            "LOWER(a.application_status) IN ('selected', 'placed') " +
            "THEN s.student_id END) AS selected_students " +
            "FROM students s " +
            "LEFT JOIN applications a ON s.student_id = a.student_id " +
            "GROUP BY s.department ORDER BY s.department";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Map<String, Integer> totals = new LinkedHashMap<>();
            Map<String, Integer> selected = new LinkedHashMap<>();

            int max = 1;

            while (rs.next()) {
                String department = rs.getString("department");

                if (department == null || department.trim().isEmpty()) {
                    department = "Not specified";
                }

                int total = rs.getInt("total_students");
                int placed = rs.getInt("selected_students");

                totals.put(department, total);
                selected.put(department, placed);

                if (total > max) {
                    max = total;
                }
            }

            if (totals.isEmpty()) {
                addEmptyMessage(rows, "No student data available yet.");
            } else {
                for (String department : totals.keySet()) {
                    int total = totals.get(department);
                    int placed = selected.get(department);

                    addBarRow(
                        rows,
                        department + "  (" + placed + " selected)",
                        total,
                        max,
                        cyan
                    );
                }
            }

        } catch (SQLException ex) {
            addEmptyMessage(rows, "Could not load department data.");
            ex.printStackTrace();
        }

        departmentChart.revalidate();
        departmentChart.repaint();
    }

    // ==================== LOAD APPLICATION STATUS CHART ====================

    private void loadStatusChart() {

        if (statusChart == null) {
            return;
        }

        statusChart.removeAll();
        statusChart.setLayout(new BorderLayout(0, 12));

        JLabel heading = new JLabel("Application Status");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(textColor);

        JPanel rows = new JPanel();
        rows.setBackground(panel);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));

        statusChart.add(heading, BorderLayout.NORTH);
        statusChart.add(rows, BorderLayout.CENTER);

        String sql =
            "SELECT application_status, COUNT(*) AS total " +
            "FROM applications GROUP BY application_status " +
            "ORDER BY application_status";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Map<String, Integer> values = new LinkedHashMap<>();
            int max = 1;

            while (rs.next()) {
                String status = rs.getString("application_status");

                if (status == null || status.trim().isEmpty()) {
                    status = "Unknown";
                }

                int total = rs.getInt("total");
                values.put(status, total);

                if (total > max) {
                    max = total;
                }
            }

            if (values.isEmpty()) {
                addEmptyMessage(rows, "No applications recorded yet.");
            } else {
                for (Map.Entry<String, Integer> entry : values.entrySet()) {

                    String status = entry.getKey();
                    Color accent = getStatusColor(status);

                    addBarRow(
                        rows,
                        status,
                        entry.getValue(),
                        max,
                        accent
                    );
                }
            }

        } catch (SQLException ex) {
            addEmptyMessage(rows, "Could not load application statuses.");
            ex.printStackTrace();
        }

        statusChart.revalidate();
        statusChart.repaint();
    }

    private Color getStatusColor(String status) {

        String value = status.toLowerCase();

        if (value.contains("select") || value.contains("placed")) {
            return green;
        }

        if (value.contains("reject")) {
            return red;
        }

        if (value.contains("pending") || value.contains("applied")) {
            return orange;
        }

        if (value.contains("interview")) {
            return violet;
        }

        return blue;
    }

    private void addEmptyMessage(JPanel panel, String message) {

        JLabel label = new JLabel(message);
        label.setForeground(muted);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        panel.add(label);
    }

    // ==================== REFRESH DASHBOARD DATA ====================

    private void refreshData() {

        if (refreshing) {
            return;
        }

        refreshing = true;

        try {
            if (refreshStatus != null) {
                refreshStatus.setText("Refreshing dashboard data...");
            }

            int students = count("SELECT COUNT(*) FROM students");
            int companies = count("SELECT COUNT(*) FROM companies");
            int jobs = count("SELECT COUNT(*) FROM jobs");
            int applications = count("SELECT COUNT(*) FROM applications");

            int selected = count(
                "SELECT COUNT(*) FROM applications " +
                "WHERE LOWER(application_status) IN ('selected', 'placed')"
            );

            if (studentValue != null) {
                studentValue.setText(String.valueOf(students));
                companyValue.setText(String.valueOf(companies));
                jobValue.setText(String.valueOf(jobs));
                applicationValue.setText(String.valueOf(applications));
                selectedValue.setText(String.valueOf(selected));
            }

            if (dbStatus != null) {
                dbStatus.setText("● Database connected");
                dbStatus.setForeground(green);
            }

            loadDepartmentChart();
            loadStatusChart();

            if (refreshStatus != null) {
                refreshStatus.setText("Dashboard updated successfully.");
            }

        } catch (Exception ex) {

            if (dbStatus != null) {
                dbStatus.setText("● Database connection error");
                dbStatus.setForeground(red);
            }

            if (refreshStatus != null) {
                refreshStatus.setText("Refresh failed. Check the database connection.");
            }

            ex.printStackTrace();

        } finally {
            refreshing = false;
        }
    }

    private int count(String sql) throws SQLException {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    // ==================== OPEN MODULES ====================

    private void openModule(String className) {

        try {
            // Main is the student management class.
            // It creates its own JFrame and does not extend JFrame.
            if ("Main".equals(className)) {
                Main.openWindow();
                return;
            }

            // Other management modules extend JFrame.
            Class<?> moduleClass = Class.forName(className);

            Object object = moduleClass.getDeclaredConstructor().newInstance();

            if (object instanceof JFrame) {
                JFrame frame = (JFrame) object;
                frame.setLocationRelativeTo(this);
                frame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    className + " is not a JFrame window.",
                    "Module Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                this,
                "Could not open " + className + ".\n\n" +
                "Check that the class exists and has a public no-argument constructor.\n\n" +
                "Error: " + ex.getMessage(),
                "Unable to Open Module",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ==================== MAIN METHOD ====================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            Theme.install();

            MainDashboard dashboard = new MainDashboard();
            dashboard.setVisible(true);
        });
    }
}