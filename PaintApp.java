import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class PaintApp extends JFrame {
    private DrawArea drawArea;
    private static final Color[] COLOR_PALETTE = {
            Color.BLACK, new Color(64, 64, 64), new Color(96, 96, 96), new Color(160, 160, 160), Color.LIGHT_GRAY,
            Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA
    };

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
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

        // Tool Buttons
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

        // Color Buttons
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        for (Color color : COLOR_PALETTE) {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(25, 25));
            btn.setBackground(color);
            btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            btn.addActionListener(e -> drawArea.setCurrentColor(color));
            colorPanel.add(btn);
        }

        JPanel topPanel = new JPanel(new BorderLayout());
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
        private Color currentColor = Color.BLACK;
        private Point startPoint, endPoint;
        private final java.util.List<ColoredShape> shapes = new ArrayList<>();

        public DrawArea() {
            setBackground(Color.WHITE);

            MouseAdapter mouseHandler = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                    if (currentTool == Tool.PENCIL || currentTool == Tool.ERASER) {
                        endPoint = startPoint;
                        addShape(startPoint, endPoint);
                        repaint();
                    }
                }

                public void mouseDragged(MouseEvent e) {
                    endPoint = e.getPoint();
                    if (currentTool == Tool.PENCIL || currentTool == Tool.ERASER) {
                        addShape(startPoint, endPoint);
                        startPoint = endPoint;
                        repaint();
                    }
                }

                public void mouseReleased(MouseEvent e) {
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

        public void setCurrentColor(Color color) {
            this.currentColor = color;
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
            shapes.add(new ColoredShape(shape, currentTool == Tool.ERASER ? Color.WHITE : currentColor));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            for (ColoredShape cs : shapes) {
                g2.setColor(cs.color);
                g2.draw(cs.shape);
            }
        }

        static class ColoredShape {
            Shape shape;
            Color color;

            public ColoredShape(Shape shape, Color color) {
                this.shape = shape;
                this.color = color;
            }
        }
    }
}
