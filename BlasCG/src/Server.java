import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jblas.DoubleMatrix;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Server {

    private static final int PORT = 5178;
    private static final String SERVER_IP = "26.193.34.184";

    
    private static final BlockingQueue<Socket> filaDeClientes = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket()) {

            /*InetAddress serverInetAddress = InetAddress.getByName("192.168.18.17");
            serverSocket.bind(new InetSocketAddress(serverInetAddress, Integer.parseInt("5178")));*/
            InetAddress serverInetAddress = InetAddress.getByName(args[0]);
            serverSocket.bind(new InetSocketAddress(serverInetAddress, Integer.parseInt(args[1])));
            System.out.println("Servidor aberto: " + SERVER_IP + ":" + PORT);

            
            Thread processarFila = new Thread(() -> {
                while (true) {
                    try {
                        
                        Socket clientSocket = filaDeClientes.take();
                        processarCliente(clientSocket);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Erro ao processar fila de clientes: " + e.getMessage());
                    }
                }
            });
            processarFila.start();

            
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nova conexão de " + clientSocket.getRemoteSocketAddress());

                    
                    filaDeClientes.put(clientSocket);
                } catch (IOException | InterruptedException e) {
                    System.err.println("Erro ao adicionar cliente na fila: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }

    
    private static void processarCliente(Socket clientSocket) throws InterruptedException {
        try {
            InputStream receptor = clientSocket.getInputStream();
            DataInputStream entradaDados = new DataInputStream(receptor);

            String nome = entradaDados.readUTF();
            String algoritmo = entradaDados.readUTF().toLowerCase();
            LocalDate dataInicio = LocalDate.now();

            System.out.println("Usuário: " + nome);
            System.out.println("Algoritmo: " + algoritmo);
            System.out.println("Data de início: " + dataInicio);

            int rowsH = entradaDados.readInt();
            int columnsH = entradaDados.readInt();
            int rowsG = entradaDados.readInt();
            int columnsG = entradaDados.readInt();
            long sizeH = entradaDados.readLong();
            long sizeG = entradaDados.readLong();

            System.out.println("Recebendo arquivo H.csv de " + sizeH + " bytes...");
            salvarArquivoCSV(entradaDados, "h.csv", sizeH);  // Recebe e salva H.csv
            File fH = new File("h.csv");
           // decompressFileFromMemory(fH);

            System.out.println("Recebendo arquivo G.csv de " + sizeG + " bytes...");
            salvarArquivoCSV(entradaDados, "g.csv", sizeG);  // Recebe e salva G.csv
            File fg = new File("g.csv");
            //decompressFileFromMemory(new File("g.zip"));

            System.out.println("Arquivos H.csv e G.csv recebidos e salvos.");

            DoubleMatrix h = lerCSVParaDoubleMatrix("h.csv");
            DoubleMatrix g = lerCSVParaDoubleMatrix("g.csv");
            fH.delete();
            fg.delete();

            SimpleDateFormat formato = new SimpleDateFormat("HH:mm:ss:SSSS");
            String Hora_antes = formato.format(new Date());
            BufferedImage img;

            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long memoriaAntes = runtime.totalMemory() - runtime.freeMemory();
            long tempoInicio = System.currentTimeMillis();
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            SystemInfo systemInfo = new SystemInfo();
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            long[] prevTicks = processor.getSystemCpuLoadTicks();

            if (algoritmo.equals("1g") || algoritmo.equals("2g") || algoritmo.equals("3g")) {
                img = ImageGenerator.criarImagem(CNGR.Calcular(h, g, 64, 794), 0.25, 40);
            } else if (algoritmo.equals("4g") || algoritmo.equals("5g") || algoritmo.equals("6g")) {
                img = ImageGenerator.criarImagem(CNGR.Calcular(h, g, 64, 436), 0.25, 40);
            } else if (algoritmo.equals("ganho")) {
                img = ImageGenerator.criarImagem(CNGR.Calcular(h, g, 64, 180), 0.25, 40);
            } else {
                img = ImageGenerator.criarImagem(CNGR.Calcular(h, g, 0, 0), 1, 200);
            }

            String Hora_depois = formato.format(new Date());
            Thread.sleep(200);
            long[] postTicks = processor.getSystemCpuLoadTicks();
            double usoCPU = calcularUsoCpu(prevTicks, postTicks);
            long tempoFim = System.currentTimeMillis();
            long memoriaDepois = runtime.totalMemory() - runtime.freeMemory();
            long memoriaGasta = memoriaDepois - memoriaAntes;
            long tempoGasto = System.currentTimeMillis() - tempoInicio;

            String teste = "Início: " + Hora_antes + "\n" + "Fim: " + Hora_depois + "\n" + "Número de iterações: " + CNGR.getI() + "\n" + "Tempo de execução (milisegundos): " + (tempoGasto) + "\n" + "Memória média usada durante o algoritmo: " + CNGR.getCPU() / (1024 * 1024) + " MB" + "\n" + 
                    "Uso médio de CPU durante o algoritmo: " + (usoCPU * 100) + "%";

            OutputStream enviar = clientSocket.getOutputStream();
            DataOutputStream enviarDados = new DataOutputStream(enviar);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(img, "png", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            enviarDados.writeInt(imageBytes.length);
            enviarDados.write(imageBytes);
            enviarDados.flush();
            enviarDados.writeUTF(teste);
            enviarDados.flush();

            clientSocket.close();
            g = null;
            h = null;

        } catch (IOException e) {
            System.err.println("Erro ao processar cliente: " + e.getMessage());
        }
    }

    private static double calcularUsoCpu(long[] prevTicks, long[] postTicks) {
    	long totalCpu = 0;
    	long totalUsedCpu = 0;
    	while(totalCpu == 0 || totalUsedCpu == 0) {
        long user = postTicks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long nice = postTicks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long sys = postTicks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long idle = postTicks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long iowait = postTicks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long irq = postTicks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = postTicks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = postTicks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        

        totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
        totalUsedCpu = totalCpu - idle - iowait;
        System.out.println("totalCpu: " + totalCpu);
        System.out.println("totalUsedCpu: " + totalUsedCpu);
    	}
        

        return (double) totalUsedCpu / totalCpu;
    }
    
    public static void salvarArquivoCSV(DataInputStream entradaDados, String nomeArquivo, long fileSize) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(
                Path.of(nomeArquivo), 
                StandardOpenOption.CREATE, 
                StandardOpenOption.READ,  // Add READ permission
                StandardOpenOption.WRITE)) {

            // Memory-map the output file
            MappedByteBuffer mappedBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);

            byte[] buffer = new byte[1024 * 1024]; // 1MB buffer
            long totalBytesRead = 0;
            int bytesRead;

            while (totalBytesRead < fileSize && (bytesRead = entradaDados.read(buffer, 0, Math.min(buffer.length, (int)(fileSize - totalBytesRead)))) != -1) {
                mappedBuffer.put(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
        }

        System.out.println(nomeArquivo + " salvo com sucesso.");
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
        int[] dimensions = new int[2]; // dimensions[0] = rows, dimensions[1] = cols
        int rowCount = 0;
        int colCount = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                rowCount++;
                if (rowCount == 1) {
                    String[] values = line.split(",");  // assuming comma-separated
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

    // Main method to read CSV into DoubleMatrix using memory-mapped file
    public static DoubleMatrix lerCSVParaDoubleMatrix(String filePath) throws IOException {
        // First, get the number of rows and columns from the CSV
        int[] dimensions = getCSVDimensions(filePath);
        int rows = dimensions[0];
        int cols = dimensions[1];

        try (FileChannel fileChannel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ)) {
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

            DoubleMatrix matrix = DoubleMatrix.zeros(rows, cols);
            int row = 0;
            int col = 0;
            
            // Temporary buffer to read CSV lines manually
            StringBuilder sb = new StringBuilder();
            
            // Loop through the buffer to extract CSV data
            while (buffer.hasRemaining()) {
                char c = (char) buffer.get();
                
                if (c == ',') {
                    // End of value, parse and store
                    matrix.put(row, col, Double.parseDouble(sb.toString()));
                    sb.setLength(0); // clear the StringBuilder for the next value
                    col++;
                } else if (c == '\n') {
                    // End of line, store last value and move to the next row
                    matrix.put(row, col, Double.parseDouble(sb.toString()));
                    sb.setLength(0);
                    row++;
                    col = 0; // reset column index for next row
                } else {
                    sb.append(c); // Keep reading characters
                }
            }
            return matrix;
        }
    }
    
    public static void decompressFileFromMemory(File zipFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipInputStream zipIn = new ZipInputStream(fis)) {

            ZipEntry zipEntry = zipIn.getNextEntry();
            while (zipEntry != null) {
                // Output to ByteArrayOutputStream in memory
                ByteArrayOutputStream fH = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = zipIn.read(buffer)) >= 0) {
                    fH.write(buffer, 0, length);
                }

                // Save the decompressed entry (fH) to a file on disk
                File outputFile = new File("h.csv");
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fH.writeTo(fos);  // Save the content of fH to the file
                }

                System.out.println("Decompressed and saved entry: " + zipEntry.getName());

                zipIn.closeEntry();
                zipEntry = zipIn.getNextEntry();
            }
        } catch (IOException e) {
            throw new IOException("Error decompressing file: " + e.getMessage());
        }
    }
}
