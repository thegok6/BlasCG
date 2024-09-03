import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import javax.imageio.ImageIO;

import org.jblas.DoubleMatrix;

import java.io.*;
import java.net.*;
import java.time.LocalDate;

public class Server {

    private static final int PORT = 5178;
    private static final String SERVER_IP = "localhost";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket()) {

            InetAddress serverInetAddress = InetAddress.getByName(SERVER_IP);
            serverSocket.bind(new InetSocketAddress(serverInetAddress, PORT));

            System.out.println("Servidor aberto: " + SERVER_IP + ":" + PORT);

            while (true) {
                try {
                    // Aguarda conexão do cliente
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nova conexão de " + clientSocket.getRemoteSocketAddress());

                    // Inicia uma nova thread para o cliente conectado
                    ThreadService threadService = new ThreadService(clientSocket);
                    threadService.start();
                    
                } catch (IOException e) {
                    System.err.println("Erro com cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}

class ThreadService extends Thread {

    private Socket clientSocket;

    public ThreadService(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            InputStream receptor = clientSocket.getInputStream();
            DataInputStream entradaDados = new DataInputStream(receptor);

            String nome = entradaDados.readUTF();
            String algoritmo = entradaDados.readUTF();
            LocalDate dataInicio = LocalDate.now();

            System.out.println("Usuário: " + nome);
            System.out.println("Algoritmo: " + algoritmo);
            System.out.println("Data de início: " + dataInicio);
            
            
            int rows = entradaDados.readInt();
            int columns = entradaDados.readInt();

            double[] data = new double[rows * columns];
            for (int i = 0; i < data.length; i++) {
                data[i] = entradaDados.readDouble();
            }
            DoubleMatrix h = new DoubleMatrix(rows, columns, data);
            
            rows = entradaDados.readInt();
            columns = entradaDados.readInt();

            data = new double[rows * columns];
            for (int i = 0; i < data.length; i++) {
                data[i] = entradaDados.readDouble();
            }
            DoubleMatrix g = new DoubleMatrix(rows, columns, data);
            BufferedImage img = ImageGenerator.criarImagem(CNGR.Calcular(h, g));
            OutputStream enviar = clientSocket.getOutputStream();
            DataOutputStream enviarDados = new DataOutputStream(enviar);
            String teste = "teste";
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(img, "png", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            enviarDados.writeInt(imageBytes.length);
            enviarDados.write(imageBytes);
            enviarDados.flush();
            
            
            
            
            
            
            
            
            
            enviarDados.writeUTF(teste);  // Envia "teste" para o cliente
            enviarDados.flush();
            
            clientSocket.close();
            
        } catch (IOException e) {
            System.err.println("Erro ao processar cliente: " + e.getMessage());
        }
    }
}
