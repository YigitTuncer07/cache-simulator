import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class Cache {

    private static ArrayList<String> ram = new ArrayList<>();
    private static Line cache[][];
    private static Queue<Line> queues[];
    private static int s;
    private static int E;
    private static int b;
    private static int hits;
    private static int misses;
    private static int evictions;

    public static void main(String[] args) throws IOException {

        // Loads the ram into memory
        InputStream inputStream = new FileInputStream("RAM.dat");
        int mem = 0;
        int byteRead = -1;
        String currentHex = "";
        String currentString = "";
        while ((byteRead = inputStream.read()) != -1) {

            currentString = intToHex(byteRead);
            mem++;

            if (currentString.length() == 1) {// Pads with 0 if 00
                currentString = "0" + currentString;
            }

            currentHex = currentHex + currentString;

            if (mem % 8 == 0) {
                ram.add(currentHex);
                currentHex = "";
            }
        }
        inputStream.close();

        // Calculates S,E,B according to the command line arguments
        s = Integer.parseInt(args[1]);
        int S = (int) Math.pow(2, s);
        E = Integer.parseInt(args[3]);
        b = Integer.parseInt(args[5]);
        int B = (int) Math.pow(2, b);
        File traceFile = new File("traces/" + args[7]);

        System.out.println("S = " + S + "||E = " + E + "||B = " + B + "||File : " + traceFile.exists() + "\n");

        // Our cache is a 2D array of lines
        cache = new Line[S][E];

        // Instantiate the cache
        for (int i = 0; i < S; i++) {
            for (int j = 0; j < E; j++) {
                cache[i][j] = new Line();
            }
        }

        // Create queues for the sets of the cache, so that we know which line to evict.
        queues = new Queue[S];

        for (int i = 0; i < S; i++) {
            queues[i] = new PriorityQueue<Line>();
        }

        Scanner scanner = new Scanner(traceFile);
        String currentCommand;
        boolean isHit;

        // Commands are composed of an address, a int representing size, and sometimes
        // some data
        char commandType;
        String address;
        int size;
        String data = null;

        while (scanner.hasNextLine()) {

            currentCommand = scanner.nextLine();

            // This part parses the command
            commandType = currentCommand.charAt(0);
            address = currentCommand.substring(2, 10);
            size = Character.getNumericValue(currentCommand.charAt(12));
            if (commandType != 'L') {
                data = currentCommand.substring(15);
            }

            // System.out.println("Command Type: " + commandType);
            // System.out.println("Address: " + address);
            // System.out.println("Size: " + size);
            // System.out.println("Data: " + data + "\n");

            switch (commandType) {
                case 'L':

                    dataLoad(address, size);

                    break;
                case 'S':

                    break;
                case 'M':

                    break;
            }
        }
        scanner.close();
    }

    // s is how many bits the set part is, and b is how many bits the block offset
    // part is. E is how many lines there are per set
    private static void dataLoad(String address, int size) {

        String binAddress = hexToBinary(address);

        int t = 32 - s - b;// How many bits the tag is
        String tag;
        int setIndex;
        int blockOffset;

        // Now, we must parse the address
        blockOffset = binaryToInteger(binAddress.substring(t + s));
        setIndex = binaryToInteger(binAddress.substring(t, t + s + 1));
        tag = binAddress.substring(0, 32 - s - b);
        System.out.println(tag);

        // Get current set
        Line currentSet[] = cache[setIndex];

        // Now, we search for our tag in the lines of the set
        for (Line line : currentSet) {
            if (line.getValidBit() == 1) {
                if ((line.getTag().equals(tag))) {
                    hits++;
                    return;
                }
            }
        }

        // The rest of the function only executes if a miss occured.
        String data = accessRam(address);

        for (Line line : currentSet) {
            // If the valid bit of a line is 0, we can just write to there without evicting
            if (line.getValidBit() == 0) {
                line.setValidBit((byte) 1);
                line.setTag(tag);
                line.setData(data);
                queues[setIndex].add(line);
                return;
            }
        }

        // This part of the function only executes if there are no lines with invalid
        // bits in the set, and it evicts using the set quee so that it uses the FIFO
        // policy
        Line evictedLine = queues[setIndex].remove();
        evictedLine.setTag(tag);
        evictedLine.setData(data);
        queues[setIndex].add(evictedLine);
        evictions++;
        return;

    }

    private static String accessRam(String address) {
        int index = Integer.parseInt(address, 16);
        index = index / 8;
        return ram.get(index);
    }

    private static String intToHex(int number) {
        // Convert the integer to hexadecimal string
        String hexString = Integer.toHexString(number);
        return hexString;
    }

    private static int binaryToInteger(String string) {
        int result = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '1') {
                result += Math.pow(2, string.length() - i - 1);
            }

        }
        return result;
    }

    private static String hexToBinary(String hexString) {
        int decimal = Integer.parseInt(hexString, 16);
        String binaryString = Integer.toBinaryString(decimal);
        StringBuilder paddedBinary = new StringBuilder(binaryString);
        while (paddedBinary.length() < 32) {
            paddedBinary.insert(0, "0");
        }
        return paddedBinary.toString();
    }
}