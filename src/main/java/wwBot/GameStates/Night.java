package wwBot.GameStates;

import java.util.Map;
import java.util.TreeMap;

import wwBot.Command;
import wwBot.Game;

public class Night {
    public Night(Game game) {
	}

	public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);


}
