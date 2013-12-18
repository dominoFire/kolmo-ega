package org.perez.kolmoEGA;

/**
 * Implementacion de la subsecuencia comun mas grande 
 * de la clase Análisis de Algoritmos
 * @author davidr
 */
public class LCS 
{
    private static final int UP = 1;
    private static final int LEFT = 2;
    private static final int UL = 3;
    
    /**
     * Genera una cadena aleatoria dfe longitud n (en un arreglo de caracteres) 
     * a partir de un alfabeto dado
     * @param n Número caracteres en la cadena aleaotria
     * @param alfabeto El conjunto de caracteres para generar la cadena
     * @return La cadena generada aleatoriamente 
     */
    public static char[] generaCadena(int n, char[] alfabeto)
    {
        char[] cadena = new char[n];
        for(int i = 0; i < n; i++) {
            cadena[i] = alfabeto[(int)(Math.random()*10)%alfabeto.length];
        }
        return cadena;
    }
    
    public static int backtrackLCS( int[][]b, char[] x, int i, int j)
    {
        int r = 0;
        if(i != 0 && j != 0) {
            if(b[i][j] == UL) {
                r = backtrackLCS(b,x,i-1,j-1);
                //System.out.print(x[i-1]);
            }
            else if(b[i][j] == UP)
                r = backtrackLCS(b,x,i-1,j) + 1;
            else
                r = backtrackLCS(b,x,i,j-1) + 1;
        }
        return r;
    }
    public static int LCSAlgorithm(char[] x, char[] y)
    {
        int m, n;
        m = x.length;
        n = y.length;
        int[][] b = new int[m+1][n+1];
        int[][] c = new int[m+1][n+1];
        for(int i = 0; i <= m; i++) {
            c[i][0] = 0;
            b[i][0] = UP;
        }
        for(int j = 0; j <= n; j++) {
            c[0][j] = 0;
            b[0][j] = LEFT;
        }
        for (int i = 1; i<= m; i++) {
            for(int j = 1; j <= n; j++) {
                if(x[i-1] == y[j-1]) {
                    c[i][j] = c[i-1][j-1] +1;
                    b[i][j] = UL;
                }
                else if(c[i-1][j] >= c[i][j-1]) {
                    c[i][j] = c[i-1][j];
                    b[i][j] = UP;
                }
                else {
                    c[i][j] = c[i][j-1];
                    b[i][j] = LEFT;
                }
            }
        }
        
        //Hasta aquí termina el algoritmo de la página 394
        /*Ahora implementamos el algoritmo de la página 395 para obtener 
         * la cadena correspondiente al LCS
         * para esto, usamos una función auxiliar llamada backtrackLCS
         */
        //System.out.print("El resultado es: " );
        return backtrackLCS(b,x,m,n);       
        //System.out.println();
        
        //return c[m][n];
    }
    
    
}
