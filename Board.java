import java.util.ArrayList;

public class Board{

    class Tile{

        private int x;
        private int y;
        private ArrayList<Row> rows;
        private Board board;
        private char status;

        public Tile(int x, int y, Board board){

            this.x = x;
            this.y = y;
            this.board = board;
            status = ' ';
            rows = new ArrayList<>();

        }

        public boolean Open(){ return status == ' ';}
        public void Set(char status){

            if(!Open())
                System.out.println("Can't set tile at " + x + " " + y);

            this.status = status;
            board.incrementTotalScore();
            for(int i = rows.size() - 1; i >= 0; i--){

                Row row = rows.get(i);
                row.updateScore(status);

                //if this move is a winning move, declare the player the winner
                if(row.winner() == status)
                    board.declareWinner(this);

                //else if this row is no longer winnable, make it remove itself from its tiles and the board
                else if(!row.winnable())
                    row.disqualify();
            }

        }

        public String coordinates(){

            return x + ", " + y;

        }

        public String toString(){

            return "[" + status + "]";

        }

        void joinRow(Row row){

            rows.add(row);

        }

        public void leaveRow(Row row){

            rows.remove(row);

        }

        //returns number of rows eliminated if given player claims this tile
        public int rowsCanEliminate(char player){

            int score = 0;

            for(Row row : rows){
                char other = row.winner();
                if(other != ' ')
                    continue;
                if(other != player)
                    score++;
            }

            return score;

        }

    }

    class Row implements Comparable<Row>{

        Board board;
        private Tile[] tiles;
        private int score, absScore, target;

        public Row(Tile[] tiles, Board board){

            this.tiles = tiles;
            this.board = board;
            score = 0; //negative if more X, positive if more O
            absScore = 0; //total number of tiles claimed
            target = tiles.length; //how many tiles of the same type necessary for win condition

            //the tiles in the row will know that they are a member of this row
            for(Tile tile : tiles)
                tile.joinRow(this);

        }

        //whether this tile is still winnable
        //  if Math.abs(score) != absScore, this means that there are tiles of different types
        //  therefore it is not possible to win on this row anymore
        //  EX. a tile with two Xs and one O has a score of -1 and an abs. score of 3
        public boolean winnable(){

            return Math.abs(score) == absScore;

        }

        //if a player has won a row, the magnitude of the score must equal the target
        //  eg. if score is -3 or 3 and target is 3, return true

        //  if the score is negative, X wins
        //  else if score is positive, O wins
        //  if row isn't won at all, return A as the default
        public char winner(){

            //only proceed if conditions are met
            if(Math.abs(score) != target)
                return ' ';

            return score > 0 ? 'O' : 'X';

        }

        public String toString(){

            String s = "";
            for(Tile tile : tiles)
                s += "(" + tile.coordinates() + ") ";

            return s;

        }

        public int compareTo(Row other){

            return this.score - ((Row)other).score;

        }

        //returns the tile with the most possible rows for this player to eliminate
        public Tile tileWithMostDisqualifications(char player){

            Tile most = null;
            int score = 0;
            for(Tile tile : tiles){

                int localScore = tile.rowsCanEliminate(player);
                if(localScore > score){
                    most = tile;
                    score = localScore;
                }

            }
            return most;

        }

        void updateScore(char player){

            absScore++; //increase absolute score by 1
            switch (player){

                case 'O': //O increases score
                    score++;
                    break;
                case 'X': //X decreases score
                    score--;
                    break;

            }

        }

        public void disqualify(){

            for(Tile tile : tiles)
                tile.leaveRow(this);

            board.disqualifyRow(this);
        }

    }

    private int size;
    private int area;
    private int totalScore;
    public char winner;
    public Tile[][] tiles;
    public DoublePriorityQueue<Row> rows;

    public Board(int size){

        this.size = size;
        area = size * size;
        totalScore = 0;
        tiles = new Tile[size][size];   //board of size 3 has 9 tiles
        rows = new DoublePriorityQueue<>();   //board of size 3 has 8 winning combinations
        winner = ' ';

        for(int i = 0; i < size; i ++){

            //make horizontal row; we'll deal with verticals and diagonals in a second
            Tile[] rowTiles = new Tile[size];

            for(int j = 0; j < size; j++){

                Tile tile = new Tile(i, j, this);
                tiles[i][j] = tile;
                rowTiles[j] = tile;

            }

            rows.add(new Row(rowTiles, this));

        }

        //vertical rows
        for(int j = 0; j < size; j++){

            Tile[] columnTiles = new Tile[size];
            for(int i = 0; i < size; i++)
                columnTiles[i] = tiles[i][j];
            rows.add(new Row(columnTiles, this));

        }

        //diagonal rows
        Tile[] diagonal_a = new Tile[size];
        Tile[] diagonal_b = new Tile[size];
        for(int k = 0; k < size; k++){
            int opposite = size - 1 - k;
            diagonal_a[k] = tiles[k][k];
            diagonal_b[k] = tiles[k][opposite];
        }

        rows.add(new Row(diagonal_a, this));
        rows.add(new Row(diagonal_b, this));

    }

    public Tile getTile(int i, int j){

        return tiles[i][j];

    }

    //prints out every winning combination of tiles
    public void printRows(){

        for(Row row : rows)
            System.out.println(row);

    }

    //prints status of board
    public String toString(){

        String s = "";
        for(int i = 0; i < size; i++){

            for(int j = 0; j < size; j++)
                s += (tiles[i][j]).toString();
            s += "\n";

        }

        return s + " (" + rows.size() + " rows)";

    }

    //returns O if O wins
    //returns X if X wins
    //returns A if no winner
    void declareWinner(Tile tile){

        winner = tile.status;
        System.out.println("Player " + winner + " wins");

    }

    void disqualifyRow(Row row){

        rows.remove(row);

    }

    public int getTotalScore(){ return totalScore; }
    public void incrementTotalScore(){ totalScore++; }
    public boolean noMoreTiles(){ return totalScore == area; }
    public boolean winnable(){ return rows.size() > 0; }
    public boolean gameOver(){ return winner != ' ' || noMoreTiles(); } //game over if winner or no more tiles

}