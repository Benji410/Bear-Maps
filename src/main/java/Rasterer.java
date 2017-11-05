import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.Iterator;


/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    // Recommended: QuadTree instance variable. You'll need to make
    //              your own QuadTree since there is no built-in quadtree in Java.

    /** imgRoot is the name of the directory containing the images.
     *  You may not actually need this for your class. */
    public Rasterer(String imgRoot) {
        // YOUR CODE HERE
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     *                    Can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     *                    forget to set this to true! <br>
     * @see #REQUIRED_RASTER_REQUEST_PARAMS
     */

    /**LonDPP, longitude distance per pixel
     * (lowerright longitude - upperleft longitude)/(width of the image/pixels
     * useful print statements
     // System.out.println(params.keySet());
     // System.out.println(params);
     // System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
     //                       + "your browser.");
     /        System.out.println(sorted.size());
     //        System.out.println(tiles);
     int heightP =(int) Math.ceil(params.get("h") / 256.0);
     int widthP = (int) Math.ceil(params.get("w") / 256.0);

     //if they have the same ullon, then same column
     //if they have same ullat, then same row
     * */
    private static final double ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
            ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;

    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        tiles.clear();
//        double lrlon = params.get("lrlon");
//        double ullon = params.get("ullon");
//        double lrlat = params.get("lrlat");
//        double ullat = params.get("ullat");
//        if (!(lrlon <= ROOT_LRLON && ullon >= ROOT_ULLON
//              && lrlat >= ROOT_LRLAT && ullat <= ROOT_ULLAT)
//                || lrlon < ullon || lrlat > ullat) {
//            results.put("query_success", false);
//        }
        //check if the params are outside of the area or bad request
        Map<String, Object> results = new HashMap<>();
        QuadTreeNode berkeleyMap = new QuadTreeNode();
        mapRasterHelper(berkeleyMap, params);

        LinkedList<QuadTreeNode> sorted = (LinkedList<QuadTreeNode>) mergeSort(tiles);
        LinkedList<QuadTreeNode> sort =
                narrowImage(sorted, params.get("ullat"), params.get("lrlat"));
        int widthP = width;
        int heightP = height;
        //sort by longitude here?
        LinkedList<QuadTreeNode> finalSort = new LinkedList<>();
        for (int i = 0; i < heightP; i++) {
            LinkedList<QuadTreeNode> sortLon = new LinkedList<>();
            for (int n = 0; n < widthP; n++) {
                sortLon.add(sort.remove());
            }
            finalSort.addAll(mergeSortLon(sortLon));
        }

        //inserts items into grid render and sets all the results
        String[][] renderGrid = new String[heightP][widthP];
        results.put("depth", finalSort.getFirst().depth);
        results.put("raster_ul_lon", finalSort.getFirst().ullon);
        results.put("raster_ul_lat", finalSort.getFirst().ullat);
        results.put("raster_lr_lon", finalSort.getLast().lrlon);
        results.put("raster_lr_lat", finalSort.getLast().lrlat);
        for (int h = 0; h < heightP; h++) {
            for (int w = 0; w < widthP; w++) {
                if (finalSort.isEmpty()) {
                    break;
                }
                QuadTreeNode node = finalSort.removeFirst(); // System.out.print(node);
                String name = "img/" + node.root + ".png";
                renderGrid[h][w] = name; //System.out.println(name);
            }
        }
        results.put("render_grid", renderGrid);
        results.put("query_success", true); //System.out.print(results);
        return results;
    }

    private Queue<QuadTreeNode> tiles = new LinkedList<>();
    private int width;
    private int height;

    private void mapRasterHelper(QuadTreeNode node, Map<String, Double> params) {
        if (node == null) {
            return;
        }
        double lrlon = params.get("lrlon");
        double ullon = params.get("ullon");
        double widthP = params.get("w");
        double ullat = params.get("ullat");
        double lrlat = params.get("lrlat");
        double lonDPP = (lrlon - ullon) / widthP;
        if (node.intersectsTile(ullon, ullat, lrlon, lrlat)) {
            if (node.lonDPPSmallerThanOrIsLeaf(lonDPP)) {
                tiles.add(node);
            } else {
                mapRasterHelper(node.topLeft, params);
                mapRasterHelper(node.topRight, params);
                mapRasterHelper(node.botLeft, params);
                mapRasterHelper(node.botRight, params);
            }
        }
    }

    private LinkedList<QuadTreeNode> narrowImage(
            LinkedList<QuadTreeNode> images, double ullatQ, double lrlatQ) {
        //checks if top layer is useful or not
        if (images == null) {
            throw new IllegalArgumentException("There are no images");
        }
        width = 0;
        height = 1;
        QuadTreeNode node1 = images.get(0);
        while (node1.ullat == images.get(width).ullat) {
            width += 1;
        }
        for (int i = 0; i < images.size() - width; i += width) {
            QuadTreeNode level1 = images.get(i);
            QuadTreeNode level2 = images.get(i + width);
            double latL1 = level1.ullat - ullatQ;
            double latL2 = level2.ullat - ullatQ;
            double latL3 = level1.lrlat - lrlatQ;
            double latL4 = level2.lrlat - lrlatQ;
            if (latL1 > 0 && latL2 > 0 && latL1 > latL2) {
                for (int n = 0; n < width; n++) {
                    images.removeFirst();
                }
                i -= width;
            } else if (latL3 < 0 && latL4 < 0) {
                LinkedList<QuadTreeNode> list = new LinkedList<>();
                for (int m = 0; m < i + width; m++) {
                    list.add(images.get(m));
                }
                return list;
            } else {
                height += 1;
            }
        }
        return images;
    }

    private static QuadTreeNode getMin(Queue<QuadTreeNode> q1, Queue<QuadTreeNode> q2) {
        if (q1.isEmpty()) {
            return q2.remove();
        } else if (q2.isEmpty()) {
            return q1.remove();
        } else {
            // Peek at the minimum item in each queue (which will be at the front, since the
            // queues are sorted) to determine which is smaller.
            Comparable q1Min = q1.peek();
            Comparable q2Min = q2.peek();
            if (q1Min.compareTo(q2Min) >= 0) {
                // Make sure to call dequeue, so that the minimum item gets removed.
                // one with max latitud gets removed
                return q1.remove();
            } else {
                return q2.remove();
            }
        }
    }

    /** Returns a queue of queues that each contain one item from items. */
    private static Queue<Queue<QuadTreeNode>> makeSingleItemQueues(Queue<QuadTreeNode> items) {
        Queue<Queue<QuadTreeNode>> queues = new LinkedList<>();
        Iterator<QuadTreeNode> item = items.iterator();
        while (item.hasNext()) {
            Queue<QuadTreeNode> addQ = new LinkedList<>();
            QuadTreeNode node = item.next();
//            System.out.println(node.root);
            addQ.add(node);
            queues.add(addQ);
        }
        return queues;
    }

    private static Queue<QuadTreeNode> mergeSortedQueues(
            Queue<QuadTreeNode> q1, Queue<QuadTreeNode> q2) {
        Queue<QuadTreeNode> mergedQ = new LinkedList<>();
        int i = q1.size() + q2.size();
        while (i != 0) {
            QuadTreeNode min = getMin(q1, q2);
//            System.out.println(min.root);
            mergedQ.add(min);
            i -= 1;
        }
        return mergedQ;
    }

    /** Returns a Queue that contains the given items sorted from least to greatest. */
    private static Queue<QuadTreeNode> mergeSort(Queue<QuadTreeNode> items) {
        Queue<Queue<QuadTreeNode>> single = makeSingleItemQueues(items);
        while (single.size() > 1) {
            Queue<QuadTreeNode> q1 = single.remove();
            Queue<QuadTreeNode> q2 = single.remove();
            Queue<QuadTreeNode> addQ = mergeSortedQueues(q1, q2);
            single.add(addQ);
        }
        return single.remove();
    }

    private static QuadTreeNode getMinLon(Queue<QuadTreeNode> q1, Queue<QuadTreeNode> q2) {
        if (q1.isEmpty()) {
            return q2.remove();
        } else if (q2.isEmpty()) {
            return q1.remove();
        } else {
            // Peek at the minimum item in each queue (which will be at the front, since the
            // queues are sorted) to determine which is smaller.
            QuadTreeNode q1Min = q1.peek();
            QuadTreeNode q2Min = q2.peek();
            if (q1Min.compareLon(q2Min) >= 0) {
                // Make sure to call dequeue, so that the minimum item gets removed.
                // one with max latitud gets removed
                return q2.remove();
            } else {
                return q1.remove();
            }
        }
    }

    private static Queue<QuadTreeNode> mergeSortedQueuesLon(
          Queue<QuadTreeNode> q1, Queue<QuadTreeNode> q2) {
        Queue<QuadTreeNode> mergedQ = new LinkedList<>();
        int i = q1.size() + q2.size();
        while (i != 0) {
            QuadTreeNode min = getMinLon(q1, q2);
//            System.out.println(min.root);
            mergedQ.add(min);
            i -= 1;
        }
        return mergedQ;
    }

    private static Queue<QuadTreeNode> mergeSortLon(Queue<QuadTreeNode> items) {
        Queue<Queue<QuadTreeNode>> single = makeSingleItemQueues(items);
        while (single.size() > 1) {
            Queue<QuadTreeNode> q1 = single.remove();
            Queue<QuadTreeNode> q2 = single.remove();
            Queue<QuadTreeNode> addQ = mergeSortedQueuesLon(q1, q2);
            single.add(addQ);
        }
        return single.remove();
    }

    private class QuadTreeNode implements Comparable<QuadTreeNode> {
        private String root;
        private QuadTreeNode topLeft;
        private QuadTreeNode topRight;
        private QuadTreeNode botLeft;
        private QuadTreeNode botRight;
        private int depth;
        private Map<String, Double> params;
        private double lrlon;
        private double lrlat;
        private double ullat;
        private double ullon;

        /**Beginning of the map or the root #specialcase
         *Generates the whole quad tree
         * ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
         ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;
         */
        private QuadTreeNode() {
            this.root = "root";
            this.params = new HashMap<>();
            this.lrlon = -122.2119140625;
            this.lrlat = 37.82280243352756;
            this.ullon = -122.2998046875;
            this.ullat = 37.892195547244356;
            this.params.put("lrlon", lrlon);
            this.params.put("lrlat", lrlat);
            this.params.put("ullon", ullon);
            this.params.put("ullat", ullat);
            this.depth = 0;
            this.topLeft = new QuadTreeNode("", "1", this.depth, this.params);
            this.topRight = new QuadTreeNode("", "2", this.depth, this.params);
            this.botLeft = new QuadTreeNode("", "3", this.depth, this.params);
            this.botRight = new QuadTreeNode("", "4", this.depth, this.params);
        }

        private QuadTreeNode(String root, String child, int dep, Map<String, Double> params) {
            this.root = root + child;
            this.depth = dep + 1;
            this.params = new HashMap<>();
            calcLongNLat(child, params);
            if (dep != 6) {
                this.topLeft = new QuadTreeNode(this.root, "1", this.depth, this.params);
                this.topRight = new QuadTreeNode(this.root, "2", this.depth, this.params);
                this.botLeft = new QuadTreeNode(this.root, "3", this.depth, this.params);
                this.botRight = new QuadTreeNode(this.root, "4", this.depth, this.params);
            }
        }

        @Override
        public int compareTo(QuadTreeNode node) {
            if (node == null) {
                throw new NullPointerException("node is null");
            }
            //ullon, the more negative the farther left
            //so if neg, this.ullon is more left, if pos then to the right
            //if 0, then the they belong to the same column
            double truth = this.ullat - node.ullat;
            if (truth < 0) {
                return -1;
            } else if (truth > 0) {
                return 1;
            } else {
                return 0;
            }
        }

        public int compareLon(QuadTreeNode node) {
            if (node == null) {
                throw new NullPointerException("node is null");
            }
            //ullon, the more negative the farther left
            //so if neg, this.ullon is more left, if pos then to the right
            //if 0, then the they belong to the same column
            double truth = this.ullon - node.ullon;
            if (truth < 0) {
                return -1;
            } else if (truth > 0) {
                return 1;
            } else {
                return 0;
            }
        }
        /**LonDPP, longitude distance per pixel
         * (lower right longitude - upper left longitude) / (width of the image/box in pixels)
         * */
        //checks the if the lonDPP of the image is within the queries lonDPP
        private boolean lonDPPSmallerThanOrIsLeaf(double queriesLonDPP) {
            double lonDPP = (this.lrlon - this.ullon) / 256.0;

            return (lonDPP <= queriesLonDPP) || this.depth == 7;
        }
        //checks if the tile intersects the queries
        private boolean intersectsTile(
                double ullonQ, double ullatQ, double lrlonQ, double lrlatQ) {
            //this one checks smaller boxes after big ones are pruned
            if ((ullonQ >= ullon && ullonQ <= lrlon) || (lrlonQ >= ullon && lrlonQ <= lrlon)) {
                if ((lrlat <= lrlatQ && ullat >= ullatQ) || (lrlat <= lrlatQ || ullat >= ullatQ)) {
                    return true;
                }
            }
            if ((lrlon > ullonQ && lrlon < lrlonQ) || (ullon > lrlonQ && ullon < ullonQ)) {
                if (lrlat < ullatQ && lrlat > lrlatQ) {
                    return true;
                }
            }
            if (ullat > lrlatQ || ullat < ullatQ) {
                if ((lrlon > ullonQ && lrlon < lrlonQ) || (ullon < lrlonQ && ullon > ullonQ)) {
                    return true;
                }
            }
            return false;
        }

        private void calcLongNLat(String child, Map<String, Double> p) {
            double subLon = (p.get("lrlon") - p.get("ullon")) / 2.0;
            double subLat = (p.get("lrlat") - p.get("ullat")) / 2.0;
            switch (child) {
                case "1":
                    this.ullon = p.get("ullon");
                    this.ullat = p.get("ullat");
                    this.lrlon = p.get("ullon") + subLon;
                    this.lrlat = p.get("ullat") + subLat;
                    break;
                case "2":
                    this.ullon = p.get("ullon") + subLon;
                    this.ullat = p.get("ullat");
                    this.lrlon = p.get("lrlon");
                    this.lrlat = p.get("ullat") + subLat;
                    break;
                case "3":
                    this.ullon = p.get("ullon");
                    this.ullat = p.get("ullat") + subLat;
                    this.lrlon = p.get("ullon") + subLon;
                    this.lrlat = p.get("lrlat");
                    break;
                case "4":
                    this.ullon = p.get("ullon") + subLon;
                    this.ullat = p.get("ullat") + subLat;
                    this.lrlon = p.get("lrlon");
                    this.lrlat = p.get("lrlat");
                    break;
                default:
                    break;
            }
            this.params.put("lrlon", lrlon);
            this.params.put("lrlat", lrlat);
            this.params.put("ullon", ullon);
            this.params.put("ullat", ullat);
        }
    }

}

