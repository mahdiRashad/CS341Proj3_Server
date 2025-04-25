import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class PlayerAuthenticator implements Runnable {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Authenticator authenticator;
    private final Consumer<PlayerData> callback;

    public PlayerAuthenticator(Socket socket, Authenticator authenticator, Consumer<PlayerData> callback) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
        this.authenticator = authenticator;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object readObject = in.readObject();
                Message message = (Message) readObject;

                Message response;
                boolean result;

                switch (message.type) {
                    case LOGIN:
                        result = authenticator.validateExistingLogin(message.username, message.password);
                        response = new Message(MessageType.LOGIN_RESULT, message.username, null);
                        response.message = result ? "SUCCESS" : "FAIL";
                        out.writeObject(response);
                        if (result) {
                            callback.accept(new PlayerData(socket, in, out, message.username));
                            return;
                        }
                        break;

                    case CREATE_ACCOUNT:
                        result = authenticator.createNewAccount(message.username, message.password);
                        response = new Message(MessageType.CREATE_ACCOUNT_RESULT, message.username, null);
                        response.message = result ? "SUCCESS" : "FAIL";
                        out.writeObject(response);
                        if (result) {
                            callback.accept(new PlayerData(socket, in, out, message.username));
                            return;
                        }
                        break;

                    default:
                        System.out.println("This message is unknown: " + message.type);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class PlayerData {
        public final Socket socket;
        public final ObjectInputStream in;
        public final ObjectOutputStream out;
        public final String username;

        public PlayerData(Socket socket, ObjectInputStream in, ObjectOutputStream out, String username) {
            this.socket = socket;
            this.in = in;
            this.out = out;
            this.username = username;
        }
    }
}
