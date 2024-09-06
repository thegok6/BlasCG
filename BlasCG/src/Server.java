import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

            InetAddress serverInetAddress = InetAddress.getByName(SERVER_IP);
            serverSocket.bind(new InetSocketAddress(serverInetAddress, PORT));

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
            String algoritmo = entradaDados.readUTF();
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
            System.out.println("Recebendo arquivo G.csv de " + sizeG + " bytes...");
            salvarArquivoCSV(entradaDados, "g.csv", sizeG);  // Recebe e salva G.csv
            System.out.println("Arquivos H.csv e G.csv recebidos e salvos.");
            /*double[] data = new double[rows * columns];
            for (int i = 0; i < data.length; i++) {
                data[i] = entradaDados.readDouble();
            }


            data = new double[rows * columns];
            for (int i = 0; i < data.length; i++) {
                data[i] = entradaDados.readDouble();
            }*/
            DoubleMatrix h = lerCSVParaDoubleMatrix("h.csv");
            DoubleMatrix g = lerCSVParaDoubleMatrix("g.csv");
            
            
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long memoriaAntes = runtime.totalMemory() - runtime.freeMemory();
            long tempoInicio = System.currentTimeMillis();
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            SystemInfo systemInfo = new SystemInfo();
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            BufferedImage img = ImageGenerator.criarImagem(CNGR.Calcular(h, g));
            long[] postTicks = processor.getSystemCpuLoadTicks();
            double usoCPU = calcularUsoCpu(prevTicks, postTicks);
            long tempoFim = System.currentTimeMillis();
            long memoriaDepois = runtime.totalMemory() - runtime.freeMemory();
            long memoriaGasta = memoriaDepois - memoriaAntes;
            long tempoGasto = System.currentTimeMillis() - tempoInicio;
            
            String teste = "Número de iterações: " + CNGR.getI() + "\n" + " Tempo de execução (milisegundos): " + (tempoGasto) + "\n" + "Memória média usada durante o algoritmo: " + CNGR.getCPU() / (1024 * 1024) + " MB" + "\n" + 
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

        } catch (IOException e) {
            System.err.println("Erro ao processar cliente: " + e.getMessage());
        }
    }
    private static double calcularUsoCpu(long[] prevTicks, long[] postTicks) {
        long user = postTicks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long nice = postTicks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long sys = postTicks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long idle = postTicks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long iowait = postTicks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long irq = postTicks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = postTicks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = postTicks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];

        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
        long totalUsedCpu = totalCpu - idle - iowait;

        return (double) totalUsedCpu / totalCpu;
    }
    
    private static void salvarArquivoCSV(DataInputStream entradaDados, String nomeArquivo, long fileSize) throws IOException {
        // Define o tamanho do buffer para 20MB
        byte[] buffer = new byte[20 * 1024 * 1024];
        long totalBytesRead = 0;
        int bytesRead;

        try (FileOutputStream fileOutputStream = new FileOutputStream(nomeArquivo)) {
            // Continue lendo até que todo o arquivo seja recebido
            while (totalBytesRead < fileSize && (bytesRead = entradaDados.read(buffer, 0, Math.min(buffer.length, (int)(fileSize - totalBytesRead)))) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
        }

        System.out.println(nomeArquivo + " salvo com sucesso.");
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
}
