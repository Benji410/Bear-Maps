import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Iterator;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest,
     * where the longs are node IDs.
     */
    public static LinkedList<Long> shortestPath(
            GraphDB g, double stlon, double stlat, double destlon, double destlat) {

        LinkedList<Long> path = new LinkedList<>();

        long s = g.closest(stlon, stlat); //start
        long t = g.closest(destlon, destlat); //destination
        double startDist = g.distance(s, s); //g(n) distance from the curr node to n
//        double heuristic = g.distance(s, t); //h(n) distance from n to the destination (t)
        GraphDB.Node start = g.getCNode(s);
        start.setPriorityDistPrev(0.0, startDist, null);
        Map<Long, Double> bestDistance = new HashMap<>();
        Map<Long, Long> edgeTo = new HashMap<>();
        bestDistance.put(s, 0.0);

        PriorityQueue<GraphDB.Node> fringe = new PriorityQueue<>();
        fringe.add(start);
        edgeTo.put(s, null);
        GraphDB.Node v;

        while (true) {
            v = fringe.poll();
            if (v.id == t) {
                break;
            }
            Iterator<Long> adjacent = g.adjacent(v.id).iterator();
            while (adjacent.hasNext()) {
                Long w = adjacent.next(); //n being some node on the fringe
                GraphDB.Node actNode = g.getCNode(w);
                double distFromStoW = g.distance(v.id, w) + bestDistance.get(v.id); //g(n) dist
                double heurFromNode = g.distance(w, t); //h(n) distance from node to goal
                double currPriority = distFromStoW + heurFromNode; //total
                if (!bestDistance.containsKey(w) || distFromStoW < bestDistance.get(v.id)) {
                    bestDistance.put(w, distFromStoW);
                    actNode.setPriorityDistPrev(currPriority, distFromStoW, v);
                    fringe.add(actNode);
                    edgeTo.put(w, v.id);
//                       System.out.println(w);
                }
            }
        }
        Long goal = t;
        while (goal != null) {
            path.addFirst(goal);
            goal = edgeTo.get(goal);
        }
        return path;
    }
}





//    LinkedList<Long> path = new LinkedList<>();
//    long s = g.closest(stlon, stlat); //start
//    long t = g.closest(destlon, destlat); //destination
//    double currDist = g.distance(s, s); //g(n) distance from the curr node to n
//    double heuristic = g.distance(s, t); //h(n) distance from n to the destination (t)
//    double priority = currDist + heuristic;
//        g.getCNode(s).setPriorityDistPrev(priority, currDist, null);
//                PriorityQueue<GraphDB.Node> fringe = new PriorityQueue<>();
//        fringe.add(g.getCNode(s));
//
//        GraphDB.Node curr = fringe.peek();
//        double bestPriority = priority;
//        while (!curr.id.equals(t)) {
//        curr = fringe.poll();
//        if (curr.marked) {
//        continue;
//        } else if (curr.id == t) {
//        break;
//        } else {
//        curr.marked = true;
//        }
//        Iterator<Long> adjacent = g.adjacent(curr.id).iterator();
//        while (adjacent.hasNext()) {
//        Long node = adjacent.next(); //n being some node on the fringe
//        GraphDB.Node actNode = g.getCNode(node);
//        if (curr.prev == null || !node.equals(curr.prev.id)) {
//        double distFromNode = g.distance(curr.id, node) + curr.distance; //g(n) dist
//        double heurFromNode = g.distance(node, t); //h(n) distance from node to goal
//        double currPriority = distFromNode + heurFromNode; //total
//        if (currPriority > bestPriority) {
//        actNode.setPriorityDistPrev(currPriority, distFromNode, curr);
//        fringe.add(actNode);
//        }
//        }
//        }
////            curr = fringe.poll();
//        }
//        GraphDB.Node goal = curr;
//        while (goal != null) {
//        path.addFirst(goal.id);
//        goal = goal.prev;
//        }
//        return path;
//        }

//    GraphDB.Node actNode = g.getCNode(w);
//                if (edgeTo.get(v.id) == null || !(w == edgeTo.get(v.id))) {
//                        double distFromStoW = g.distance(v.id, w) + v.distance; //g(n) dist
// if (!marked.contains(v.id) || distFromStoW < bestDistance.get(v)) { //!marked.contains(v.id) ||
//        double heurFromNode = g.distance(w, t); //h(n) distance from node to goal
//        double currPriority = distFromStoW +  heurFromNode; //total
//        bestDistance.put(v, distFromStoW);
//        GraphDB.Node newNode = new GraphDB.Node(actNode.id, actNode.lon, actNode.lat);
//        newNode.connectNodes = actNode.connectNodes;
//        newNode.setPriorityDistPrev(currPriority, distFromStoW, v);
//        fringe.add(newNode);
//        marked.add(v.id);
//        bestDistance.put(newNode, 10.9);
//        edgeTo.put(w, v.id);
////                        System.out.println(newNode.id);
//        }

//        }

//    GraphDB.Node source = g.getCNode(s);
//        source.setPriorityDistPrev(0.0, 0.0);
//
//    PriorityQueue<GraphDB.Node> fringe = new PriorityQueue<>();
//    HashMap<Long, Double> best = new HashMap<>();
//    HashMap<Long, Long> vParent = new HashMap<>();
//
//        fringe.add(source);
//        best.put(source.id, 999.0);
//        while (true) {
//        GraphDB.Node v = fringe.poll();
//        if (v.id == t) {
//            break;
//        }
//        for (long w : v.connectNodes) {
//            double startToV = v.distance;
//            double vTOw = g.distance(v.id, w);
//            double wTOH = g.distance(w, t);
//            double wBest = startToV + vTOw;
//            if (wBest < best.get(v.id)) {
//                best.remove(v.id);
//                best.put(v.id, wBest);
//                double priority = wBest + wTOH;
//                GraphDB.Node wNode = g.getCNode(w);
//                wNode.setPriorityDistPrev(priority, wBest);
//                fringe.add(wNode);
//                vParent.put(w, v.id);
//            }
//        }
//    }
//    long goal = t;
//        while(vParent.containsKey(goal)) {
//        path.addFirst(goal);
//        goal = vParent.get(goal);
//    }
//        path.addFirst(s);
//        return path;
//
//}


