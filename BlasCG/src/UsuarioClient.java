import javax.imageio.ImageIO;
import javax.swing.*;
import org.jblas.DoubleMatrix;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class UsuarioClient extends JFrame {

    private JTextField ipField;
    private JTextField portField;
    private JTextField nomeField;
    private JTextField algoritmoField;
    private JTextArea messageArea;
    private DoubleMatrix H;
    private DoubleMatrix g;
    private String t;
    private int j;
    private String alg;

    public UsuarioClient() throws InterruptedException {
    	t = "continuo";
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
        j = 0;
        alg = "";

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                	j++;
					sendMessage();
				} catch (InterruptedException e1) {
					
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
        alg = algoritmoField.getText();
        int in = 0;
        while(algoritmo.equals("continuo") || in == 0) {
        in++;
        try (Socket socket = new Socket(serverIP, port)) {
        	messageArea.repaint();
            OutputStream emissor = socket.getOutputStream();
            DataOutputStream saidaDados = new DataOutputStream(emissor);

            GerarModeloSinal();
            saidaDados.writeUTF(nomeUsuario);
            saidaDados.flush();
            saidaDados.writeUTF(t);
            saidaDados.flush();
            saidaDados.writeInt(H.rows);
            saidaDados.flush();
            saidaDados.writeInt(H.columns);
            saidaDados.flush();
            saidaDados.writeInt(g.rows);
            saidaDados.flush();
            saidaDados.writeInt(g.columns);
            saidaDados.flush();

            String fileH;
            String fileG ;
            
            if (t.equals("1") || t.equals("1G")) {
                fileH = ("h1.csv");
                fileG = ("g1.csv");
            } else if (t.equals("2") || t.equals("2G")) {
                fileH = ("h1.csv");
                fileG = ("g2.csv");
            } else if (t.equals("3") || t.equals("3G")) {
                fileH = ("h1.csv");
                fileG = ("g3.csv");
            } else if (t.equals("4") || t.equals("4G")) {
                fileH = ("h2.csv");
                fileG = ("g4.csv");
            } else if (t.equals("5") || t.equals("5G")) {
                fileH = ("h2.csv");
                fileG = ("g5.csv");
            } else if (t.equals("6") || t.equals("6G")) {
                fileH = ("h2.csv");
                fileG = ("g6.csv");
            } else {
                fileH = ("Hal.csv");
                fileG = ("Gal.csv");
            }

            
            /*long fileHSize = fileH.length();
            long fileGSize = fileG.length();

            saidaDados.writeLong(fileHSize);
            saidaDados.flush();
            saidaDados.writeLong(fileGSize);
            saidaDados.flush();*/

            try (FileChannel fileChannelH = new FileInputStream(fileH).getChannel();
                    FileChannel fileChannelG = new FileInputStream(fileG).getChannel()) {
	                saidaDados.writeLong(fileChannelH.size());
	                saidaDados.flush();
	                saidaDados.writeLong(fileChannelG.size());
	                saidaDados.flush();
                   
                   MappedByteBuffer bufferH = fileChannelH.map(MapMode.READ_ONLY, 0, fileChannelH.size());
                   byte[] bufferArrayH = new byte[bufferH.remaining()];
                   bufferH.get(bufferArrayH);

                   
                   saidaDados.write(bufferArrayH);
                   saidaDados.flush();

                   
                   Thread.sleep(1000);

                   
                   MappedByteBuffer bufferG = fileChannelG.map(MapMode.READ_ONLY, 0, fileChannelG.size());
                   byte[] bufferArrayG = new byte[bufferG.remaining()];
                   bufferG.get(bufferArrayG);

                   
                   saidaDados.write(bufferArrayG);
                   saidaDados.flush();

               } catch (IOException | InterruptedException e) {
                   e.printStackTrace();
               }

            InputStream receptor = socket.getInputStream();
            DataInputStream entradaDados = new DataInputStream(receptor);

            messageArea.append("Aguardando na fila...\n");

            int length = entradaDados.readInt();
            byte[] imageBytes = new byte[length];
            entradaDados.readFully(imageBytes);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage receivedImage = ImageIO.read(byteArrayInputStream);
            
            ImageIO.write(receivedImage, "png", new File(t + ".png"));

            String resposta = entradaDados.readUTF();
            messageArea.append("Resposta do servidor: " + resposta + "\n");
            socket.close();
            try {
                FileWriter escritor = new FileWriter("Relatorio_" + nomeUsuario + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
                escritor.write(resposta); 
                escritor.close(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            messageArea.append("Erro de conexão: " + e.getMessage() + "\n");
        }
        messageArea.revalidate();
        messageArea.repaint();
        if(algoritmo.equals("continuo"))
        {
        	Thread.sleep(2000);
        }
        }
    }


    private void GerarModeloSinal() throws IOException {
        int minRows = 11520;
        int maxRows = 11520;
        int minSqrtColumns = 60;
        int maxSqrtColumns = 60;
        Random random = new Random();
        int rows = 11520;
        int sqrtColumns = 60;
        int columns = sqrtColumns * sqrtColumns;
        t = algoritmoField.getText().toUpperCase();
        if (!t.equals("1") && !t.equals("2") && !t.equals("3") && !t.equals("4") &&
                !t.equals("5") && !t.equals("6") && !t.equals("1G") && !t.equals("2G") &&
                !t.equals("3G") && !t.equals("4G") && !t.equals("5G") && !t.equals("6G"))
        {
            String[] elements = {"1", "2","3", "4", "5", "6", "1G", "2G","3G", "4G", "5G", "6G"};
            Random r = new Random();
            t = elements[r.nextInt(elements.length)];
        }
        if(t.equals("1") || t.equals("1G")) 
        {
        	H = lerCSVParaDoubleMatrix("h1.csv");
        	g = lerCSVParaDoubleMatrix("g1.csv");
        }
        else if(t.equals("2") || t.equals("2G")) 
        {
        	H = lerCSVParaDoubleMatrix("h1.csv");
        	g = lerCSVParaDoubleMatrix("g2.csv");
        }
        else if(t.equals("3") || t.equals("3G")) 
        {
        	H = lerCSVParaDoubleMatrix("h1.csv");
        	g = lerCSVParaDoubleMatrix("g3.csv");
        }
        else if(t.equals("4") || t.equals("4G")) 
        {
        	H = lerCSVParaDoubleMatrix("h2.csv");
        	g = lerCSVParaDoubleMatrix("g4.csv");
        }
        else if(t.equals("5") || t.equals("5G")) 
        {
        	H = lerCSVParaDoubleMatrix("h2.csv");
        	g = lerCSVParaDoubleMatrix("g5.csv");
        }
        else if(t.equals("6") || t.equals("6G")) 
        {
        	H = lerCSVParaDoubleMatrix("h2.csv");
        	g = lerCSVParaDoubleMatrix("g6.csv");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
			try {
				new UsuarioClient();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		});
    }
    
    
    /*public static DoubleMatrix lerCSVParaDoubleMatrix(String csvFile) {
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
    }*/
    
    public static int[] getCSVDimensions(String csvFile) throws IOException {
        int[] dimensions = new int[2]; 
        int rowCount = 0;
        int colCount = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                rowCount++;
                if (rowCount == 1) {
                    String[] values = line.split(",");  
                    colCount = values.length;
                }
            }
        } catch (IOException e) {
            throw new IOException("Error reading CSV file: " + e.getMessage());
        }
        
        dimensions[0] = rowCount;
        dimensions[1] = colCount;
        return dimensions;
    }

    
    public static DoubleMatrix lerCSVParaDoubleMatrix(String filePath) throws IOException {
        
        int[] dimensions = getCSVDimensions(filePath);
        int rows = dimensions[0];
        int cols = dimensions[1];

        try (FileChannel fileChannel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ)) {
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

            DoubleMatrix matrix = DoubleMatrix.zeros(rows, cols);
            int row = 0;
            int col = 0;
            
            
            StringBuilder sb = new StringBuilder();
            
            
            while (buffer.hasRemaining()) {
                char c = (char) buffer.get();
                
                if (c == ',') {
                    
                    matrix.put(row, col, Double.parseDouble(sb.toString()));
                    sb.setLength(0); 
                    col++;
                } else if (c == '\n') {
                    
                    matrix.put(row, col, Double.parseDouble(sb.toString()));
                    sb.setLength(0);
                    row++;
                    col = 0; 
                } else {
                    sb.append(c); 
                }
            }
            return matrix;
        }
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
    
    
    public static File compressFileToMemory(File fileToCompress) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream);
             FileInputStream fis = new FileInputStream(fileToCompress)) {

            ZipEntry zipEntry = new ZipEntry("hrar");
            zipOut.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zipOut.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new IOException("Error compressing file: " + e.getMessage());
        }

        
        File compressedFile = new File("hrar" + ".zip");
        try (FileOutputStream fos = new FileOutputStream(compressedFile)) {
            byteArrayOutputStream.writeTo(fos);
        }

        return compressedFile;
    }
    
    
    
    
    
    
}
