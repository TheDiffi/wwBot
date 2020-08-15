package wwBot.GameStates;

import wwBot.Game;
import wwBot.GameStates.DayPhases.DaySemi;
import wwBot.GameStates.DayPhases.FirstNight;
import wwBot.GameStates.DayPhases.FirstNightSemi;
import wwBot.GameStates.DayPhases.MorningSemi;
import wwBot.GameStates.DayPhases.NightSemi;
import wwBot.Interfaces.Command;

//----------------------- ! WORK IN PROGRESS ! --------------------------------

public class AutoState extends MainState {

    public Day day = null;
	public Night night = null;
	public Morning morning = null;
	public FirstNight firstNight = null;
	public DayPhase dayPhase = DayPhase.FIRST_NIGHT;
    

    AutoState(Game game) {
        super(game);
        registerStateCommands();

        //TODO: greet Players
        changeDayPhase(DayPhase.FIRST_NIGHT);
    }


        // --------------------- Day/Night - Cycle ----------------------------

    @Override
	public void changeDayPhase(DayPhase nextPhase) {
        loadGameLists();
        //TODO: clear GameEndChecks
		// transitions to Night
		if (nextPhase == DayPhase.NORMAL_NIGHT) {
			checkIfGameEnds();
			setMuteAllPlayers(game.livingPlayers, true);
			createWerwolfChat();
			
			night = new NightSemi(game);
			dayPhase = DayPhase.NORMAL_NIGHT;

			// transitions to Morning
		} else if (nextPhase == DayPhase.MORNING) {
			setMuteAllPlayers(game.livingPlayers, false);
			deleteWerwolfChat();
			
			morning = new MorningSemi(game);
			dayPhase = DayPhase.MORNING;

			// transitions to Day
		} else if (nextPhase == DayPhase.DAY) {
			checkIfGameEnds();
			
			day = new DaySemi(game);
			dayPhase = DayPhase.DAY;

			// transitions to 1st Night
		}else if (nextPhase == DayPhase.FIRST_NIGHT) {
			
			firstNight = new FirstNightSemi(game);
			dayPhase = DayPhase.FIRST_NIGHT;
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
            //TODO: FILL
        };
        gameStateCommands.put("showCommands", showCommandsCommand);

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {
            //TODO: Fill
        };
        gameStateCommands.put("help", helpCommand);
        gameStateCommands.put("hilfe", helpCommand);

    }

}
