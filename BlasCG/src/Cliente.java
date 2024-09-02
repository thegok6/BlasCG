import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Cliente {

    private static final int SERVER_PORT = 5178;
    private static final String SERVER_IP = "2001:1284:f508:a321:339a:a082:4c5f:7705";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            System.out.println("Conectado ao servidor: " + SERVER_IP + ":" + SERVER_PORT);

            
            String nomeUsuario = "Rodrigo";
            String algoritmo = "CGNR";

            
            OutputStream emissor = socket.getOutputStream();
            DataOutputStream saidaDados = new DataOutputStream(emissor);
            saidaDados.writeUTF(nomeUsuario); 
            saidaDados.flush();

            
            saidaDados.writeUTF(algoritmo); 
            saidaDados.flush();

            System.out.println("Nome do usuário enviado: " + nomeUsuario);
            System.out.println("Nome do algoritmo enviado: " + algoritmo);
        } catch (IOException e) {
            System.err.println("Erro de conexão: " + e.getMessage());
        }
    }
}
