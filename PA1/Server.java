import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Server{
    private static int specialID; //unique ID for each connection
    private ArrayList<ClientThread> array; //creation of an array list in order to keep track of the clients 
    private int port; //that port number that will listen for connection 
    private boolean go; //checks to make sure the server is runnning 
    private SimpleDateFormat timeStamp; //displays the time of connections 
    private String notification = " # "; //notification 

    public Server(int port){ //constructor that will receive the port to listen to 
        this.port = port; 
        timeStamp = new SimpleDateFormat("HH:mm:ss"); //display to the user what the time of connection is 
        array = new ArrayList<ClientThread>(); //keep a list of the clients as they join 
    }

    public void start(){ //start method that will begin waiting for socket server requests 
        go = true;
        try{ //creation of socket server and will wait for connection requests 
            ServerSocket serverSock = new ServerSocket(port);
            while(go){ //infinite loop that will wait for connections 
                display("Sever is waiting for clients on port " + port + ".");
                Socket sock = serverSock.accept(); //accept the connection if request by the client 
                if(!go)
                break;
                ClientThread threading = new ClientThread(sock); //if the client connects, create a thread for them
                array.add(threading); //add client to the array 
                threading.start(); //begin the thread and start the connection
            }
            try{ //try block to stop the server 
                serverSock.close(); 
                for(int i = 0; i < array.size(); i++){
                    ClientThread threadClient = array.get(i);
                    try{ //close the data streams and the socket 
                        threadClient.socketInput.close();
                        threadClient.socketOutput.close();
                        threadClient.sock.close();
                    }
                    catch(IOException io){
                    }
                }
            }
            catch(Exception e){
                display("Exception is closing the server and clients: " + e); //display to the user that the exception will now be closing the connection between server and client
            }

        }
        catch(IOException e){
            String message = timeStamp.format(new Date()) + " Exception on new server socket: " + e + "\n";
            display(message); //output to the user if there is an exception error on whatever server socket used by user
        }
    }

    protected void stop(){ //used to stop the server 
        go = false;
        try{
            new Socket("localhost", port);
        }
        catch(Exception e){
        }
    }

    private void display(String message){ //display the event to the user 
        String time = timeStamp.format(new Date()) + " " + message; //output to the user the time of message delivery and the message that was sent from one client to the other 
        System.out.println(time); //print the time out to the user 
    }

    private synchronized boolean broadcast(String msg){ //broadcast a message to all of the clients 
        String time = timeStamp.format(new Date()); //add a time to the message 
        String[] word = msg.split(" ",3); //ensure the message is client to client 
        boolean isPrivate = false; //in case the message isn't between client to client 
        if(word[1].charAt(0) == '@')
            isPrivate = true;
            if(isPrivate == true){ //handles if the user would just like to send the message to one client and not the entire user group 
                String checker = word[1].substring(1, word[1].length());
                msg = word[0] + word[2];
                String msg1 = time + " " + msg + "\n";
                boolean found = false;
                    for(int y = array.size(); --y >= 0;){
                        ClientThread clientThread1 = array.get(y);
                        String check = clientThread1.getuserName();
                        if(check.equals(checker)){
                            if(!clientThread1.writeMessage(msg1)){
                                array.remove(y);
                                display("Disconnected client " + clientThread1.userName + " removed from list"); // if the client disconnects, output this to the user 
                            }
                            found = true;
                            break;
                        }
                    } 
                    if(found != true){
                        return false;
                    }
            }
            else{
            String msg1 = time + " " + msg + "\n";
            System.out.print(msg1);
            for(int i = array.size(); --i >= 0;){ //loop in reverse order so that in case a client disconnects, we would remove them from the array 
                ClientThread clientThread = array.get(i); 
                if(!clientThread.writeMessage(msg1)){ //try and write to the client and if it fails, remove it from the array 
                    array.remove(i); 
                    display("Disconnected client " + clientThread.userName + " removed from list."); //output to the user that said user has been removed from the list of clients 
                }
            }
        }
        return true; 
    }
    synchronized void remove(int id){ //if client sent BYE message, exit the chat 
        String disconnectClient = "";
        for(int i = 0; i < array.size(); ++i){ //scan the array until we find username
            ClientThread clientThread = array.get(i);
            if(clientThread.id == id){
                disconnectClient = clientThread.getuserName();
                array.remove(i);
                break;
            }
        }
        broadcast(notification + "Server: Goodbye " + disconnectClient);
    }
    public static void main(String[] args){
        int portNumber = 5000; //start server on port 5000 unless another port number is specified 
        switch(args.length){
            case 1:
                try{
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e){
                    System.out.println("Invalid port number.");
                    return;
                }
            case 0:
                break;
            }    
                Server server = new Server(portNumber); //create a server object and then start it 
                server.start();

        }
        class ClientThread extends Thread{ //one instance of this thread will run for each individual client
            Socket sock; //the socket that will receive messages from the client 
            ObjectInputStream socketInput; //read from the clients socket 
            ObjectOutputStream socketOutput; //write from the clients socket 
            int id; 
            String userName; //username of the client
            Messenger chatter; //message object to receive message and its type 
            String t; //time

            ClientThread(Socket sock){
                id = ++specialID; //username that the client chose 
                this.sock = sock;
                System.out.println("Thread is attempting to make object input and output streams."); //output to user that input & output streams are being created using threading 
                try{
                    socketOutput = new ObjectOutputStream(sock.getOutputStream()); //write the socket from the output stream 
                    socketInput = new ObjectInputStream(sock.getInputStream()); //read from the socket the input dtream 
                    userName =  (String) socketInput.readObject(); //read in the username given
                    broadcast(notification + "Server: Welcome " + userName); //welcome prompt on server's side 
                }
                catch(IOException e){
                    display("Exception creating new input and output streams: " + e);
                    return;
                }
                catch(ClassNotFoundException e){
                }
                t = new Date().toString() + "\n"; //display the time stamp 
            }
            public String getuserName(){
                return userName; //get the username from the client 
            }
            public void setuserName(String userName){
                this.userName = userName; //set the clients user name 
            }
            public void run(){ //loop that will run indefinitley to read & forward messages 
                boolean go = true; //loop until BYE is typed
                while(go){ //initiation of infinite while loop 
                    try{
                        chatter = (Messenger) socketInput.readObject();
                    }
                    catch(IOException e){
                        display(userName + " Exception reading streams: " + e);
                        break;
                    }
                    catch(ClassNotFoundException e1){
                        break;
                    }
                    String msg = chatter.getMsg(); //get the message from the client 
                    switch(chatter.getType()){
                        case Messenger.MSG: 
                            boolean confirm = broadcast(userName + ": " + msg); //display the message on the console 
                            if(confirm == false){ 
                                String message = notification + "Please type a valid user name: ";
                                writeMessage(message);
                            }
                            break;
                        case Messenger.BYE: //if user types in bye, log them out of the chat
                            display("Server: Goodbye " + userName); //output if the user typed in Bye 
                            go = false; //if results go results in false, break 
                            break; //end if log out message was input 
                        case Messenger.ALLUSERS: //outout to user of whatever one client sent to another 
                            writeMessage("List of all active user " + timeStamp.format(new Date())+ "\n"); 
                            for(int i = 0; i < array.size(); i++){ //send the list of all active clients on the server 
                                ClientThread clientThread = array.get(i); //array that will keep a list of all of the active users on the server 
                                writeMessage((i + 1) + ") " + clientThread.userName + " since " + clientThread.t); //output to user the list of users along with how long they have been connected to the server 
                            }
                            break;
                    }
                }
                remove(id);
                close();
            }
            private void close(){ //close everything else out 
                try{
                    if(socketOutput != null) socketOutput.close();
                }
                catch(Exception e){}
                try{
                    if(socketInput != null) socketInput.close();
                }
                catch(Exception e){};
                try{
                    if(sock != null) sock.close();
                }
                catch(Exception e){}
            }
            private boolean writeMessage(String message){
                if(!sock.isConnected()){ //if the client is still connected, send the message 
                    close();
                    return false;
                }
                try{ //write the message to the stream
                    socketOutput.writeObject(message);
                }
                catch(IOException e){ //if an error does occur, dont abort the server, instead just alert the user 
                    display(notification + "Error sending message to " + userName); //string that will be displayed to user if error does occur 
                    display(e.toString());
                }
                return true;
            }
        }
    }
