package uk.ac.soton.comp1206.scene;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class SettingsScene extends BaseScene{

  private static final Logger logger = LogManager.getLogger(SettingsScene.class);

  /**
   * The sfx volume
   */
  private double sfx = 1;

  /**
   * The music volume
   */
  private double music = 1;

  /**
   * Width of the window
   */
  private int width = 800;

  /**
   * Height of the window
   */
  private int height = 600;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public SettingsScene(GameWindow gameWindow) {
    super(gameWindow);
    var path = new File("src/main/resources/settings.txt");
    //If the file does not exist, create it instead of loading it
    if(path.exists()){
      load("src/main/resources/settings.txt");
    } else {
      write("src/main/resources/settings.txt");
    }
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
    //Stackpane with a background
    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("lobby-background");
    root.getChildren().add(menuPane);
    //Main Border pane
    var mainPane = new BorderPane();
    mainPane.setPadding(new Insets(50, 10, 10, 10));
    menuPane.getChildren().add(mainPane);
    //Title at the top
    var title = new Text("Settings");
    title.getStyleClass().add("title");
    var box = new HBox(title);
    box.setAlignment(Pos.CENTER);
    mainPane.setTop(box);
    //Main VBox to hold all components
    var vbox = new VBox();
    vbox.setAlignment(Pos.CENTER);
    vbox.setSpacing(40);
    mainPane.setCenter(vbox);
    //Labels
    var lbl1 = new Text("SFX:");
    lbl1.getStyleClass().add("heading");
    var lbl2 = new Text("Music:");
    lbl2.getStyleClass().add("heading");

    //Volume slider for sfx
    var sfxSlider = new Slider(0, 1, sfx);
    //snapping to ticks
    sfxSlider.setMajorTickUnit(0.1);
    sfxSlider.setSnapToTicks(true);
    sfxSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
      sfx = newValue.doubleValue();
      Multimedia.changeSFX(sfx);
      logger.info("Changed sfx from {} to {}", oldValue, newValue);
    }));

    //Play music for test
    sfxSlider.setOnMouseReleased(event -> Multimedia.playAudioEffect("sounds/pling.wav"));

    //Volume slider for music volume
    var musicSlider = new Slider(0,1, music);
    //snapping to ticks
    musicSlider.setMajorTickUnit(0.1);
    musicSlider.setSnapToTicks(true);
    musicSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
      music = newValue.doubleValue();
      Multimedia.changeMusic(music);
      logger.info("Changed music from {} to {}", oldValue, newValue);
    }));
    //An HBox to hold the first sfx slider
    var hbox1 = new HBox(lbl1, sfxSlider);
    hbox1.setSpacing(10);
    hbox1.setAlignment(Pos.CENTER);
    //Another HBox to hold the second slider for music
    var hbox2 = new HBox(lbl2, musicSlider);
    hbox2.setSpacing(10);
    hbox2.setAlignment(Pos.CENTER);
    vbox.getChildren().addAll(hbox1, hbox2);

    //Create a menu button for the resolution
    var menu = new MenuButton("Resolution");
    menu.getStyleClass().add("menuButton");

    //Information text
    var text = new Text("To update resolution, restart the app.");
    text.setVisible(false);
    text.getStyleClass().add("heading");

    //A second vbox that holds the menu button
    var vbox2 = new VBox(menu);
    vbox2.setAlignment(Pos.CENTER);

    //Items to be displayed in the menu
    var item1 = new RadioMenuItem("800 x 600");
    item1.getStyleClass().add("menu-item");
    var item2 = new RadioMenuItem("1000 x 750");
    item2.getStyleClass().add("menu-item");
    var item3 = new RadioMenuItem("1100 x 800");
    item3.getStyleClass().add("menu-item");

    //What to do when menu hides
    menu.setOnHiding(event -> {
      menu.setFocusTraversable(false);
      root.requestFocus();
      text.setVisible(true);
    });

    //What to do when menu shows
    menu.setOnShowing(event -> {
      menu.setFocusTraversable(true);
      text.setVisible(false);
    });
    //Confirm button, click this a apply any settings changed
    var confirm = new Button("Confirm");
    confirm.getStyleClass().add("button");
    confirm.setOnAction(event -> {
      //Set the resolution
      if(item1.isSelected()){
        width = 800;
        height = 600;
      } else if(item2.isSelected()){
        width = 1000;
        height = 750;
      } else if(item3.isSelected()){
        width = 1100;
        height = 800;
      }
      //Write all the files
      write("src/main/resources/settings.txt");
      //Go back to menu
      logger.info("Back to previous screen");
      gameWindow.startMenu();
    });

    //Show a selected item according to resolution
    if(width == 800){
      item1.setSelected(true);
    } else if(width == 1000){
      item2.setSelected(true);
    } else {
      item3.setSelected(true);
    }
    //Toggle group to allow only one selection
    var tGroup = new ToggleGroup();
    tGroup.getToggles().addAll(item1, item2, item3);

    //Separator for the menu
    var separator = new SeparatorMenuItem();
    var separator1 = new SeparatorMenuItem();
    //Add items and separators to the menu
    menu.getItems().addAll(item1, separator, item2, separator1, item3);
    //Add eveything to the UI
    vbox.getChildren().addAll(vbox2, text, confirm);
  }

  /**
   * Loads the settings from a local file
   * @param file the file path
   * @return returns a completion
   */
  private boolean load(String file) {
    var settings = new ArrayList<String>();
    try {
      logger.info("Loading settings from {}", file);
      var file1 = new File(file);
      var reader = new Scanner(file1);
      while (reader.hasNextLine()) {
        String data = reader.nextLine();
        var vol = data.split("=")[1];
        settings.add(vol);
      }
      if(settings.size() < 4){
        logger.error("File mutated, delete it to create a new one.");
        return false;
      }
      sfx = Double.parseDouble(settings.get(0));
      music = Double.parseDouble(settings.get(1));
      width = Integer.parseInt(settings.get(2));
      height = Integer.parseInt(settings.get(3));
      reader.close();
      return true;
    } catch (Exception e) {
      logger.error("Unable to read file");
      return false;
    }
  }


  /**
   * Write to a local file
   * @param file the file path
   */
  private void write(String file) {
    var data = new File(file);
    //Only if the file exists
    if (data.exists()) {
      logger.info("File exists, writing...");
      try {
        var writer = new FileWriter(data);
        writer.write("sfx=" + sfx + "\n");
        writer.write("music=" + music + "\n");
        writer.write("width=" + width + "\n");
        writer.write("height=" + height);
        writer.close();
      } catch (IOException e) {
        logger.error("Issue has occurred.");
        e.printStackTrace();
      }
    } else {
      logger.info("No file exists, creating new file.");
      //Make a new file with default values if no new data exists
      try {
        data.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        var writer = new FileWriter(data);
        writer.write("sfx=" + sfx + "\n");
        writer.write("music=" + music + "\n");
        writer.write("width=" + width + "\n");
        writer.write("height=" + height);
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Get the sfx
   * @return volume
   */
  public double getSfx() {
    return sfx;
  }

  /**
   * Get the music
   * @return volume
   */
  public double getMusic() {
    return music;
  }

  /**
   * Get the window width
   * @return width
   */
  public int getWidth() {
    return width;
  }

  /**
   * Get the window height
   * @return height
   */
  public int getHeight() {
    return height;
  }
}
