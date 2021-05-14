package wwBot.WerwolfGame.cards;

import wwBot.Globals;
import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;
import wwBot.WerwolfGame.GameStates.MainState.DeathState;

public class RoleSäufer extends Role {
    public Player drinkingAt;

    RoleSäufer() {
        super("Säufer");

    }

    @Override
    public void executePreWW(Player säufer, Game game, AutoState state) {
        MessagesWW.callSäufer(säufer);

        PrivateCommand säuferCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null) {

                if (drinkingAt != null && player.name.equals(drinkingAt.name)) {
                    MessagesWW.errorChoseIdenticalPlayer(msgChannel);
                    return false;
                    //TODO: test if works
                } else if (drinkingAt != null && player.equals(säufer)){
                    MessagesWW.errorChoseIdenticalPlayer(msgChannel);
                    return false;
                }
                else if (drinkingAt == null || !player.name.equals(drinkingAt.name)) {
                    drinkingAt = player;
                    säufer.role.deathDetails.deathState = DeathState.PROTECTED;
                    MessagesWW.confirm(msgChannel);
                    state.setDoneNight(säufer);
                    return true;

                }
                return false;
            } else {
                return false;
            }

        };
        game.addPrivateCommand(säufer.user.getId(), säuferCommand);
    }
}