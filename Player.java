/**
* Player.java
*
* This class contains all the required streams for client and server to communicate
*/

import java.net.Socket;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;

public class Player{
  private final Socket playerSocket;
  private final BufferedReader playerInput;
  private final BufferedWriter playerOutput;
  private final String username;
  private int role;
  private boolean alive;

  public Player(){
    playerSocket=null;
    playerInput=null;
    playerOutput=null;
    username="";
    role=-1;
    alive=false;
  }
  public Player(Socket socket, BufferedReader input, BufferedWriter output, String name, int r){
    playerSocket=socket;
    playerInput=input;
    playerOutput=output;
    username=name;
    role=r;
    alive=true;
  }
  
  //input and output methods from server to client
  public void sendMessage(String s){
    try{
      playerOutput.write(s+"\n");
      playerOutput.flush();
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
  public String getMessage(){
    String s="";
    try{
      s=playerInput.readLine();
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
    }
    return s;
  }
  
  //Setters and getters
  public int getRole(){
    return role;
  }
  public String getUsername(){
    return username;
  }
  public boolean isAlive(){
    return alive;
  }
  public Socket getSocket(){
    return playerSocket;
  }
  public void setRole(int r){
    role=r;
  }
  public void setAlive(boolean a){
    alive=a;
  }
 
  public void close(){//closing all the streams
    try{
      if(playerOutput!=null)
      playerOutput.close();
      if(playerInput!=null)
      playerInput.close();
      if(playerSocket!=null)
      playerSocket.close();
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}
