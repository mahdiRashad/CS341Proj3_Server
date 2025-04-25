import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Server implements Runnable {
    //when required automatically creates new threads and reuses old ones when possible, handles many games at once
    private static final ExecutorService threadsPool = Executors.newCachedThreadPool();
    //hold connected clients who are waiting to be paired with another player
    private static final BlockingQueue<PlayerAuthenticator.PlayerData> clientsQueue = new LinkedBlockingQueue<>();

    private final Consumer<Message> callback;

    private final Authenticator userManager = new Authenticator();

    public Server(Consumer<Message> callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(5555)) {
            new Thread(() -> {
                while (true) {
                    try {
                        //add clients to the queue and start the game when there are two clients in the queue
                        PlayerAuthenticator.PlayerData player1 = clientsQueue.take();
                        PlayerAuthenticator.PlayerData player2 = clientsQueue.take();
                        //start the s game session with two players
                        threadsPool.execute(new ServerThread(player1, player2, callback, userManager));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            while(true) {
                Socket clientSocket = serverSocket.accept();
                threadsPool.execute(new PlayerAuthenticator(clientSocket, userManager, clientsQueue::offer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
