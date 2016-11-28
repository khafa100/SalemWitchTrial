public class PlayerSync{
  public volatile boolean endStage;
  public volatile int threadDone;//counter to make sure every thread finished the stages
  public volatile int currentStage;//1=day chat, 2=lynch, 3=night chat, 4=night action, 5=explanation, 6=game over
  public volatile int mafiaMember, innocentMember, mostVote;
  public volatile int vote[];
  public volatile String deadInitial;

  public PlayerSync(){
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
