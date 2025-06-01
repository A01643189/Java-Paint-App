// import com.formdev.flatlaf.FlatLightLaf; ONLY IF WANTED TO USE FLAT LAF

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class PaintApp extends JFrame {
    private DrawArea drawArea;
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

        // === Tools ===
        JToggleButton pencilBtn = new JToggleButton("Lines");
        JToggleButton rectBtn = new JToggleButton("Rectangle");
        JToggleButton ovalBtn = new JToggleButton("Circle");
        JToggleButton eraserBtn = new JToggleButton("Eraser");
        JButton clearBtn = new JButton("Clear");

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

        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolPanel.add(pencilBtn);
        toolPanel.add(rectBtn);
        toolPanel.add(ovalBtn);
        toolPanel.add(eraserBtn);
        toolPanel.add(clearBtn);

        // === Color Buttons ===
        JPanel palettePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
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

        // === Stroke & Fill Panels ===
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

        // === Combine Panels ===
        JPanel colorPanel = new JPanel(new BorderLayout());
        colorPanel.add(palettePanel, BorderLayout.CENTER);
        colorPanel.add(previewPanel, BorderLayout.EAST);

        // === Top Panel ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        topPanel.add(toolPanel, BorderLayout.WEST);
        topPanel.add(colorPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        setVisible(true);
    }

    static class DrawArea extends JPanel {
        enum Tool {
            PENCIL, RECTANGLE, OVAL, ERASER
        }

        private Tool currentTool = Tool.PENCIL;
        private Color strokeColor = Color.BLACK;
        private Color fillColor = Color.WHITE;
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
            shapes.add(new ColoredShape(shape,
                    currentTool == Tool.ERASER ? Color.WHITE : strokeColor,
                    currentTool == Tool.PENCIL || currentTool == Tool.ERASER ? null : fillColor));
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
                    g2.draw(cs.shape);
                }
            }
        }

        static class ColoredShape {
            Shape shape;
            Color stroke;
            Color fill;

            public ColoredShape(Shape shape, Color stroke, Color fill) {
                this.shape = shape;
                this.stroke = stroke;
                this.fill = fill;
            }
        }
    }
}
