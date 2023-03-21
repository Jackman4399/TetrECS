package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Listener for a new piece to be displayed
 */
public interface NextPieceListener {

  /**
   * Handle the interactions with the piece boards
   * @param gp the game piece
   * @param i the identification number for the type of action to take
   */
  void nextPiece(GamePiece gp, int i);

}
