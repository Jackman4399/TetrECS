package uk.ac.soton.comp1206.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

public class MultiplayerGame extends Game {

  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
  private final Communicator communicator;
  private boolean ready = false;
  private ArrayDeque<GamePiece> pieces = new ArrayDeque();
  private ArrayList<Pair<String, Integer>> scores = new ArrayList();

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public MultiplayerGame(int cols, int rows, GameWindow gameWindow) {
    super(cols, rows, true, true);
    this.communicator = gameWindow.getCommunicator();
  }

  /**
   * Initialise the game
   */
  @Override
  public void initialiseGame() {
    logger.info("Initialising multiplayer game");
    communicator.addListener(e -> Platform.runLater(() -> receive(e.trim())));
    //Create new GamePiece
    gameOn = true;
    //Pass to listener
    Multimedia.playMusic("music/game_start.wav");
    TimerTask startLoop = new TimerTask() {
      @Override
      public void run() {
        if (gameOn) {
          //Play loop music
          Multimedia.playLoopedMusic("music/game.wav");
        }
      }
    };
    //Starts the looped music
    var startTimer = new Timer();
    startTimer.schedule(startLoop, 46000);

    gameTimerTask = new TimerTask() {
      @Override
      public void run() {
        Platform.runLater(() -> {
          if(gameOn){
            gameLoop();
          }
        });
      }
    };
    //Runs the game loop
    gameTimer = new Timer();
    gameTimer.schedule(gameTimerTask, getTimerDelay());
    gameLoopListener.setOnGameLoop(getTimerDelay());
    //Request 6 initial pieces
    for(int i = 0; i < 6; i ++) {
      communicator.send("PIECE");
    }
  }

  /**
   * Receives messages from the server
   *
   * @param message message from communicator
   */
  public void receive(String message) {
    logger.info("Received message: {}", message);
    var msg = message.split(" ", 2);
    if (msg[0].equals("PIECE") && msg.length > 1) {
      //WHEN IT RECEIVES A PIECE
      //To give a random rotation
      var random = new Random();
      var newPiece = GamePiece.createPiece(Integer.parseInt(msg[1]), random.nextInt(0, 4));
      logger.info("Received piece from server: {}", newPiece);
      pieces.add(newPiece);
      if (!ready && pieces.size() > 2) {
        ready = true;
        followingPiece = spawnPiece();
        nextPiece(); //The one from the parent class
      }
    } else if (msg[0].equals("SCORES") && msg.length > 1) {
      //WHEN IT RECEIVES A SET OF SCORES
      logger.info("Received scores: {}", msg[1]);
      this.scores.clear();
      String[] scoreList = msg[1].split("\n");

      for (String score : scoreList) {
        String[] split = score.split(":");
        logger.info("Received score: {} = {}", split[0], Integer.parseInt(split[1]));
        this.scores.add(new Pair(split[0], Integer.parseInt(split[1])));
      }
      //Sort the scores
      this.scores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    }
  }

  /**
   * Spawns a game piece
   * @return the first piece in the queue
   */
  @Override
  protected GamePiece spawnPiece() {
    communicator.send("PIECE");
    return pieces.pop();
  }

  /**
   * Block Click handler
   * @param gameBlock the block that was clicked
   */
  @Override
  public void blockClicked(GameBlock gameBlock) {
    //Use a string builder to append continuously instead of creating a new string
    var builder = new StringBuilder();
    for(int x = 0; x < getCols(); x++){
      for(int y = 0; y < getRows(); y++){
        //Add the value of the block to the string
        builder.append(getGrid().get(x, y) + " ");
      }
    }
    //Send the string for cheating purposes
    communicator.send("BOARD" + builder.toString().trim());
    super.blockClicked(gameBlock);
  }

  /**
   * After piece is placed actions
   */
  @Override
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
      Multimedia.playAudioEffect("sounds/clear.wav");
    }
    score(lines, blocks);
    lineCleared(coordinates);
  }

  /**
   * Give a score, or reset multiplier
   * @param lines         cleared lines
   * @param blocksCleared cleared blocks
   */
  @Override
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
      //Send it to the server
      communicator.send("SCORE " + getScore());
    } else {
      resetMultiplier();
    }
  }

  /**
   * The game loop
   */
  @Override
  protected void gameLoop() {
    //Subtract one life
    if (getLives() > 0) {
      setLives(getLives() - 1);
      logger.info("Lost a life!");
      Multimedia.playAudioEffect("sounds/lifelose.wav");
      //Create new GamePiece
      currentPiece = spawnPiece();
      this.nextPiece(currentPiece, 1);
      resetMultiplier();
      gameLooped();
      resetTimer();
      communicator.send("LIVES " + getLives());
    } else {
      //end the game if out of lives
      communicator.send("DIE");
      endGame();
    }
  }

  /**
   * Returns a list since it is an online game
   * @return scores array list
   */
  @Override
  public ArrayList<Pair<String, Integer>> getScoresList() {
    return scores;
  }
}
