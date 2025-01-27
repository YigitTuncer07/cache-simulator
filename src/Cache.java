import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Cache {

    private static ArrayList<String> ram = new ArrayList<>();
    private static Line cache[][];
    private static ArrayList<ArrayList<Line>> queues = new ArrayList<>();
    private static int s;
    private static int E;
    private static int b;
    private static int hits;
    private static int misses;
    private static int evictions;
    private static boolean isModify;

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
        File traceFile = new File(args[7]);

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
        for (int i = 0; i < S; i++) {
            queues.add(new ArrayList<Line>());
        }

        Scanner scanner = new Scanner(traceFile);
        String currentCommand;

        // Commands are composed of an address, an int representing size, and sometimes
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

            switch (commandType) {
                case 'L':

                    isModify = false;
                    dataLoad(address, size);

                    break;
                case 'S':

                    isModify = false;
                    dataStore(address, size, data);

                    break;
                case 'M':
                    isModify = true;
                    System.out.println("M " + " " + address + ", "+ size + ", " + data);
                    dataLoad(address, size);
                    dataStore(address, size, data);

                    break;
            }
        }
        scanner.close();

        // Now we must print the cache into its file and print hits evictions and misses
        System.out.println("Hits: " + hits + " misses: " + misses + " evictions: " + evictions);
        printCache(S, E);

    }

    // Loads data into cache if it is not already in the cache
    private static void dataLoad(String address, int size) {
        String binAddress = hexToBinary(address);

        int t = 32 - s - b;// How many bits the tag is
        String tag;
        int setIndex;
        int blockOffset;

        // Now, we must parse the address
        blockOffset = binaryToInteger(binAddress.substring(t + s));
        setIndex = binaryToInteger(binAddress.substring(t, t + s));
        tag = binAddress.substring(0, 32 - s - b);

        // Get current set
        Line currentSet[] = cache[setIndex];

        // Now, we search for our tag in the lines of the set
        for (Line line : currentSet) {
            if (line.getValidBit() == 1) {
                if ((line.getTag().equals(tag))) {
                    if (isModify == true) {
                        System.out.println("  L " + address + ", " + size);
                        System.out.println("    Hit");
                    } else {
                        System.out.println("L " + address + ", " + size);
                        System.out.println("  Hit");
                    }

                    hits++;
                    return;
                }
            }
        }

        // The rest of the function only executes if a miss occured.
        String data = accessRam(address);
        ArrayList<Line> queu = queues.get(setIndex);// Gets out current queu

        if (isModify == true) {
            System.out.println("  L " + address + ", " + size);
            System.out.println("    Miss");
            System.out.println("    Place in cache");
        } else {
            System.out.println("L " + address + ", " + size);
            System.out.println("  Miss");
            System.out.println("  Place in cache" + "\n");
        }

        misses++;

        for (Line line : currentSet) {
            // If the valid bit of a line is 0, we can just write to there without evicting
            if (line.getValidBit() == 0) {
                line.setValidBit((byte) 1);
                line.setTag(tag);
                line.setData(data);
                queu.add(0, line);// Adds to start of queu
                return;
            }
        }

        // This part of the function only executes if there are no lines with invalid
        // bits in the set, and it evicts using the set quee so that it uses the FIFO
        // policy
        Line evictedLine = queu.get(queu.size() - 1);// Get last element
        queu.remove(queu.size() - 1);// Remove it from the queu
        evictedLine.setTag(tag);
        evictedLine.setData(data);
        queu.add(0, evictedLine);// Add to start of queu
        evictions++;

        return;

    }

    // Stores the data into the cache and memory if already in the cache, if not
    // straight to memory
    private static void dataStore(String address, int size, String data) {
        String binAddress = hexToBinary(address);

        int t = 32 - s - b;// How many bits the tag is
        String tag;
        int setIndex;
        int blockOffset;

        // Now, we must parse the address
        blockOffset = binaryToInteger(binAddress.substring(t + s));
        setIndex = binaryToInteger(binAddress.substring(t, t + s));
        tag = binAddress.substring(0, 32 - s - b);

        // Get current set
        Line currentSet[] = cache[setIndex];

        boolean isHit = false;

        // Now, we search for our tag in the lines of the set
        // If found, we edit its data
        for (Line line : currentSet) {
            if (line.getValidBit() == 1) {
                if ((line.getTag().equals(tag))) {
                    line.setData(editString(line.getData(), data, blockOffset));// Changes the data
                    isHit = true;
                }
            }
        }

        if (isHit) {
            if (isModify == true) {
                System.out.println("  S " + address + ", " + size + ", " + data);
                System.out.println("    Hit");
                System.out.println("    Stored in cache and ram"+ "\n");
                hits++;
            }else{
                System.out.println("S " + address + ", " + size + ", " + data);
                System.out.println("  Hit");
                System.out.println("  Stored in cache and ram" + "\n");
            }

        } else {
            if (isModify == true) {
                System.out.println("  S " + address + ", " + size + ", " + data);
                System.out.println("    Hit");
                System.out.println("    Stored in ram"+ "\n");
                hits++;
            }else{
                System.out.println("S " + address + ", " + size + ", " + data);
                System.out.println("  Hit");
                System.out.println("  Stored in ram" + "\n");
            }
        }

        // if it is found or not found in the cache, we still write to memory
        writeToRam(address, data);
    }

    private static void writeToRam(String address, String data) {
        int index = Integer.parseInt(address, 16);
        index = index / 8;
        if (index >= ram.size()) {
            return;
        }
        ram.set(index, editString(ram.get(index), data, 0));
    }

    // Replaces the characters after blockOffset in the originalString with data.
    private static String editString(String originalString, String data, int blockOffset) {
        StringBuilder stringBuilder = new StringBuilder(originalString);
        stringBuilder.replace(blockOffset, blockOffset + data.length(), data);
        return stringBuilder.toString();
    }

    // Returns 0 if a non existant adress is accesed.
    private static String accessRam(String address) {
        int intAdress = Integer.parseInt(address, 16);
        int index = intAdress;
        index = index / 8;
        if (index >= ram.size()) {
            return "0000000000000000";
        }
        String data = "";
        int offset = intAdress % 8;

        if (b == 0) {
            data = ram.get(index).substring(2 * offset, 2 * offset + 2);
        } else if (b == 1) {
            if (offset == 0 || offset == 1) {
                data = ram.get(index).substring(0, 4);
            } else if (offset == 2 || offset == 3) {
                data = ram.get(index).substring(4, 8);
            } else if (offset == 4 || offset == 5) {
                data = ram.get(index).substring(8, 12);
            } else {
                data = ram.get(index).substring(12);
            }
            data = ram.get(index).substring(offset, offset + 4);
        } else if (b == 2) {
            if (offset > 3) {
                data = ram.get(index).substring(0, 8);
            } else {
                data = ram.get(index).substring(8);
            }
        } else {
            data = ram.get(index);
        }
        return data;
    }

    // Prints to cache.txt
    private static void printCache(int S, int E) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter("cache.txt");

        for (int i = 0; i < S; i++) {
            writer.println("SET " + i + ": ");
            for (int j = 0; j < E; j++) {
                writer.println(cache[i][j].toString());
            }
            writer.println("--------------------------");
        }
        writer.close();
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