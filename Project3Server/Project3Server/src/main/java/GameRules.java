

public class GameRules {
    //gameBord may contain 0=empty slot, 1=red/player1, 2=yellow,player2
    private final int[][] gameBoard = new int[6][7];
    private int totalMovesSoFar = 0;
    private int playerNumber =0;

    //return true if it is a valid move
    public boolean isMakeMove(int player, int col) {
        for (int row = 5; row >= 0; row--) {
            if (gameBoard[row][col] == 0) { //empty slot
                gameBoard[row][col] = player + 1; //full the slot accordingly
                totalMovesSoFar++;
                return true;
            }
        }
        return false;
    }

    //scan the entire game board to see if there is a four connect in all directions
    public boolean isWin(int player) {
        int toCheck = player + 1;
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                //dr:0,dc1 horizontal check, dr:1,dc0 vertical check, dr:1,dc1 Diagonal check, dr:0,dc1 Diagonal check
                if (isFourConnected(r, c, 0, 1, toCheck) || isFourConnected(r, c, 1, 0, toCheck) || isFourConnected(r, c, 1, 1, toCheck) || isFourConnected(r, c, 1, -1, toCheck)) {
                    return true;
                }
            }
        }
        return false;
    }

    //the actual function that checks for connected four of the same type
    private boolean isFourConnected(int row, int col, int dir, int dc, int val) {
        for (int i = 0; i < 4; i++) {
            int nr = row + dir * i;
            int nc = col + dc * i;
            if (nr < 0 || nr >= 6 || nc < 0 || nc >= 7 || gameBoard[nr][nc] != val) return false;
        }
        return true;
    }

    //check if the board is all full in this case return tu=rue for draw 6*7=42
    public boolean isDraw() {
        return totalMovesSoFar == 42;
    }

    //return the current player number
    public int getPlayerNumber() {
        return playerNumber;
    }

    //switch player by changing the player number
    public void switchPlayer() {
        playerNumber = 1 - playerNumber;
    }
}
