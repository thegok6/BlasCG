import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jblas.DoubleMatrix;
import org.jblas.Eigen;
import org.jblas.MatrixFunctions;
public class CNGE {
	
    public static DoubleMatrix Calcular(DoubleMatrix h, DoubleMatrix g) {
        int i = 0;
        double erro = 1.0;
        DoubleMatrix f = DoubleMatrix.zeros(h.columns);
        DoubleMatrix r = g.sub(h.mmul(f));
        DoubleMatrix p = (h.transpose()).mmul(r);
        double beta, alpha;

        while (erro > 0.0001) {
            i++;
            DoubleMatrix Hp = h.mmul(p);
            alpha = CalcularAlpha(r, p);
            f = CalcularF(f, alpha, p);
            DoubleMatrix rAntes = r;
            r = CalcularR(r, alpha, Hp);
            DoubleMatrix z = (h.transpose()).mmul(r);
            beta = CalcularBeta(r, rAntes);
            p = CalcularP(z, beta, p);
            erro = Erro(r, rAntes);
            System.out.println("Erro: " + erro);
        }

        System.out.println("Interações: " + i);
        return f;
    }

    public static double Erro(DoubleMatrix a, DoubleMatrix b) {
        double N = a.norm2();
        double C = b.norm2();
        return Math.abs(N - C);
    }

    private static double CalcularAlpha(DoubleMatrix r, DoubleMatrix p) {
        double rTr = r.dot(r);
        double pTp = p.dot(p);
        return rTr / pTp;
    }

    private static DoubleMatrix CalcularF(DoubleMatrix f, double alpha, DoubleMatrix p) {
        return f.add(p.mul(alpha));
    }

    private static DoubleMatrix CalcularR(DoubleMatrix r, double alpha, DoubleMatrix Hp) {
        return r.sub(Hp.mul(alpha));
    }

    private static double CalcularBeta(DoubleMatrix rNovo, DoubleMatrix rAntigo) {
        double rNovoTrNovo = rNovo.dot(rNovo);
        double rAntigoTrAntigo = rAntigo.dot(rAntigo);
        return rNovoTrNovo / rAntigoTrAntigo;
    }

    private static DoubleMatrix CalcularP(DoubleMatrix z, double beta, DoubleMatrix p) {
        return z.add(p.mul(beta));
    }
	
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args)
	{
		DoubleMatrix H = lerCSVParaDoubleMatrix("C:\\Users\\ichib\\OneDrive\\Documents\\Arquivos para backup\\Blas\\h1.csv");
		DoubleMatrix f = DoubleMatrix.rand(H.columns, 1);
        
		DoubleMatrix g = lerCSVParaDoubleMatrix("C:\\Users\\ichib\\OneDrive\\Documents\\Arquivos para backup\\Blas\\g2.csv");
        DoubleMatrix Imagem = Calcular(H, g);
        ImageGenerator.gerarImagem(Imagem, "teste.png");
        salvarEmCSV(Imagem, "texto.csv");
        
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
	
	
}

