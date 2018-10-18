

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;


// Continuously reads from message queue for a particular client,
// forwarding to the client.

public class ServerSender extends Thread {

    private boolean end = false;
    private ArrayList<Message> clientList;
    private BlockingQueue<Message> clientQueue;
    private PrintStream client;
    private String clientName;
    private MessageTable messageTable;

    private String logoutMessage;
    private String previousMessage;
    private String nextMessage;
    private String deleteMessage;


    public ServerSender(int uid, String clientName, MessageTable t, BlockingQueue<Message> clientQueue, PrintStream c) {

        this.clientName = clientName;
        this.messageTable = t;
        this.clientList = messageTable.getList(clientName);
        this.clientQueue = clientQueue;
        this.client = c;

        //messages only one client will be able to send to the server
        this.logoutMessage = "logout" + uid;
        this.previousMessage = "previous" + uid;
        this.nextMessage = "next" + uid;
        this.deleteMessage = "delete" + uid;

    }

    public void run() {

        //set current message to the last message received
        int currentMsgIndex = messageTable.getCurrentMsgIndex(clientName);

        while (!end) {

            try {

                Message msg = clientQueue.take();

                if (msg.getSender().equals(clientName)) {

                    if (msg.getText().equals(logoutMessage)) {

                        //remove last message from the client table
                        clientList.remove(clientList.size()-1);
                        client.println(msg);
                        end = true;
                        Report.behaviour(clientName + " logged out");

                    } else if (msg.getText().equals("quit")) {

                        client.println(msg);
                        end = true;
                        messageTable.remove(clientName);
                        Report.behaviour(clientName + " disconnected and removed from registered accounts.");

                    } else if (msg.getText().equals(previousMessage)) {

                        clientList.remove(clientList.size()-1);
                        Report.error("got here removing last input");

                        if (clientList.size()>0) {

                            if (currentMsgIndex > 0) {
                                currentMsgIndex--;
                                client.println(clientList.get(currentMsgIndex));

                            } else {
                                client.println("no previous messages");
                            }

                        }else{
                            client.println("messages empty");
                        }

                    } else if (msg.getText().equals(nextMessage)) {
                        //remove last message from the client table
                        clientList.remove(clientList.size()-1);

                        if(clientList.size()>0) {

                            if (currentMsgIndex < (clientList.size() - 1)) {
                                currentMsgIndex++;
                                client.println(clientList.get(currentMsgIndex));

                            } else {
                                client.println("no messages after");
                            }

                        }else{
                            client.println("messages empty");
                        }

                    } else if (msg.getText().equals(deleteMessage)) {

                        //remove last message from the client table
                        clientList.remove(clientList.size()-1);

                        if (clientList.size() > 0) {

                            //delete the current message
                            clientList.remove(currentMsgIndex);

                            //if index is at the end of the list decrement it
                            if (currentMsgIndex == clientList.size() && currentMsgIndex > 0) {
                                currentMsgIndex--;
                            }

                            if ((clientList.size()) > 0) {
                                client.println(clientList.get(currentMsgIndex));
                            } else {
                                client.println("messages empty");
                            }

                        } else {
                            client.println("messages empty");
                        }

                    } else if (msg.getText().contains("delete")) {

                        currentMsgIndex--;

                    } else if (msg.getText().contains("added to group ")){

                        //remove last message from the client table
                        clientList.remove(clientList.size()-1);

                        client.println("You have been " + msg.getText());

                    } else if (msg.getText().contains("removed from group ")){

                        //remove last message from the client table
                        clientList.remove(clientList.size()-1);

                        client.println("You have been " + msg.getText());

                    }

                } else {

                    //send msg to client
                    client.println(msg);

                    //set current message to the last message received
                    currentMsgIndex = messageTable.getCurrentMsgIndex(clientName);

                }

            } catch (InterruptedException e) {

                //go back to the loop

            }

        }

    }

}



