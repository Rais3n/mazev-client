package example.domain.game;

import example.domain.Board;
import example.domain.Response;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Movement {

    static Location myPlayerLocation;
    static Location closestGoldLocation;


    public static void setClosestGold(Collection<Response.StateLocations.ItemLocation> itemLocations){
        Location closestGold = new Location(1,1);
        int minDist = 99999;
        int dist;
        for(Response.StateLocations.ItemLocation itemLocation : itemLocations){
            if(itemLocation.entity() instanceof Item.Gold){
                dist = taxicabGeometryDistance(myPlayerLocation,itemLocation.location());
                if(dist<minDist){
                    Location goldLocation = itemLocation.location();
                    minDist = dist;
                    closestGold = goldLocation;
                }
            }
        }

        closestGoldLocation = closestGold;
    }

    private static int taxicabGeometryDistance(Location playerLocation, Location goldLocation){
        int xDistance = Math.abs(playerLocation.column() - goldLocation.column());
        int yDistance = Math.abs(playerLocation.row() - goldLocation.row());

        return xDistance + yDistance;
    }

    public static void setPlayerLocation(Collection<Response.StateLocations.PlayerLocation> playerLocations, Player myPlayer){

        myPlayerLocation = playerLocations.stream()
                .filter(player -> player.entity().equals(myPlayer))
                .findFirst()
                .map(Response.StateLocations.PlayerLocation::location)
                .orElse(null);


//        Location location = null;
//        for(Response.StateLocations.PlayerLocation player1 : playerLocations){
//            if(player1.entity().equals(myPlayer)){
//                location = player1.location();
//            }
//        }
//        myPlayerLocation = location;
    }

    private static List<Location> dijkstra(Board board){
        Location[][] previousVertex = new Location[board.getBoardHeigth()][board.getBoardWidth()];
        previousVertex[myPlayerLocation.row()][myPlayerLocation.column()] = closestGoldLocation;

        List<Location> queue = new LinkedList<>();
        queue.add(myPlayerLocation);

        Location currentLocation = new Location(0,0);
        while(!queue.isEmpty()){
            currentLocation = queue.getFirst();
            queue.removeFirst();
            addNeighbours(board,queue,currentLocation, previousVertex);
            if(currentLocation.equals(closestGoldLocation))
                break;
        }

        List<Location> goldPath = new LinkedList<>();

        while(currentLocation != myPlayerLocation){
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

    public static Direction Direction(Board board){
        List<Location> goldPath = dijkstra(board);
        if(!goldPath.isEmpty()) {
            Location firstMove = new Location(goldPath.getLast().row() - myPlayerLocation.row(), goldPath.getLast().column() - myPlayerLocation.column());
            goldPath.removeLast();
            if (firstMove.column() - myPlayerLocation.column() == 1)
                return Direction.Right;
            if (firstMove.column() - myPlayerLocation.column() == -1)
                return Direction.Left;
            if (firstMove.row() - myPlayerLocation.row() == 1)
                return Direction.Down;
            else
                return Direction.Up;
        }
        else return  Direction.Up;
    }


}
