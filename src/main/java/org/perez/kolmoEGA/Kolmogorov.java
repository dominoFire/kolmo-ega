package org.perez.kolmoEGA;

/* ESTE PROGRAMA TRATA DE ENCONTRAR LA MÁQUINA DE TURING QUE ESCRIBA EN UNA
 * CINTA ORIGINALMENTE LLENA DE CEROS UNA CADENA IGUAL A LA QUE SE LE PROPORCIONA
 * EN UN ARCHIVO BINARIO-ASCII
 */

import java.io.*;
import java.util.Random;
import java.util.Scanner;

class Kolmogorov {
    /* ***********************************
     * PARAMETERS
     * *********************************** */

    /**
     * Longitud del genoma
     */
    static int LG = 1024;
    /**
     * Tamaño de la Elite
     */
    static int EliteSize = 10;

    // ************************************
    /**
     * Empty Working Tape
     */
    static String EWT;
    static int P, N, TRS, N_2, L_2, FN = 1, G, B2M, Nx2, iTmp;
    static String Resp, TT;
    /**
     * Working Tape Length
     */
    static int WTL;
    static double Pc, Pm;
    static double fTmp, W; // WW --> Ponderación para Solos,Pares,Tríadas,Cuartetas
    static double BestSingleMatches = -1;
    static double BestFitness = -1;
    static double TTLen;
    static String BestTapeMatch = "";
    static int root;
    static Random RandN;

    //Parámetros del EGA en los que se busca
    static int maxN = 250, minN = 1;		// Número de individuos
    static int maxT = 5000000, minT = 1;        // Número de transiciones
    static int maxL = 50000, minL = 1;		// Longitud de la cinta
    static double maxPc = 1f, minPc = .01f;	// Probabilidad de cruza
    static double maxPm = 1f, minPm = .001f;	// Probabilidad de mutación
    static int maxG = 10000, minG = 1;		// Número de generaciones
    static double maxW = 1, minW = 0;		// Valores de W

    //Arreglos para buardar
    public static String genoma[];
    public static double fitness[];

    //Objetos para lectura
    public static BufferedReader Fbr, Kbr, Tbr;

    /**
     * @param BR El objeto BufferedReader desde donde va a leer
     * @return La cadena hasta el primer caracter TAB encontrado, excluyendo
     * @throws Exception Si ocurre algún error
     */
    public static String readUntilTab(BufferedReader BR) throws Exception {
        String Dato = BR.readLine();
        for (int i = 0; i < Dato.length(); i++) {
            if (Dato.substring(i, i + 1).equals("\t")) {
                return Dato.substring(0, i);
            }
            //endIf
        }//endFor
        System.out.println("No se encontro el tabulador");
        return "";
    }//endLHT

    public static void CreaParams() throws Exception {
        try { //para checar si existe el archivo
            Fbr = new BufferedReader(new InputStreamReader(new FileInputStream(new File("AGParams.txt"))));
        }//endTry
        catch (Exception e) {
            PrintStream Fps = new PrintStream(new FileOutputStream(new File("AGParams.txt")));
            Fps.println("200\t\t1) Individuos");
            Fps.println("50000\t\t2) Numero de transiciones");
            Fps.println("1000\t\t3) Longitud de la cinta");
            Fps.println("0.900\t\t4) Pc");
            Fps.println("0.010\t\t5) Pm");
            Fps.println("1000\t\t6) Generaciones");
            Fps.println("0.5\t\t7) Ponderación");
        }//endCatch
    }//endCreaParams

    public static void GetParams() throws Exception {
        Fbr = new BufferedReader(new InputStreamReader(new FileInputStream(new File("AGParams.txt"))));
        N = Integer.parseInt(readUntilTab(Fbr));			// 1) Individuos
        TRS = Integer.parseInt(readUntilTab(Fbr));			// 2) Transiciones
        WTL = Integer.parseInt(readUntilTab(Fbr));			// 3) Long. de la cinta
        Tbr = new BufferedReader(new InputStreamReader(new FileInputStream(new File("TT.txt"))));
        TT = Tbr.readLine();						// Cinta destino (Target Tape)
        TTLen = TT.length();						// Longitud de la cinta deseada
        Pc = Double.valueOf(readUntilTab(Fbr)).floatValue();	// 4) Pc
        Pm = Double.valueOf(readUntilTab(Fbr)).floatValue();	// 5) Pm
        G = Integer.parseInt(readUntilTab(Fbr));			// 6) Generaciones
        W = Double.valueOf(readUntilTab(Fbr)).floatValue();	// 7) Ponderación
    }//endGetParams

    public static void DispParams() throws Exception {
        System.out.println();
        System.out.println("1) Numero de individuos:    " + N);
        System.out.println("2) Numero de transiciones:  " + TRS);
        System.out.println("3) Long. de la cinta	    " + WTL);
        System.out.printf("4) Prob. de cruzamiento:    %8.6f\n", Pc);
        System.out.printf("5) Prob. de mutacion:       %8.6f\n", Pm);
        System.out.println("6) Numero de generaciones:  " + G);
        System.out.printf("7) Factor de Ponderacion:   %8.6f\n", W);
    }//endDispParams

    public static void ModiParams() throws Exception {
        Kbr = new BufferedReader(new InputStreamReader(System.in));
        String Resp;
        while (true) {
            CalcParams();
            DispParams();
            System.out.print("\nModificar (S/N)? ");
            Resp = Kbr.readLine().toUpperCase();
            if (!Resp.equals("S") & !Resp.equals("N")) {
                continue;
            }
            if (Resp.equals("N")) {
                return;
            }
            if (Resp.equals("S")) {
                while (true) {
                    System.out.print("Opcion No:       ");
                    int Opt;
                    try {
                        Opt = Integer.parseInt(Kbr.readLine());
                    } catch (Exception e) {
                        continue;
                    }
                    if (Opt < 1 | Opt > 7) {
                        continue;
                    }
                    System.out.print("Nuevo valor:     ");
                    iTmp = 1;
                    fTmp = 1;
                    try {
                        if (Opt == 4 | Opt == 5 | Opt == 7) {
                            fTmp = Double.valueOf(Kbr.readLine()).floatValue();
                        } else {
                            iTmp = Integer.parseInt(Kbr.readLine());
                        }
                    } catch (Exception e) {
                        continue;
                    }
                    boolean OK = true;
                    switch (Opt) {
                        case 1: {
                            N = iTmp;
                            if (N < minN | N > maxN) {
                                OK = false;
                            }
                            break;
                        }
                        case 2: {
                            TRS = iTmp;
                            if (TRS < minT | TRS > maxT) {
                                OK = false;
                            }
                            break;
                        }
                        case 3: {
                            WTL = iTmp;
                            if (WTL < minL | WTL > maxL) {
                                OK = false;
                            }
                            break;
                        }
                        case 4: {
                            Pc = fTmp;
                            if (Pc < minPc | Pc > maxPc) {
                                OK = false;
                            }
                            break;
                        }
                        case 5: {
                            Pm = fTmp;
                            if (Pm < minPm | Pm > maxPm) {
                                OK = false;
                            }
                            break;
                        }
                        case 6: {
                            G = iTmp;
                            if (G < minG | G > maxG) {
                                OK = false;
                            }
                            break;
                        }
                        case 7: {
                            W = fTmp;
                            if (W < minW | W > maxW) {
                                OK = false;
                            }
                            break;
                        }
                    }//endSwitch
                    if (OK) {
                        break;
                    }
                    System.out.println("Error en la opcion # " + Opt);
                }//endWhile
            }//endIf
        }//endWhile
    }//endModiParams

    public static void CalcParams() {
        N_2 = N / 2;
        Nx2 = N * 2;
        genoma = new String[Nx2];
        fitness = new double[Nx2];
        L_2 = LG / 2;
        B2M = (int) ((double) N * (double) LG * Pm);		//Bits to Mutate
    }//endCalcParams

    public static void UpdateParams() throws Exception {
        PrintStream Fps = new PrintStream(new FileOutputStream(new File("AGParams.txt")));
        Fps.println(N + "\t\t1) Individuos");
        Fps.println(TRS + "\t\t2) Número de transiciones");
        Fps.println(WTL + "\t\t3) Longitud de la Cinta");
        Fps.printf("%8.6f\t\t4)Pc", Pc);
        Fps.println();
        Fps.printf("%8.6f\t\t5)Pm", Pm);
        Fps.println();
        Fps.println(G + "\t\t6) Generaciones");
        Fps.printf("%8.6f\t\t7) W", W);
        Fps.println();
    }//endUpdateParams

    public static void PoblacionInicial(double fitness[], String genoma[]) throws Exception {
        /*
         *Genera Nx2 individuos aleatoriamente
         */
        for (int i = 0; i < N; i++) {
            genoma[i] = "";
            for (int j = 0; j < LG; j++) {
                if (RandN.nextFloat() < 0.5) {
                    genoma[i] = genoma[i].concat("0");
                } else {
                    genoma[i] = genoma[i].concat("1");
                }
                //endIf
            }//endFor
        }//endFor
    }//endPoblacionInicial

    /**
     * Calcula los fitnes del algoritmo en base al longest common subsequence
     * @param fitness Arreglo de doubles en donde pondrá los resultados
     * @param genoma genomas que ocupa
     * @throws Exception si la simulación arroja algún error
     */
    public static void EvaluaLCS(double fitness[], String genoma[]) throws Exception
    {
        String outputTape;
        int lcslength;
        int ttl = TT.length();
        int HP = EWT.length() / 2;
        String subtt;
        for(int i=0; i<N; i++) {
            outputTape = UTM_AG.OutTape(genoma[i], TRS, EWT);
            
            if(outputTape!=null && outputTape.length()!=0) {
                subtt = outputTape.substring(HP, HP + ttl);
                lcslength = LCS.LCSAlgorithm(subtt.toCharArray(), TT.toCharArray());
            } else {
                subtt = null;
                lcslength = TT.length();
            }
            fitness[i] = 1.0/(0.0001+(double)lcslength);
            if (fitness[i] > BestFitness) {
                BestFitness = fitness[i];
                BestTapeMatch = outputTape;
            }
            BestSingleMatches = Math.max(BestSingleMatches, getSingleMatches(subtt, TT));
        }
    }   
    
    public static void EvaluaSubstring(double fitness[], String genoma[])
            throws Exception
    {
        String outputTape;
        int matches;
        int maxMatches = 0;
        for(int i=0; i<N; i++) {
            outputTape = UTM_AG.OutTape(genoma[i], TRS, EWT);
            if(outputTape!=null)
                for(int j=0; j<outputTape.length()-TT.length(); j++) {
                    matches = getSingleMatches(TT, outputTape.substring(i, i+TT.length()));
                    maxMatches = Math.max(matches, maxMatches);
                    fitness[i] = maxMatches;
                    if (fitness[i] > BestFitness) {
                        BestFitness = fitness[i];
                        BestTapeMatch = outputTape.substring(i, i+TT.length());
                    }
                    BestSingleMatches = Math.max(BestSingleMatches, maxMatches);
                }
            else {
                maxMatches = 0;
                fitness[i] = maxMatches;
                if (fitness[i] > BestFitness) {
                    BestFitness = fitness[i];
                    BestTapeMatch = "";
                }
                BestSingleMatches = Math.max(BestSingleMatches, maxMatches);
            }
        }
    }
    
    private static int getSingleMatches(String a, String b)
    {
        if(a==null || b==null || a.length()!=b.length())
            return 0;
       
        int t = a.length();
        int r = 0;
        for(int i=0; i<t; i++) 
            if(a.charAt(i)==b.charAt(i))
                r++;
        
        return r;
    }

    public static void EvaluaTodoTodo(double fitness[], String genoma[]) throws Exception {
        double WF = 1 + W;
        String outputTape, subtt, maxtt;
        int[] coinc;
        double f = 0, maxf;
        int cerosIzq, cerosDer;
        
        for(int i=0; i<N; i++) {
            outputTape = UTM_AG.OutTape(genoma[i], TRS, EWT);
            f = 0;
            maxf = 0;
            subtt = "";
            maxtt = "";
            if(outputTape!=null && outputTape.length()!=0) {
                for(int j=0; j<outputTape.length() - TT.length(); j++) {
                    subtt = outputTape.substring(j, j + TT.length());
                    coinc = coincidencias(TT, subtt);
                    cerosIzq = cuenta(outputTape.substring(0, j), '0');
                    cerosDer = cuenta(outputTape.substring(j+TT.length()+1), '0');
                    //coeficiente
                    for(int x=2; x<coinc.length; x++) 
                        f += coinc[x] * Math.pow(WF, x-1);
                    f += coinc[1] + cerosIzq + cerosDer; //no va ponderado
                    if(f>maxf){
                        maxtt = subtt;
                        maxf = f;
                    }
                    BestSingleMatches = Math.max(BestSingleMatches, coinc[1]);
                }
            }
            fitness[i] = maxf;
            if (fitness[i] > BestFitness) {
                BestFitness = fitness[i];
                BestTapeMatch = maxtt;
            }//endIf
        }
    }
    
    
    public static int cuenta(String a, char c) {
        int r = 0;
        if(a!=null)
            for(int i=0; i<a.length(); i++)
                if(a.charAt(i)==c)
                    r++;
        return r;
    }
    
    public static void EvaluaTodo(double fitness[], String genoma[]) throws Exception {
        double WF = 1 + W;
        String outputTape, subtt;
        int[] coinc;
        double f = 0;
        
        for(int i=0; i<N; i++) {
            outputTape = UTM_AG.OutTape(genoma[i], TRS, EWT);
            f = 0;
            subtt = "";
            if(outputTape!=null && outputTape.length()!=0) {
                subtt = outputTape.substring(outputTape.length()/2, 
                                            outputTape.length()/2 + TT.length());
                coinc = coincidencias(TT, subtt);
                for(int x=2; x<coinc.length; x++) 
                    f = (f + coinc[x]) * WF;
                f += coinc[1]; //no va ponderado
                BestSingleMatches = Math.max(BestSingleMatches, coinc[1]);
            }
            fitness[i] = f;
            if (fitness[i] > BestFitness) {
                BestFitness = fitness[i];
                BestTapeMatch = subtt;
            }//endIf
        }
    }
    
    public static int[] coincidencias(String a, String b) {
        if(a==null || b==null || a.length()!=b.length())
            return null;
        int t = a.length();
        int[] ret = new int[t+1];
        
        for(int i=0; i<t; i++) {
            for(int j=1; i+j<=t; j++) {
                if( a.substring(i, i+j).equals(b.substring(i, i+j)) ) 
                    ret[j]++;
            }
        }
        
        return ret;
    }
    
    
    public static void Evalua(double fitness[], String genoma[]) throws Exception {
        String OutTape;
        double Solos, Pares, Triadas, Cuartetas;
        boolean FF2, FF3, FF4;
        double WF = 1 + W;
        int HP, TTLen;
        for (int i = 0; i < N; i++) {
            //La cinta inicial está Llena de 0s
            OutTape = UTM_AG.OutTape(genoma[i], TRS, EWT);
            Solos = 0;
            Pares = 0;
            Triadas = 0;
            Cuartetas = 0;
            FF2 = true;
            FF3 = true;
            FF4 = true;
            HP = WTL / 2;
            TTLen = TT.length();
            if (OutTape.length() != 0) {
                int k = 0;
                for (int j = HP; j < HP + TTLen; j++) {
                    if (OutTape.substring(j, j + 1).equals(TT.substring(k, k + 1))) {
                        Solos++;
                        if (Solos > BestSingleMatches) {
                            BestSingleMatches = Solos;
                        }
                    }//endIf
                    if (FF2) {
                        try {
                            if (OutTape.substring(j, j + 2).equals(TT.substring(k, k + 2))) {
                                Pares++;
                            }
                        } catch (Exception e) {
                            FF2 = false;
                        }
                    }//endif
                    if (FF3) {
                        try {
                            if (OutTape.substring(j, j + 3).equals(TT.substring(k, k + 3))) {
                                Triadas++;
                            }
                        } catch (Exception e) {
                            FF3 = false;
                        }
                    }//endIf
                    if (FF4) {
                        try {
                            if (OutTape.substring(j, j + 4).equals(TT.substring(k, k + 4))) {
                                Cuartetas++;
                            }
                        } catch (Exception e) {
                            FF4 = false;
                        }
                    }//endIf
                    k++;
                }//endFor
                fitness[i] = Solos + WF * (Pares + WF * (Triadas + WF * Cuartetas));
//			System.out.println("Fitness="+fitness[i]);
                if (fitness[i] > BestFitness) {
                    BestFitness = fitness[i];
                    BestTapeMatch = OutTape.substring(HP, HP + TTLen);
                }//endIf
            } else {
                fitness[i] = 0d;
            }//endIf
        }//endFor
        return;
    }//endEvalua

    public static void Duplica(double fitness[], String genoma[]) {
        for (int i = 0; i < N; i++) {
            genoma[N + i] = genoma[i];
            fitness[N + i] = fitness[i];
        }//endFor
    }//endCopia

    public static void Cruza(String genoma[]) {
        int N_i, P;
        String LI, MI, RI, LN, MN, RN;
        for (int i = 0; i < N_2; i++) {
            if (RandN.nextFloat() > Pc) {
                continue;
            }
            N_i = N - i - 1;
            P = -1;
            while (P < 0 | P >= L_2) {
                P = (int) (RandN.nextFloat() * L_2);
            }
            LI = genoma[i].substring(0, P);
            MI = genoma[i].substring(P, P + L_2);
            RI = genoma[i].substring(P + L_2);
            LN = genoma[N_i].substring(0, P);
            MN = genoma[N_i].substring(P, P + L_2);
            RN = genoma[N_i].substring(P + L_2);
            genoma[i] = LI.concat(MN).concat(RI);
            genoma[N_i] = LN.concat(MI).concat(RN);
        }//endFor
    }//endCruza

    public static void Muta(String genoma[]) throws Exception {
        int nInd, nBit;
        for (int i = 1; i <= B2M; i++) {
            nInd = -1;
            while (nInd < 0 | nInd >= N) {
                nInd = (int) (RandN.nextFloat() * N);
            }
            nBit = -1;
            while (nBit < 0 | nBit >= LG) {
                nBit = (int) (RandN.nextFloat() * LG);
            }
            /*
             *		** Mutation **
             */
            String mBit = "0";
            String G = genoma[nInd];
            if (nBit != 0 & nBit != LG - 1) {
                if (G.substring(nBit, nBit + 1).equals("0")) {
                    mBit = "1";
                }
                genoma[nInd] = G.substring(0, nBit).concat(mBit).concat(G.substring(nBit + 1));
                continue;
            }//endif
            if (nBit == 0) {
                if (G.substring(0, 1).equals("0")) {
                    mBit = "1";
                }
                genoma[nInd] = mBit.concat(G.substring(1));
                continue;
            }//endif
            //if (nBit==LG-1){
            if (G.substring(LG - 1).equals("0")) {
                mBit = "1";
            }
            genoma[nInd] = G.substring(0, LG - 1).concat(mBit);
            //}//endIf
        }//endFor
    }//endMuta

    /*		Selecciona los mejores N individuos
     *
     */
    public static void Selecciona(double fitness[], String genoma[]) {
        double fitnessOfBest, fTmp;
        String sTmp;
        int indexOfBest;
        for (int i = 0; i < N; i++) {
            fitnessOfBest = fitness[i];
            indexOfBest = i;
            for (int j = i + 1; j < Nx2; j++) {
                if (fitness[j] > fitnessOfBest) {
                    fitnessOfBest = fitness[j];
                    indexOfBest = j;
                }//endIf
            }//endFor
            if (indexOfBest != i) {
                sTmp = genoma[i];
                genoma[i] = genoma[indexOfBest];
                genoma[indexOfBest] = sTmp;
                fTmp = fitness[i];
                fitness[i] = fitness[indexOfBest];
                fitness[indexOfBest] = fTmp;
            }//endIf
        }//endFor
        return;
    }//endSelecciona

    /**
     * De los de la elite, busca a la maquina con menos estados
     * @param Elite
     * @throws Exception 
     */
    public static void ResultadosDeLaCorrida(String Elite[]) throws Exception {
        /*
         *		EL MEJOR AJUSTE
         */
        System.out.printf("\n\nAjuste maximo: %15.7f\n", fitness[0]);
        /*
         *		COMPLEJIDAD
         */
        String Tape = EWT;
        int EstadosTM = 10000, EstadosTM_i, BestTMNdx = 0;
        for (int i = 0; i < EliteSize; i++) {
            Tape = EWT;
            EstadosTM_i = UTM_AG.Complejidad(Elite[i], TRS, EWT, false);
            if (EstadosTM_i < EstadosTM) {
                EstadosTM = EstadosTM_i;
                BestTMNdx = i;
            }//endIf
        }//endFor
/*
         *		LA MEJOR MÁQUINA
         */
        PrintStream TgtTMps = new PrintStream(new FileOutputStream(new File("TargetTM.txt")));
        TgtTMps.println(Elite[BestTMNdx]);
        System.out.println("La mejor MT encontrada esta en \"TargetTM.txt\"\n");
        EstadosTM = UTM_AG.Complejidad(Elite[BestTMNdx], TRS, EWT, true);
        /*
         *		LA MEJOR CINTA DESTINO
         */
        
        System.out.println("La mejor cinta encontrada esta en \"TargetTape.txt\"\n");
        /*
         *		COINCIDENCIAS
         */
        System.out.println("\na) Numero de coincidencias: " + BestSingleMatches);
        double Ratio = (double) BestSingleMatches / TTLen;
        System.out.println("b) Longitud de la cinta de datos: " + TTLen + "\n");
        System.out.printf("\t===> Tasa de coincidencias: %6.4f\n\n\n", Ratio);
        System.out.println("Estados en la Maquina de Turing: " + EstadosTM);
        System.out.println("\n\t******************************************");
        System.out.printf("\t*  La complejidad de Kolmogorov: %7.0f *\n", (float) (EstadosTM * 16));
        System.out.println("\t**********************************|********\n");
        return;
    }//endMethod

    /**
     * Método main del programa que calcula la complejidad de Kolmogorov
     * de un mensaje
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        Scanner jin = new Scanner(System.in);
        while (true) {
            while (true) {
                System.out.println("Deme la raiz del generador de numeros aleatorios");
                try {
                    root = jin.nextInt();
                    break;
                } catch (Exception e) {
                    System.out.println("Debe ser entero!\n");
                }
            }//endWhile
            RandN = new Random(root);
            LeeDatos.Cinta(); // Rutina externa
            CreaParams();     //Crea archivo si no existe
            GetParams();      //Lee parametros de archivo
            ModiParams();     //Modifica valores
            CalcParams();     //Calcula parametros
            UpdateParams();   //Graba en archivo

            //EMPIEZA EL ALGORITMO GENETICO
            EWT = "0";
            for (int i = 1; i < WTL; i++) {
                EWT = EWT + "0";
            }
            PoblacionInicial(fitness, genoma);  //Genera la poblacion inicial
            Evalua(fitness, genoma);
            int First = 1, Last = G;            //Evalua los primeros N
            int Optimo = 0;
            boolean BestFound = false;
            String[] Elite = new String[EliteSize];
            while (true) {
                BestSingleMatches = -1;
                BestFitness = -1;
                BestTapeMatch = "";
                for (int i = First; i < Last; i++) {
                    Duplica(fitness, genoma); //Duplica los primeros N
                    Cruza(genoma); //Cruza los primeros N
                    Muta(genoma); //Muta los primeros N
                    Evalua(fitness, genoma); //Evalua los primeros N
                    //EvaluaLCS(fitness, genoma);
                    //EvaluaSubstring(fitness, genoma);
                    //EvaluaTodo(fitness, genoma);
                    //EvaluaTodoTodo(fitness, genoma);
                    if (BestSingleMatches == TTLen) {
                        BestFound = true;
                        Elite[Optimo] = genoma[0];
                        Optimo++;
                        if (Optimo == EliteSize) {
                            break; //Termina si hay <EliteSize> ajustes perfectos
                        }
                    }//endIf
                    Selecciona(fitness, genoma); //Selecciona los mejores N
                    System.out.printf("GEN  %8.0f\tMatches %8.0f\n", (float) i, (float) BestSingleMatches);
                }//endFor
                if (!BestFound) {
                    for (int i = 0; i < EliteSize; i++) {
                        Elite[i] = genoma[i];
                    }
                    //endFor
                }//Endif
                ResultadosDeLaCorrida(Elite);
                System.out.println("DESEA CONTINUAR LA BUSQUEDA? (S/*)");
                if(!jin.nextLine().toUpperCase().equals("S")) {
                    break;
                }
                Optimo = 0;
                First = First + G;
                Last = Last + G;
            }//endWhile
            System.out.println("\n\nOtra corrida? (S/*)");
            String Resp = jin.nextLine().toLowerCase();
            if (!Resp.equals("S")) {
                break;
            }
        }//endMain
        System.out.println("\n\n*****\t\t\tFIN DE PROGRAMA\t\t\t*****\n\n\n");
        jin.close();
    }//endLoop
} //endClass