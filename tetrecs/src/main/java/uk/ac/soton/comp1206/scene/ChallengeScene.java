package uk.ac.soton.comp1206.scene;

import java.util.HashSet;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the
 * game.
 */
public class ChallengeScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * The game
   */
  private Game game;

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
  private StackPane botStack;

  /**
   * The high score
   */
  private SimpleIntegerProperty highestScore = new SimpleIntegerProperty();

  /**
   * Music on status
   */
  private boolean musicOn;

  /**
   * SFX on status
   */
  private boolean sfxOn;

  /**
   * Number of lives
   */
  private int lives;

  /**
   * Easy mode to not compete online scores
   */
  private boolean easy = false;

  /**
   * Hard mode for settings
   */
  private boolean hard = false;

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
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public ChallengeScene(GameWindow gameWindow, boolean music, boolean sfx, int lives, boolean hard) {
    super(gameWindow);
    logger.info("Creating Challenge Scene");
    musicOn = music;
    sfxOn = sfx;
    this.lives = lives;
    if(lives == 4){
      easy = true;
    }
    this.hard = hard;
  }

  /**
   * Build the Challenge window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();
    getHighScore();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("challenge-background");
    root.getChildren().add(challengePane);

    var mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);
    mainPane.setPadding(new Insets(20));
    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);

    //Coordinates for hovering
    boardXmax = board.getGrid().getCols() - 1;
    boardYmax = board.getGrid().getRows() - 1;
    hoverX = Math.round(board.getGrid().getCols()/2);
    hoverY = Math.round(board.getGrid().getRows()/2);
    keyboard = false;


    mainPane.setCenter(board);

    botStack = new StackPane();
    //New Rectangle
    barTime = new Rectangle();
    barTime.setArcHeight(10);
    barTime.setArcWidth(10);
    barTime.setHeight(25);
    barTime.setFill(Color.GREEN);
    botStack.setAlignment(Pos.BOTTOM_CENTER);
    botStack.setPadding(new Insets(5,20,5,0));

    botStack.getChildren().add(barTime);
    //The rectangle will shrink towards the center-left side
    StackPane.setAlignment(barTime, Pos.CENTER_LEFT);

    mainPane.setBottom(botStack);
    BorderPane.setMargin(botStack, new Insets(6));


    //Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);

    //Handle when the main board is right clicked
    board.setOnRightClicked(this::rightClicked);

    board.setOnMouseMoved((event) -> {
      if(board.getBlock(hoverX, hoverY).getValue() == 0 && keyboard){
        board.getBlock(hoverX, hoverY).hover(false);
        board.getBlock(hoverX, hoverY).paint();
        keyboard = false;
      }
    });

    //Set up a listener for the game piece
    game.setNextPieceListener(this :: changePiece);

    //Set up a listener for any cleared lines to animate them
    game.setLineClearedListener(this :: lineCleared);

    //Set up listener for when the game loop is called
    game.setGameLoopListener(this :: resetGameLoop);

    //Set up a listener for when the game ends (when lives are 0)
    game.setGameEndListener(this :: endChallenge);

    //Making labels for each: Score, Lives, Level, and Multiplier
    //Binding each property to the labels as a String
    scoreLbl = new Label();
    scoreLbl.getStyleClass().add("score");
    scoreLbl.textProperty().bind(game.scoreProperty().asString());
    //Animation for score increase
    st = new ScaleTransition(Duration.millis(500), scoreLbl);
    st.setFromX(0);
    st.setFromY(0);
    st.setToX(1);
    st.setToY(1);
    st.setCycleCount(1);
    st.setAutoReverse(true);

    var livesLbl = new Label();
    livesLbl.getStyleClass().add("lives");
    livesLbl.textProperty().bind(game.livesProperty().asString());
    var levelLbl = new Label();
    levelLbl.getStyleClass().add("level");
    levelLbl.textProperty().bind(game.levelProperty().asString());
    var highScoreLbl = new Label();
    highScoreLbl.textProperty().bind(highestScore.asString());
    highScoreLbl.getStyleClass().add("hiscore");
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
    var label1 = new Label("Score");
    label1.getStyleClass().add("score");
    var label2 = new Label("Lives");
    label2.getStyleClass().add("lives");
    var label3 = new Label("Level");
    label3.getStyleClass().add("level");
    var label4 = new Label("Current High Score");
    label4.getStyleClass().add("hiscore");
    var label5 = new Label("Multiplier");
    label5.setPadding(new Insets(10, 0,0,0));
    label5.getStyleClass().add("level");
    //Adding them all to a secluded VBox
    var scoreBox = new VBox(label1, scoreLbl, label5, multiplier);
    scoreBox.setSpacing(5);
    scoreBox.setAlignment(Pos.TOP_CENTER);
    scoreBox.setPadding(new Insets(0,20,20,20));
    var liveBox = new VBox(label2, livesLbl);
    liveBox.setSpacing(5);
    liveBox.setAlignment(Pos.TOP_CENTER);
    var levelBox = new VBox(label3, levelLbl);
    levelBox.setSpacing(5);
    levelBox.setAlignment(Pos.TOP_CENTER);
    var highscoreBox = new VBox(label4, highScoreLbl);
    highscoreBox.setSpacing(5);
    highscoreBox.setAlignment(Pos.TOP_CENTER);

    //Setup a VBox to add elements on side of borderpane
    var vbox = new VBox(liveBox, levelBox, highscoreBox);
    vbox.setSpacing(10);
    vbox.setAlignment(Pos.CENTER);
    mainPane.setLeft(scoreBox);
    mainPane.setRight(vbox);

    //Create a new board to display current piece
    currentPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 5,
        gameWindow.getWidth() / 5, "Current");
    //Set listener for this board
    currentPieceBoard.setOnRightClicked(this::rightClicked);
    //Create a new board to display following piece
    followingPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 8,
        gameWindow.getWidth() / 8, "Following");
    //Add them to the vbox
    vbox.getChildren().addAll(currentPieceBoard, followingPieceBoard);

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
    if(game.getScore() > highestScore.get()){
      highestScore.set(game.getScore());
    }
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
    } else if (gb.getGrid().getCols() == 5 && bool == true){ //For main board
      game.rotateCurrentPiece(true);
    }
  }

  /**
   * Setup the game object and model
   */
  public void setupGame() {
    logger.info("Starting a new challenge");

    //Start new game
    game = new Game(5, 5, musicOn, sfxOn);
    game.setLives(lives);
    if(hard){
      game.setHard();
    }
  }

  /**
   * Initialise the scene and start the game
   */
  @Override
  public void initialise() {
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
        game.switchOff();
        logger.info("Back to previous screen");
        gameWindow.startMenu();
      } else if (event.getCode() == KeyCode.E || event.getCode() == KeyCode.C || event.getCode() == KeyCode.CLOSE_BRACKET) {
        game.rotateCurrentPiece(true); //Rotates piece clockwise
      } else if (event.getCode() == KeyCode.Q || event.getCode() == KeyCode.Z || event.getCode() == KeyCode.OPEN_BRACKET){
        game.rotateCurrentPiece(false); //Rotate anti-clockwise
      } else if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.R){
        game.swapCurrentPiece(); //Swap pieces
      } else if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP){
        if(hoverY > 0){
            if(board.getBlock(hoverX, hoverY).getValue() == 0) {
              //Clear previous block
              board.getBlock(hoverX, hoverY).hover(false);
              board.getBlock(hoverX, hoverY).paint();
            }
            hoverY--; //Shift up
            if(board.getBlock(hoverX, hoverY).getValue() == 0) {
              board.getBlock(hoverX, hoverY).hover(true);
              board.getBlock(hoverX, hoverY).paint();
            }
        }
      } else if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN){
        if(hoverY < boardYmax){
            if(board.getBlock(hoverX, hoverY).getValue() == 0) {
              //Clear previous block
              board.getBlock(hoverX, hoverY).hover(false);
              board.getBlock(hoverX, hoverY).paint();
            }
            hoverY++; //Shift down
            if(board.getBlock(hoverX, hoverY).getValue() == 0) {
              board.getBlock(hoverX, hoverY).hover(true);
              board.getBlock(hoverX, hoverY).paint();
            }
        }
      } else if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT){
        if(hoverX > 0){
            if(board.getBlock(hoverX, hoverY).getValue() == 0) {
              //Clear previous block
              board.getBlock(hoverX, hoverY).hover(false);
              board.getBlock(hoverX, hoverY).paint();
            }
            hoverX--; //Shift left
          if(board.getBlock(hoverX, hoverY).getValue() == 0) {
            board.getBlock(hoverX, hoverY).hover(true);
            board.getBlock(hoverX, hoverY).paint();
          }
        }
      } else if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT){
        if(hoverX < boardXmax){
            if(board.getBlock(hoverX, hoverY).getValue() == 0) {
              //Clear previous block
              board.getBlock(hoverX, hoverY).hover(false);
              board.getBlock(hoverX, hoverY).paint();
            }
            hoverX++; //Shift right
            if(board.getBlock(hoverX, hoverY).getValue() == 0) {
              board.getBlock(hoverX, hoverY).hover(true);
              board.getBlock(hoverX, hoverY).paint();
            }
        }
      } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.X){
        //Use blockClicked to treat event like a block was clicked
        game.blockClicked(board.getBlock(hoverX, hoverY));
      }
    }));
  }


  /**
   * Method to change the piece according to the integer
   * @param gp the game piece
   * @param i the action identifier
   */
  private void changePiece(GamePiece gp, int i){
    if(i == 1){
      logger.info("New piece created!");
      currentPieceBoard.setPiece(gp);
    } else if (i == 2) {
      followingPieceBoard.setPiece(gp);
    } else if (i == 3) {
      logger.info("Piece rotated!");
      currentPieceBoard.setPiece(gp);
    } else if (i == 4){
      currentPieceBoard.setPiece(gp);
    } else if (i == 5){
      followingPieceBoard.setPiece(gp);
    }
    if(i == 4 || i == 5){
      logger.info("Pieces swapped!");
    }
  }

  /**
   * Pass the coordinates
   * @param coordinates the cleared blocks
   */
  private void lineCleared(HashSet<GameBlockCoordinate> coordinates){
    board.fadeOut(coordinates);
  }

  /**
   * Resets the game loops
   * @param millis the time in milliseconds for a loop
   */
  private void resetGameLoop(int millis){
    //keyframes used to animate the rectangle
    var timeProgress = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(barTime.fillProperty(), Color.GREEN)),
        new KeyFrame(Duration.ZERO, new KeyValue(barTime.widthProperty(), botStack.getWidth()*0.9)),
        new KeyFrame(new Duration( millis * 0.5), new KeyValue(barTime.fillProperty(),
            Color.YELLOW)), new KeyFrame(new Duration( millis * 0.75),
        new KeyValue(barTime.fillProperty(), Color.RED)),
        new KeyFrame(new Duration(millis), new KeyValue(barTime.widthProperty(), 0)));
    timeProgress.play();

  }

  /**
   * Cleans up resources and stops the game
   * @param game
   */
  protected void endChallenge(Game game){
    Multimedia.stopM();
    Multimedia.stopLM();
    game.switchOff();
    gameWindow.startScores(game, false, easy);
  }

  /**
   * Gets the high score from the score scene
   */
  private void getHighScore(){
    var tempScene = new ScoresScene(gameWindow, null, false, easy);
    highestScore.set(tempScene.getHighestScore());
  }
}
