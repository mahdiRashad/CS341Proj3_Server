import java.io.*;
import java.util.function.Consumer;

public class PlayerThread implements Runnable {

    //each player has outStream, inStream, int(0 or 1), a GameRules, callback, authenticator, username, and his opponent username
    ObjectOutputStream out;
    ObjectInputStream in;
    ObjectOutputStream opponentOut;
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
        this.opponentOut = out2;
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
                        opponentOut.writeObject(message);
                        out.writeObject(message);
                        callback.accept(message);
                        break;

                    case MOVE:
                        synchronized (rules) {
                            if (playerId != rules.getPlayerNumber()) {
                                Message notYourTurn = new Message(playerId, "It is not your turn");
                                notYourTurn.type = MessageType.INVALID;
                                out.writeObject(notYourTurn);
                                callback.accept(notYourTurn);
                                break;
                            }
                            int column = Integer.parseInt(message.message);
                            if (rules.isMakeMove(playerId, column)) {
                                Message moveMessage = new Message(playerId, playerId + ":" + column);
                                moveMessage.type = MessageType.MOVE;
                                out.writeObject(moveMessage);
                                opponentOut.writeObject(moveMessage);
                                callback.accept(moveMessage);

                                if (rules.isWin(playerId)) {
                                    Message winMessage = new Message(playerId, "WIN");
                                    winMessage.type = MessageType.WIN;
                                    out.writeObject(winMessage);
                                    opponentOut.writeObject(winMessage);
                                    callback.accept(winMessage);
                                    authenticator.recordWin(username);
                                    authenticator.recordLoss(opponentUsername);

                                    sendStats(out, username);
                                    sendStats(opponentOut, opponentUsername);

                                    authenticator.logout(username);
                                    authenticator.logout(opponentUsername);

                                    Message disconnect1 = new Message(0, false);
                                    out.writeObject(disconnect1);
                                    callback.accept(disconnect1);

                                    Message disconnect2 = new Message(1, false);
                                    opponentOut.writeObject(disconnect2);
                                    callback.accept(disconnect2);
                                    return;

                                } else if (rules.isDraw()) {
                                    Message drawMessage = new Message(-1, "DRAW");
                                    drawMessage.type = MessageType.DRAW;
                                    out.writeObject(drawMessage);
                                    opponentOut.writeObject(drawMessage);
                                    callback.accept(drawMessage);

                                    sendStats(out, username);
                                    sendStats(opponentOut, opponentUsername);

                                    authenticator.logout(username);
                                    authenticator.logout(opponentUsername);

                                    Message disconnect1 = new Message(0, false);
                                    out.writeObject(disconnect1);
                                    callback.accept(disconnect1);

                                    Message disconnect2 = new Message(1, false);
                                    opponentOut.writeObject(disconnect2);
                                    callback.accept(disconnect2);
                                    return;
                                }
                                rules.switchPlayer();

                            } else {
                                Message invalidMessage = new Message(playerId, "INVALID");
                                invalidMessage.type = MessageType.INVALID;
                                out.writeObject(invalidMessage);
                                callback.accept(invalidMessage);
                            }
                        }
                        break;

                    case DISCONNECT:
                        Message disconnectt = new Message(playerId, false);
                        opponentOut.writeObject(disconnectt);
                        callback.accept(disconnectt);
                        return;

                    case CLOSED:
                        try {
                            int id2;
                            if(playerId==0){
                                id2=1;
                            }
                            else{
                                id2=0;
                            }

                            authenticator.logout(username);
                            authenticator.logout(opponentUsername);

                            Message disconnect = new Message(id2, false);
                            disconnect.type = MessageType.CLOSED;
                            disconnect.username = this.opponentUsername;

                            opponentOut.writeObject(disconnect);
                            opponentOut.flush();
                            callback.accept(disconnect);

                            Message selfDisconnect = new Message(playerId, false);
                            selfDisconnect.type = MessageType.CLOSED;
                            selfDisconnect.username = this.username;

                            out.writeObject(selfDisconnect);
                            out.flush();
                            callback.accept(selfDisconnect);

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                        return;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendStats(ObjectOutputStream out, String username) throws IOException {
        int[] stats = authenticator.getStats(username);
        Message statsMessage = new Message(playerId, username + " Wins: " + stats[0] + ", Losses: " + stats[1]);
        statsMessage.type = MessageType.TEXT;
        statsMessage.username = username;
        out.writeObject(statsMessage);
        callback.accept(statsMessage);
    }
}
