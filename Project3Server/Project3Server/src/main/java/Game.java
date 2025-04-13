import java.io.*;
import java.net.Socket;

public class Game implements Runnable {

    //each game thread need two players and a rules to start and run the thread
    private Socket player1Socket;
    private Socket player2Socket;
    private GameRules rules = new GameRules();

    //thread constructor
    public Game(Socket p1S, Socket p2S) {
        this.player1Socket = p1S;
        this.player2Socket = p2S;
    }

    @Override
    public void run() {
        //two inputs and two outputs because each playerSocket need an out and in
        try (
                BufferedReader in1 = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
                BufferedReader in2 = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));
                PrintWriter out1 = new PrintWriter(player1Socket.getOutputStream(), true);
                PrintWriter out2 = new PrintWriter(player2Socket.getOutputStream(), true)
        ) {
            //send a start message to the client indicating that the match started
            out1.println("START:RED");
            out2.println("START:YELLOW");

            BufferedReader[] ins = {in1, in2};
            PrintWriter[] outs = {out1, out2};

            int currentPlayer = 0;

            while (true) {
                //start reading messages from the clients and take actions accordingly
                String playerInput = ins[currentPlayer].readLine();
                if (playerInput == null || playerInput.startsWith("EXIT")) break;

                if (playerInput.startsWith("MOVE")) {
                    int column = Integer.parseInt(playerInput.split(":")[1]);
                    if (rules.isMakeMove(currentPlayer, column)) {
                        sendDetails("MOVE:" + currentPlayer + ":" + column, outs);

                        if (rules.isWin(currentPlayer)) {
                            sendDetails("WINER:" + currentPlayer, outs);
                            break;
                        } else if (rules.isDraw()) {
                            sendDetails("DRAW", outs);
                            break;
                        }
                        currentPlayer = 1 - currentPlayer;
                    } else {
                        outs[currentPlayer].println("INVALID");
                    }
                }
            }

            //close the sockets when you are done
            player1Socket.close();
            player2Socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //this method send the message to both players
    private void sendDetails(String message, PrintWriter[] outs) {
        for (PrintWriter out : outs) {
            out.println(message);
        }
    }
}
