package wwBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.ReadJSONCard;
import wwBot.WerwolfGame.cards.Card;
import wwBot.WerwolfGame.cards.Role;

//In dieser Klasse werden alle Global nützliche Methoden geseichert
public class Globals {
	public static Map<String, Card> mapRegisteredCardsSpecs;

	// wird zu start aufgerufen und inizialisiert wichtige Variablen
	public static void loadGlobals() throws Exception {
		mapRegisteredCardsSpecs = ReadJSONCard.readCards();
	}

	// summiert die Values aller karten in der liste
	public static int totalCardValue(List<Role> list) {
		int totalValue = 0;
		for (var card : list) {
			totalValue += card.specs.value;
		}
		return totalValue;
	}

	// macht aus einer liste von Karten einen String, welcher tabellarisch den namen
	// und wert jeder Card enthält
	// param: list - eine Liste von Card, title - wird zum titel der Tabelle (meist
	// der name der Liste)
	public static String cardListToString(List<Role> list, String title, boolean printTotalValue) {
		// this variable gets filled with the card infos
		var message = "";

		// if the list is empty an error message appears, else it goes through every
		// single card and adds them to message
		if (list.isEmpty()) {
			message += "seems like this bitch empty";
		} else {
			message += "-----------------" + title + "----------------------  \n";

			for (int i = 0; i < list.size(); i++) {
				message += "**Karte " + (i + 1) + ":** " + list.get(i).specs.name + " ----- **Value:** "
						+ list.get(i).specs.value + "\n";
			}
			if (printTotalValue) {
				message += "**Total Value**: " + totalCardValue(list);
			}
		}
		return (message);
	}

	public static String cardListToString(List<Card> list, String title) {
		// this variable gets filled with the card infos
		var message = "";

		// if the list is empty an error message appears, else it goes through every
		// single card and adds them to message
		if (list.isEmpty()) {
			message += "seems like this bitch empty";
		} else {
			message += "----------------- " + title + " ----------------------  \n";

			for (int i = 0; i < list.size(); i++) {
				message += "Karte " + (i + 1) + ": **" + list.get(i).name + "** ----- Value: **" + list.get(i).value
						+ "**\n";
			}

		}
		return (message);
	}

	// converts a list of users to a String, listing all the users
	public static String userListToString(List<User> list, String title, Game game) {
		// this variable gets filled with the players
		var message = "";

		// if the list is empty an error message appears, else it goes through every
		// single player and adds them to message
		if (list.isEmpty()) {
			message += "seems like this bitch empty";
		} else {
			message += "-----------------" + title + "----------------------  \n";

			for (int i = 0; i < list.size(); i++) {
				message += list.get(i).asMember(game.server.getId()).block().getDisplayName();

				if (!list.get(i).getUsername()
						.equals(list.get(i).asMember(game.server.getId()).block().getDisplayName())) {
					message += " (aka. " + list.get(i).getUsername() + ")";
				}
				message += "\n";
			}
		}
		return (message);
	}

	public static String stringListToList(List<String> list, String title) {
		var mssg = "";

		// if the list is empty an error message appears, else it goes through every
		// single player and adds them to message
		if (list.isEmpty()) {
			mssg += "seems like this bitch empty";
		} else {

			// header
			mssg += "----------------  " + title + "  ----------------";
			mssg += "```diff\n";

			// lists every player in the list
			for (var entry : list) {
				mssg += "\n";
				mssg += entry;

			}
			mssg += "\n```";
		}
		return mssg;
	}

	public static String playerListToRoleList(List<Player> listPlayer, String title) {
		var tempList = new ArrayList<String>();
		for (Player player : listPlayer) {
			tempList.add(player.role.name);
		}

		return stringListToList(tempList, title);
	}

	public static String playerListToList(List<Player> listPlayer, String title) {
		var tempList = new ArrayList<String>();
		for (Player player : listPlayer) {
			tempList.add(player.name);
		}

		return stringListToList(tempList, title);
	}

	public static String playerListToList(List<Player> list, String title, boolean revealInformation) {
		var mssgPlayerList = "";

		// if the list is empty an error message appears, else it goes through every
		// single player and adds them to message
		if (list.isEmpty()) {
			mssgPlayerList += "seems like this bitch empty";
		} else {

			// header
			mssgPlayerList += "-------------------  " + title + "  -------------------";
			mssgPlayerList += "```diff\n";

			// lists every player in the list
			for (var entry : list) {
				mssgPlayerList += "\n";

				if (revealInformation) {
					if (entry.role.specs.friendly) {
						mssgPlayerList += "+ ";
					} else {
						mssgPlayerList += "- ";
					}
				}

				mssgPlayerList += entry.name;

				if (revealInformation) {
					mssgPlayerList += " ---> " + entry.role.name;
				}

			}
			mssgPlayerList += "\n```";
		}
		return mssgPlayerList;
	}

	public static void printPlayersMap(MessageChannel channel, Map<Snowflake, Player> map, String title, 
			boolean revealInformation) {
		var tempList = new ArrayList<Player>();
		for (var entry : map.entrySet()) {
			tempList.add(entry.getValue());
		}
		Globals.createEmbed(channel, Color.LIGHT_GRAY, "",
				Globals.playerListToList(tempList, title, revealInformation));
	}

	// erhält den Namen einer Karte, sucht diese in allen verfügbaren Karten und
	// erstellt ein embed (nachricht) mit den Informationen der Karte und sendet
	// dieses in den erhaltenen Channel
	public static void printCard(String cardName, MessageChannel channel) {
		var requestedCard = mapRegisteredCardsSpecs.get(cardName);

		// falls eine karte gefunden wird, wird ein embed mit den infos erstellt und in
		// den channel geschickt
		if (requestedCard != null) {
			String message = "Wert: " + Integer.toString(requestedCard.value) + "\n" + "Beschreibung: "
					+ requestedCard.description;
			// die farbe ist grün wenn die karte friendly ist, sonst rot
			var color = requestedCard.friendly ? Color.GREEN : Color.RED;

			channel.createEmbed(spec -> {
				spec.setColor(color).setTitle(cardName).setDescription(message);

			}).block();
		} else {
			channel.createMessage("Card not found").block();
		}
	}

	public static void createEmbed(MessageChannel channel, Color color, String title, String description) {
		channel.createEmbed(emb -> {
			emb.setColor(color).setTitle(title).setDescription(description);
		}).block();
	}

	public static void createMessage(MessageChannel channel, String message) {
		channel.createMessage(messageSpec -> {
			messageSpec.setContent(message);
		}).block();
	}

	public static void createMessage(MessageChannel channel, String message, boolean ifTTS) {
		channel.createMessage(messageSpec -> {
			messageSpec.setContent(message).setTts(ifTTS);
		}).block();
	}

	// fügt mehrere Karten einer Liste hinzu
	public static void addMultipleCards(int amount, String cardName, List<Role> list) {
		for (int i = 0; i < amount; i++) {
			list.add(Role.createRole(cardName));
		}
	}

	// checks the syntax of a Private command and find the player
	public static Player commandPlayerFinder(MessageCreateEvent event, List<String> parameters,
			MessageChannel msgChannel, Game game) {
		if (parameters == null || parameters.size() > 2) {
			MessagesWW.errorWrongSyntax(msgChannel);
			return null;
		} else {
			// finds the player
			var player = game.findPlayerByName(parameters.get(0));

			if (player == null) {
				MessagesWW.errorPlayerNotFound(msgChannel);
				return null;
			}
			if (!player.role.deathDetails.alive) {
				MessagesWW.errorPlayerAlreadyDead(msgChannel);
				return null;
			}

			else {
				return player;
			}
		}

	}

	public static void sleepWCatch(int ms) {
		// waits for a bit for suspense
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			System.out.println("error in: Thread.sleep();");
			e.printStackTrace();
		}
	}

	// finds a card in the map of all Cards
	public static boolean ifCardExists(String name) {
		var mapCards = Globals.mapRegisteredCardsSpecs;
		var ifFound = mapCards.containsKey(name);

		return ifFound;
	}

	public static String removeDash(String rawName) {
		var name = rawName.replaceAll("-", " ");
		return name;
	}

	public static boolean listContainsCard(List<Role> listDeck, Card card) {
		var containsCard = false;

		for (Role cardDeck : listDeck) {
			if (cardDeck.name.equalsIgnoreCase(card.name)) {
				containsCard = true;
			}
		}
		return containsCard;
	}

	static boolean listContainsCard(List<Role> listDeck, Role card) {
		var containsCard = false;

		for (Role cardDeck : listDeck) {
			if (cardDeck.name.equalsIgnoreCase(card.name)) {
				containsCard = true;
			}
		}
		return containsCard;
	}

	public static void setMuteAllPlayers(Map<Snowflake, Player> mapPlayers, boolean isMuted, Snowflake serverId) {
		// mutes all players at night
		var listUsers = new ArrayList<User>();
		for (var player : mapPlayers.values()) {
			listUsers.add(player.user);
		}

		setMuteAllPlayers(listUsers, isMuted, serverId);

	}

	public static void setMuteAllPlayers(List<User> listUsers, boolean isMuted, Snowflake serverId) {
		for (var user : listUsers) {
			try {
				user.asMember(serverId).block().edit(a -> {
					a.setMute(isMuted).setDeafen(false);
				}).block();
			} catch (Exception e) {
				System.out.println("Could Not Mute");;
			}
		}
	}

	public static List<User> getUsersFromJoinedVoicechannel(Snowflake serverId, MessageCreateEvent event) {
		var voiceStates = event.getMessage().getAuthor().get().asMember(serverId).block().getVoiceState().block()
				.getChannel().block().getVoiceStates().collectList().block();

		List<User> listUsers = new ArrayList<>();
		for (VoiceState voiceState : voiceStates) {
			listUsers.add(voiceState.getUser().block());
		}
		
		return listUsers;
	}

}