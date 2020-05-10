package wwBot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;
import wwBot.GameStates.GameState;
import wwBot.GameStates.LobbyState;
import wwBot.GameStates.MainGameState;

public class Game {
    public Map<String, Command> gameCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    public Map<Snowflake, Player> listPlayer = new HashMap<Snowflake,Player>();
    public GameState currentGameState;
    public Snowflake runningInServer;


    Game(Snowflake snowflakeServer){

        runningInServer = snowflakeServer;  
        registerGameCommands();
        //initializes the first game State
        currentGameState = new LobbyState(this);

    }



    public void handleCommands(MessageCreateEvent event){

        String messageContent = event.getMessage().getContent().orElse("");
        List<String> parameters = new LinkedList<>(Arrays.asList(messageContent.split(" ")));
        var requestedCommand = parameters.remove(0);
        requestedCommand = requestedCommand.substring(1);


       var foundCommand = gameCommands.get(requestedCommand);
       if(foundCommand != null){
           foundCommand.execute(event, parameters);
        } else {
            foundCommand = currentGameState.gameStateCommands.get(requestedCommand);
            if(foundCommand != null){
                foundCommand.execute(event, parameters);
            }else{
                event.getMessage().getChannel().block().createMessage("Command Not Found").block();
            }
       }
       




    }

    //loads the Commands available throughout the game into the map gameCommands 
    private void registerGameCommands(){

        // speichert den Prefix in einer Variable
        var prefix = "&";
        

        Command showCardCommand = (event, parameters) -> {
            var channel = event.getMessage().getChannel().block();
            String cardName = parameters.get(0);

            Globals.printCard(cardName, channel);
        };


        gameCommands.put("showCard",showCardCommand);
            
        //basically !help
        Command explainCommand =(event, parameters) -> {
            var channel = event.getMessage().getChannel().block();

            //TODO: create a message builder or embed with all the info

            channel.createMessage("Choose a Deck with **" + prefix + "chooseCustomDeck** or ++"
                            + "chooseAlgorithmDeck**").block();
                    channel.createMessage("If you have not created a Deck jet, try **" + prefix
                            + "buildDeck** to let the Algorithm build a Deck for you or try **" + prefix
                            + "addCard** to add a Card to your Custom Deck").block();
 
        };
        gameCommands.put("explain", explainCommand);
        


    }



	public void changeGameState(MainGameState nextGameState) {
        currentGameState.exit();
        currentGameState = nextGameState;
	}



	
    
}