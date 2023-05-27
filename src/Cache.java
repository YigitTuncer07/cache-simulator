import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Cache {

    private static Line cache[][];
    private static int s;
    private static int E;
    private static int b;

    public static void main(String[] args) throws FileNotFoundException {

        // Calculates S,E,B according to the input
        s = Integer.parseInt(args[1]);
        int S = (int) Math.pow(2, s);
        E = Integer.parseInt(args[3]);
        b = Integer.parseInt(args[5]);
        int B = (int) Math.pow(2, b);
        File traceFile = new File("traces/" + args[7]);


        System.out.println("S = " + S + "||E = " + E + "||B = " + B + "||File : " + traceFile.exists() + "\n");

        // Our cache is a 2D array of lines
        cache = new Line[S][E];

        //Instantiate the cache
        for (int i = 0; i < S; i++){
            for (int j = 0; j < E; j++){
                cache[i][j] = new Line();
            }
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
                    isHit = checkCache(address, s, b, E);
                    if (isHit == true) {

                    }

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
    private static boolean checkCache(String address, int s, int b, int E) {

        address = hexToBinary(address);

        int t = 32 - s - b;// How many bits the tag is
        String tag;
        int setIndex;
        int blockOffset;

        // Now, we must parse the address
        blockOffset = binaryToInteger(address.substring(t + s));
        setIndex = binaryToInteger(address.substring(t, t + s + 1));
        tag = address.substring(0, 32 - s - b);
        System.out.println(tag);

        Line currentSet[] = cache[setIndex];

        // Now, we search for our tag
        for (Line line : currentSet) {
            if ((line.getTag().equals(tag))) {
                if (line.getValidBit() == 1) {
                    return true;
                }
            }
        }
        return false;
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