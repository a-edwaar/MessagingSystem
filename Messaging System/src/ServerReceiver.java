

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.*;

// Gets messages from client and puts them in a queue, for another
// thread to forward to the appropriate client.

public class ServerReceiver extends Thread {

    private boolean end = false;

    private String myClientsName;
    private BufferedReader myClient;
    private MessageTable messageTable;
    private GroupTable groupTable;
    private int uid;

    public ServerReceiver(int uid, String n, BufferedReader c, MessageTable m, GroupTable g) {
        myClientsName = n;
        myClient = c;
        messageTable = m;
        groupTable = g;
        this.uid = uid;
    }

    public void run() {
        try {
            while (!end) {

                boolean isGroupMsg = false;
                boolean alteringGroup = false;
                String groupName = "";

                String recipient = myClient.readLine(); // Matches CCCCC in ClientSender.java
                String text = myClient.readLine();
                ArrayList<String> recipients = new ArrayList<>();

                if (recipient.equals(myClientsName)) {

                    switch (text) {

                        case "group":

                            String groupAction = myClient.readLine();
                            groupName = myClient.readLine();

                            switch (groupAction) {

                                case "new":

                                    if (!groupTable.groupPresent(groupName)){
                                        groupTable.addGroup(groupName);
                                        groupTable.addMember(groupName, myClientsName);
                                        Report.behaviour("new group: '" + groupName + "' created by '" + myClientsName + "'");
                                    }else{
                                        Report.error("group already exists");
                                    }

                                    break;

                                case "add":

                                    String newMember = myClient.readLine();
                                    if(messageTable.containsUser(newMember)){

                                        if (groupTable.groupPresent(groupName)){

                                            if(groupTable.getMembers(groupName).contains(myClientsName)) {

                                                //if user wanting to add is not already in the group.
                                                if (!(groupTable.getMembers(groupName).contains(newMember))) {

                                                    groupTable.addMember(groupName, newMember);

                                                    //send a message to the client that they have been added to the group
                                                    recipients.add(newMember);
                                                    text = "added to group " + groupName;
                                                    alteringGroup = true;


                                                } else {
                                                    Report.error("user already in group.");
                                                }
                                            }else{

                                                Report.error("user can not be added to a group by a user not in the group");

                                            }

                                        }else{
                                            Report.error("invalid group");
                                        }

                                    }else{
                                        Report.error("invalid user.");
                                    }

                                    break;

                                case "send":

                                    //get the message to be sent to the group
                                    text = myClient.readLine();

                                    if (groupTable.groupPresent(groupName)){

                                        if (groupTable.getMembers(groupName).contains(myClientsName)) {

                                            recipients.addAll(groupTable.getMembers(groupName));
                                            //remove client as don't want to send message to themselves
                                            recipients.remove(myClientsName);

                                            isGroupMsg = true;

                                        }else{
                                            Report.error("User can't send message to group if they are not a member");
                                        }

                                    }else{

                                        Report.error("Group is not present in table");

                                    }

                                    break;

                                case "remove":

                                    if (groupTable.groupPresent(groupName)){

                                        if (groupTable.getMembers(groupName).contains(myClientsName)) {

                                            String userToRemove = myClient.readLine();

                                            if (groupTable.getMembers(groupName).contains(userToRemove)) {

                                                groupTable.removeMember(groupName, userToRemove);

                                                //send a message to the user to let them know they have been removed
                                                recipients.add(userToRemove);
                                                text = "removed from group " + groupName;
                                                alteringGroup = true;


                                            }else{

                                                Report.error("user is not in group");
                                            }

                                        }else{
                                            Report.error("could not remove member as client does not belong to that group.");
                                        }

                                    }else{
                                        Report.error("member could not be removed as group does not exist.");
                                    }


                                    break;

                                case "delete":

                                    if (groupTable.groupPresent(groupName)){

                                        if (groupTable.getMembers(groupName).contains(myClientsName)){

                                            groupTable.removeGroup(groupName);
                                            Report.behaviour(groupName+" removed from table");

                                        }else{
                                            Report.error("user who is not in group can not remove that group");
                                        }

                                    }else{
                                        Report.error("group does not exist so can't be deleted.");
                                    }


                                    break;

                                default:

                                    Report.error("wrong syntax for group messaging");
                                    break;

                            }



                            break;

                        case "new member":

                            //read new name to add to group

                        case "quit":

                            recipients.add(recipient);
                            end = true;
                            break;

                        case "logout":

                            recipients.add(recipient);
                            end = true;
                            text = text + uid;
                            break;

                        default:

                            recipients.add(recipient);
                            text = text + uid;
                            break;

                    }

                }else{

                    recipients.add(recipient);

                }

                // Matches DDDDD in ClientSender.java
                if (recipient != null && text != null) {

                    if (recipients.size() > 0) {

                        Message msg;

                        if (isGroupMsg){
                            msg = new Message(myClientsName+"(" + groupName + ")", text);
                        }else if(alteringGroup){
                            //send to the client that is being added or removed from group and make it so it looks like its come from their client.
                            msg = new Message(recipients.get(0), text);
                        }else{
                            msg = new Message(myClientsName, text);
                        }

                        //go through each member of the group you want to send to.
                        for (String user : recipients) {

                            //get list for the recipient of the message by using the get method passing in the recipitants user name(key)
                            ArrayList<Message> recipientsList = messageTable.getList(user); // Matches EEEEE in ServerSender.java

                            ConcurrentLinkedQueue<BlockingQueue<Message>> recipientQueues = messageTable.getQueues(user);


                            if (recipientsList != null) {

                                recipientsList.add(msg);

                                for (BlockingQueue<Message> b : recipientQueues) {
                                    b.offer(msg);
                                }

                            } else {

                                Report.error("Message for unexistent client " + recipient + ": " + text);

                            }

                        }

                    }

                } else
                    // No point in closing socket. Just give up.
                    return;
            }

        } catch (IOException e) {
            Report.error("Something went wrong with the client "
                    + myClientsName + " " + e.getMessage());
            // No point in trying to close sockets. Just give up.
            // We end this thread (we don't do System.exit(1)).
        }
    }
}
