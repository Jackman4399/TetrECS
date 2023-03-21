package uk.ac.soton.comp1206.component;

import java.util.ArrayList;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoresList extends VBox {

  private static final Logger logger = LogManager.getLogger(ScoresList.class);

  /**
   * A list property to work with the binding
   */
  private SimpleListProperty<Pair<String, Integer>> list = new SimpleListProperty();

  /**
   * The arraylist that holds all the HBox with the pairs
   */
  protected final ArrayList<HBox> scoresArray = new ArrayList<>();

  /**
   * A final list of colors for the UI visuals
   */
  public static final Color[] COLOURS = {
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

  /**
   * Constructor to make a score list object
   */
  public ScoresList(){
    getStyleClass().add("scorelist");
    setPadding(new Insets(5));
    setAlignment(Pos.CENTER);
    setSpacing(4);
  }

  /**
   * Creates the scores
   */
  public void createScores(){

    logger.info("Creating score list...");
    scoresArray.clear();
    this.getChildren().clear();

    int i = 0;
    while(i < 10 && i < list.size()){

      //One pair per HBox
      var score = new HBox();
      //score.setOpacity(0);
      score.setAlignment(Pos.CENTER);
      score.setSpacing(5);

      //Gets the name of the player
      var name = new Text(list.get(i).getKey());
      if(name.getText().contains("DEAD100")){
        name.setText(name.getText().split("DEAD100")[0]);
        name.setStrikethrough(true);
      }
      name.getStyleClass().add("scorer");
      name.setTextAlignment(TextAlignment.CENTER);
      name.setFill(COLOURS[i]);
      HBox.setHgrow(name, Priority.ALWAYS);

      //Gets the score of the player
      var scoreNum = new Text(": " + list.get(i).getValue().toString());
      scoreNum.getStyleClass().add("points");
      scoreNum.setTextAlignment(TextAlignment.CENTER);
      scoreNum.setFill(COLOURS[i]);
      HBox.setHgrow(scoreNum, Priority.ALWAYS);

      logger.info(list.get(i).toString());

      score.getChildren().addAll(name, scoreNum);
      scoresArray.add(score);
      i++;
    }
  }

  /**
   * Animates the display of the scores
   */
  public void reveal() {
    logger.info("Revealing scores");

    //Add it to the score
    for(HBox score: scoresArray){
      this.getChildren().add(score);
    }

    var transitions = new ArrayList<>();


    for (HBox tag : scoresArray) {
      var ft = new FadeTransition(new Duration(200), tag);
      ft.setFromValue(0);
      ft.setToValue(1);
      transitions.add(ft);
    }
    var st = new SequentialTransition(transitions.toArray(Animation[]::new));
    st.play();
  }

  /**
   * To bind the list in ScoreScene
   *
   * @return the list of name and score
   */
  public ListProperty<Pair<String, Integer>> getListProperty() {
    return list;
  }

}
