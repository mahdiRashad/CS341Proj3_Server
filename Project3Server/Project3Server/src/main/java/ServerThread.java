import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ServerThread implements Runnable {

    //each game thread need two players to start and run the thread
    private Socket player1Socket;
    private Socket player2Socket;
    private GameRules logic = new GameRules();
    private final Consumer<Message> callback;
    //thread constructor
    public ServerThread(Socket p1S, Socket p2S, Consumer<Message> callback) {
        this.player1Socket = p1S;
        this.player2Socket = p2S;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            //two inputs and two outputs because each playerSocket need an out and in
            ObjectOutputStream out1 = new ObjectOutputStream(player1Socket.getOutputStream());
            ObjectInputStream in1 = new ObjectInputStream(player1Socket.getInputStream());
            ObjectOutputStream out2 = new ObjectOutputStream(player2Socket.getOutputStream());
            ObjectInputStream in2 = new ObjectInputStream(player2Socket.getInputStream());

            //send a connect message to tell the server that the two player (recipient 0, red and recipient 1, yellow) are joined
            out1.writeObject(new Message(0, true));
            out2.writeObject(new Message(1, true));

            callback.accept(new Message(0, true));
            callback.accept(new Message(1, true));

            //start a listener for player 0
            new Thread(new PlayerThread(in1, out1, out2, 0, logic, callback)).start();
            //start a listener for player 1
            new Thread(new PlayerThread(in2, out2, out1, 1, logic, callback)).start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
