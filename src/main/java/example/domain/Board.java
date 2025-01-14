package example.domain;

import example.domain.game.Cave;
import example.domain.game.Item;
import example.domain.game.Location;
import example.domain.game.Player;

import java.util.Collection;

public class Board {

    char[][] board;

    public Board(Cave cave){
        board = new char[cave.rows()][cave.columns()];
        clearBoard();
        assignRocks(cave);
    }

    public void clearBoard(){
        for (int i = 0; i < board.length; i++)
            for(int j=0;j < board[0].length;j++) {
                if(board[i][j]!='X')
                    board[i][j]=' ';
            }

    }

    public void printBoard(Collection<Response.StateLocations.PlayerLocation> playerLocations, Collection<Response.StateLocations.ItemLocation> itemLocations){
        clearBoard();
        updateBoard(playerLocations, itemLocations);
        for(int i=0;i< board.length;i++) {
            for (int j = 0; j < board[0].length; j++) {
                System.out.print(board[i][j]);
            }
            System.out.println();
        }
    }

    private void updateBoard(Collection<Response.StateLocations.PlayerLocation> playerLocations, Collection<Response.StateLocations.ItemLocation> itemLocations){
        drawPlayers(playerLocations);
        drawItems(itemLocations);
    }

    private void drawPlayers(Collection<Response.StateLocations.PlayerLocation> playerLocations){
        for (int i = 0; i < board.length; i++){
            for(int j=0;j<board[0].length;j++){
                if(isPlayer(playerLocations,i,j)){
                    board[i][j] = 'P';

                }
            }
        }
    }

    public Location getClosestGold(Collection<Response.StateLocations.PlayerLocation> playerLocations, Collection<Response.StateLocations.ItemLocation> itemLocations,  Player player){

        Location myPlayerlocation = null;
        for(Response.StateLocations.PlayerLocation player1 : playerLocations){
            if(player1.entity().equals(player)){
                myPlayerlocation = player1.location();
                System.out.println(myPlayerlocation + " frs");
            }
        }

        Location closestGold = null;
        int minDist = 99999;
        int dist;
        for(Response.StateLocations.ItemLocation itemLocation : itemLocations){
            if(itemLocation.entity() instanceof Item.Gold){
                dist = taxicabGeometryDistance(myPlayerlocation,itemLocation.location());
                if(dist<minDist){
                    Location goldLocation = itemLocation.location();
                    minDist = dist;
                    closestGold = goldLocation;
                }
            }
        }

        return closestGold;
    }

    private void drawItems(Collection<Response.StateLocations.ItemLocation> itemLocations){
        for (int i = 0; i < board.length; i++){
            for(int j=0;j<board[0].length;j++){
                char item = isItem(itemLocations,i,j);
                if (item != ' ') {
                    board[i][j] = item;
                }
            }
        }
    }

    private void assignRocks(Cave cave){
        for (int i = 0; i < board.length; i++){
            for(int j=0;j<board[0].length;j++){
                if (cave.rock(i, j)) {
                    board[i][j] = 'X';
                }
            }
        }
    }

    private char isItem(Collection<Response.StateLocations.ItemLocation> itemLocations, int coloumn, int row) {
        Location position;
        for(Response.StateLocations.ItemLocation item : itemLocations){
            position = item.location();
            if(position.row() == row && position.column() == coloumn){
                switch (item.entity()){
                    case Item.Gold ignored -> {
                        return 'G';
                    }
                    case Item.Health ignored -> {
                        return 'H';
                    }

                }
            }
        }
        return ' ';
    }
    private boolean isPlayer(Collection<Response.StateLocations.PlayerLocation> playerLocation, int coloumn, int row) {
        Location location;
        for(Response.StateLocations.PlayerLocation player : playerLocation){
            location = player.location();
            if(location.row() == row && location.column() == coloumn){
                return true;
            }
        }
        return false;
    }

    private int taxicabGeometryDistance(Location playerLocation, Location goldLocation){
        int xDistance = Math.abs(playerLocation.column() - goldLocation.column());
        int yDistance = Math.abs(playerLocation.row() - goldLocation.row());

        return xDistance + yDistance;
    }

}
