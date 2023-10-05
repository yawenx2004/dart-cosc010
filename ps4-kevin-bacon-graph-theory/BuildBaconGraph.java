import java.io.IOException;
import java.util.*;

/**
 * purpose: PS-4
 * Class for building a graph with actors as vertices and a set of common movies in which actors co-star as edges
 * from files with movie, actor, and movie-actor data
 *
 * @author Iroda Abdulazizova
 * @author Yawen Xue
 *
 * date: 20 Feb. 2023
 **/

public class BuildBaconGraph {
    public static Graph<String, Set<String>> buildGraph(String actorsFile, String moviesFile, String movieActorsFile) throws IOException {
        Map<String, String> actors = ProcessBaconFiles.buildActorsMap(actorsFile);
        Map<String, String> movies = ProcessBaconFiles.buildMoviesMap(moviesFile);
        Map<String, Set<String>> movieActors = ProcessBaconFiles.buildMovieActorsMap(movieActorsFile);
        Graph<String, Set<String>> baconGraph = new AdjacencyMapGraph<String, Set<String>>();

        // loop over all keys (after conversion via keySet) in ActorsMap and insert each as a vertex into the map.
        for (String actor : actors.keySet()) {
            baconGraph.insertVertex(actors.get(actor));
        }

        // insert the edges -- set of movies in which both actors appear.
        // loop over the movies (keys) in movieActors map
        for (String movie : movieActors.keySet()) {

            // loop over the set twice to get the IDs of 2 actors (by def, they are in the same movie,
            // hence can be used to create an edge between their corr-g vertices)
            for (String actorx : movieActors.get(movie)) {
                String actor1 = actors.get(actorx);
                for (String actory : movieActors.get(movie)) {
                    if (!actory.equals(actorx)) {
                        String actor2 = actors.get(actory);

                        // insert the edge between actor 1 and actor 2 vertices (get name via ID from actorsMap)
                        // insert name of movie (by getting from movieMap)
                        // cannot directly add to set by inserting into edge, create the set first.
                        if (!baconGraph.hasEdge((actor1), (actor2))) {
                            baconGraph.insertUndirected(actor1, actor2, new HashSet<String>());
                        }
                        baconGraph.getLabel(actor1, actor2).add(movies.get(movie));
                    }
                }
            }
        }
        return baconGraph;
    }
}
