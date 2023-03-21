package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

public class PieceBoard extends GameBoard{

  /**
   * Columns
   */
  private int cols;

  /**
   * Rows
   */
  private int rows;

  /**
   * An identifier for the board
   */
  private String name;

  public PieceBoard(int cols, int rows, double width, double height, String name) {
    super(cols, rows, width, height);
    this.cols = cols;
    this.rows = rows;
    this.name = name;
  }

  public void setPiece(GamePiece gamePiece){
    for(int i = 0; i < cols; i++){
      for(int j = 0; j < rows; j++){
        grid.set(i, j, 0);
      }
    }
    grid.playPiece(gamePiece, 1,1);
  }

  /**
   * Gets the name of this piece board
   * @return
   */
  public String getName() {
    return name;
  }
}
