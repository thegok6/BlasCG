import org.jblas.DoubleMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageGenerator {

    public static BufferedImage gerarImagem(DoubleMatrix f, String outputPath) {
    	BufferedImage i = criarImagem(f);
        try {
            ImageIO.write(i, "png", new File(outputPath));  
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i;
    }
    
    public static BufferedImage criarImagem(DoubleMatrix f) {
        int size = (int) Math.sqrt(f.length);
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);

        
        double brilho = 2.5; 

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int index = i * size + j;
                double value = f.get(index) * brilho;  
                int pixelValue = (int) (value * 200);
                pixelValue = Math.min(255, Math.max(0, pixelValue));  
                image.setRGB(j, i, new Color(pixelValue, pixelValue, pixelValue).getRGB());
            }
        }

        
        BufferedImage rotatedImage = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                rotatedImage.setRGB(size - 1 - j, i, image.getRGB(i, j));
            }
        }

        return rotatedImage;
    }
    
    
    
    
    
    public static void CriarImagem(DoubleMatrix F, String filePath) {
        int totalElements = F.length;
        int widthHeight = (int) Math.sqrt(totalElements); 

        
        if (widthHeight * widthHeight != totalElements) {
            throw new IllegalArgumentException("O número de elementos na matriz F não permite uma imagem quadrada.");
        }

        
        BufferedImage image = new BufferedImage(widthHeight, widthHeight, BufferedImage.TYPE_INT_RGB);

        
        double minVal = F.min();
        double maxVal = F.max();

        
        for (int i = 0; i < widthHeight; i++) {
            for (int j = 0; j < widthHeight; j++) {
                double value = F.get(i * widthHeight + j);

                
                int grayValue = (int) ((value - minVal) / (maxVal - minVal) * 255);

                
                Color color = new Color(grayValue, grayValue, grayValue);
                image.setRGB(j, i, color.getRGB());
            }
        }

        
        try {
            ImageIO.write(image, "png", new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}



/*public static BufferedImage criarImagem(DoubleMatrix f) {
    int size = (int) Math.sqrt(f.length);
    BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

    for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
            int index = i * size + j;
            double value = f.get(index);

            // Calcula o valor da opacidade (transparência inversa)
            int alpha = (int) (255 * value);
            alpha = Math.min(255, Math.max(0, alpha));  // Garante que esteja entre 0 e 255

            // Cor branca com opacidade variável
            int pixelValue = new Color(255, 255, 255, alpha).getRGB();
            image.setRGB(j, i, pixelValue);
        }
    }

    // Rotaciona a imagem em 90 graus, se necessário
    BufferedImage rotatedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
            rotatedImage.setRGB(size - 1 - j, i, image.getRGB(i, j));
        }
    }

    return rotatedImage;
}*/
