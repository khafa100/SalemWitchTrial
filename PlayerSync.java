public class PlayerSync{
  public volatile boolean endStage;
  public volatile int threadDone;//counter to make sure every thread finished the stages
  public volatile int currentStage;//1=day chat, 2=vote, 3=lynch, 4=night chat, 5=night action, 6=explanation, 7=game over
  public volatile int mafiaMember, innocentMember, mostVote;
  public volatile int vote[];

  public PlayerSync(){
    endStage=false;
    threadDone=0;
    currentStage=1;
    mafiaMember=3;
    innocentMember=6;
    vote= new int[]{0,0,0,0,0,0,0,0,0};
    mostVote=-1;
  }
}
