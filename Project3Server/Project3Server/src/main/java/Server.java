import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Server implements Runnable {
    //when required automatically creates new threads and reuses old ones when possible, handles many games at once
    private static final ExecutorService threadsPool = Executors.newCachedThreadPool();
    //hold connected clients who are waiting to be paired with another player
    private static final BlockingQueue<PlayerAuthHandler.PlayerSessionData> clientsQueue = new LinkedBlockingQueue<>();

    private final Consumer<Message> callback;

    private final PlayersManger userManager = new PlayersManger();

    public Server(Consumer<Message> callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(5555)) {
            System.out.println("Connect Four Server is running...");

            new Thread(() -> {
                while (true) {
                    try {
                        PlayerAuthHandler.PlayerSessionData player1 = clientsQueue.take();
                        PlayerAuthHandler.PlayerSessionData player2 = clientsQueue.take();
                        threadsPool.execute(new ServerThread(player1, player2, callback, userManager));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // Accept incoming connections and authenticate
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadsPool.execute(new PlayerAuthHandler(clientSocket, userManager, clientsQueue::offer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
