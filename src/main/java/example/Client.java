package example;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.domain.Request;
import example.domain.Response;
import example.domain.game.Cave;
import example.domain.game.Direction;
import example.domain.game.Location;
import example.domain.game.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import static java.lang.Math.abs;

public class Client {
    private static final String HOST = "35.208.184.148";
    private static final int PORT = 8080;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        new Client().startClient();
    }

    public void startClient() {
        try (final var socket = new Socket(HOST, PORT);
             final var is = socket.getInputStream();
             final var isr = new InputStreamReader(is);
             final var reader = new BufferedReader(isr);
             final var os = socket.getOutputStream();
             final var osr = new OutputStreamWriter(os);
             final var writer = new BufferedWriter(osr)) {
            logger.info("Connected to server at {}:{}", HOST, PORT);

            {
                final var json = objectMapper.writeValueAsString(new Request.Authorize("1679"));
                writer.write(json);
                writer.newLine();
                writer.flush();
                logger.info("Sent command: {}", json);
            }

            Cave cave;
            Player player;
            Collection<Response.StateLocations.ItemLocation> itemLocations;
            Collection<Response.StateLocations.PlayerLocation> playerLocations;
            char[][] board = new char[0][0];
            ArrayList<LinkedList<Character>> goldpaths= new ArrayList<>();

            while (!Thread.currentThread().isInterrupted()) {
                final var line = reader.readLine();
                if (line == null) {
                    break;
                }

                final var response = objectMapper.readValue(line, Response.class);
                switch (response) {
                    case Response.Authorized authorized -> {
                        player = authorized.humanPlayer();
                        logger.info("authorized: {}", authorized);
                    }
                    case Response.Unauthorized unauthorized -> {
                        logger.error("unauthorized: {}", unauthorized);
                        return;
                    }
                    case Response.StateCave stateCave -> {
                        cave = stateCave.cave();
                        board = new char[cave.rows()][cave.columns()];
                        clearBoard(board);
                        assignRocks(board,cave);
                        logger.info("cave: {}", cave);

                    }
                    case Response.StateLocations stateLocations -> {
                        itemLocations = stateLocations.itemLocations();
                        playerLocations = stateLocations.playerLocations();
                        logger.info("itemLocations: {}", itemLocations);
                        logger.info("playerLocations: {}", playerLocations);

                        final var cmd = new Request.Command(Direction.Up);
                        final var cmdJson = objectMapper.writeValueAsString(cmd);
                        writer.write(cmdJson);
                        writer.newLine();
                        writer.flush();
                        logger.info("Sent command: {}", cmd);
                        clearBoard(board);
                        assignItems(board, itemLocations,playerLocations);
                        writeBoard(board);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error in client operation", e);
        } finally {
            logger.info("Client exiting");
        }
    }
    public void clearBoard(char[][] board){
        for (int i = 0; i < board[0].length; i++)
            for(int j=0;j<board.length;j++) {
                if(board[i][j]!='X')
                    board[i][j]=' ';
            }

    }
    public void assignRocks(char[][] board, Cave cave){
        for (int i = 0; i < board[0].length; i++){
            for(int j=0;j<board.length;j++){
                if (cave.rock(i, j)) {
                    board[i][j] = 'X';
                }
            }
        }
    }

    public void assignItems(char[][] board, Collection<Response.StateLocations.ItemLocation> itemLocations, Collection<Response.StateLocations.PlayerLocation> playerLocations){
        for (int i = 0; i < board[0].length; i++){
            for(int j=0;j<board.length;j++){
                if (isGold(itemLocations,i,j)) {
                    board[i][j] = 'G';
                }
                else if(isPlayer(playerLocations,i,j)){
                    board[i][j] = 'P';
                }
            }
        }
    }
    public void writeBoard(char[][] board){
        for(int i=0;i< board[0].length;i++) {
            for (int j = 0; j < board.length; j++) {
                System.out.print(board[i][j]);
            }
            System.out.println();
        }
    }

    private boolean isGold(Collection<Response.StateLocations.ItemLocation> itemLocations, int coloumn, int row) {
        Location location;
        for(Response.StateLocations.ItemLocation item : itemLocations){
            location = item.location();
            if(location.row() == row && location.column() == coloumn){
                return true;
            }
        }
        return false;
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

    private void getShortestGoldpath(Location playerLocation, Collection<Response.StateLocations.ItemLocation> itemLocations, Cave cave){
        int length;
        int shortestLength = 999999;
        Location goldLocation = new Location(0,0);

        for(var item : itemLocations){
            length = abs(item.location().column() - playerLocation.column()) + abs(item.location().row() - playerLocation.row());
            if(length<shortestLength) {
                goldLocation = item.location();
                shortestLength = length;
            }
        }

        StringBuilder path = new StringBuilder();
        Location updatedPlayerLocation = playerLocation;

        length = goldLocation.row() - playerLocation.row();
     //   <---------------------->
        while(updatedPlayerLocation != goldLocation){
            int offset = length/abs(length);
            while(!cave.rock(playerLocation.row() + offset, updatedPlayerLocation.row()) && updatedPlayerLocation != goldLocation )
            {
                if(offset == 1){
                    path.append('w');
                    updatedPlayerLocation = new Location(updatedPlayerLocation.row() + 1, updatedPlayerLocation.column());
                }
                else {
                    path.append('s');
                    updatedPlayerLocation = new Location(updatedPlayerLocation.row() - 1, updatedPlayerLocation.column());
                }
            }
        }

    }

}


