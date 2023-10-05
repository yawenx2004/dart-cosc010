import java.io.*;
import java.util.*;

/**
 * purpose: PS-4; processes files for Kevin Bacon game
 *
 * @author Iroda Abdulazizova
 * @author Yawen Xue
 *
 * date: 20 Feb. 2023
 **/

public class ProcessBaconFiles {
    public static Map<String, String> buildActorsMap (String fileName) throws IOException {
        BufferedReader inputActor = new BufferedReader(new FileReader(fileName));
        Map<String, String> actors = new HashMap<String, String>();

        // read file line by line
        String line;
        while (((line = inputActor.readLine()) != null)) {

            // pipe separated
            String[] pieces = line.split("\\|");

            // extract actorID and actorName as string objects
            String actorID = pieces[0];
            String actorName = pieces[1];

            // put into map
            actors.put(actorID, actorName);
        }
        return actors;
    }

    public static Map<String, String> buildMoviesMap (String fileName) throws IOException {
        BufferedReader inputActor = new BufferedReader(new FileReader(fileName));
        Map<String, String> movies = new HashMap<String, String>();

        // read file line by line
        String line;
        while (((line = inputActor.readLine()) != null)) {

            // pipe separated
            String[] pieces = line.split("\\|");

            // extract actorID and actorName as string objects
            String movieID = pieces[0];
            String movieName = pieces[1];

            // put into map
            movies.put(movieID, movieName);
        }
        return movies;
    }

    public static Map<String, Set<String>> buildMovieActorsMap (String fileName) throws IOException {
        BufferedReader inputActor = new BufferedReader(new FileReader(fileName));
        Map<String, Set<String>> moviesToActors = new HashMap<String, Set<String>>();

        // read file line by line
        String line;
        while (((line = inputActor.readLine()) != null)) {

            // pipe separated
            String[] pieces = line.split("\\|");

            // extract actorID and actorName as string objects
            String movieID = pieces[0];
            String actorID = pieces[1];

            // put into map
            // if movie already in set, add actorID to set
            if (moviesToActors.containsKey(movieID)==false) {
                moviesToActors.put(movieID, new HashSet<>());
            }
            moviesToActors.get(movieID).add(actorID);
        }
        return moviesToActors;
    }
}
