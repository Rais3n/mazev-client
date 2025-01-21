package example.domain.game;

import example.domain.Board;
import example.domain.Response;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Movement {

    static Location myPlayerLocation;
    static Location closestGoldLocation;

    public static Location getMyPlayerLocation() {
        return myPlayerLocation;
    }

    public static void setGoldToPath(Collection<Response.StateLocations.ItemLocation> itemLocations, Collection<Response.StateLocations.PlayerLocation> playerLocations, Player myPlayer){
        List<Location> skipGoldLocation = new LinkedList<>();
        int myDistance = 0;
        int opponentDistance;
        Location goldLocation;
        boolean isGoldFound = false;

        while(!isGoldFound) {

            for (Response.StateLocations.PlayerLocation player : playerLocations) {
                if (player.entity().equals(myPlayer)) {
                    closestGoldLocation = setClosestGold(itemLocations, player.location(), skipGoldLocation);
                    myDistance = taxicabGeometryDistance(player.location(), closestGoldLocation);
                }
            }
            isGoldFound = true;

            for (Response.StateLocations.PlayerLocation player : playerLocations) {
                if (!player.entity().equals(myPlayer)) {
                    goldLocation = setClosestGold(itemLocations, player.location(), null);
                    opponentDistance = taxicabGeometryDistance(player.location(), goldLocation);
                    if (goldLocation.equals(closestGoldLocation) && opponentDistance < myDistance) {
                        isGoldFound = false;
                        skipGoldLocation.add(closestGoldLocation);
                    }
                }
            }

        }
    }

    public static Location setClosestGold(Collection<Response.StateLocations.ItemLocation> itemLocations, Location playerLocation, Collection<Location> skipGoldLocationList){
        Location closestGold = new Location(1,1);
        int minDist = 99999;
        int dist;
        for(Response.StateLocations.ItemLocation itemLocation : itemLocations){
            if(itemLocation.entity() instanceof Item.Gold && (skipGoldLocationList == null || !skipGoldLocationList.contains(itemLocation.location()))){

                dist = taxicabGeometryDistance(playerLocation,itemLocation.location());
                if(dist<minDist){
                    Location goldLocation = itemLocation.location();
                    minDist = dist;
                    closestGold = goldLocation;
                }
            }
        }

        return closestGold;
    }

    private static int taxicabGeometryDistance(Location playerLocation, Location goldLocation){
        int xDistance = Math.abs(playerLocation.column() - goldLocation.column());
        int yDistance = Math.abs(playerLocation.row() - goldLocation.row());

        return xDistance + yDistance;
    }

    public static void setMyPlayerLocation(Collection<Response.StateLocations.PlayerLocation> playerLocations, Player myPlayer){

        myPlayerLocation = playerLocations.stream()
                .filter(player -> player.entity().equals(myPlayer))
                .findFirst()
                .map(Response.StateLocations.PlayerLocation::location)
                .orElse(null);

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

        System.out.println("sciezka do zlota" + goldPath);
        System.out.println("moja lokalizacja" + myPlayerLocation);
        if(!goldPath.isEmpty()) {
            Location firstMove = new Location(goldPath.getLast().row() - myPlayerLocation.row(), goldPath.getLast().column() - myPlayerLocation.column());
            goldPath.removeLast();
            if (firstMove.column() == 1)
                return Direction.Right;
            if (firstMove.column() == -1)
                return Direction.Left;
            if (firstMove.row() == 1)
                return Direction.Down;
            else
                return Direction.Up;
        }
        else return  Direction.Up;
    }


}
