/**
 * Manages the ghosts within the Pac-Man game, including their initialization, animation, movement, and interactions with other game elements.
 * This class handles the behavior of ghosts, such as moving them around the game board, changing their states (normal or scared),
 * resetting their positions, and checking for collisions with Pac-Man. It also manages the visual representation of ghosts using JavaFX components.
 * The static nature of the class members allows them to be accessed globally within the game's context.
 * Key functionalities include:
 * - Initializing ghosts at the start of the game based on predefined positions on the game board.
 * - Animating ghost movements and updating their positions on the game board.
 * - Changing the visual state of the ghosts (e.g., to a scared state when Pac-Man eats an energy dot).
 * - Resetting ghost positions after they have been eaten by Pac-Man or when a new level starts.
 * - Handling collisions between ghosts and Pac-Man, including updating the game state accordingly.
 * The class utilizes JavaFX's Timeline and ImageView to animate and display the ghosts, and makes extensive use of JavaFX's threading model (Platform.runLater)
 * to update the UI components in a thread-safe manner.
 *
 * @author Mille Brekke Amundsen
 */

package pac_man.ghosts;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import pac_man.gameBoards.GameBoard;

import java.util.*;

import static pac_man.PacMan_Pane.*;
import static pac_man.gameApp.Pac_Man_App.pacManPane;

public class Ghost {
    public static boolean pacmanGotEnergy = false;
    public static Timeline ghostAnimation;
    public static Map<String, Point2D> startPositions;
    public static Map<String, Label> ghostViews = new HashMap<>();
    public static Map<String, Boolean> ghostEatenStatus = new HashMap<>();
    private static Map<String, Long> lastMoveTime = new HashMap<>();
    private static Map<String, Point2D> ghostDirections = new HashMap<>();

    /**
     * Initializes ghosts on the game board using their starting positions and sets their images.
     * This method should be called at the start of the game or whenever the game is reset.
     */

    public static void initializeGhostsBasedOnBoard() {
        Map<String, Point2D> startPositions = findAndSetGhostsStartPosition();
        startPositions.forEach((ghostName, position) -> {
            Image image = new Image(Objects.requireNonNull(Ghost.class.getResource(STR."/images/\{ghostName}.png")).toExternalForm());
            ImageView ghostView = new ImageView(image);
            ghostView.setFitWidth(cell);
            ghostView.setFitHeight(cell);

            Label ghostLabel = new Label("", ghostView);
            ghostLabel.setLayoutX(position.getX() * cell);
            ghostLabel.setLayoutY(position.getY() * cell);

            ghostViews.put(ghostName, ghostLabel);

        });
        ghostViews.keySet().forEach(ghostName -> lastMoveTime.put(ghostName, System.currentTimeMillis()));
    }

    /**
     * Checks if a given cell type corresponds to a ghost.
     *
     * @param cellType The type of cell to check.
     * @return true if the cell type corresponds to a ghost, false otherwise.
     */

    public static boolean isGhost(int cellType) {
        return cellType == GameBoard.BLINKY || cellType == GameBoard.INKY || cellType == GameBoard.PINKY || cellType == GameBoard.CLYDE;
    }

    /**
     * Returns the name of a ghost based on its cell type.
     *
     * @param cellType The cell type of the ghost.
     * @return The name of the ghost corresponding to the cell type.
     */

    public static String getGhostNameFromCellType(int cellType) {
        return switch (cellType) {
            case GameBoard.BLINKY -> "blinky";
            case GameBoard.INKY -> "inky";
            case GameBoard.PINKY -> "pinky";
            case GameBoard.CLYDE -> "clyde";
            default -> throw new IllegalArgumentException(STR."Unknown: \{cellType}");
        };
    }

    /**
     * Sets all ghosts to a scared state, changing their images to indicate this.
     * This state change typically occurs when Pac-Man eats an energy dot.
     */

    public static void setGhostsScared() {
        Platform.runLater(() -> {
            ghostViews.values().forEach(ghostName -> {
                ImageView ghostView = (ImageView) ghostName.getGraphic();
                ghostView.setImage(new Image(Objects.requireNonNull(Ghost.class.getResource("/images/blueghost.gif")).toExternalForm()));
            });
        });

        Timeline scaredTimer = new Timeline(new KeyFrame(Duration.seconds(20), e -> resetGhostsNormal()));
        scaredTimer.play();
    }

    /**
     * Resets the visuals of all ghosts to their normal state after the scared state timer expires.
     */

    private static void resetGhostsNormal() {
        Platform.runLater(() -> {
            ghostViews.forEach((ghostName, ghostLabel) -> {
                ImageView ghostView = (ImageView) ghostLabel.getGraphic();
                ghostView.setImage(new Image(Objects.requireNonNull(Ghost.class.getResource(STR."/images/\{ghostName}.png")).toExternalForm()));
            });
        });
    }

    /**
     * Finds and sets the starting positions of all ghosts based on the game board layout.
     *
     * @return A map of ghost names to their starting positions.
     */

    public static Map<String, Point2D> findAndSetGhostsStartPosition() {
        startPositions = new HashMap<>();
        int[][] board = gameBoard.getBoard();
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                switch (board[y][x]) {
                    case GameBoard.BLINKY -> startPositions.put("blinky", new Point2D(x, y));
                    case GameBoard.INKY -> startPositions.put("inky", new Point2D(x, y));
                    case GameBoard.PINKY -> startPositions.put("pinky", new Point2D(x, y));
                    case GameBoard.CLYDE -> startPositions.put("clyde", new Point2D(x, y));
                }
            }
        }
        return startPositions;
    }

    /**
     * Updates the visual positions of the ghosts on the game board.
     * This method should be called regularly to animate the movement of the ghosts.
     */

    public static void updateGhostViews() {
        int[][] board = gameBoard.getBoard();
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                int cellType = board[y][x];
                if (isGhost(cellType)) {
                    String ghostName = getGhostNameFromCellType(cellType);
                    double posX = x * cell;
                    double posY = y * cell;

                    Label ghostLabel = ghostViews.get(ghostName);
                    if (ghostLabel != null) {
                        Platform.runLater(() -> {
                            ghostLabel.setLayoutX(posX);
                            ghostLabel.setLayoutY(posY);
                        });
                    } else {
                        ImageView newGhostView = new ImageView(new Image(Objects.requireNonNull(Ghost.class.getResource(STR."/images/\{ghostName}.png")).toExternalForm()));
                        newGhostView.setFitWidth(cell * 0.8);
                        newGhostView.setFitHeight(cell * 0.8);
                        Label newGhostLabel = new Label("", newGhostView);
                        newGhostLabel.setLayoutX(posX);
                        newGhostLabel.setLayoutY(posY);
                        Platform.runLater(() -> {
                            ghostViews.put(ghostName, newGhostLabel);
                        });
                    }
                }
            }
        }
    }

    /**
     * Resets the position of a specific ghost to its starting position.
     * This is typically called after the ghost has been eaten by Pac-Man.
     *
     * @param ghostName The name of the ghost to reset.
     */

    public static void resetGhost(String ghostName) {
        Point2D startPosition = startPositions.get(ghostName);
        if (startPosition != null && ghostViews.containsKey(ghostName)) {
            Label ghostView = ghostViews.get(ghostName);
            Platform.runLater(() -> {
                ghostView.setLayoutX(startPosition.getX() * cell);
                ghostView.setLayoutY(startPosition.getY() * cell);
                ghostView.setVisible(true);
                ghostEatenStatus.put(ghostName, false);
                pacmanGotEnergy = false;
            });
        }
    }

    /**
     * Starts the animation timeline for moving the ghosts.
     * This method should be called to begin the movement of ghosts around the game board.
     */

    public static void startGhostAnimation() {
        ghostAnimation = new Timeline(new KeyFrame(Duration.millis(1000), e -> moveGhosts()));
        ghostAnimation.setCycleCount(Timeline.INDEFINITE);
        ghostAnimation.play();
    }

    /**
     * Moves all ghosts based on their current directions. This method checks if the game is paused or if Pac-Man has
     * currently obtained an energy dot (making ghosts vulnerable). If neither condition is met, each ghost attempts to
     * move in its current direction. If a ghost encounters a wall, it chooses a new direction. If a ghost's new position
     * would coincide with Pac-Man's position, it triggers a life loss for Pac-Man and checks for game over conditions.
     */

    public static void moveGhosts() {
        if (isPaused) return;

        if (pacmanGotEnergy) {
            return;
        }

        ghostViews.keySet().forEach(ghostName -> {
            Point2D direction = ghostDirections.getOrDefault(ghostName, new Point2D(0, -1));
            Point2D currentPosition = startPositions.get(ghostName);
            if (currentPosition == null) return;

            double nextX = currentPosition.getX() + direction.getX();
            double nextY = currentPosition.getY() + direction.getY();

            if (isValidMove(nextX, nextY)) {
                Point2D newPosition = new Point2D(nextX, nextY);
                double pacmanX = position.getX();
                double pacmanY = position.getY();
                Point2D pacmanPosition = new Point2D(Math.round(pacmanX), Math.round(pacmanY));

                if (newPosition.equals(pacmanPosition)) {
                    lifes--;
                    pacManPane.updateLifesPane();
                    pacManPane.checkGameOver();
                }
                updateGhostPositionBasedOnGrid(ghostName, nextX, nextY);
            } else {
                chooseNewDirectionWhenBlocked(ghostName, currentPosition);
            }
        });
    }

    /**
     * Chooses a new direction for a ghost when its current path is blocked. This method generates a list of possible
     * new directions, shuffles them for randomness, and selects the first valid move that doesn't lead into a wall.
     *
     * @param ghostName       The name of the ghost that needs to change direction.
     * @param currentPosition The current position of the ghost before moving.
     */

    private static void chooseNewDirectionWhenBlocked(String ghostName, Point2D currentPosition) {
        Point2D currentDirection = ghostDirections.getOrDefault(ghostName, new Point2D(1, 0));
        List<Point2D> possibleDirections = new ArrayList<>(Arrays.asList(new Point2D(0, 1), new Point2D(0, -1), new Point2D(1, 0), new Point2D(-1, 0)));

        Collections.shuffle(possibleDirections);

        for (Point2D dir : possibleDirections) {
            double newX = currentPosition.getX() + dir.getX();
            double newY = currentPosition.getY() + dir.getY();

            if (isValidMove(newX, newY)) {
                ghostDirections.put(ghostName, dir);
                updateGhostPositionBasedOnGrid(ghostName, newX, newY);
                break;
            }
        }
    }

    /**
     * Checks if a proposed move to the next X and Y coordinates is valid (i.e., not moving into a wall).
     *
     * @param nextX The next X coordinate the ghost intends to move to.
     * @param nextY The next Y coordinate the ghost intends to move to.
     * @return true if the move is valid (not into a wall); false otherwise.
     */


    private static boolean isValidMove(double nextX, double nextY) {
        int gridX = (int) nextX;
        int gridY = (int) nextY;
        return gridX >= 0 && gridX < gameBoard.getBoard()[0].length && gridY >= 0 && gridY < gameBoard.getBoard().length && gameBoard.getBoard()[gridY][gridX] != GameBoard.WALL;
    }

    /**
     * Updates the position of a ghost on the grid based on the next X and Y coordinates. This method moves the ghost if the
     * next position is valid and updates the ghost's visual representation on the screen. If the new position coincides with
     * Pac-Man's position, it decreases Pac-Man's lives and checks for the game over condition.
     *
     * @param ghostName The name of the ghost whose position is to be updated.
     * @param nextX The next X coordinate the ghost will move to.
     * @param nextY The next Y coordinate the ghost will move to.
     */

    public static void updateGhostPositionBasedOnGrid(String ghostName, double nextX, double nextY) {
        int gridX = (int) nextX;
        int gridY = (int) nextY;

        if (isValidMove(gridX, gridY)) {
            Point2D newPosition = new Point2D(gridX, gridY);
            startPositions.put(ghostName, newPosition);

            Label ghostLabel = ghostViews.get(ghostName);
            if (ghostLabel != null) {
                Platform.runLater(() -> {
                    ghostLabel.setLayoutX(gridX * cell);
                    ghostLabel.setLayoutY(gridY * cell);
                });
            }

            Point2D pacManPosition = pacmanPOS;

            if (newPosition.equals(pacManPosition)) {
                lifes--;
                pacManPane.updateLifesPane();
                pacManPane.checkGameOver();
                System.out.println("BOOM!");
            }
        }
    }

}



