import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class Server {

    private static final int PORT = 5178;
    private static final String SERVER_IP = "2001:1284:f508:a321:339a:a082:4c5f:7705";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket()) {

            InetAddress serverInetAddress = InetAddress.getByName(SERVER_IP);
            serverSocket.bind(new InetSocketAddress(serverInetAddress, PORT));

            System.out.println("Servidor aberto: " + SERVER_IP + ":" + PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("New connection from " + clientSocket.getRemoteSocketAddress());
                    InputStream receptor = clientSocket.getInputStream();
                    DataInputStream entradaDados = new DataInputStream(receptor);
                    String nome = entradaDados.readUTF();
                    String algoritmo = entradaDados.readUTF();
                    LocalDate dataInicio = LocalDate.now();
                    System.out.println("Usuário: " + nome);
                    System.out.println("Algoritmo: " + algoritmo);
                    System.out.println("Conexão encerrada com cliente: " + clientSocket.getRemoteSocketAddress());
                } catch (IOException e) {
                    System.err.println("Erro com cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}
