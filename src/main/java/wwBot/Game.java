package wwBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import wwBot.GameStates.GameState;
import wwBot.GameStates.LobbyState;
import wwBot.GameStates.MainGameState;

public class Game {
    public Map<String, Command> gameCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    public Map<Snowflake, Player> mapPlayers = new HashMap<Snowflake, Player>();
    public Map<Snowflake, Player> livingPlayers = new HashMap<Snowflake, Player>();
    public HashMap<Snowflake, PrivateCommand> mapPrivateCommands = new HashMap<Snowflake, PrivateCommand>();
    public List<Card> deck = new ArrayList<>();
    public GameState currentGameState;
    public Snowflake runningInServer;
    public MessageChannel runningInChannel;
    public boolean gameRuleAutomatic = true;
    public User userModerator;

    Game(Snowflake snowflakeServer, MessageChannel givenChannel) {

        runningInChannel = givenChannel;
        runningInServer = snowflakeServer;
        registerGameCommands();

        // initializes the first game State
        currentGameState = new LobbyState(this);

    }





    public void handleCommands(MessageCreateEvent event) {

        String messageContent = event.getMessage().getContent().orElse("");
        List<String> parameters = new LinkedList<>(Arrays.asList(messageContent.split(" ")));
        var requestedCommand = parameters.remove(0);
        requestedCommand = requestedCommand.substring(1);

        var foundCommandGame = gameCommands.get(requestedCommand);
        if (foundCommandGame != null) {
            foundCommandGame.execute(event, parameters, runningInChannel);
        }

        var foundCommandState = currentGameState.gameStateCommands.get(requestedCommand);
        if (foundCommandState != null) {
            foundCommandState.execute(event, parameters, runningInChannel);
        } else if( !requestedCommand.equalsIgnoreCase("newGame") && !requestedCommand.equalsIgnoreCase("DeleteGame")) {
            runningInChannel.createMessage("Command Not Found").block();
        }

    }

    // loads the Commands available throughout the game into the map gameCommands
    private void registerGameCommands() {

        // speichert den Prefix in einer Variable
        var prefix = "&";

        // ping testet ob der bot antwortet
        Command pingCommand = (event, parameters, msgChannel) -> {
            
               msgChannel.createMessage("Pong! Game").block();
            
        };
        gameCommands.put("ping", pingCommand);
        
        Command showCardCommand = (event, parameters, msgChannel) -> {
            String cardName = parameters.get(0);

            Globals.printCard(cardName, event.getMessage().getChannel().block());
        };

        gameCommands.put("showCard", showCardCommand);

        // basically !help
        Command helpCommand = (event, parameters, msgChannel) -> {

            // TODO: create a message builder or embed with all the info
            // help
            
        msgChannel.createMessage("TODO: add help Command in Game").block();
        
            msgChannel.createMessage("If you have not created a Deck jet, try **" + prefix
                    + "buildDeck** to let the Algorithm build a Deck for you or try **" + prefix
                    + "addCard** to add a Card or **" + prefix
                    + "removeCard** to remove a Card to your Custom Deck").block();

        };
        gameCommands.put("help", helpCommand);

    }

    public void changeGameState(GameState nextGameState) {
        currentGameState.exit();
        currentGameState = nextGameState;
    }

}