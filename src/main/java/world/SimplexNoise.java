package world;

/**
 * Implementazione dell'algoritmo SimplexNoise per generare terreni naturali
 */
public class SimplexNoise {
    private static final int PERM_TABLE_SIZE = 512;
    private static final int PERM_MASK = 255;
    
    private int[] perm = new int[PERM_TABLE_SIZE];
    
    public SimplexNoise(int seed) {
        // Inizializza la tabella di permutazione con un seed
        java.util.Random rand = new java.util.Random(seed);
        for (int i = 0; i < 256; i++) {
            perm[i] = i;
        }
        
        // Mescola la tabella di permutazione
        for (int i = 0; i < 256; i++) {
            int j = rand.nextInt(256);
            int temp = perm[i];
            perm[i] = perm[j];
            perm[j] = temp;
        }
        
        // Duplica per evitare overflow
        for (int i = 0; i < 256; i++) {
            perm[i + 256] = perm[i];
        }
    }
    
    // Gradiente 2D per il noise
    private static double dot(int g[], double x, double y) {
        return g[0] * x + g[1] * y;
    }
    
    // Gradients per Simplex noise
    private static final int[][] grad2 = {
            {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    
    // Funzione principale di Simplex Noise 2D
    public double noise(double xin, double yin) {
        // Costanti di gradiente skew per passare da spazio cartesiano a spazio simplex
        final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
        final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;
        
        // Skewing
        double s = (xin + yin) * F2;
        int i = fastfloor(xin + s);
        int j = fastfloor(yin + s);
        
        // Unskewing
        double t = (i + j) * G2;
        double X0 = i - t;
        double Y0 = j - t;
        double x0 = xin - X0;
        double y0 = yin - Y0;
        
        // Determina quale cella del simplex contiene il punto
        int i1, j1;
        if (x0 > y0) {
            i1 = 1; j1 = 0;
        } else {
            i1 = 0; j1 = 1;
        }
        
        // Calcola le coordinate degli altri vertici del simplex
        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double y2 = y0 - 1.0 + 2.0 * G2;
        
        // Calcola gli indici hash per i tre vertici del simplex
        int ii = i & PERM_MASK;
        int jj = j & PERM_MASK;
        
        // Calcola i contributi di ciascun vertice
        double n0, n1, n2;
        
        double t0 = 0.5 - x0*x0 - y0*y0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            int gi0 = perm[ii + perm[jj]] % 8;
            n0 = t0 * t0 * dot(grad2[gi0], x0, y0);
        }
        
        double t1 = 0.5 - x1*x1 - y1*y1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            int gi1 = perm[ii + i1 + perm[jj + j1]] % 8;
            n1 = t1 * t1 * dot(grad2[gi1], x1, y1);
        }
        
        double t2 = 0.5 - x2*x2 - y2*y2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            int gi2 = perm[ii + 1 + perm[jj + 1]] % 8;
            n2 = t2 * t2 * dot(grad2[gi2], x2, y2);
        }
        
        // Scala per ottenere un range approssimativamente [-1, 1]
        return 70.0 * (n0 + n1 + n2);
    }
    
    private static int fastfloor(double x) {
        int xi = (int)x;
        return x < xi ? xi - 1 : xi;
    }
}
