import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;

public class PhotoManager extends JFrame {
    private JLabel imageLabel;
    private File currentFile;
    private ArrayList<File> imageHistory = new ArrayList<>();
    private int currentImageIndex = -1;
    private JButton prevBtn, nextBtn;

    public PhotoManager() {
        setTitle("Photo Manager");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(400, 300));

        // Modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize components
        initComponents();
    }

    private void initComponents() {
        // Main panel with card layout for better organization
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton uploadBtn = createStyledButton("Upload");
        JButton deleteBtn = createStyledButton("Delete");
        JButton replaceBtn = createStyledButton("Replace");
        prevBtn = createStyledButton("Previous");
        nextBtn = createStyledButton("Next");

        buttonPanel.add(prevBtn);
        buttonPanel.add(uploadBtn);
        buttonPanel.add(replaceBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(nextBtn);

        // Image display area
        imageLabel = new JLabel("No Image Selected", SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.WHITE);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Scroll pane for large images
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Action Listeners
        uploadBtn.addActionListener(e -> uploadImage());
        deleteBtn.addActionListener(e -> deleteImage());
        replaceBtn.addActionListener(e -> replaceImage());
        prevBtn.addActionListener(e -> showPreviousImage());
        nextBtn.addActionListener(e -> showNextImage());

        // Layout
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // Initial button states
        updateNavigationButtons();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(new Color(66, 135, 245));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(86, 155, 255));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(66, 135, 245));
            }
        });
        return button;
    }

    private void uploadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image files", ImageIO.getReaderFileSuffixes()));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (isValidImageFile(selectedFile)) {
                currentFile = selectedFile;
                imageHistory.add(currentFile);
                currentImageIndex = imageHistory.size() - 1;
                setImage(currentFile);
                updateNavigationButtons();
            } else {
                showError("Please select a valid image file (JPEG, PNG, GIF).");
            }
        }
    }

    private void deleteImage() {
        if (currentFile != null) {
            imageHistory.remove(currentImageIndex);
            currentImageIndex = Math.min(currentImageIndex, imageHistory.size() - 1);
            if (imageHistory.isEmpty()) {
                currentFile = null;
                imageLabel.setIcon(null);
                imageLabel.setText("No Image Selected");
            } else {
                currentFile = imageHistory.get(currentImageIndex);
                setImage(currentFile);
            }
            updateNavigationButtons();
        } else {
            showError("No image to delete.");
        }
    }

    private void replaceImage() {
        if (currentFile != null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", ImageIO.getReaderFileSuffixes()));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (isValidImageFile(selectedFile)) {
                    currentFile = selectedFile;
                    imageHistory.set(currentImageIndex, currentFile);
                    setImage(currentFile);
                } else {
                    showError("Please select a valid image file (JPEG, PNG, GIF).");
                }
            }
        } else {
            showError("Upload an image first to replace it.");
        }
    }

    private void showPreviousImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--;
            currentFile = imageHistory.get(currentImageIndex);
            setImage(currentFile);
            updateNavigationButtons();
        }
    }

    private void showNextImage() {
        if (currentImageIndex < imageHistory.size() - 1) {
            currentImageIndex++;
            currentFile = imageHistory.get(currentImageIndex);
            setImage(currentFile);
            updateNavigationButtons();
        }
    }

    private void setImage(File file) {
        try {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            // Maintain aspect ratio
            Image image = icon.getImage();
            Dimension imageDim = new Dimension(image.getWidth(null), image.getHeight(null));
            Dimension labelDim = new Dimension(imageLabel.getWidth(), imageLabel.getHeight());
            
            double scale = Math.min(
                labelDim.getWidth() / imageDim.getWidth(),
                labelDim.getHeight() / imageDim.getHeight()
            );
            
            int scaledWidth = (int) (imageDim.getWidth() * scale);
            int scaledHeight = (int) (imageDim.getHeight() * scale);
            
            Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            imageLabel.setText("");
            setTitle("Photo Manager - " + file.getName());
        } catch (Exception e) {
            showError("Error loading image: " + e.getMessage());
            imageLabel.setIcon(null);
            imageLabel.setText("Error Loading Image");
        }
    }

    private boolean isValidImageFile(File file) {
        try {
            String name = file.getName().toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
                   name.endsWith(".png") || name.endsWith(".gif");
        } catch (Exception e) {
            return false;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void updateNavigationButtons() {
        prevBtn.setEnabled(currentImageIndex > 0);
        nextBtn.setEnabled(currentImageIndex < imageHistory.size() - 1);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PhotoManager().setVisible(true));
    }
}