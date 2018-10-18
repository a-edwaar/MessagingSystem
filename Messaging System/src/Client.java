

// Usage:
//        java Client user-nickname server-hostname
//
// After initializing and opening appropriate sockets, we start two
// client threads, one to send messages, and another one to get
// messages.
//
// A limitation of our implementation is that there is no provision
// for a client to end after we start it. However, we implemented
// things so that pressing ctrl-c will cause the client to end
// gracefully without causing the server to fail.
//
// Another limitation is that there is no provision to terminate when
// the server dies.


import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class Client {

    private static boolean correctInfo = false;

    public static void main(String[] args) {

        int uid = 0;

        //Check correct usage:
        if (args.length != 1) {
            Report.errorAndGiveUp("Usage: java Client server-hostname");
        }

        // Initialize information
        String hostname = args[0];
        String command = "";
        String nickname = "";

        // Open sockets:
        PrintStream toServer = null;
        BufferedReader fromServer = null;
        Socket server = null;

        try {
            server = new Socket(hostname, Port.number); // Matches AAAAA in Server.java
            toServer = new PrintStream(server.getOutputStream());
            fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
        }
        catch (UnknownHostException e) {
            Report.errorAndGiveUp("Unknown host: " + hostname);
        }
        catch (IOException e) {
            Report.errorAndGiveUp("The server doesn't seem to be running " + e.getMessage());
        }

        BufferedReader user = new BufferedReader(new InputStreamReader(System.in));

        while(!correctInfo) {

            try {

                //command should either be register or login
                command = user.readLine();
                nickname = user.readLine();



                switch (command){

                    case "register":

                        //tell server what the command word is
                        toServer.println(command);
                        // Tell the server what my nickname is:
                        toServer.println(nickname);// Matches BBBBB in Server.java

                        if (fromServer.readLine().equals("registering")) {

                            //get unique identifier from the server
                            uid = Integer.parseInt(fromServer.readLine());

                            Report.behaviour("registered.");

                            Report.behaviour("logged in.");

                            correctInfo = true;
                        } else {

                            Report.error("name already registered.");
                        }

                        break;

                    case "login":

                        //tell server what the command word is
                        toServer.println(command);
                        // Tell the server what my nickname is:
                        toServer.println(nickname);// Matches BBBBB in Server.java

                        //get unique identifier from the server
                        uid = Integer.parseInt(fromServer.readLine());

                        String event = fromServer.readLine();

                        switch(event) {

                            case "logging in":

                                correctInfo = true;

                                Report.behaviour("logged in.");

                                String currentMessage = fromServer.readLine();

                                //if it is not an empty message
                                if (!currentMessage.equals("From : ")) {

                                    System.out.println("LAST MESSAGE RECEIVED");

                                    System.out.println(currentMessage);
                                }

                                break;

                            case "unable to login":

                                Report.error("name not registered yet.");
                                break;

                            case "client already logged in":

                                Report.error("account already in use.");
                                break;

                        }

                        break;

                    default:

                        Report.error("wrong syntax");
                        break;
                }


            } catch (IOException e) {
                Report.error("can't read text from client");
            }

        }

        //carry on only if the command and nickname has been set
        assert (!nickname.equals("") && !command.equals(""));


        // Create two client threads of a different nature:
        ClientSender sender = new ClientSender(nickname,toServer);
        ClientReceiver receiver = new ClientReceiver(uid, nickname, fromServer);

        // Run them in parallel:
        sender.start();
        receiver.start();




        // Wait for them to end and close sockets.
        try {
            sender.join();
            receiver.join();
            toServer.close();
            fromServer.close();
            server.close();
        }
        catch (IOException e) {
            Report.errorAndGiveUp("Something wrong " + e.getMessage());
        }
        catch (InterruptedException e) {
            Report.errorAndGiveUp("Unexpected interruption " + e.getMessage());
        }
    }
}

