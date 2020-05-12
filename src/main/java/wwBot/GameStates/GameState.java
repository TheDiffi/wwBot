package wwBot.GameStates;

import java.util.Map;
import java.util.TreeMap;

import wwBot.Command;
import wwBot.Game;

public class GameState {
    public Map<String, Command> gameStateCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    Game game;

    protected GameState (Game game2){
        game = game2;
        
    }

	public void exit() {
	}


}