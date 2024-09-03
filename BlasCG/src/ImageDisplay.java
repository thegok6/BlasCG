import javax.swing.*;
import java.awt.image.BufferedImage;

public class ImageDisplay {

    public static void windows(BufferedImage receivedImage) {
        JFrame frame = new JFrame("Received Image");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a JLabel to hold the image
        JLabel imageLabel = new JLabel(new ImageIcon(receivedImage));

        // Add the label to the frame
        frame.getContentPane().add(imageLabel);

        // Set the frame size to 200x200
        frame.setSize(90, 90);

        // Make sure the image is centered and fits within the frame
        frame.setLocationRelativeTo(null);

        // Display the frame
        frame.setVisible(true);
    }
}
/*    public static void windows(BufferedImage receivedImage) {
// Scale the image to 200x200
Image scaledImage = receivedImage.getScaledInstance(90, 90, Image.SCALE_SMOOTH);

// Create a new BufferedImage to hold the scaled image
BufferedImage resizedImage = new BufferedImage(90, 90, BufferedImage.TYPE_INT_ARGB);
Graphics2D g2d = resizedImage.createGraphics();
g2d.drawImage(scaledImage, 0, 0, null);
g2d.dispose();

// Create a JFrame to display the image
JFrame frame = new JFrame("Received Image");
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

// Create a JLabel to hold the scaled image
JLabel imageLabel = new JLabel(new ImageIcon(resizedImage));

// Add the label to the frame
frame.getContentPane().add(imageLabel);

// Set the frame size to 200x200
frame.setSize(90, 90);

// Make sure the image is centered and fits within the frame
frame.setLocationRelativeTo(null);

// Display the frame
frame.setVisible(true);
}*/