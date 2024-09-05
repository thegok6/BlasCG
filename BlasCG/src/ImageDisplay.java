import javax.swing.*;
import java.awt.image.BufferedImage;

public class ImageDisplay {

    public static void windows(BufferedImage receivedImage) {
        JFrame frame = new JFrame("Received Image");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        JLabel imageLabel = new JLabel(new ImageIcon(receivedImage));

        
        frame.getContentPane().add(imageLabel);

        
        frame.setSize(90, 90);

        
        frame.setLocationRelativeTo(null);

        
        frame.setVisible(true);
    }
}
