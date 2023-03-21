package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class OptionsScene extends BaseScene{

  private static final Logger logger = LogManager.getLogger(OptionsScene.class);

  /**
   * To set the number of lives
   */
  private int lives = 3;

  /**
   * Booleans to set the audio of the game
   */
  private boolean musicOn = true;
  private boolean sfxOn = true;

  /**
   * Hard mode
   */
  private boolean hard = false;


  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public OptionsScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Pick your poison~");
  }

  @Override
  public void initialise() {
    //Back to main menu
    scene.setOnKeyPressed((event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        logger.info("Back to previous screen");
        gameWindow.startMenu();
      }
    }));
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var mainStackPane = new StackPane();
    mainStackPane.setMaxWidth(gameWindow.getWidth());
    mainStackPane.setMaxHeight(gameWindow.getHeight());
    mainStackPane.getStyleClass().add("instructions-background");
    root.getChildren().add(mainStackPane);
    //The main VBox to hold the elements in the centre
    var mainVBox = new VBox();
    mainVBox.setPadding(new Insets(10));
    mainVBox.setAlignment(Pos.CENTER);
    mainVBox.setSpacing(40);
    //The main border pane
    var main = new BorderPane();
    mainStackPane.getChildren().add(main);
    //Title
    var title = new Text("Pick your options");
    title.getStyleClass().add("title");
    var wrap = new HBox(title);
    wrap.setAlignment(Pos.CENTER);
    main.setTop(wrap);

    //For the music
    var music = new CheckBox("Music");
    music.getStyleClass().add("option");
    music.setSelected(true);
    //Stop music from playing
    music.setOnAction(event -> {
      if(!music.isSelected()){
        Multimedia.stopLM();
      } else {
        Multimedia.playLoopedMusic("music/menu.mp3");
      }
    });
    //For the SFX
    var sfx = new CheckBox("SFX");
    sfx.getStyleClass().add("option");
    sfx.setSelected(true);

    //For the hard mode
    var hardMode = new CheckBox("Hard Mode");
    hardMode.getStyleClass().add("option");
    //Warning for the easy mode
    var warning = new Text("Warning: 4 lives means your score will NOT count!");
    warning.getStyleClass().add("heading");
    warning.setVisible(false);
    //Radio buttons for the lives
    var lives1 = new RadioButton("1 life");
    lives1.getStyleClass().add("option");
    lives1.setOnAction(event -> {
      if(lives1.isSelected()){
        hardMode.setSelected(false);
        warning.setVisible(false);
      }
    });
    var lives2 = new RadioButton("2 lives");
    lives2.getStyleClass().add("option");
    lives2.setOnAction(event -> {
      if(lives2.isSelected()){
        hardMode.setSelected(false);
        warning.setVisible(false);
      }
    });
    var lives3 = new RadioButton("3 lives");
    lives3.getStyleClass().add("option");
    lives3.setOnAction(event -> {
      if(lives3.isSelected()){
        hardMode.setSelected(false);
        warning.setVisible(false);
      }
    });
    var lives4 = new RadioButton("4 lives");
    lives4.setOnAction(event -> {
      if(lives4.isSelected()){
        warning.setVisible(true);
      }
    });
    lives4.getStyleClass().add("option");
    //Set the 3 lives as standard
    lives3.fire();
    //A group to hold all the radio buttons, also only allows one to be selected
    var livesGroup = new ToggleGroup();
    lives1.setToggleGroup(livesGroup);
    lives2.setToggleGroup(livesGroup);
    lives3.setToggleGroup(livesGroup);
    lives4.setToggleGroup(livesGroup);
    //Add to UI
    var hbox1 = new HBox(lives1, lives2, lives3, lives4);
    hbox1.setSpacing(10);
    hbox1.setAlignment(Pos.CENTER);
    main.setCenter(mainVBox);

    mainVBox.getChildren().addAll(warning, hbox1, music, sfx, hardMode);

    hardMode.setOnAction(event -> {
      if(hardMode.isSelected()){
        lives1.setSelected(true);
        hardMode.setSelected(true);
      }
    });

    //Confirm button
    var confirm = new Button("Confirm");
    confirm.getStyleClass().add("button");
    confirm.setPadding(new Insets(10));
    confirm.setTextAlignment(TextAlignment.CENTER);
    mainVBox.getChildren().add(confirm);

    confirm.setOnAction(event -> {
      //Gets all the confirmed settings and passes it on
      musicOn = music.isSelected();
      sfxOn = sfx.isSelected();
      var selected = livesGroup.getSelectedToggle();
      if(selected == lives1){
        lives = 1;
      } else if(selected == lives2){
        lives = 2;
      } else if(selected == lives3){
        lives = 3;
      } else if(selected == lives4){
        lives = 4;
      }
      if(sfxOn) {
        Multimedia.playAudioEffect("sounds/pling.wav");
      }
      if(hardMode.isSelected()){
        hard = true;
      }
      startGame();
    });
  }

  //Proceeds to the game with the current settings
  private void startGame(){
    gameWindow.startChallenge(musicOn, sfxOn, lives, hard);
  }
}
