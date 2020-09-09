package wwBot.GameStates;

import java.util.ArrayList;
import java.util.List;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.DayPhases.Auto.AutoDayPhase;
import wwBot.GameStates.DayPhases.Auto.Day;
import wwBot.GameStates.DayPhases.Auto.FirstNight;
import wwBot.GameStates.DayPhases.Auto.Morning;
import wwBot.GameStates.DayPhases.Auto.Night;
import wwBot.Interfaces.Command;
import wwBot.cards.Role;
import wwBot.cards.RoleDoppelgängerin;
import wwBot.cards.RoleHarterBursche;
import wwBot.cards.RolePriester;
import wwBot.cards.RoleWerwolf;

//----------------------- ! WORK IN PROGRESS ! --------------------------------

//TODO: every morning set all surviving players.deathstate to ALIVE
//TODO: refractore DayPhases
//TODO: setDone() an automatic initiate role

public class AutoState extends MainState {

    public DayPhase dayPhase = DayPhase.FIRST_NIGHT;
    public AutoDayPhase aDayPhase = null;

    // TODO: mby make a enum out of this
    public boolean wwEnraged = false;
    public boolean wwInfected = false;

    public List<Player> pending = new ArrayList<>();

    AutoState(Game game) {
        super(game);
        registerStateCommands();

        // TODO: greet Players
        changeDayPhase(DayPhase.FIRST_NIGHT);
    }

    // --------------------- Day/Night - Cycle ----------------------------

    @Override
    public void changeDayPhase(DayPhase nextPhase) {
        updateGameLists();

        if (!checkIfGameEnds()) {
            // TODO: clear GameEndChecks
            // transitions to Night

            if (nextPhase == DayPhase.NORMAL_NIGHT) {

                // TODO: Harter-Bursche

                setMuteAllPlayers(game.livingPlayers, true);

                aDayPhase = new Night(game);
                dayPhase = DayPhase.NORMAL_NIGHT;

                MessagesMain.onNightAuto(game);

                // transitions to Morning
            } else if (nextPhase == DayPhase.MORNING) {
                setMuteAllPlayers(game.livingPlayers, false);
                deleteWerwolfChat();

                aDayPhase = new Morning(game);
                dayPhase = DayPhase.MORNING;

                MessagesMain.onMorningAuto(game);

                // transitions to Day
            } else if (nextPhase == DayPhase.DAY) {
                wwEnraged = false;
                wwInfected = false;

                aDayPhase = new Day(game);
                dayPhase = DayPhase.DAY;

                // transitions to 1st Night
            } else if (nextPhase == DayPhase.FIRST_NIGHT) {
                createWerwolfChat();

                aDayPhase = new FirstNight(game);
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
    // survivalCheck
    // TODO: kill
    // TODO: calculate Consequences
    @Override
    public void killPlayer(Player victim, String cause) {

        if (checkIfDies(victim, cause)) {
            // kills player
            killSwitch(victim);

            // reveals the players death and identity
            sendDeathMessage(victim, cause);

            checkConsequences(victim, cause);
        }

    }

    // checks the conditions if the player dies
    @Override
    public boolean checkIfDies(Player victim, String cause) {
        var dies = true;

        if (savedByPriester(victim)) {
            dies = false;

        } else {

            // Verfluchter
            if (victim.role.name.equalsIgnoreCase("Verfluchter") && cause.equalsIgnoreCase("Werwolf")) {
                dies = false;
                victim.role = Role.createRole("Werwolf");
                MessagesMain.verfluchtenMutation(game);

            }

            // Prinz
            else if (victim.role.name.equalsIgnoreCase("Prinz") && cause.equalsIgnoreCase("Dorfbewohner")) {
                dies = false;
                MessagesMain.prinzSurvives(game);

            }

            // Harter-Bursche
            else if (victim.role.name.equalsIgnoreCase("Harter-Bursche") && cause.equalsIgnoreCase("Werwolf")) {
                var bursche = (RoleHarterBursche) mapExistingRoles.get("Harter-Bursche").get(0).role;
                dies = false;
                bursche.isDying = true;
                MessagesMain.harterBurscheSurvives(game);

            }

        }

        return dies;
    }

    private boolean savedByPriester(Player victim) {
        // Priester
        if (mapExistingRoles.containsKey("Priester")) {
            var priester = (RolePriester) mapExistingRoles.get("Priester").get(0).role;

            if (priester.abilityActive && victim == priester.protectedPlayer) {
                MessagesMain.savedByPriester(victim, game);
                priester.abilityActive = false;
                return true;

            }
        }
        return false;
    }

    private void killSwitch(Player victim) {
        victim.role.deathDetails.alive = false;
        game.deadPlayers.add(victim);

        updateDeathChat();
        updateGameLists();

        try {
            victim.user.asMember(game.server.getId()).block().edit(a -> a.setMute(true)).block();
        } catch (Exception e) {
        }
    }

    private void checkConsequences(Player victim, String cause) {

        // Wolfsjunges
        if (victim.role.name.equals("Werwolf") && ((RoleWerwolf) victim.role).isJunges) {
            MessagesMain.onWolfsjungesDeath(game);
            wwEnraged = true;
        }

        // Seher Lehrling
        if (victim.role.name.equals("Seher") && game.gameState.mapExistingRoles.containsKey("Seher-Lehrling")) {
            var lehrling = game.gameState.mapExistingRoles.get("Doppelgängerin").get(0);
            MessagesMain.onSeherlehrlingPromotion(game, victim);
            lehrling.role = Role.createRole("Seher");

        }

        // Aussätzige
        if (victim.role.name.equals("Aussätzige") && cause.equalsIgnoreCase("Werwolf")) {
            MessagesMain.onAussätzigeDeath(game);
            wwInfected = true;

        }
        // TODO: Jäger
        if (victim.role.name.equals("Jäger")) {
            MessagesMain.onJägerDeath(game, victim);

        }

        // Doppelgängerin
        if (mapExistingRoles.containsKey("Doppelgängerin")) {
            var dp = game.gameState.mapExistingRoles.get("Doppelgängerin").get(0);

            if (((RoleDoppelgängerin) dp.role).boundTo == victim) {
                dp.role = Role.createRole(victim.role.name);
                MessagesMain.onDoppelgängerinTransformation(game, dp, victim);

            }
        }

    }

    private void sendDeathMessage(Player player, String cause) {

        switch (cause) {
            case "Werwolf":
                MessagesMain.deathByWW(game, player);
            case "Hexe":
                MessagesMain.deathByMagic(game, player);
            case "Magier":
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

    /*
     * public void setDone(Game game, String role) { // sets this roles state to
     * done
     * 
     * if (dayPhase == DayPhase.FIRST_NIGHT) { firstNight.endChecks.replace(role,
     * true); firstNight.endNightCheck();
     * 
     * } else if (dayPhase == DayPhase.NORMAL_NIGHT) { night.endChecks.replace(role,
     * true); night.endNightCheck();
     * 
     * } else { game.mainChannel.createMessage("ERROR in Role.setDone"); } }
     */

    public void setPending(Player player) {
        pending.add(player);
    }

    public void setDone(Player player) {
        pending.remove(player);
        endNightCheck();
    }

    public void endNightCheck() {

        if (pending.isEmpty()) {
            aDayPhase.changeNightPhase();
        }
    }
}
