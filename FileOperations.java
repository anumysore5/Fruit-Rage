import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

class FileOperations {
    BufferedReader reader;
    BufferedReader calibrateReader;
    private HashMap<Integer, String> mapping;

    public FileOperations() {
        mapping = new HashMap<>();
        fillColMapping(mapping);
        try {
            calibrateReader = new BufferedReader(new FileReader("calibration.txt"));
            reader = new BufferedReader(new FileReader("input.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("Could not find \"input.txt\" in the current directory");
        }
    }

    public HashMap<Integer, String> getMapping() {
        return mapping;
    }

    private void fillColMapping(HashMap<Integer, String> mapping) {
        mapping.put(0, "A");
        mapping.put(1, "B");
        mapping.put(2, "C");
        mapping.put(3, "D");
        mapping.put(4, "E");
        mapping.put(5, "F");
        mapping.put(6, "G");
        mapping.put(7, "H");
        mapping.put(8, "I");
        mapping.put(9, "J");
        mapping.put(10, "K");
        mapping.put(11, "L");
        mapping.put(12, "M");
        mapping.put(13, "N");
        mapping.put(14, "O");
        mapping.put(15, "P");
        mapping.put(16, "Q");
        mapping.put(17, "R");
        mapping.put(18, "S");
        mapping.put(19, "T");
        mapping.put(20, "U");
        mapping.put(21, "V");
        mapping.put(22, "W");
        mapping.put(23, "X");
        mapping.put(24, "Y");
        mapping.put(25, "Z");
    }

    public void readInputFile(FruitRage fruitRage) throws IOException {
        int size = Integer.parseInt(reader.readLine());
        fruitRage.setBoardSize(size);

        int types = Integer.parseInt(reader.readLine());
        fruitRage.setNoOfFruitTypes(types);

        double timeRemaining = Double.parseDouble(reader.readLine());
        fruitRage.setGivenTime(timeRemaining);

        int [][] array = new int[size][size];
        String string;
        for(int i=0; i<size; i++) {
            string = reader.readLine();
            for(int j=0; j<size; j++) {
                if(string.charAt(j) == '*') {
                    array[i][j] = Character.getNumericValue(string.charAt(j));
                } else {
                    array[i][j] = Integer.parseInt(String.valueOf(string.charAt(j)));
                }
            }
            string = null;
        }
        fruitRage.setCurrentState(array);
    }

    public void writeOutputFile(int[][] finalMove, int finalRow, int finalCol) {
        Path path = Paths.get("output.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path))
        {
            String columnValue = this.getMapping().get(finalCol);
            String rowNumber = String.valueOf(finalRow+1);
            writer.write(String.valueOf(columnValue)+String.valueOf(rowNumber));
            writer.write("\n");
            displayBoard(finalMove, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayBoard(int[][] modifiedBorad, BufferedWriter writer) throws IOException {
        int[][] board = modifiedBorad;
        int boardSize = modifiedBorad.length;
        for(int i=0; i<boardSize; i++) {
            for(int j=0; j<boardSize; j++) {
                if(board[i][j] == -1) {
                    writer.write("*");
                } else {
                    writer.write(String.valueOf(board[i][j]));
                }
            }
            writer.write("\n");
        }
    }

    public void readCalibrationInput(FruitRage homework) {
        double nodeProcessingRate = 0.0;
        try {
            nodeProcessingRate = Double.parseDouble(calibrateReader.readLine());
            homework.setNodeProcessingRate(Math.floor(nodeProcessingRate));
        } catch (FileNotFoundException e) {
            System.out.println("File \"calibration.txt\" not found");
        } catch (IOException e) {
            System.out.println("Error in reading \"calibration.txt\"");
        }
    }
}