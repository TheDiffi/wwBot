package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.GameStates.MainState.DeathState;
import wwBot.Interfaces.PrivateCommand;


//not sure if this will work / Magier & Hexe in one role
public class RoleZauberer extends Role {
    public boolean healUsed = false;
    public boolean poisonUsed = false;

    RoleZauberer(String role) {
        super(role);
    }

    @Override
    public void execute(Game game, Player zauberer) {
        var state = (AutoState) game.gameState;

        MessagesMain.callZauberer(zauberer, this, state.night.endangeredPlayers, game);

        if (!healUsed) {
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
            game.addPrivateCommand(zauberer.user.getId(), healCommand);
        }

        if (!poisonUsed) {
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
            game.addPrivateCommand(zauberer.user.getId(), poisonCommand);

        }

        PrivateCommand poisonCommand = (event, parameters, msgChannel) -> {

            if (parameters != null && parameters.get(0).equalsIgnoreCase("&continue")) {

                MessagesMain.confirm(msgChannel);
                
                setDone(game, zauberer.name);

                return true;

            } else {
                return false;
            }

        };
        game.addPrivateCommand(zauberer.user.getId(), poisonCommand);
    }
}