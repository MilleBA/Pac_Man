/**
 * The main application class for a Pac-Man game, extending the JavaFX Application class.
 * This class is responsible for initializing the game's UI, handling user input for starting and pausing the game,
 * and displaying game information such as the player's score, high score, life count, and current level.
 * It starts by displaying a login dialog where the user can enter their username, which then is used throughout the game.
 * The game board, score labels, life, and level indicators are arranged in a BorderPane layout.
 * Controls are setup to listen for key presses to navigate Pac-Man and control the game's state (play/pause).
 *
 * @author Mille Brekke Amundsen
 */

package pac_man.gameApp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import pac_man.PacMan_Pane;

import java.util.Objects;
import java.util.Optional;

public class Pac_Man_App extends Application {

    private static HBox lifesPane, levelPane;
    public static Label lblPoints, lblHighscore, lblText;
    public static int userPoints = 0;
    public static int highscore = 0;
    public static PacMan_Pane pacManPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        String userName = logInn();
        userName = userName.toUpperCase();

        pacManPane = new PacMan_Pane(userName);
        lifesPane = PacMan_Pane.lifesPane();
        levelPane = PacMan_Pane.levelPane();

        BorderPane borderPane = getRootPane();
        VBox vBox = setupUI();
        borderPane.setRight(vBox);

        Scene scene = new Scene(borderPane, 900, 600);
        setupSceneControls(scene);
        primaryStage.setTitle("Pac Man");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        Platform.runLater(() -> {
            if (pacManPane.getWidth() > 0 && pacManPane.getHeight() > 0) {
                pacManPane.drawBoard();
            } else {
                System.out.println("Wrong height or width");
            }
        });

    }

    /**
     * Displays a login dialog for the user to enter their username.
     *
     * @return The username entered by the user or "Unknown user" if none is entered.
     */

    private String logInn() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Log inn");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter your username:");

        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/pacfinish2.png")).toExternalForm()));
        imageView.setFitHeight(300);
        imageView.setFitWidth(300);
        dialog.setGraphic(imageView);

        Optional<String> result = dialog.showAndWait();
        return result.orElse("Unknown user");
    }

    /**
     * Constructs the root BorderPane for the application, including the logo and the game board.
     *
     * @return A fully constructed BorderPane with all necessary components for the game's UI.
     */

    private BorderPane getRootPane() {
        ImageView logo = new ImageView(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toExternalForm());
        logo.setFitHeight(130);
        logo.setPreserveRatio(true);
        HBox logoContainer = new HBox(logo);
        logoContainer.setPadding(new Insets(30, 5, 20, 5));
        logoContainer.setAlignment(Pos.CENTER);

        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: #423f3f");
        borderPane.setTop(logoContainer);
        borderPane.setCenter(pacManPane);
        borderPane.setFocusTraversable(true);
        borderPane.requestFocus();

        return borderPane;
    }

    /**
     * Sets up the UI elements on the right side of the BorderPane, including score labels and control instructions.
     *
     * @return A VBox containing the UI elements for displaying scores and controls.
     */

    private VBox setupUI() {

        lblPoints = new Label(STR."Points: \{pacManPane.getScore()}");
        lblPoints.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 20));
        lblPoints.setTextFill(Color.WHITE);

        lblHighscore = new Label(STR."HighScore: \{highscore}");
        lblHighscore.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 20));
        lblHighscore.setTextFill(Color.WHITE);

        lblText = new Label("Play: S\nPause: P");
        lblText.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 20));
        lblText.setTextFill(Color.RED);

        VBox vBox = new VBox(lblText, lblPoints, lblHighscore, lifesPane, levelPane);
        vBox.setMaxWidth(250);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));
        vBox.setAlignment(Pos.CENTER);

        return vBox;
    }

    /**
     * Configures the scene's key event handlers for game control, including movement controls and play/pause functionality.
     *
     * @param scene The main game scene to which the key event handlers are to be attached.
     */

    private void setupSceneControls(Scene scene) {
        scene.setOnKeyPressed(e -> {
            KeyCode key = e.getCode();
            pacManPane.updateDirection(key);
            if (e.getCode() == KeyCode.S) pacManPane.play();
            else if (e.getCode() == KeyCode.P) PacMan_Pane.pause();
        });
    }
}
