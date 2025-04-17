import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import javax.imageio.ImageIO;
import java.io.File;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.*;

public class RoomDashboard extends JFrame {
    // Color constant
    private static final Map<String, Color> WARM_COLORS = new HashMap<>() {{
        put("Coral", new Color(0xFF6F61));
        put("Rust", new Color(0xD75C37));
        put("Orange", new Color(0xFFB347));
    }};
    
    private static final Map<String, Color> COOL_COLORS = new HashMap<>() {{
        put("Ocean Blue", new Color(0x4B9CD3));
        put("Turquoise", new Color(0x00CED1));
        put("Sea Green", new Color(0x2E8B57));
    }};
    
    private static final Map<String, Color> NEUTRAL_COLORS = new HashMap<>() {{
        put("White", new Color(0xFFFFFF));
        put("Light Gray", new Color(0xF5F5F5));
        put("Gray", new Color(0xA9A9A9));
        put("Dark Gray", new Color(0x808080));
        put("Black", new Color(0x000000));
    }};
    
    private static final Map<String, Color> PASTEL_COLORS = new HashMap<>() {{
        put("Pink", new Color(0xFFD1DC));
        put("Blue", new Color(0xAEC6CF));
        put("Purple", new Color(0xCBAACC));
        put("Green", new Color(0xBFD8B8));
    }};

    // Furniture data structures
    private enum FurnitureCategory {
        CHAIRS("Chairs"),
        TABLES("Tables"),
        SOFAS("Sofas"),
        BEDS("Beds");

        private final String displayName;

        FurnitureCategory(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // Undo/Redo support
    private final Stack<List<FurnitureItem>> undoStack = new Stack<>();
    private final Stack<List<FurnitureItem>> redoStack = new Stack<>();

    private static class FurnitureItem implements Cloneable {
        private final String name;
        private final FurnitureCategory category;
        private final Dimension defaultSize;
        private Color color;
        private Point position;
        private Dimension currentSize;
        private String tooltip;
        private double rotation; // rotation in degrees
        private Shape shape; // for different furniture representations

        public FurnitureItem(String name, FurnitureCategory category, Dimension defaultSize, String tooltip) {
            this.name = name;
            this.category = category;
            this.defaultSize = defaultSize;
            this.currentSize = new Dimension(defaultSize);
            this.color = Color.GRAY;
            this.tooltip = tooltip;
            this.rotation = 0.0;
            this.shape = createDefaultShape();
        }

        private Shape createDefaultShape() {
            switch (category) {
                case CHAIRS:
                    return createChairShape();
                case TABLES:
                    return name.toLowerCase().contains("round") ? 
                           createRoundTableShape() : createTableShape();
                case SOFAS:
                    return createSofaShape();
                case BEDS:
                    return createBedShape();
                default:
                    return new Rectangle2D.Double(0, 0, 1, 1);
            }
        }

        private Shape createChairShape() {
            Path2D.Double path = new Path2D.Double();
            // Chair back
            path.moveTo(0.2, 0);
            path.lineTo(0.8, 0);
            path.lineTo(0.8, 0.3);
            path.lineTo(0.2, 0.3);
            path.closePath();
            // Chair seat
            path.moveTo(0, 0.3);
            path.lineTo(1, 0.3);
            path.lineTo(1, 1);
            path.lineTo(0, 1);
            path.closePath();
            return path;
        }

        private Shape createTableShape() {
            Path2D.Double path = new Path2D.Double();
            // Table top
            path.moveTo(0, 0);
            path.lineTo(1, 0);
            path.lineTo(1, 0.8);
            path.lineTo(0, 0.8);
            path.closePath();
            // Table legs
            double legWidth = 0.1;
            path.moveTo(0, 0.8);
            path.lineTo(legWidth, 0.8);
            path.lineTo(legWidth, 1);
            path.lineTo(0, 1);
            path.closePath();
            path.moveTo(1-legWidth, 0.8);
            path.lineTo(1, 0.8);
            path.lineTo(1, 1);
            path.lineTo(1-legWidth, 1);
            path.closePath();
            return path;
        }

        private Shape createRoundTableShape() {
            return new Ellipse2D.Double(0, 0, 1, 1);
        }

        private Shape createSofaShape() {
            Path2D.Double path = new Path2D.Double();
            // Sofa back
            path.moveTo(0, 0);
            path.lineTo(1, 0);
            path.lineTo(1, 0.4);
            path.lineTo(0, 0.4);
            path.closePath();
            // Sofa seat
            path.moveTo(0.1, 0.4);
            path.lineTo(0.9, 0.4);
            path.lineTo(0.9, 1);
            path.lineTo(0.1, 1);
            path.closePath();
            // Sofa arms
            path.moveTo(0, 0);
            path.lineTo(0.1, 0);
            path.lineTo(0.1, 1);
            path.lineTo(0, 1);
            path.closePath();
            path.moveTo(0.9, 0);
            path.lineTo(1, 0);
            path.lineTo(1, 1);
            path.lineTo(0.9, 1);
            path.closePath();
            return path;
        }

        private Shape createBedShape() {
            Path2D.Double path = new Path2D.Double();
            // Bed frame
            path.moveTo(0, 0);
            path.lineTo(1, 0);
            path.lineTo(1, 1);
            path.lineTo(0, 1);
            path.closePath();
            // Headboard
            path.moveTo(0, 0);
            path.lineTo(1, 0);
            path.lineTo(1, 0.2);
            path.lineTo(0, 0.2);
            path.closePath();
            // Mattress lines
            path.moveTo(0.1, 0.3);
            path.lineTo(0.9, 0.3);
            path.moveTo(0.1, 0.6);
            path.lineTo(0.9, 0.6);
            path.moveTo(0.1, 0.9);
            path.lineTo(0.9, 0.9);
            return path;
        }

        @Override
        public FurnitureItem clone() {
            try {
                FurnitureItem clone = (FurnitureItem) super.clone();
                clone.position = position != null ? new Point(position) : null;
                clone.currentSize = new Dimension(currentSize);
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        public String getName() { return name; }
        public FurnitureCategory getCategory() { return category; }
        public Dimension getDefaultSize() { return defaultSize; }
        public Color getColor() { return color; }
        public void setColor(Color color) { this.color = color; }
        public Point getPosition() { return position; }
        public void setPosition(Point position) { this.position = position; }
        public Dimension getCurrentSize() { return currentSize; }
        public void setCurrentSize(Dimension size) { this.currentSize = size; }
        public String getTooltip() { return tooltip; }
        public double getRotation() { return rotation; }
        public void setRotation(double rotation) { this.rotation = rotation; }
        public Shape getShape() { return shape; }
    }

    // Furniture catalog
    private final Map<FurnitureCategory, java.util.List<FurnitureItem>> furnitureCatalog;

    // List of placed furniture in the room
    private final List<FurnitureItem> placedFurniture = new ArrayList<>();

    private JTree furnitureCatalogTree;
    private JPanel furniturePreviewPanel;
    private FurnitureItem selectedFurniture;
    private JColorChooser furnitureColorChooser;

    // UI Components
    private JPanel roomPreviewPanel;
    private JSpinner roomWidthSpinner;
    private JSpinner roomLengthSpinner;
    private JSpinner roomHeightSpinner;
    private JComboBox<String> measurementUnitCombo;
    private JComboBox<String> furnitureTypeCombo;
    private JList<String> furnitureList;
    private DefaultListModel<String> furnitureListModel;
    private JComboBox<String> colorPresetCombo;
    private JPanel colorPreviewPanel;
    private JToggleButton viewToggleButton;
    private boolean is3DView = false;

    // Room properties
    private Color wallColor = Color.WHITE;
    private Color floorColor = new Color(0xF5F5F5);
    private Color ceilingColor = Color.WHITE;
    private double roomWidth = 4.0;
    private double roomLength = 5.0;
    private double roomHeight = 2.8;
    private String currentUnit = "Meters";

    private JDialog threeDViewDialog;
    private ThreeDViewExporter threeDViewExporter;
    
    private class ThreeDViewExporter extends JPanel {
        private JFXPanel jfxPanel;
        private Group sceneRoot;
        private PerspectiveCamera camera;
        private double mouseOldX, mouseOldY;
        private double mousePosX, mousePosY;
        private double mouseOldRotateX = 0;
        private double mouseOldRotateY = 0;
        private final double CAMERA_INITIAL_DISTANCE = -450;
        private final double CAMERA_INITIAL_X_ANGLE = 70.0;
        private final double CAMERA_INITIAL_Y_ANGLE = 320.0;
        private final double CAMERA_NEAR_CLIP = 0.1;
        private final double CAMERA_FAR_CLIP = 10000.0;
        private final double ROTATION_SPEED = 2.0;
        private final double ZOOM_SPEED = 1.5;
        
        public ThreeDViewExporter() {
            setLayout(new BorderLayout());
            
            // Initialize JavaFX Panel
            jfxPanel = new JFXPanel();
            add(jfxPanel, BorderLayout.CENTER);
            
            // Add control panel
            add(createControlPanel(), BorderLayout.SOUTH);
            
            // Initialize JavaFX Scene
            Platform.runLater(() -> createScene());
        }
        
        private void createScene() {
            sceneRoot = new Group();
            
            // Setup camera
            camera = new PerspectiveCamera(true);
            camera.setNearClip(CAMERA_NEAR_CLIP);
            camera.setFarClip(CAMERA_FAR_CLIP);
            camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
            
            // Create scene
            SubScene scene = new SubScene(sceneRoot, 800, 600, true, SceneAntialiasing.BALANCED);
            scene.setFill(javafx.scene.paint.Color.LIGHTGRAY);
            scene.setCamera(camera);
            
            // Add room and furniture
            addRoom();
            addFurniture();
            
            // Setup mouse controls
            setupMouseControls(scene);
            
            // Create root group
            Group root = new Group(scene);
            
            // Set the scene
            jfxPanel.setScene(new Scene(root));
        }
        
        private void addRoom() {
            double width = (Double) roomWidthSpinner.getValue() * 100;
            double length = (Double) roomLengthSpinner.getValue() * 100;
            double height = (Double) roomHeightSpinner.getValue() * 100;
            
            // Floor
            Box floor = new Box(width, 1, length);
            floor.setMaterial(createMaterial(floorColor));
            floor.setTranslateY(height/2);
            
            // Ceiling
            Box ceiling = new Box(width, 1, length);
            ceiling.setMaterial(createMaterial(ceilingColor));
            ceiling.setTranslateY(-height/2);
            
            // Walls
            Box wallLeft = new Box(1, height, length);
            wallLeft.setMaterial(createMaterial(wallColor));
            wallLeft.setTranslateX(-width/2);
            
            Box wallRight = new Box(1, height, length);
            wallRight.setMaterial(createMaterial(wallColor));
            wallRight.setTranslateX(width/2);
            
            Box wallBack = new Box(width, height, 1);
            wallBack.setMaterial(createMaterial(wallColor));
            wallBack.setTranslateZ(-length/2);
            
            Box wallFront = new Box(width, height, 1);
            wallFront.setMaterial(createMaterial(wallColor));
            wallFront.setTranslateZ(length/2);
            
            sceneRoot.getChildren().addAll(floor, ceiling, wallLeft, wallRight, wallBack, wallFront);
        }
        
        private void addFurniture() {
            for (FurnitureItem item : placedFurniture) {
                Point pos = item.getPosition();
                Dimension size = item.getCurrentSize();
                
                // Create furniture piece
                Box furniture = new Box(
                    size.width,
                    50, // Standard height
                    size.height
                );
                
                // Set material
                furniture.setMaterial(createMaterial(item.getColor()));
                
                // Position
                furniture.setTranslateX(pos.x - getWidth()/2);
                furniture.setTranslateZ(pos.y - getHeight()/2);
                furniture.setTranslateY(0);
                
                // Rotation
                furniture.setRotate(item.getRotation());
                furniture.setRotationAxis(Rotate.Y_AXIS);
                
                sceneRoot.getChildren().add(furniture);
            }
        }
        
        private PhongMaterial createMaterial(Color awtColor) {
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(javafx.scene.paint.Color.rgb(
                awtColor.getRed(),
                awtColor.getGreen(),
                awtColor.getBlue()
            ));
            return material;
        }
        
        private void setupMouseControls(SubScene scene) {
            scene.setOnMousePressed(event -> {
                mouseOldX = event.getSceneX();
                mouseOldY = event.getSceneY();
            });
            
            scene.setOnMouseDragged(event -> {
                mousePosX = event.getSceneX();
                mousePosY = event.getSceneY();
                
                if (event.isPrimaryButtonDown()) {
                    // Rotate
                    double deltaX = (mousePosX - mouseOldX) * ROTATION_SPEED;
                    double deltaY = (mousePosY - mouseOldY) * ROTATION_SPEED;
                    
                    mouseOldRotateX += deltaX;
                    mouseOldRotateY += deltaY;
                    
                    sceneRoot.getTransforms().clear();
                    sceneRoot.getTransforms().addAll(
                        new Rotate(mouseOldRotateX, Rotate.Y_AXIS),
                        new Rotate(mouseOldRotateY, Rotate.X_AXIS)
                    );
                } else if (event.isSecondaryButtonDown()) {
                    // Zoom
                    double factor = (mousePosY - mouseOldY) * ZOOM_SPEED;
                    camera.setTranslateZ(camera.getTranslateZ() + factor);
                }
                
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
            });
            
            scene.setOnScroll(event -> {
                // Mouse wheel zoom
                double delta = event.getDeltaY() * ZOOM_SPEED;
                camera.setTranslateZ(camera.getTranslateZ() + delta);
            });
        }
        
        private JPanel createControlPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            
            JButton exportImageButton = new JButton("Export as Image");
            exportImageButton.addActionListener(e -> exportAsImage());
            
            JButton resetViewButton = new JButton("Reset View");
            resetViewButton.addActionListener(e -> resetView());
            
            panel.add(exportImageButton);
            panel.add(resetViewButton);
            
            return panel;
        }
        
        private void exportAsImage() {
            WritableImage image = jfxPanel.getScene().snapshot(null);
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
                }
                public String getDescription() {
                    return "PNG Images (*.png)";
                }
            });
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        file = new File(file.getPath() + ".png");
                    }
                    
                    // Convert JavaFX image to AWT
                    BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
                    ImageIO.write(bImage, "png", file);
                    
                    JOptionPane.showMessageDialog(this,
                        "3D view exported successfully!",
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error exporting 3D view: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        private void resetView() {
            Platform.runLater(() -> {
                mouseOldRotateX = 0;
                mouseOldRotateY = 0;
                camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
                sceneRoot.getTransforms().clear();
            });
        }
    }

    public RoomDashboard(String designerId) {
        setTitle("FurnitureVision - Room Designer");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize furniture catalog
        furnitureCatalog = new HashMap<>();
        initializeFurnitureCatalog();

        // Create main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create and add panels
        mainPanel.add(createTopPanel(designerId), BorderLayout.NORTH);
        mainPanel.add(createControlPanel(), BorderLayout.WEST);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createFurniturePanel(), BorderLayout.EAST);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel);
        setupEventListeners();
    }

    private void initializeFurnitureCatalog() {
        furnitureCatalog.put(FurnitureCategory.CHAIRS, Arrays.asList(
            new FurnitureItem("Standard Chair", FurnitureCategory.CHAIRS, new Dimension(50, 50), "Basic chair suitable for dining or desk"),
            new FurnitureItem("Office Chair", FurnitureCategory.CHAIRS, new Dimension(60, 60), "Ergonomic office chair with adjustable height")
        ));
        
        furnitureCatalog.put(FurnitureCategory.TABLES, Arrays.asList(
            new FurnitureItem("Dining Table", FurnitureCategory.TABLES, new Dimension(150, 90), "Standard dining table for 6 people"),
            new FurnitureItem("Side Table", FurnitureCategory.TABLES, new Dimension(45, 45), "Small side table for living room"),
            new FurnitureItem("Round Table", FurnitureCategory.TABLES, new Dimension(120, 120), "Circular dining or conference table"),
            new FurnitureItem("Square Table", FurnitureCategory.TABLES, new Dimension(90, 90), "Square multi-purpose table"),
            new FurnitureItem("Office Table", FurnitureCategory.TABLES, new Dimension(120, 60), "Work desk with computer space"),
            new FurnitureItem("Corner Table", FurnitureCategory.TABLES, new Dimension(60, 60), "Corner table for living room")
        ));
        
        furnitureCatalog.put(FurnitureCategory.SOFAS, Arrays.asList(
            new FurnitureItem("Single Sofa", FurnitureCategory.SOFAS, new Dimension(90, 85), "Individual armchair"),
            new FurnitureItem("2-Seater Sofa", FurnitureCategory.SOFAS, new Dimension(150, 85), "Love seat for two people"),
            new FurnitureItem("3-Seater Sofa", FurnitureCategory.SOFAS, new Dimension(200, 85), "Full-size sofa for three people")
        ));
        
        furnitureCatalog.put(FurnitureCategory.BEDS, Arrays.asList(
            new FurnitureItem("Single Bed", FurnitureCategory.BEDS, new Dimension(90, 190), "Standard single bed"),
            new FurnitureItem("Double Bed", FurnitureCategory.BEDS, new Dimension(135, 190), "Double bed for two people"),
            new FurnitureItem("Queen Bed", FurnitureCategory.BEDS, new Dimension(150, 200), "Queen size bed"),
            new FurnitureItem("King Bed", FurnitureCategory.BEDS, new Dimension(180, 200), "King size bed")
        ));
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        // Create view toggle button
        viewToggleButton = new JToggleButton("Switch to 3D View");
        viewToggleButton.addActionListener(e -> toggleView());
        
        // Create room preview
        roomPreviewPanel = new RoomPreviewPanel();
        
        centerPanel.add(viewToggleButton, BorderLayout.NORTH);
        centerPanel.add(roomPreviewPanel, BorderLayout.CENTER);
        
        return centerPanel;
    }

    private void toggleView() {
        is3DView = !is3DView;
        viewToggleButton.setText(is3DView ? "Switch to 2D View" : "Switch to 3D View");
        roomPreviewPanel.repaint();
    }

    // Inner class for room preview with enhanced drawing
    private class RoomPreviewPanel extends JPanel {
        private double scale = 30.0; // pixels per meter
        private double zoomFactor = 1.0;
        private Point panOffset = new Point(0, 0);
        private Point lastPanPoint;
        private boolean isPanning = false;
        private FurnitureItem selectedPlacedFurniture;
        private Point dragStart;
        private boolean isResizing;
        private boolean isRotating;
        private static final int RESIZE_HANDLE_SIZE = 8;
        private static final int ROTATE_HANDLE_SIZE = 8;
        private static final int GRID_SIZE = 20; // pixels
        private boolean snapToGrid = true;
        private JPanel infoOverlay;

        public RoomPreviewPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setupInfoOverlay();

            // Add mouse listeners for furniture manipulation
            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow(); // For keyboard shortcuts
                    Point p = transformPoint(e.getPoint());

                    // Middle mouse button for panning
                    if (e.getButton() == MouseEvent.BUTTON2) {
                        isPanning = true;
                        lastPanPoint = e.getPoint();
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        return;
                    }

                    selectedPlacedFurniture = null;
                    isResizing = false;
                    isRotating = false;

                    // Check if right-click for context menu
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        for (FurnitureItem item : placedFurniture) {
                            if (getFurnitureBounds(item).contains(p)) {
                                selectedPlacedFurniture = item;
                                showContextMenu(e.getPoint());
                                updateInfoOverlay();
                                return;
                            }
                        }
                        return;
                    }

                    // Check handles and furniture selection
                    for (FurnitureItem item : placedFurniture) {
                        Rectangle bounds = getFurnitureBounds(item);
                        
                        // Check rotate handle
                        Rectangle rotateHandle = getRotateHandle(bounds);
                        if (rotateHandle.contains(p)) {
                            selectedPlacedFurniture = item;
                            isRotating = true;
                            dragStart = p;
                            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                            updateInfoOverlay();
                            return;
                        }

                        // Check resize handle
                        Rectangle resizeHandle = getResizeHandle(bounds);
                        if (resizeHandle.contains(p)) {
                            selectedPlacedFurniture = item;
                            isResizing = true;
                            dragStart = p;
                            setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                            updateInfoOverlay();
                            return;
                        }

                        // Check furniture body
                        if (bounds.contains(p)) {
                            selectedPlacedFurniture = item;
                            dragStart = p;
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            updateInfoOverlay();
                            return;
                        }
                    }
                    
                    updateInfoOverlay();
                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    Point p = transformPoint(e.getPoint());

                    if (isPanning) {
                        Point current = e.getPoint();
                        panOffset.x += current.x - lastPanPoint.x;
                        panOffset.y += current.y - lastPanPoint.y;
                        lastPanPoint = current;
                        repaint();
                        return;
                    }

                    if (selectedPlacedFurniture != null && dragStart != null) {
                        Point current = p;
                        int dx = current.x - dragStart.x;
                        int dy = current.y - dragStart.y;

                        if (isRotating) {
                            Point center = getFurnitureCenter(selectedPlacedFurniture);
                            double angle = Math.atan2(current.y - center.y, current.x - center.x)
                                       - Math.atan2(dragStart.y - center.y, dragStart.x - center.x);
                            angle = Math.toDegrees(angle);
                            
                            if (e.isShiftDown()) {
                                angle = Math.round(angle / 45.0) * 45.0;
                            }
                            
                            selectedPlacedFurniture.setRotation(
                                (selectedPlacedFurniture.getRotation() + angle) % 360
                            );
                        } else if (isResizing) {
                            Dimension currentSize = selectedPlacedFurniture.getCurrentSize();
                            int newWidth = currentSize.width + (int)(dx / (scale * zoomFactor) * 100);
                            int newHeight = currentSize.height + (int)(dy / (scale * zoomFactor) * 100);
                            
                            newWidth = Math.max(30, newWidth);
                            newHeight = Math.max(30, newHeight);
                            
                            if (snapToGrid) {
                                newWidth = Math.round(newWidth / GRID_SIZE) * GRID_SIZE;
                                newHeight = Math.round(newHeight / GRID_SIZE) * GRID_SIZE;
                            }
                            
                            selectedPlacedFurniture.setCurrentSize(new Dimension(newWidth, newHeight));
                        } else {
                            Point pos = selectedPlacedFurniture.getPosition();
                            int newX = pos.x + dx;
                            int newY = pos.y + dy;
                            
                            if (snapToGrid) {
                                newX = Math.round(newX / GRID_SIZE) * GRID_SIZE;
                                newY = Math.round(newY / GRID_SIZE) * GRID_SIZE;
                            }
                            
                            // Constrain to room bounds
                            Rectangle roomBounds = getRoomBounds();
                            newX = Math.max(roomBounds.x, Math.min(roomBounds.x + roomBounds.width - 
                                  (int)(selectedPlacedFurniture.getCurrentSize().width * scale * zoomFactor), newX));
                            newY = Math.max(roomBounds.y, Math.min(roomBounds.y + roomBounds.height - 
                                  (int)(selectedPlacedFurniture.getCurrentSize().height * scale * zoomFactor), newY));
                            
                            selectedPlacedFurniture.setPosition(new Point(newX, newY));
                        }
                        
                        dragStart = current;
                        updateInfoOverlay();
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isPanning) {
                        isPanning = false;
                        setCursor(Cursor.getDefaultCursor());
                    } else if (selectedPlacedFurniture != null) {
                        saveState();
                        setCursor(Cursor.getDefaultCursor());
                    }
                    dragStart = null;
                    isResizing = false;
                    isRotating = false;
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    // Zoom in/out with mouse wheel
                    if (e.isControlDown()) {
                        double oldZoom = zoomFactor;
                        zoomFactor = Math.max(0.1, Math.min(5.0, 
                            zoomFactor * (e.getWheelRotation() < 0 ? 1.1 : 0.9)));
                        
                        // Adjust pan offset to zoom toward cursor
                        if (oldZoom != zoomFactor) {
                            Point mouse = e.getPoint();
                            double zoomRatio = zoomFactor / oldZoom;
                            panOffset.x = mouse.x - (int)((mouse.x - panOffset.x) * zoomRatio);
                            panOffset.y = mouse.y - (int)((mouse.y - panOffset.y) * zoomRatio);
                            repaint();
                        }
                    }
                }
            };

            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
            addMouseWheelListener(mouseHandler);

            // Add keyboard shortcuts
            setFocusable(true);
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.isControlDown()) {
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_Z -> undo();
                            case KeyEvent.VK_Y -> redo();
                            case KeyEvent.VK_G -> toggleGrid();
                            case KeyEvent.VK_0 -> resetView();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_DELETE && selectedPlacedFurniture != null) {
                        removeFurniture(selectedPlacedFurniture);
                    } else if (selectedPlacedFurniture != null) {
                        // Arrow keys for fine movement
                        int delta = e.isShiftDown() ? GRID_SIZE : 1;
                        Point pos = selectedPlacedFurniture.getPosition();
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_LEFT -> pos.x -= delta;
                            case KeyEvent.VK_RIGHT -> pos.x += delta;
                            case KeyEvent.VK_UP -> pos.y -= delta;
                            case KeyEvent.VK_DOWN -> pos.y += delta;
                            case KeyEvent.VK_R -> {
                                // Rotate 90 degrees
                                selectedPlacedFurniture.setRotation(
                                    (selectedPlacedFurniture.getRotation() + 90) % 360
                                );
                            }
                        }
                        selectedPlacedFurniture.setPosition(pos);
                        updateInfoOverlay();
                        repaint();
                    }
                }
            });

            // Add drag and drop support
            setTransferHandler(new TransferHandler() {
                @Override
                public boolean canImport(TransferSupport support) {
                    return support.isDataFlavorSupported(DataFlavor.stringFlavor);
                }

                @Override
                public boolean importData(TransferSupport support) {
                    if (!canImport(support)) return false;
                    
                    try {
                        String furnitureName = (String)support.getTransferable()
                            .getTransferData(DataFlavor.stringFlavor);
                        
                        FurnitureItem newItem = null;
                        for (java.util.List<FurnitureItem> items : furnitureCatalog.values()) {
                            for (FurnitureItem item : items) {
                                if (item.getName().equals(furnitureName)) {
                                    newItem = new FurnitureItem(
                                        item.getName(),
                                        item.getCategory(),
                                        item.getDefaultSize(),
                                        item.getTooltip()
                                    );
                                    break;
                                }
                            }
                            if (newItem != null) break;
                        }
                        
                        if (newItem != null) {
                            Point dropPoint = transformPoint(support.getDropLocation().getDropPoint());
                            if (snapToGrid) {
                                dropPoint.x = Math.round(dropPoint.x / GRID_SIZE) * GRID_SIZE;
                                dropPoint.y = Math.round(dropPoint.y / GRID_SIZE) * GRID_SIZE;
                            }
                            newItem.setPosition(dropPoint);
                            placedFurniture.add(newItem);
                            selectedPlacedFurniture = newItem;
                            saveState();
                            updateInfoOverlay();
                            repaint();
                            return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }

        private void setupInfoOverlay() {
            infoOverlay = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    if (selectedPlacedFurniture != null) {
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                           RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        Point pos = selectedPlacedFurniture.getPosition();
                        String info = String.format("Position: (%d, %d) | Rotation: %.1f° | Size: %dx%d",
                            pos.x, pos.y,
                            selectedPlacedFurniture.getRotation(),
                            selectedPlacedFurniture.getCurrentSize().width,
                            selectedPlacedFurniture.getCurrentSize().height);
                        
                        g2d.setColor(new Color(0, 0, 0, 180));
                        g2d.fillRoundRect(5, 5, g2d.getFontMetrics().stringWidth(info) + 20, 25, 10, 10);
                        g2d.setColor(Color.WHITE);
                        g2d.drawString(info, 15, 22);
                    }
                }
            };
            infoOverlay.setOpaque(false);
            add(infoOverlay);
        }

        private void updateInfoOverlay() {
            if (infoOverlay != null) {
                infoOverlay.repaint();
            }
        }

        private void resetView() {
            zoomFactor = 1.0;
            panOffset = new Point(0, 0);
            repaint();
        }

        private Point transformPoint(Point screenPoint) {
            return new Point(
                (int)((screenPoint.x - panOffset.x) / zoomFactor),
                (int)((screenPoint.y - panOffset.y) / zoomFactor)
            );
        }

        private Rectangle getRoomBounds() {
            double width = (double) roomWidthSpinner.getValue();
            double length = (double) roomLengthSpinner.getValue();
            int roomWidth = (int)(width * scale * zoomFactor);
            int roomLength = (int)(length * scale * zoomFactor);
            return new Rectangle(
                panOffset.x + (getWidth() - roomWidth) / 2,
                panOffset.y + (getHeight() - roomLength) / 2,
                roomWidth, roomLength
            );
        }

        private void showContextMenu(Point p) {
            JPopupMenu menu = new JPopupMenu();
            
            JMenuItem deleteItem = new JMenuItem("Delete");
            deleteItem.addActionListener(e -> removeFurniture(selectedPlacedFurniture));
            
            JMenuItem rotateItem = new JMenuItem("Rotate 90°");
            rotateItem.addActionListener(e -> {
                selectedPlacedFurniture.setRotation(
                    (selectedPlacedFurniture.getRotation() + 90) % 360
                );
                saveState();
                repaint();
            });
            
            JMenuItem colorItem = new JMenuItem("Change Color");
            colorItem.addActionListener(e -> {
                Color newColor = JColorChooser.showDialog(
                    this,
                    "Choose Furniture Color",
                    selectedPlacedFurniture.getColor()
                );
                if (newColor != null) {
                    selectedPlacedFurniture.setColor(newColor);
                    saveState();
                    repaint();
                }
            });
            
            menu.add(deleteItem);
            menu.add(rotateItem);
            menu.add(colorItem);
            menu.show(this, p.x, p.y);
        }

        private void removeFurniture(FurnitureItem item) {
            placedFurniture.remove(item);
            selectedPlacedFurniture = null;
            saveState();
            repaint();
        }

        private void toggleGrid() {
            snapToGrid = !snapToGrid;
            repaint();
        }

        private Point getFurnitureCenter(FurnitureItem item) {
            Rectangle bounds = getFurnitureBounds(item);
            return new Point(bounds.x + bounds.width/2, bounds.y + bounds.height/2);
        }

        private Rectangle getRotateHandle(Rectangle bounds) {
            return new Rectangle(
                bounds.x + bounds.width/2 - ROTATE_HANDLE_SIZE/2,
                bounds.y - ROTATE_HANDLE_SIZE - 5,
                ROTATE_HANDLE_SIZE,
                ROTATE_HANDLE_SIZE
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Apply zoom and pan transformation
            AffineTransform transform = g2d.getTransform();
            g2d.translate(panOffset.x, panOffset.y);
            g2d.scale(zoomFactor, zoomFactor);

            // Draw grid if enabled
            if (snapToGrid) {
                g2d.setColor(new Color(230, 230, 230));
                Rectangle bounds = getRoomBounds();
                for (int x = bounds.x; x < bounds.x + bounds.width; x += GRID_SIZE) {
                    g2d.drawLine(x, bounds.y, x, bounds.y + bounds.height);
                }
                for (int y = bounds.y; y < bounds.y + bounds.height; y += GRID_SIZE) {
                    g2d.drawLine(bounds.x, y, bounds.x + bounds.width, y);
                }
            }

            // Get room dimensions
            double width = (double) roomWidthSpinner.getValue();
            double length = (double) roomLengthSpinner.getValue();
            double height = (double) roomHeightSpinner.getValue();

            if (is3DView) {
                draw3DView(g2d, width, length, height);
            } else {
                draw2DView(g2d, width, length);
            }

            // Draw placed furniture
            for (FurnitureItem item : placedFurniture) {
                drawFurniture(g2d, item);
            }

            // Reset transform
            g2d.setTransform(transform);
        }

        private void drawFurniture(Graphics2D g2d, FurnitureItem item) {
            Rectangle bounds = getFurnitureBounds(item);
            
            // Create transform for rotation
            AffineTransform oldTransform = g2d.getTransform();
            AffineTransform transform = new AffineTransform();
            transform.rotate(Math.toRadians(item.getRotation()),
                           bounds.x + bounds.width/2,
                           bounds.y + bounds.height/2);
            g2d.transform(transform);
            
            // Draw furniture shape
            Shape scaledShape = createScaledShape(item.getShape(), bounds);
            g2d.setColor(item.getColor());
            g2d.fill(scaledShape);
            g2d.setColor(item == selectedPlacedFurniture ? Color.BLUE : Color.BLACK);
            g2d.setStroke(new BasicStroke(item == selectedPlacedFurniture ? 2f : 1f));
            g2d.draw(scaledShape);
            
            // Reset transform
            g2d.setTransform(oldTransform);
            
            // Draw handles if selected
            if (item == selectedPlacedFurniture) {
                // Draw resize handle
                Rectangle resizeHandle = getResizeHandle(bounds);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(resizeHandle.x, resizeHandle.y,
                            resizeHandle.width, resizeHandle.height);
                g2d.setColor(Color.BLUE);
                g2d.drawRect(resizeHandle.x, resizeHandle.y,
                            resizeHandle.width, resizeHandle.height);
                
                // Draw rotate handle
                Rectangle rotateHandle = getRotateHandle(bounds);
                g2d.setColor(Color.WHITE);
                g2d.fillOval(rotateHandle.x, rotateHandle.y,
                            rotateHandle.width, rotateHandle.height);
                g2d.setColor(Color.BLUE);
                g2d.drawOval(rotateHandle.x, rotateHandle.y,
                            rotateHandle.width, rotateHandle.height);
            }
            
            // Draw label
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString(item.getName(), bounds.x, bounds.y - 2);
        }

        private Shape createScaledShape(Shape shape, Rectangle bounds) {
            Rectangle2D shapeBounds = shape.getBounds2D();
            AffineTransform transform = new AffineTransform();
            transform.translate(bounds.x, bounds.y);
            transform.scale(bounds.width / shapeBounds.getWidth(),
                          bounds.height / shapeBounds.getHeight());
            return transform.createTransformedShape(shape);
        }

        private Rectangle getFurnitureBounds(FurnitureItem item) {
            Point pos = item.getPosition();
            Dimension size = item.getCurrentSize();
            int width = (int)(size.width * scale / 100);
            int height = (int)(size.height * scale / 100);
            return new Rectangle(pos.x, pos.y, width, height);
        }

        private Rectangle getResizeHandle(Rectangle bounds) {
            return new Rectangle(
                bounds.x + bounds.width - RESIZE_HANDLE_SIZE,
                bounds.y + bounds.height - RESIZE_HANDLE_SIZE,
                RESIZE_HANDLE_SIZE,
                RESIZE_HANDLE_SIZE
            );
        }

        private void draw2DView(Graphics2D g2d, double width, double length) {
            int padding = 50;
            int availableWidth = getWidth() - (2 * padding);
            int availableHeight = getHeight() - (2 * padding);
            
            // Calculate scale to fit the room in the available space
            double scaleX = availableWidth / width;
            double scaleY = availableHeight / length;
            scale = Math.min(scaleX, scaleY);
            
            // Calculate room dimensions in pixels
            int roomWidth = (int) (width * scale);
            int roomLength = (int) (length * scale);
            
            // Calculate starting position to center the room
            int startX = (getWidth() - roomWidth) / 2;
            int startY = (getHeight() - roomLength) / 2;
            
            // Draw room outline with border
            g2d.setColor(floorColor);
            g2d.fillRect(startX, startY, roomWidth, roomLength);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(startX, startY, roomWidth, roomLength);
            
            // Draw grid
            g2d.setColor(new Color(200, 200, 200));
            g2d.setStroke(new BasicStroke(1));
            for (int x = 0; x <= width; x++) {
                int gridX = startX + (int)(x * scale);
                g2d.drawLine(gridX, startY, gridX, startY + roomLength);
            }
            for (int y = 0; y <= length; y++) {
                int gridY = startY + (int)(y * scale);
                g2d.drawLine(startX, gridY, startX + roomWidth, gridY);
            }
            
            // Draw measurements
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String unit = measurementUnitCombo.getSelectedItem().toString();
            unit = unit.substring(0, unit.length() - 1); // Remove 's' from "Meters" or "Feet"
            
            // Width measurement with arrow
            String widthText = String.format("%.1f %s", width, unit);
            drawMeasurementArrow(g2d, 
                new Point(startX, startY - 20),
                new Point(startX + roomWidth, startY - 20),
                widthText);
            
            // Length measurement with arrow
            String lengthText = String.format("%.1f %s", length, unit);
            g2d.rotate(-Math.PI/2, startX - 20, startY);
            drawMeasurementArrow(g2d,
                new Point(startX - 20, startY),
                new Point(startX - 20, startY + roomLength),
                lengthText);
            g2d.rotate(Math.PI/2, startX - 20, startY);
        }

        private void drawMeasurementArrow(Graphics2D g2d, Point start, Point end, String text) {
            // Draw the line
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(start.x, start.y, end.x, end.y);
            
            // Draw arrow heads
            int arrowSize = 6;
            g2d.fillPolygon(
                new int[]{start.x, start.x + arrowSize, start.x + arrowSize},
                new int[]{start.y, start.y - arrowSize, start.y + arrowSize},
                3);
            g2d.fillPolygon(
                new int[]{end.x, end.x - arrowSize, end.x - arrowSize},
                new int[]{end.y, end.y - arrowSize, end.y + arrowSize},
                3);
            
            // Draw measurement text
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textX = start.x + (end.x - start.x - textWidth) / 2;
            int textY = start.y - 5;
            g2d.drawString(text, textX, textY);
        }

        private void draw3DView(Graphics2D g2d, double width, double length, double height) {
            int padding = 50;
            int availableWidth = getWidth() - (2 * padding);
            int availableHeight = getHeight() - (2 * padding);
            
            // Calculate scale to fit the room in the available space
            double scaleX = availableWidth / (width * 1.5); // Account for perspective
            double scaleY = availableHeight / (length + height);
            scale = Math.min(scaleX, scaleY);
            
            // Calculate room dimensions in pixels
            int roomWidth = (int) (width * scale);
            int roomLength = (int) (length * scale);
            int roomHeight = (int) (height * scale);
            
            // Calculate starting position
            int startX = (getWidth() - roomWidth) / 2;
            int startY = (getHeight() - roomLength - roomHeight) / 2 + roomHeight;
            
            // Create points for isometric view
            int[] floorX = {startX, startX + roomWidth, startX + roomWidth, startX};
            int[] floorY = {startY, startY, startY - roomLength/2, startY - roomLength/2};
            
            int[] leftWallX = {startX, startX, startX};
            int[] leftWallY = {startY, startY - roomHeight, startY - roomHeight - roomLength/2};
            
            int[] backWallX = {startX, startX + roomWidth, startX + roomWidth};
            int[] backWallY = {startY - roomHeight - roomLength/2, startY - roomHeight - roomLength/2, startY - roomHeight};
            
            // Draw the room with borders
            // Floor
            g2d.setColor(floorColor);
            g2d.fillPolygon(floorX, floorY, 4);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawPolygon(floorX, floorY, 4);
            
            // Left wall
            g2d.setColor(wallColor);
            int[] leftWallCompleteX = {leftWallX[0], leftWallX[1], leftWallX[2], floorX[3]};
            int[] leftWallCompleteY = {leftWallY[0], leftWallY[1], leftWallY[2], floorY[3]};
            g2d.fillPolygon(leftWallCompleteX, leftWallCompleteY, 4);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(leftWallCompleteX, leftWallCompleteY, 4);
            
            // Back wall
            g2d.setColor(wallColor.brighter());
            int[] backWallCompleteX = {backWallX[0], backWallX[1], backWallX[2], leftWallX[1]};
            int[] backWallCompleteY = {backWallY[0], backWallY[1], backWallY[2], leftWallY[1]};
            g2d.fillPolygon(backWallCompleteX, backWallCompleteY, 4);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(backWallCompleteX, backWallCompleteY, 4);
            
            // Draw measurements in 3D
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String unit = measurementUnitCombo.getSelectedItem().toString();
            unit = unit.substring(0, unit.length() - 1);
            
            // Width measurement
            String widthText = String.format("%.1f %s", width, unit);
            g2d.drawString(widthText, startX + roomWidth/2 - 20, startY + 20);
            
            // Length measurement
            String lengthText = String.format("%.1f %s", length, unit);
            g2d.drawString(lengthText, startX - 60, startY - roomLength/4);
            
            // Height measurement
            String heightText = String.format("%.1f %s", height, unit);
            g2d.drawString(heightText, startX - 60, startY - roomHeight/2);
            
            // Draw grid lines
            g2d.setColor(new Color(200, 200, 200));
            g2d.setStroke(new BasicStroke(1));
            // Floor grid
            for (int x = 0; x <= width; x++) {
                int gridX = startX + (int)(x * scale);
                g2d.drawLine(gridX, startY, gridX, startY - roomLength/2);
            }
            for (int y = 0; y <= length; y++) {
                int gridY = startY - (int)(y * scale/2);
                g2d.drawLine(startX, gridY, startX + roomWidth, gridY);
            }
            
            // Wall grid
            for (int y = 0; y <= height; y++) {
                int gridY = startY - (int)(y * scale);
                g2d.drawLine(startX, gridY, startX, gridY - roomLength/2);
                g2d.drawLine(startX + roomWidth, gridY, startX + roomWidth, gridY - roomLength/2);
            }
        }
    }

    private void handleFurniturePlacement(Point point) {
        String selectedFurniture = (String) furnitureTypeCombo.getSelectedItem();
        if (selectedFurniture != null) {
            furnitureListModel.addElement(selectedFurniture + " at (" + point.x + ", " + point.y + ")");
            roomPreviewPanel.repaint();
        }
    }

    private void setupEventListeners() {
        // Room dimension spinners
        roomWidthSpinner.addChangeListener(e -> updateRoomDimensions());
        roomLengthSpinner.addChangeListener(e -> updateRoomDimensions());
        roomHeightSpinner.addChangeListener(e -> updateRoomDimensions());
        
        // Measurement unit combo
        measurementUnitCombo.addActionListener(e -> convertMeasurements());
        
        // Color preset combo
        colorPresetCombo.addActionListener(e -> applyColorPreset());
    }

    private void updateRoomDimensions() {
        roomWidth = (Double) roomWidthSpinner.getValue();
        roomLength = (Double) roomLengthSpinner.getValue();
        roomHeight = (Double) roomHeightSpinner.getValue();
        roomPreviewPanel.repaint();
    }

    private void convertMeasurements() {
        String newUnit = (String) measurementUnitCombo.getSelectedItem();
        if (!newUnit.equals(currentUnit)) {
            double factor = newUnit.equals("Meters") ? 0.3048 : 3.28084;
            roomWidth *= factor;
            roomLength *= factor;
            roomHeight *= factor;
            
            // Update spinners without triggering events
            roomWidthSpinner.setValue(roomWidth);
            roomLengthSpinner.setValue(roomLength);
            roomHeightSpinner.setValue(roomHeight);
            
            currentUnit = newUnit;
            roomPreviewPanel.repaint();
        }
    }

    private void applyColorPreset() {
        String preset = (String) colorPresetCombo.getSelectedItem();
        switch (preset) {
            case "Warm Tones":
                wallColor = WARM_COLORS.get("Coral");
                floorColor = WARM_COLORS.get("Orange");
                ceilingColor = WARM_COLORS.get("Rust");
                break;
            case "Cool Tones":
                wallColor = COOL_COLORS.get("Ocean Blue");
                floorColor = COOL_COLORS.get("Turquoise");
                ceilingColor = COOL_COLORS.get("Sea Green");
                break;
            case "Neutral":
                wallColor = NEUTRAL_COLORS.get("Light Gray");
                floorColor = NEUTRAL_COLORS.get("Gray");
                ceilingColor = NEUTRAL_COLORS.get("White");
                break;
            case "Pastels":
                wallColor = PASTEL_COLORS.get("Pink");
                floorColor = PASTEL_COLORS.get("Blue");
                ceilingColor = PASTEL_COLORS.get("Purple");
                break;
        }
        colorPreviewPanel.repaint();
        roomPreviewPanel.repaint();
    }

    private JPanel createTopPanel(String designerId) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Title
        JLabel titleLabel = new JLabel("Room Designer");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 51, 51));
        panel.add(titleLabel, BorderLayout.WEST);

        // Designer info
        JLabel designerLabel = new JLabel("Designer: " + designerId);
        designerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(designerLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(280, 0));

        // Room Dimensions Section
        JLabel dimensionsLabel = new JLabel("Room Dimensions");
        dimensionsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dimensionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(dimensionsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Measurement unit selector
        JPanel unitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        unitPanel.setBackground(panel.getBackground());
        JLabel unitLabel = new JLabel("Unit:");
        measurementUnitCombo = new JComboBox<>(new String[]{"Meters", "Feet"});
        unitPanel.add(unitLabel);
        unitPanel.add(measurementUnitCombo);
        unitPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(unitPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Dimension inputs
        String[] labels = {"Width:", "Length:", "Height:"};
        JSpinner[] spinners = new JSpinner[3];
        double[] defaultValues = {4.0, 5.0, 2.8};
        double[] minValues = {2.0, 2.0, 2.0};
        double[] maxValues = {15.0, 20.0, 4.0};

        for (int i = 0; i < labels.length; i++) {
            JPanel dimensionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            dimensionPanel.setBackground(panel.getBackground());
            JLabel label = new JLabel(labels[i]);
            label.setPreferredSize(new Dimension(50, 20));
            
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(
                defaultValues[i], minValues[i], maxValues[i], 0.1));
            spinner.setPreferredSize(new Dimension(70, 25));
            
            dimensionPanel.add(label);
            dimensionPanel.add(spinner);
            dimensionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(dimensionPanel);
            
            spinners[i] = spinner;
        }
        roomWidthSpinner = spinners[0];
        roomLengthSpinner = spinners[1];
        roomHeightSpinner = spinners[2];

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Color Scheme Section
        JLabel colorLabel = new JLabel("Room Colors");
        colorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        colorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(colorLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Color presets
        JPanel presetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        presetPanel.setBackground(panel.getBackground());
        JLabel presetLabel = new JLabel("Preset:");
        colorPresetCombo = new JComboBox<>(new String[]{
            "Warm Tones", "Cool Tones", "Neutral", "Pastels"
        });
        presetPanel.add(presetLabel);
        presetPanel.add(colorPresetCombo);
        presetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(presetPanel);

        // Color preview
        colorPreviewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int width = getWidth();
                int height = getHeight();
                
                // Draw wall color
                g.setColor(wallColor);
                g.fillRect(0, 0, width, height/2);
                
                // Draw floor color
                g.setColor(floorColor);
                g.fillRect(0, height/2, width, height/2);
                
                // Draw labels
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.PLAIN, 10));
                g.drawString("Wall", 5, 15);
                g.drawString("Floor", 5, height/2 + 15);
            }
        };
        colorPreviewPanel.setPreferredSize(new Dimension(0, 60));
        colorPreviewPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        colorPreviewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(colorPreviewPanel);

        // Individual color pickers
        String[] colorLabels = {"Wall Color:", "Floor Color:", "Ceiling Color:"};
        for (String label : colorLabels) {
            JPanel colorPickerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            colorPickerPanel.setBackground(panel.getBackground());
            JLabel pickerLabel = new JLabel(label);
            JButton colorButton = new JButton("Pick");
            colorButton.addActionListener(e -> showColorPicker(label));
            colorPickerPanel.add(pickerLabel);
            colorPickerPanel.add(colorButton);
            colorPickerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
            panel.add(colorPickerPanel);
        }

        return panel;
    }

    private void showColorPicker(String target) {
        Color initialColor = switch (target) {
            case "Wall Color:" -> wallColor;
            case "Floor Color:" -> floorColor;
            case "Ceiling Color:" -> ceilingColor;
            default -> Color.WHITE;
        };

        Color newColor = JColorChooser.showDialog(
            this,
            "Choose " + target.toLowerCase(),
            initialColor
        );

        if (newColor != null) {
            switch (target) {
                case "Wall Color:" -> wallColor = newColor;
                case "Floor Color:" -> floorColor = newColor;
                case "Ceiling Color:" -> ceilingColor = newColor;
            }
            colorPreviewPanel.repaint();
            roomPreviewPanel.repaint();
        }
    }

    private JPanel createFurniturePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Create title
        JLabel titleLabel = new JLabel("Furniture Catalog");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Create split pane for catalog and preview
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setBackground(panel.getBackground());

        // Create furniture catalog tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Furniture");
        for (FurnitureCategory category : FurnitureCategory.values()) {
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
            for (FurnitureItem item : furnitureCatalog.get(category)) {
                categoryNode.add(new DefaultMutableTreeNode(item));
            }
            root.add(categoryNode);
        }

        furnitureCatalogTree = new JTree(root);
        furnitureCatalogTree.setRootVisible(false);
        furnitureCatalogTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        furnitureCatalogTree.setCellRenderer(new FurnitureCellRenderer());
        furnitureCatalogTree.addTreeSelectionListener(e -> updateFurniturePreview());

        JScrollPane treeScroll = new JScrollPane(furnitureCatalogTree);
        treeScroll.setPreferredSize(new Dimension(250, 0));
        splitPane.setTopComponent(treeScroll);

        // Create preview panel
        furniturePreviewPanel = new JPanel(new BorderLayout(10, 10));
        furniturePreviewPanel.setBackground(Color.WHITE);
        furniturePreviewPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        // Add color chooser button
        JButton colorButton = new JButton("Change Color");
        colorButton.addActionListener(e -> showFurnitureColorChooser());
        
        JPanel previewControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        previewControls.setBackground(furniturePreviewPanel.getBackground());
        previewControls.add(colorButton);
        
        furniturePreviewPanel.add(previewControls, BorderLayout.SOUTH);
        splitPane.setBottomComponent(furniturePreviewPanel);

        panel.add(splitPane, BorderLayout.CENTER);
        
        // Setup drag and drop
        furnitureCatalogTree.setDragEnabled(true);
        furnitureCatalogTree.setTransferHandler(new FurnitureTransferHandler());
        
        return panel;
    }

    // Custom cell renderer for furniture tree
    private class FurnitureCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            
            if (userObject instanceof FurnitureItem) {
                FurnitureItem item = (FurnitureItem) userObject;
                setText(item.getName());
                setToolTipText(item.getTooltip());
            }
            
            return this;
        }
    }

    private void updateFurniturePreview() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
            furnitureCatalogTree.getLastSelectedPathComponent();
            
        if (node == null) return;
        
        Object nodeInfo = node.getUserObject();
        if (nodeInfo instanceof FurnitureItem) {
            selectedFurniture = (FurnitureItem) nodeInfo;
            furniturePreviewPanel.removeAll();
            
            // Add preview component
            JPanel previewComponent = new JPanel() {
                private final double PREVIEW_SCALE = 0.3; // 30% of original size
                
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                       RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Draw furniture preview
                    int width = (int)(selectedFurniture.getDefaultSize().width * PREVIEW_SCALE);
                    int height = (int)(selectedFurniture.getDefaultSize().height * PREVIEW_SCALE);
                    
                    g2d.setColor(selectedFurniture.getColor());
                    g2d.fillRect(10, 10, width, height);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(10, 10, width, height);
                    
                    // Draw dimensions
                    String dimensions = String.format("%dcm × %dcm", 
                        selectedFurniture.getDefaultSize().width,
                        selectedFurniture.getDefaultSize().height);
                    g2d.drawString(dimensions, 10, height + 25);
                }
            };
            previewComponent.setPreferredSize(new Dimension(200, 200));
            previewComponent.setBackground(Color.WHITE);
            
            // Add name and description
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(Color.WHITE);
            
            JLabel nameLabel = new JLabel(selectedFurniture.getName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            JLabel descLabel = new JLabel(selectedFurniture.getTooltip());
            descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            
            infoPanel.add(nameLabel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(descLabel);
            
            furniturePreviewPanel.add(previewComponent, BorderLayout.CENTER);
            furniturePreviewPanel.add(infoPanel, BorderLayout.NORTH);
            furniturePreviewPanel.revalidate();
            furniturePreviewPanel.repaint();
        }
    }

    private void showFurnitureColorChooser() {
        if (selectedFurniture == null) return;
        
        if (furnitureColorChooser == null) {
            furnitureColorChooser = new JColorChooser(selectedFurniture.getColor());
        }
        
        Color newColor = JColorChooser.showDialog(
            this,
            "Choose Furniture Color",
            selectedFurniture.getColor()
        );
        
        if (newColor != null) {
            selectedFurniture.setColor(newColor);
            furniturePreviewPanel.repaint();
        }
    }

    // Drag and Drop support
    private class FurnitureTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            if (selectedFurniture != null) {
                return new StringSelection(selectedFurniture.getName());
            }
            return null;
        }
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton threeDViewButton = new JButton("View in 3D");
        styleButton(threeDViewButton, false);
        threeDViewButton.addActionListener(e -> showThreeDView());

        JButton saveButton = new JButton("Save Design");
        styleButton(saveButton, true);

        panel.add(threeDViewButton);
        panel.add(saveButton);

        return panel;
    }

    private void styleButton(JButton button, boolean isPrimary) {
        button.setPreferredSize(new Dimension(120, 30));
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        if (isPrimary) {
            button.setBackground(new Color(181, 101, 29));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(200, 200, 200));
            button.setForeground(new Color(51, 51, 51));
        }

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(isPrimary ? new Color(181, 101, 29).darker() : new Color(180, 180, 180));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(isPrimary ? new Color(181, 101, 29) : new Color(200, 200, 200));
            }
        });
    }

    private void saveState() {
        List<FurnitureItem> currentState = new ArrayList<>();
        for (FurnitureItem item : placedFurniture) {
            currentState.add(item.clone());
        }
        undoStack.push(currentState);
        redoStack.clear();
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            List<FurnitureItem> currentState = new ArrayList<>();
            for (FurnitureItem item : placedFurniture) {
                currentState.add(item.clone());
            }
            redoStack.push(currentState);
            
            placedFurniture.clear();
            placedFurniture.addAll(undoStack.pop());
            roomPreviewPanel.repaint();
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            List<FurnitureItem> currentState = new ArrayList<>();
            for (FurnitureItem item : placedFurniture) {
                currentState.add(item.clone());
            }
            undoStack.push(currentState);
            
            placedFurniture.clear();
            placedFurniture.addAll(redoStack.pop());
            roomPreviewPanel.repaint();
        }
    }

    private void showThreeDView() {
        if (threeDViewDialog == null) {
            threeDViewDialog = new JDialog(this, "3D View", false);
            threeDViewDialog.setSize(800, 600);
            threeDViewDialog.setLocationRelativeTo(this);
            
            // Initialize JavaFX toolkit
            Platform.setImplicitExit(false);
            
            threeDViewExporter = new ThreeDViewExporter();
            threeDViewDialog.add(threeDViewExporter);
            
            threeDViewDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    threeDViewDialog.dispose();
                    threeDViewDialog = null;
                }
            });
        }
        
        threeDViewDialog.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            RoomDashboard dashboard = new RoomDashboard("DEMO");
            dashboard.setVisible(true);
        });
    }
} 