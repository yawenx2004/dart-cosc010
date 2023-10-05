import java.util.*;
import java.io.*;

/**
 * purpose: ps-3 huffman implementation
 *
 * @author yawen xue
 * 06 feb. 23
 */

public class HuffmanImplementation implements Huffman {
    public Map<Character, Long> countFrequencies(String pathName) throws IOException {
        Map<Character, Long> frequencyTable = new TreeMap<Character, Long>();

        // Reads file & loops over characters to make frequency table
        BufferedReader input = new BufferedReader(new FileReader(pathName));
        int n = 0;  // characters
        while ((n = input.read()) != -1) {
            char character = (char)n;

            // Adds character to frequency table
            if (frequencyTable.containsKey(character)) {
                frequencyTable.put(character, (long)(frequencyTable.get(character) + 1));
            } else {
                frequencyTable.put(character, (long)1);
            }
        }
        input.close();
        return frequencyTable;
    }

    public BinaryTree<CodeTreeElement> makeCodeTree(Map<Character, Long> frequencies) {
        PriorityQueue<BinaryTree<CodeTreeElement>> q = initialTreeQueue(frequencies);    // Priority queue of initial trees

        // Safety check: boundary case of empty input file
        if (q.size() == 0) return null;

        // Loops through priority queue & build binary tree
        else {
            while (q.size() > 1) {

                // Extract lowest-frequency elements
                BinaryTree<CodeTreeElement> t1 = q.remove();
                BinaryTree<CodeTreeElement> t2 = q.remove();

                // Make new tree with node r (frequency = frequency of t1 + frequency of t2)
                CodeTreeElement r = new CodeTreeElement(t1.data.getFrequency() + t2.data.getFrequency(), null);
                BinaryTree<CodeTreeElement> t = new BinaryTree<CodeTreeElement>(r, t1, t2);

                // Insert t into priority queue
                q.add(t);
            }
            return q.remove();  // Only element left in the priority queue is the complete binary tree
        }
    }

    /**
     * Helper method for makeCodeTree() — builds priority queue of initial trees
     *
     * @param frequencies
     * @return priority queue of initial trees, where trees with the smallest frequencies are extracted first
     */
    private PriorityQueue<BinaryTree<CodeTreeElement>> initialTreeQueue(Map<Character, Long> frequencies) {

        // Comparator class for priority queue
        Comparator<BinaryTree<CodeTreeElement>> TreeComparator = new Comparator<BinaryTree<CodeTreeElement>>() {
            public int compare(BinaryTree<CodeTreeElement> t1, BinaryTree<CodeTreeElement> t2) {
                int i = 0;

                // If first tree has smaller frequency, return -1
                if (t1.getData().getFrequency() < t2.getData().getFrequency()) i = -1;

                // If frequency counts are equal, return 0
                else if (t1.getData().getFrequency() == t2.getData().getFrequency()) i = 0;

                // If second tree has smaller frequency, return 1
                else if (t1.getData().getFrequency() > t2.getData().getFrequency()) i = 1;
                return i;
            }
        };

        // Adds map entries to priority queue based on comparator
        PriorityQueue<BinaryTree<CodeTreeElement>> initialTrees = new PriorityQueue<BinaryTree<CodeTreeElement>>(TreeComparator);
        for (Map.Entry<Character, Long> entry : frequencies.entrySet()) {
            CodeTreeElement e = new CodeTreeElement(entry.getValue(), entry.getKey());
            initialTrees.add(new BinaryTree<CodeTreeElement>(e));
        }
        return initialTrees;
    }

    public Map<Character, String> computeCodes(BinaryTree<CodeTreeElement> codeTree) {
        Map<Character, String> codeMap = new TreeMap<Character, String>();

        // Adds to code map, 0 for left & 1 for right
        if (codeTree != null) {
            if (codeTree.size() > 1) {
                String code = new String();                 // Avoids creating new string with every round of recursion
                traverseCodeTree(codeTree, code, codeMap);  // Helper method adds to map
            }

            // Safety check: boundary case of 1 character
            else if (codeTree.size() == 1) {
                codeMap.put(codeTree.getData().getChar(), "0");
            }
        }
        return codeMap;
    }

    /**
     * Helper method for computeCodes that adds to map of codes
     * Each code: 0 for left movement, 1 for right movement
     *
     * @param codeTree
     * @param code
     * @param codeMap
     */
    public void traverseCodeTree(BinaryTree<CodeTreeElement> codeTree, String code, Map<Character, String> codeMap) {

        // If tree has children, recurse with children
        if (codeTree.hasLeft()) {
            code = code + "0";
            traverseCodeTree(codeTree.getLeft(), code, codeMap);
        }
        if (codeTree.hasRight()) {
            if (codeTree.hasLeft()) code = code.substring(0, code.length() - 1);    // Avoids repetition from left tree
            code = code + "1";
            traverseCodeTree(codeTree.getRight(), code, codeMap);
        }

        // Adds characters and codes to map when leaves are reached
        if (codeTree.isLeaf()) {
            codeMap.put(codeTree.getData().getChar(), code);
        }
    }

    public void compressFile(Map<Character, String> codeMap, String pathName, String compressedPathName) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(pathName));
        BufferedBitWriter bitOutput = new BufferedBitWriter(compressedPathName);

        // Loops through all characters
        int n = 0;  // characters
        while ((n = input.read()) != -1) {
            char key = (char)n;
            String code = codeMap.get(key);

            // Loops through each code & write corresponding bits (0 = false, 1 = true)
            for (int i = 0; i < code.length(); i++) {
                boolean bit = (code.charAt(i) == '1');
                bitOutput.writeBit(bit);
            }
        }
        input.close();
        bitOutput.close();
    }

    public void decompressFile(String compressedPathName, String decompressedPathName, BinaryTree<CodeTreeElement> codeTree) throws IOException {
        BufferedBitReader bitInput = new BufferedBitReader(compressedPathName);
        BufferedWriter output = new BufferedWriter(new FileWriter(decompressedPathName));

        // Keeps copy of original code tree
        BinaryTree<CodeTreeElement> original = codeTree;

        // Loops through all bits
        while (bitInput.hasNext()) {
            boolean bit = bitInput.readBit();
            if (codeTree != null) {

                // Safety check: boundary chase of 1 character
                if (original.size() == 1) {
                    output.write(original.getData().getChar());
                }
                else {

                    // Parses through code tree according to bits
                    if (bit == true) codeTree = codeTree.getRight();
                    if (bit == false) codeTree = codeTree.getLeft();

                    // When leaf is reached, writes character onto output file & resets code tree to original copy
                    if (codeTree != null) {
                        if (codeTree.isLeaf()) {
                            output.write(codeTree.getData().getChar());
                            codeTree = original;
                        }
                    }
                }
            }
        }
        bitInput.close();
        output.close();
    }

    // Concise version of functions to avoid too many lines of code in main method:
    public void compressAndDecompress(String pathName, String compressedPathName, String decompressedPathName) throws IOException {
        Map<Character, Long> frequencyTable = countFrequencies(pathName);
        BinaryTree<CodeTreeElement> codeTree = makeCodeTree(frequencyTable);
        Map<Character, String> codeMap = computeCodes(codeTree);
        compressFile(codeMap, pathName, compressedPathName);
        decompressFile(compressedPathName, decompressedPathName, codeTree);
    }

    public static void main(String[] args) throws IOException {
        HuffmanImplementation huff = new HuffmanImplementation();

        // Test case #1: empty file
        huff.compressAndDecompress("inputs/HuffTest1.txt", "outputs/HuffTest1_compressed.txt", "outputs/HuffTest1_decompressed.txt");

        // Test case #2: 1 character
        huff.compressAndDecompress("inputs/HuffTest2.txt", "outputs/HuffTest2_compressed.txt", "outputs/HuffTest2_decompressed.txt");

        // Test case #3: short text
        huff.compressAndDecompress("inputs/HuffTest3.txt", "outputs/HuffTest3_compressed.txt", "outputs/HuffTest3_decompressed.txt");

        // Test case #4: Constitution
        huff.compressAndDecompress("inputs/USConstitution.txt", "outputs/USConstitution_compressed.txt", "outputs/USConstitution_decompressed.txt");

        // Test case #5: War and Peace
        huff.compressAndDecompress("inputs/WarAndPeace.txt", "outputs/WarAndPeace_compressed.txt", "outputs/WarAndPeace_decompressed.txt");
    }
}
