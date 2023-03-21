package uk.ac.soton.comp1206.ui;

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.*;

/**
 * The GameWindow is the single window for the game where everything takes place. To move between screens in the game,
 * we simply change the scene.
 *
 * The GameWindow has methods to launch each of the different parts of the game by switching scenes. You can add more
 * methods here to add more screens to the game.
 */
public class GameWindow {

    private static final Logger logger = LogManager.getLogger(GameWindow.class);

    private final int width;
    private final int height;

    private final Stage stage;

    private BaseScene currentScene;
    private Scene scene;

    final Communicator communicator;

    /**
     * Create a new GameWindow attached to the given stage with the specified width and height
     * @param stage stage
     * @param width width
     * @param height height
     */
    public GameWindow(Stage stage, int width, int height) {
        this.width = width;
        this.height = height;

        this.stage = stage;

        //Setup window
        setupStage();

        //Setup resources
        setupResources();

        //Setup default scene
        setupDefaultScene();

        //Setup communicator
        communicator = new Communicator("ws://ofb-labs.soton.ac.uk:9700");

        //Go to loading screen/start scene
        startStart();
    }

    /**
     * Setup the font and any other resources we need
     */
    private void setupResources() {
        logger.info("Loading resources");

        //We need to load fonts here due to the Font loader bug with spaces in URLs in the CSS files
        Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Regular.ttf"),32);
        Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Bold.ttf"),32);
        Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-ExtraBold.ttf"),32);
    }

    /**
     * Display the main menu
     */
    public void startMenu() {
        loadScene(new MenuScene(this));
    }

    /**
     * Display the single player challenge
     */
    public void startChallenge(boolean music, boolean sfx, int lives, boolean hard) {
        //Stops the options music
        Multimedia.stopLM();
        logger.info("Stopped playing menu music");
        loadScene(new ChallengeScene(this, music, sfx, lives, hard));
    }

    /**
     * Single player options
     */
    public void startOptions(){
        logger.info("Start the single player options");
        loadScene(new OptionsScene(this));
    }

    /**
     * The general settings
     */
    public void startSetting(){
        logger.info("Starting the settings");
        loadScene(new SettingsScene(this));
    }
    /**
     * Display the scores at the end of the game
     */
    public void startScores(Game game, boolean online, boolean easy){
        //Stops the menu music
        Multimedia.stopLM();
        logger.info("Stopped playing music");
        loadScene(new ScoresScene(this, game, online, easy));
    }

    /**
     * Starts with the intro screen
     */
    public void startStart(){
        logger.info("Welcome to TetrECS");
        loadScene(new StartScene(this));
    }

    /**
     * Start instructions scene
     */
    public void startInstr(){
        logger.info("Showing instructions");
        loadScene(new InstructionScene(this));
    }

    /**
     * Start a new lobby
     */
    public void startLobby(){
        logger.info("Starting lobby");
        loadScene(new LobbyScene(this));
    }

    /**
     * Setup the default settings for the stage itself (the window), such as the title and minimum width and height.
     */
    public void setupStage() {
        stage.setTitle("TetrECS");
        stage.setMinWidth(width);
        stage.setMinHeight(height + 20);
        stage.setOnCloseRequest(ev -> App.getInstance().shutdown());
    }

    /**
     * Load a given scene which extends BaseScene and switch over.
     * @param newScene new scene to load
     */
    private void loadScene(BaseScene newScene) {
        //Cleanup remains of the previous scene
        cleanup();

        //Create the new scene and set it up
        newScene.build();
        currentScene = newScene;
        scene = newScene.setScene();
        stage.setScene(scene);

        //Initialise the scene when ready
        Platform.runLater(() -> currentScene.initialise());
    }

    /**
     * Setup the default scene (an empty black scene) when no scene is loaded
     */
    public void setupDefaultScene() {
        this.scene = new Scene(new Pane(),width,height, Color.BLACK);
        stage.setScene(this.scene);
    }

    /**
     * When switching scenes, perform any cleanup needed, such as removing previous listeners
     */
    public void cleanup() {
        logger.info("Clearing up previous scene");
        communicator.clearListeners();
    }

    /**
     * Get the current scene being displayed
     * @return scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Get the width of the Game Window
     * @return width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Get the height of the Game Window
     * @return height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get the communicator
     * @return communicator
     */
    public Communicator getCommunicator() {
        return communicator;
    }

    /**
     * Starts the multiplayer game
     */
    public void startMultiPlayer(String username){
        loadScene(new MultiplayerScene(this, username));
    }
}
