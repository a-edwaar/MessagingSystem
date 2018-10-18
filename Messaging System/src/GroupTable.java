import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GroupTable {

    private static ConcurrentMap<String, ArrayList<String>> groupList
            = new ConcurrentHashMap<>();

    private String fileName = "groupStorage.txt";

    //create new group and add whoever created it to it
    public void addGroup(String groupName){

        groupList.put(groupName, new ArrayList<>());

    }

    public void removeGroup(String groupName){

        groupList.remove(groupName);

    }

    //add new member to group
    public void addMember(String groupName, String user){

        groupList.get(groupName).add(user);

    }

    public void removeMember(String groupName, String user){

        groupList.get(groupName).remove(user);

    }


    public ArrayList<String> getMembers(String groupName){

        return groupList.get(groupName);

    }

    public boolean groupPresent(String group){

        ArrayList<String> groups = new ArrayList<>(groupList.keySet());
        return groups.contains(group);

    }


    public ArrayList<String> getGroupsForUser(String user){

        ArrayList<String> usersGroups = new ArrayList<>();

        for (String groupName : groupList.keySet()){

            ArrayList<String> membersOfGroup = getMembers(groupName);

            if (membersOfGroup.contains(user)){

                //add to list of groups for that user
                usersGroups.add(groupName);

            }

        }

        //will return an empty array if user is not in any groups
        return usersGroups;

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

            Report.behaviour("loading groups..");

            while ((key = bufferedReader.readLine()) != null) {

                addGroup(key);

                // if the next line is not empty, go through members adding them to list
                if (!((value = bufferedReader.readLine()).equals(""))) {

                    //splits up each message by looking for " ::: "
                    ArrayList<String> members = new ArrayList<>(Arrays.asList(value.split(" ::: ")));


                    for (String member : members) {

                        //add member into group table
                        groupList.get(key).add(member);

                    }

                }

            }

            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            //no previous messages have been stored
            Report.behaviour("no previous groups to load");
        } catch (IOException ex) {
            Report.error("Error reading file '" + fileName + "'");

        }


    }


    //to write to file
    public void writeFile() {

        try {

            FileWriter fileWriter = new FileWriter(fileName);

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);


            for (String user : groupList.keySet()) {

                bufferedWriter.write(user);

                ArrayList<String> members = getMembers(user);

                bufferedWriter.newLine();

                if (members.size() > 0) {

                    for (String member : members) {
                        bufferedWriter.write(member);
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
