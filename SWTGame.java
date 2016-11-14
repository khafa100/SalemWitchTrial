import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class SWTGame implements Runnable
{
	private ArrayList<Player> playerList;
	private ArrayList<Player> waitingList;//players wanting to play again will be added back to the list
	private String roleArray[]= new String[]{"Doctor","Civilian","Civilian","Sheriff","Lookout","Vigilante","Framer","Mafiaso","Godfather"};
	private String descriptionArray[]= new String[]{"You heal one person (including yourself) each night preventing them from dying.",
	"You are a part of the crowd that can vote to lynch people each round.",
	"You are a part of the crowd that can vote to lynch people each round.",
	"You investigate one person each night to find out if the target is a member of the Mafia, except for the Godfather and Vigilante. Beware for Framer can fool you.",
	"You watch one person each night to see who visits them.",
	"You kill someone each night to save the town.",
	"You choose someone to frame at night. If the target is investigated, they will appear to be a member of the Mafia. If Mafioso dies, you will take their position.",
	"You carry out Godfather’s orders. If Godfather dies, you will take their position.",
	"You will kill someone each night if the Mafioso is dead. Else, the Mafioso will kill the target for you."};
	private PlayerSync sync=new PlayerSync();

	SWTGame(ArrayList<Player> playerList,  ArrayList<Player> waitingList)
	{
		this.playerList=playerList;
		this.waitingList=waitingList;
	}

	public void run(){//main thread
		System.out.println("Assigning a role to each player...");
		shuffle(playerList);//will shuffle the list and assign the roles randomly

		System.out.println("Telling players their respective roles...");
		for(Player p: playerList){
			p.sendMessage("0\nYour role is "+roleArray[p.getRole()]+".");
			p.sendMessage("0\n"+descriptionArray[p.getRole()]);
		}

		//Starting game

		System.out.println("Sending out the current dead/alive state...");
		printBoard();

		System.out.println("Going into chat server mode...");
		broadcastPlayers("2");
		broadcastPlayers("You can now chat with all other players for two minutes.");
		for(Player p: playerList){
			ClientHandler handler=new ClientHandler(p,playerList,sync);
			Thread theThread = new Thread(handler);
			theThread.start();
		}
		while(sync.currentStage<7){
			try{
				Thread.sleep(120000);//sleep for 2 min
				sync.endStage=true;
				while(true){
					if(sync.threadDone==sync.mafiaMember+sync.innocentMember){
						System.out.println("Proceeding into next stage...");
						sync.currentStage++;
						sync.threadDone=0;
						break;
					}
					else{
						System.out.println("Waiting for all players to finish");
						Thread.sleep(1000);
					}
				}
				System.out.println("Asking for nominations...");
				while(true){
					if(sync.threadDone==sync.mafiaMember+sync.innocentMember){
						int half= (sync.mafiaMember+sync.innocentMember)/2;
						for (int x: sync.vote){
							if(sync.vote[x]>half){
								sync.mostVote=x;
							}
						}
						System.out.println("Proceeding into next stage...");
						sync.currentStage++;
						sync.threadDone=0;
						sync.vote=new int[]{0,0,0,0,0,0,0,0,0};
						break;
					}
					else{
						System.out.println("Waiting for all players to finish");
						Thread.sleep(2000);
					}
				}
				System.out.println("Asking whether to lynch or not...");
			}
			catch(InterruptedException ie){
				System.out.println(ie.getMessage());
			}



		}

	}
	private void shuffle(ArrayList<Player> playerList){//assigning random roles to players
		Collections.shuffle(playerList);
		for(int x=0; x<playerList.size();++x){
			Player player=playerList.get(x);
			player.setRole(x);
		}
		Collections.shuffle(playerList);
	}
	private int isGameOver(){//0=game continues, 1=Innocents win, 2=Mafia win
		if (sync.mafiaMember>sync.innocentMember)
		return 2;
		else if (sync.mafiaMember==0)
		return 1;
		else
		return 0;
	}
	private void printBoard(){
		String alive="0\nAlive:  ";
		String dead="\n0\nDead:  ";
		for(Player p: playerList){
			if(p.isAlive()){
				alive+=p.getUsername()+", ";
			}
			else{
				dead+=p.getUsername()+" ("+roleArray[p.getRole()]+"), ";
			}
		}
		alive= alive.substring(0,alive.length()-2);//deleting last instance of ", "
		dead= dead.substring(0,dead.length()-2);//deleting last instance of ", "
		broadcastPlayers(alive+dead);
	}
	private void broadcastPlayers(String s){
		for(Player p: playerList)
		p.sendMessage(s);
	}
}
