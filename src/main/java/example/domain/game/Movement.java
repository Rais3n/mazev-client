package example.domain.game;

import example.domain.Request;
import example.domain.Response;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Movement {

    private Location getMyLocation(Collection<Response.StateLocations.PlayerLocation> playerLocations, Player player){

        Location location = null;
        for(Response.StateLocations.PlayerLocation player1 : playerLocations){
            if(player1.entity().equals(player)){
                location = player1.location();
                System.out.println(location + " frs");
            }
        }
        return location;
    }


    private List<Location> dijkstra(char[][] board , Location startLocation, Location goldLocation){
        Location[][] previousVertex = new Location[board.length][board[0].length];
        previousVertex[startLocation.row()][startLocation.column()] = startLocation;

        List<Location> queue = new LinkedList<>();
        queue.add(startLocation);

        Location currentLocation = new Location(0,0);
        while(!queue.isEmpty()){
            currentLocation = queue.get(0);
            queue.remove(0);
            addNeighbours(board,queue,currentLocation, previousVertex);
            if(currentLocation == goldLocation)
                break;
        }

        List<Location> goldPath = new LinkedList<>();

        while(currentLocation != startLocation){
            goldPath.add(currentLocation);
            currentLocation = previousVertex[currentLocation.row()][currentLocation.column()];
        }

        return goldPath;
    }
    private void addNeighbours(char[][] board,List<Location> queue, Location location, Location[][] previousVertex){
        if(board[location.row() + 1][location.column()] != 'X' && previousVertex[location.row() + 1][location.column()].equals(new Location(0,0))) {
            queue.add(new Location(location.row() + 1, location.column()));
            previousVertex[location.row() + 1][location.column()] = location;
        }
        if(board[location.row() - 1][location.column()] != 'X' && previousVertex[location.row() - 1][location.column()].equals(new Location(0,0))) {
            queue.add(new Location(location.row() - 1, location.column()));
            previousVertex[location.row() - 1][location.column()] = location;
        }
        if(board[location.row()][location.column() + 1] != 'X' && previousVertex[location.row()][location.column() + 1].equals(new Location(0,0))) {
            queue.add(new Location(location.row(), location.column() + 1));
            previousVertex[location.row()][location.column() + 1] = location;
        }
        if(board[location.row()][location.column() - 1] != 'X' && previousVertex[location.row()][location.column() - 1].equals(new Location(0,0))) {
            queue.add(new Location(location.row(), location.column() - 1));
            previousVertex[location.row()][location.column() - 1] = location;
        }

    }

    private Request Direction(char[][] board , Location myPlayerLocation, Location goldLocation){

        dijkstra(board,myPlayerLocation,goldLocation);


        return new Request.Command(Direction.Up);
    }



}
