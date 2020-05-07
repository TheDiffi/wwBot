/* package wwBot;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WriteJSONExample
{
    @SuppressWarnings("unchecked")
    public static void main( String[] args )
    {
        //First Employee
        JSONObject werwolfDetails = new JSONObject();
        werwolfDetails.put("name", "Werwolf");
        werwolfDetails.put("value", "-6");
        werwolfDetails.put("unique", "false");
        werwolfDetails.put("priority", "100");
         
        JSONObject werwolfCard = new JSONObject(); 
        werwolfCard.put("Card", werwolfDetails);
         
        //Second Employee
        JSONObject seherDetails = new JSONObject();
        seherDetails.put("name", "Seher");
        seherDetails.put("value", "-8");
        seherDetails.put("unique", "true");
        seherDetails.put("priority", "80");
         
        JSONObject seherCard = new JSONObject(); 
        seherCard.put("Card", seherDetails);
         
        //Add employees to list
        JSONArray cardList = new JSONArray();
        cardList.add(werwolfCard);
        cardList.add(seherCard);
         
        //Write JSON file
        try (FileWriter file = new FileWriter("availableCards.json")) {
 
            file.write(cardList.toJSONString());
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} */