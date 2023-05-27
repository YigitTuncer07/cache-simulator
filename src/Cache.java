import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Cache {

    private static String cache[][];

    public static void main(String[] args) throws FileNotFoundException {

        // Calculates S,E,B according to the input
        int s = Integer.parseInt(args[1]);
        int S = (int) Math.pow(2, s);
        int E = Integer.parseInt(args[3]);
        int b = Integer.parseInt(args[5]);
        int B = (int) Math.pow(2, b);
        File traceFile = new File("traces/" + args[7]);

        System.out.println("S = " + S + "||E = " + E + "||B = " + B + "||File : " + traceFile.exists() + "\n");

        // Our cache is a 2D array, where sets are the first dimension and lines the
        // second
        cache = new String[S][E];

        Scanner scanner = new Scanner(traceFile);
        String currentCommand;
        boolean isHit;


        //Commands are composed of an address, a int representing size, and sometimes some data
        char commandType;
        String address;
        int size;
        String data = null;

        while (scanner.hasNextLine()) {

            currentCommand = scanner.nextLine();

            //This part parses the command
            commandType = currentCommand.charAt(0);
            address = currentCommand.substring(2,10);
            size = Character.getNumericValue(currentCommand.charAt(12));
            if (commandType != 'L'){
                data = currentCommand.substring(15);
            }

            System.out.println("Command Type: " + commandType);
            System.out.println("Address: " + address);
            System.out.println("Size: " + size);
            System.out.println("Data: " + data + "\n");


            switch (commandType) {
                case 'L':
                    isHit = checkCache("temp");
                    if (isHit == true){

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

    private static boolean checkCache(String address){

        return true;
    }
}