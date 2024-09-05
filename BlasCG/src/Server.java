import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
    private static final String SERVER_IP = "localhost";

    
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
}
