

// Usage:
//        java Server
//
// There is no provision for ending the server gracefully.  It will
// end if (and only if) something exceptional happens.


import java.net.*;
import java.io.*;
import java.util.concurrent.BlockingQueue;


public class Server {

    private static int uid;
    private static BufferedReader fromClient;
    private static PrintStream toClient;
    private static String clientName;
    private static boolean eventCompletion;
    private static MessageTable messageTable;
    private static GroupTable groupTable;

    public static void main(String[] args) {

        ServerSocket serverSocket;

        //add shutdown hook thread to save the contents of the table when we close the server
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());

        // This table will be shared by the server threads:
        messageTable = new MessageTable();
        groupTable = new GroupTable();

        //get any messages that were sent last time the server was on, get any groups that were created.
        messageTable.readFile();
        groupTable.readFile();

        serverSocket = null;

        try {
            serverSocket = new ServerSocket(Port.number);
        } catch (IOException e) {
            Report.errorAndGiveUp("Couldn't listen on port " + Port.number);
        }


        while(true) {


            try {

                // Listen to the socket, accepting connections from new clients:
                Socket socket = serverSocket.accept();// Matches AAAAA in Client

                uid++;

                //set logged in back to false when new client arrives
                eventCompletion = false;

                // This is so that we can use readLine():
                fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //to send message back to the client
                toClient = new PrintStream(socket.getOutputStream());

                while (!eventCompletion) {

                    //we ask the client what the command is:
                    String command = fromClient.readLine();

                    // we ask the client what its name is:
                    clientName = fromClient.readLine();// Matches BBBBB in Client

                    switch (command) {

                        //if registering for the first time
                        case "register":

                            if (!messageTable.containsUser(clientName)) {

                                toClient.println("registering");

                                messageTable.add(clientName);

                                //send uid to the client
                                toClient.println(uid);

                                Report.behaviour(clientName + " has registered");

                                startThreads();

                            } else {

                                toClient.println("already registered");

                            }

                            break;

                        case "login":

                            if (messageTable.containsUser(clientName)) {

                                //send uid to the client
                                toClient.println(uid);

                                toClient.println("logging in");

                                //send last message received to client
                                Message lastMSg = messageTable.getCurrentMsg(clientName);

                                toClient.println(lastMSg);

                                startThreads();

                            } else {

                                toClient.println("unable to login");

                            }

                            break;

                    }
                }

            } catch (IOException e) {
                // Lazy approach:
                Report.error("IO error " + e.getMessage());
                // A more sophisticated approach could try to establish a new
                // connection. But this is beyond the scope of this simple exercise.
            }

        }
    }


    private static void startThreads() {

        eventCompletion = true;

        Report.behaviour(clientName + " logged in");

        BlockingQueue<Message> b = messageTable.addNewQueue(clientName);

        // We create and start a new thread to read from the client:
        (new ServerReceiver(uid, clientName, fromClient, messageTable, groupTable)).start();

        // We create and start a new thread to write to the client:
        (new ServerSender(uid, clientName, messageTable, b, toClient)).start();

    }


    //thread that starts when server closes
    private static class ShutdownThread extends Thread {

        public void run() {

            Report.behaviour("saving all users...");
            Report.behaviour("saving all messages...");
            Report.behaviour("saving all groups...");
            Report.behaviour("shutdown.");

            //write the table to the file
            messageTable.writeFile();
            groupTable.writeFile();
        }

    }


}




