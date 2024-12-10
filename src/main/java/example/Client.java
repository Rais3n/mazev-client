package example;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.domain.Request;
import example.domain.Response;
import example.domain.game.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;

public class Client {
    private static final String HOST = "35.208.184.138";
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
                        assignItems(board, itemLocations);
                        movePlayer(board, playerLocations);
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

    public void assignItems(char[][] board, Collection<Response.StateLocations.ItemLocation> itemLocations){
        for (int i = 0; i < board[0].length; i++){
            for(int j=0;j<board.length;j++){
                char item = isItem(itemLocations,i,j);
                if (item != ' ') {
                    board[i][j] = item;
                }
            }
        }
    }

    private void movePlayer(char[][] board, Collection<Response.StateLocations.PlayerLocation> playerLocations){
        //dijkstra(board,)
        for (int i = 0; i < board[0].length; i++){
            for(int j=0;j<board.length;j++){
                if(isPlayer(playerLocations,i,j)){
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

    private List<Location> dijkstra(char[][] board ,Location startLocation, Location goldLocation){
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

}


