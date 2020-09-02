package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.MainState.DeathState;
import wwBot.Interfaces.PrivateCommand;

public class RoleSäufer extends Role {
    public Player drinkingAt;

    RoleSäufer() {
        super("Säufer");

    }

    @Override
    public void execute(Game game, Player säufer) {
        MessagesMain.callSäufer(säufer);
        säufer.user.getPrivateChannel().block().createMessage("TEST");

        PrivateCommand säuferCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null && (drinkingAt == null || !player.name.equals(drinkingAt.name))) {
                drinkingAt = player;
                säufer.role.deathState = DeathState.PROTECTED;

                setDone(game, "Säufer");
                return true;

            } else if (drinkingAt != null && player.name.equals(drinkingAt.name)) {
                MessagesMain.errorChoseIdenticalPlayer(msgChannel);
                return false;
                
            } else {
                return false;
            }
            
        };
        game.addPrivateCommand(säufer.user.getId(), säuferCommand);
    }
}