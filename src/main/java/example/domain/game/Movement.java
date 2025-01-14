package example.domain.game;

import example.domain.Board;
import example.domain.Request;
import example.domain.Response;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Movement {


    private static List<Location> dijkstra(Board board, Location startLocation, Location goldLocation){
        Location[][] previousVertex = new Location[board.getHeigth()][board.getWidth()];
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

    private static void addNeighbours(Board board,List<Location> queue, Location location, Location[][] previousVertex){
        if(board.getField(location.row() + 1,location.column()) != 'X' && previousVertex[location.row() + 1][location.column()] == null) { //drugi warunek isunivisited
            queue.add(new Location(location.row() + 1, location.column()));
            previousVertex[location.row() + 1][location.column()] = location;
        }
        if(board.getField(location.row() - 1,location.column()) != 'X' && previousVertex[location.row() - 1][location.column()] == null) {
            queue.add(new Location(location.row() - 1, location.column()));
            previousVertex[location.row() - 1][location.column()] = location;
        }
        if(board.getField(location.row(),location.column() + 1) != 'X' && previousVertex[location.row()][location.column() + 1] == null) {
            queue.add(new Location(location.row(), location.column() + 1));
            previousVertex[location.row()][location.column() + 1] = location;
        }
        if(board.getField(location.row(),location.column() - 1) != 'X' && previousVertex[location.row()][location.column() - 1] == null) {
            queue.add(new Location(location.row(), location.column() - 1));
            previousVertex[location.row()][location.column() - 1] = location;
        }

    }

    public static Direction Direction(Board board,Location myPlayerLocation, Location closestGold){
        List<Location> goldPath = dijkstra(board, myPlayerLocation, closestGold);
        Location firstMove = new Location(goldPath.getFirst().row() - myPlayerLocation.row(), goldPath.getFirst().column() - myPlayerLocation.column());
        if(firstMove.column() - myPlayerLocation.column() == 1)
            return Direction.Right;
        if(firstMove.column() - myPlayerLocation.column() == -1)
            return Direction.Left;
        if(firstMove.row() - myPlayerLocation.row() == 1)
            return Direction.Down;
        else
            return Direction.Up;
    }



}
