/**
 * Represents the game board for a Pac-Man game. It includes functionalities such as reading and
 * initializing the game board from a file, checking the state of cells (walls, dots, etc.),
 * resetting the board to its original state, and calculating game elements like the total number of dots.
 * Utility methods are also provided to find the nearest dot to Pac-Man and set positions for dots on the board.
 *
 * @author Mille Brekke Amundsen
 */

package pac_man.gameBoards;

import javafx.geometry.Point2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static pac_man.PacMan_Pane.gameBoard;

public class GameBoard {
    public final static int EMPTY = 0;
    public final static int WALL = 1;
    public final static int DOT = 2;
    public final static int ENERGY_DOT = 3;
    public final static int BLINKY = 4;
    public final static int INKY = 5;
    public final static int PINKY = 6;
    public final static int CLYDE = 7;
    public final static int PACMAN = 8;
    public final static int NEXT_LEVEL = 9;
    private int[][] board;
    private int[][] originalBoardState;

    /**
     * Constructs a GameBoard object by reading and initializing the game board from the specified file path.
     *
     * @param filePath The path to the file containing the board layout.
     * @throws IOException If an error occurs during reading the file.
     */

    public GameBoard(String filePath) throws IOException {
        readBoard(filePath);
    }

    private void readBoard(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        int height = lines.size(); // int height = (int) (lines.size() * 0.8);
        int width = lines.stream().filter(line -> !line.trim().isEmpty()).findFirst().orElse("").replaceAll("\\s+", "").length();
        board = new int[height][width];

        for (int i = 0; i < height; i++) {
            String line = lines.get(i).replaceAll("\\s+", "");
            if (line.isEmpty()) continue;
            for (int j = 0; j < width && j < line.length(); j++) {
                try {
                    board[i][j] = Character.getNumericValue(line.charAt(j));
                    System.out.print(board[i][j]);
                } catch (Exception e) {
                    System.err.println(STR."False in line \{i}, col \{j}");
                }
            }
            System.out.println();
        }

        System.out.println(Arrays.toString(Arrays.stream(board).toArray()));
        originalBoardState = deepCopyBoard(board);
    }

    /**
     * Creates a deep copy of the provided 2D array representing the board state.
     *
     * @param source The 2D array to be copied.
     * @return A deep copy of the source array.
     */

    private int[][] deepCopyBoard(int[][] source) {
        int[][] copy = new int[source.length][source[0].length];
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, copy[i], 0, source[i].length);
        }
        return copy;
    }

    /**
     * Resets the board to its original state by restoring the values from the originalBoardState array.
     */

    public void resetBoard() {
        board = deepCopyBoard(originalBoardState);
    }

    /**
     * Returns the current state of the game board.
     *
     * @return A 2D array representing the current game board.
     */

    public int[][] getBoard() {
        return board;
    }

    public boolean isWall(int x, int y) {
        return board[y][x] == WALL;
    }

    public boolean isDot(int x, int y) {
        return board[y][x] == DOT;
    }

    public boolean isEnergyDot(int x, int y) {
        return board[y][x] == ENERGY_DOT;
    }

    public boolean isEmpty(int x, int y) {
        return board[y][x] == EMPTY;
    }

    public boolean isNextLevel(int x, int y) {
        return board[y][x] == NEXT_LEVEL;
    }

    /**
     * Generates a file path for the board configuration corresponding to the specified level.
     *
     * @param level The game level for which the board configuration is needed.
     * @return The file path of the board configuration.
     */

    public static String getBoard(int level) {
        String board = STR."src/main/java/pac_man/gameBoards/board\{level}.txt";
        return board;
    }

    /**
     * Finds the nearest dot to the given position on the board.
     *
     * @param currentPosition The current position of Pac-Man or a ghost.
     * @param dotsPositions A list of positions of dots on the board.
     * @return The nearest dot position to the given currentPosition.
     */


    public static Point2D findNearestDot(Point2D currentPosition, List<Point2D> dotsPositions) {
        Point2D nearestDot = null;
        double minDistance = Double.MAX_VALUE;

        for (Point2D dotPosition : dotsPositions) {
            double distance = currentPosition.distance(dotPosition);
            if (distance < minDistance) {
                minDistance = distance;
                nearestDot = dotPosition;
            }
        }

        return nearestDot;
    }

    /**
     * Calculates the total number of dots and energy dots present on the board.
     *
     * @param gameBoard The game board on which the calculation is to be performed.
     * @return The total number of dots and energy dots on the board.
     */

    public static int calculateTotalDots(GameBoard gameBoard) {
        int dots = 0;
        int[][] board = gameBoard.getBoard();
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == GameBoard.DOT || cell == GameBoard.ENERGY_DOT) {
                    dots++;
                }
            }
        }
        return dots;
    }

    /**
     * Finds and records the positions of all dots, energy dots, and empty cells on the game board.
     * This method iterates through each cell of the board, checking for cells that contain a dot,
     * an energy dot, or are empty. It then adds the positions of these cells to a list.
     * @return A List of Point2D objects representing the positions of dots, energy dots, and empty cells on the board.
     */

    public static List<Point2D> findAndSetDotsPositions() {
        List<Point2D> dotsPositions = new ArrayList<>();
        int[][] board = gameBoard.getBoard();
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                if (board[y][x] == GameBoard.DOT || board[y][x] == GameBoard.ENERGY_DOT || board[y][x] == GameBoard.EMPTY) {
                    dotsPositions.add(new Point2D(x, y));
                }
            }
        }
        return dotsPositions;
    }


}


