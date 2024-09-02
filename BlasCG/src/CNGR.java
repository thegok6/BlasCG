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
public class CNGR {
	
	public static DoubleMatrix Calcular(DoubleMatrix h, DoubleMatrix g)
	{
		int i = 0;
		double erro = 1.0;
		DoubleMatrix f = DoubleMatrix.zeros(h.columns);
		DoubleMatrix r = g.sub(h.mmul(f));
		DoubleMatrix z = (h.transpose()).mmul(r);
		DoubleMatrix p = z;
		DoubleMatrix w;
		double beta = 0;
		double alp = 0;
		while(erro > 0.0001)
		{
			i++;
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
			System.out.println("Erro: " + erro);
		}
		System.out.println("Interacoes: " + i);
		return f;
	}
	
    public static double Erro(DoubleMatrix a, DoubleMatrix b) {
        double N = a.norm2();
        double C = b.norm2();
        double erro = N - C;
        return Math.abs(erro);
    }
		
	
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
	
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args)
	{
		DoubleMatrix H = lerCSVParaDoubleMatrix("C:\\Users\\ichib\\OneDrive\\Documents\\Arquivos para backup\\Blas\\h1.csv");
		DoubleMatrix f = DoubleMatrix.rand(H.columns, 1);
        //DoubleMatrix g = H.mmul(f);
		DoubleMatrix g = lerCSVParaDoubleMatrix("C:\\Users\\ichib\\OneDrive\\Documents\\Arquivos para backup\\Blas\\g2.csv");
        DoubleMatrix Imagem = Calcular(H, g);
        ImageGenerator.gerarImagem(Imagem, "teste.png");
        salvarEmCSV(Imagem, "texto.csv");
        //System.out.println(H);
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

