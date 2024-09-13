import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.ejml.data.DMatrixSparseCSC;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;
import org.jblas.MatrixFunctions;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
public class CGNR {
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
		DoubleMatrix H_T = h.transpose();
		DoubleMatrix z = (H_T).mmul(r);
		DoubleMatrix p = z;
		DoubleMatrix w;
		double beta = 0;
		double alp = 0;
		while(erro > 0.0001)
		{
			long startTime, endTime, duration;
			iteracoes++;
			startTime = System.nanoTime();
			w = CalcularW(h, p);
			endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000; 
			System.out.println("CalcularW took: " + duration + " ms");

			
			startTime = System.nanoTime();
			alp = CalcularAlpha(z, w);
			endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000;
			System.out.println("CalcularAlpha took: " + duration + " ms");

			
			startTime = System.nanoTime();
			f = CalcularF(f, alp, p);
			endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000;
			System.out.println("CalcularF took: " + duration + " ms");

			
			DoubleMatrix rAntes = r;
			startTime = System.nanoTime();
			r = CalcularR(r, alp, w);
			endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000;
			System.out.println("CalcularR took: " + duration + " ms");

			
			DoubleMatrix zAntes = z;
			startTime = System.nanoTime();
			z = CalcularZ(H_T, r);
			endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000;
			System.out.println("CalcularZ took: " + duration + " ms");

			
			startTime = System.nanoTime();
			beta = CalcularBeta(z, zAntes);
			endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000;
			System.out.println("CalcularBeta took: " + duration + " ms");

			
			startTime = System.nanoTime();
			p = CalcularP(z, beta, p);
			endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000;
			System.out.println("CalcularP took: " + duration + " ms");

			
			startTime = System.nanoTime();
			erro = Erro(r, rAntes);
			endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000;
			System.out.println("Erro took: " + duration + " ms");
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
	
	private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();


	private static DoubleMatrix CalcularZ(DoubleMatrix H_T, DoubleMatrix r) {
        return (H_T).mmul(r);
    }
	
	private static double CalcularBeta(DoubleMatrix z_i_plus_1, DoubleMatrix z_i) {
        double normZ_i_plus_1_squared = Math.pow((z_i_plus_1).norm2(), 2);
        double normZ_i_squared = Math.pow((z_i).norm2(), 2);
        return normZ_i_plus_1_squared / normZ_i_squared;
    }
	
	private static DoubleMatrix CalcularP(DoubleMatrix z_i_plus_1, double beta_i, DoubleMatrix p_i) {
        return z_i_plus_1.add(p_i.mul(beta_i));
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
	
	
	
	
	
	
	public static void main(String[] args) throws IOException
	{
        int minRows = 8000;
        int maxRows = 56000;
        int minSqrtColumns = 20;
        int maxSqrtColumns = 80;
		DoubleMatrix H = lerCSVParaDoubleMatrix("h1.csv");
		
		
        
		DoubleMatrix g = lerCSVParaDoubleMatrix("g3.csv");
        DoubleMatrix Imagem = Calcular(H, g, 64, 794);
        
        
        
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
    
    
    
    
    
    public static DMatrixSparseCSC convertToSparse(DoubleMatrix matrix) {
        int rows = matrix.rows;
        int cols = matrix.columns;

        
        int nonZeroCount = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix.get(i, j) != 0) {
                    nonZeroCount++;
                }
            }
        }

        
        DMatrixSparseCSC sparseMatrix = new DMatrixSparseCSC(rows, cols, nonZeroCount);

        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double value = matrix.get(i, j);
                if (value != 0) {
                    sparseMatrix.set(i, j, value);
                }
            }
        }

        return sparseMatrix;
    }
    public static DoubleMatrix convertToDense(DMatrixSparseCSC sparseMatrix) {
        int rows = sparseMatrix.numRows;
        int cols = sparseMatrix.numCols;

        
        DoubleMatrix denseMatrix = new DoubleMatrix(rows, cols);

        
        for (int col = 0; col < sparseMatrix.numCols; col++) {
            int idxStart = sparseMatrix.col_idx[col];
            int idxEnd = sparseMatrix.col_idx[col + 1];

            for (int i = idxStart; i < idxEnd; i++) {
                int row = sparseMatrix.nz_rows[i];
                double value = sparseMatrix.nz_values[i];

                
                denseMatrix.put(row, col, value);
            }
        }

        return denseMatrix;
    }
	
	
}

