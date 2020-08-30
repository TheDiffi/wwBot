package wwBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import wwBot.cards.Card;
import wwBot.cards.Role;

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
			message += "-----------------" + title + "----------------------  \n";

			for (int i = 0; i < list.size(); i++) {
				message += "**Karte " + (i + 1) + ":** " + list.get(i).name + " ----- **Value:** " + list.get(i).value
						+ "\n";
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
				message += list.get(i).asMember(game.server.getId()).block().getDisplayName() + " (aka. "
						+ list.get(i).getUsername() + ")\n";
			}
		}
		return (message);
	}

	public static String playerListToString(List<Player> list, String title, Game game) {
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

				if (entry.role.specs.friendly) {
					mssgPlayerList += "+ ";
				} else {
					mssgPlayerList += "- ";
				}

				mssgPlayerList += entry.name + " ---> " + entry.role.name;

			}
			mssgPlayerList += "\n```";
		}
		return mssgPlayerList;
	}

	public static void printPlayersMap(MessageChannel channel, Map<Snowflake, Player> map, String title, Game game) {
		var tempList = new ArrayList<Player>();
		for (var entry : map.entrySet()) {
			tempList.add(entry.getValue());
		}
		Globals.createEmbed(channel, Color.LIGHT_GRAY, "", Globals.playerListToString(tempList, title, game));
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

	// finds a player in a Map by Username. Returns null if it finds noone or
	// multiple Players
	public static Player findPlayerByName(String name, Map<Snowflake, Player> map, Game game) {
		Player foundPlayer = null;
		var found = 0;
		for (var entry : map.entrySet()) {

			var displayName = entry.getValue().name;
			var userName = entry.getValue().user.getUsername();

			if (displayName.equalsIgnoreCase(name) || userName.equalsIgnoreCase(name)) {
				foundPlayer = entry.getValue();
				found++;
			}

		}
		if (found != 1) {
			foundPlayer = null;
		}
		return foundPlayer;
	}

	// checks the syntax of a Private command and find the player
	public static Player privateCommandPlayerFinder(MessageCreateEvent event, List<String> parameters,
			MessageChannel msgChannel, Game game) {
		if (parameters == null || parameters.size() > 2) {
			MessagesMain.errorWrongSyntax(msgChannel);
			return null;
		} else {
			// finds the player
			var player = Globals.findPlayerByName(Globals.removeDash(parameters.get(0)), game.mapPlayers, game);

			if (player == null) {
				MessagesMain.errorPlayerNotFound(msgChannel);
				return null;
			}
			if (!player.role.alive) {
				MessagesMain.errorPlayerAlreadyDead(msgChannel);
				return null;
			}

			else {
				return player;
			}
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

}