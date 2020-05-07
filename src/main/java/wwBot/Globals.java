package wwBot;

import java.util.ArrayList;
import java.util.List;

public class Globals {
    public static List<Card> listAvailableCards = new ArrayList<>();


    public static void loadGlobals() throws Exception {
        listAvailableCards = ReadJSONCard.readAvailableCards();
    }
    
}