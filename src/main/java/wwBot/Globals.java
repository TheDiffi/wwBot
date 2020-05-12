package wwBot;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import discord4j.core.object.entity.MessageChannel;

//In dieser Klasse werden alle Global nützliche Methoden geseichert
public class Globals {
	public static Map<String, Card> mapAvailableCards;

	// wird zu start aufgerufen und inizialisiert wichtige Variablen
	public static void loadGlobals() throws Exception {
		mapAvailableCards = ReadJSONCard.readAvailableCards();
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
			message += "**Total Value**: " + totalCardValue(list);
		}
		return (message);
	}

	// erhält den Namen einer Karte, sucht diese in allen verfügbaren Karten und
	// erstellt ein embed (nachricht) mit den Informationen der Karte und sendet
	// dieses in den erhaltenen Channel
	public static void printCard(String cardName, MessageChannel channel) {
		var requestedCard = mapAvailableCards.get(cardName);

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
			channel.createMessage("card not found").block();
		}
	}

	public static void createEmbed(MessageChannel channel, Color color, String title, String description) {
        channel.createEmbed(emb -> {
            emb.setColor(color).setTitle(title).setDescription(description);
        }).block();
	}
	
	public static void createMessageBuilder(MessageChannel channel, String message, boolean ifTTS) {
        channel.createMessage(messageSpec -> {
            messageSpec.setContent(message)
                    .setTts(ifTTS);
                }).block();
    }

	
}