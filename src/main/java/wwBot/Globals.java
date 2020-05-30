package wwBot;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;

//In dieser Klasse werden alle Global nützliche Methoden geseichert
public class Globals {
	public static Map<String, Card> mapRegisteredCards;

	// wird zu start aufgerufen und inizialisiert wichtige Variablen
	public static void loadGlobals() throws Exception {
		mapRegisteredCards = ReadJSONCard.readCards();
	}

	// summiert die Values aller karten in der liste
	public static int totalCardValue(List<Card> list) {
		int totalValue = 0;
		for (var card : list) {
			totalValue += card.value;
		}
		return totalValue;
	}

	// macht aus einer liste von Karten einen String, welcher tabellarisch den namen
	// und wert jeder Card enthält
	// param: list - eine Liste von Card, title - wird zum titel der Tabelle (meist
	// der name der Liste)
	public static String cardListToString(List<Card> list, String title, boolean printTotalValue) {
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
			if (printTotalValue) {
				message += "**Total Value**: " + totalCardValue(list);
			}
		}
		return (message);
	}

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
				message += list.get(i).asMember(game.server.getId()).block().getDisplayName() + "\n";
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
			mssgPlayerList += "-----------------" + title + "----------------------  \n";

			for (var entry : game.livingPlayers.entrySet()) {
				if (!entry.getValue().alive) {
					mssgPlayerList += "~~";
				}

				mssgPlayerList += entry.getValue().name + " ---> " + entry.getValue().role.name + "\n";

				if (!entry.getValue().alive) {
					mssgPlayerList += "~~";
				}
			}
		}

		return mssgPlayerList;

	}

	// erhält den Namen einer Karte, sucht diese in allen verfügbaren Karten und
	// erstellt ein embed (nachricht) mit den Informationen der Karte und sendet
	// dieses in den erhaltenen Channel
	public static void printCard(String cardName, MessageChannel channel) {
		var requestedCard = mapRegisteredCards.get(cardName);

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
	public static void addMultiple(int amount, Card card, List<Card> list) {
		for (int i = 0; i < amount; i++) {
			list.add(card);
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

	public static String removeDash(String rawName) {
		var name = rawName.replaceAll("-", " ");
		return name;
	}

}