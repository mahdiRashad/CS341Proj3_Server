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

    private ListView<String> messages;
    private ListView<String> users;
    private Server server;
    String currentPlayer;
    String player1;
    String player2;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        messages = new ListView<>();
        users = new ListView<>();

        server = new Server((Message msg) -> {
            Platform.runLater(() -> {
                switch (msg.type) {
                    case TEXT:
                        if(msg.recipient==1){
                            currentPlayer=player1;
                        }
                        else{
                            currentPlayer=player2;
                        }
                        messages.getItems().add("[Chatting] Player " + currentPlayer + " send a message: " + msg.message);
                        break;

                    case NEW_USER:
                        if(msg.recipient==1){
                            player1=msg.username;
                            users.getItems().add("Player " + player1);
                            messages.getItems().add("[Joining] Player " + player1 + " has joined the game.");
                            break;
                        }
                        else{
                            player2=msg.username;
                            users.getItems().add("Player " + player2);
                            messages.getItems().add("[Joining] Player " + player2 + " has joined the game.");
                            break;
                        }

                    case DISCONNECT:
                        if(msg.recipient==1){
                            currentPlayer=player1;
                        }
                        else{
                            currentPlayer=player2;
                        }
                        users.getItems().remove("Player " + currentPlayer);
                        messages.getItems().add("[Disconnected] Player " + currentPlayer + " has left the game.");
                        break;

                    case MOVE:
                        if(msg.recipient==1){
                            currentPlayer=player1;
                        }
                        else{
                            currentPlayer=player2;
                        }
                        String[] parts = msg.message.split(":");
                        if (parts.length == 2) {
                            int column = Integer.parseInt(parts[1]);
                            messages.getItems().add("[Move] Player " + currentPlayer + " placed a token in column " + column);
                        } else {
                            messages.getItems().add("[Move] Received malformed move message: " + msg.message);
                        }
                        break;

                    case WIN:
                        if(msg.recipient==1){
                            currentPlayer=player1;
                        }
                        else{
                            currentPlayer=player2;
                        }
                        messages.getItems().add("[Game Over] Player " + currentPlayer + " has won the game!");
                        break;

                    case DRAW:
                        if(msg.recipient==1){
                            currentPlayer=player1;
                        }
                        else{
                            currentPlayer=player2;
                        }
                        messages.getItems().add("[Game Over] The game ended in a draw.");
                        break;

                    case INVALID:
                        if(msg.recipient==1){
                            currentPlayer=player1;
                        }
                        else{
                            currentPlayer=player2;
                        }
                        messages.getItems().add("[Invalid] Player " + currentPlayer + ": " + msg.message);
                        break;

                    default:
                        if(msg.recipient==1){
                            currentPlayer=player1;
                        }
                        else{
                            currentPlayer=player2;
                        }
                        messages.getItems().add("[Unknown] Message type: " + msg.type + " from Player " + currentPlayer);
                        break;
                }

            });
        });

        HBox hbox = new HBox(10, users, messages);
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

        new Thread(server).start();
    }
}
