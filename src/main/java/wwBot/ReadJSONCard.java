package wwBot;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 
public class ReadJSONCard {

    

    @SuppressWarnings("unchecked")
    public static List<Card> readAvailableCards() throws Exception {

        List<Card> listAvailableCards = new ArrayList<Card>();

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader("availableCards.json");
        //Read JSON file
        Object obj = jsonParser.parse(reader);

        JSONArray JSONCardList = (JSONArray) obj;
            
        //Iterate over card array
        JSONCardList.forEach( card -> {
            listAvailableCards.add( parseCardObject((JSONObject) card) );
            System.out.println();
        });

        return listAvailableCards;
    }
 
     
 
    private static Card parseCardObject(JSONObject card) {
        //Get card object within list
        JSONObject cardJSONObject = (JSONObject) card.get("Card");
         
        //Get card name as String
        String name = (String) cardJSONObject.get("name");    
        System.out.println(name);
         
        //Get value as an int
        int value = Integer.parseInt((String) cardJSONObject.get("value"));  
        System.out.println(value);

        //Get priority as int
        int priority = Integer.parseInt((String) cardJSONObject.get("priority"));  
        System.out.println(priority);

        //Get minPlayers as int
        int minPlayers = Integer.parseInt((String) cardJSONObject.get("minPlayers"));  
        System.out.println(minPlayers);
         
        //Get unique as a Boolean
        boolean unique = Boolean.parseBoolean((String)cardJSONObject.get("unique"));    
        System.out.println(unique);

        Card cardObj = new Card();
        cardObj.name = name;
        cardObj.value = value;
        cardObj.priority = priority;
        cardObj.minPlayers = minPlayers;
        cardObj.unique = unique;

        return cardObj;

    }
}