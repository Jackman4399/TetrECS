package uk.ac.soton.comp1206.event;

import java.util.HashSet;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

public interface LineClearedListener {

  /**
   * Passes the coordinates of cleared blocks
   * @param coordinates cleared blocks
   */
  void lineCleared(HashSet<GameBlockCoordinate> coordinates);

}
