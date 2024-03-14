/**
 * The {@code PacMan_Pane} class represents the main game pane for the Pac-Man game. It extends the JavaFX {@link Pane} class
 * to create a custom drawing surface on which the game's elements like Pac-Man, ghosts, walls, and dots are rendered.
 * This class manages game logic including movement, collision detection, score tracking, and game state changes (start, pause, game over).
 * It initializes the game environment, handles user input for Pac-Man's movement, and updates the game's visual components accordingly.
 *
 * @author Mille Brekke Amundsen
 */

package pac_man;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import pac_man.gameBoards.GameBoard;
import pac_man.ghosts.Ghost;

import java.io.IOException;
import java.util.*;

import static pac_man.gameApp.Pac_Man_App.*;
import static pac_man.ghosts.Ghost.*;

public class PacMan_Pane extends Pane {
    public static Point2D position;
    private static double dx = 1;
    private static double dy = 1;
    private static Timeline animation;
    public static Label pacManLabel;
    public static GameBoard gameBoard;
    public static double cell;
    public static int lifes = 3;
    private static int level = 1;
    private static String username;
    private static HBox lifesPane, levelPane;
    private static boolean isInvulnerable = false;
    private static long lastHitTime = 0;
    public static int score = 0;
    public static boolean gameOver;
    public static boolean isPaused = false;
    public static int totalDots;
    private static final Map<Point2D, Circle> dotsMap = new HashMap<>();
    public static final List<Point2D> dotsMapGhosts = new ArrayList<>();
    public static Point2D pacmanPOS;


    /**
     * Constructor that initializes a new game pane with a specified username.
     * It loads the game board, starts the Pac-Man and ghosts animations, and sets up key event handling for game control.
     *
     * @param username The username of the player.
     * @throws IOException If there is an error loading the game board from a file.
     */

    public PacMan_Pane(String username) throws IOException {
        gameBoard = new GameBoard(GameBoard.getBoard(level));
        PacMan_Pane.username = username;
        setStyle("-fx-background-color: black");
        drawBoard();
        totalDots = GameBoard.calculateTotalDots(gameBoard);
        System.out.println(STR."Total: \{totalDots}");
        startAnimation();
        //'Ghost ghost = new Ghost();
        startGhostAnimation();
        this.setOnKeyPressed(event -> updateDirection(event.getCode()));
        this.setFocusTraversable(true);
    }

    /**
     * Initializes Pac-Man's visual representation and sets its starting position on the game board.
     */

    private void initializePacMan() {
        findAndSetPacManStartPosition();
        ImageView pacManImage = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/pacman.png")).toExternalForm()));
        pacManImage.setFitHeight(cell * 0.8);
        pacManImage.setFitWidth(cell * 0.8);

        pacManLabel = new Label("", pacManImage);
        pacManLabel.setLayoutX(position.getX());
        pacManLabel.setLayoutY(position.getY());
        getChildren().add(pacManLabel);
    }

    /**
     * Draws the game board, including static elements like walls, dots, and energy dots, and initializes the game's dynamic elements (Pac-Man and ghosts).
     */

    public void drawBoard() {
        getChildren().clear();
        drawStaticBoardElements();
        addGhostsToPane();
        initializePacMan();
    }

    /**
     * Draws the static elements of the game board, including walls, dots, and energy dots. It calculates
     * the positioning of these elements based on the game board's current state. This method also initializes
     * the starting positions of the ghosts if their corresponding cells are encountered on the board.
     * The method iterates through each cell of the game board array, drawing walls and dots as specified by
     * the game board layout. For each dot and energy dot, a Circle object is created and added to the pane,
     * and its position is recorded in the dotsMap for later reference. The size and position of each element
     * are calculated based on the cell size, which is determined by the pane's width and the number of columns
     * in the game board.
     */

    private void drawStaticBoardElements() {
        int[][] board = gameBoard.getBoard();
        int rows = board.length;
        int cols = (rows > 0) ? board[0].length : 0;

        cell = this.getWidth() / cols;

        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                double posX = x * cell;
                double posY = y * cell;

                switch (board[y][x]) {
                    case GameBoard.WALL -> drawWall(posX, posY, cell, cell);
                    case GameBoard.DOT -> {
                        Circle dot = drawDot(posX + cell / 2, posY + cell / 2, cell / 8);
                        dotsMap.put(new Point2D(x, y), dot);
                        totalDots++;
                    }
                    case GameBoard.ENERGY_DOT -> {
                        Circle dot = drawEnergyDot(posX + cell / 2, posY + cell / 2, cell / 4);
                        dotsMap.put(new Point2D(x, y), dot);
                        totalDots++;
                    }
                    case GameBoard.BLINKY, GameBoard.INKY, GameBoard.PINKY, GameBoard.CLYDE -> {
                        initializeGhostsBasedOnBoard();
                    }
                }
            }
        }
    }

    /**
     * Draws walls on the game board.
     *
     * @param x      The x-coordinate of the wall's position.
     * @param y      The y-coordinate of the wall's position.
     * @param width  The width of the wall.
     * @param height The height of the wall.
     */

    private void drawWall(double x, double y, double width, double height) {
        Rectangle wall = new Rectangle(x, y, width, height);
        wall.setFill(Color.DODGERBLUE);
        getChildren().add(wall);
    }


    /**
     * Draws a dot on the game board.
     *
     * @param centerX The center x-coordinate of the dot.
     * @param centerY The center y-coordinate of the dot.
     * @param radius  The radius of the dot.
     * @return The {@link Circle} representing the dot.
     */

    private Circle drawDot(double centerX, double centerY, double radius) {
        Circle dot = new Circle(centerX, centerY, radius, Color.WHITE);
        getChildren().add(dot);
        return dot;
    }

    /**
     * Draws an energy dot on the game board.
     *
     * @param centerX The center x-coordinate of the energy dot.
     * @param centerY The center y-coordinate of the energy dot.
     * @param radius  The radius of the energy dot.
     * @return The {@link Circle} representing the energy dot.
     */

    private Circle drawEnergyDot(double centerX, double centerY, double radius) {
        Circle energyDot = new Circle(centerX, centerY, radius, Color.ORANGE);
        getChildren().add(energyDot);
        return energyDot;
    }

    /**
     * Adds ghost labels to the game pane.
     */

    private void addGhostsToPane() {
        ghostViews.values().forEach(ghostView -> this.getChildren().add(ghostView));
    }

    /**
     * Finds and sets Pac-Man's start position based on the game board layout.
     */

    private static void findAndSetPacManStartPosition() {
        for (int y = 0; y < gameBoard.getBoard().length; y++) {
            for (int x = 0; x < gameBoard.getBoard()[y].length; x++) {
                if (gameBoard.getBoard()[y][x] == GameBoard.PACMAN) {
                    position = new Point2D(x * cell, y * cell);
                }
            }
        }
    }

    /**
     * Starts the main game animation which includes moving Pac-Man and handling game logic.
     */

    public void startAnimation() {
        animation = new Timeline(new KeyFrame(Duration.millis(40), e -> {
            try {
                move();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();
    }

    /**
     * Resumes the game and ghost animations, marking the game as not paused.
     */

    public void play() {
        if (animation != null) {
            animation.play();
            ghostAnimation.play();
            isPaused = false;
        }
    }

    /**
     * Pauses the game and ghost animations, marking the game as paused.
     */

    public static void pause() {
        if (animation != null) {
            animation.pause();
            ghostAnimation.pause();
            isPaused = true;
        }
    }

    /**
     * Increases the speed of the Pac-Man and ghost animations.
     */

    public void increaseSpeed() {
        animation.setRate(animation.getRate() + 0.5);
        ghostAnimation.setRate(ghostAnimation.getRate() + 0.5);
    }


    /**
     * Decreases the speed of the Pac-Man and ghost animations if the current speed is greater than zero.
     */

    public static void decreaseSpeed() {
        animation.setRate(animation.getRate() > 0 ? animation.getRate() - 0.5 : 0);
        ghostAnimation.setRate(ghostAnimation.getRate() > 0 ? ghostAnimation.getRate() - 0.5 : 0);
    }

    /**
     * Handles the logic for Pac-Man eating a ghost. It makes the ghost invisible, updates the score,
     * resets the life count, and schedules a timer to reset the ghost's visibility.
     *
     * @param ghostName The name of the ghost that was eaten.
     */

    private void eatGhost(String ghostName) {
        Boolean isEaten = ghostEatenStatus.getOrDefault(ghostName, false);
        if (!isEaten) {
            Label ghostLabel = ghostViews.get(ghostName);
            if (ghostLabel != null) {
                Platform.runLater(() -> ghostLabel.setVisible(false));
                ghostEatenStatus.put(ghostName, true);
                score += 200;
                lifes = 3;
                updateLifesPane();
                Platform.runLater(this::updateScoreDisplay);
                Timeline restartTimer = new Timeline(new KeyFrame(Duration.seconds(15), e -> resetGhost(ghostName)));
                restartTimer.play();
            }
        }
    }

    /**
     * Manages the action of Pac-Man eating a dot or an energy dot. Removes the dot from the game board,
     * updates the score, and decreases the total dot count.
     *
     * @param x The x-coordinate of the dot.
     * @param y The y-coordinate of the dot.
     */

    private void eatDot(int x, int y) {
        Point2D position = new Point2D(x, y);
        Circle dot = dotsMap.get(position);
        int cellType = gameBoard.getBoard()[y][x];
        if (dot != null) {
            if (cellType == GameBoard.DOT || cellType == GameBoard.ENERGY_DOT) {
                getChildren().remove(dot);
                dotsMap.remove(position);
                gameBoard.getBoard()[y][x] = GameBoard.EMPTY;
                score += (cellType == GameBoard.DOT) ? 10 : 50;
                totalDots--;
                System.out.println(totalDots);
                Platform.runLater(this::updateScoreDisplay);
            }
        }
    }

    /**
     * Updates the score display with the current score.
     */

    private void updateScoreDisplay() {
        userPoints = score;
        lblPoints.setText(STR."Points: \{getScore()}");
    }

    /**
     * Checks and updates the game level based on the current score. Resets the game board and increases
     * the game speed for the new level.
     *
     * @throws IOException If an error occurs while reloading the game board for the new level.
     */

    private void updateLevelDisplay() throws IOException {
        if (score > 90) {
            level = 2;
            getChildren().clear();
            gameBoard.resetBoard();
            updateLevelPane();
            increaseSpeed();
            startAnimation();
            gameBoard = new GameBoard(GameBoard.getBoard(level));
            drawBoard();
        }
    }

    /**
     * Returns the current score.
     *
     * @return The current score.
     */

    public int getScore() {
        return score;
    }

    /**
     * Decreases the life count of Pac-Man and checks for the game over condition.
     * Implements a cooldown period during which Pac-Man is invulnerable.
     */

    public void reduceLife() {
        long currentTime = System.currentTimeMillis();
        long invulnerableTime = 1500;
        if (!isInvulnerable || (currentTime - lastHitTime) > invulnerableTime) {
            lifes--;
            isInvulnerable = true;
            lastHitTime = currentTime;

            updateLifesPane();
            checkGameOver();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isInvulnerable = false;
                }
            }, invulnerableTime);
        }
    }

    /**
     * Constructs and returns a horizontal box displaying the current number of lives as icons.
     *
     * @return The HBox containing life icons.
     */

    public static HBox lifesPane() {
        lifesPane = new HBox(10);
        lifesPane.setAlignment(Pos.CENTER);
        lifesPane.setPadding(new Insets(5));
        for (int i = 0; i < lifes; i++) {
            ImageView lifeLogo = new ImageView(Objects.requireNonNull(PacMan_Pane.class.getResource("/images/paclife.png")).toExternalForm());
            lifeLogo.setFitHeight(30);
            lifeLogo.setFitWidth(30);
            lifesPane.getChildren().add(lifeLogo);
        }
        return lifesPane;
    }

    /**
     * Updates the life pane to reflect the current number of lives.
     */

    public void updateLifesPane() {
        lifesPane.getChildren().clear();
        for (int i = 0; i < lifes; i++) {
            ImageView lifeLogo = new ImageView(new Image(Objects.requireNonNull(PacMan_Pane.class.getResource("/images/paclife.png")).toExternalForm()));
            lifeLogo.setFitHeight(30);
            lifeLogo.setFitWidth(30);
            lifesPane.getChildren().add(lifeLogo);
            pacManPane.checkGameOver();
        }
    }

    /**
     * Checks if the game is over (when lives are equal to zero) and displays the game over dialog.
     */

    public void checkGameOver() {
        Platform.runLater(() -> {
            if (lifes <= 0) {
                gameOver = true;
                animation.pause();
                getDialogBox(username, "Game Over", "pacmanHorror", "you loose!");
            }
        });
    }

    /**
     * Constructs and returns a horizontal box displaying the current level as icons.
     *
     * @return The HBox containing level icons.
     */

    public static HBox levelPane() {
        levelPane = new HBox(10);
        levelPane.setAlignment(Pos.CENTER);
        levelPane.setPadding(new Insets(5));
        for (int i = 0; i < level; i++) {
            ImageView levelLogo = new ImageView(Objects.requireNonNull(PacMan_Pane.class.getResource("/images/watermellon.png")).toExternalForm());
            levelLogo.setFitHeight(30);
            levelLogo.setFitWidth(30);
            levelPane.getChildren().add(levelLogo);
        }
        return levelPane;
    }

    /**
     * Updates the level pane to reflect the current game level.
     */

    private static void updateLevelPane() {
        levelPane.getChildren().clear();
        for (int i = 0; i < level; i++) {
            ImageView levelLogo = new ImageView(new Image(Objects.requireNonNull(PacMan_Pane.class.getResource("/images/watermellon.png")).toExternalForm()));
            levelLogo.setFitHeight(30);
            levelLogo.setFitWidth(30);
            levelPane.getChildren().add(levelLogo);
        }
    }

    /**
     * Displays a dialog box with a custom message. The dialog can trigger a game restart or exit based on user interaction.
     *
     * @param username  The username of the player.
     * @param title     The title of the dialog.
     * @param imageName The name of the image to display in the dialog.
     * @param message   The message to display in the dialog.
     */

    public void getDialogBox(String username, String title, String imageName, String message) {
        Platform.runLater(() -> {
            pause();
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle(title);

            VBox vbox = new VBox();
            vbox.setAlignment(Pos.CENTER);

            Label gameOverLabel = new Label(STR."\{username}, \{message}!");
            gameOverLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            gameOverLabel.setTextFill(Color.RED);

            try {
                ImageView imageView = new ImageView(new Image(Objects.requireNonNull(PacMan_Pane.class.getResource(STR."/images/\{imageName}.png")).toExternalForm()));
                imageView.setFitHeight(300);
                imageView.setFitWidth(300);
                vbox.getChildren().addAll(gameOverLabel, imageView);
            } catch (NullPointerException e) {
                System.err.println(e.getMessage());
            }

            dialog.getDialogPane().setContent(vbox);

            ButtonType restartButtonType = new ButtonType("Restart", ButtonBar.ButtonData.OK_DONE);
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(restartButtonType, closeButton);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == restartButtonType) restartGame();
                else if (dialogButton == closeButton) {
                    Platform.exit();
                }
                return null;
            });

            dialog.showAndWait();
        });
    }

    /**
     * Restarts the game, resetting the score, lives, level, and speed. Also checks and updates the high score.
     */

    public void restartGame() {
        if (score > highscore) {
            highscore = score;
        }

        score = 0;
        lifes = 3;
        level = 1;
        gameOver = false;

        Platform.runLater(() -> {
            lblHighscore.setText(STR."HighScore: \{highscore}");
            lblPoints.setText(STR."Points: \{score}");

            updateLifesPane();
            updateLevelPane();
            decreaseSpeed();

            try {
                gameBoard = new GameBoard(GameBoard.getBoard(level));
                drawBoard();
            } catch (IOException e) {
                e.printStackTrace();
            }
            startAnimation();
            Ghost.startGhostAnimation();
        });
    }

    /**
     * Moves Pac-Man based on the current direction and handles interactions with dots, energy dots, and ghosts.
     * Checks for wall collisions and level completion conditions.
     *
     * @throws IOException If an error occurs during movement.
     */


    public void move() throws IOException {
        if (isPaused) {
            return;
        }
        Point2D nextPosition = position.add(dx, dy);
        int nextX = (int) (nextPosition.getX() / cell);
        int nextY = (int) (nextPosition.getY() / cell);

        if (nextX < 0 || nextY < 0 || nextX >= gameBoard.getBoard()[0].length || nextY >= gameBoard.getBoard().length) {
            return;
        }

        int cellType = gameBoard.getBoard()[nextY][nextX];
        if (cellType != GameBoard.WALL) {
            position = nextPosition;
        } else {
            reduceLife();
        }

        if (cellType == GameBoard.DOT) {
            eatDot(nextX, nextY);
        }

        if (cellType == GameBoard.ENERGY_DOT) {
            eatDot(nextX, nextY);
            pacmanGotEnergy = true;
            setGhostsScared();
            updateGhostViews();
        }


        if (isGhost(cellType) && pacmanGotEnergy) {
            eatGhost(getGhostNameFromCellType(cellType));
        }

        if (cellType == GameBoard.NEXT_LEVEL && totalDots / 2 == 0) {
            updateLevelDisplay();
        }

        if (cellType == GameBoard.NEXT_LEVEL && score > 1270 && level == 2) {
            getDialogBox(username, "Winner", "pacfinish", "you are a winner");
        }

        pacManLabel.setLayoutX(position.getX());
        pacManLabel.setLayoutY(position.getY());
        pacmanPOS = new Point2D(nextX, nextY);
    }

    /**
     * Sets the direction of Pac-Man's movement.
     *
     * @param dx The change in the x-coordinate.
     * @param dy The change in the y-coordinate.
     */

    public void setDirection(double dx, double dy) {
        PacMan_Pane.dx = dx;
        PacMan_Pane.dy = dy;
    }

    /**
     * Updates Pac-Man's direction based on keyboard input. Adjusts Pac-Man's velocity in the specified direction
     * and rotates the Pac-Man label to face the current direction of movement.
     *
     * @param key The KeyCode corresponding to the arrow key pressed by the user to determine Pac-Man's direction.
     */

    public void updateDirection(KeyCode key) {
        final double speed = 2.0;

        switch (key) {
            case DOWN -> {
                setDirection(0, speed);
                pacManLabel.setRotate(90);
            }
            case UP -> {
                setDirection(0, -speed);
                pacManLabel.setRotate(270);
            }
            case LEFT -> {
                setDirection(-speed, 0);
                pacManLabel.setRotate(180);
            }
            case RIGHT -> {
                setDirection(speed, 0);
                pacManLabel.setRotate(0);
            }
        }
    }

}



