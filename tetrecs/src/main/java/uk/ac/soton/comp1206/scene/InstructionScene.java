package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class InstructionScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(InstructionScene.class);


  /**
   * GridPane for the pieces to be added dynamically
   */
  private final GridPane gridPane = new GridPane();

  /**
   * Create a new instruction scene
   *
   * @param gameWindow the game window
   */
  public InstructionScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Showing instructions");
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

    var instructions = new StackPane();
    instructions.setMaxWidth(gameWindow.getWidth());
    instructions.setMaxHeight(gameWindow.getHeight());
    instructions.getStyleClass().add("instructions-background");
    root.getChildren().add(instructions);
    var mainVBox = new VBox();
    mainVBox.setPadding(new Insets(10));
    mainVBox.setAlignment(Pos.CENTER);
    mainVBox.setSpacing(10);
    //Title
    var title = new Text("Instructions");
    title.getStyleClass().add("heading");

    //Game description
    var description = new Text("TetrECS is a fast-paced gravity-free block placement game, "
        + "where you must survive by clearing rows through careful placement of the upcoming "
        + "blocks before the time runs out. Lose all 3 lives and you're destroyed!");
    //Adding to text flow
    var instructionFlow = new TextFlow(description);
    description.getStyleClass().add("instructions");
    description.setTextAlignment(TextAlignment.CENTER);
    instructionFlow.setTextAlignment(TextAlignment.CENTER);
    description.getStyleClass().add("textD");
    description.setTextAlignment(TextAlignment.CENTER);
    instructionFlow.setTextAlignment(TextAlignment.CENTER);

    //Game piece title
    var title2 = new Text("Game Pieces");
    title2.getStyleClass().add("heading");

    //Setup gridpane
    gridPane.setHgap(5);
    gridPane.setVgap(5);
    gridPane.setAlignment(Pos.CENTER);
    //Piece identification
    int x = 0;
    //Loop through grid pane
    for(int i = 0; i < 3; i++){
      for(int j = 0; j < 5; j++){
        var p = GamePiece.createPiece(x);
        var board = new PieceBoard(3, 3, gameWindow.getWidth()/16, gameWindow.getWidth()/16, "" + i*j);
        board.setPiece(p);
        gridPane.add(board, j, i);
        x++;
      }
    }

    //Instructions image
    var instr = new Image(
        Multimedia.class.getResource("/images/Instructions.png").toExternalForm());
    //Using ImageView to display the instructions
    var viewInst = new ImageView(instr);
    viewInst.setFitWidth(gameWindow.getWidth() * 0.60);
    viewInst.setPreserveRatio(true);

    //Add all nodes to mainVBox
    mainVBox.getChildren().addAll(title, instructionFlow, viewInst, title2, gridPane);
    mainVBox.setMaxHeight(gameWindow.getHeight());
    instructions.getChildren().add(mainVBox);

  }
}
