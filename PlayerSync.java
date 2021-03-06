/**
* PlayerSync.java
*
* This class is shared between SWTGame and all threads in SWTGame. This is used to synchronize all the threads so that all players are on the same page
*/
public class PlayerSync{
  public volatile boolean endStage;//a sentinel value to end chat servers
  public volatile int threadDone;//counter to make sure every thread finished the stages
  public volatile int currentStage;//1=day chat, 2=lynch, 3=night chat, 4=night action, 5=explanation, 6=game over
  public volatile int mafiaMember, innocentMember;//amount of players alive
  public volatile int mostVote;//index of player who received most vote
  public volatile int vote[];//will contain votes and also actions chosen by players
  public volatile String deadInitial;//will contain username of players who left the game

  public PlayerSync(){//initializing to default values
    endStage=false;
    threadDone=0;
    currentStage=1;
    mafiaMember=3;
    innocentMember=6;
    vote= new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1};
    mostVote=-1;
    deadInitial="\n0\nDead:  ";
  }
}
