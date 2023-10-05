import java.util.*;

/**
 * purpose: PS-4 graph library for implementing Kevin Bacon game
 *
 * @author Iroda Abdulazizova
 * @author Yawen Xue
 *
 * date: 20 Feb. 2023
 **/

public class GraphLib {

    /**
     * BFS to build shortest path trees for current center of universe
     * Returns path as directed graph
     * @param g
     * @param source
     * @return
     */
    public static <V,E> Graph<V,E> bfs(Graph<V,E> g, V source) {
        Graph<V,E> shortestPathTreeGraph = new AdjacencyMapGraph<>(); // initialize new graph to be created via BFS
        shortestPathTreeGraph.insertVertex(source); // load the 'source' vertex as root of new graph
        Set<V> visited = new HashSet<V>(); //Set to track which vertices have already been visited
        Queue<V> queue = new LinkedList<V>(); //queue to implement BFS

        queue.add(source); // enqueue root vertex
        visited.add(source); // add root to visited Set
        while (!queue.isEmpty()) {
            V current = queue.remove(); // dequeue
            for (V v : g.outNeighbors(current)) { //loop over out neighbors
                if (!visited.contains(v)) { //if neighbor not visited, then neighbor is discovered from this vertex
                    visited.add(v); //add neighbor to visited Set
                    queue.add(v); //enqueue neighbor
                    //save that this vertex was discovered from prior vertex through creating of directed edge
                    shortestPathTreeGraph.insertVertex(current);
                    shortestPathTreeGraph.insertVertex(v);
                    shortestPathTreeGraph.insertDirected(v, current, null);
                }
            }
        }
        return shortestPathTreeGraph;
    }

    /**
     * Given shortest path tree & vertex, constructs path from vertex back to center of universe
     * Returns path as list
     * @param tree
     * @param v
     * @return
     */
    public static <V, E> List<V> getPath(Graph<V,E> tree, V v) {

        // Check that given graph is not empty and does not consist of only the root node
        if (tree.numEdges() == 0) {
            return new ArrayList<V>();
        }

        // Make sure given vertex is in shortestPathGraph
        if (!tree.hasVertex(v)) {
            return new ArrayList<V>();
        }

        // Start from given vertex and go to root (center of universe) vertex
        // Traverse via out vertex of directed edge
        ArrayList<V> shortestPath = new ArrayList<>();

        // Start at the end vertex (leaf of 'tree')
        while (tree.outDegree(v) > 0) {
            for (V current : tree.outNeighbors(v)) {
                shortestPath.add(current);
                v = current;
                break;
            }
        }
        return shortestPath;
    }

    /**
     * Given a graph and a subgraph (here - shortest path tree), determine which
     * vertices are in the graph but not subgraph (here, not reached by BFS)
     * If there are no missing vertices, just returns, by default, an empty set
     * @param tree
     * @param subgraph
     * @return
     */
    public static <V, E> Set<V> missingVertices(Graph<V,E> tree, Graph<V,E> subgraph) {
        Set<V> missingVertices = new HashSet<V>();

        // Iterate through every vertex of a graph
        for (V currentVertex : tree.vertices()) {
            if (!subgraph.hasVertex(currentVertex)) {
                missingVertices.add(currentVertex);
            }
        }
        return missingVertices;
    }

    /**
     * Find the average distance-from-root in a shortest path tree
     * without enumerating all the paths - with tree recursion
     * @param tree
     * @param root
     * @return
     */
    public static <V, E> double averageSeparation(Graph<V,E> tree, V root) {
        double sum = totalSeparation(tree, root, 0);
        double average = sum / tree.numVertices();
        return average;
    }

    /**
     * Helper method for averageSeparation
     * @param tree
     * @param root
     * @param total
     * @return
     */
    public static <V, E> double totalSeparation(Graph<V,E> tree, V root, int total) {

        // Find the total separation (includes all vertices in graph)
        int num = total;

        // Base case: key set of the out neighbors is empty
        for ( V neighbor : tree.inNeighbors(root)) {
            num += totalSeparation(tree, neighbor, total + 1);
        }
        return num;
    }
}
