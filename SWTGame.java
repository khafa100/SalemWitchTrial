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

	SWTGame(ArrayList<Player> playerList,  ArrayList<Player> waitingList){
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

		
		for(Player p: playerList){
			ClientHandler handler=new ClientHandler(p,playerList,waitingList,sync);
			Thread theThread = new Thread(handler);
			theThread.start();
		}
		
		game:
		while(sync.currentStage<6){
			try{
			  //Day Server Stage
			  System.out.println("Going into day chat server stage...");
				Thread.sleep(120000);//sleep for 2 min
				sync.endStage=true;
				while(true){
					if(sync.threadDone==sync.mafiaMember+sync.innocentMember){
						System.out.println("Proceeding into next stage...");
						sync.mostVote=-1;
						sync.vote=new int[]{0,0,0,0,0,0,0,0,0};
						sync.currentStage++;
						sync.threadDone=0;
						break;
					}
					else{
					  System.out.println("Thread done: "+sync.threadDone);
  					System.out.println("Mafia left: "+sync.mafiaMember);
  					System.out.println("Innocent left: "+sync.innocentMember);
						System.out.println("Waiting for all players to finish");
						Thread.sleep(5000);
					}
				}
				sync.endStage=false;
				
        //Lynching Stage
				System.out.println("Asking for nominations...");
				while(true){
					if(sync.threadDone==sync.mafiaMember+sync.innocentMember){
						int half= (sync.mafiaMember+sync.innocentMember)/2;
						for (int x=0;x<sync.mafiaMember+sync.innocentMember;++x){
							if(sync.vote[x]>half){
								sync.mostVote=x;
							}
						}
						int gameOver=0;
						if(sync.mostVote!=-1){
							Player p= playerList.get(sync.mostVote);
							if(p.getRole()>5){
								sync.mafiaMember--;
							}
							else{
								sync.innocentMember--;
							}
							p.setAlive(false);
							gameOver=isGameOver();
						}
						System.out.println("Proceeding into next stage...");
						if(gameOver==0){
							sync.currentStage++;
						}
						else{
							sync.currentStage=6;
							break game;
						}
						sync.threadDone=0;
						break;
					}
					else{
						System.out.println("Thread done: "+sync.threadDone);
						System.out.println("Mafia left: "+sync.mafiaMember);
						System.out.println("Innocent left: "+sync.innocentMember);
						System.out.println("Waiting for all players to finish");
						Thread.sleep(5000);
					}
				}

        //Giving results of the vote and starting Night Server Stage
				System.out.println("Going into night chat server stage...");
				Thread.sleep(60000);//sleep for 1 min
				sync.endStage=true;
				while(true){
					if(sync.threadDone==sync.mafiaMember+sync.innocentMember){
						System.out.println("Proceeding into next stage...");
						sync.currentStage++;
						sync.threadDone=0;
						break;
					}
					else{
					  System.out.println("Thread done: "+sync.threadDone);
						System.out.println("Mafia left: "+sync.mafiaMember);
						System.out.println("Innocent left: "+sync.innocentMember);
						System.out.println("Waiting for all players to finish");
						Thread.sleep(5000);
					}
				}
				sync.endStage=false;

        //Asking actions from users
				System.out.println("Going into night action mode...");
				sync.vote=new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1};
				while(true){
					if(sync.threadDone==sync.mafiaMember+sync.innocentMember){
						System.out.println("Processing all the actions...");
						int mafiaKill=-1, vigilanteKill=-1, lookoutNum=-1, sheriffNum=0;
						if (sync.vote[8]!=-1){//godfater and doctor
							if(sync.vote[0]!=sync.vote[8]){
								mafiaKill=sync.vote[8];
							}
							else{
								Player p= playerList.get(sync.vote[8]);
								broadcastPlayers("0\nLast night "+p.getUsername()+" was attacked. Fortunately, he was saved by the Doctor.");
							}
						}
						else if(sync.vote[7]!=sync.vote[0]){//mafiaso and doctor
							mafiaKill=sync.vote[7];
						}
						else{
							Player p= playerList.get(sync.vote[7]);
							broadcastPlayers("0\nLast night "+p.getUsername()+" was attacked. Fortunately, he was saved by the Doctor.");
						}
						if (sync.vote[5]!=sync.vote[0]){//vigilante and doctor
							vigilanteKill=sync.vote[5];
						}
						else{
							Player p= playerList.get(sync.vote[7]);
							broadcastPlayers("0\nLast night "+p.getUsername()+" was attacked. Fortunately, he was saved by the Doctor.");
						}
						for(int x=0; x<9;++x){//lookout
							if(sync.vote[4]==sync.vote[x]&& x!=4){
								lookoutNum=x;
							}
						}

						if (sync.vote[3]==sync.vote[6]){//sheriff and framer
							sheriffNum=1;
						}
						else{// sheriff only
							int v=playerList.get(sync.vote[3]).getRole();
							if(v==6 || v==7 ){
								sheriffNum=1;
							}
						}

						if(mafiaKill!=-1){
							Player p= playerList.get(mafiaKill);
							broadcastPlayers("0\nLast night, "+p.getUsername()+" was attacked. Unfortunately, no one was there to save him.");
							broadcastPlayers("0\nTheir role was "+roleArray[p.getRole()]+".");

							if(p.getRole()>5){
								sync.mafiaMember--;
							}
							else{
								sync.innocentMember--;
							}
							p.setAlive(false);
						}
						if(vigilanteKill!=-1){
							Player p= playerList.get(vigilanteKill);
							broadcastPlayers("0\nLast night, "+p.getUsername()+" was attacked. Unfortunately, no one was there to save him.");
							broadcastPlayers("0\nTheir role was "+roleArray[p.getRole()]+".");

							if(p.getRole()>5){
								sync.mafiaMember--;
							}
							else{
								sync.innocentMember--;
							}
							p.setAlive(false);
						}
						int gameOver=isGameOver();
						if(gameOver==0){
							sync.vote[0]=mafiaKill;
							sync.vote[1]=vigilanteKill;
							sync.vote[2]=lookoutNum;
							sync.vote[3]=sheriffNum;
							sync.currentStage++;
						}
						else{
							sync.currentStage=6;
							break game;
						}
						sync.threadDone=0;
						break;
					}
					else{
					  System.out.println("Thread done: "+sync.threadDone);
						System.out.println("Mafia left: "+sync.mafiaMember);
						System.out.println("Innocent left: "+sync.innocentMember);
						System.out.println("Waiting for all players to finish");
						Thread.sleep(5000);
					}
				}

        //Giving out information
				System.out.println("Giving information to Sheriff and Lookout...");
				while(true){
					if(sync.threadDone==sync.mafiaMember+sync.innocentMember){
						System.out.println("Proceeding into next stage...");
						sync.currentStage=1;
						sync.threadDone=0;
						break;
					}
					else{
					  System.out.println("Thread done: "+sync.threadDone);
						System.out.println("Mafia left: "+sync.mafiaMember);
						System.out.println("Innocent left: "+sync.innocentMember);
						System.out.println("Waiting for all players to finish");
						Thread.sleep(1000);
					}
				}

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
			player.setAlive(true);
		}
		Collections.shuffle(playerList);
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
		broadcastPlayers("0\n-----------------------------------------------------------------");
		broadcastPlayers(alive+dead);
		broadcastPlayers("0\n-----------------------------------------------------------------");
	}
	private void broadcastPlayers(String s){
		for(Player p: playerList)
		  p.sendMessage(s);
	}
	private int isGameOver(){//send the updated board and check for end conditions
		broadcastPlayers("0\nUpdating the player list...");
		printBoard();
		if (sync.mafiaMember>sync.innocentMember){
			broadcastPlayers("0\nThe game is over since there are more Mafia members now.");
			return 2;
		}
		else if (sync.mafiaMember==0){
			broadcastPlayers("0\nThe game is over since all Mafia members are dead.");
			return 1;
		}
		else{
			return 0;
		}
	}
}
