package uk.ac.soton.comp1206.scene;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class ScoresScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(ScoresScene.class);


  /**
   * The game state
   */
  private Game gameState;

  /**
   * Holds the current list of scores in the Scene
   */
  private static SimpleListProperty<Pair<String, Integer>> localScores;

  /**
   * Online scores
   */
  private static SimpleListProperty<Pair<String, Integer>> remoteScores;

  /**
   * The scores list
   */
  private ArrayList<Pair<String, Integer>> scoresList = new ArrayList();

  /**
   * Observable list for binding
   */
  private static ObservableList<Pair<String, Integer>> observableScores;

  /**
   * The default list for the writer in case no file exists
   */
  private static final String[] defaultList = {
      "Jack:10000", "Jack:9000", "Jack:8000", "Jack:7000", "Jack:6000", "Jack:5000",
      "Jack:4000", "Jack:3000", "Jack:2000", "Jack:1000"
  };

  /**
   * Instance of the high score
   */
  private static int highestScore;

  /**
   * An arraylist of the remote scores
   */
  private ArrayList<Pair<String, Integer>> remoteScoreList = new ArrayList<>();

  /**
   * An observable list for the remote scores
   */
  private ObservableList<Pair<String, Integer>> observableOnline;

  /**
   * Communicator instance variable
   */
  private final Communicator communicator;

  /**
   * A UI score list component for the remote scores
   */
  private static ScoresList online;

  /**
   * The username of the player
   */
  private final SimpleStringProperty playerName = new SimpleStringProperty("");

  /**
   * A boolean to check the readiness of te UI to display
   */
  private SimpleBooleanProperty ready = new SimpleBooleanProperty(false);

  /**
   * To see if the game is online or not
   */
  private boolean onlineGame;

  /**
   * Easy game boolean to not submit online scores
   */
  private final boolean easy;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public ScoresScene(GameWindow gameWindow, Game game, boolean online, boolean easy) {
    super(gameWindow);
    gameState = game;
    communicator = gameWindow.getCommunicator();
    onlineGame = online;
    //Get a different local score list if it's an online game
    if (onlineGame) {
      scoresList = game.getScoresList();
    }
    this.easy = easy;
  }

  /**
   * Create new scores scene
   */
  @Override
  public void initialise() {
    Multimedia.stopLM();
    Multimedia.playMusic("music/end.wav");
    scene.setOnKeyPressed((event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        //Go back to menu
        Multimedia.stopM();
        if(onlineGame){
          //Disconnect from the server if it's an online game
          communicator.send("QUIT");
        }
        logger.info("Back to menu");
        gameWindow.startMenu();
      }
    }));
  }

  /**
   * Build the layout and UI elements
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    //Communicator requests online scores
    communicator.addListener(s -> Platform.runLater(() -> loadOnlineScores(s.trim())));
    communicator.send("HISCORES");
    //Set the observable lists
    observableScores = FXCollections.observableList(scoresList);
    observableOnline = FXCollections.observableList(remoteScoreList);

    //Wrappers
    remoteScores = new SimpleListProperty<>(observableOnline);
    localScores = new SimpleListProperty<>(observableScores);

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //Main stack pane
    var scorePane = new StackPane();
    scorePane.setMaxWidth(gameWindow.getWidth());
    scorePane.setMaxHeight(gameWindow.getHeight());
    scorePane.getStyleClass().add("scores-background");
    root.getChildren().add(scorePane);

    //Border Pane on the stack pane
    var mainScorePane = new BorderPane();
    scorePane.getChildren().add(mainScorePane);

    //Main vbox
    var vbox = new VBox();
    vbox.setAlignment(Pos.TOP_CENTER);
    vbox.setPadding(new Insets(5));
    vbox.setSpacing(20);
    mainScorePane.setCenter(vbox);

    //Logo
    var logoImage = new Image(Multimedia.class.getResource("/images/TetrECS.png").toExternalForm());
    var logoView = new ImageView(logoImage);
    logoView.setPreserveRatio(true);
    logoView.setFitWidth(gameWindow.getWidth() * 0.75);
    vbox.getChildren().add(logoView);

    //1st Title (Game over)
    var title = new Text("Game Over");
    title.setTextAlignment(TextAlignment.CENTER);
    title.getStyleClass().add("bigtitle");
    VBox.setVgrow(title, Priority.ALWAYS);
    vbox.getChildren().add(title);

    //2nd Title (High Scores)
    var title2 = new Text("High Scores");
    title2.setOpacity(0);
    title2.setTextAlignment(TextAlignment.CENTER);
    title2.getStyleClass().add("title");
    VBox.setVgrow(title2, Priority.ALWAYS);
    vbox.getChildren().add(title2);

    //To have the two score boards and headers show side by side
    var scoreboard = new GridPane();
    scoreboard.setOpacity(0);
    scoreboard.setAlignment(Pos.CENTER);
    scoreboard.setHgap(80);
    vbox.getChildren().add(scoreboard);

    //For local scores title
    var localTitle = new Text("Local Scores");
    if (onlineGame) {
      localTitle.setText("This Game");
    }
    localTitle.setTextAlignment(TextAlignment.CENTER);
    localTitle.getStyleClass().add("heading");
    GridPane.setHalignment(localTitle, HPos.CENTER);

    //For online scores title
    var onlineTitle = new Text("Online Scores");
    onlineTitle.setTextAlignment(TextAlignment.CENTER);
    onlineTitle.getStyleClass().add("heading");
    GridPane.setHalignment(onlineTitle, HPos.CENTER);

    //The score list for the local scores
    var local = new ScoresList();
    local.getListProperty().bind(localScores);
    local.setAlignment(Pos.TOP_CENTER);

    //A change listener so local and online UI display at the same time
    ready.addListener(((observable, oldValue, newValue) -> local.reveal()));

    //Adds nodes to the grid
    scoreboard.add(localTitle, 0, 0);
    scoreboard.add(onlineTitle, 1, 0);
    scoreboard.add(local, 0, 1);

    //Loads the local scores
    if (!onlineGame) {
      loadScores("src/main/resources/localScores.txt");
    }

    //The score list for the remote scores
    online = new ScoresList();
    online.getListProperty().bind(remoteScores);
    online.setAlignment(Pos.TOP_CENTER);

    //Adding to grid
    scoreboard.add(online, 1, 1);

    //A boolean to state if a new high score has been achieved or not
    boolean highscore = true;
    if (scoresList.size() < 10) {
      if (gameState.getScore() < scoresList.get(scoresList.size() - 1)
          .getValue()) {
        highscore = false;
      }
    } else {
      if (gameState.getScore() < scoresList.get(9).getValue()) {
        highscore = false;
      }
    }
    //if a high score has been achieved
    if (highscore && !onlineGame && !easy) {
      logger.info("New high score: {}", gameState.getScore());
      //Shows a text field for user to enter name
      var prompt = new TextField();
      prompt.setPromptText("Enter name");
      prompt.requestFocus();
      prompt.setMaxWidth(gameWindow.getWidth() / 4);
      //Add it to a specific position
      vbox.getChildren().add(2, prompt);
      //Button to submit name
      var submit = new Button("Submit");
      submit.setDefaultButton(true);
      vbox.getChildren().add(3, submit);
      submit.setOnAction(e -> {
        var username = prompt.getText();
        playerName.set(username);
        vbox.getChildren().remove(2);
        vbox.getChildren().remove(2);
        Multimedia.playAudioEffect("sounds/pling.wav");
        //In case the name is empty
        if (username.equals("")) {
          scoresList.add(new Pair("Unknown", gameState.getScore()));
        } else {
          scoresList.add(new Pair(username, gameState.getScore()));
        }
        //Sorts score list
        scoresList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        writeScores("src/main/resources/localScores.txt");

        //Reveal UI component
        scoreboard.setOpacity(1);
        title2.setOpacity(1);
        local.createScores();
        writeOnlineScore();
      });
    } else {
      //Reveal UI component without prompt for username
      scoreboard.setOpacity(1);
      title2.setOpacity(1);
      local.createScores();
      writeOnlineScore();
    }
  }


  /**
   * Loads the scores from a text file
   *
   * @param file the file path
   */
  private void loadScores(String file) {
    var leaderboard = new ArrayList<String>();
    try {
      logger.info("Loading scores from {}", file);
      File file1 = new File(file);
      Scanner reader = new Scanner(file1);
      while (reader.hasNextLine()) {
        String data = reader.nextLine();
        //Splits data by '='
        var name = data.split("=")[0];
        var score = data.split("=")[1];
        leaderboard.add(name + "=" + score);
      }
      reader.close();

      for (String input : leaderboard) {
        var pair = input.split("=");
        scoresList.add(new Pair<>(pair[0], Integer.parseInt(pair[1])));
      }
      scoresList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    } catch (Exception e) {
      logger.error("Unable to read file");
      e.printStackTrace();
    }
  }

  /**
   * Writes the scores into the text file
   *
   * @param file the path to the file
   */
  private void writeScores(String file) {
    var data = new File(file);
    //Only if the file exists
    if (data.exists()) {
      try {
        var writer = new FileWriter(data);
        for (Pair tag : scoresList) {
          writer.write(tag.toString() + "\n");
        }
        writer.close();
      } catch (IOException e) {
        logger.error("Issue has occurred.");
        e.printStackTrace();
      }
    } else {
      //Make a new file with default values if no new data exists
      try {
        data.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        var writer = new FileWriter(data);
        for (var i = 0; i < defaultList.length; i++) {
          writer.write(defaultList[i] + "\n");
        }
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Gets the simple integer property of the high score
   *
   * @return SimpleIntegerProperty
   */
  public int getHighestScore() {
    loadScores("src/main/resources/localScores.txt");
    if (scoresList != null) {
      highestScore = scoresList.get(0).getValue();
      return highestScore;
    } else {
      return -1;
    }
  }

  /**
   * Loads remote high scores
   *
   * @param message
   */
  private void loadOnlineScores(String message) {

    if (message.startsWith("NEWSCORE")) {
      logger.info("New Score received");
      var newscore = message.split(" ")[1].split(":");
      //Adds new score to the online list
      remoteScoreList.add(new Pair<>(newscore[0], Integer.parseInt(newscore[1])));
      ready.set(true);
    } else if (message.startsWith("HISCORES")){
      logger.info("Data received " + message);
      var data = message.split("\n");
      data[0] = data[0].split(" ")[1];
      for (int i = 0; i < data.length; i++) {
        var str = data[i].trim().split(":");
        //Replace empty names with Unknown
        if (str[0].equals("")) {
          str[0] = "Unknown";
        }
        logger.info("Check this out " + remoteScoreList.toArray().toString());
        remoteScoreList.add(new Pair<>(str[0].trim(), Integer.parseInt(str[1])));
      }
    }
    remoteScoreList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    //Calls method to set ready as TRUE if no new high score has been achieved
    checkOnlineHigh();
    online.createScores();
    //Prevents displaying twice
    if (ready.get()) {
      online.reveal();
    }
  }

  /**
   * write an online score if a high score has been achieved
   */
  private void writeOnlineScore() {
    //For easy games
    if(easy){
      logger.info("Easy game detected, cannot write score");
      return;
    }
    //To make sure the list isn't empty, so it can be accessed without throwing a null pointer exception
    if (remoteScoreList.size() != 0) {
      //To make sure that it gets the 10th or last element of the list if the size is smaller than 10
      int limit = 9;
      if (remoteScoreList.size() < 10) {
        limit = remoteScoreList.size() - 1;
      }
      if (gameState.getScore() > remoteScoreList.get(limit).getValue()) {
        //Sends a new high score
        communicator.send("HISCORE " + playerName.get() + ":" + gameState.getScore());
        logger.info("Sent {}:{}", playerName.get(), gameState.getScore());
      }
    }
  }

  /**
   * Checks the high score
   */
  private void checkOnlineHigh() {
    logger.info("Checking online high");
    //Don't check if it's an online game
    if(onlineGame){
      ready.set(true);
      return;
    }
    //For easy games
    if(easy){
      logger.info("Easy game detected");
      ready.set(true);
      return;
    }
    //Check for 10th high score on the leaderboard
    if (!(gameState.getScore() > remoteScoreList.get(9).getValue())) {
      ready.set(true);
    }
  }

}
