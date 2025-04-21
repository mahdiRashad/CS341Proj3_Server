import java.io.*;
import java.util.function.Consumer;

public class PlayerThread implements Runnable {

    ObjectOutputStream out;
    ObjectInputStream in;
    ObjectOutputStream otherOut;
    int playerId;
    private GameRules rules;
    private Consumer<Message> callback;

    public PlayerThread(ObjectInputStream in1, ObjectOutputStream out1, ObjectOutputStream out2, int playerId, GameRules rules, Consumer<Message> callback) {
        this.in = in1;
        this.out = out1;
        this.otherOut = out2;
        this.playerId = playerId;
        this.rules = rules;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (!(obj instanceof Message)) continue;
                Message msg = (Message) obj;

                switch (msg.type) {
                    case TEXT:
                        otherOut.writeObject(msg);
                        callback.accept(msg);
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

                            int col = Integer.parseInt(msg.message);
                            if (rules.isMakeMove(playerId, col)) {
                                Message moveMsg = new Message(playerId, playerId + ":" + col);
                                moveMsg.type = MessageType.MOVE;
                                out.writeObject(moveMsg);
                                otherOut.writeObject(moveMsg);
                                callback.accept(moveMsg);

                                if (rules.isWin(playerId)) {
                                    Message win = new Message(playerId, "WIN");
                                    win.type = MessageType.WIN;
                                    out.writeObject(win);
                                    otherOut.writeObject(win);
                                    callback.accept(win);
                                    return;
                                } else if (rules.isDraw()) {
                                    Message draw = new Message(-1, "DRAW");
                                    draw.type = MessageType.DRAW;
                                    out.writeObject(draw);
                                    otherOut.writeObject(draw);
                                    callback.accept(draw);
                                    return;
                                }

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
                        Message disc = new Message(playerId, false);
                        otherOut.writeObject(disc);
                        callback.accept(disc);
                        return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
