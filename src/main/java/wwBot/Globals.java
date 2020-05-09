package wwBot;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import discord4j.core.object.entity.MessageChannel;

public class Globals {
	public static Map<String, Card> mapAvailableCards;

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

	public static String printCardList(List<Card> list, String listName) {

		var messageList = "";

		if (list.isEmpty()) {
			messageList += "seems like this bitch empty";
		} else if (list != null) {
			messageList += "-----------------" + listName + "----------------------  \n";

			for (int i = 0; i < list.size(); i++) {
				messageList += "**Karte " + (i + 1) + ":** " + list.get(i).name + " ----- **Value:** "
						+ list.get(i).value + "\n";
			}
			messageList += "**Total Value**: " + totalCardValue(list);
		}

		return (messageList);
	}

	public static void printCard(String cardName, MessageChannel channel) {
		var requestedCard = mapAvailableCards.get(cardName);

		if (requestedCard != null) {
			String message = "Wert: " + Integer.toString(requestedCard.value) + "\n" + "Beschreibung: "
					+ requestedCard.description;
			var color = requestedCard.friendly ? Color.GREEN : Color.RED;

			channel.createEmbed(spec -> {
				spec.setColor(color).setTitle(cardName).setDescription(message);

			}).block();
		} else {
			channel.createMessage("card not found").block();
		}
	}

}