package wwBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Globals {
    public static Map<String,Card> mapAvailableCards;


    public static void loadGlobals() throws Exception {
        mapAvailableCards = ReadJSONCard.readAvailableCards();
    }
    
}