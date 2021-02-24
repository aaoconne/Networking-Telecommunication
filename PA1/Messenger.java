import java.io.*;

public class Messenger implements Serializable{
    static final int ALLUSERS = 0, MSG = 1, BYE = 2; //special cases for when a user types in bye or all users while connected to the server 
    private int type; //the type of message 
    private String msg; //the message itself 
Messenger(int type, String msg){ //setting of the variables by type and the message itself
    this.type = type;
    this.msg = msg; 
    }
    int getType(){
        return type; //return the type of message 
    }
    String getMsg(){
        return msg; //get the mesage from the client 
    }
}