import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class GuiServer {
	//when required automatically creates new threads and reuses old ones when possible, handles many games at once
	private static final ExecutorService threadsPool = Executors.newCachedThreadPool();
	//hold connected clients who are waiting to be paired with another player
	private static final BlockingQueue<Socket> ClientsQueue = new LinkedBlockingQueue<>();

	public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5555)) {
            System.out.println("Connect Four Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client has connected");

				//add clients to the queue and start the game when there are two clients in the queue
				ClientsQueue.offer(clientSocket);
                if (ClientsQueue.size() >= 2) {  //when the queue has two player or more
					//poll from the queue
                    Socket player1Socket = ClientsQueue.poll();
                    Socket player2Socket = ClientsQueue.poll();

					//start the s game session with two players
					threadsPool.execute(new Game(player1Socket, player2Socket));
                }
            }
        }
		catch (IOException e) {System.out.println("Connect Four Server is not running, exiting...");}
    }
}
