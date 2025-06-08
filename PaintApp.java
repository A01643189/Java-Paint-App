import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class PaintApp extends JFrame {
    private DrawArea drawArea;
    private JSlider strokeSlider;

    private static final Color[] COLOR_PALETTE = {
            Color.BLACK, Color.WHITE,
            Color.RED, Color.ORANGE, Color.YELLOW,
            Color.GREEN, Color.CYAN, Color.BLUE,
            Color.MAGENTA, Color.PINK
    };

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        SwingUtilities.invokeLater(PaintApp::new);
    }

    public PaintApp() {
        setTitle("Java Paint App");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        drawArea = new DrawArea();
        add(drawArea, BorderLayout.CENTER);

        // === Tool Buttons with Scaled Icons ===
        JToggleButton pencilBtn = createToggleToolButton("pencil.png", "Lines");
        JToggleButton rectBtn = createToggleToolButton("rectangle.png", "Rectangle");
        JToggleButton ovalBtn = createToggleToolButton("oval.png", "Oval");
        JToggleButton eraserBtn = createToggleToolButton("eraser.png", "Eraser");
        JButton clearBtn = createIconButton("clear.png", "Clear");

        ButtonGroup tools = new ButtonGroup();
        tools.add(pencilBtn);
        tools.add(rectBtn);
        tools.add(ovalBtn);
        tools.add(eraserBtn);

        pencilBtn.setSelected(true);
        drawArea.setTool(DrawArea.Tool.PENCIL);

        pencilBtn.addActionListener(e -> drawArea.setTool(DrawArea.Tool.PENCIL));
        rectBtn.addActionListener(e -> drawArea.setTool(DrawArea.Tool.RECTANGLE));
        ovalBtn.addActionListener(e -> drawArea.setTool(DrawArea.Tool.OVAL));
        eraserBtn.addActionListener(e -> drawArea.setTool(DrawArea.Tool.ERASER));
        clearBtn.addActionListener(e -> drawArea.clearCanvas());

        strokeSlider = new JSlider(1, 20, 2);
        strokeSlider.setMajorTickSpacing(5);
        strokeSlider.setMinorTickSpacing(1);
        strokeSlider.setPaintTicks(true);
        strokeSlider.setPaintLabels(true);
        strokeSlider.addChangeListener(e -> drawArea.setStrokeWidth(strokeSlider.getValue()));

        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolPanel.add(pencilBtn);
        toolPanel.add(rectBtn);
        toolPanel.add(ovalBtn);
        toolPanel.add(eraserBtn);
        toolPanel.add(clearBtn);
        toolPanel.add(new JLabel("Stroke:"));
        toolPanel.add(strokeSlider);

        JPanel palettePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        palettePanel.setPreferredSize(new Dimension(300, 50));

        JPanel strokePreview = new JPanel();
        JPanel fillPreview = new JPanel();

        for (Color color : COLOR_PALETTE) {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(25, 25));
            btn.setBackground(color);
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setBorderPainted(true);
            btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        drawArea.setStrokeColor(color);
                        strokePreview.setBackground(color);
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        drawArea.setFillColor(color);
                        fillPreview.setBackground(color);
                    }
                }
            });
            palettePanel.add(btn);
        }

        strokePreview.setPreferredSize(new Dimension(60, 40));
        strokePreview.setBackground(drawArea.getStrokeColor());
        strokePreview.setBorder(BorderFactory.createTitledBorder("Stroke"));

        fillPreview.setPreferredSize(new Dimension(60, 40));
        fillPreview.setBackground(drawArea.getFillColor());
        fillPreview.setBorder(BorderFactory.createTitledBorder("Fill"));

        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        previewPanel.add(strokePreview);
        previewPanel.add(Box.createVerticalStrut(5));
        previewPanel.add(fillPreview);

        JPanel colorPanel = new JPanel(new BorderLayout());
        colorPanel.add(palettePanel, BorderLayout.CENTER);
        colorPanel.add(previewPanel, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        topPanel.add(toolPanel, BorderLayout.WEST);
        topPanel.add(colorPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        setVisible(true);
    }

    private JToggleButton createToggleToolButton(String iconPath, String tooltip) {
        ImageIcon icon = new ImageIcon(
                new ImageIcon(iconPath).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JToggleButton btn = new JToggleButton(icon);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(40, 40));
        return btn;
    }

    private JButton createIconButton(String iconPath, String tooltip) {
        ImageIcon icon = new ImageIcon(
                new ImageIcon(iconPath).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(40, 40));
        return btn;
    }

    static class DrawArea extends JPanel {
        enum Tool {
            PENCIL, RECTANGLE, OVAL, ERASER
        }

        private Tool currentTool = Tool.PENCIL;
        private Color strokeColor = Color.BLACK;
        private Color fillColor = Color.WHITE;
        private int strokeWidth = 2;
        private Point startPoint, endPoint;
        private final java.util.List<ColoredShape> shapes = new ArrayList<>();
        private boolean isRightClick = false;

        public DrawArea() {
            setBackground(Color.WHITE);

            MouseAdapter mouseHandler = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                    isRightClick = SwingUtilities.isRightMouseButton(e);
                    if (isRightClick)
                        return;

                    if (currentTool == Tool.PENCIL || currentTool == Tool.ERASER) {
                        endPoint = startPoint;
                        addShape(startPoint, endPoint);
                        repaint();
                    }
                }

                public void mouseDragged(MouseEvent e) {
                    if (isRightClick)
                        return;

                    endPoint = e.getPoint();
                    if (currentTool == Tool.PENCIL || currentTool == Tool.ERASER) {
                        addShape(startPoint, endPoint);
                        startPoint = endPoint;
                        repaint();
                    }
                }

                public void mouseReleased(MouseEvent e) {
                    if (isRightClick)
                        return;

                    endPoint = e.getPoint();
                    if (currentTool != Tool.PENCIL && currentTool != Tool.ERASER) {
                        addShape(startPoint, endPoint);
                        repaint();
                    }
                }
            };

            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }

        public void setTool(Tool tool) {
            this.currentTool = tool;
        }

        public void setStrokeColor(Color color) {
            this.strokeColor = color;
        }

        public void setFillColor(Color color) {
            this.fillColor = color;
        }

        public void setStrokeWidth(int width) {
            this.strokeWidth = width;
        }

        public Color getStrokeColor() {
            return strokeColor;
        }

        public Color getFillColor() {
            return fillColor;
        }

        public void clearCanvas() {
            shapes.clear();
            repaint();
        }

        private void addShape(Point start, Point end) {
            Shape shape;
            if (currentTool == Tool.RECTANGLE) {
                shape = new Rectangle(Math.min(start.x, end.x), Math.min(start.y, end.y),
                        Math.abs(start.x - end.x), Math.abs(start.y - end.y));
            } else if (currentTool == Tool.OVAL) {
                shape = new java.awt.geom.Ellipse2D.Float(Math.min(start.x, end.x), Math.min(start.y, end.y),
                        Math.abs(start.x - end.x), Math.abs(start.y - end.y));
            } else {
                shape = new Line2D.Float(start, end);
            }

            Color stroke = currentTool == Tool.ERASER ? Color.WHITE : strokeColor;
            shapes.add(new ColoredShape(shape, stroke,
                    (currentTool == Tool.PENCIL || currentTool == Tool.ERASER) ? null : fillColor, strokeWidth));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            for (ColoredShape cs : shapes) {
                if (cs.fill != null) {
                    g2.setColor(cs.fill);
                    g2.fill(cs.shape);
                }
                if (cs.stroke != null) {
                    g2.setColor(cs.stroke);
                    g2.setStroke(new BasicStroke(cs.strokeWidth));
                    g2.draw(cs.shape);
                }
            }
        }

        static class ColoredShape {
            Shape shape;
            Color stroke;
            Color fill;
            int strokeWidth;

            public ColoredShape(Shape shape, Color stroke, Color fill, int strokeWidth) {
                this.shape = shape;
                this.stroke = stroke;
                this.fill = fill;
                this.strokeWidth = strokeWidth;
            }
        }
    }
}
