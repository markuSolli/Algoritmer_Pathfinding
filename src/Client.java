import java.io.*;

public class Client{
    private static final String[] felt = new String[10];      //Max 10 felt i en linje

    public static void main(String[] args) throws IOException{
        Graph graph = new Graph(readNodes("./noder.txt"),readEdges("./kanter.txt"));

        //Nordkapp, Hattuvaara, Padborg, Florø
        int[] landmarks = {2151398, 4212646, 3264971, 2269305};
        //Trondheim-Tampere
        int[] rute = {6861306, 136963};

        long start = System.nanoTime();
        Result result = Path.dijkstra(graph, rute[0], rute[1]);
        long end = System.nanoTime() - start;
        System.out.println("Dijkstra\n------------");
        printResult(result, end, "./result1.txt");

        File file = new File("./mapinfo.txt");
        int[][] fromLM;
        int[][] toLM;
        if(file.exists()){
            MapInfo mapInfo = readMapInfo(file.toString());
            landmarks = mapInfo.getLandmarks();
            fromLM = mapInfo.getFromLM();
            toLM = mapInfo.getToLM();
        }else{
            fromLM = graph.preprocessFrom(landmarks);
            toLM = graph.preprocessTo(landmarks);
            writeMapInfo(landmarks,fromLM,toLM,file.toString());
        }

        start = System.nanoTime();
        result = Path.alt(graph,landmarks,fromLM,toLM, rute[0], rute[1]);
        end = System.nanoTime() - start;
        System.out.println("\nALT\n------------");
        printResult(result, end, "./result2.txt");

        readIntNodes(graph, "./interessepkt.txt");
        //Røros Hotell
        Node[] points = Path.dijkstraPOI(graph,1419364,4);
        System.out.println("\n10 ladestasjoner nær Røros Hotell");
        for(Node point : points){
            System.out.println(point.getLatitude() + "," + point.getLongitude());
        }
    }

    /**
     * Print ut innholdet til et Resultat-objekt
     * @param result resultatet
     * @param end slutt-noden
     * @param path filbane til hvor veien skal lagres
     */
    private static void printResult(Result result, long end, String path) throws IOException{
        if(result == null){
            System.out.println("Path not found");
        }else{
            writePath(path, result.toString());

            int seconds = result.getEnd().getDistance()/100;
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            seconds = seconds % 60;
            String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            System.out.printf("Runtime:\t\t%,dms\nDriving time:\t%s\nVisited:\t\t%,d\n",
                    end/1000000,time,result.getVisited());
        }
    }

    /**
     * Les inn noder fra en fil
     * @param filepath filbane til node-fil
     * @return et array av noder
     */
    private static Node[] readNodes(String filepath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        String str = br.readLine().strip();
        Node[] nodes = new Node[Integer.parseInt(str)];
        for(int i=0; i<nodes.length; i++){
            str = br.readLine();
            hsplit(str, 3);
            nodes[i] = new Node(Integer.parseInt(felt[0]),Double.parseDouble(felt[1]),
                    Double.parseDouble(felt[2]));
        }
        br.close();
        return nodes;
    }

    /**
     * Les inn kanter fra fil
     * @param filepath filbane til kant-fil
     * @return et array av kanter
     */
    private static Edge[] readEdges(String filepath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        String str = br.readLine().strip();
        Edge[] edges = new Edge[Integer.parseInt(str)];
        for(int i=0; i<edges.length; i++){
            str = br.readLine();
            hsplit(str, 5);
            edges[i] = new Edge(Integer.parseInt(felt[0]),Integer.parseInt(felt[1]),
                    Integer.parseInt(felt[2]));
        }
        br.close();
        return edges;
    }

    private static void hsplit(String linje, int antall) {
        int j = 0;
        int lengde = linje.length();
        for (int i = 0; i < antall; ++i) {
            //Hopp over innledende blanke, finn starten på ordet
            while (linje.charAt(j) <= ' ') ++j;
            int ordstart = j;
            //Finn slutten på ordet, hopp over ikke-blanke
            while (j < lengde && linje.charAt(j) > ' ') ++j;
            felt[i] = linje.substring(ordstart, j);
        }
    }

    /**
     * Skriv en vei til fil
     * @param filepath filbane
     * @param path koordinater som beskriver veien
     */
    private static void writePath(String filepath, String path) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
        bw.write(path);
        bw.close();
    }

    /**
     * Les inn landemerker med tabeller fra fil
     * @param path filbane
     * @return et objekt med landemerker og fra/til tabellene
     */
    private static MapInfo readMapInfo(String path) throws IOException{
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
        int[] landmarks = new int[dis.readByte()];
        for(int i=0; i<landmarks.length; i++){
            landmarks[i] = dis.readInt();
        }
        int tableLen = dis.readInt();
        int[][] fromLM = new int[landmarks.length][tableLen];
        int[][] toLM = new int[landmarks.length][tableLen];
        for(int i=0; i<landmarks.length; i++){
            for(int j=0; j<fromLM[i].length; j++){
                fromLM[i][j] = dis.readInt();
            }
        }
        for(int i=0; i<landmarks.length; i++){
            for(int j=0; j<toLM[i].length; j++){
                toLM[i][j] = dis.readInt();
            }
        }
        dis.close();
        return new MapInfo(landmarks,fromLM,toLM);
    }

    /**
     * Skriv kartinfo til fil for videre bruk
     * @param landmarks landemerker brukt i tabellene
     * @param fromLM distanser fra landemerker til alle noder
     * @param toLM distanser fra alle noder til landemerkene
     * @param path filbane
     */
    private static void writeMapInfo(int[] landmarks, int[][] fromLM, int[][] toLM, String path)
    throws IOException{
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
        dos.writeByte((byte) landmarks.length);
        for(int landmark : landmarks){
            dos.writeInt(landmark);
        }
        dos.writeInt(fromLM[0].length);
        for(int i=0; i<landmarks.length; i++){
            for(int j=0; j<fromLM[i].length; j++){
                dos.writeInt(fromLM[i][j]);
            }
        }
        for(int i=0; i<landmarks.length; i++){
            for(int j=0; j<toLM[i].length; j++){
                dos.writeInt(toLM[i][j]);
            }
        }
        dos.close();
    }

    /**
     * Les info om noder og gi verdier til gitt graf
     * @param graph hvilken graf som skal endres
     * @param filepath filbane til node-info
     */
    private static void readIntNodes(Graph graph, String filepath) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        String str = br.readLine().strip();
        int len = Integer.parseInt(str);
        for(int i=0; i<len; i++){
            str = br.readLine();
            hsplit(str, 2);
            Node node = graph.get(Integer.parseInt(felt[0]));
            node.setType(Integer.parseInt(felt[1]));
        }
        br.close();
    }
}
