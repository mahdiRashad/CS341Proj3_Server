import java.io.*;
import java.util.function.Consumer;

public class PlayerThread implements Runnable {

    //each player has outStream, inStream, int(0 or 1), a GameRules, and callback
    ObjectOutputStream out;
    ObjectInputStream in;
    ObjectOutputStream otherPlayerOut;
    int playerId;
    private final GameRules rules;
    private final Consumer<Message> callback;

    private final String username;
    private final String opponentUsername;
    private final PlayersManger userManager;

    //thread constructor
    public PlayerThread(ObjectInputStream in1, ObjectOutputStream out1, ObjectOutputStream out2, int playerId, GameRules rules, Consumer<Message> callback, String username, String opponentUsername, PlayersManger userManager) {
        this.in = in1;
        this.out = out1;
        this.otherPlayerOut = out2;
        this.playerId = playerId;
        this.rules = rules;
        this.callback = callback;

        this.username = username;
        this.opponentUsername = opponentUsername;
        this.userManager = userManager;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object readObject = in.readObject();
                if (!(readObject instanceof Message)){
                    continue;
                }
                Message message = (Message) readObject;

                switch (message.type) {
                    case TEXT:
                        message.username = this.username;      // ✅ ensure username is filled
                        otherPlayerOut.writeObject(message);
                        out.writeObject(message);
                        callback.accept(message);
                        break;

                    case MOVE:
                        synchronized (rules) {
                            if (playerId != rules.getPlayerNumber()) {
                                Message notTurn = new Message(playerId, "Not your turn");
                                System.out.println("[SERVER] Not your turn: " + playerId);
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
                                    return;
                                } else if (rules.isDraw()) {
                                    Message draw = new Message(-1, "DRAW");
                                    draw.type = MessageType.DRAW;
                                    out.writeObject(draw);
                                    otherPlayerOut.writeObject(draw);
                                    callback.accept(draw);
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
}
