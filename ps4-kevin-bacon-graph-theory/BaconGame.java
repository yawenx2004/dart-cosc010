import java.io.IOException;
import java.util.Scanner;
import java.util.*;

/**
 * purpose: PS-4; interface for Kevin Bacon game
 *
 * @author Iroda Abdulazizova
 * @author Yawen Xue
 *
 * date: 20 Feb. 2023
 **/
public class BaconGame {
    private Graph<String, Set<String>> baconGraph;
    private Graph<String, Set<String>> shortestPathBacon;
    private String universeCenter = "Kevin Bacon";
    private boolean isRunning = true;
    private Scanner myInput = new Scanner(System.in);

    public BaconGame(String actorsFile, String moviesFile, String movieActorsFile) throws IOException {
        baconGraph = BuildBaconGraph.buildGraph(actorsFile, moviesFile, movieActorsFile);
        shortestPathBacon = GraphLib.bfs(baconGraph, universeCenter);
    }

    /**
     * Lists top (positive number) or bottom (negative number) <#> centers of the universe sorted by average separation
     * @param number
     * @return
     */
    public ArrayList<Map.Entry<String,Double>> topCenters(int number) {

        // get the average separation for every vertex
        Map<String, Double> byAverageSeparation = new HashMap<String, Double>();
        for (String currentVertex : baconGraph.vertices()) {
            double averageSeparation = GraphLib.averageSeparation(GraphLib.bfs(baconGraph, currentVertex), currentVertex);
            byAverageSeparation.put(currentVertex, averageSeparation);
        }

        // create array list & sort
        ArrayList<Map.Entry<String,Double>> sortedCenters = new ArrayList<>();
        for (Map.Entry<String, Double> e : byAverageSeparation.entrySet()) {
            if (e.getValue() != 0.0) {
                sortedCenters.add(e);
            }
        }
        //sortedCenters.addAll(byAverageSeparation.entrySet());
        sortedCenters = sortCenters(sortedCenters);
        //sortedCenters.sort((p1, p2) -> (int)(p1.getValue() - p2.getValue()));

        // base output on number
        if (number > 0) {
            ArrayList<Map.Entry<String, Double>> best = new ArrayList<>();
            for (int i = 0; i < number; i++) {
                best.add(sortedCenters.get(i));
            }
            sortedCenters = best;
        } else if (number < 0) {
            ArrayList<Map.Entry<String, Double>> worst = new ArrayList<>();
            for (int i = 0; i < (0 - number); i++) {
                worst.add(sortedCenters.get(sortedCenters.size() - i - 1));
            }
            sortedCenters = worst;
        }
        return sortedCenters;
    }

    /**
     * Helper method for topCenters(); sorts by average degree of separation
     * @param list
     * @return
     */
    public ArrayList<Map.Entry<String, Double>> sortCenters(ArrayList<Map.Entry<String, Double>> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size() - i - 1; j++) {
                if (list.get(j).getValue() > list.get(j + 1).getValue()) {
                    Map.Entry<String, Double> temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
        return list;
    }

    /**
     * Returns list of actors with degrees between low & high
     * @param low
     * @param high
     * @return
     */
    public ArrayList<Map.Entry<String, Integer>> actorsByDegreeBetween (int low, int high) {
        Map<String, Integer> degreeMap = new HashMap<>();

        // add info for vertices by looping over graph
        for (String currentVertex : baconGraph.vertices()) {
            int toCompareDegree = baconGraph.outDegree(currentVertex);
            if (toCompareDegree >= low && toCompareDegree <= high) {
                degreeMap.put(currentVertex, toCompareDegree);
            }
        }

        // create array list & sort
        ArrayList<Map.Entry<String, Integer>> sortedDegrees = new ArrayList<Map.Entry<String, Integer>>();
        sortedDegrees.addAll(degreeMap.entrySet());
        sortedDegrees.sort((p1, p2) -> (p1.getValue() - p2.getValue()));
        return sortedDegrees;
    }

    /**
     * Returns list of actors with infinite degrees of separation from center
     * @return
     */
    public Set<String> actorsWithInfiniteSep () {

        // implement missingVertices & return set
        Set<String> infSep = GraphLib.missingVertices(baconGraph, shortestPathBacon);
        return infSep;
    }

    /**
     * Returns list of actors with non-infinite degrees of separation between low & high from current center
     * @param low
     * @param high
     * @return
     */
    public ArrayList<Map.Entry<String, Integer>> actorsByNonInfiniteSeparation(int low, int high) {
        Map<String, Integer> separationMap = new HashMap<String, Integer>();
        ArrayList<Map.Entry<String, Integer>> sortedSeparations = new ArrayList<Map.Entry<String, Integer>>();

        // add info for vertices by looping over graph
        for (String currentVertex : shortestPathBacon.vertices()) {

            // get size of path list, which is the separation from center of u
            int separation = GraphLib.getPath(shortestPathBacon, currentVertex).size();
            if (separation >= low && separation <= high) {
                separationMap.put(currentVertex, separation);
            }
        }

        // create array list & sort
        sortedSeparations.addAll(separationMap.entrySet());
        sortedSeparations.sort((p1, p2) -> (p1.getValue() - p2.getValue()));
        return sortedSeparations;
    }

    public void makeCenterUniverse(String name) {
        universeCenter = name;
        shortestPathBacon = GraphLib.bfs(baconGraph, universeCenter);
    }

    /**
     * Returns shortest path from given point
     * @param name
     * @return
     */
    public void findShortestBaconPath(String name) {
        List<String> shortestBaconPath = GraphLib.getPath(shortestPathBacon, name);
        if (shortestBaconPath.size() == 0) {
            System.out.println("No path found.");
        } else {
            int baconNumber = shortestBaconPath.size();
            shortestBaconPath.add(0, name);
            System.out.println(name + "'s Bacon number is " + baconNumber);

            // print bacon path
            //System.out.println("The shortest path from " + name + " to " + universeCenter + " is: " + shortestBaconPath);
            for (int i = 0; i < baconNumber; i++) {
                String actor1 = shortestBaconPath.get(i);
                String actor2 = shortestBaconPath.get(i + 1);
                System.out.println(actor1 + " appeared in " + baconGraph.getLabel(actor1, actor2) + " with " + actor2);
            }
        }
    }

    /**
     * Implements interface
     */
    public void play() {
        System.out.println(universeCenter + " is now the center of the acting universe, connected to " +
                shortestPathBacon.numVertices() + "/" + baconGraph.numVertices() + " with average separation "
                + GraphLib.averageSeparation(shortestPathBacon, universeCenter));

        // continue getting input while game is running
        while (isRunning == true) {

            // read user input & split based on empty space
            System.out.println();
            System.out.println(universeCenter + " game >");
            String command = myInput.nextLine();
            String[] commandArray = command.split(" ");

            // call different methods based on command
            if (Objects.equals(commandArray[0], "c")) {
                try {
                    int number = Integer.parseInt(commandArray[1]);
                    if (number > 0) {
                        System.out.println("Best " + number + " centers of the universe: " + topCenters(number));
                    } else {
                        System.out.println("Worst " + (0 - number) + " centers of the universe: " + topCenters(number));
                    }
                } catch (Exception e) {
                    System.out.println("Invalid input. Please try again.");
                }
            }

            if (Objects.equals(commandArray[0], "d")) {
                try {
                    int low = Integer.parseInt(commandArray[1]);
                    int high = Integer.parseInt(commandArray[2]);
                    System.out.println("Actors that co-starred in between " + low + " and " + high + " movies: " + actorsByDegreeBetween(low, high));
                } catch (Exception e) {
                    System.out.println("Invalid input. Please try again.");
                }
            }

            if (Objects.equals(commandArray[0], "i")) {
                System.out.println("Actors with infinite Bacon numbers: " + actorsWithInfiniteSep());
            }

            if (Objects.equals(commandArray[0], "s")) {
                try {
                    int low = Integer.parseInt(commandArray[1]);
                    int high = Integer.parseInt(commandArray[2]);
                    System.out.println("Actors that have a bacon number between " + low + " and " + high + ": " + actorsByNonInfiniteSeparation(low, high));
                } catch (Exception e) {
                    System.out.println("Invalid input. Please try again.");
                }
            }

            if (Objects.equals(commandArray[0], "u")) {
                try {
                    String name = commandArray[1]; // fetches actor first name
                    for (int i = 2; i < commandArray.length; i++) { // append other parts of name
                        name += " " + commandArray[i];
                    }
                    makeCenterUniverse(name);
                    shortestPathBacon = GraphLib.bfs(baconGraph, name);
                    System.out.println(universeCenter + " is now the center of the acting universe, connected to " +
                            shortestPathBacon.numVertices() + "/" + baconGraph.numVertices() + " with average separation "
                            + GraphLib.averageSeparation(shortestPathBacon, universeCenter));
                } catch (Exception e) {
                    System.out.println("Invalid input. Please try again.");
                }
            }

            if (Objects.equals(commandArray[0], "q")) {
                System.out.println("Game over.");
                isRunning = false;
            }

            if (Objects.equals(commandArray[0], "p")) {
                try {
                    String name = commandArray[1]; // fetches actor first name
                    for (int i = 2; i < commandArray.length; i++) { // append other parts of name
                        name += " " + commandArray[i];
                    }
                    findShortestBaconPath(name);
                } catch (Exception e) {
                    System.out.println("Invalid input. Please try again.");
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BaconGame kbgTest = new BaconGame("inputs/actorsTest.txt", "inputs/moviesTest.txt", "inputs/movie-ActorsTest.txt");
        kbgTest.play();

        BaconGame kbg = new BaconGame("inputs/actors.txt", "inputs/movies.txt", "inputs/movie-Actors.txt");
        kbg.play();
    }
}
