package wwBot.GameStates;

import java.util.Map;
import java.util.TreeMap;

import wwBot.Command;
import wwBot.Game;

public class GameState {
    public Map<String, Command> gameStateCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    Game game;

    GameState (Game incomingGame){
        game = incomingGame;
        
    }

	public void exit() {
	}


}