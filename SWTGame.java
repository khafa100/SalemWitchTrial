/**
* SWTGame.java
*
* This thread is run everytime the server has 9 players in the waitingList
* This will encompass one game and end when the game is over
*/


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class SWTGame implements Runnable
{
	private ArrayList<Player> playerList;// list of players
	private String roleArray[]= new String[]{"Doctor","Civilian","Civilian","Sheriff","Lookout","Vigilante","Framer","Mafiaso","Godfather"};
	private PlayerSync sync=new PlayerSync();//class to be shared between all threads

	SWTGame(ArrayList<Player> playerList){
		this.playerList=playerList;
	}
	public void run(){//main thread
		System.out.println("Assigning a role to each player...");
		shuffle(playerList);//will shuffle the list and assign the roles randomly

		//Starting game
		System.out.println("Starting game...");
		for(Player p: playerList){// running threads for each client
			ClientHandler handler=new ClientHandler(p,playerList,sync);
			Thread theThread = new Thread(handler);
			theThread.start();
		}
		
		game:
		while(sync.currentStage<6){//This game loop will end when ending condition is met
			try{
			  //Day Server Stage
			  System.out.println("Going into day chat server stage...");
				Thread.sleep(120000);//sleep for 2 min
				sync.endStage=true;//notify other threads to end stage
				while(true){
					if(sync.threadDone==sync.mafiaMember+sync.innocentMember){//if all other threads are done
						System.out.println("Proceeding into next stage...");
						sync.mostVote=-1;
						sync.currentStage++;//move to next stage
						sync.threadDone=0;
						break;
					}
					else{
					  System.out.println("Thread done: "+sync.threadDone);
  					System.out.println("Mafia left: "+sync.mafiaMember);
  					System.out.println("Innocent left: "+sync.innocentMember);
						System.out.println("Waiting for all players to finish");
						Thread.sleep(5000);//sleep for 5 sec
					}
				}
				sync.endStage=false;
				
        //Lynching Stage
				System.out.println("Asking for nominations...");
				sync.vote=new int[]{0,0,0,0,0,0,0,0,0};
				while(true){
					if(sync.threadDone==sync.mafiaMember+sync.innocentMember){//if other threads are done
						int half= (sync.mafiaMember+sync.innocentMember)/2;
						for (int x=0;x<sync.mafiaMember+sync.innocentMember;++x){
							if(sync.vote[x]>half){
								sync.mostVote=x;//try to find the player who got most vote
							}
						}
						if(sync.mostVote!=-1){//if someone is lynched update the values
							Player p= playerList.get(sync.mostVote);
							if(p.getRole()>5){
								sync.mafiaMember--;
							}
							else{
								sync.innocentMember--;
							}
							p.setAlive(false);
						}
						System.out.println("Proceeding into next stage...");
						if(isGameOver()){//if end conditions are met
							sync.currentStage=6;//tell all threads to end
							break game;//end game
						}
						else{
							sync.currentStage++;
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
					if(sync.threadDone==sync.mafiaMember+sync.innocentMember){//if all other threads are done
						System.out.println("Proceeding into next stage...");
						sync.currentStage++;//move on to next stage
						sync.threadDone=0;
						break;
					}
					else{//else wait 5 sec before trying again
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
						int mafiaKill=-1, vigilanteKill=-1, lookoutNum=-1, sheriffNum=-1;
						if (sync.vote[8]!=-1){//if godfather chose to kill
						  Player p= playerList.get(sync.vote[8]);
							if(sync.vote[0]!=sync.vote[8]){//if doctor didn't chose the same target
								mafiaKill=sync.vote[8];
							  broadcastPlayers("0\nLast night, "+p.getUsername()+" was attacked. Unfortunately, no one was there to save him.");
							  broadcastPlayers("0\nTheir role was "+roleArray[p.getRole()]+".");
							//updating values
							  if(p.getRole()>5){
								  sync.mafiaMember--;
							  }
							  else{
								  sync.innocentMember--;
							  }
							  p.setAlive(false);
							}
							else{//doctor and godfather had same target
								broadcastPlayers("0\nLast night, "+p.getUsername()+" was attacked. Fortunately, he was saved by the Doctor.");
							}
						}
						else if(sync.vote[7]!=-1){//if godfather didn't choose to kill and mafia chose to kill
						  Player p= playerList.get(sync.vote[7]);
							if(sync.vote[7]!=sync.vote[0]){//check again with the doctor's choice
							  mafiaKill=sync.vote[7];
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
							else{
							  broadcastPlayers("0\nLast night, "+p.getUsername()+" was attacked. Fortunately, he was saved by the Doctor.");
							}
						}
						if (sync.vote[5]!=-1){//vigilante and doctor
						  Player p= playerList.get(sync.vote[5]);
						  if(p.isAlive()){
						    if(sync.vote[5]!=sync.vote[0]){
  							  vigilanteKill=sync.vote[5];
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
						    else{
							    broadcastPlayers("0\nLast night, "+p.getUsername()+" was attacked. Fortunately, he was saved by the Doctor.");
						    }
						  }
						}
						if (sync.vote[4]!=-1){
  						for(int x=0; x<9;++x){//lookout
  							if(sync.vote[4]==sync.vote[x]&& x!=4){
  								lookoutNum=x;
  							}
  						}
						}
            if (sync.vote[3]!=-1){
						  if (sync.vote[3]==sync.vote[6]){//sheriff and framer
							  sheriffNum=1;
						  }
						  else{// sheriff only
							  int v=playerList.get(sync.vote[3]).getRole();
							  if(v==6 || v==7 ){
								  sheriffNum=1;
							  }
							  else{
							    sheriffNum=0;
							  }
						  }
            }
            
						if(isGameOver()){//check for end conditions
							sync.currentStage=6;
							break game;
						}
						else{//relaying the end results
							sync.vote[0]=mafiaKill;
							sync.vote[1]=vigilanteKill;
							sync.vote[2]=lookoutNum;
							sync.vote[3]=sheriffNum;
							sync.currentStage++;
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
	private void broadcastPlayers(String s){//sending out message to all player on the list
		for(Player p: playerList)
		  p.sendMessage(s);
	}
	private boolean isGameOver(){//check for end conditions
		if (sync.mafiaMember>sync.innocentMember){
			broadcastPlayers("0\nThe game is over since there are more Mafia members now.");
			return true;
		}
		else if (sync.mafiaMember==0){
			broadcastPlayers("0\nThe game is over since all Mafia members are dead.");
			return true;
		}
		else{
			return false;
		}
	}
}
