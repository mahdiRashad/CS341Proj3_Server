import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiServer extends Application {

    private ListView<String> listMessages;
    private ListView<String> listUsers;
    private Server server; // Custom class that wraps the server logic

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        listMessages = new ListView<>();
        listUsers = new ListView<>();

        server = new Server((Message msg) -> {
            Platform.runLater(() -> {
                switch (msg.type) {
                    case TEXT:
                        listMessages.getItems().add("[Chat] Player " + msg.recipient + ": " + msg.message);
                        break;

                    case NEWUSER:
                        listUsers.getItems().add("Player " + msg.recipient);
                        listMessages.getItems().add("[Join] Player " + msg.recipient + " has joined the game.");
                        break;

                    case DISCONNECT:
                        listUsers.getItems().remove("Player " + msg.recipient);
                        listMessages.getItems().add("[Disconnect] Player " + msg.recipient + " has left the game.");
                        break;

                    case MOVE:
                        String[] parts = msg.message.split(":");
                        if (parts.length == 2) {
                            int player = Integer.parseInt(parts[0]);
                            int column = Integer.parseInt(parts[1]);
                            listMessages.getItems().add("[Move] Player " + player + " placed a token in column " + column);
                        } else {
                            listMessages.getItems().add("[Move] Received malformed move message: " + msg.message);
                        }
                        break;

                    case WIN:
                        listMessages.getItems().add("[Game Over] Player " + msg.recipient + " has won the game!");
                        break;

                    case DRAW:
                        listMessages.getItems().add("[Game Over] The game ended in a draw.");
                        break;

                    case INVALID:
                        listMessages.getItems().add("[Invalid] Player " + msg.recipient + ": " + msg.message);
                        break;

                    default:
                        listMessages.getItems().add("[Unknown] Message type: " + msg.type + " from Player " + msg.recipient);
                        break;
                }

            });
        });

        HBox hbox = new HBox(10, listUsers, listMessages);
        BorderPane root = new BorderPane(hbox);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f0f0; -fx-font-family: 'serif';");

        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Connect Four Server GUI");
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });

        new Thread(server).start(); // Run the server in background
    }
}
