package example;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.domain.Board;
import example.domain.Request;
import example.domain.Response;
import example.domain.game.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Collection;

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
                final var json = objectMapper.writeValueAsString(new Request.Authorize("79rb42Fs"));
                writer.write(json);
                writer.newLine();
                writer.flush();
                logger.info("Sent command: {}", json);
            }

            Board board = null;
            Cave cave;
            Player player = null;
            Collection<Response.StateLocations.ItemLocation> itemLocations;
            Collection<Response.StateLocations.PlayerLocation> playerLocations;
            Integer health;
            Integer gold;

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
                        board = new Board(cave);
                        logger.info("cave: {}", cave);

                    }
                    case Response.StateLocations stateLocations -> {
                        itemLocations = stateLocations.itemLocations();
                        playerLocations = stateLocations.playerLocations();
                        health = stateLocations.health();
                        gold = stateLocations.gold();
                        logger.info("itemLocations: {}", itemLocations);
                        logger.info("playerLocations: {}", playerLocations);
                        Movement.setMyPlayerLocation(playerLocations,player);
                        Movement.updateOpponentsLocationList(playerLocations, player);
                        Movement.setKindOfResourceToPath(health);
                        System.out.println("Moje życie: " + health);
                        System.out.println("Moje złoto: " + gold);


                        Movement.setResourceToPath(itemLocations,playerLocations,player);

                        final var cmd = new Request.Command(Movement.direction(board));
                        final var cmdJson = objectMapper.writeValueAsString(cmd);
                        writer.write(cmdJson);
                        writer.newLine();
                        writer.flush();
                        logger.info("Sent command: {}", cmd);
                        board.printBoard(playerLocations,itemLocations);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error in client operation", e);
        } finally {
            logger.info("Client exiting");
        }
    }

}


