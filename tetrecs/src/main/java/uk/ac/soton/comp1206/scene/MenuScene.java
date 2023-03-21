package uk.ac.soton.comp1206.scene;

import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * The communicator to send/receive messages
   */
  private final Communicator communicator;


  /**
   * Create a new menu scene
   *
   * @param gameWindow the Game Window this will be displayed in
   */
  public MenuScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
    try {
      Multimedia.stopLM();
    } catch (Exception e){
      logger.error("Null player, proceed.");
    }
    Multimedia.playLoopedMusic("music/menu.mp3");
    communicator = gameWindow.getCommunicator();
  }

  /**
   * Build the menu layout
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);

    var mainPane = new BorderPane();
    mainPane.setPadding(new Insets(50, 10, 10, 10));
    menuPane.getChildren().add(mainPane);

    //Logo
    var logoImage = new Image(Multimedia.class.getResource("/images/TetrECS.png").toExternalForm());
    var logoView = new ImageView(logoImage);
    logoView.setPreserveRatio(true);
    logoView.setFitWidth(gameWindow.getWidth() * 0.75);

    //Animation for the logo
    RotateTransition rt = new RotateTransition(Duration.millis(3000), logoView);
    rt.setCycleCount(Timeline.INDEFINITE);
    rt.setFromAngle(-10);
    rt.setToAngle(10);
    rt.setAutoReverse(true);

    var logoBox = new HBox(logoView);
    logoBox.setAlignment(Pos.BASELINE_CENTER);
    logoBox.setPadding(new Insets(100, 10, 10, 10));
    mainPane.setTop(logoBox);
    rt.play();

    //Single player button
    var buttonSingle = new Button("Single Player");
    buttonSingle.getStyleClass().add("button");

    //Instructions button
    var buttonInst = new Button("Instructions");
    buttonInst.getStyleClass().add("button");

    //Settings button
    var buttonSet = new Button("Settings");
    buttonSet.getStyleClass().add("button");

    //Multiplayer button
    var buttonMulti = new Button("Multiplayer");
    buttonSingle.getStyleClass().add("button");

    //Exit button
    var exitButton = new Button("Exit");
    exitButton.getStyleClass().add("button");

    var menu = new VBox();
    menu.getChildren().addAll(buttonSingle, buttonInst, buttonMulti, buttonSet, exitButton);
    menu.setSpacing(10);
    menu.setPadding(new Insets(25));
    menu.setAlignment(Pos.BOTTOM_CENTER);
    mainPane.setCenter(menu);

    //Bind the button action to the startGame method in the menu
    buttonSingle.setOnAction(this::startGame);

    //Create an instructions scene
    buttonInst.setOnAction((event -> gameWindow.startInstr()));

    //Starts the settings
    buttonSet.setOnAction((event -> gameWindow.startSetting()));

    //Exit when button clicked
    exitButton.setOnAction((event -> {
      communicator.send("QUIT");
      Platform.exit();
      System.exit(0);
    }));

    //Create a new lobby for multiplayer
    buttonMulti.setOnAction((event -> gameWindow.startLobby()));
  }

  /**
   * Initialise the menu
   */
  @Override
  public void initialise() {

  }

  /**
   * Handle when the Start Game button is pressed
   *
   * @param event event
   */
  private void startGame(ActionEvent event) {
    gameWindow.startOptions();
  }

}
