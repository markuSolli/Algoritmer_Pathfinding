import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Klasse fylt med statiske metoder for søk i grafer
 */
public class Path {

    /**
     * ALT algoritme for korteste vei.
     * @param graph grafen det skal søkes i
     * @param landmarks array med landemerker
     * @param fromLM tabell med distanser fra landemerker til alle nodene
     * @param toLM tabell med distanser fra alle noder til landemerkene
     * @param start start-node
     * @param end slutt-node
     * @return et Resultat-objekt med antall noder besøkt og slutt-noden
     */

    public static Result alt(Graph graph, int[] landmarks, int[][] fromLM, int[][] toLM,
                             int start, int end){
        graph.initialize(start);
        Node current = graph.get(start);
        int visited = 0;
        int dist;

        PriorityQueue<Node> unvisited = new PriorityQueue<>(1000, Comparator.comparingInt(Node::getTotalDist));
        unvisited.add(current);

        while(current != null){
            for(Edge e=current.getEdge(); e != null; e = e.getNext()){
                Node node = graph.get(e.getTo());
                if(!node.isVisited()){
                    if(!node.isFound()){
                        node.setDistToTarget(getDistEstimate(landmarks,fromLM,toLM,node.getIndex(),end));
                        unvisited.add(node);
                        node.setFound(true);
                    }
                    dist = current.getDistance() + e.getWeight();
                    if(dist < node.getDistance()){
                        unvisited.remove(node);
                        node.setDistance(dist);
                        node.setPrev(current);
                        unvisited.add(node);
                    }
                }
            }
            current.setVisited(true);
            visited++;
            if(current.getIndex() == end){ return new Result(current,visited);}
            unvisited.remove(current);
            current = unvisited.poll();
        }

        return null;
    }

    /**
     * Regn ut et estimat på en nodes distanse til mål
     * @param landmarks landemerker å bruke
     * @param fromLM tabell med distanser fra landemerker til alle nodene
     * @param toLM tabell med distanser fra alle noder til landemerkene
     * @param index noden å estimere avstand fra
     * @param end slutt-node å estimere avstand til
     * @return et estimat på avstanden.
     */
    private static int getDistEstimate(int[] landmarks, int[][] fromLM, int[][] toLM, int index, int end){
        int estimate = 0;
        int dist1,dist2;
        for(int i=0; i<landmarks.length; i++){
            dist1 = Math.max(fromLM[i][end] - fromLM[i][index], 0);
            dist2 = toLM[i][index] - toLM[i][end];
            estimate = Math.max(Math.max(dist1, dist2),estimate);
        }
        return estimate;
    }

    /**
     * Dijkstra's algoritme for korteste vei
     * @param graph grafen å søke i
     * @param start start-node
     * @param end slutt-nod
     * @return et Resultat-objekt med antall noder besøkt og slutt-noden
     */
    public static Result dijkstra(Graph graph, int start, int end){
        graph.initialize(start);
        Node current = graph.get(start);
        int visited = 0;

        PriorityQueue<Node> unvisited = new PriorityQueue<>(10000);
        unvisited.add(current);

        while(current != null){
            dijkstra(graph,unvisited,current);
            current.setVisited(true);
            visited++;
            if(current.getIndex() == end){ return new Result(current,visited);}
            unvisited.remove(current);
            current = unvisited.poll();
        }

        return null;
    }

    /**
     * Dijkstra's algoritme for korteste vei på hele grafen.
     * @param graph grafen det søkes i
     * @param start start-node
     */

    static void dijkstra(Graph graph, int start){
        graph.initialize(start);
        Node current = graph.get(start);

        PriorityQueue<Node> unvisited = new PriorityQueue<>(10000);
        unvisited.add(current);

        while(current != null){
            dijkstra(graph,unvisited,current);
            current.setVisited(true);
            unvisited.remove(current);
            current = unvisited.poll();
        }
    }

    /**
     * Dijkstra's algoritme for korteste vei som leter etter de 10 nærmeste nodene av en gitt type.
     * @param graph grafen det søkes i
     * @param start start-node.
     * @param type hvilke noder det letes etter. 2 for bensinstasjon, 4 for ladestasjon.
     * @return et array med de 10 resultatene
     */

    public static Node[] dijkstraPOI(Graph graph, int start, int type){
        Node[] points = new Node[10];
        int i = 0;
        graph.initialize(start);
        Node current = graph.get(start);

        PriorityQueue<Node> unvisited = new PriorityQueue<>(10000);
        unvisited.add(current);

        while(current != null){
            dijkstra(graph,unvisited,current);
            current.setVisited(true);
            if(current.getType() == type){
                points[i] = current;
                i++;
                if(i > 9){ break;}
            }
            unvisited.remove(current);
            current = unvisited.poll();
        }

        return points;
    }

    /**
     * Hjelpe-metode for dijkstra
     * @param graph grafen det søkes i
     * @param unvisited prioritetskøen
     * @param current noden det skal søkes fra
     */
    private static void dijkstra(Graph graph, PriorityQueue<Node> unvisited, Node current){
        int dist;
        for(Edge e=current.getEdge(); e != null; e = e.getNext()){
            Node node = graph.get(e.getTo());
            if(!node.isVisited()){
                if(!node.isFound()){
                    unvisited.add(node);
                    node.setFound(true);
                }
                dist = current.getDistance() + e.getWeight();
                if(dist < node.getDistance()){
                    unvisited.remove(node);
                    node.setDistance(dist);
                    node.setPrev(current);
                    unvisited.add(node);
                }
            }
        }
    }
}

/**
 * Objekt for å holde på et søkeresultat
 */
class Result {
    private final Node end;
    private final int visited;

    Result(Node end, int visited){
        this.end = end;
        this.visited = visited;
    }

    Node getEnd() {
        return end;
    }
    int getVisited() {
        return visited;
    }

    /**
     * Lag String av koordinatene til en vei, traverserer gjennom alle 'prev' nodene til slutt-noden.
     */
    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        Node current = end;
        while(current.getPrev() != null){
            strBuilder.append(current.getLatitude())
                    .append(",").append(current.getLongitude()).append("\n");
            current = current.getPrev();
        }
        return strBuilder.toString();
    }
}

/**
 * Objekt som beskriver en graf med noder og kanter
 */
class Graph {
    private final int INF = 1<<29;
    private final Node[] nodes;
    private final int n;

    Graph(Node[] nodes, Edge[] edges){
        this.nodes = nodes;
        n = edges.length;

        for(Edge edge : edges) {
            nodes[edge.getFrom()].setEdge(edge);
        }
    }

    Node[] getNodes() {
        return nodes;
    }
    Node get(int index){
        return nodes[index];
    }

    /**
     * Gjør grafen klar til et nytt søk, 'restarter' alle verdier til nodene.
     * @param start start-noden til det nye søket. Denne får distanse 0 og found blir sant
     */
    void initialize(int start){
        for(Node node : nodes) {
            node.setPrev(null);
            node.setVisited(false);
            node.setFound(false);
            node.setDistance(INF);
            node.setDistToTarget(0);
        }

        nodes[start].setDistance(0);
        nodes[start].setFound(true);
    }

    /**
     * Lag tabell for distanser til alle noder fra alle landemerker
     * @param landmarks landemerkene å måle fra
     * @return en tabell med [landemerke][node] = distanse
     */

    int[][] preprocessFrom(int[] landmarks){
        int[][] fromLM = new int[landmarks.length][nodes.length];
        for(int i=0; i<landmarks.length; i++){
            Path.dijkstra(this,landmarks[i]);
            for(int j=0; j<nodes.length; j++){
                fromLM[i][j] = nodes[j].getDistance();
            }
        }
        return fromLM;
    }

    /**
     * Lag tabell for distanser til alle landemerker fra alle noder
     * @param landmarks landemerkene å måle fra
     * @return en tabell med [landemerke][node] = distanse
     */

    int[][] preprocessTo(int[] landmarks){
        Graph reverse = this.reverse();
        Node[] revNodes = reverse.getNodes();
        int[][] toLM = new int[landmarks.length][revNodes.length];
        for(int i=0; i<landmarks.length; i++){
            Path.dijkstra(reverse,landmarks[i]);
            for(int j=0; j<revNodes.length; j++){
                toLM[i][j] = revNodes[j].getDistance();
            }
        }
        return toLM;
    }

    /**
     * Lag en kopi av denne grafen og reverser alle kantene
     * @return den reverserte grafen med egne kopier av noder og kanter.
     */
    private Graph reverse(){
        Node[] copy = new Node[nodes.length];
        Edge[] edges = new Edge[n];
        int i = 0;
        for(Node node : nodes){
            for(Edge e = node.getEdge(); e != null; e = e.getNext()){
                edges[i] = new Edge(e.getTo(),e.getFrom(),e.getWeight());
                i++;
            }
            copy[node.getIndex()] = new Node(node.getIndex(),node.getLatitude(),node.getLongitude());
        }
        return new Graph(copy,edges);
    }
}

/**
 * Node i en graf
 */
class Node implements Comparable<Node>{
    private final int i;
    private final double latitude;
    private final double longitude;
    private Edge edge;
    private Node prev;
    private boolean visited;
    private boolean found;
    private int distance;
    private int distToTarget = 0;
    private int type;

    Node(int index, double latitude, double longitude){
        this.i = index;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    int getIndex() {
        return i;
    }
    double getLatitude() {
        return latitude;
    }
    double getLongitude() {
        return longitude;
    }
    Edge getEdge() {
        return edge;
    }
    Node getPrev() {
        return prev;
    }
    boolean isVisited() {
        return visited;
    }
    boolean isFound() {
        return found;
    }
    int getDistance() {
        return distance;
    }
    int getDistToTarget(){
        return distToTarget;
    }
    int getTotalDist(){
        return distance + distToTarget;
    }
    int getType(){
        return type;
    }

    void setEdge(Edge edge) {
        edge.setNext(this.edge);
        this.edge = edge;
    }
    void setPrev(Node prev) {
        this.prev = prev;
    }
    void setVisited(boolean value) {
        visited = value;
    }
    void setFound(boolean value) {
        found = value;
    }
    void setDistance(int distance) {
        this.distance = distance;
    }
    void setDistToTarget(int distance){
        distToTarget = distance;
    }
    void setType(int type){
        this.type = type;
    }

    /**
     * Sammenligner på 'distance'
     */
    @Override
    public int compareTo(Node o) {
        return Integer.compare(this.distance, o.getDistance());
    }
}

/**
 * Kant i en graf
 */
class Edge {
    private final int from;
    private final int to;
    private final int weight;
    private Edge next;

    Edge(int from, int to, int weight){
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    int getFrom() {
        return from;
    }
    int getTo() {
        return to;
    }
    int getWeight() {
        return weight;
    }
    Edge getNext() {
        return next;
    }

    void setNext(Edge next) {
        this.next = next;
    }
}

/**
 * Objekt for å holde på prosessert data fra kartet
 */
class MapInfo {
    private final int[] landmarks;
    private final int[][] fromLM;
    private final int[][] toLM;

    MapInfo(int[] landmarks, int[][] fromLM, int[][] toLM){
        this.landmarks = landmarks;
        this.fromLM = fromLM;
        this.toLM = toLM;
    }

    int[] getLandmarks() {
        return landmarks;
    }
    int[][] getFromLM() {
        return fromLM;
    }
    int[][] getToLM() {
        return toLM;
    }
}
