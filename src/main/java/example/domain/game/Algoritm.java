package example.domain.game;

import example.domain.Board;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Algoritm {

    public static int taxicabGeometryDistance(Location playerLocation, Location goldLocation){
        int xDistance = Math.abs(playerLocation.column() - goldLocation.column());
        int yDistance = Math.abs(playerLocation.row() - goldLocation.row());

        return xDistance + yDistance;
    }

    public static List<Location> dijkstra(Board board, Location myPlayerLocation, Location closestResourceLocation, boolean avoidPlayers, Collection<Location> opponentsLocationList){
        Location[][] previousVertex = new Location[board.getBoardHeigth()][board.getBoardWidth()];
        previousVertex[myPlayerLocation.row()][myPlayerLocation.column()] = closestResourceLocation;

        List<Location> queue = new LinkedList<>();
        queue.add(myPlayerLocation);

        Location currentLocation = new Location(0,0);
        while(!queue.isEmpty()){
            currentLocation = queue.getFirst();
            queue.removeFirst();
            addNeighbours(board,queue,currentLocation, previousVertex, avoidPlayers, opponentsLocationList);
            if(currentLocation.equals(closestResourceLocation))
                break;
        }

        if(!currentLocation.equals(closestResourceLocation))
            return Collections.emptyList();

        List<Location> ResourcePath = new LinkedList<>();

        while(currentLocation != myPlayerLocation){
            ResourcePath.add(currentLocation);
            currentLocation = previousVertex[currentLocation.row()][currentLocation.column()];
        }

        return ResourcePath;
    }

    public static boolean isSafe(Location location, Collection<Location> opponentsLocationList){
        int safeDistanceFromOpponent = 2;
        return opponentsLocationList.stream()
                .allMatch(opponentLocation -> safeDistanceFromOpponent < Algoritm.taxicabGeometryDistance(opponentLocation, location));
    }

    private static void addNeighbours(Board board,List<Location> queue, Location location, Location[][] previousVertex, boolean avoidPlayers, Collection<Location> opponentsLocationList){
        if(board.getField(location.row() + 1,location.column()) != 'X' && previousVertex[location.row() + 1][location.column()] == null) { //drugi warunek isunivisited
            Location temp = new Location(location.row() + 1, location.column());
            if(!avoidPlayers || isSafe(temp, opponentsLocationList)) {
                queue.add(new Location(location.row() + 1, location.column()));
                previousVertex[location.row() + 1][location.column()] = location;
            }
        }
        if(board.getField(location.row() - 1,location.column()) != 'X' && previousVertex[location.row() - 1][location.column()] == null) {
            Location temp = new Location(location.row() - 1, location.column());
            if(!avoidPlayers || isSafe(temp, opponentsLocationList)) {
                queue.add(new Location(location.row() - 1, location.column()));
                previousVertex[location.row() - 1][location.column()] = location;
            }
        }
        if(board.getField(location.row(),location.column() + 1) != 'X' && previousVertex[location.row()][location.column() + 1] == null) {
            Location temp = new Location(location.row(), location.column() + 1);
            if(!avoidPlayers || isSafe(temp, opponentsLocationList)) {
                queue.add(new Location(location.row(), location.column() + 1));
                previousVertex[location.row()][location.column() + 1] = location;
            }
        }
        if(board.getField(location.row(),location.column() - 1) != 'X' && previousVertex[location.row()][location.column() - 1] == null) {
            Location temp = new Location(location.row(), location.column()-1);
            if(!avoidPlayers || isSafe(temp, opponentsLocationList)) {
                queue.add(new Location(location.row(), location.column() - 1));
                previousVertex[location.row()][location.column() - 1] = location;
            }
        }

    }
}
