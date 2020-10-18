package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.GameStates.MainState.DeathState;
import wwBot.Interfaces.PrivateCommand;

public class RoleSäufer extends Role {
    public Player drinkingAt;

    RoleSäufer() {
        super("Säufer");

    }

    @Override
    public void executePreWW(Player säufer,Game game, AutoState state) {
        MessagesMain.callSäufer(säufer);

        PrivateCommand säuferCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null && (drinkingAt == null || !player.name.equals(drinkingAt.name))) {
                drinkingAt = player;
                säufer.role.deathDetails.deathState = DeathState.PROTECTED;
                MessagesMain.confirm(msgChannel);
                state.setDoneNight(säufer);
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