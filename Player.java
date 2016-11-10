import java.net.Socket;
import java.io.BufferedWriter;
import java.io.BufferedReader;

public class Player{
  private final Socket playerSocket;
  private final BufferedReader playerInput;
  private final BufferedWriter playerOutput;
  private final String username;
  private int role;
  
  public Player(){
    playerSocket=null;
    playerInput=null;
    playerOutput=null;
    username="";
    role=-1;
  }
  public Player(Socket socket, BufferedReader input, BufferedWriter output, String name, int r){
    playerSocket=socket;
    playerInput=input;
    playerOutput=output;
    username=name;
    role=r;
  }
  public void sendMessage(String s){
    playerOutput.write(s+"\n");
    playerOutput.flush();
  }
  public String getMessage(){
    return (playerInput.readLine());
  }
  public int getRole(){
    return role;
  }
  public void close(){
    if(playerOutput!=null)
    playerOutput.close();
    if(playerInput!=null)
    playerInput.close();
    if(playerSocket!=null)
    playerSocket.close();
  }
}
