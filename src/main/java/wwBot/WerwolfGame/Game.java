package wwBot.WerwolfGame;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import wwBot.CommandHandler;
import wwBot.Globals;
import wwBot.Interfaces.Command;
import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.GameStates.GameState;
import wwBot.WerwolfGame.GameStates.LobbyState;
import wwBot.WerwolfGame.GameStates.MainState;
import wwBot.WerwolfGame.cards.Card;
import wwBot.WerwolfGame.cards.Role;

public class Game {
    public Map<String, Command> gameCommands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public Map<Snowflake, Player> mapPlayers = new HashMap<>();
    public Map<Snowflake, Player> livingPlayers = new HashMap<>();
    public List<Player> deadPlayers = new ArrayList<>();
    public HashMap<Snowflake, ArrayList<PrivateCommand>> mapPrivateCommands = new HashMap<>();
    public List<Role> deck = new ArrayList<>();
    public List<Message> msgToDel = new ArrayList<>();

    public boolean gameRuleAutomaticMod = false;
    public boolean gameRulePrintCardOnDeath = false;
    public boolean gameRuleMutePlayersAtNight = false;

    public Game backupGame = this;
    public MainState.DayPhase backupDayPhase = null;

    public Guild server;
    public MessageChannel mainChannel;
    public GameState gameState;
    public User userModerator;
    public int avgDelaytime = 1000;

    public Game(Guild guild, MessageChannel givenChannel) {

        mainChannel = givenChannel;
        server = guild;
        registerGameCommands();

        // initializes the first game State
        gameState = new LobbyState(this);




       /*  Main.client.getEventDispatcher().on(TypingStartEvent.class).filter(message -> message.getUser().blockOptional()
        .map(user -> !user.getId().equals(Main.client.getSelfId().get())).orElse(false)).subscribe(event -> {
            try {
                event.getChannel().block().createMessage("You startet Typing\n" + event.toString()).block();

            } catch (Exception ex) {
                ex.printStackTrace();
                event.getChannel().block().createMessage("ex in test").block();
            }
        }); */

    }

    public void handleCommands(MessageCreateEvent event, MessageChannel msgChannel) {

        
        String messageContent = event.getMessage().getContent().orElse("");
        List<String> parameters = new LinkedList<>(Arrays.asList(messageContent.split(" ")));
        var requestedCommand = parameters.remove(0);
        requestedCommand = requestedCommand.substring(1);
        boolean found = false;
        if (msgChannel == null) {
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
            MessagesWW.errorCommandNotFound(msgChannel);
        }

        //deletes Messages registered to be deleted
        for (Message message : msgToDel) {
            message.delete().block();
        }
        msgToDel.clear();
    }

    //TODO: add admin Commands

    // loads the Commands available throughout the game into the map gameCommands
    private void registerGameCommands() {
        final var mapRegisteredCards = Globals.mapRegisteredCardsSpecs;

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

            // prints the moderator if there is one
            if (!gameRuleAutomaticMod && userModerator != null) {
                msgChannel
                        .createMessage("Moderator: " + userModerator.asMember(server.getId()).block().getDisplayName());
            } else if (!gameRuleAutomaticMod && userModerator == null) {
                msgChannel.createMessage("Moderator: wurde noch nicht bestimmt!");
            }

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
            msgChannel.createMessage(Globals.cardListToString(tempList, "Alle Karten")).block();

        };
        gameCommands.put("allCards", allCardsCommand);
        gameCommands.put("listAllCards", allCardsCommand);

        // prints a link to the official manual
        Command showManualCommand = (event, parameters, msgChannel) -> {
            Globals.createEmbed(msgChannel, Color.BLACK, "",
                    "To view the official manual go here ---> [https://www.prometheusshop.de/media/pdf/d3/5d/36/werw-lfe-spielanleitung.pdf]");

        };
        gameCommands.put("Anleitung", showManualCommand);
        gameCommands.put("Handbuch", showManualCommand);
        gameCommands.put("Manual", showManualCommand);

        Command reset = (event, parameters, msgChannel) ->{
            if (msgChannel.getType() != Channel.Type.DM) {
                var serverId = event.getGuildId().get();
                CommandHandler.mapRunningGames.put(serverId, backupGame);
                if(backupDayPhase != null){
                    CommandHandler.mapRunningGames.get(serverId).gameState.changeDayPhase(backupDayPhase);
                }
            } else{
                msgChannel.createMessage("Command can only be used in a Guild (server)").block();
            }
            

        };
        gameCommands.put("reset4567", reset);
        

    }

    public void changeGameState(GameState nextGameState) {
        gameState.exit();
        gameState = nextGameState;
        gameState.start();
    }

    public void addPrivateCommand(Snowflake id, PrivateCommand command) {
        var tempList = new ArrayList<PrivateCommand>();
        if (mapPrivateCommands.containsKey(id)) {
            tempList = mapPrivateCommands.get(id);
        }

        tempList.add(command);
        mapPrivateCommands.put(id, tempList);
    }

    public boolean closeGame() {
        return gameState.exit();
    }

	
    // finds a player in a Map by Username. Returns null if it finds noone or
	// multiple Players
	public Player findPlayerByNameLiving(String name) {
		return findPlayerByName(name, livingPlayers);
    }
    
    public Player findPlayerByName(String name) {
		return findPlayerByName(name, mapPlayers);
    }

    // finds a player in a Map by Username. Returns null if it finds noone or
	// multiple Players
	private Player findPlayerByName(String name, Map<Snowflake, Player> map) {
        Globals.removeDash(name);
		Player foundPlayer = null;
		var found = 0;
		for (var entry : map.values()) {
	
			var playerName = entry.name;
            var userName = entry.user.getUsername();
            var displayName = entry.user.asMember(server.getId()).block().getDisplayName();
            
	
			if (playerName.equalsIgnoreCase(name) || userName.equalsIgnoreCase(name) || displayName.equalsIgnoreCase(name)) {
				foundPlayer = entry;
                found++;
                break;
			}
	
		}
		if (found != 1) {
			foundPlayer = null;
		}
		return foundPlayer;
    }

}