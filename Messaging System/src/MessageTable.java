

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

public class MessageTable {

    private static ConcurrentMap<String, ArrayList<Message>> messageList
            = new ConcurrentHashMap<>();

    private static ConcurrentMap<String, ConcurrentLinkedQueue<BlockingQueue<Message>>> messageQueues = new ConcurrentHashMap<>();

    private String fileName = "messageStorage.txt";


    // The following overrides any previously existing nickname, and
    // hence the last client to use this nickname will get the messages
    // for that nickname, and the previously exisiting clients with that
    // nickname won't be able to get messages. Obviously, this is not a
    // good design of a messaging system. So I don't get full marks:


    public synchronized void add(String user) {

        messageList.put(user, new ArrayList<>());
        messageQueues.put(user, new ConcurrentLinkedQueue<>());
    }

    public synchronized void remove(String user) {

        messageList.remove(user);
        messageQueues.remove(user);

    }

    public BlockingQueue<Message> addNewQueue(String user) {

        BlockingQueue<Message> b = new LinkedBlockingQueue<>();
        messageQueues.get(user).add(b);
        return b;

    }

    public ConcurrentLinkedQueue<BlockingQueue<Message>> getQueues(String user) {

        return messageQueues.get(user);

    }

    // Returns null if the nickname is not in the table:
    public ArrayList<Message> getList(String user) {

        return messageList.get(user);

    }

    public boolean containsUser(String user) {

        return messageList.containsKey(user);

    }

    public int getCurrentMsgIndex(String user) {

        ArrayList previousMessages = messageList.get(user);

        if (previousMessages.size() > 0) {
            return previousMessages.size() - 1;
        } else {
            return 0;
        }
    }

    public Message getCurrentMsg(String user) {

        Message lastMsg = new Message("", "");

        ArrayList previousMessages = messageList.get(user);

        if (previousMessages.size() > 0) {

            lastMsg = (Message) previousMessages.get(previousMessages.size() - 1);

        }

        return lastMsg;

    }


    //to read to file
    public void readFile() {

        // This will reference one line at a time
        String key;
        String value;

        try {

            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            Report.behaviour("loading previous messages..");

            while ((key = bufferedReader.readLine()) != null) {

                add(key);

                // if the messages on the next line are not empty, go through them
                if (!((value = bufferedReader.readLine()).equals(""))) {

                    //splits up each message by looking for " ::: "
                    ArrayList<String> messages = new ArrayList<>(Arrays.asList(value.split(" ::: ")));


                    for (String message : messages) {

                        //convert each message string into an actual message and add to message list of client
                        messageList.get(key).add(Message.toMessage(message));

                    }

                }

            }

            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            //no previous messages have been stored
            Report.behaviour("no previous messages to load");
        } catch (IOException ex) {
            Report.error("Error reading file '" + fileName + "'");

        }


    }


    //to write to file
    public void writeFile() {

        try {

            FileWriter fileWriter = new FileWriter(fileName);

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);


            for (String user : messageList.keySet()) {

                bufferedWriter.write(user);

                ArrayList<Message> messages = getList(user);

                bufferedWriter.newLine();

                if (messages.size() > 0) {

                    for (Message message : messages) {
                        bufferedWriter.write(message.toString());
                        //add separator for when reading
                        bufferedWriter.write(" ::: ");
                    }
                }

                bufferedWriter.newLine();

            }

            bufferedWriter.close();
        } catch (IOException ex) {
            Report.error("Error writing to file '" + fileName + "'");
        }

    }


}
