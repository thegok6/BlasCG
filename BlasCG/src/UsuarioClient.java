import javax.imageio.ImageIO;
import javax.swing.*;

import org.jblas.DoubleMatrix;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class UsuarioClient extends JFrame {

    private JTextField ipField;
    private JTextField portField;
    private JTextField nomeField;
    private JTextField algoritmoField;
    private JTextArea messageArea;
    private DoubleMatrix H;
    private DoubleMatrix g;
    
    

    public UsuarioClient() {
        setTitle("Usuario Client");
        setSize(1440, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Campos de entrada
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        add(new JLabel("Server IP:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        ipField = new JTextField();
        add(ipField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        add(new JLabel("Port:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        portField = new JTextField();
        add(portField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.2;
        add(new JLabel("Nome do Usuário:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        nomeField = new JTextField();
        add(nomeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.2;
        add(new JLabel("Algoritmo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        algoritmoField = new JTextField();
        add(algoritmoField, gbc);

        // Área de mensagem
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        messageArea = new JTextArea(10, 30);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, gbc);

        // Botão de envio
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        JButton sendButton = new JButton("Send");
        add(sendButton, gbc);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setVisible(true);
    }

    private void sendMessage() {
        String serverIP = ipField.getText();
        int port = Integer.parseInt(portField.getText());
        String nomeUsuario = nomeField.getText();
        String algoritmo = algoritmoField.getText();

        try (Socket socket = new Socket(serverIP, port)) {
            OutputStream emissor = socket.getOutputStream();
            DataOutputStream saidaDados = new DataOutputStream(emissor);

            // Envia dados ao servidor
            saidaDados.writeUTF(nomeUsuario);
            saidaDados.flush();
            saidaDados.writeUTF(algoritmo);
            saidaDados.flush();
            GerarModeloSinal();
            saidaDados.writeInt(H.rows);
            saidaDados.flush();
            saidaDados.writeInt(H.columns);
            saidaDados.flush();
            for (int i = 0; i < H.length; i++) {
            	saidaDados.writeDouble(H.get(i));
            	saidaDados.flush();
            }
            saidaDados.writeInt(g.rows);
            saidaDados.flush();
            saidaDados.writeInt(g.columns);
            saidaDados.flush();

            // Send the data
            for (int i = 0; i < g.length; i++) {
            	saidaDados.writeDouble(g.get(i));
            	saidaDados.flush();
            }
            
            

            // Recebe a resposta do servidor
            InputStream receptor = socket.getInputStream();
            DataInputStream entradaDados = new DataInputStream(receptor);
            
            
            
            int length = entradaDados.readInt();
            byte[] imageBytes = new byte[length];

            // Receive the image byte array
            entradaDados.readFully(imageBytes);

            // Convert byte array back to BufferedImage
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage receivedImage = ImageIO.read(byteArrayInputStream);
            ImageDisplay.windows(receivedImage);
            ImageIO.write(receivedImage, "png", new File("imagem.png"));
            
            
            
            String resposta = entradaDados.readUTF();  // Lê a resposta do servidor

            messageArea.append("Detalhes: " + resposta + "\n");

        } catch (IOException e) {
            messageArea.append("Erro de conexão: " + e.getMessage() + "\n");
        }
    }
    
    
    
    private void GerarModeloSinal()
    {
        int minRows = 10000;
        int maxRows = 12000;
        int minSqrtColumns = 25;
        int maxSqrtColumns = 35;
        Random random = new Random();
        int rows = random.nextInt(maxRows - minRows + 1) + minRows;
        int sqrtColumns = random.nextInt(maxSqrtColumns - minSqrtColumns + 1) + minSqrtColumns;
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
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UsuarioClient());
    }
}
