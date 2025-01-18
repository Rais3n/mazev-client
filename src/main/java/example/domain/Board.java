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

    public char getField(int row, int column){
        return board[row][column];
    }
    public int getBoardHeigth(){
        return board.length;
    }
    public int getBoardWidth(){
        return board[0].length;
    }

    //dynamiczne proxy - udawanie interfejsow
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

    private char isItem(Collection<Response.StateLocations.ItemLocation> itemLocations, int row, int column) {
//        Location position;
//        for(Response.StateLocations.ItemLocation item : itemLocations){
//            position = item.location();
//            if(position.row() == row && position.column() == column){
//                switch (item.entity()){
//                    case Item.Gold ignored -> {
//                        return 'G';
//                    }
//                    case Item.Health ignored -> {
//                        return 'H';
//                    }
//
//                }
//            }
//        }
//        return ' ';

        return itemLocations.stream()
                .filter(item -> item.location().row() == row && item.location().column() == column)
                .map(item -> {
                    switch (item.entity()) {
                        case Item.Gold ignored: return 'G';
                        case Item.Health ignored: return 'H';
                        default: return ' ';
                    }
                })
                .findFirst()
                .orElse(' ');
    }
    private boolean isPlayer(Collection<Response.StateLocations.PlayerLocation> playerLocation, int row, int column) {
//        Location location;
//        for(Response.StateLocations.PlayerLocation player : playerLocation){
//            location = player.location();
//            if(location.row() == row && location.column() == column){
//                return true;
//            }
//        }
//        return false;

        return playerLocation.stream()
                .map(Response.StateLocations.PlayerLocation::location)
                .anyMatch(location -> location.row() == row && location.column() == column);
    }



}
