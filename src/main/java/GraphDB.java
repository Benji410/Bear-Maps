import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */

    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        nodes.clear();
//        Iterator<Long> nodeIDs = nodes.keySet().iterator();
//        ArrayList<Long> removed = new ArrayList<>();
//        while (nodeIDs.hasNext()) {
//            Long currNode = nodeIDs.next();
//            Node node = nodes.get(currNode);
//            if (node.connectNodes.isEmpty()) {
//                nodeIDs.remove();
//                nodes.remove(currNode);
//                removed.add(currNode);
//            }
//        }
    }

    /** Returns an iterable of all vertex IDs in the graph. */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
//        new ArrayList<Long>()
//        Set<Long> vertexes = connectedNodes.keySet();
        return connectedNodes.keySet();
    }

    /** Returns ids of all vertices adjacent to v. */
    Iterable<Long> adjacent(long v) {
        Node vertex = connectedNodes.get(v);
        return vertex.connectNodes;
    }

//    PriorityQueue<Long> adjacentPQ(long v, )

    /** Returns the Euclidean distance between vertices v and w, where Euclidean distance
     *  is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ). */
    double distance(long v, long w) {
        Node a = connectedNodes.get(v);
        Node b = connectedNodes.get(w);
        return Math.sqrt(Math.pow((a.lon - b.lon), 2) + Math.pow((a.lat - b.lat), 2));
    }

    private double distanceClosest(long v, double longitude, double latitude) {
        Double vLon = lon(v);
        Double vLat = lat(v);
        return Math.sqrt(Math.pow((vLon - longitude), 2) + Math.pow((vLat - latitude), 2));
    }

    /** Returns the vertex id closest to the given longitude and latitude. */
    long closest(double lon, double lat) {
        if (connectedNodes.isEmpty()) {
            throw new IllegalArgumentException("there is no graph");
        }
        Iterator<Long> keys = connectedNodes.keySet().iterator();
        Long vertex = keys.next();
        while (keys.hasNext()) {
            Long curr = keys.next();
            Double vDist = distanceClosest(vertex, lon, lat);
            Double cDist = distanceClosest(curr, lon, lat);
            if (vDist == 0) {
                return vertex;
            }
            if (cDist == 0) {
                return curr;
            }
            if (cDist < vDist) {
                vertex = curr;
            }
        }
        return vertex;
    }

    /** Longitude of vertex v. */
    double lon(long v) {
        return connectedNodes.get(v).lon;
    }

    /** Latitude of vertex v. */
    double lat(long v) {
        return connectedNodes.get(v).lat;
    }

    //make a hashmap for each, with the id being the name and the value being a node
    private final Map<Long, Node> nodes = new HashMap<>();
    private final Map<Long, Node> connectedNodes = new HashMap<>();

    public void addConNode(Node n) {
        this.connectedNodes.put(n.id, n);
    }
    public Node getNode(Long nodeID) {
        return nodes.get(nodeID);
    }
    public void addNode(Node n) {
        this.nodes.put(n.id, n);
    }
    public Node getCNode(Long nodeID) {
        return connectedNodes.get(nodeID);
    }
    //Nodes
    static class Node implements Comparable<Node> {
        final Long id;
        String name;
        final Double lon;
        final Double lat;
        double priority;
        double distance;
        Node prev;
//        boolean marked = false;
        ArrayList<Long> connectNodes = new ArrayList<>();

        Node(String id, String lon, String lat) {
            this.id = Long.parseLong(id);
            this.lon = Double.parseDouble(lon);
            this.lat = Double.parseDouble(lat);
        }
        Node(long id, double lon, double lat) {
            this.id = id;
            this.lon = lon;
            this.lat = lat;
        }
        public void setName(String nodeName) {
            name = nodeName;
        }
        public void connect(Long iden) {
            if (iden != null) {
                this.connectNodes.add(iden);
            }
        }
        public void setPriorityDistPrev(double p, double d, Node pre) {
            this.priority = p;
            this.distance = d;
            this.prev = pre;
        }
        @Override
        public int compareTo(Node comp) {
            if (this.priority < comp.priority) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    //Ways
    static class Edge {
        String id;
        String name;
        String highway;
        ArrayList<Long> connections = new ArrayList<>();
        String lastNode;
        String maxSpeed;

        Edge(String id) {
            this.id = id;
        }
        public void setMaxSpeed(String speed) {
            this.maxSpeed = speed;
        }
        public void addConnection(Long node) {
            this.connections.add(node);
        }
        public void setLastNode(String node) {
            this.lastNode = node;
        }
        public  void setHighway(String wayType) {
            this.highway = wayType;
        }
        public void setName(String wayName) {
            this.name = wayName;
        }
    }
}
