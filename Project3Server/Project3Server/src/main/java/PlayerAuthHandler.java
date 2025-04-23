import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class PlayerAuthHandler implements Runnable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final PlayersManger userManager;
    private final Consumer<PlayerSessionData> sessionCallback;

    public PlayerAuthHandler(Socket socket, PlayersManger userManager, Consumer<PlayerSessionData> sessionCallback) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();  // 🔥
        this.in = new ObjectInputStream(socket.getInputStream());
        this.userManager = userManager;
        this.sessionCallback = sessionCallback;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object readObject = in.readObject();
                Message message = (Message) readObject;

                if (message.type == MessageType.LOGIN) {
                    boolean success = userManager.validateLogin(message.username, message.password);
                    Message response = new Message(MessageType.LOGIN_RESULT, message.username, null);
                    response.message = success ? "SUCCESS" : "FAIL";
                    out.writeObject(response);
                    if (success) {
                        sessionCallback.accept(new PlayerSessionData(socket, in, out, message.username));
                        break;
                    }
                } else if (message.type == MessageType.CREATE_ACCOUNT) {
                    boolean success = userManager.createAccount(message.username, message.password);
                    Message response = new Message(MessageType.CREATE_ACCOUNT_RESULT, message.username, null);
                    response.message = success ? "SUCCESS" : "FAIL";
                    System.out.println("[SERVER] Sending SIGNUP_RESPONSE: " + response.message); // ✅ Add this line
                    out.writeObject(response);
                    if (success) {
                        sessionCallback.accept(new PlayerSessionData(socket, in, out, message.username));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class PlayerSessionData {
        public final Socket socket;
        public final ObjectInputStream in;
        public final ObjectOutputStream out;
        public final String username;

        public PlayerSessionData(Socket socket, ObjectInputStream in, ObjectOutputStream out, String username) {
            this.socket = socket;
            this.in = in;
            this.out = out;
            this.username = username;
        }
    }
}
