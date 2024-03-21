// --------------Section 1: Import Statements------------------
import java.io.*;
import java.util.*;
// import java.util.PriorityQueue;
// import java.util.HashMap;


// ------------------Section 2: HuffmanNode Class----------------
class HuffmanNode implements Comparable<HuffmanNode> {
    char data;
    int frequency;
    HuffmanNode left, right;

    public HuffmanNode(char data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        left = right = null;
    }

    @Override
    public int compareTo(HuffmanNode node) {
        return this.frequency - node.frequency;
    }
}

// ----------------Section 3: HuffmanCompression Class-------------------

  // Class to handle Huffman compression and decompression
// huffmanCodes is a static variable to store the Huffman codes for each character
public class HuffmanCompression {

    private static HashMap<Character, String> huffmanCodes = new HashMap<>();
    public static void main(String[] args) {
        String inputFileName = "D:\\PROJECTS\\huffman\\input.txt";
        String compressedFileName = "D:\\PROJECTS\\huffman\\compressed.bin";
        String decompressedFileName = "D:\\PROJECTS\\huffman\\decompressed.txt";

        // Step 1: Read input file and build frequency table
        HashMap<Character, Integer> frequencyTable = buildFrequencyTable(inputFileName);

        // Step 2: Build Huffman Tree
        HuffmanNode root = buildHuffmanTree(frequencyTable);

        // Step 3: Build Huffman Codes
        buildHuffmanCodes(root, new StringBuilder());

        // Step 4: Compress the input file
        compress(inputFileName, compressedFileName);

        // Step 5: Decompress the compressed file
        decompress(compressedFileName, decompressedFileName);

        System.out.println("Compression and Decompression completed successfully.");
    }

    //------------Section 4: buildFrequencyTable Method----------------

    // This method reads the input file character by character, counts the frequency of each character, and stores it in a hash map.

    private static HashMap<Character, Integer> buildFrequencyTable(String fileName) {

        HashMap<Character, Integer> frequencyTable = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            int currentChar;
            while ((currentChar = br.read()) != -1) {
                char character = (char) currentChar;
                frequencyTable.put(character, frequencyTable.getOrDefault(character, 0) + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return frequencyTable;
    }

    //---------------- Section 5: buildHuffmanTree Method-------------

    private static HuffmanNode buildHuffmanTree(HashMap<Character, Integer> frequencyTable) {
       // Method to build the Huffman Tree using a priority queue
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();

        // Create a leaf node for each character and add it to the priority queue until a single root node is obtained.
        frequencyTable.forEach((character, frequency) ->
                priorityQueue.add(new HuffmanNode(character, frequency)));

        // Build the Huffman Tree
        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();

            HuffmanNode newNode = new HuffmanNode('\0', left.frequency + right.frequency);
            newNode.left = left;
            newNode.right = right;

            priorityQueue.add(newNode);
        }

        // The remaining node is the root of the Huffman Tree
        return priorityQueue.poll();
    }

    //----------------Section 6: buildHuffmanCodes Method------------

    // This method recursively traverses the Huffman Tree and assigns Huffman codes to each character based on their position in the tree.

    private static void buildHuffmanCodes(HuffmanNode root, StringBuilder code) {
        if (root == null) {
            return;
        }

        if (root.data != '\0') {
            huffmanCodes.put(root.data, code.toString());
        }

        code.append('0');
        buildHuffmanCodes(root.left, code);
        code.deleteCharAt(code.length() - 1);

        code.append('1');
        buildHuffmanCodes(root.right, code);
        code.deleteCharAt(code.length() - 1);
    }

    // --------------------Section 7: compress Method-------------------

    // This method compresses the input file by converting each character into its corresponding Huffman code and then storing the binary representation in a byte array.

    private static void compress(String inputFileName, String compressedFileName) {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFileName));
             ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(compressedFileName))) {

            int currentChar;
            StringBuilder compressedData = new StringBuilder();

            while ((currentChar = inputStream.read()) != -1) {
                char character = (char) currentChar;
                compressedData.append(huffmanCodes.get(character));
            }

            // Convert the binary string to bytes
            byte[] compressedBytes = getBytesFromString(compressedData.toString());

            // Write the Huffman codes and compressed data to the output file
            outputStream.writeObject(huffmanCodes);
            outputStream.writeObject(compressedBytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------------Section 8: decompress Method-------------------

    // This method decompresses the compressed file by reading Huffman codes and compressed data, converting the binary string back to bytes, and then writing the decompressed data to the output file.

    private static void decompress(String compressedFileName, String decompressedFileName) {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(compressedFileName));
             BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(decompressedFileName))) {

            // Read Huffman codes and compressed data from the input file
            huffmanCodes = (HashMap<Character, String>) inputStream.readObject();
            byte[] compressedBytes = (byte[]) inputStream.readObject();

            // Convert bytes to binary string
            StringBuilder binaryString = new StringBuilder();
            for (byte b : compressedBytes) {
                binaryString.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
            }

            // Decompress the binary string and write to the output file
            decompressBinaryString(binaryString.toString(), outputStream);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


// ------------Section 9: getBytesFromString Method------------

// This method converts a binary string to bytes by grouping the binary digits into bytes of 8 bits each.

    private static byte[] getBytesFromString(String binaryString) {
        int length = (int) Math.ceil(binaryString.length() / 8.0);
        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++) {
            int endIndex = Math.min((i + 1) * 8, binaryString.length());
            String byteString = binaryString.substring(i * 8, endIndex);
            bytes[i] = (byte) Integer.parseInt(byteString, 2);
        }

        return bytes;
    }


// ------------Section 10: decompressBinaryString Method---------------

// Method to decompress a binary string and write the decompressed data to the output stream
    private static void decompressBinaryString(String binaryString, BufferedOutputStream outputStream) throws IOException {
        StringBuilder currentCode = new StringBuilder();
        for (char bit : binaryString.toCharArray()) {
            currentCode.append(bit);
            for (char key : huffmanCodes.keySet()) {
                if (huffmanCodes.get(key).equals(currentCode.toString())) {
                    outputStream.write((int) key);
                    currentCode.setLength(0);
                    break;
                }
            }
        }
        outputStream.flush();
    }
}
