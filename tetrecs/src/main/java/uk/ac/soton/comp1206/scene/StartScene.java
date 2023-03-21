package uk.ac.soton.comp1206.scene;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class StartScene extends BaseScene{

  private static final Logger logger = LogManager.getLogger(StartScene.class);

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public StartScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  @Override
  public void initialise() {
    var temp = new SettingsScene(gameWindow);
    //Set volume to match the settings
    Multimedia.changeSFX(temp.getSfx());
    Multimedia.changeMusic(temp.getMusic());
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //Set up a stack pane and make the background black
    var mainPane = new StackPane();
    mainPane.setMaxWidth(gameWindow.getWidth());
    mainPane.setMaxHeight(gameWindow.getHeight());
    mainPane.setStyle("-fx-background-color: black");
    root.getChildren().add(mainPane);

    //Get the image
    var image = new Image(Multimedia.class.getResource("/images/ECSGames.png").toExternalForm());
    //Use image in imageview
    var imageView = new ImageView(image);
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(gameWindow.getWidth()/3);
    //Put the node in a HBox to set alignment
    var box = new HBox(imageView);
    box.setAlignment(Pos.CENTER);
    mainPane.getChildren().add(box);
    //Fade transitions
    var ft = new FadeTransition(Duration.millis(4000), imageView);
    ft.setFromValue(0);
    ft.setToValue(1);
    var ft2 = new FadeTransition(Duration.millis(4000), imageView);
    ft2.setFromValue(1);
    ft2.setToValue(0);
    var st = new SequentialTransition(ft, ft2);
    //What to do on end, call menu scene
    st.setOnFinished(event -> {
      logger.info("Proceeding to menu...");
      gameWindow.startMenu();
    });
    //Play audio with animation
    Multimedia.playAudioEffect("sounds/intro.mp3");
    st.play();

  }
}
