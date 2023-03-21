package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameEndListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game {

  private static final Logger logger = LogManager.getLogger(Game.class);

  /**
   * Number of rows
   */
  protected final int rows;

  /**
   * Number of columns
   */
  protected final int cols;

  /**
   * The grid model linked to the game
   */
  protected final Grid grid;

  /**
   * Current piece to be played
   */
  protected GamePiece currentPiece;

  /**
   * The upcoming piece to be played
   */
  protected GamePiece followingPiece;

  /**
   * The score, initial value is 0
   */
  protected SimpleIntegerProperty score = new SimpleIntegerProperty(0);

  /**
   * The level, initial value is 0
   */
  protected SimpleIntegerProperty level = new SimpleIntegerProperty(0);

  /**
   * The lives, initial value 3
   */
  protected SimpleIntegerProperty lives = new SimpleIntegerProperty(3);

  /**
   * The point multiplier, starts at 1
   */
  protected SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

  /**
   * The bound of the level to level up
   */
  private int levelUp = 1000;

  /**
   * Listens for next piece
   */
  private NextPieceListener nextPieceListener;

  /**
   * To check if game is on for music to play
   */
  protected boolean gameOn;

  /**
   * Listens for a cleared line
   */
  protected LineClearedListener lineClearedListener;

  /**
   * the main timer of the game
   */
  protected Timer gameTimer;

  /**
   * The task of the main game timer
   */
  protected TimerTask gameTimerTask;

  /**
   * The game loop listener
   */
  protected GameLoopListener gameLoopListener;

  /**
   * THe game end listener
   */
  private GameEndListener gameEndListener;

  /**
   * Music on status
   */
  private boolean musicOn;

  /**
   * SFX on status
   */
  private boolean sfxOn;

  /**
   * Delay loop music
   */
  private Timer startTimer;

  /**
   * Hard mode
   */
  private boolean hard = false;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Game(int cols, int rows, boolean music, boolean sfx) {
    this.cols = cols;
    this.rows = rows;
    this.musicOn = music;
    this.sfxOn = sfx;
    //Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);
  }

  /**
   * Start the game
   */
  public void start() {
    logger.info("Starting game");
    initialiseGame();
  }

  /**
   * Initialise a new game and set up anything that needs to be done at the start
   */
  public void initialiseGame() {
    logger.info("Initialising game");
    //Create new GamePiece
    currentPiece = spawnPiece();
    this.nextPiece(currentPiece, 1);
    //Create another second GamePiece
    followingPiece = spawnPiece();
    this.nextPiece(followingPiece, 2);
    gameOn = true;
    //Pass to listener
    if(musicOn) {
      startTimer = new Timer();
      Multimedia.playMusic("music/game_start.wav");
      TimerTask startLoop = new TimerTask() {
        @Override
        public void run() {
            //Play loop music
            Multimedia.playLoopedMusic("music/game.wav");
        }
      };

      startTimer.schedule(startLoop, 46000);
    }
    gameTimer = new Timer();
    gameTimerTask = new TimerTask() {
      @Override
      public void run() {
        Platform.runLater(() -> {
            gameLoop();
        });
      }
    };

    gameTimer.schedule(gameTimerTask, getTimerDelay());
    gameLoopListener.setOnGameLoop(getTimerDelay());
  }


  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public void blockClicked(GameBlock gameBlock) {
    //Get the position of this block
    int x = gameBlock.getX();
    int y = gameBlock.getY();

    //Attempt to play the piece if it's playable, otherwise inform
    if (grid.canPlayPiece(currentPiece, x, y)) {
      grid.playPiece(currentPiece, x, y);
      gameLooped();
      if(sfxOn) {
        Multimedia.playAudioEffect("sounds/place.wav");
      }
      logger.info("Piece {} has been placed", currentPiece);
      afterPiece();
      //Reset timer
      resetTimer();
    } else {
      logger.info("Piece cannot be placed");
    }
  }

  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /**
   * A method that randomly creates a GamePiece
   *
   * @return
   */
  protected GamePiece spawnPiece() {
    Random random = new Random();
    //Generate a random integer from 0 to 14
    int piece = random.nextInt(15);
    return GamePiece.createPiece(piece);
  }

  /**
   * Creates a new piece and replaces the current one with it
   */
  protected void nextPiece() {
    currentPiece = followingPiece; //Substitute with next piece
    //Pass to listener
    this.nextPiece(currentPiece, 1);
    followingPiece = spawnPiece(); //Get a new following piece
    //Pass to listener
    this.nextPiece(followingPiece, 2);
  }

  /**
   * This method clears any full vertical/horizontal lines that have been created
   */
  public void afterPiece() {
    int blocks = 0;
    boolean full;
    ArrayList<Integer> fullCol = new ArrayList<Integer>();
    ArrayList<Integer> fullRow = new ArrayList<Integer>();
    HashSet coordinates = new HashSet();
    //Check full columns
    for (var x = 0; x < cols; x++) {
      full = true;
      for (var y = 0; y < rows; y++) {
        if (grid.getGridProperty(x, y).get() == 0) {
          full = false;
        }
      }
      if (full == true) {
        fullCol.add(x);
      }
    }
    //Check full rows
    for (var y = 0; y < rows; y++) {
      full = true;
      for (var x = 0; x < cols; x++) {
        if (grid.getGridProperty(x, y).get() == 0) {
          full = false;
        }
      }
      if (full == true) {
        fullRow.add(y);
      }
    }
    //Remove the full columns
    for (var x = 0; x < fullCol.size(); x++) {
      for (var y = 0; y < rows; y++) {
        if (grid.get(fullCol.get(x), y) != 0) {
          coordinates.add(new GameBlockCoordinate(fullCol.get(x), y));
          grid.set(fullCol.get(x), y, 0);
          blocks++;
        }
      }
    }
    //Remove the full rows
    for (var y = 0; y < fullRow.size(); y++) {
      for (var x = 0; x < cols; x++) {
        if (grid.get(x, fullRow.get(y)) != 0) {
          coordinates.add(new GameBlockCoordinate(x, fullRow.get(y)));
          grid.set(x, fullRow.get(y), 0);
          blocks++;
        }
      }
    }
    //Set lines removed in total
    int lines = fullCol.size() + fullRow.size();
    //Play clear sound
    if (lines != 0) {
      if(sfxOn) {
        Multimedia.playAudioEffect("sounds/clear.wav");
      }
    }
    score(lines, blocks);
    lineCleared(coordinates);
  }

  /**
   * Gets the score
   *
   * @return the score
   */
  public int getScore() {
    return score.get();
  }

  /**
   * Gets the score simple property
   *
   * @return the score property
   */
  public SimpleIntegerProperty scoreProperty() {
    return score;
  }

  /**
   * Sets the score
   *
   * @param score the score
   */
  public void setScore(int score) {
    this.score.set(score);
  }

  /**
   * Gets the level
   *
   * @return the level
   */
  public int getLevel() {
    return level.get();
  }

  /**
   * Gets the level simple property
   *
   * @return the level property
   */
  public SimpleIntegerProperty levelProperty() {
    return level;
  }

  /**
   * Sets the level
   *
   * @param level the level
   */
  public void setLevel(int level) {
    this.level.set(level);
  }

  /**
   * Gets the lives available
   *
   * @return the lives
   */
  public int getLives() {
    return lives.get();
  }

  /**
   * Gets the lives simple property
   *
   * @return the lives property
   */
  public SimpleIntegerProperty livesProperty() {
    return lives;
  }

  /**
   * Sets the lives
   *
   * @param lives
   */
  public void setLives(int lives) {
    this.lives.set(lives);
  }

  /**
   * Gets the multiplier
   *
   * @return multiplier
   */
  public int getMultiplier() {
    return multiplier.get();
  }

  /**
   * Gets the multiplier simple property
   *
   * @return multiplier property
   */
  public SimpleIntegerProperty multiplierProperty() {
    return multiplier;
  }

  /**
   * Sets the multiplier
   *
   * @param multiplier multiplier
   */
  public void setMultiplier(int multiplier) {
    this.multiplier.set(multiplier);
  }

  /**
   * Resets the multiplier back to 1
   */
  public void resetMultiplier() {
    this.multiplier.set(1);
    logger.info("Multiplier has reset back to {}x", getMultiplier());
  }

  /**
   * Adds to the score if lines are cleared
   *
   * @param lines         cleared lines
   * @param blocksCleared cleared blocks
   */
  protected void score(int lines, int blocksCleared) {
    //Checks if lines are cleared or not
    if (lines != 0 && blocksCleared != 0) {
      //Calculates to add to new score. Current score + linesCleared*blocksCleared*10*multiplier
      setScore(getScore() + (lines * blocksCleared * 10 * getMultiplier()));
      logger.info("{} lines cleared. {} blocks cleared", lines, blocksCleared);
      logger.info("New score is {}", getScore());
      //Increments multiplier
      setMultiplier(getMultiplier() + 1);
      logger.info("Multiplier: {}x", getMultiplier());
      updateLevel();
    } else {
      resetMultiplier();
    }
  }

  /**
   * Check if level can increase
   */
  protected void updateLevel() {
    if (getScore() >= levelUp) {
      //Level up +1
      setLevel(getLevel() + 1);
      //Increases bound for next level
      levelUp += 1000;
      logger.info("Level up! Current level: {}", getLevel());
      if(sfxOn) {
        Multimedia.playAudioEffect("sounds/level.wav");
      }
    }
  }

  /**
   * Add listener to listen for a new piece
   *
   * @param listener
   */
  public void setNextPieceListener(NextPieceListener listener) {
    this.nextPieceListener = listener;
  }

  /**
   * Call attached listener when a piece is created or rotated
   *
   * @param gamePiece
   */
  public void nextPiece(GamePiece gamePiece, int i) {
    if (nextPieceListener != null) {
      nextPieceListener.nextPiece(gamePiece, i);
    }
  }

  /**
   * Switch off the game to tell audio to stop playing
   */
  public void switchOff() {
    gameOn = false;
    if(startTimer != null){
      startTimer.cancel();
      startTimer.purge();
    }
    gameTimer.cancel();
    gameTimer.purge();
  }

  /**
   * Rotate the current piece
   */
  public void rotateCurrentPiece(boolean clockwise) {
    if (clockwise) {
      currentPiece.rotate();
    } else {
      currentPiece.rotateInv();
    }
    if(sfxOn) {
      Multimedia.playAudioEffect("sounds/rotate.wav");
    }
    nextPiece(currentPiece, 3);
  }

  /**
   * Method to swap the pieces
   */
  public void swapCurrentPiece() {
    if(sfxOn) {
      Multimedia.playAudioEffect("sounds/rotate.wav");
    }
    var temp = currentPiece;
    currentPiece = followingPiece;
    followingPiece = temp;
    //Pass to listener
    nextPiece(currentPiece, 4);
    nextPiece(followingPiece, 5);
  }

  /**
   * Set a listener for lines cleared
   *
   * @param listener LineClearedListener
   */
  public void setLineClearedListener(LineClearedListener listener) {
    this.lineClearedListener = listener;
  }

  /**
   * Call attached listener for the blocks cleared
   *
   * @param coordinates the coordinates of the cleared blocks
   */
  public void lineCleared(HashSet<GameBlockCoordinate> coordinates) {
    if (lineClearedListener != null) {
      lineClearedListener.lineCleared(coordinates);
    }
  }

  /**
   * Get the time for the game loop
   *
   * @return
   */
  protected int getTimerDelay() {
    int defaultTime = 12000;
    if(hard){
      defaultTime = 4500;
    }
    //Check if it is lower than 2500 milliseconds
    if (defaultTime - (500 * getLevel()) <= 2500) {
      return 2500;
    } else {
      return defaultTime - (500 * getLevel());
    }
  }

  /**
   * The loop that executes when the player does not place a block in the time left
   */
  protected void gameLoop() {
    //Subtract one life
    if (getLives() > 0) {
      setLives(getLives() - 1);
      logger.info("Lost a life!");
      if(sfxOn) {
        Multimedia.playAudioEffect("sounds/lifelose.wav");
      }
      //Create new GamePiece
      currentPiece = spawnPiece();
      this.nextPiece(currentPiece, 1);
      resetMultiplier();
      gameLooped();
      resetTimer();
    } else {
      endGame();
    }
  }

  /**
   * Sets game loop listener
   *
   * @param gameLoopListener
   */
  public void setGameLoopListener(GameLoopListener gameLoopListener) {
    this.gameLoopListener = gameLoopListener;
  }

  /**
   * Calls attacked listener for the loop time
   */
  public void gameLooped() {
    if (gameLoopListener != null) {
      gameLoopListener.setOnGameLoop(getTimerDelay());
    }
  }

  /**
   * Sets the listener for when the game ends
   * @param gameEndListener
   */
  public void setGameEndListener(GameEndListener gameEndListener){
    this.gameEndListener = gameEndListener;
  }

  /**
   * Ends the game
   */
  public void endGame(){
    gameTimer.cancel();
    gameTimer.purge();
    if (gameEndListener != null) {
      gameEndListener.gameEnded(this);
    }
  }

  /**
   * Resets the timer
   */
  protected void resetTimer(){
    nextPiece();
    gameTimer.cancel();
    gameTimer.purge();
    gameTimer = new Timer();
    gameTimerTask = new TimerTask() {
      @Override
      public void run() {
        Platform.runLater(() -> gameLoop());
      }
    };
    gameTimer.schedule(gameTimerTask, getTimerDelay());
  }

  /**
   * Returns null is this is just a local game
   * @return null
   */
  public ArrayList<Pair<String, Integer>> getScoresList() {
    return null;
  }

  /**
   * Makes the game "slightly" harder
   */
  public void setHard(){
    hard = true;
  }
}
