package wwBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import wwBot.GameStates.GameState;
import wwBot.GameStates.LobbyState;
import wwBot.GameStates.MessagesMain;
import wwBot.Interfaces.Command;
import wwBot.Interfaces.PrivateCommand;

public class Game {
    public Map<String, Command> gameCommands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public Map<Snowflake, Player> mapPlayers = new HashMap<>();
    public Map<Snowflake, Player> livingPlayers = new HashMap<>();
    public List<Player> deadPlayers = new ArrayList<>();
    public HashMap<Snowflake, ArrayList<PrivateCommand>> mapPrivateCommands = new HashMap<>();
    public List<Card> deck = new ArrayList<>();
    public GameState gameState;
    public Guild server;
    public MessageChannel mainChannel;
    public boolean gameRuleAutomatic = false;
    public User userModerator;

    Game(Guild guild, MessageChannel givenChannel) {

        mainChannel = givenChannel;
        server = guild;
        registerGameCommands();
        MessagesMain.newGameStartMessage(mainChannel);

        // initializes the first game State
        gameState = new LobbyState(this);

    }

    public void handleCommands(MessageCreateEvent event, MessageChannel msgChannel) {

        String messageContent = event.getMessage().getContent().orElse("");
        List<String> parameters = new LinkedList<>(Arrays.asList(messageContent.split(" ")));
        var requestedCommand = parameters.remove(0);
        requestedCommand = requestedCommand.substring(1);
        boolean found = false;
        if(msgChannel == null){
            msgChannel = mainChannel;
        }

        if (gameState.handleCommand(requestedCommand, event, parameters, msgChannel)) {
            found = true;
        }

        else if (gameCommands.containsKey(requestedCommand)) {
            gameCommands.get(requestedCommand).execute(event, parameters, msgChannel);
            found = true;
        }

        // found überprüft ob der Command irgentwo gefunden wurde
        if (!found) {
            event.getMessage().getChannel().block().createMessage("Command Not Found").block();
        }

    }

    // loads the Commands available throughout the game into the map gameCommands
    private void registerGameCommands() {
        final var mapRegisteredCards = Globals.mapRegisteredCards;

        // ping testet ob der bot antwortet
        Command pingCommand = (event, parameters, msgChannel) -> {

            msgChannel.createMessage("Pong! Game").block();

        };
        gameCommands.put("ping", pingCommand);

        // zeigt die verfügbaren commands
        Command showCommandsCommand = (event, parameters, msgChannel) -> {
            var mssg = "";
            for (var command : gameCommands.entrySet()) {
                mssg += "\n" + command.getKey();
            }
            msgChannel.createMessage(mssg).block();
        };
        gameCommands.put("showCommands", showCommandsCommand);
        

        // basically !help
        Command helpCommand = (event, parameters, msgChannel) -> {
            MessagesMain.helpLobbyPhase(event);

        };
        gameCommands.put("help", helpCommand);
        gameCommands.put("hilfe", helpCommand);

        // prints a requested card
        Command showCardCommand = (event, parameters, msgChannel) -> {
            String cardName = parameters.get(0);
            if (cardName != null) {
                Globals.printCard(cardName, event.getMessage().getChannel().block());
            } else {
                event.getMessage().getChannel().block()
                        .createMessage("Ich verstehe dich nicht. Benutze \"&ShowCard <Kartenname>\"").block();
            }
        };
        gameCommands.put("Card", showCardCommand);
        gameCommands.put("showCard", showCardCommand);
        gameCommands.put("findCard", showCardCommand);

        // shows the current Deck to the user
        Command showDeckCommand = (event, parameters, msgChannel) -> {
            // prints the deck
            msgChannel.createMessage(Globals.cardListToString(deck, "Deck", true)).block();

        };
        gameCommands.put("Deck", showDeckCommand);
        gameCommands.put("showDeck", showDeckCommand);

        // lists all registered Cards
        Command allCardsCommand = (event, parameters, msgChannel) -> {
            // converts the map to a list
            var tempList = new ArrayList<Card>();
            for (var entry : mapRegisteredCards.entrySet()) {
                tempList.add(entry.getValue());
            }
            // prints the list
            msgChannel.createMessage(Globals.cardListToString(tempList, "Alle Karten", false)).block();

        };
        gameCommands.put("allCards", allCardsCommand);
        gameCommands.put("listAllCards", allCardsCommand);

    }

    public void changeGameState(GameState nextGameState) {
        gameState.exit();
        gameState = nextGameState;
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