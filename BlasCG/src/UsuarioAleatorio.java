import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

import javax.imageio.ImageIO;

import org.jblas.DoubleMatrix;

public class UsuarioAleatorio {
    private static DoubleMatrix H;
    private static DoubleMatrix g;
    private static String nomeUsuario;
	public static void main(String args[]) throws InterruptedException, IOException
	{
        String serverIP = args[0];
        int port = Integer.parseInt(args[1]);
        /*String serverIP = "26.193.34.184";
        int port = Integer.parseInt("5178");*/
    	String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder usuario = new StringBuilder();
        int t = 0;
        while(true)
        {
            
            Random random = new Random();

            for (int i = 0; i < 8; i++) {
                int index = random.nextInt(caracteres.length());
                usuario.append(caracteres.charAt(index));
            }
            nomeUsuario = new String(usuario) + t;
            String algoritmo = "aleatorio";
            try (Socket socket = new Socket(serverIP, port)) {
                OutputStream emissor = socket.getOutputStream();
                DataOutputStream saidaDados = new DataOutputStream(emissor);

                
                saidaDados.writeUTF(nomeUsuario);
                saidaDados.flush();
                saidaDados.writeUTF(algoritmo);
                saidaDados.flush();

                GerarModeloSinal();
                saidaDados.writeInt(H.rows);
                saidaDados.flush();
                saidaDados.writeInt(H.columns);
                saidaDados.flush();
                saidaDados.writeInt(g.rows);
                saidaDados.flush();
                saidaDados.writeInt(g.columns);
                saidaDados.flush();
                /*saidaDados.writeUTF(algoritmo);
                saidaDados.flush();*/
                
                File fileH = null;
                File fileG = null;
            	fileH = new File(nomeUsuario + "H.csv");
            	fileG = new File(nomeUsuario + "G.csv");
                long fileHSize = fileH.length();
                long fileGSize = fileG.length();

                /*for (int i = 0; i < H.length; i++) {
                    saidaDados.writeDouble(H.get(i));
                    saidaDados.flush();
                }*/
                //byte[] buffer = new byte[(int) fileHSize]; // 2GB de buffer
                saidaDados.writeLong(fileHSize);
                saidaDados.flush();
                saidaDados.writeLong(fileGSize);
                saidaDados.flush();
                byte[] buffer = new byte[20 * 1024 * 1024];

                try (FileInputStream fileInputStreamH = new FileInputStream(fileH);
                     FileInputStream fileInputStreamG = new FileInputStream(fileG)) {

                    int bytesRead;
                    
                    while ((bytesRead = fileInputStreamH.read(buffer)) != -1) {
                        saidaDados.write(buffer, 0, bytesRead);
                        saidaDados.flush();
                    }

                    Thread.sleep(1000);

                    while ((bytesRead = fileInputStreamG.read(buffer)) != -1) {
                        saidaDados.write(buffer, 0, bytesRead);
                        saidaDados.flush();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                InputStream receptor = socket.getInputStream();
                DataInputStream entradaDados = new DataInputStream(receptor);


                
                int length = entradaDados.readInt();
                byte[] imageBytes = new byte[length];
                entradaDados.readFully(imageBytes);

                
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
                BufferedImage receivedImage = ImageIO.read(byteArrayInputStream);
                ImageDisplay.windows(receivedImage);
                ImageIO.write(receivedImage, "png", new File(usuario + ".png"));
                String resposta = entradaDados.readUTF();
                
                try {
                    FileWriter escritor = new FileWriter("Relatorio_" + usuario + ".txt");
                    escritor.write(resposta); 
                    escritor.close(); 
                } catch (IOException e) {
                    e.printStackTrace();
                }
            
            Thread.sleep((10 + random.nextInt(7)) * 1000);
            t++;
        }
	}}
        
        
        private static void GerarModeloSinal() {
            int minRows = 25600;
            int maxRows = 25600;
            int minSqrtColumns = 60;
            int maxSqrtColumns = 60;
            Random random = new Random();
            int rows = 11520;
            int sqrtColumns = 60;
            int columns = sqrtColumns * sqrtColumns;
            
            DoubleMatrix modelo = DoubleMatrix.zeros(rows, columns);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    if (random.nextDouble() < 0.01) {
                        modelo.put(i, j, random.nextDouble() * 1e-6);
                    }
                }
            }
            H = modelo;
            DoubleMatrix f = DoubleMatrix.rand(H.columns, 1);
            g = H.mmul(f);
            salvarDoubleMatrixEmCSV(H, nomeUsuario + "H.csv");
            salvarDoubleMatrixEmCSV(g, nomeUsuario + "G.csv");
            
        }
        
        public static void salvarDoubleMatrixEmCSV(DoubleMatrix matrix, String caminhoArquivo) {
            try (FileWriter writer = new FileWriter(caminhoArquivo)) {
                for (int i = 0; i < matrix.rows; i++) {
                    for (int j = 0; j < matrix.columns; j++) {
                        writer.append(String.valueOf(matrix.get(i, j)));
                        if (j < matrix.columns - 1) {
                            writer.append(",");
                        }
                    }
                    writer.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
