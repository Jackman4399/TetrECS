package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.LeaderBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class MultiplayerScene extends ChallengeScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  private MultiplayerGame game;

  /**
   * The piece board that shows the current piece
   */
  private PieceBoard currentPieceBoard;

  /**
   * The piece board the shows the current piece
   */
  private PieceBoard followingPieceBoard;

  /**
   * To keep track of the current hover block's x coordinate
   */
  private int hoverX = 0;

  /**
   * To keep track of the current hover block's y coordinate
   */
  private int hoverY = 0;

  /**
   * The maximum X-boundary of the main gameBoard
   */
  private int boardXmax;

  /**
   * The maximum Y-boundary of the main gameBoard
   */
  private int boardYmax;

  /**
   * The main GameBoard
   */
  private GameBoard board;

  /**
   * To check if keyboard is enabled
   */
  private boolean keyboard;

  /**
   * The progress bar for the timer
   */
  private Rectangle barTime;

  /**
   * Pane with the progress bar
   */
  private BorderPane botPane;

  /**
   * The array list to hold all the scores
   */
  private ArrayList<Pair<String, Integer>> liveScores = new ArrayList<>();

  /**
   * The text field to send messages
   */
  private TextField txtfield;

  /**
   * The players name
   */
  private String username;

  /**
   * The communicator to send/receive messages
   */
  private final Communicator communicator;

  /**
   * Text to hold a chat message
   */
  private Text chat;

  /**
   * A VBox holding the live scores
   */
  private VBox scores;

  /**
   * A boolean to check for chatting
   */
  private boolean texting = false;

  /**
   * The leaderboard of live scores
   */
  private LeaderBoard scoresList;
  /**
   * Timer that loads scorers
   */
  private Timer loadTimer;

  /**
   * The score label
   */
  private Label scoreLbl;

  /**
   * The animation to run with the scores
   */
  private ScaleTransition st;

  /**
   * The multiplier label
   */
  private Label multiplier;

  /**
   * The parallel transition for multiplier
   */
  private ParallelTransition pt;

  /**
   * The scaling animation to for multiplier
   */
  private ScaleTransition st1;

  /**
   * Create a new Multiplayer challenge scene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow, String username) {
    super(gameWindow, true, true, 3, false);
    communicator = gameWindow.getCommunicator();
    this.username = username;
  }

  /**
   * Build the Challenge window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var obsevableScores = FXCollections.observableList(liveScores);
    var listProperty = new SimpleListProperty<>(obsevableScores);

    scoresList = new LeaderBoard();
    //Bind it to the changing scores observable list
    scoresList.getListProperty().bind(listProperty);

    //The main stackpane
    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("multiplayer-background");
    root.getChildren().add(challengePane);

    //A borderpane to hold all the UI
    var mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);
    mainPane.setPadding(new Insets(10));
    board = new GameBoard(game.getGrid(), gameWindow.getWidth() * 0.5, gameWindow.getWidth() * 0.5);

    boardXmax = board.getGrid().getCols() - 1;
    boardYmax = board.getGrid().getRows() - 1;
    hoverX = Math.round(board.getGrid().getCols() / 2);
    hoverY = Math.round(board.getGrid().getRows() / 2);
    //Boolean for keyboard function
    keyboard = false;

    mainPane.setCenter(board);
    //A pane to hold the bottom UI of the window
    botPane = new BorderPane();
    //New Rectangle
    barTime = new Rectangle();
    barTime.setArcHeight(10);
    barTime.setArcWidth(10);
    barTime.setHeight(25);
    barTime.setFill(Color.GREEN);
    botPane.setPadding(new Insets(5, 20, 5, 0));

    botPane.setBottom(barTime);
    //The rectangle will shrink towards the center-left side
    BorderPane.setAlignment(barTime, Pos.CENTER_LEFT);

    //New text field to send a message during game
    txtfield = new TextField();
    txtfield.setVisible(false);
    txtfield.setPromptText("Send a message");
    //Action events
    txtfield.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() != KeyCode.ENTER) {
        return;
      }
      if (txtfield.getText() != null) {
        communicator.send("MSG " + txtfield.getText());
        txtfield.clear();
        txtfield.setVisible(false);
      }
    });
    //Text node for text messages
    chat = new Text("In-Game Chat: Press T to send a chat message");
    chat.getStyleClass().add("player");
    var chatbox = new VBox(chat);
    chatbox.setAlignment(Pos.TOP_CENTER);
    //Add to border pane
    botPane.setTop(chatbox);
    botPane.setCenter(txtfield);
    //Set to the border pane
    mainPane.setBottom(botPane);
    BorderPane.setMargin(botPane, new Insets(6));

    //Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);

    //Handle when the main board is right clicked
    board.setOnRightClicked(this::rightClicked);
    //Handle mouse hover
    board.setOnMouseMoved((event) -> {
      if (board.getBlock(hoverX, hoverY).getValue() == 0 && keyboard) {
        board.getBlock(hoverX, hoverY).hover(false);
        board.getBlock(hoverX, hoverY).paint();
        keyboard = false;
      }
    });

    //Set up a listener for the game piece
    game.setNextPieceListener(this::changePiece);

    //Set up a listener for any cleared lines to animate them
    game.setLineClearedListener(this::lineCleared);

    //Set up listener for when the game loop is called
    game.setGameLoopListener(this::resetGameLoop);

    //Set up a listener for when the game ends (when lives are 0)
    game.setGameEndListener(this::endChallenge);

    //Making labels for each: Score, Lives, Level, and Multiplier
    //Binding each property to the labels as a String
    var scoreLbl = new Label();
    scoreLbl.getStyleClass().add("score");
    scoreLbl.textProperty().bind(game.scoreProperty().asString());
    var livesLbl = new Label();
    livesLbl.getStyleClass().add("lives");
    livesLbl.textProperty().bind(game.livesProperty().asString());

    multiplier = new Label();
    multiplier.textProperty().bind(game.multiplierProperty().asString());
    multiplier.getStyleClass().add("level");

    var ft = new FadeTransition(Duration.millis(1000), multiplier);
    ft.setFromValue(1.0);
    ft.setToValue(0.3);
    ft.setCycleCount(Timeline.INDEFINITE);
    ft.setAutoReverse(true);

    st1 = new ScaleTransition(Duration.millis(1500), multiplier);
    st1.setFromX(1);
    st1.setFromY(1);
    st1.setToX(1.3);
    st1.setToY(1.3);
    st1.setCycleCount(Timeline.INDEFINITE);
    st1.setAutoReverse(true);

    pt = new ParallelTransition(ft, st1);
    //Titles over the numbers
    var label1 = new Text(username);
    label1.getStyleClass().add("heading");
    var label2 = new Text("Lives");
    label2.getStyleClass().add("heading");
    var label3 = new Text("Multiplayer Game");
    label3.getStyleClass().add("title");
    var label4 = new Text("Versus");
    label4.getStyleClass().add("heading");
    var label5 = new Text("Incoming");
    label5.getStyleClass().add("heading");
    var label6 = new Label("Multiplier");
    label6.getStyleClass().add("level");

    scores = new VBox();

    //Adding them all to a secluded VBox
    var scoreBox = new VBox(label1, scoreLbl, label5);
    scoreBox.setSpacing(5);
    scoreBox.setAlignment(Pos.TOP_CENTER);
    scoreBox.setPadding(new Insets(0, 20, 20, 20));
    var liveBox = new VBox(label2, livesLbl);
    liveBox.setSpacing(5);
    liveBox.setAlignment(Pos.TOP_CENTER);
    //Holds the three elements on the top of the window
    var gPane = new BorderPane();
    gPane.setLeft(scoreBox);
    gPane.setCenter(label3);
    gPane.setRight(liveBox);

    mainPane.setTop(gPane);

    var vbox1 = new VBox(label6, multiplier);
    vbox1.setAlignment(Pos.TOP_CENTER);
    mainPane.setLeft(vbox1);

    var versusBox = new VBox(label4, scoresList);
    versusBox.setSpacing(5);
    versusBox.setAlignment(Pos.TOP_CENTER);

    //Setup a VBox to add elements on side of borderpane
    var vbox = new VBox(versusBox);
    vbox.setSpacing(10);
    vbox.setAlignment(Pos.CENTER);
    mainPane.setRight(vbox);

    //Create a new board to display current piece
    currentPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 6,
        gameWindow.getWidth() / 6, "Current");
    //Set listener for this board
    currentPieceBoard.setOnRightClicked(this::rightClicked);
    //Create a new board to display following piece
    followingPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 9,
        gameWindow.getWidth() / 9, "Following");
    //Add them to the vbox
    vbox.getChildren().addAll(label5, currentPieceBoard, followingPieceBoard);

  }

  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clocked
   */
  private void blockClicked(GameBlock gameBlock) {
    int temp = game.getScore();
    int mult = game.getMultiplier();
    game.blockClicked(gameBlock);
    //Everytime the score increases by 500
    if(game.getScore() >= (temp + 500)){
      st.play();
    }
    //Animation runs faster as the multiplier increases, resets otherwise
    if(game.getMultiplier() == (mult + 1)){
      st1.setDuration(st1.getDuration().multiply(0.65));
      logger.info("PLAYING ANIMATION");
      pt.play();
    } else {
      st1.setDuration(Duration.millis(1500));
      pt.stop();
    }
  }

  /**
   * Handle when a board (Main/Current) is clicked
   */
  private void rightClicked(GameBoard gb, boolean bool) {
    if (gb.getGrid().getCols() == 3 && bool == false) { //For current board
      game.rotateCurrentPiece(true);
    } else if (gb.getGrid().getCols() == 5 && bool == true) { //For main board
      game.rotateCurrentPiece(true);
    }
  }

  /**
   * Setup the game object and model
   */
  public void setupGame() {
    logger.info("Starting multiplayer");
    loadTimer = new Timer("Loader");
    var load = new TimerTask() {
      @Override
      public void run() {
        communicator.send("SCORES");
      }
    };
    //Schedule the timer
    loadTimer.schedule(load, 0, 1000);
    //Start new game
    game = new MultiplayerGame(5, 5, gameWindow);
  }

  /**
   * Initialise the scene and start the game
   */
  @Override
  public void initialise() {
    //Stop music from previous scene
    Multimedia.stopLM();
    Multimedia.stopM();
    logger.info("Initialising Challenge");
    game.start();
    scene.setOnKeyPressed((event -> {
      for (GameBlock[] blockRow : board.getBlocks()) {
        for (GameBlock blockTile : blockRow) {
          if (blockTile.isHover() && blockTile.getY() != hoverY && blockTile.getX() != hoverX) {
            blockTile.hover(false);
            blockTile.paint();
            logger.info("Block check clear");
          }
        }
      }
      keyboard = true;

      if (event.getCode() == KeyCode.ESCAPE) {
        //Go back to menu
        Multimedia.stopM();
        Multimedia.stopLM();
        loadTimer.cancel();
        loadTimer.purge();
        game.switchOff();
        //Tell server that player is done/left
        communicator.send("DIE");
        logger.info("Back to menu");
        gameWindow.startMenu();
      }
      if(!texting) {
        if (event.getCode() == KeyCode.E || event.getCode() == KeyCode.C
            || event.getCode() == KeyCode.CLOSE_BRACKET) {
          game.rotateCurrentPiece(true); //Rotates piece clockwise
        } else if (event.getCode() == KeyCode.Q || event.getCode() == KeyCode.Z
            || event.getCode() == KeyCode.OPEN_BRACKET) {
          game.rotateCurrentPiece(false); //Rotate anti-clockwise
        } else if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.R) {
          game.swapCurrentPiece(); //Swap pieces
        } else if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) {
          if (hoverY > 0) {
            if (board.getBlock(hoverX, hoverY).getValue() == 0) {
              //Clear previous block
              board.getBlock(hoverX, hoverY).hover(false);
              board.getBlock(hoverX, hoverY).paint();
            }
            hoverY--; //Shift up
            if (board.getBlock(hoverX, hoverY).getValue() == 0) {
              board.getBlock(hoverX, hoverY).hover(true);
              board.getBlock(hoverX, hoverY).paint();
            }
          }
        } else if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) {
          if (hoverY < boardYmax) {
            if (board.getBlock(hoverX, hoverY).getValue() == 0) {
              //Clear previous block
              board.getBlock(hoverX, hoverY).hover(false);
              board.getBlock(hoverX, hoverY).paint();
            }
            hoverY++; //Shift down
            if (board.getBlock(hoverX, hoverY).getValue() == 0) {
              board.getBlock(hoverX, hoverY).hover(true);
              board.getBlock(hoverX, hoverY).paint();
            }
          }
        } else if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) {
          if (hoverX > 0) {
            if (board.getBlock(hoverX, hoverY).getValue() == 0) {
              //Clear previous block
              board.getBlock(hoverX, hoverY).hover(false);
              board.getBlock(hoverX, hoverY).paint();
            }
            hoverX--; //Shift left
            if (board.getBlock(hoverX, hoverY).getValue() == 0) {
              board.getBlock(hoverX, hoverY).hover(true);
              board.getBlock(hoverX, hoverY).paint();
            }
          }
        } else if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
          if (hoverX < boardXmax) {
            if (board.getBlock(hoverX, hoverY).getValue() == 0) {
              //Clear previous block
              board.getBlock(hoverX, hoverY).hover(false);
              board.getBlock(hoverX, hoverY).paint();
            }
            hoverX++; //Shift right
            if (board.getBlock(hoverX, hoverY).getValue() == 0) {
              board.getBlock(hoverX, hoverY).hover(true);
              board.getBlock(hoverX, hoverY).paint();
            }
          }
        } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.X) {
          //Use blockClicked to treat event like a block was clicked
          game.blockClicked(board.getBlock(hoverX, hoverY));
        }
      }
      if (event.getCode() == KeyCode.T) {
        texting = true;
        //Set the text field as visible
        txtfield.setVisible(true);
      }
    }));
    communicator.addListener(e -> Platform.runLater(() -> receive(e.trim())));
  }

  /**
   * Receives the communicators messages
   *
   * @param message the message from the communicator
   */
  private void receive(String message) {
    if (message.startsWith("MSG")) {
      //Replace with new message
      chat.setText(message.split(" ", 2)[1].replace(":", ": "));
      Multimedia.playAudioEffect("sounds/message.wav");
      if(texting){
        texting = false;
      }
    } else if (message.startsWith("SCORES")) {
      //Player scores
      liveScores.clear();
      var scores = message.split(" ", 2)[1].split("\n");
      for (String info : scores) {
        //Format the string
        var newInfo = info.split(":");
        var player = newInfo[0].replace("<", "").replace(">", "");
        var score = newInfo[1].replace("<", "").replace(">", "");
        var lives = newInfo[2].replace("<", "").replace(">", "");
        if(lives.equals("DEAD")){
          player = player + "DEAD100";
        }
        liveScores.add(new Pair<>(player, Integer.parseInt(score)));
      }
      //Sort the array
      liveScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
      scoresList.createScores();
      scoresList.reveal();
    }
  }

  /**
   * Method to change the piece according to the integer
   *
   * @param gp the game piece
   * @param i  the action identifier
   */
  private void changePiece(GamePiece gp, int i) {
    if (i == 1) {
      logger.info("New piece created!");
      currentPieceBoard.setPiece(gp);
    } else if (i == 2) {
      followingPieceBoard.setPiece(gp);
    } else if (i == 3) {
      logger.info("Piece rotated!");
      currentPieceBoard.setPiece(gp);
    } else if (i == 4) {
      currentPieceBoard.setPiece(gp);
    } else if (i == 5) {
      followingPieceBoard.setPiece(gp);
    }
    if (i == 4 || i == 5) {
      logger.info("Pieces swapped!");
    }
  }

  /**
   * Pass the coordinates
   *
   * @param coordinates the cleared blocks
   */
  private void lineCleared(HashSet<GameBlockCoordinate> coordinates) {
    board.fadeOut(coordinates);
  }

  /**
   * Resets the game loops
   *
   * @param millis the time in milliseconds for a loop
   */
  private void resetGameLoop(int millis) {
    //keyframes used to animate the rectangle
    var timeProgress = new Timeline(new KeyFrame(
        Duration.ZERO, new KeyValue(barTime.fillProperty(), Color.GREEN)),
        new KeyFrame(Duration.ZERO,
            new KeyValue(barTime.widthProperty(), botPane.getWidth() * 0.9)),
        new KeyFrame(new Duration(millis * 0.5), new KeyValue(barTime.fillProperty(),
            Color.YELLOW)), new KeyFrame(new Duration(millis * 0.75),
        new KeyValue(barTime.fillProperty(), Color.RED)),
        new KeyFrame(new Duration(millis), new KeyValue(barTime.widthProperty(), 0)));
    timeProgress.play();

  }

  /**
   * Cleans up resources and stops the game
   *
   * @param game
   */
  public void endChallenge(Game game) {
    Multimedia.stopM();
    Multimedia.stopLM();
    loadTimer.cancel();
    loadTimer.purge();
    game.switchOff();
    communicator.send("DIE");
    gameWindow.startScores(game, true, false);
  }

}
