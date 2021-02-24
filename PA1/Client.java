import java.net.*;
import java.io.*;
import java.util.*;

public class Client{
    private String notification = " # "; //notification
    private ObjectInputStream socketInput; //read from the socket
	private ObjectOutputStream socketOutput; //write on the socket
    private Socket sock; //creation of the socket object 
    private String server, userName; //the sever and the username of the client 
    private int port; //the port number 
    public String getuserName(){
        return userName;
    }
    public void setuserName(String userName){
        this.userName = userName; 
    }
    Client(String server, int port, String userName){ //constructor 
        this.server = server; //sets the server number of the client 
        this.port = port; //sets the port number of the client 
        this.userName = userName; //sets the user name of the client 
    }
    public boolean start(){
        try{ //attempt to connect to the server 
            sock = new Socket(server, port);  
        }
        catch(Exception ex){ //exception handler incase connection fails
            display("Error connecting to the server: " + ex);
            return false;
        }
        String message = "Connection accepted " + sock.getInetAddress() + ":" + sock.getPort(); //output to user that connection has been accepted and is working 
        display(message);
        try{ //creating both the object stream and the input stream
            socketInput = new ObjectInputStream(sock.getInputStream());
            socketOutput = new ObjectOutputStream(sock.getOutputStream());
        }
        catch(IOException eo){
            display("Exception creating new input and output streams: " + eo);
            return false;
        }
        new listenToServer().start(); //creation of thread to listen from the server 
        try{ //send the username as a string and every other message will be a part of the Messenger object
            socketOutput.writeObject(userName); //output the username of the client when server has started 
        }
        catch(IOException eo){
            display("Exception login: " + eo);
            disconnect();
            return false;
        }
        return true; //let the user know that it worked 
    }
    private void display(String message){ //send a message to the console 
        System.out.println(message);
    }
    void sendMsg(Messenger message){ //send a message to the server itself 
        try{
            socketOutput.writeObject(message);
        }
        catch(IOException e){
            display("Exception is currently writing to the server: " + e);
        }
    }
    private void disconnect(){
        try{ //if something were to go wrong with the connection, close the input & output streams 
            if(socketInput != null) socketInput.close();
        }
        catch(Exception e){}
        try{
            if(socketOutput != null) socketOutput.close();
        }
        catch(Exception e){}
        try{
            if(sock != null) sock.close();
        }
        catch(Exception e){}
    }
    public static void main(String[] args){
        int pNumber = 5000; //port number we will be using(used the same one from the example of socket programming given)
        String sAddress = "localhost"; //string of the servers address 
        String usersName = " "; //sring of the username 
        Scanner scan = new Scanner(System.in); //take in input from the user 
        System.out.println("Username = "); //print line prompting the user for the username they would like to use 
        usersName = scan.nextLine(); //username will be set to whatever the user typed in 

        switch(args.length){
            case 3:
                sAddress = args[2];
            case 2: 
                try{
                    pNumber = Integer.parseInt(args[1]);
                }
                catch(Exception e){
                    System.out.println("Invalid port number input."); //output to user if an invalid port number was input, although this doesn't reallt matter 
                    return;
                }
            case 1:
                usersName = args[0];
            case 0:
                break;
            default: 
                System.out.println("Usage is greater than java Client [username] [portNumber] [serverAddress]");
                return;
        }
        Client consumer = new Client(sAddress, pNumber, usersName); //creation of the client object 
        if(!consumer.start())
            return;
        System.out.println("Hello " + usersName);
        System.out.println("Welcome to the chat peeps");
        while(true){ //infite while loop to take in input from user 
            System.out.println("> ");
            String message = scan.nextLine(); //read the message from the user 
            if(message.equalsIgnoreCase("BYE")){ //if the client types in bye, they will be disconnected from the server 
                consumer.sendMsg(new Messenger(Messenger.BYE,"")); 
                break; //end the connection if client has 
            }
            else if(message.equalsIgnoreCase("ALLUSERS")){
                consumer.sendMsg(new Messenger(Messenger.ALLUSERS, ""));
            }
        else{
            consumer.sendMsg(new Messenger(Messenger.MSG, message)); //just a regular message 
        }
    }
    scan.close(); //close the resource 
    consumer.disconnect(); //client completed the job so go ahead and disconnect the client from the server 
}
class listenToServer extends Thread{ //class that waits for the message from the server 
    public void run(){
        while(true){
            try{
                String message = (String) socketInput.readObject(); //read the message from the input data stream
                System.out.println(message); //print the message to the user 
                System.out.println("> ");
            }
            catch(IOException e){
                display(notification + "Sever has closed the connection: " + e); //output to client that the connection to the server has been closed 
                break; //end the connection
            }
            catch(ClassNotFoundException e1){
                }
            }
        }
    } 
}