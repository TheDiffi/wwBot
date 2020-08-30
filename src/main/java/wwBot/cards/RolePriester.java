package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.Interfaces.PrivateCommand;

public class RolePriester extends Role {
    public boolean usedAbility = false;
    public Player protectedPlayer;
    public boolean abilityVanished = false;

    RolePriester() {
        super("Priester");
    }

    @Override
    public void execute(Game game, Player priester) {
        /// TODO: send mssg
        priester.user.getPrivateChannel().block().createMessage("TEST");

        PrivateCommand priesterCommand = (event, parameters, msgChannel) -> {
            if (parameters.size() != 1) {
                MessagesMain.errorWrongAnswer(msgChannel);
                return false;

                // NO
            } else if (parameters.get(0).equalsIgnoreCase("no")) {
                MessagesMain.sendApproval(msgChannel);
                
                setDone(game, "Priester");

                return true;

                // YES: if the priest chooses to use his ability he gets granted access to the "bless" Command
            } else if (parameters.get(0).equalsIgnoreCase("yes")) {
                MessagesMain.sendApproval(msgChannel);
                
                //this Command saves the priests player of choice as the protected player
                PrivateCommand blessCommand = (event2, parameters2, msgChannel2) -> {
                    var player = Globals.privateCommandPlayerFinder(event2, parameters2, msgChannel2, game);
        
                    if (player != null) {
                        protectedPlayer = player;
                        usedAbility = true;
        
                        setDone(game, "Priester");
                        return true;
        
                    } else {
                        return false;
                    }
        
                };
                game.addPrivateCommand(priester.user.getId(), blessCommand);

                return true;

            } else {
                MessagesMain.errorWrongAnswer(msgChannel);
                return false;
            }
        };
        game.addPrivateCommand(priester.user.getId(), priesterCommand);


    }
}


