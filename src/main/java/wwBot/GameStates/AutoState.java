package wwBot.GameStates;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.DayPhases.Auto.Day;
import wwBot.GameStates.DayPhases.Auto.FirstNight;
import wwBot.GameStates.DayPhases.Auto.Morning;
import wwBot.GameStates.DayPhases.Auto.Night;
import wwBot.Interfaces.Command;

//----------------------- ! WORK IN PROGRESS ! --------------------------------

//TODO: every morning set all surviving players.deathstate to ALIVE

public class AutoState extends MainState {

    public Day day = null;
    
      public Night night = null; 
      public Morning morning = null;
    
    public FirstNight firstNight = null;
    public DayPhase dayPhase = DayPhase.FIRST_NIGHT;

    AutoState(Game game) {
        super(game);
        registerStateCommands();

        // TODO: greet Players
        changeDayPhase(DayPhase.FIRST_NIGHT);
    }

    // --------------------- Day/Night - Cycle ----------------------------

    @Override
    public void changeDayPhase(DayPhase nextPhase) {
        loadGameLists();

        if (!checkIfGameEnds()) {
            // TODO: clear GameEndChecks
            // transitions to Night

            if (nextPhase == DayPhase.NORMAL_NIGHT) {

                setMuteAllPlayers(game.livingPlayers, true);
                createWerwolfChat();

                night = new Night(game);
                dayPhase = DayPhase.NORMAL_NIGHT;

                MessagesMain.onNightAuto(game);

                // transitions to Morning
            } else if (nextPhase == DayPhase.MORNING) {
                setMuteAllPlayers(game.livingPlayers, false);
                deleteWerwolfChat();

                //morning = new Morning(game);
                dayPhase = DayPhase.MORNING;

                MessagesMain.onMorningAuto(game);

                // transitions to Day
            } else if (nextPhase == DayPhase.DAY) {

                day = new Day(game);
                dayPhase = DayPhase.DAY;


                // transitions to 1st Night
            } else if (nextPhase == DayPhase.FIRST_NIGHT) {

                firstNight = new FirstNight(game);
                dayPhase = DayPhase.FIRST_NIGHT;

            }
        }

    }

    // loads the Commands available in this GameState into the map gameStateCommands
    private void registerStateCommands() {

        // ping testet ob der bot antwortet
        Command pingCommand = (event, parameters, msgChannel) -> {
            msgChannel.createMessage("Pong! Game").block();
        };
        gameStateCommands.put("ping", pingCommand);

        // zeigt die verfügbaren commands
        Command showCommandsCommand = (event, parameters, msgChannel) -> {
            // TODO: FILL
        };
        gameStateCommands.put("showCommands", showCommandsCommand);

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {
            // TODO: Fill
        };
        gameStateCommands.put("help", helpCommand);
        gameStateCommands.put("hilfe", helpCommand);

    }

    // TODO: implement kill system
    @Override
    public void killPlayer(Player victim, String causedByRole) {

        // kills player
        victim.role.alive = false;
        game.deadPlayers.add(victim);

        updateDeathChat();

        try {
            victim.user.asMember(game.server.getId()).block().edit(a -> a.setMute(true)).block();
        } catch (Exception e) {
        }

        loadGameLists();

        // reveals the players death and identity
        sendDeathMessage(victim, causedByRole);

        // calculates the consequences
        // checkConsequences(victim, causedByRole);
    }

    private void sendDeathMessage(Player player, String cause) {

        switch (cause) {
            case "Werwolf":
                MessagesMain.deathByWW(game, player);
            case "Hexe":
                MessagesMain.deathByMagic(game, player);
            case "Amor":
                MessagesMain.deathByLove(game, player);
            case "Jäger":
                MessagesMain.deathByGunshot(game, player);
            case "Dorfbewohner":
                MessagesMain.deathByLynchen(game, player);
            case "Märtyrerin":
                MessagesMain.deathBySacrifice(game, player);

            default:
                MessagesMain.deathByDefault(game, player);
        }
        Globals.printCard(player.role.name, game.mainChannel);
    }
}
