import org.jblas.DoubleMatrix;
import org.jblas.Eigen;
import org.jblas.MatrixFunctions;

public class CGOperacoes {

	
	public static DoubleMatrix GanSinal(DoubleMatrix g, int N, int S) {
        DoubleMatrix gModificado = DoubleMatrix.zeros(S*N); 
        
        for (int c = 0; c < N; c++) {
            for (int l = 0; l < S; l++) {
            	double gammaL = 100 + (1.0 / 20.0) * (l) * Math.sqrt(l); 
                double glc = g.get(l*c); 
                gModificado.put(l*c, glc * gammaL); 
            }
        }
        
        return gModificado; 
    }
	
	public static double FatorReducao(DoubleMatrix H) {
        DoubleMatrix Ht = H.transpose();
        DoubleMatrix HtH = Ht.mmul(H);
        DoubleMatrix valor = Eigen.symmetricEigenvalues(HtH);
        double Maximo = valor.max();
        double reducao = Math.sqrt(Maximo);
        return reducao;
    }
	
    
	public static double calcularCoeficienteRegularizacao(DoubleMatrix H, DoubleMatrix g) {
        DoubleMatrix Ht = H.transpose();
        DoubleMatrix HtG = Ht.mmul(g);
        DoubleMatrix absHtG = MatrixFunctions.abs(HtG);
        double maximo = absHtG.max();
        double lambda = maximo * 0.10;
        return lambda;
    }
	
    public static double Erro(DoubleMatrix a, DoubleMatrix b) {
        double N = a.norm2();
        double C = b.norm2();
        double erro = N - C;
        return Math.abs(erro);
    }
    
    private static double[][] calcularGanhoDeSinal(int N, int S) {
        double[][] gamma = new double[S][N];
        
        for (int l = 0; l < S; l++) {
            double gammaL = 100 + (1.0 / 20.0) * (l + 1) * Math.sqrt(l + 1);  
            for (int c = 0; c < N; c++) {
                gamma[l][c] = gammaL;
            }
        }
        return gamma;
    }
    
    private static DoubleMatrix aplicarGanho(DoubleMatrix g, double[][] gamma) {
        int rows = g.rows;
        int columns = g.columns;
        DoubleMatrix result = new DoubleMatrix(rows, columns);
        
        for (int l = 0; l < rows; l++) {
            for (int c = 0; c < columns; c++) {
                result.put(l, c, g.get(l, c) * gamma[l][c]);  
            }
        }
        return result;
    }
    
    public static DoubleMatrix GanhoDeSinal(DoubleMatrix g, int N, int S)
    {
    	return aplicarGanho(g, calcularGanhoDeSinal(N, S));
    }
    
    public static DoubleMatrix GanhoSinal(DoubleMatrix g, int N, int S)
    {
        System.out.println("Ganho de sinal");
    	DoubleMatrix aux = DoubleMatrix.zeros(N*S, 1);
    	double gamma = 0;
    	for(int c = 1; c <= N; c++) {
    		for(int l = 1; l <= S; l++) {
    			gamma = g.get(l*c - 1)*(100 + (1/20)*(l)*Math.sqrt(l));
    			aux.put(l*c - 1, gamma);
    		}
    	}
    	return aux;
    }
    
    
    
    public static void main(String[] args) {
        double[][] data = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };

        DoubleMatrix H = new DoubleMatrix(data);
        double c = FatorReducao(H);
        System.out.println("Fator de redução (c) = " + c);
        
        
        double[][] HData = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
            };

        double[] gData = {1, 2, 3};

        H = new DoubleMatrix(HData);
        DoubleMatrix g = new DoubleMatrix(gData);

        double lambda = calcularCoeficienteRegularizacao(H, g);
        System.out.println("Coeficiente de regularização (λ) = " + lambda);
        
        
        
        double[][] gdata = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
            };
            
        DoubleMatrix G = new DoubleMatrix(gdata);
        int S = G.rows;
        int N = G.columns;
       
        
        
        DoubleMatrix gModificado = GanhoDeSinal(G, N, S);
        
        
        System.out.println("Matriz de sinal modificada g':");
        System.out.println(gModificado);
    }
}
