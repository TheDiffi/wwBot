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

public class Game {
    public Map<String, Command> gameCommands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public Map<Snowflake, Player> mapPlayers = new HashMap<>();
    public Map<Snowflake, Player> livingPlayers = new HashMap<>();
    public List<Player> deadPlayers = new ArrayList<>();
    public HashMap<Snowflake, ArrayList<PrivateCommand>> mapPrivateCommands = new HashMap<>();
    public List<Card> deck = new ArrayList<>();
    public GameState currentGameState;
    public Snowflake runningInServer;
    public MessageChannel runningInChannel;
    public boolean gameRuleAutomatic = false;
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
        boolean found = false;

        var foundCommandGame = gameCommands.get(requestedCommand);
        if (foundCommandGame != null) {
            foundCommandGame.execute( event, parameters, runningInChannel);
            found = true;
        }

        if (currentGameState.handleCommand(requestedCommand, event, parameters, runningInChannel)) {
            found = true;
        }

        if (!found) {
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

        //zeigt die verfügbaren commands
        Command showCommandsCommand = (event, parameters, msgChannel) -> {
            var mssg = "";
            for (var command : gameCommands.entrySet()) {
                mssg+= "\n"+command.getKey();
            }
             msgChannel.createMessage(mssg).block();
        };
        gameCommands.put("showCommands", showCommandsCommand);
        
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


	public void addPrivateCommand(Snowflake id, PrivateCommand lynchCommand) {
	    var tempList = new ArrayList<PrivateCommand>();
	    if (mapPrivateCommands.containsKey(id)) {  
	        tempList = mapPrivateCommands.get(id);
	    } 
	       
	        tempList.add(lynchCommand);
	        mapPrivateCommands.put(id, tempList);
	}

}