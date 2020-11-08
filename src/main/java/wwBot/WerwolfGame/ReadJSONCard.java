package wwBot.WerwolfGame;

import java.io.FileReader;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import wwBot.WerwolfGame.cards.Card;


public class ReadJSONCard {

    @SuppressWarnings("unchecked")
    public static Map<String, Card> readCards() throws Exception {

        // loads the available cards
        var registeredCards = new TreeMap<String, Card>(String.CASE_INSENSITIVE_ORDER);

        // JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader("availableCards.json");
        // Read JSON file
        Object obj = jsonParser.parse(reader);

        JSONArray JSONCardList = (JSONArray) obj;

        // Iterate over card array
        JSONCardList.forEach(card -> {
            var parsedCard = parseCardObject((JSONObject) card);
            registeredCards.put(parsedCard.name, parsedCard);
        });

        return registeredCards;
    }

    // reads the Json to create a Card Object
    private static Card parseCardObject(JSONObject card) {
        // Get card object within list
        JSONObject cardJSONObject = (JSONObject) card.get("Card");

        // Get card name as String
        String name = (String) cardJSONObject.get("name");

        // Get value as an int
        int value = Integer.parseInt((String) cardJSONObject.get("value"));

        // Get priority as int
        int priority = Integer.parseInt((String) cardJSONObject.get("priority"));

        // Get minPlayers as int
        int minPlayers = Integer.parseInt((String) cardJSONObject.get("minPlayers"));

        // Get unique as a Boolean
        boolean unique = Boolean.parseBoolean((String) cardJSONObject.get("unique"));

        // Get friendly as a Boolean
        boolean friendly = Boolean.parseBoolean((String) cardJSONObject.get("friendly"));

        // Get description as a String
        String description = (String) cardJSONObject.get("description");

        // Get nightSequence as int
        int nightPrio = Integer.parseInt((String) cardJSONObject.get("nightPrio"));

        // writes the properties on the card Object
        Card cardObj = new Card();
        cardObj.name = name;
        cardObj.value = value;
        cardObj.priority = priority;
        cardObj.minPlayers = minPlayers;
        cardObj.unique = unique;
        cardObj.description = description;
        cardObj.friendly = friendly;
        cardObj.nightSequence = nightPrio;

        return cardObj;

    }
}