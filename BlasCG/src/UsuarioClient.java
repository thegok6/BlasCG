import javax.imageio.ImageIO;
import javax.swing.*;
import org.jblas.DoubleMatrix;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
        add(new JLabel("Escolha se modelo é imagem de teste 1 2 3 / 4 5 6 / ganho aleatório ou aleatório"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        algoritmoField = new JTextField();
        add(algoritmoField, gbc);

        
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
                try {
					sendMessage();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });

        setVisible(true);
    }

    private void sendMessage() throws InterruptedException {
        String serverIP = ipField.getText();
        int port = Integer.parseInt(portField.getText());
        String nomeUsuario = nomeField.getText();
        String algoritmo = algoritmoField.getText();

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
            String t = algoritmoField.getText().toUpperCase();
            if(algoritmoField.getText().equals("1") || t.equals("1G")) 
            {
            	fileH = new File("h1.csv");
            	fileG = new File("g1.csv");
            }
            else if(algoritmoField.getText().equals("2") || t.equals("2G")) 
            {
            	fileH = new File("h1.csv");
            	fileG = new File("g2.csv");
            }
            else if(algoritmoField.getText().equals("3") || t.equals("3G")) 
            {
            	fileH = new File("h1.csv");
            	fileG = new File("g3.csv");
            }
            else if(algoritmoField.getText().equals("4") || t.equals("4G")) 
            {
            	fileH = new File("h2.csv");
            	fileG = new File("g4.csv");
            }
            else if(algoritmoField.getText().equals("5") || t.equals("5G")) 
            {
            	fileH = new File("h2.csv");
            	fileG = new File("g5.csv");
            }
            else if(algoritmoField.getText().equals("6") || t.equals("6G")) 
            {
            	fileH = new File("h2.csv");
            	fileG = new File("g6.csv");
            }
            else {
            	fileH = new File("Hal.csv");
            	fileG = new File("Gal.csv");
            }
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
                
                // Envia o arquivo H em blocos de 20MB
                while ((bytesRead = fileInputStreamH.read(buffer)) != -1) {
                    saidaDados.write(buffer, 0, bytesRead);
                    saidaDados.flush();
                }

                // Pausa breve para assegurar que o buffer não seja misturado
                Thread.sleep(1000);

                // Envia o arquivo G em blocos de 20MB
                while ((bytesRead = fileInputStreamG.read(buffer)) != -1) {
                    saidaDados.write(buffer, 0, bytesRead);
                    saidaDados.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }



            
            /*for (int i = 0; i < g.length; i++) {
                saidaDados.writeDouble(g.get(i));
                saidaDados.flush();
            }*/

            
            InputStream receptor = socket.getInputStream();
            DataInputStream entradaDados = new DataInputStream(receptor);

            messageArea.append("Aguardando na fila...\n");

            
            int length = entradaDados.readInt();
            byte[] imageBytes = new byte[length];
            entradaDados.readFully(imageBytes);

            
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage receivedImage = ImageIO.read(byteArrayInputStream);
            ImageDisplay.windows(receivedImage);
            ImageIO.write(receivedImage, "png", new File(t + ".png"));

            
            String resposta = entradaDados.readUTF();
            messageArea.append("Resposta do servidor: " + resposta + "\n");

        } catch (IOException e) {
            messageArea.append("Erro de conexão: " + e.getMessage() + "\n");
        }
    }

    private void GerarModeloSinal() {
        int minRows = 25600;
        int maxRows = 25600;
        int minSqrtColumns = 60;
        int maxSqrtColumns = 60;
        Random random = new Random();
        int rows = 25600;
        int sqrtColumns = 60;
        int columns = sqrtColumns * sqrtColumns;
        String t = algoritmoField.getText().toUpperCase();
        if(algoritmoField.getText().equals("1") || t.equals("1G")) 
        {
        	H = lerCSVParaDoubleMatrix("h1.csv");
        	g = lerCSVParaDoubleMatrix("g1.csv");
        }
        else if(algoritmoField.getText().equals("2") || t.equals("2G")) 
        {
        	H = lerCSVParaDoubleMatrix("h1.csv");
        	g = lerCSVParaDoubleMatrix("g2.csv");
        }
        else if(algoritmoField.getText().equals("3") || t.equals("3G")) 
        {
        	H = lerCSVParaDoubleMatrix("h1.csv");
        	g = lerCSVParaDoubleMatrix("g3.csv");
        }
        else if(algoritmoField.getText().equals("4") || t.equals("4G")) 
        {
        	H = lerCSVParaDoubleMatrix("h2.csv");
        	g = lerCSVParaDoubleMatrix("g4.csv");
        }
        else if(algoritmoField.getText().equals("5") || t.equals("5G")) 
        {
        	H = lerCSVParaDoubleMatrix("h2.csv");
        	g = lerCSVParaDoubleMatrix("g5.csv");
        }
        else if(algoritmoField.getText().equals("6") || t.equals("6G")) 
        {
        	H = lerCSVParaDoubleMatrix("h2.csv");
        	g = lerCSVParaDoubleMatrix("g6.csv");
        }
        else
        {
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
        salvarDoubleMatrixEmCSV(H, "Hal.csv");
        salvarDoubleMatrixEmCSV(g, "Gal.csv");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UsuarioClient());
    }
    
    
    public static DoubleMatrix lerCSVParaDoubleMatrix(String csvFile) {
        List<double[]> rows = new ArrayList<>();
        String line;
        String csvSplitBy = ","; 

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                String[] values = line.split(csvSplitBy);
                double[] row = new double[values.length];
                
                for (int i = 0; i < values.length; i++) {
                    row[i] = Double.parseDouble(values[i]); 
                }
                
                rows.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        double[][] data = new double[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            data[i] = rows.get(i);
        }


        return new DoubleMatrix(data);
    }
    
    public static void salvarDoubleMatrixEmCSV(DoubleMatrix matrix, String caminhoArquivo) {
        try (FileWriter writer = new FileWriter(caminhoArquivo)) {
            for (int i = 0; i < matrix.rows; i++) {
                for (int j = 0; j < matrix.columns; j++) {
                    writer.append(String.valueOf(matrix.get(i, j)));
                    if (j < matrix.columns - 1) {
                        writer.append(",");  // Se não for o último valor da linha, adiciona vírgula
                    }
                }
                writer.append("\n");  // Vai para a próxima linha
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
