import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FruitRage {
    private int boardSize;
    private int noOfFruitTypes;
    private double givenTime;
    private int[][] currentState;
    private FileOperations file;
    private int finalRow=-1, finalCol=-1;
    int noOfChildNodes = 0;
    long startTimeInMillisec;
    private double nodeProcessingRate;
    private int depthLimit;
    private int currentDepth;

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public int getNoOfFruitTypes() {
        return noOfFruitTypes;
    }

    public void setNoOfFruitTypes(int noOfFruitTypes) {
        this.noOfFruitTypes = noOfFruitTypes;
    }

    public double getGivenTime() {
        return givenTime;
    }

    public void setGivenTime(double givenTime) {
        this.givenTime = givenTime;
    }

    public int[][] getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int[][] currentState) {
        this.currentState = currentState;
    }

    public FileOperations getFileObject() {
        return file;
    }

    public int getFinalRow() {
        return finalRow;
    }

    public void setFinalRow(int finalRow) {
        this.finalRow = finalRow;
    }

    public int getFinalCol() {
        return finalCol;
    }

    public void setFinalCol(int finalCol) {
        this.finalCol = finalCol;
    }

    public int getDepthLimit() {
        return depthLimit;
    }

    public void setDepthLimit(int depthLimit) {
        this.depthLimit = depthLimit;
    }

    public double getNodeProcessingRate() {
        return nodeProcessingRate;
    }

    public void setNodeProcessingRate(double nodeProcessingRate) {
        this.nodeProcessingRate = nodeProcessingRate;
    }

    public FruitRage() {
        startTimeInMillisec = System.currentTimeMillis();
        currentDepth = 0;
        file = new FileOperations();
        file.readCalibrationInput(this);

        try {
            file.readInputFile(this);
        } catch (FileNotFoundException e) {
            System.out.println("File \"input.txt\" not found");
        } catch(IOException e) {
            System.out.println("Error in reading \"input.txt\"");
        }
    }

    public static void main(String[] args) {
//        long start = System.nanoTime();
        FruitRage fruitRage = new FruitRage();
        fruitRage.play();
//        long end = System.nanoTime();
//        System.out.println("Total startTimeInMillisec taken: " + ((end-start)/1000000000)+"secs");
    }

    public double Max_Value(int[][] currentBoard, double alpha, double beta, int[][] finalMove, int currentDepth) {
        // check if game over
        if(isGameOver(currentBoard)) {
            return 0;
        }

//        if(this.currentDepth == this.getDepthLimit())
//            return 0;

//        this.currentDepth++;
        currentDepth++;
        LinkedList<List<String>> clusters = getAllPossibleClusters(currentBoard);
        Collections.sort(clusters, new sortLinkedList());
        int clusterNoToRemember = -1, clusterNo=0;
        double res=Double.NEGATIVE_INFINITY;

        while (clusterNo < clusters.size()) {
            noOfChildNodes++;
            int[][] modifiedBorad = new int[currentBoard.length][currentBoard.length];
            List<String> cluster = clusters.get(clusterNo);
            clusterNo++;

            if(currentDepth == this.getDepthLimit())
                return (cluster.size() * cluster.size());

            int score = cluster.size() * cluster.size();
            modifiedBorad = InsertStars(cluster, currentBoard);
            applyGravity(modifiedBorad);
            double temp = Min_Value(modifiedBorad, alpha, beta, finalMove, currentDepth);

            if(currentDepth==1 && res<temp+score) {
                String[] selectedMove = cluster.get(0).split(",");
                this.setFinalRow(Integer.parseInt(selectedMove[0]));
                this.setFinalCol(Integer.parseInt(selectedMove[1]));
                for (int i = 0; i < modifiedBorad.length; i++) {
                    finalMove[i] = new int[modifiedBorad.length];
                    System.arraycopy(modifiedBorad[i], 0, finalMove[i], 0, modifiedBorad.length);
                }
            }
            res = Math.max(res, temp+score);

            // for alpha-beta pruning
            if(res >= beta) {
                return res;
            }

//            if(alpha < res) {
//                clusterNoToRemember = clusterNo;
//            }
            alpha = Math.max(alpha, res);
        }

        // deep copy the final board to be displayed in output.txt
//        for (int k=1; k<=clusters.size(); k++) {
//            if(k == clusterNoToRemember) {
//                int[][] requiredBoard = new int[currentBoard.length][currentBoard.length];
//                List<String> cluster = clusters.get(k-1);
//                String[] selectedMove = cluster.get(0).split(",");
//                this.setFinalRow(Integer.parseInt(selectedMove[0]));
//                this.setFinalCol(Integer.parseInt(selectedMove[1]));
//                requiredBoard = InsertStars(cluster, currentBoard);
//                applyGravity(requiredBoard);
//                for (int i = 0; i < requiredBoard.length; i++) {
//                    finalMove[i] = new int[requiredBoard.length];
//                    System.arraycopy(requiredBoard[i], 0, finalMove[i], 0, requiredBoard.length);
//                }
//                break;
//            }
//        }

        return res;
    }

    public double Min_Value(int[][] currentBoard, double alpha, double beta, int[][] finalMove, int currentDepth) {
        if(isGameOver(currentBoard)) {
            return 0;
        }

//        if(this.currentDepth == this.getDepthLimit())
//            return 0;

        LinkedList<List<String>> clusters = getAllPossibleClusters(currentBoard);
        Collections.sort(clusters, new sortLinkedList());
        double res= Double.POSITIVE_INFINITY;
//        this.currentDepth++;
        currentDepth++;

        while (!clusters.isEmpty()) {
            noOfChildNodes++;
            int[][] modifiedBorad = new int[currentBoard.length][currentBoard.length];
            List<String> cluster = clusters.removeFirst();
            int score = -(cluster.size() * cluster.size());

            if(currentDepth == this.getDepthLimit())
                return -(cluster.size() * cluster.size());

            modifiedBorad = InsertStars(cluster, currentBoard);
            applyGravity(modifiedBorad);
            res = Math.min(res, Max_Value(modifiedBorad, alpha, beta, finalMove, currentDepth)+score);

            // for alpha-beta pruning
            if(res <= alpha) {
                return res;
            }
            beta = Math.min(beta, res);
        }

        return res;
    }

    protected int[][] InsertStars(List<String> strings, int[][] currentBoard) {
        int[][] modifiedBorad = new int[currentBoard.length][];
        String [] strArray;
        int row; // row
        int col; // column
        for(int i=0; i<currentBoard.length; i++) {
            modifiedBorad[i] = new int[currentBoard.length];
            System.arraycopy(currentBoard[i], 0, modifiedBorad[i], 0, currentBoard.length);
        }
        for(String s: strings) {
            strArray = s.split(",");
            row = Integer.parseInt(strArray[0]); // row
            col = Integer.parseInt(strArray[1]); // column
            modifiedBorad[row][col] = -1;
        }
        return modifiedBorad;
    }

    private double play() {
        int [][] currentBoard = this.getCurrentState();
        calculateDepthLimit(currentBoard);
//        System.out.println("Depth Limit being set = " + this.getDepthLimit());
        int [][] finalMove = new int[currentBoard.length][];
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        int currentDepth = 0;
        alpha = Max_Value(currentBoard, alpha, beta, finalMove, currentDepth);
//        System.out.println("Number of child nodes = " + noOfChildNodes);
//        System.out.println("Final alpha value = " + alpha);
//        System.out.println("Final row value = " + this.getFinalRow());
//        System.out.println("Final col value = " + this.getFinalCol());
//        System.out.println("Final board is:");
//        displayBoard(finalMove);
        if(finalRow != -1 && finalCol!=-1)
            this.getFileObject().writeOutputFile(finalMove, this.getFinalRow(), this.getFinalCol());
        return alpha;
    }

    private void calculateDepthLimit(int[][] currentBoard) {
        LinkedList<List<String>> clusters = getAllPossibleClusters(currentBoard);
        int noOfClusters = clusters.size();
        double remainingTimeForGame = this.getGivenTime();
        double relaxationPercent = 0.75;
        double nodeProcessingRate = this.getNodeProcessingRate();
        double depth = Math.log((nodeProcessingRate*remainingTimeForGame)/(2*relaxationPercent)) / Math.log(noOfClusters);
        if((int)Math.floor(depth) > 5) {
            this.setDepthLimit(5);
        } else {
            this.setDepthLimit((int)Math.floor(depth));
        }
    }

    public void applyGravity(int[][] modifiedBoard) {
        int[][] board = modifiedBoard;
        int boardSize = modifiedBoard.length;
        LinkedList<Integer> colValues = new LinkedList<>();
        for(int j=0; j<boardSize; j++) {    // scan column wise
            for(int i=0; i<boardSize; i++) {
                if(board[i][j] == -1) // means this is a '*'
                    continue;
                colValues.addFirst(board[i][j]);
            }
            for (int k=boardSize-1; k>=0; k--) {
                if (!colValues.isEmpty()) {
                    board[k][j] = colValues.removeFirst();
                    continue;
                }
                board[k][j] = -1;
            }
        }
    }

    public void displayBoard(int[][] modifiedBorad) {
        int[][] board = modifiedBorad;
        int boardSize = modifiedBorad.length;
        for(int i=0; i<boardSize; i++) {
            for(int j=0; j<boardSize; j++) {
                if(board[i][j] == -1) {
                    System.out.print('*');
                } else {
                    System.out.print(String.valueOf(board[i][j]));
                }
            }
            System.out.print("\n");
        }
    }

    protected boolean isGameOver(int[][] state) {
        for(int i=0; i<state.length; i++) {
            for(int j=0; j<state.length; j++) {
                if(state[i][j] != -1) {
                    return false;
                }
            }
        }
        return true;
    }

    public LinkedList<List<String>> getAllPossibleClusters(int[][] currentBoard) {
        LinkedList<List<String>> clusters = new LinkedList<>();
        List<String> explored = new LinkedList<>();

        for(int i=0; i<currentBoard.length; i++) {
            for(int j=0; j<currentBoard.length; j++) {
                // skip '*' in the board
                if(currentBoard[i][j] == -1) continue;

                // if the node has already been visited, skip that
                String key = String.valueOf(i) + "," + String.valueOf(j);
                if(explored.contains(key)) continue;

                LinkedList<String> temp = new LinkedList<>();
                List<String> cluster = null;
                temp.add(key);
                while(!temp.isEmpty()) {
                    String str = temp.removeFirst();
                    if(cluster == null)
                        cluster = new LinkedList<>();
                    explored.add(str);
                    cluster.add(str);
                    LinkedList<String> connectedNodes = findAllConnectedNodes(str, explored, currentBoard);
                    if(connectedNodes != null) {
                        while(!connectedNodes.isEmpty()) {
                            String s = connectedNodes.remove();
                            if(!temp.contains(s))
                                temp.add(s);
                        }
                    }
                }
                if(cluster != null)
                    clusters.add(cluster);
            }
        }
        return clusters;
    }

    private LinkedList<String> findAllConnectedNodes(String str, List<String> explored, int[][] board) {
        String [] strArray = str.split(",");
        int row = Integer.parseInt(strArray[0]); // row
        int col = Integer.parseInt(strArray[1]); // column

        if(board[row][col] == -1) return null;

        int numberInBlock = board[row][col];
        String value=null;
        LinkedList<String> values = new LinkedList<>();
        int i=row, j=col;

        // examine the left block
        if((j-1) >= 0) {
            if(board[i][j-1] == numberInBlock) {
                value = String.valueOf(i)+","+String.valueOf(j-1);
                if(!explored.contains(value))
                    values.add(value);
            }
        }
        // examine the right block
        if((j+1) <= (board.length-1)) {
            if(board[i][j+1] == numberInBlock) {
                value = String.valueOf(i)+","+String.valueOf(j+1);
                if(!explored.contains(value))
                    values.add(value);
            }
        }
        // examine the top block
        if((i-1) >= 0) {
            if(board[i-1][j] == numberInBlock) {
                value = String.valueOf(i-1)+","+String.valueOf(j);
                if(!explored.contains(value))
                    values.add(value);
            }
        }
        // examine the bottom block
        if((i+1) <= (board.length-1)) {
            if(board[i+1][j] == numberInBlock) {
                value = String.valueOf(i+1)+","+String.valueOf(j);
                if(!explored.contains(value))
                    values.add(value);
            }
        }

        if (values.size() == 0)
            return null;
        else
            return values;
    }

    class sortLinkedList<T extends Comparator<T>> implements Comparator<List<T>> {

        @Override
        public int compare(List<T> o1, List<T> o2) {
            return -Integer.compare(o1.size(), o2.size());
        }
    }
}