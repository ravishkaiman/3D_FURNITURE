import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton resetButton;
    private Color primaryColor = new Color(51, 51, 51);
    private Color accentColor = new Color(181, 101, 29);
    private Color backgroundColor = new Color(245, 245, 245);

    public LoginPage() {
        setTitle("FurnitureVision - Designer Login");
        setSize(600, 400);  // Reduced window size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create the main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Left branding panel
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(accentColor);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));  // Reduced padding

        JLabel companyName = new JLabel("FurnitureVision");
        companyName.setFont(new Font("Arial", Font.BOLD, 28));  // Reduced font size
        companyName.setForeground(Color.WHITE);
        companyName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("<html><center>Transform Spaces<br>Design Dreams</center></html>");
        tagline.setFont(new Font("Arial", Font.PLAIN, 18));  // Reduced font size
        tagline.setForeground(Color.WHITE);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel features = new JLabel("<html><center><br>• 3D Room Visualization<br>• Custom Furniture Design<br>• Color Scheme Planning<br>• Space Optimization</center></html>");
        features.setFont(new Font("Arial", Font.PLAIN, 14));  // Reduced font size
        features.setForeground(Color.WHITE);
        features.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandPanel.add(Box.createVerticalGlue());
        brandPanel.add(companyName);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 15)));  // Reduced spacing
        brandPanel.add(tagline);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 25)));  // Reduced spacing
        brandPanel.add(features);
        brandPanel.add(Box.createVerticalGlue());

        // Right login panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBackground(backgroundColor);
        loginPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));  // Reduced padding

        // Login form components
        JLabel titleLabel = new JLabel("Designer Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));  // Reduced font size
        titleLabel.setForeground(primaryColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        JPanel usernamePanel = new JPanel(new BorderLayout(0, 5));
        usernamePanel.setOpaque(false);
        JLabel usernameLabel = new JLabel("Designer ID");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        usernameField = new JTextField(15);  // Reduced column count
        usernameField.setFont(new Font("Arial", Font.PLAIN, 12));
        usernameField.setPreferredSize(new Dimension(200, 25));  // Reduced size
        usernamePanel.add(usernameLabel, BorderLayout.NORTH);
        usernamePanel.add(usernameField, BorderLayout.CENTER);

        // Password field
        JPanel passwordPanel = new JPanel(new BorderLayout(0, 5));
        passwordPanel.setOpaque(false);
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 12));
        passwordField = new JPasswordField(15);  // Reduced column count
        passwordField.setFont(new Font("Arial", Font.PLAIN, 12));
        passwordField.setPreferredSize(new Dimension(200, 25));  // Reduced size
        passwordPanel.add(passwordLabel, BorderLayout.NORTH);
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));  // Reduced spacing
        buttonPanel.setOpaque(false);

        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(90, 30));  // Reduced size
        loginButton.setBackground(accentColor);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 12));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);

        resetButton = new JButton("Reset");
        resetButton.setPreferredSize(new Dimension(90, 30));  // Reduced size
        resetButton.setBackground(new Color(200, 200, 200));
        resetButton.setForeground(primaryColor);
        resetButton.setFont(new Font("Arial", Font.BOLD, 12));
        resetButton.setFocusPainted(false);
        resetButton.setBorderPainted(false);

        buttonPanel.add(loginButton);
        buttonPanel.add(resetButton);

        // Add components to login panel
        loginPanel.add(titleLabel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 25)));  // Reduced spacing
        loginPanel.add(usernamePanel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 15)));  // Reduced spacing
        loginPanel.add(passwordPanel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 20)));  // Reduced spacing
        loginPanel.add(buttonPanel);

        // Add panels to main panel
        mainPanel.add(brandPanel, BorderLayout.WEST);
        mainPanel.add(loginPanel, BorderLayout.CENTER);

        // Add action listeners
        loginButton.addActionListener(e -> handleLogin());
        resetButton.addActionListener(e -> handleReset());

        // Add key listener for Enter key
        ActionListener loginAction = e -> handleLogin();
        usernameField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);

        // Add the main panel to the frame
        add(mainPanel);
        
        // Set initial focus
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both Designer ID and password", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // For demonstration, using simple validation
        if (username.equals("designer") && password.equals("password")) {
            // Hide login window
            setVisible(false);
            dispose();

            // Open dashboard
            SwingUtilities.invokeLater(() -> {
                RoomDashboard dashboard = new RoomDashboard(username);
                dashboard.setVisible(true);
            });
        } else {
            showMessage("Invalid Designer ID or password", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private void handleReset() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocusInWindow();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage();
            loginPage.setVisible(true);
        });
    }
} 