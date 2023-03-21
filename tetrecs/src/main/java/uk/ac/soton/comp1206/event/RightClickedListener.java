package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBoard;

/**
 * A listener interface for when the main board is clicked
 */
public interface RightClickedListener {

  /**
   * Method with no parameters for right click
   */
  void rightClicked(GameBoard gameBoard, boolean bool);

}
