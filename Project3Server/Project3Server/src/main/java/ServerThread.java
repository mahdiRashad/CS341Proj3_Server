import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ServerThread implements Runnable {

    //each game thread need two players, GameRules, and a callback to start and run the thread
    private final PlayerAuthHandler.PlayerSessionData player1;
    private final PlayerAuthHandler.PlayerSessionData player2;
    private final GameRules rules = new GameRules();
    private final Consumer<Message> callback;

    private final PlayersManger userManager;

    //thread constructor
    public ServerThread(PlayerAuthHandler.PlayerSessionData p1S, PlayerAuthHandler.PlayerSessionData p2S, Consumer<Message> callback, PlayersManger userManager) {
        this.player1 = p1S;
        this.player2 = p2S;
        this.callback = callback;
        this.userManager = userManager;
    }

    @Override
    public void run() {
        try {
            //send a connect message to tell the server that the two player (recipient 0, red and recipient 1, yellow) are joined
            //two player has been connected message to the server GUI
            Message m1 = new Message(0, true);
            m1.username = player1.username;  // ✅ inject name
            player1.out.writeObject(m1);
            callback.accept(m1);             // for GUI

            Message m2 = new Message(1, true);
            m2.username = player2.username;  // ✅ inject name
            player2.out.writeObject(m2);
            callback.accept(m2);             // for GUI

            //start a listener for player 0
            new Thread(new PlayerThread(player1.in, player1.out, player2.out, 0, rules, callback, player1.username, player2.username, userManager)).start();
            //start a listener for player 1
            new Thread(new PlayerThread(player2.in, player2.out, player1.out, 1, rules, callback, player2.username, player1.username, userManager)).start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
