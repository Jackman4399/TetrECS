package uk.ac.soton.comp1206.component;

import java.util.ArrayList;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;

public class LeaderBoard extends ScoresList{

  private static final Logger logger = LogManager.getLogger(LeaderBoard.class);

  /**
   * Constructor using super
   */
  public LeaderBoard(){
    super();
  }

  /**
   * Instant reveal instead of animated
   */
  @Override
  public void reveal() {
    logger.info("Revealing scores");

    //Add it to the score
    for(HBox score: scoresArray){
      this.getChildren().add(score);
    }
  }
}
