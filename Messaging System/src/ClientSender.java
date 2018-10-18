

import java.io.*;
import java.util.ArrayList;


// Repeatedly reads recipient's nickname and text from the user in two
// separate lines, sending them to the server (read by ServerReceiver
// thread).


public class ClientSender extends Thread {


    private String nickname;
    private PrintStream server;
    private String recipient;
    private String text;

    private boolean end = false;

    ClientSender(String nickname, PrintStream server) {
        this.nickname = nickname;
        this.server = server;
    }

    public void run() {
        // So that we can use the method readLine:
        BufferedReader user = new BufferedReader(new InputStreamReader(System.in));

        try {
            // Then loop forever sending messages to recipients via the server:
            while (!end) {


                String command = user.readLine();


                switch (command) {

                    case "send":
                        this.recipient = user.readLine();
                        if (!(recipient.equals(nickname))) {
                            sendToServer(recipient, user.readLine());// Matches DDDDD in ServerReceiver
                        } else {
                            Report.error("can't send a message to yourself");
                        }
                        break;

                    case "quit":
                        sendToServer(nickname, "quit");
                        break;

                    case "logout":
                        sendToServer(nickname, "logout");
                        break;

                    case "previous":
                        sendToServer(nickname, "previous");
                        break;

                    case "next":
                        sendToServer(nickname, "next");
                        break;

                    case "delete":
                        sendToServer(nickname, "delete");
                        break;

                    case "group":

                        //let server know that we are about to do something with a group
                        sendToServer(nickname, command);

                        String action = user.readLine();
                        server.println(action);

                        String groupName = user.readLine();
                        server.println(groupName);

                        switch (action) {

                            case "add":

                                String newMember = user.readLine();
                                server.println(newMember);

                                break;

                            case "send":

                                String message = user.readLine();
                                server.println(message);

                                break;

                            case "remove":

                                String userToRemove = user.readLine();
                                server.println(userToRemove);

                                break;

                            default:

                                break;

                        }

                        break;

                    default:
                        Report.error("wrong syntax");
                        break;
                }

            }

        } catch (IOException e) {
            Report.errorAndGiveUp("Communication broke in ClientSender"
                    + e.getMessage());
        }
    }


    private void sendToServer(String recipient, String text) {

        this.recipient = recipient;
        this.text = text;

        if (recipient.equals(nickname) && (text.equals("quit") || text.equals("logout"))) {
            end = true;
        }

        server.println(recipient);
        server.println(text);

    }
}

/*

What happens if recipient is null? Then, according to the Java
documentation, println will send the string "null" (not the same as
null!). So maye we should check for that case! Paticularly in
extensions of this system.

 */
