import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * NEXUS visual theme.
 *
 * One place that controls how the whole application looks: colours, fonts,
 * vector icons, the window icon and every reusable styled component
 * (buttons, text fields, drop-downs, tables, scroll bars, tabs, cards).
 *
 * It contains NO business logic. Change a colour here and every window
 * in the application follows.
 */
public final class Theme {

    private Theme() {
    }

    // =====================================================================
    //  PALETTE
    // =====================================================================

    public static final Color BG = new Color(0xF3F5FA);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SURFACE_ALT = new Color(0xF8FAFC);
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color BORDER_STRONG = new Color(0xCBD5E1);

    public static final Color TEXT = new Color(0x1E293B);
    public static final Color MUTED = new Color(0x64748B);

    public static final Color PRIMARY = new Color(0x4F46E5);
    public static final Color PRIMARY_DARK = new Color(0x4338CA);
    public static final Color PRIMARY_SOFT = new Color(0xEEF2FF);
    public static final Color VIOLET = new Color(0x7C3AED);
    public static final Color BLUE = new Color(0x2563EB);
    public static final Color TEAL = new Color(0x0D9488);

    public static final Color SUCCESS = new Color(0x16A34A);
    public static final Color WARNING = new Color(0xD97706);
    public static final Color DANGER = new Color(0xDC2626);

    public static final Color SELECTION = new Color(0xE0E7FF);
    public static final Color TABLE_HEADER = new Color(0xF1F5F9);

    public static final Color SIDEBAR = new Color(0x0F172A);
    public static final Color SIDEBAR_HOVER = new Color(0x1E293B);
    public static final Color SIDEBAR_TEXT = new Color(0x94A3B8);

    // =====================================================================
    //  FONTS
    // =====================================================================

    private static final String FAMILY = "Segoe UI";

    public static Font font(int style, int size) {
        return new Font(FAMILY, style, size);
    }

    public static Color alpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    private static Color mix(Color a, Color b, double t) {
        int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bl);
    }

    private static void quality(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
    }

    // =====================================================================
    //  INSTALL (one look and feel for every window)
    // =====================================================================

    private static boolean installed = false;

    public static synchronized void install() {

        if (installed) {
            return;
        }

        installed = true;

        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName()
            );
        } catch (Exception ignored) {
            // Continue with whatever look and feel is available.
        }

        // Buttons everywhere (including dialog buttons) use the modern UI.
        UIManager.put("ButtonUI", "Theme$ButtonUI");
        UIManager.put("Theme$ButtonUI", ButtonUI.class);
        UIManager.put("Button.border",
                new BorderUIResource(new EmptyBorder(9, 20, 9, 20)));
        UIManager.put("Button.font", font(Font.BOLD, 13));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.background", PRIMARY);

        // Dialogs
        UIManager.put("Panel.background", SURFACE);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", font(Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", font(Font.BOLD, 13));
        UIManager.put("OptionPane.border", new EmptyBorder(16, 18, 14, 18));
        UIManager.put("OptionPane.buttonAreaBorder", new EmptyBorder(8, 0, 0, 0));
        UIManager.put("OptionPane.informationIcon", icon(Glyph.INFO, 36, PRIMARY));
        UIManager.put("OptionPane.warningIcon", icon(Glyph.WARN, 36, WARNING));
        UIManager.put("OptionPane.errorIcon", icon(Glyph.ERROR, 36, DANGER));
        UIManager.put("OptionPane.questionIcon", icon(Glyph.QUESTION, 36, PRIMARY));

        // Text
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", font(Font.PLAIN, 13));
        UIManager.put("TextField.font", font(Font.PLAIN, 14));
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.caretForeground", PRIMARY);
        UIManager.put("TextField.selectionBackground", SELECTION);
        UIManager.put("TextField.selectionForeground", TEXT);
        UIManager.put("ToolTip.background", SIDEBAR);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", new EmptyBorder(6, 10, 6, 10));
        UIManager.put("ToolTip.font", font(Font.PLAIN, 12));
    }

    // =====================================================================
    //  WINDOW HELPERS
    // =====================================================================

    /** Applies the NEXUS icon and background colour to a window. */
    public static void applyWindow(JFrame frame) {

        List<Image> icons = new ArrayList<>();
        for (int size : new int[]{16, 24, 32, 48, 64, 128, 256}) {
            icons.add(appIcon(size));
        }

        frame.setIconImages(icons);
        frame.getContentPane().setBackground(BG);
    }

    /** The NEXUS application icon (graduation cap on an indigo tile). */
    public static Image appIcon(int size) {

        BufferedImage image = new BufferedImage(
                size, size, BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2 = image.createGraphics();
        quality(g2);

        g2.setPaint(new GradientPaint(0, 0, PRIMARY, size, size, VIOLET));
        double arc = size * 0.28;
        g2.fill(new RoundRectangle2D.Double(0, 0, size, size, arc, arc));

        double inner = size * 0.64;
        double offset = (size - inner) / 2.0;
        g2.translate(offset, offset);
        g2.scale(inner / 24.0, inner / 24.0);
        g2.setStroke(new BasicStroke(
                size < 32 ? 2.6f : 2.0f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND
        ));
        g2.setColor(Color.WHITE);
        paintGlyph(g2, Glyph.CAP);

        g2.dispose();
        return image;
    }

    // =====================================================================
    //  VECTOR ICONS
    // =====================================================================

    public enum Glyph {
        HOME, STUDENT, BUILDING, ELIGIBLE, MATCH, CLIPBOARD, CHART,
        REFRESH, PLUS, EDIT, TRASH, SEARCH, EXPORT, CLEAR, CHECK, CAP,
        BRIEFCASE, ARROW, INFO, WARN, ERROR, QUESTION
    }

    public static Icon icon(Glyph glyph, int size, Color color) {
        return new GlyphIcon(glyph, size, color, color);
    }

    public static Icon icon(Glyph glyph, int size, Color color, Color hover) {
        return new GlyphIcon(glyph, size, color, hover);
    }

    private static final class GlyphIcon implements Icon {

        private final Glyph glyph;
        private final int size;
        private final Color color;
        private final Color hover;

        GlyphIcon(Glyph glyph, int size, Color color, Color hover) {
            this.glyph = glyph;
            this.size = size;
            this.color = color;
            this.hover = hover;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {

            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);

            Color paint = color;
            if (c instanceof AbstractButton) {
                ButtonModel m = ((AbstractButton) c).getModel();
                if (m.isRollover() || m.isPressed()) {
                    paint = hover;
                }
            }

            g2.translate(x, y);
            g2.scale(size / 24.0, size / 24.0);
            g2.setStroke(new BasicStroke(
                    2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND
            ));
            g2.setColor(paint);
            paintGlyph(g2, glyph);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    /** Draws a glyph inside a 24 x 24 box using the current colour/stroke. */
    private static void paintGlyph(Graphics2D g, Glyph glyph) {

        switch (glyph) {

            case PLUS:
                g.draw(new Line2D.Double(12, 5, 12, 19));
                g.draw(new Line2D.Double(5, 12, 19, 12));
                break;

            case CLEAR:
                g.draw(new Line2D.Double(6, 6, 18, 18));
                g.draw(new Line2D.Double(18, 6, 6, 18));
                break;

            case CHECK:
                g.draw(poly(false, 5, 12.5, 10, 17.5, 19, 7));
                break;

            case ARROW:
                g.draw(new Line2D.Double(5, 12, 19, 12));
                g.draw(poly(false, 13, 6, 19, 12, 13, 18));
                break;

            case SEARCH:
                g.draw(new Ellipse2D.Double(4, 4, 13, 13));
                g.draw(new Line2D.Double(15, 15, 20, 20));
                break;

            case EDIT:
                g.draw(poly(true, 4, 20, 5, 15, 15, 5, 19, 9, 9, 19));
                g.draw(new Line2D.Double(13, 7, 17, 11));
                break;

            case TRASH:
                g.draw(new Line2D.Double(4.5, 7, 19.5, 7));
                g.draw(poly(false, 9, 7, 9, 4.5, 15, 4.5, 15, 7));
                g.draw(poly(false, 6.5, 7, 7.5, 20, 16.5, 20, 17.5, 7));
                g.draw(new Line2D.Double(10.5, 10.5, 10.5, 16.5));
                g.draw(new Line2D.Double(13.5, 10.5, 13.5, 16.5));
                break;

            case EXPORT:
                g.draw(new Line2D.Double(12, 4, 12, 15));
                g.draw(poly(false, 7, 10.5, 12, 15.5, 17, 10.5));
                g.draw(poly(false, 4.5, 15, 4.5, 19.5, 19.5, 19.5, 19.5, 15));
                break;

            case REFRESH:
                g.draw(new Arc2D.Double(4.5, 4.5, 15, 15, 50, 270, Arc2D.OPEN));
                g.draw(poly(false, 15.5, 3.5, 18.8, 7.2, 14.2, 8.4));
                break;

            case HOME:
                g.draw(poly(false, 3, 11.5, 12, 4, 21, 11.5));
                g.draw(poly(false, 5.5, 10, 5.5, 20, 18.5, 20, 18.5, 10));
                g.draw(poly(false, 10, 20, 10, 14, 14, 14, 14, 20));
                break;

            case CAP:
                g.draw(poly(true, 12, 5, 22, 10, 12, 15, 2, 10));
                g.draw(poly(false, 6, 12.5, 6, 16.5));
                g.draw(new QuadCurve2D.Double(6, 16.5, 12, 21.5, 18, 16.5));
                g.draw(new Line2D.Double(18, 16.5, 18, 12.5));
                g.draw(new Line2D.Double(22, 10, 22, 16));
                break;

            case STUDENT:
                g.draw(new Ellipse2D.Double(8, 3.5, 8, 8));
                g.draw(new Arc2D.Double(4, 14, 16, 14, 0, 180, Arc2D.OPEN));
                break;

            case BUILDING:
                g.draw(new RoundRectangle2D.Double(5, 3.5, 10, 17, 2, 2));
                g.draw(poly(false, 15, 9.5, 19, 9.5, 19, 20.5));
                g.draw(new Line2D.Double(3.5, 20.5, 20.5, 20.5));
                g.fill(new Rectangle2D.Double(8, 7, 1.8, 1.8));
                g.fill(new Rectangle2D.Double(11, 7, 1.8, 1.8));
                g.fill(new Rectangle2D.Double(8, 11, 1.8, 1.8));
                g.fill(new Rectangle2D.Double(11, 11, 1.8, 1.8));
                g.fill(new Rectangle2D.Double(8, 15, 1.8, 1.8));
                g.fill(new Rectangle2D.Double(11, 15, 1.8, 1.8));
                break;

            case ELIGIBLE:
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(poly(false, 8, 12.3, 11, 15.3, 16.3, 9));
                break;

            case MATCH:
                g.draw(new Line2D.Double(4, 8, 19, 8));
                g.draw(poly(false, 15, 4, 19, 8, 15, 12));
                g.draw(new Line2D.Double(20, 16, 5, 16));
                g.draw(poly(false, 9, 12, 5, 16, 9, 20));
                break;

            case CLIPBOARD:
                g.draw(new RoundRectangle2D.Double(5, 5, 14, 16, 3, 3));
                g.fill(new RoundRectangle2D.Double(8.5, 2.5, 7, 5, 2, 2));
                g.draw(new Line2D.Double(8.5, 12, 15.5, 12));
                g.draw(new Line2D.Double(8.5, 16, 13.5, 16));
                break;

            case CHART:
                g.draw(new Line2D.Double(3.5, 20.5, 20.5, 20.5));
                g.fill(new RoundRectangle2D.Double(5, 12, 3.4, 8, 1, 1));
                g.fill(new RoundRectangle2D.Double(10.3, 6, 3.4, 14, 1, 1));
                g.fill(new RoundRectangle2D.Double(15.6, 9.5, 3.4, 10.5, 1, 1));
                break;

            case BRIEFCASE:
                g.draw(new RoundRectangle2D.Double(3, 8, 18, 12, 3, 3));
                g.draw(poly(false, 9, 8, 9, 5, 15, 5, 15, 8));
                g.draw(new Line2D.Double(3, 13.5, 21, 13.5));
                break;

            case INFO:
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(new Line2D.Double(12, 11, 12, 16.5));
                g.fill(new Ellipse2D.Double(11, 6.8, 2, 2));
                break;

            case WARN:
                g.draw(poly(true, 12, 4, 21, 19.5, 3, 19.5));
                g.draw(new Line2D.Double(12, 10, 12, 14));
                g.fill(new Ellipse2D.Double(11, 15.8, 2, 2));
                break;

            case ERROR:
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(new Line2D.Double(8.7, 8.7, 15.3, 15.3));
                g.draw(new Line2D.Double(15.3, 8.7, 8.7, 15.3));
                break;

            case QUESTION:
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(new Arc2D.Double(9, 7.5, 6, 5, 160, -250, Arc2D.OPEN));
                g.draw(new Line2D.Double(12, 12.6, 12, 14));
                g.fill(new Ellipse2D.Double(11, 15.6, 2, 2));
                break;

            default:
                break;
        }
    }

    private static Path2D poly(boolean closed, double... xy) {

        Path2D path = new Path2D.Double();
        path.moveTo(xy[0], xy[1]);

        for (int i = 2; i < xy.length; i += 2) {
            path.lineTo(xy[i], xy[i + 1]);
        }

        if (closed) {
            path.closePath();
        }

        return path;
    }

    /** A rounded square in a soft tint of the accent colour holding an icon. */
    public static JComponent chip(Glyph glyph, Color accent, int size) {

        final int iconSize = Math.round(size * 0.5f);

        JComponent chip = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                quality(g2);
                g2.setColor(alpha(accent, 30));
                g2.fill(new RoundRectangle2D.Double(
                        0, 0, getWidth(), getHeight(), 14, 14
                ));
                int x = (getWidth() - iconSize) / 2;
                int y = (getHeight() - iconSize) / 2;
                icon(glyph, iconSize, accent).paintIcon(this, g2, x, y);
                g2.dispose();
            }
        };

        Dimension d = new Dimension(size, size);
        chip.setPreferredSize(d);
        chip.setMinimumSize(d);
        chip.setMaximumSize(d);
        chip.setOpaque(false);
        return chip;
    }

    // =====================================================================
    //  BUTTONS
    // =====================================================================

    /**
     * Variants: primary, success, danger, secondary, ghost, nav, navActive.
     */
    public static JButton button(String text, Glyph glyph, String variant) {

        JButton button = new JButton(text);
        button.putClientProperty("variant", variant);
        button.setFont(font(Font.BOLD, 13));
        button.setForeground(foregroundFor(variant));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setIconTextGap(8);

        if (glyph != null) {
            button.setIcon(icon(glyph, 17, foregroundFor(variant)));
        }

        return button;
    }

    private static Color foregroundFor(String variant) {
        switch (variant) {
            case "secondary":
                return TEXT;
            case "ghost":
                return PRIMARY;
            case "nav":
                return SIDEBAR_TEXT;
            default:
                return Color.WHITE;
        }
    }

    public static class ButtonUI extends BasicButtonUI {

        public static ComponentUI createUI(JComponent c) {
            return new ButtonUI();
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton b = (AbstractButton) c;
            b.setOpaque(false);
            b.setRolloverEnabled(true);
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        private static String variant(AbstractButton b) {
            Object v = b.getClientProperty("variant");
            return v == null ? "primary" : v.toString();
        }

        private static Color foreground(AbstractButton b) {

            if (!b.isEnabled()) {
                return new Color(0x94A3B8);
            }

            String v = variant(b);
            ButtonModel m = b.getModel();

            if (v.equals("nav") && (m.isRollover() || m.isPressed())) {
                return Color.WHITE;
            }

            return b.getForeground();
        }

        @Override
        public void paint(Graphics g, JComponent c) {

            AbstractButton b = (AbstractButton) c;
            ButtonModel m = b.getModel();
            String v = variant(b);

            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);

            int w = c.getWidth();
            int h = c.getHeight();
            boolean hot = m.isRollover() || m.isPressed();

            Color fill = null;
            Color line = null;

            if (!b.isEnabled()) {
                fill = BORDER;
            } else {
                switch (v) {
                    case "success":
                        fill = m.isPressed() ? new Color(0x166534)
                                : hot ? new Color(0x15803D) : SUCCESS;
                        break;
                    case "danger":
                        fill = m.isPressed() ? new Color(0x991B1B)
                                : hot ? new Color(0xB91C1C) : DANGER;
                        break;
                    case "secondary":
                        fill = m.isPressed() ? new Color(0xE2E8F0)
                                : hot ? TABLE_HEADER : SURFACE;
                        line = BORDER_STRONG;
                        break;
                    case "ghost":
                        fill = hot ? PRIMARY_SOFT : null;
                        break;
                    case "nav":
                        fill = hot ? SIDEBAR_HOVER : null;
                        break;
                    case "navActive":
                        fill = PRIMARY;
                        break;
                    default:
                        fill = m.isPressed() ? new Color(0x3730A3)
                                : hot ? PRIMARY_DARK : PRIMARY;
                        break;
                }
            }

            if (fill != null) {
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
            }

            if (line != null) {
                g2.setColor(line);
                g2.draw(new RoundRectangle2D.Double(
                        0.5, 0.5, w - 1, h - 1, 12, 12
                ));
            }

            g2.dispose();

            super.paint(g, c);
        }

        @Override
        protected void paintText(
                Graphics g, JComponent c, Rectangle textRect, String text
        ) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);
            g2.setFont(b.getFont());
            g2.setColor(foreground(b));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, textRect.x, textRect.y + fm.getAscent());
            g2.dispose();
        }

        @Override
        protected void paintFocus(
                Graphics g, AbstractButton b, Rectangle viewRect,
                Rectangle textRect, Rectangle iconRect
        ) {
            // No dotted focus rectangle.
        }
    }

    // =====================================================================
    //  LABELS / HEADINGS
    // =====================================================================

    /** Small form-field caption. */
    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, 12));
        label.setForeground(MUTED);
        return label;
    }

    /** Bold heading used at the top of a card. */
    public static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, 15));
        label.setForeground(TEXT);
        return label;
    }

    /** Muted helper text. */
    public static JLabel hint(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.PLAIN, 12));
        label.setForeground(MUTED);
        return label;
    }

    /** A caption above a field, stacked vertically. */
    public static JPanel labeled(String caption, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(label(caption), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Wraps a button so that it lines up with fields that have a caption
     * above them (leaves space for the caption).
     */
    public static JPanel alignedWithFields(JComponent button) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, 0, 0, 0));
        panel.add(button, BorderLayout.CENTER);
        return panel;
    }

    /** Status line with a coloured dot-free, readable style. */
    public static void styleInfoLabel(JLabel label) {
        label.setFont(font(Font.BOLD, 13));
        label.setForeground(PRIMARY);
    }

    // =====================================================================
    //  CARDS / HEADER
    // =====================================================================

    public static class Card extends JPanel {

        public Card(LayoutManager layout, int padding) {
            super(layout);
            setOpaque(false);
            setBackground(SURFACE);
            setBorder(new EmptyBorder(padding, padding, padding + 2, padding));
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(15, 23, 42, 10));
            g2.fill(new RoundRectangle2D.Double(0, 2, w, h - 2, 18, 18));

            g2.setColor(SURFACE);
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h - 2, 16, 16));

            g2.setColor(BORDER);
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 3, 16, 16));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static Card card(LayoutManager layout, int padding) {
        return new Card(layout, padding);
    }

    private static class GradientPanel extends JPanel {

        GradientPanel(LayoutManager layout) {
            super(layout);
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);

            int w = getWidth();
            int h = getHeight();

            g2.setPaint(new GradientPaint(
                    0, 0, new Color(0x4338CA), w, h, new Color(0x7C3AED)
            ));
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(255, 255, 255, 22));
            g2.fill(new Ellipse2D.Double(w - 210, -90, 260, 260));
            g2.setColor(new Color(255, 255, 255, 16));
            g2.fill(new Ellipse2D.Double(w - 340, h - 70, 190, 190));

            g2.dispose();
        }
    }

    /** Lays children out left to right with an exact gap and no outer indent. */
    public static class HRow implements LayoutManager {

        private final int gap;

        public HRow(int gap) {
            this.gap = gap;
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            int w = 0;
            int h = 0;
            int n = 0;
            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                Dimension d = c.getPreferredSize();
                w += d.width;
                h = Math.max(h, d.height);
                n++;
            }
            Insets in = parent.getInsets();
            w += gap * Math.max(0, n - 1) + in.left + in.right;
            return new Dimension(w, h + in.top + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int x = in.left;
            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                Dimension d = c.getPreferredSize();
                c.setBounds(x, in.top, d.width, d.height);
                x += d.width + gap;
            }
        }
    }

    /** A transparent row of buttons/fields with an exact gap. */
    public static JPanel row(int gap) {
        JPanel panel = new JPanel(new HRow(gap));
        panel.setOpaque(false);
        return panel;
    }

    /** Gradient banner shown at the top of every module window. */
    public static JPanel pageHeader(String title, String subtitle, Glyph glyph) {

        JPanel header = new GradientPanel(new BorderLayout(18, 0));
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JComponent iconBox = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                quality(g2);
                g2.setColor(new Color(255, 255, 255, 45));
                g2.fill(new RoundRectangle2D.Double(
                        0, 0, getWidth(), getHeight(), 18, 18
                ));
                icon(glyph, 28, Color.WHITE).paintIcon(
                        this, g2, (getWidth() - 28) / 2, (getHeight() - 28) / 2
                );
                g2.dispose();
            }
        };
        iconBox.setPreferredSize(new Dimension(54, 54));

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(iconBox);

        JLabel crumb = new JLabel("NEXUS   \u203A   PLACEMENT MANAGEMENT");
        crumb.setFont(font(Font.BOLD, 11));
        crumb.setForeground(new Color(255, 255, 255, 170));

        JLabel heading = new JLabel(title);
        heading.setFont(font(Font.BOLD, 26));
        heading.setForeground(Color.WHITE);

        JLabel sub = new JLabel(subtitle);
        sub.setFont(font(Font.PLAIN, 13));
        sub.setForeground(new Color(255, 255, 255, 215));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        crumb.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(crumb);
        text.add(Box.createVerticalStrut(4));
        text.add(heading);
        text.add(Box.createVerticalStrut(3));
        text.add(sub);

        header.add(iconWrap, BorderLayout.WEST);
        header.add(text, BorderLayout.CENTER);

        return header;
    }

    /** A statistic tile: icon chip, caption, big value, small description. */
    public static JPanel statCard(
            String title, JLabel value, String caption,
            Color accent, Glyph glyph
    ) {

        Card card = card(new BorderLayout(12, 0), 14);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font(Font.BOLD, 11));
        titleLabel.setForeground(MUTED);

        value.setFont(font(Font.BOLD, 28));
        value.setForeground(TEXT);

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(font(Font.PLAIN, 12));
        captionLabel.setForeground(MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        captionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(value);
        text.add(Box.createVerticalStrut(1));
        text.add(captionLabel);

        JPanel chipWrap = new JPanel(new BorderLayout());
        chipWrap.setOpaque(false);
        chipWrap.add(chip(glyph, accent, 44), BorderLayout.NORTH);

        card.add(chipWrap, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    // =====================================================================
    //  TEXT FIELDS AND DROP-DOWNS
    // =====================================================================

    private static Color ancestorBackground(Component c) {

        Container p = c.getParent();

        while (p != null) {
            if (p instanceof Card) {
                return SURFACE;
            }
            if (p.isOpaque()) {
                return p.getBackground();
            }
            p = p.getParent();
        }

        return BG;
    }

    /** Rounded outline that highlights while the field has focus. */
    private static class FieldBorder extends AbstractBorder {

        private final Insets padding;

        FieldBorder(Insets padding) {
            this.padding = padding;
        }

        @Override
        public void paintBorder(
                Component c, Graphics g, int x, int y, int w, int h
        ) {
            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);

            Shape outer = new Rectangle2D.Double(x, y, w, h);
            Shape inner = new RoundRectangle2D.Double(
                    x + 0.5, y + 0.5, w - 1, h - 1, 10, 10
            );

            // Hide the square corners behind the rounded outline.
            Area corners = new Area(outer);
            corners.subtract(new Area(new RoundRectangle2D.Double(
                    x, y, w, h, 10, 10
            )));
            g2.setColor(ancestorBackground(c));
            g2.fill(corners);

            boolean focused = c.hasFocus()
                    || (c instanceof JComboBox
                    && ((JComboBox<?>) c).isPopupVisible());

            if (focused) {
                g2.setColor(alpha(PRIMARY, 45));
                g2.setStroke(new BasicStroke(3f));
                g2.draw(new RoundRectangle2D.Double(
                        x + 1, y + 1, w - 2, h - 2, 10, 10
                ));
            }

            g2.setStroke(new BasicStroke(1f));
            g2.setColor(focused ? PRIMARY : BORDER_STRONG);
            g2.draw(inner);

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(padding.top, padding.left,
                    padding.bottom, padding.right);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(padding.top, padding.left,
                    padding.bottom, padding.right);
            return insets;
        }
    }

    private static final FocusListener REPAINT_ON_FOCUS = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            e.getComponent().repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            e.getComponent().repaint();
        }
    };

    public static void styleTextField(JTextField field) {
        field.setFont(font(Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(SURFACE);
        field.setOpaque(true);
        field.setCaretColor(PRIMARY);
        field.setSelectionColor(SELECTION);
        field.setSelectedTextColor(TEXT);
        field.setBorder(new FieldBorder(new Insets(9, 12, 9, 12)));
        field.removeFocusListener(REPAINT_ON_FOCUS);
        field.addFocusListener(REPAINT_ON_FOCUS);
    }

    public static JTextField textField() {
        JTextField field = new JTextField();
        styleTextField(field);
        return field;
    }

    public static void styleCombo(JComboBox<?> combo) {
        combo.setUI(new ComboUI());
        combo.setFont(font(Font.PLAIN, 14));
        combo.setForeground(TEXT);
        combo.setBackground(SURFACE);
        combo.setOpaque(true);
        combo.setBorder(new FieldBorder(new Insets(5, 8, 5, 2)));
        combo.setRenderer(new ComboRenderer());
        combo.setMaximumRowCount(12);
        combo.removeFocusListener(REPAINT_ON_FOCUS);
        combo.addFocusListener(REPAINT_ON_FOCUS);
    }

    private static class ComboRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus
        ) {
            super.getListCellRendererComponent(
                    list, value, index, isSelected, false
            );

            setFont(font(Font.PLAIN, 14));
            setOpaque(true);

            if (index >= 0 && isSelected) {
                setBackground(SELECTION);
                setForeground(PRIMARY_DARK);
            } else {
                setBackground(SURFACE);
                setForeground(TEXT);
            }

            setBorder(new EmptyBorder(8, 10, 8, 10));
            return this;
        }
    }

    public static class ComboUI extends BasicComboBoxUI {

        public static ComponentUI createUI(JComponent c) {
            return new ComboUI();
        }

        @Override
        protected JButton createArrowButton() {

            JButton arrow = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    quality(g2);
                    g2.setColor(MUTED);
                    g2.setStroke(new BasicStroke(
                            2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND
                    ));
                    double cx = getWidth() / 2.0;
                    double cy = getHeight() / 2.0;
                    g2.draw(poly(false,
                            cx - 4, cy - 2, cx, cy + 2, cx + 4, cy - 2));
                    g2.dispose();
                }
            };

            arrow.setBorder(new EmptyBorder(0, 0, 0, 0));
            arrow.setContentAreaFilled(false);
            arrow.setFocusable(false);
            arrow.setOpaque(false);
            arrow.setPreferredSize(new Dimension(30, 20));
            return arrow;
        }

        @Override
        public void paintCurrentValueBackground(
                Graphics g, Rectangle bounds, boolean hasFocus
        ) {
            g.setColor(comboBox.getBackground());
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override
        public void paintCurrentValue(
                Graphics g, Rectangle bounds, boolean hasFocus
        ) {
            // Never paint the "selected" blue block behind the current value.
            super.paintCurrentValue(g, bounds, false);
        }

        @Override
        protected ComboPopup createPopup() {

            BasicComboPopup popup = new BasicComboPopup(comboBox) {
                @Override
                protected JScrollPane createScroller() {
                    JScrollPane scroller = super.createScroller();
                    scroller.getVerticalScrollBar().setUI(new ScrollUI());
                    scroller.getVerticalScrollBar()
                            .setPreferredSize(new Dimension(10, 10));
                    scroller.setBorder(new EmptyBorder(0, 0, 0, 0));
                    return scroller;
                }
            };

            popup.setBorder(new LineBorder(BORDER_STRONG, 1));
            return popup;
        }
    }

    // =====================================================================
    //  SCROLL BARS / SCROLL PANES
    // =====================================================================

    public static class ScrollUI extends BasicScrollBarUI {

        public static ComponentUI createUI(JComponent c) {
            return new ScrollUI();
        }

        private static JButton none() {
            JButton b = new JButton();
            Dimension zero = new Dimension(0, 0);
            b.setPreferredSize(zero);
            b.setMinimumSize(zero);
            b.setMaximumSize(zero);
            b.setBorder(null);
            return b;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return none();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return none();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            // Transparent track.
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {

            if (r.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);
            g2.setColor(isThumbRollover() || isDragging
                    ? new Color(0x94A3B8) : BORDER_STRONG);
            g2.fill(new RoundRectangle2D.Double(
                    r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8
            ));
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return new Dimension(12, 12);
        }
    }

    public static void styleScroll(JScrollPane scroll) {

        scroll.setBorder(new LineBorder(BORDER, 1));
        scroll.setBackground(SURFACE);
        scroll.getViewport().setBackground(SURFACE);
        scroll.getVerticalScrollBar().setUI(new ScrollUI());
        scroll.getHorizontalScrollBar().setUI(new ScrollUI());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);

        JPanel corner = new JPanel();
        corner.setBackground(SURFACE);
        scroll.setCorner(JScrollPane.LOWER_RIGHT_CORNER, corner);
    }

    /** Page-level scroll pane (no border, light background). */
    public static void styleScrollPage(JScrollPane scroll) {
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUI(new ScrollUI());
        scroll.getHorizontalScrollBar().setUI(new ScrollUI());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
    }

    // =====================================================================
    //  TABLES
    // =====================================================================

    public static void styleTable(JTable table) {

        table.setFont(font(Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(SURFACE);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xEEF2F7));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(PRIMARY_DARK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        CellRenderer renderer = new CellRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Number.class, renderer);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);
        header.setBackground(TABLE_HEADER);
        header.setOpaque(true);
    }

    /** A styled table inside a styled scroll pane. */
    public static JScrollPane tableScroll(JTable table) {
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        styleScroll(scroll);
        return scroll;
    }

    private static class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column
        ) {
            super.getTableCellRendererComponent(
                    table, value, isSelected, false, row, column
            );

            setFont(table.getFont());
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setHorizontalAlignment(SwingConstants.LEFT);

            if (isSelected) {
                setBackground(SELECTION);
                setForeground(PRIMARY_DARK);
            } else {
                setBackground(row % 2 == 0 ? SURFACE : SURFACE_ALT);
                setForeground(TEXT);
            }

            return this;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column
        ) {
            super.getTableCellRendererComponent(
                    table, value, false, false, row, column
            );

            String text = value == null ? "" : value.toString();

            RowSorter<?> sorter = table.getRowSorter();
            if (sorter != null && !sorter.getSortKeys().isEmpty()) {
                RowSorter.SortKey key = sorter.getSortKeys().get(0);
                if (key.getColumn() == table.convertColumnIndexToModel(column)) {
                    if (key.getSortOrder() == SortOrder.ASCENDING) {
                        text += "   \u25B2";
                    } else if (key.getSortOrder() == SortOrder.DESCENDING) {
                        text += "   \u25BC";
                    }
                }
            }

            setText(text);
            setFont(font(Font.BOLD, 12));
            setForeground(MUTED);
            setBackground(TABLE_HEADER);
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, BORDER_STRONG),
                    new EmptyBorder(0, 14, 0, 14)
            ));

            return this;
        }
    }

    /** Coloured "pill" for status-style columns. */
    public static TableCellRenderer badgeRenderer(
            Function<String, Color> colorFor
    ) {
        return new BadgeRenderer(colorFor, false);
    }

    /** Same as badgeRenderer but the pill is aligned to the left. */
    public static TableCellRenderer badgeRendererLeft(
            Function<String, Color> colorFor
    ) {
        return new BadgeRenderer(colorFor, true);
    }

    private static class BadgeRenderer extends DefaultTableCellRenderer {

        private final Function<String, Color> colorFor;
        private final boolean left;
        private Color pill;

        BadgeRenderer(Function<String, Color> colorFor, boolean left) {
            this.colorFor = colorFor;
            this.left = left;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column
        ) {
            super.getTableCellRendererComponent(
                    table, value, isSelected, false, row, column
            );

            String text = value == null ? "" : value.toString();
            pill = text.isEmpty() ? null : colorFor.apply(text);

            setHorizontalAlignment(left ? SwingConstants.LEFT : SwingConstants.CENTER);
            setFont(font(Font.BOLD, 12));
            setForeground(pill == null ? TEXT : pill);
            setBorder(left ? new EmptyBorder(0, 21, 0, 6) : new EmptyBorder(0, 6, 0, 6));
            setOpaque(false);

            setBackground(isSelected ? SELECTION
                    : row % 2 == 0 ? SURFACE : SURFACE_ALT);

            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);

            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (pill != null && getText() != null && !getText().isEmpty()) {
                FontMetrics fm = g2.getFontMetrics(getFont());
                int w = fm.stringWidth(getText()) + 26;
                int h = 24;
                int x = left ? 8 : (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;
                g2.setColor(alpha(pill, 30));
                g2.fill(new RoundRectangle2D.Double(x, y, w, h, h, h));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =====================================================================
    //  TABS
    // =====================================================================

    public static void styleTabs(JTabbedPane tabs) {

        tabs.setUI(new TabsUI());
        tabs.setFont(font(Font.BOLD, 13));
        tabs.setOpaque(false);
        tabs.setFocusable(false);
        tabs.setBorder(new EmptyBorder(0, 0, 0, 0));

        Runnable recolor = () -> {
            for (int i = 0; i < tabs.getTabCount(); i++) {
                tabs.setForegroundAt(
                        i, i == tabs.getSelectedIndex() ? PRIMARY : MUTED
                );
            }
        };

        tabs.addChangeListener(e -> recolor.run());
        recolor.run();
    }

    /** Recolours tab titles; call after adding the last tab. */
    public static void refreshTabs(JTabbedPane tabs) {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setForegroundAt(
                    i, i == tabs.getSelectedIndex() ? PRIMARY : MUTED
            );
        }
    }

    public static class TabsUI extends BasicTabbedPaneUI {

        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabAreaInsets = new Insets(0, 28, 0, 0);
            contentBorderInsets = new Insets(0, 0, 0, 0);
            tabInsets = new Insets(11, 26, 11, 26);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
        }

        @Override
        protected void paintTabBackground(
                Graphics g, int tabPlacement, int tabIndex,
                int x, int y, int w, int h, boolean isSelected
        ) {
            g.setColor(isSelected ? SURFACE : BG);
            g.fillRect(x, y, w, h);
        }

        @Override
        protected void paintTabBorder(
                Graphics g, int tabPlacement, int tabIndex,
                int x, int y, int w, int h, boolean isSelected
        ) {
            if (isSelected) {
                g.setColor(PRIMARY);
                g.fillRect(x, y + h - 3, w, 3);
            }
        }

        @Override
        protected void paintContentBorder(
                Graphics g, int tabPlacement, int selectedIndex
        ) {
            g.setColor(BORDER);
            int y = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
            g.drawLine(0, y, tabPane.getWidth(), y);
        }

        @Override
        protected void paintFocusIndicator(
                Graphics g, int tabPlacement, Rectangle[] rects,
                int tabIndex, Rectangle iconRect, Rectangle textRect,
                boolean isSelected
        ) {
            // No focus rectangle.
        }
    }
}
