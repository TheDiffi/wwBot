package wwBot.cards;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.GameStates.MainState.DeathState;
import wwBot.Interfaces.PrivateCommand;

public class RoleHexe extends Role {
    public boolean healUsed = false;
    public boolean poisonUsed = false;

    RoleHexe() {
        super("Hexe");
    }

    @Override
    public void execute(Game game, Player hexe) {
        var state = (AutoState) game.gameState;

        MessagesMain.callHexe(hexe, state.night.endangeredPlayers, game);

        PrivateCommand healCommand = (event, parameters, msgChannel) -> {

            if (parameters != null && parameters.get(0).equalsIgnoreCase("&heal")) {

                parameters.remove(0);
                var target = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

                if (target != null) {
                    if (target.role.deathState == DeathState.AT_RISK) {
                        // uses the ability to save the player
                        target.role.deathState = DeathState.SAVED;
                        healUsed = true;
                        MessagesMain.confirm(msgChannel);
                        return true;

                    } else {
                        MessagesMain.errorPlayerNotFound(msgChannel);
                        return false;
                    }

                } else {
                    return false;
                }

            } else {
                return false;
            }

        };
        game.addPrivateCommand(hexe.user.getId(), healCommand);

        PrivateCommand poisonCommand = (event, parameters, msgChannel) -> {

            if (parameters != null && parameters.get(0).equalsIgnoreCase("&heal")) {

                parameters.remove(0);
                var target = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

                if (target != null) {
                    target.role.deathState = DeathState.AT_RISK;
                    poisonUsed = true;
                    MessagesMain.confirm(msgChannel);
                    return true;

                } else {
                    return false;
                }

            } else {
                return false;
            }

        };
        game.addPrivateCommand(hexe.user.getId(), poisonCommand);

    }
}