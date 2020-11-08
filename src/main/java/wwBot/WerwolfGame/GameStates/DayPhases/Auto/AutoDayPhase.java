package wwBot.WerwolfGame.GameStates.DayPhases.Auto;

import java.util.Map;
import java.util.TreeMap;

import wwBot.Interfaces.Command;

public class AutoDayPhase {
	public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);

	public void changeNightPhase() {
	}

}