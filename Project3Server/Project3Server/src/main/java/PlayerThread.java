import java.io.*;
import java.util.function.Consumer;

public class PlayerThread implements Runnable {

    //each player has outStream, inStream, int(0 or 1), a GameRules, callback, and a username
    ObjectOutputStream out;
    ObjectInputStream in;
    ObjectOutputStream otherPlayerOut;
    int playerId;
    private final GameRules rules;
    private final Consumer<Message> callback;
    private final String username;
    private final Authenticator authenticator;
    private final String opponentUsername;

    //thread constructor
    public PlayerThread(ObjectInputStream in1, ObjectOutputStream out1, ObjectOutputStream out2, int playerId, GameRules rules, Consumer<Message> callback, String username, String opponentUsername, Authenticator authenticator) {
        this.in = in1;
        this.out = out1;
        this.otherPlayerOut = out2;
        this.playerId = playerId;
        this.rules = rules;
        this.callback = callback;
        this.username = username;
        this.authenticator = authenticator;
        this.opponentUsername = opponentUsername;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object readObject = in.readObject();
                Message message = (Message) readObject;

                switch (message.type) {
                    case TEXT:
                        message.username = this.username;
                        otherPlayerOut.writeObject(message);
                        out.writeObject(message);
                        callback.accept(message);
                        break;

                    case MOVE:
                        synchronized (rules) {
                            if (playerId != rules.getPlayerNumber()) {
                                Message notTurn = new Message(playerId, "Not your turn");
                                notTurn.type = MessageType.INVALID;
                                out.writeObject(notTurn);
                                callback.accept(notTurn);
                                break;
                            }
                            int column = Integer.parseInt(message.message);
                            if (rules.isMakeMove(playerId, column)) {
                                Message moveMsg = new Message(playerId, playerId + ":" + column);
                                moveMsg.type = MessageType.MOVE;
                                out.writeObject(moveMsg);
                                otherPlayerOut.writeObject(moveMsg);
                                callback.accept(moveMsg);

                                if (rules.isWin(playerId)) {
                                    Message win = new Message(playerId, "WIN");
                                    win.type = MessageType.WIN;
                                    out.writeObject(win);
                                    otherPlayerOut.writeObject(win);
                                    callback.accept(win);
                                    authenticator.recordWin(username);
                                    authenticator.recordLoss(opponentUsername);

                                    sendStats(out, username);
                                    sendStats(otherPlayerOut, opponentUsername);

                                    authenticator.logout(username);
                                    authenticator.logout(opponentUsername);

                                    Message disconnect1 = new Message(0, false);
                                    out.writeObject(disconnect1);
                                    callback.accept(disconnect1);

                                    Message disconnect2 = new Message(1, false);
                                    otherPlayerOut.writeObject(disconnect2);
                                    callback.accept(disconnect2);
                                    return;
                                } else if (rules.isDraw()) {
                                    Message draw = new Message(-1, "DRAW");
                                    draw.type = MessageType.DRAW;
                                    out.writeObject(draw);
                                    otherPlayerOut.writeObject(draw);
                                    callback.accept(draw);

                                    sendStats(out, username);
                                    sendStats(otherPlayerOut, opponentUsername);

                                    authenticator.logout(username);
                                    authenticator.logout(opponentUsername);

                                    Message disconnect1 = new Message(0, false);
                                    out.writeObject(disconnect1);
                                    callback.accept(disconnect1);

                                    Message disconnect2 = new Message(1, false);
                                    otherPlayerOut.writeObject(disconnect2);
                                    callback.accept(disconnect2);
                                    return;
                                }
                                System.out.println("[SERVER] Received MOVE from player " + playerId + ": column " + message.message);

                                rules.switchPlayer();
                            } else {
                                Message invalid = new Message(playerId, "INVALID");
                                invalid.type = MessageType.INVALID;
                                out.writeObject(invalid);
                                callback.accept(invalid);
                            }
                        }
                        break;

                    case DISCONNECT:
                        Message disconnect = new Message(playerId, false);
                        otherPlayerOut.writeObject(disconnect);
                        callback.accept(disconnect);
                        return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendStats(ObjectOutputStream out, String username) throws IOException {
        int[] stats = authenticator.getStats(username);
        Message statMsg = new Message(playerId, username + " stats - Wins: " + stats[0] + ", Losses: " + stats[1]);
        statMsg.type = MessageType.TEXT;
        statMsg.username = username;
        out.writeObject(statMsg);
        callback.accept(statMsg);
    }
}
