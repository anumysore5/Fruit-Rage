import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class calibrate {

    public static void main(String[] args) {
        int[][] randomBoard = new int[12][12];
        Random rand = new Random();
        int randomNumber;
        for (int i = 0; i < 12; i++) {
            for(int j=0; j<12; j++) {
                randomNumber = rand.nextInt(10);
                randomBoard[i][j] = randomNumber;
            }
        }

        Minimax minimax = new Minimax();
        double alpha = Double.NEGATIVE_INFINITY, beta=Double.POSITIVE_INFINITY;
        int [][] finalMove = new int[randomBoard.length][];
        minimax.Max_Value(randomBoard, alpha, beta, finalMove);
        System.out.println("noOfChildNodes---->" + minimax.noOfChildNodes);
        System.out.println("start startTimeInMillisec---->" + minimax.startTimeInMillisec);
        double rate = minimax.noOfChildNodes/240;
        System.out.println("Rate = " + rate);

        Path path = Paths.get("calibration.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(String.valueOf(rate));
            writer.write("\n");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Minimax {
    long startTimeInMillisec=0;
    int noOfChildNodes;

    public Minimax() {
        startTimeInMillisec = System.currentTimeMillis();
        noOfChildNodes = 0;
    }

    public double Max_Value(int[][] currentBoard, double alpha, double beta, int[][] finalMove) {
        if((System.currentTimeMillis()/1000 - startTimeInMillisec/1000) > 240) {
            return alpha;
        }

        // check if game over
        if (isGameOver(currentBoard)) {
            return 0;
        }

        LinkedList<List<String>> clusters = getAllPossibleClusters(currentBoard);
        int clusterNoToRemember = -1, clusterNo = 0;
        double res = Double.NEGATIVE_INFINITY;

        while (clusterNo < clusters.size()) {
            noOfChildNodes++;
            int[][] modifiedBorad = new int[currentBoard.length][currentBoard.length];
            List<String> cluster = clusters.get(clusterNo);
            clusterNo++;
            int score = cluster.size() * cluster.size();
            modifiedBorad = InsertStars(cluster, currentBoard);
            applyGravity(modifiedBorad);
            res = Math.max(res, Min_Value(modifiedBorad, alpha, beta, finalMove) + score);
            alpha = Math.max(alpha, res);
        }

        return alpha;
    }

    public double Min_Value(int[][] currentBoard, double alpha, double beta, int[][] finalMove) {
        if((System.currentTimeMillis()/1000 - startTimeInMillisec/1000) > 240) {
            return alpha;
        }

        if (isGameOver(currentBoard)) {
            return 0;
        }

        LinkedList<List<String>> clusters = getAllPossibleClusters(currentBoard);
        double res = Double.POSITIVE_INFINITY;

        while (!clusters.isEmpty()) {
            noOfChildNodes++;
            int[][] modifiedBorad = new int[currentBoard.length][currentBoard.length];
            List<String> cluster = clusters.removeFirst();
            int score = -(cluster.size() * cluster.size());
            modifiedBorad = InsertStars(cluster, currentBoard);
            applyGravity(modifiedBorad);
            res = Math.min(res, Max_Value(modifiedBorad, alpha, beta, finalMove) + score);
            beta = Math.min(beta, res);
        }

        return beta;
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

}