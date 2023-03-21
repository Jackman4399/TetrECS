package uk.ac.soton.comp1206.scene;

import static javafx.scene.layout.HBox.setHgrow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class LobbyScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(LobbyScene.class);

  /**
   * The communicator
   */
  private final Communicator communicator;

  /**
   * The timer for loading the channels
   */
  private final Timer loadTimer = new Timer();

  /**
   * The task that is assigned later to be done by the timer
   */
  private TimerTask load;

  /**
   * A VBox to hold all the channels
   */
  private VBox listing = new VBox();

  /**
   * A list of all the channel names
   */
  private ArrayList<String> channelNames = new ArrayList<>();

  /**
   * Boolean to state if the user has joined a channel or not
   */
  private boolean joined = false;

  /**
   * The joined channel
   */
  private SimpleStringProperty joinedChannel = new SimpleStringProperty();

  /**
   * The main border pane
   */
  private BorderPane borderPane;

  /**
   * The messages holder UI
   */
  private TextFlow messages;

  /**
   * Name of the player
   */
  private SimpleStringProperty nickname = new SimpleStringProperty("");

  /**
   * To scroll or not to scroll, boolean
   */
  private static boolean scrollToBottom = false;

  /**
   * Scroll pane to hold the text flow
   */
  private ScrollPane scroller = new ScrollPane();

  /**
   * To hold all the players in the game
   */
  private ArrayList<String> players = new ArrayList<>();

  /**
   * To see who is the host
   */
  private SimpleBooleanProperty host = new SimpleBooleanProperty(false);

  /**
   * The timer for th users list
   */
  private Timer usersTimer;

  /**
   * A text flow to hold all the users in game
   */
  private TextFlow userBox;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    communicator = gameWindow.getCommunicator();
  }

  /**
   * Initialise the window
   */
  @Override
  public void initialise() {
    Multimedia.stopLM();
    Multimedia.playMusic("music/menu.mp3");
    load = new TimerTask() {
      @Override
      public void run() {
        communicator.send("LIST");
      }
    };
    //Schedule the timer
    loadTimer.schedule(load, 0, 1000);
    scene.setOnKeyPressed((event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        //Go back to menu
        Multimedia.stopM();
        //Cancel every running timer
        loadTimer.cancel();
        loadTimer.purge();
        if(usersTimer != null){
          usersTimer.cancel();
          usersTimer.purge();
        }
        logger.info("Back to menu");
        gameWindow.startMenu();
      }
    }));
    communicator.addListener(e -> Platform.runLater(() -> receive(e.trim())));
  }

  /**
   * Build the UI
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //Main stack pane
    var mainPane = new StackPane();
    mainPane.setMaxWidth(gameWindow.getWidth());
    mainPane.setMaxHeight(gameWindow.getHeight());
    mainPane.getStyleClass().add("lobby-background");
    root.getChildren().add(mainPane);

    //Border Pane on the stack pane
    borderPane = new BorderPane();
    mainPane.getChildren().add(borderPane);

    //A title at the top of the page
    var title = new Text("Multiplayer");
    title.getStyleClass().add("title");
    title.setTextAlignment(TextAlignment.CENTER);
    var titleBox = new HBox(title);
    titleBox.setAlignment(Pos.CENTER);
    borderPane.setTop(titleBox);

    //A button to create a new channel
    var createChannel = new Button("Create new channel");
    createChannel.getStyleClass().add("button");

    //Set properties for the VBox
    listing.setSpacing(5);
    listing.setPadding(new Insets(10));
    listing.setAlignment(Pos.CENTER);

    //Holds the UI elements on the left side of the borderPane
    var vbox = new VBox();
    vbox.setAlignment(Pos.TOP_CENTER);
    vbox.getChildren().addAll(createChannel, listing);
    vbox.setPadding(new Insets(20));
    vbox.setSpacing(20);

    //Add the function of the create channel button
    createChannel.setOnAction((event -> {
      vbox.getChildren().remove(createChannel);
      //A text field prompt for the user to type
      var prompt = new TextField();
      prompt.setPromptText("Enter name");
      prompt.requestFocus();
      prompt.setMaxWidth(gameWindow.getWidth() / 4);
      //Add it to a specific position
      vbox.getChildren().add(1, prompt);
      //Button to submit name
      var submit = new Button("Submit");
      submit.setDefaultButton(true);
      //Add it to a specific position
      vbox.getChildren().add(2, submit);
      //Action to submit name
      submit.setOnAction(event1 -> {
        var channelName = prompt.getText();
        boolean duplicate = false;
        //If there are 0 channels
        if (channelNames.size() != 0) {
          for (String channel : channelNames) {
            logger.info("Channels are :" + channel);
            //Checking for an already existing channel with the same name
            if (channelName.equals(channel)) {
              logger.info("There is a duplicate");
              prompt.clear();
              //Show a message to the user
              prompt.setPromptText("The name is taken");
              duplicate = true;
              break;
            }
          }
        }
        //If there is no duplicate and the name isn't empty
        if (!duplicate && !channelName.equals("")) {
          createChannel(channelName);
          logger.info("Channel successfully created");
          //Remove the two
          vbox.getChildren().removeAll(prompt, submit);
          vbox.getChildren().add(0, createChannel);
        }
      });
    }));

    borderPane.setLeft(vbox);
  }

  /**
   * A method to handle all incoming messages from the communicator
   *
   * @param message the message from the communicator
   */
  private void receive(String message) {
    //Print out the message
    logger.info("Received: " + message);
    if (message.startsWith("CHANNELS")) {
      logger.info("Received Channels");
      //Clears all children
      listing.getChildren().clear();
      channelNames.clear();
      //formats the message
      if (message.split(" ").length == 2) {
        var channels = message.split(" ", 2)[1].split("\n");
        //Add all the channels to the ArrayList
        for (String channel : channels) {
          channelNames.add(channel);
        }
        //Sort the String array list in natural order (ascending A-Z)
        channelNames.sort(Comparator.naturalOrder());
        logger.info("Channels sorted");
        for (String text : channelNames) {
          //Add the channels to nodes and UI
          var channelNode = new Text(text);
          //Click a channel to join
          channelNode.setOnMouseClicked(e -> joinChannel(channelNode.getText()));
          channelNode.getStyleClass().add("channelItem");
          listing.getChildren().add(channelNode);
        }
      } else {
        //If there are no channels
        listing.getChildren().clear();
        var standard = new Text("No channels found");
        standard.getStyleClass().add("channelItem");
        listing.getChildren().add(standard);
      }
    } else if (message.startsWith("JOIN")) {
      //JOIN A CHANNEL
      joinedChannel.set(message.split(" ")[1]);
      borderPane.setCenter(textChannel());
      joined = true;
    } else if (message.startsWith("MSG")) {
      //Received a chat message
      if (!joined) {
        return;
      }
      //Create a time stamp
      var now = LocalDateTime.now();
      var timestamp = now.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));
      // RECEIVE A MESSAGE
      if (messages != null) {
        String msg = message.split(" ", 2)[1];
        if (msg.contains("/name")) {
          //If it's a renaming command
          var nickname = msg.split(":")[1].replace("/name", "").trim();
          communicator.send("NICK " + nickname);
        } else if (!msg.split(":")[1].equals("")) {
          //If it's not a renaming command
          messages.getChildren().add(new Text(" <" + timestamp + "> " + msg + "\n"));
          Multimedia.playAudioEffect("sounds/pling.wav");
        }
      }
    } else if (message.startsWith("NICK")) {
      //CHANGE NICKNAME
      nickname.set(message.split(" ")[1]);
    } else if (message.startsWith("HOST")) {
      //SET USER AS HOST
      logger.info("You are the host");
      host.set(true);
    } else if (message.startsWith("USERS")) {
      //Request users in the channel
      var users = message.split(" ")[1].split("\n");
      //Clear the array
      players.clear();
      logger.info("Adding Players");
      for (String user : users) {
        players.add(user);
      }
      //Clear the UI, replace with refreshed list
      userBox.getChildren().removeAll(userBox.getChildren());
      for (String user1 : players) {
        var player = new Text(user1 + " ");
        userBox.getChildren().add(player);
      }
    } else if (message.startsWith("PARTED")) {
      //If the player has left the channel, remove chat UI in the center
      joinedChannel.set(null);
      logger.info("Left channel");
      borderPane.setCenter(null);
      host.set(false);
      //Clear everything
      players.clear();
      userBox.getChildren().removeAll(userBox.getChildren());
      joined = false;
    } else if (message.startsWith("ERROR")) {
      var error = message.split(" ", 2);
      logger.error(error[1]);
      var alert = new Alert(AlertType.ERROR, error[1]);
      alert.showAndWait();
    } else if (message.startsWith("START")) {
      //Start the game
      logger.info("Starting online game...");
      //Cancel repeating timers
      loadTimer.cancel();
      loadTimer.purge();
      usersTimer.cancel();
      usersTimer.purge();
      gameWindow.startMultiPlayer(nickname.get());
    }
    //To scroll the chat pane
    if (scroller.getVvalue() == 0.0f || scroller.getVvalue() > 0.9f) {
      scrollToBottom = true;
    }
  }

  /**
   * Creates a new channel by sending a message using the communicator
   *
   * @param name the name of the new channel
   */
  private void createChannel(String name) {
    logger.info("Creating new channel {}", name);
    communicator.send("CREATE " + name);
  }

  /**
   * Joins an existing channel by sending a message using the communicator
   *
   * @param name the name of the channel
   */
  private void joinChannel(String name) {
    //If the user is not in a channel already
    if (!joined) {
      logger.info("Joining channel {}...", name);
      communicator.send("JOIN " + name);
      joined = true;
    }
  }

  /**
   * Show a new UI component for joining channels
   *
   * @return the border pane of UI elements to add to the main pane
   */
  private BorderPane textChannel() {
    //Main pane
    var main = new BorderPane();
    main.setPadding(new Insets(10));
    main.setMaxWidth(gameWindow.getWidth() * 0.75);
    main.setMaxHeight(gameWindow.getHeight() * 0.75);

    //Create a new timer for requesting users inside the channel
    usersTimer = new Timer();

    //A text flow to hold the users inside the channel
    userBox = new TextFlow();
    userBox.setPadding(new Insets(3));
    userBox.getStyleClass().add("playerBox");

    //The task of the timer
    var usersTimerTask = new TimerTask() {
      @Override
      public void run() {
        if (joined) {
          communicator.send("USERS");
        }
      }
    };
    //Repeat every 1 second
    usersTimer.schedule(usersTimerTask, 0, 1000);

    //A VBox to hold the UI elements at the top of the window
    var topBox = new VBox();
    topBox.setSpacing(5);
    //The name of the channel bound to the text node
    var channelName = new Text();
    channelName.getStyleClass().add("heading");
    channelName.textProperty().bind(joinedChannel);
    //Add to VBox
    topBox.getChildren().addAll(channelName, userBox);
    main.setTop(topBox);

    //Create a horizontal bar with a text box and send button
    var txtfield = new TextField();
    txtfield.setPromptText("Send /name <nickname> to add a nickname");
    //Action events
    txtfield.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() != KeyCode.ENTER) {
        return;
      }
      if (txtfield.getText() != null) {
        communicator.send("MSG " + txtfield.getText());
        txtfield.clear();
      }
    });
    //Bar to add both the text field and the send button
    var bottomBar = new VBox();
    bottomBar.getChildren().add(txtfield);
    bottomBar.setPrefWidth(main.getWidth());
    //.setHgrow(txtfield, Priority.ALWAYS);
    var box = new BorderPane();
    //A button the leave the channel
    var leave = new Button("Leave");
    leave.setAlignment(Pos.BASELINE_RIGHT);
    leave.getStyleClass().add("sendButton");
    leave.setOnAction(event -> {
      if (joined) {
        usersTimer.cancel();
        usersTimer.purge();
        communicator.send("PART");
      }
    });
    box.setRight(leave);
    //A button to start the game if the user is a host
    var start = new Button("Start");
    start.getStyleClass().add("sendButton");
    start.setAlignment(Pos.BASELINE_LEFT);
    //Set it as not visible first
    start.setVisible(false);
    box.setLeft(start);

    //Change the visibility of the button depending on the boolean value
    host.addListener(((observable, oldValue, newValue) -> {
      if (newValue == true) {
        start.setVisible(true);
      } else if (newValue == false) {
        start.setVisible(false);
      }
    }));

    bottomBar.getChildren().add(box);

    //Starts the game if the user is the host
    start.setOnAction(event -> {
      //Stop music and timers
      Multimedia.stopLM();
      joined = false;
      communicator.send("START");
    });

    //Add to main border pane
    main.setBottom(bottomBar);
    //Text flow for text messages
    messages = new TextFlow();
    messages.getStyleClass().add("messages");

    //Scrollpane to allow scrolling messages
    scroller.setFitToWidth(true);
    scroller.setFitToHeight(true);
    scroller.getStyleClass().add("scroller");
    scroller.setFitToWidth(true);

    scroller.setContent(messages);
    scene.addPostLayoutPulseListener(this::jumpToBottom);
    main.setCenter(scroller);

    return main;
  }

  /**
   * Jump to the bottom if the messages exceed size
   */
  private void jumpToBottom() {
    if (!scrollToBottom) {
      return;
    }
    scroller.setVvalue(1.0f);
    scrollToBottom = false;
  }
}
