package wwBot.WerwolfGame.cards;

public class RoleWerwolf extends Role {
    // TODO: rethink
    public boolean isJunges;

    RoleWerwolf(boolean junges) {
        super(junges ? "Wolfsjunges" : "Werwolf");
        isJunges = junges;
    }

}