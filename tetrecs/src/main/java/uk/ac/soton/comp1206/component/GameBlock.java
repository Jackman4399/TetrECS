package uk.ac.soton.comp1206.component;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.Glow;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 * <p>
 * Extends Canvas and is responsible for drawing itself.
 * <p>
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * <p>
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

  private static final Logger logger = LogManager.getLogger(GameBlock.class);

  /**
   * The set of colours for different pieces
   */
  public static final Color[] COLOURS = {
      Color.TRANSPARENT,
      Color.DEEPPINK,
      Color.RED,
      Color.ORANGE,
      Color.YELLOW,
      Color.YELLOWGREEN,
      Color.LIME,
      Color.GREEN,
      Color.DARKGREEN,
      Color.DARKTURQUOISE,
      Color.DEEPSKYBLUE,
      Color.AQUA,
      Color.CRIMSON,
      Color.BLUE,
      Color.MEDIUMPURPLE,
      Color.PURPLE
  };

  private final GameBoard gameBoard;

  private final double width;
  private final double height;

  /**
   * The column this block exists as in the grid
   */
  private final int x;

  /**
   * The row this block exists as in the grid
   */
  private final int y;

  /**
   * The value of this block (0 = empty, otherwise specifies the colour to render as)
   */
  private final IntegerProperty value = new SimpleIntegerProperty(0);

  /**
   * Animation Timer of this block
   */
  private GameBlock.AnimationTimer animTimer;

  /**
   * Create a new single Game Block
   *
   * @param gameBoard the board this block belongs to
   * @param x         the column the block exists in
   * @param y         the row the block exists in
   * @param width     the width of the canvas to render
   * @param height    the height of the canvas to render
   */
  public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
    this.gameBoard = gameBoard;
    this.width = width;
    this.height = height;
    this.x = x;
    this.y = y;

    //A canvas needs a fixed width and height
    setWidth(width);
    setHeight(height);

    //Do an initial paint
    paint();

    //When the value property is updated, call the internal updateValue method
    value.addListener(this::updateValue);
  }

  /**
   * When the value of this block is updated,
   *
   * @param observable what was updated
   * @param oldValue   the old value
   * @param newValue   the new value
   */
  private void updateValue(ObservableValue<? extends Number> observable, Number oldValue,
      Number newValue) {
    paint();
  }

  /**
   * Handle painting of the block canvas
   */
  public void paint() {
    if (this.isHover() == true && value.get() == 0) {
      //Paint when hovered over
      paintHover();
    } else if (!this.isHover() && value.get() == 0) {
      //If the block is empty, paint as empty
      paintEmpty();
    } else if (gameBoard instanceof PieceBoard && ((PieceBoard) gameBoard).getName()
        .equalsIgnoreCase("Current") && getX() == 1 && getY() == 1) {
      paintColor(COLOURS[value.get()], true);
    } else {
      //If the block is not empty, paint with the colour represented by the value
      paintColor(COLOURS[value.get()], false);
    }
  }

  /**
   * Paint this canvas empty
   */
  private void paintEmpty() {
    var gc = getGraphicsContext2D();

    //Clear
    gc.clearRect(0, 0, width, height);

    //Fill
    gc.setFill(Color.TRANSPARENT);
    gc.fillRoundRect(0, 0, width, height, 5, 5);

    //Border
    gc.setStroke(Color.GRAY);
    gc.strokeRoundRect(0, 0, width, height, 5, 5);
  }

  /**
   * Paint this canvas with the given colour
   *
   * @param colour the colour to paint
   */
  private void paintColor(Color colour, boolean center) {
    var gc = getGraphicsContext2D();

    //Clear
    gc.clearRect(0, 0, width, height);
    gc.setEffect(new Glow(0.8));

    //Colour fill
    gc.setFill(colour);
    gc.fillRoundRect(0, 0, width, height, 5, 5);
    gc.setLineWidth(1.5);

    gc.setEffect(null);

    gc.setStroke(Color.BLACK);
    gc.strokeRect(width * .2, height * .2, width * .6, height * .6);

    //Border
    gc.setStroke(Color.DARKGRAY.darker());
    gc.strokeRoundRect(0, 0, width, height, 2, 2);

    gc.beginPath();
    gc.setStroke(Color.BLACK);
    gc.moveTo(0, 0);
    gc.lineTo(width * .2, height * .2);
    gc.moveTo(0, height);
    gc.lineTo(width * .2, height * .8);
    gc.moveTo(width, height);
    gc.lineTo(width * .8, height * .8);
    gc.moveTo(width, 0);
    gc.lineTo(width * .8, height * .2);
    gc.stroke();
    gc.closePath();

    if (center) {
      gc.setFill(Color.WHITE.deriveColor(1, 1, 1, 0.7));
      gc.fillOval(width / 3, height / 3, width / 3, height / 3);
    }
  }

  /**
   * Paint for the hovered block
   */
  private void paintHover() {
    var gc = getGraphicsContext2D();
    //Clear
    gc.clearRect(0, 0, width, height);
    //Apply low opacity colour
    gc.setFill(Color.WHITESMOKE.deriveColor(1, 1, 1, 0.5));
    gc.fillRoundRect(0, 0, width, height, 5, 5);
  }


  /**
   * Get the column of this block
   *
   * @return column number
   */
  public int getX() {
    return x;
  }

  /**
   * Get the row of this block
   *
   * @return row number
   */
  public int getY() {
    return y;
  }

  /**
   * Get the current value held by this block, representing it's colour
   *
   * @return value
   */
  public int getValue() {
    return this.value.get();
  }

  /**
   * Bind the value of this block to another property. Used to link the visual block to a
   * corresponding block in the Grid.
   *
   * @param input property to bind the value to
   */
  public void bind(ObservableValue<? extends Number> input) {
    value.bind(input);
  }

  /**
   * Sets hover status
   * @param bool status true or false
   */
  public void hover(boolean bool) {
    this.setHover(bool);
    paint();
  }

  /**
   * Animation Timer class makes the block disappear/fade out
   */
  public class AnimationTimer extends javafx.animation.AnimationTimer{
    //Set initial opacity
    double opacity = 1.0;

    private void fadeOut(){
      paintEmpty();
      //Decreases opacity of the block
      opacity-=0.05;
      if(opacity <= 0){
        this.stop();
        GameBlock.this.animTimer = null;
      } else {
        var gc = getGraphicsContext2D();
        //Set new color and variable opacity
        gc.setFill(Color.color(1,1,1, opacity));
        gc.fillRect(0,0,width, height);
      }
    }

    @Override
    public void handle(long now) {
      fadeOut();
    }
  }

  /**
   * Starts the timer for the animation
   */
  public void fade(){
    animTimer = new AnimationTimer();
    animTimer.start();
  }

}
