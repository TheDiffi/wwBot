package wwBot;

public class CardHexe {
    boolean healingPotion = true;
    boolean deathPotion = true;
    Card specs;

    CardHexe() {
        specs = Globals.mapRegisteredCards.get("Hexe");
    }
}