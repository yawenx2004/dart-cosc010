import org.bytedeco.opencv.presets.opencv_core;

import java.nio.Buffer;
import java.util.*;
import java.io.*;

/**
 * purpose: PS-5
 * training for POS tagging
 * creates map of transition probabilities & map of observation probabilities
 *
 * @author Iroda Abdulazizova
 * @author Yawen Xue
 *
 * date: 22 Feb. 2023
 **/
public class Training {
    private List<String> states;
    private List<String> observations;
    private Map<String, HashMap<String, Double>> tProb; // transition probabilities
    private Map<String, HashMap<String, Double>> oProb; // observation probabilities

    public Training(String statesPath, String observationsPath) throws IOException {
        this.states = new ArrayList<>();
        this.observations = addToObservations(observationsPath);
        this.tProb = buildTProb(statesPath);
        this.oProb = buildOProb(states, observations);
    }

    public Map<String, HashMap<String, Double>> getTProb() { return tProb;}
    public Map<String, HashMap<String, Double>> getOProb() { return oProb;}

    /**
     * Reads tags file to build tProb
     * @param path
     * @return
     * @throws IOException
     */
    private Map<String, HashMap<String, Double>> buildTProb(String path) throws IOException {
        Map<String, HashMap<String, Double>> rmap = new HashMap<>();
        BufferedReader input = new BufferedReader(new FileReader(path));

        // Loop through lines in file
        String line = input.readLine();
        while (line != null) {

            // Add # to start
            String start = "# ";
            line = start.concat(line);
            String[] arr = line.split(" ");

            // Loop through strings in each line
            for (int i = 1; i < arr.length; i++) {
                String current = arr[i - 1];
                String next = arr[i];

                // Add to list of states if state is not #
                if (i != 0) { states.add(arr[i]);}

                // If transitions for current state already in map
                HashMap<String, Double> tCount = new HashMap<>();
                if (rmap.containsKey(current)) {
                    tCount = rmap.get(current);
                }

                // Map each transition to count
                if (!tCount.containsKey(next)) {
                    tCount.put(next, 1.0);
                } else {
                    tCount.put(next, tCount.get(next) + 1);
                }
                rmap.put(current, tCount);
            }
            line = input.readLine();
        }
        input.close();
        rmap = makeLog(rmap);
        return rmap;
    }

    /**
     * Uses lists of states & observations to build oProb
     * @param st
     * @param obs
     * @return
     */
    private Map<String, HashMap<String, Double>> buildOProb(List<String> st, List<String> obs) {
        HashMap<String, HashMap<String, Double>> rmap = new HashMap<>();

        // Loop through states & observations
        for (int i = 0; i < st.size(); i++) {
            String currentSt = st.get(i);
            String currentObs = obs.get(i);

            // If count for current observation already in map
            HashMap<String, Double> obsCount = new HashMap<>();
            if (rmap.containsKey(currentSt)) {
                obsCount = rmap.get(currentSt);
            }

            // Map each observation to count
            if (!obsCount.containsKey(currentObs)) {
                obsCount.put(currentObs, 1.0);
            } else {
                obsCount.put(currentObs, obsCount.get(currentObs) + 1);
            }
            //System.out.println(obsCount);
            rmap.put(currentSt, obsCount);
        }
        rmap = makeLog(rmap);
        return rmap;
    }

    /**
     * Helper method for buildOProb()
     * Reads sentences file to build list of observations
     * @param path
     * @return
     * @throws IOException
     */
    private List<String> addToObservations(String path) throws IOException {
        List<String> rlist = new ArrayList<>();
        BufferedReader input = new BufferedReader(new FileReader(path));

        // Loop through lines in file
        String line = input.readLine();
        while (line != null) {

            // Loop through strings in each line & add to list
            String[] arr = line.split(" ");
            for (String s : arr) {
                rlist.add(s.toLowerCase());
            }
            line = input.readLine();
        }
        input.close();
        return rlist;
    }

    /**
     * Currently, tProb & oProb strings to maps that map strings to counts
     * This method turns counts to probabilities using the lo function
     */
    private HashMap<String, HashMap<String, Double>> makeLog(Map<String, HashMap<String, Double>> probMap) {
        HashMap<String, HashMap<String, Double>> rmap = new HashMap<>();

        // Loop through all maps within map
        for (String k : probMap.keySet()) {
            HashMap<String, Double> innerMap = probMap.get(k);

            // Sum up counts of all transitions from the current states
            Double sum = 0.0;
            for (String s : innerMap.keySet()) {
                sum += innerMap.get(s);
            }

            // Set value of map tp log(value/sum)
            for (String s : innerMap.keySet()) {
                double v = innerMap.get(s);
                innerMap.put(s, Math.log(v/sum));
            }
            rmap.put(k, innerMap);
        }
        return rmap;
    }

    public static void main(String[] args) throws IOException {
        Training t = new Training("inputs/example-tags.txt", "inputs/example-sentences.txt");
        System.out.println(t.getTProb());
        System.out.println(t.getOProb());
    }
}
