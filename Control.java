/**
* Control.java
*
* This class is shared by SWTClient and ClientListener to set a end of chat server stage
*/
public class Control{
  public volatile boolean end;
  public Control(){
    end=false;
  }
}
