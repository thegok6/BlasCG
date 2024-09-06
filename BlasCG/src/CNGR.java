import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jblas.DoubleMatrix;
import org.jblas.Eigen;
import org.jblas.MatrixFunctions;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
public class CNGR {
	private static int iteracoes = 0;
	private static long memoriaMediaUsada = 0;
	public static DoubleMatrix Calcular(DoubleMatrix h, DoubleMatrix g,int n,int ganho)
	{
		
		Runtime runtime = Runtime.getRuntime();
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        List<Long> leiturasDeMemoria = new ArrayList<>();
		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		memoriaMediaUsada = 0;
		iteracoes = 0;
		double erro = 1.0;
		if(ganho != 0)
		g = CGOperacoes.GanhoSinal(g, n, ganho);
		DoubleMatrix f = DoubleMatrix.zeros(h.columns);
		DoubleMatrix r = g.sub(h.mmul(f));
		DoubleMatrix z = (h.transpose()).mmul(r);
		DoubleMatrix p = z;
		DoubleMatrix w;
		double beta = 0;
		double alp = 0;
		while(erro > 0.0001)
		{
			iteracoes++;
			w = CalcularW(h, p);
			alp = CalcularAlpha(z, w);
			f = CalcularF(f, alp, p);
			DoubleMatrix rAntes = r;
			r = CalcularR(r, alp, w);
			DoubleMatrix zAntes = z;
			z = CalcularZ(h, r);
			beta = CalcularBeta(z,zAntes);
			p = CalcularP(z, beta, p);
			erro = Erro(r, rAntes);
            long memoriaDisponivel = memory.getAvailable();
            long memoriaUsada = runtime.totalMemory() - runtime.freeMemory();
            leiturasDeMemoria.add(memoriaUsada);
            System.out.println(erro);
		}
        memoriaMediaUsada = calcularMediaDeMemoria(leiturasDeMemoria);
		System.out.println("Interacoes: " + iteracoes);
		return f;
	}
	
    public static double Erro(DoubleMatrix a, DoubleMatrix b) {
        double N = a.norm2();
        double C = b.norm2();
        double erro = N - C;
        return Math.abs(erro);
    }
    public static int getI()
    {return iteracoes;}
    
    public static long getCPU()
    {return memoriaMediaUsada;}
		
	
	private static double CalcularAlpha(DoubleMatrix z_i, DoubleMatrix w_i) {
        double Z = Math.pow(z_i.norm2(), 2);
        double W = Math.pow(w_i.norm2(), 2);
        return Z / W;
    }
	
	private static DoubleMatrix CalcularW(DoubleMatrix H, DoubleMatrix p_i) {
        return H.mmul(p_i);
    }
	
	private static DoubleMatrix CalcularF(DoubleMatrix f_i, double alpha_i, DoubleMatrix p_i) {
        return f_i.add(p_i.mul(alpha_i));
    }
	
	private static DoubleMatrix CalcularR(DoubleMatrix r_i, double alpha_i, DoubleMatrix w_i) {
        return r_i.sub(w_i.mul(alpha_i));
    }
	
	private static DoubleMatrix CalcularZ(DoubleMatrix H_T, DoubleMatrix r) {
        return (H_T.transpose()).mmul(r);
    }
	
	private static double CalcularBeta(DoubleMatrix z_i_plus_1, DoubleMatrix z_i) {
        double normZ_i_plus_1_squared = Math.pow((z_i_plus_1).norm2(), 2);
        double normZ_i_squared = Math.pow((z_i).norm2(), 2);
        return normZ_i_plus_1_squared / normZ_i_squared;
    }
	
	private static DoubleMatrix CalcularP(DoubleMatrix z_i_plus_1, double beta_i, DoubleMatrix p_i) {
        DoubleMatrix betaTimesP_i = p_i.mul(beta_i);
        return z_i_plus_1.add(betaTimesP_i);
    }
	
	
	
	
	
    public static DoubleMatrix gerarModeloAleatorio(int minRows, int maxRows, int minSqrtColumns, int maxSqrtColumns) {
        Random random = new Random();
        int rows = random.nextInt(maxRows - minRows + 1) + minRows;
        int sqrtColumns = random.nextInt(maxSqrtColumns - minSqrtColumns + 1) + minSqrtColumns;
        int columns = sqrtColumns * sqrtColumns;
        DoubleMatrix modelo = DoubleMatrix.zeros(rows, columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (random.nextDouble() < 0.04) {
                    modelo.put(i, j, random.nextDouble() * 1e-6);
                }
            }
        }
        
        return modelo;
    }
	
	
	
	
	
	
	/*public static void main(String[] args)
	{
        int minRows = 8000;
        int maxRows = 56000;
        int minSqrtColumns = 20;
        int maxSqrtColumns = 80;
		DoubleMatrix H = lerCSVParaDoubleMatrix("h1.csv");
		
		
        
		DoubleMatrix g = lerCSVParaDoubleMatrix("g2.csv");
        //DoubleMatrix Imagem = Calcular(H, g);
        ImageGenerator.gerarImagem(Imagem, "teste.png");
        salvarEmCSV(Imagem, "texto.csv");
        
	}
	*/
	
	
	
	
	
	
	
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

	
	public static void salvarEmCSV(DoubleMatrix matriz, String nomeArquivo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) {
            for (int i = 0; i < matriz.rows; i++) {
                StringBuilder linha = new StringBuilder();
                for (int j = 0; j < matriz.columns; j++) {
                    linha.append(matriz.get(i, j));
                    if (j < matriz.columns - 1) {
                        linha.append(",");  
                    }
                }
                writer.write(linha.toString());
                writer.newLine(); 
            }
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }
	
	


    
    public static long calcularMediaDeMemoria(List<Long> leiturasDeMemoria) {
        long soma = 0;
        for (long leitura : leiturasDeMemoria) {
            soma += leitura;
        }
        return soma / leiturasDeMemoria.size(); 
    }
	
	
}

