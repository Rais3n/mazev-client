package example.domain.game;

import example.domain.Board;
import example.domain.Response;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Movement {

    static Location myPlayerLocation;
    static Location closestResourceLocation;
    static boolean avoidPlayers = false;
    static ItemType pathingTo = ItemType.Gold;
    static List<Location> opponentsLocationList = new LinkedList<>();

    public static void setMyPlayerLocation(Collection<Response.StateLocations.PlayerLocation> playerLocations, Player myPlayer){

        myPlayerLocation = playerLocations.stream()
                .filter(player -> player.entity().equals(myPlayer))
                .findFirst()
                .map(Response.StateLocations.PlayerLocation::location)
                .orElse(null);

    }

    public static Location getMyPlayerLocation() {
        return myPlayerLocation;
    }

    private static void setClosestResourceLocation(Location closestResourceLocation) {
        Movement.closestResourceLocation = closestResourceLocation;
    }

    private static boolean filterResource(Response.StateLocations.ItemLocation item){
        if(item.entity() instanceof Item.Gold && pathingTo == ItemType.Gold)
            return true;
        else return item.entity() instanceof Item.Health && pathingTo == ItemType.HP;
    }

    public static void updateOpponentsLocationList(Collection<Response.StateLocations.PlayerLocation> playerLocations, Player myPlayer){
        opponentsLocationList.clear();
        playerLocations.stream()
                .filter(player -> !player.entity().equals(myPlayer))
                .map(Response.StateLocations.PlayerLocation::location)
                .forEach(location -> opponentsLocationList.add(location));

    }
    public static void setKindOfResourceToPath(Integer health){
        if(health < 75){
            avoidPlayers = true;
            pathingTo = ItemType.HP;
        }
        else if(avoidPlayers && health > 150){
            pathingTo = ItemType.Gold;
            avoidPlayers = false;
        }
    }


    public static Location findClosestResource(Collection<Response.StateLocations.ItemLocation> itemLocations, Location playerLocation, Collection<Location> skipResourceLocationList){
        Location closestResource = new Location(1,1);
        int minDist = 99999;
        int dist;

        for(Response.StateLocations.ItemLocation itemLocation : itemLocations){
            if(filterResource(itemLocation) && (skipResourceLocationList == null || !skipResourceLocationList.contains(itemLocation.location())))
            {
                dist = Algoritm.taxicabGeometryDistance(playerLocation,itemLocation.location());

                if(dist<minDist){
                    Location resourceLocation = itemLocation.location();
                    minDist = dist;
                    closestResource = resourceLocation;
                }
            }
        }

        return closestResource;
    }

    public static void setResourceToPath(Collection<Response.StateLocations.ItemLocation> itemLocations, Collection<Response.StateLocations.PlayerLocation> playerLocations, Player myPlayer){
        List<Location> skipResourceLocation = new LinkedList<>();
        int myDistance = 0;
        int opponentDistance;
        Location resourceLocation;
        boolean isResourceFound = false;

        while(!isResourceFound) {
            for (Response.StateLocations.PlayerLocation player : playerLocations) {
                if (player.entity().equals(myPlayer)) {
                    setClosestResourceLocation(findClosestResource(itemLocations, player.location(), skipResourceLocation));
                    myDistance = Algoritm.taxicabGeometryDistance(player.location(), closestResourceLocation);
                }
            }
            isResourceFound = true;

            for (Response.StateLocations.PlayerLocation player : playerLocations) {
                if (!player.entity().equals(myPlayer)) {
                    resourceLocation = findClosestResource(itemLocations, player.location(), null);
                    opponentDistance = Algoritm.taxicabGeometryDistance(player.location(), resourceLocation);
                    if (resourceLocation.equals(closestResourceLocation) && opponentDistance < myDistance) {
                        isResourceFound = false;
                        skipResourceLocation.add(closestResourceLocation);
                        break;
                    }
                }
            }

        }
        if(closestResourceLocation.equals(new Location(1,1))) {
            setClosestResourceLocation(findClosestResource(itemLocations, myPlayerLocation, null));
        }
    }

    public static Direction direction(Board board){

        List<Location> goldPath = Algoritm.dijkstra(board, myPlayerLocation, closestResourceLocation, avoidPlayers, opponentsLocationList);

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
