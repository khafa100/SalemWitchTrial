import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

public class SWTGame implements Runnable
{
	private ArrayList<Player> playerList;
  private ArrayList<Player> waitingList;//players wanting to play again will be added back to the list
  

	SWTGame(ArrayList<Player> playerList,  ArrayList<Player> waitingList)
	{
		this.playerList=playerList;
    this.waitingList=waitingList;
	}

	public void run(){//main thread
		
	}
  private void shuffle(ArrayList<Player> playerList){//assigning random roles to players
  
  }
}
