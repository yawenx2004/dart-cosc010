import java.sql.Array;
import java.util.*;
import java.io.*;

/**
 * purpose: PS-5
 * performs Viterbi algorithm on given observation to recognize Parts of Speech
 *
 * @author Iroda Abdulazizova
 * @author Yawen Xue
 *
 * date: 20 Feb. 2023
 **/
public class Viterbi {
    private List<String> observations;      // the (stream of) string of input words that we need to parse
    private String tagsFile;
    private String sentencesFile;
    private String start = "#";             // the state from which we start reading
                                            // based on which we will choose the "best score" to proceed to;
    private Map<String, HashMap<String, Double>> oProb; // observation probabilities
    private Map<String, HashMap<String, Double>> tProb; // transition probabilities

    public Viterbi(String trainingStates, String trainingObservations) throws IOException{
        this.tagsFile = trainingStates;
        this.sentencesFile = trainingObservations;
        this.observations = new ArrayList<>();

        // training
        Training t = new Training(trainingStates, trainingObservations);
        this.tProb = t.getTProb();
        this.oProb = t.getOProb();
    }

    /**
     * console-driven testing
     * tags sentences the user inputs in the terminal
     */
    public void consoleTest() {
        boolean isRunning = true;
        Scanner input = new Scanner(System.in);
        while (isRunning == true) {

            // get sentence
            System.out.println("Please type in a sentence to check:");
            String sentence = input.nextLine();

            // split sentence into array
            String[] arr = sentence.split(" ");
            for (String s : arr) {
                observations.add(s.toLowerCase());
            }

            // run Viterbi and output string
            List<String> decodedPOS = decode();
            System.out.println(consoleTestOutput(arr, decodedPOS));

            // start new round
            observations = new ArrayList<>();
        }
    }

    /**
     * helper method for consoleTest()
     * concatenates output string
     * @param arr
     * @param decodedPOS
     * @return
     */
    public String consoleTestOutput(String[] arr, List<String> decodedPOS) {
        String s = "";
        for (int i = 0; i < arr.length; i++) {
            s = s.concat(arr[i]);
            s = s.concat("/");
            s = s.concat(decodedPOS.get(i));
            s = s.concat(" ");
        }
        return s;
    }

    /**
     * reads sentence file and writes another file with tags
     * @param outputPath
     * @throws IOException
     */
    public void fileTest(String outputPath) throws IOException {
        List<String> results = new ArrayList<>();
        BufferedReader input = new BufferedReader(new FileReader(sentencesFile));
        BufferedWriter output = new BufferedWriter(new FileWriter(outputPath));

        // loop through lines
        String line = input.readLine();
        while (line != null) {
            observations = new ArrayList<>();

            // split sentence into array
            String[] arr = line.split(" ");
            for (String s : arr) {
                observations.add(s.toLowerCase());
            }

            // turn observations into POS
            List<String> decodedPOS = decode();
            for(String s : decodedPOS) {
                results.add(s);
                output.write(s);
                output.write(" ");
            }
            output.write("\n");
            line = input.readLine();
        }
        input.close();
        output.close();

        // compare
        List<String> correctTags = new ArrayList<>();
        BufferedReader comparison = new BufferedReader(new FileReader(tagsFile));
        int correctCount = 0;
        int incorrectCount = 0;
        double percentageCorrect = 0.0;

        // loop through lines of tags file
        String tline = comparison.readLine();
        while (tline != null) {

            // split sentence into array
            String[] arr = tline.split(" ");
            for (String s : arr) {
                correctTags.add(s);
            }
            tline = comparison.readLine();
        }

        // compare the two files
        // for each string in the array from output at index i
        // compare it with the string in the array from input at index i
        for ( int i = 0; i < results.size(); i++) {
            if (results.get(i).equals(correctTags.get(i))) {
                correctCount++;
            } else {
                incorrectCount++;
            }
        }
        percentageCorrect = 100 * ((double) correctCount / results.size());
        System.out.println("Training complete! \n");
        System.out.println("Correct tags: " + correctCount);
        System.out.println("Incorrect tags: " + incorrectCount);
        System.out.println("Total tags: " + results.size());
        System.out.println("Correctness Percentage: " + percentageCorrect);
        comparison.close();
    }

    /**
     * uses Viterbi algorithm to tag POS
     * @return
     */
    public List<String> decode() {
        List<String> decodedPOS = new ArrayList<>();
        Map<String, Double> currScores = new HashMap<>();   // the scores for the current states
        Set<String> currStates = new HashSet<>();
        Set<String> nextStates = new HashSet<>();           // the next states from the current state
        Map<String, Double> nextScores = new HashMap<>();
        List<HashMap<String, String>> backTrack = new ArrayList<>();

        // begin
        currStates.add(start);
        currScores.put(start, 0.0);

        // loop through each observation
        for (String observation : observations) {
            HashMap<String, String> backTrackTemp = new HashMap<>();
            nextStates = new HashSet<>();
            nextScores = new HashMap<>();

            // loop through current states
            for (String currState : currStates) {

                // for each transition currState -> nextState
                Map<String, Double> transitions = tProb.get(currState);
                for (String transition : transitions.keySet()) {

                    // add nextState and nextScore
                    nextStates.add(transition);
                    Double transitionScore = transitions.get(transition);
                    Double observationScore = oProb.get(transition).get(observation);
                    if (observationScore == null) { observationScore = -100.0;}
                    double nextScore = currScores.get(currState) + transitionScore + observationScore;

                    // if nextState not in nextScores or nextScore > nextScore[nextState]
                    if (!nextScores.containsKey(transition) || nextScore > nextScores.get(transition)) {

                        // backtrack
                        HashMap<String, String> nextToCurr = new HashMap<>();
                        nextToCurr.put(transition, currState);
                        backTrackTemp.put(transition, currState);

                        // set nextScores[nextState] to nextScore
                        nextScores.put(transition, nextScore);
                    }
                }
            }
            backTrack.add(backTrackTemp);
            currStates = nextStates;
            currScores = nextScores;
        }

        // build decodedPOS from backTrack
        String lastState = returnMax(nextScores);
        decodedPOS.add(lastState);
        for (int i = backTrack.size() - 1; i >= 0; i--) {
            Map<String, String> currMap = backTrack.get(i);

            // add previous element (aka backtrack value) to front of list
            decodedPOS.add(0, currMap.get(lastState));
            lastState = currMap.get(lastState);
        }
        decodedPOS.remove(0);
        return decodedPOS;
    }

    /**
     * helper method for decode()
     * returns max value among nextScores, which is the correct POS
     * @param nextScores
     * @return
     */
    private String returnMax(Map<String, Double> nextScores) {
        String maxKey = "";
        double maxValue = 0.0;
        boolean isFirst = true; // only set key and value on the first loop
        for (String key : nextScores.keySet()) {
            if (isFirst == true) {
                maxKey = key;
                maxValue = nextScores.get(key);
                isFirst = false;
            }
            if (nextScores.get(key) > maxValue) {
                maxKey = key;
                maxValue = nextScores.get(key);
            }
        }
        return maxKey;
    }

    public static void main(String[] args) throws IOException {
        //Viterbi v = new Viterbi("inputs/example-tags.txt", "inputs/example-sentences.txt");
        Viterbi v = new Viterbi("inputs/brown-train-tags.txt", "inputs/brown-train-sentences.txt");
        //v.consoleTest();
        v.fileTest("outputs/brown-output.txt");
    }
}
