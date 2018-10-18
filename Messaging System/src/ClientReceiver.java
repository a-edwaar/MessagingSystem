



import java.io.*;

// Gets messages from other clients via the server (by the
// ServerSender thread).

public class ClientReceiver extends Thread {

    private BufferedReader server;
    private String clientName;
    private boolean end = false;
    private int uid;

    ClientReceiver(int uid, String clientName, BufferedReader server) {
        this.clientName = clientName;
        this.server = server;
        this.uid = uid;
    }

    public void run() {

        String logoutMessage = "logout" + uid;
        String quitMessage = "quit" + uid;

        // Print to the user whatever we get from the server:
        try {
            while (!end) {

                String s = server.readLine();// Matches FFFFF in ServerSender.java

                if (s != null) {

                    if(s.contains(clientName) && (s.contains(quitMessage) || s.contains(logoutMessage))){

                        end = true;

                        if (s.contains(quitMessage)){
                            Report.behaviour("disconnected from server.");

                        }else if (s.contains(logoutMessage)){
                            Report.behaviour("logged out.");

                        }

                    }else{

                        System.out.println(s);

                    }


                }else {
                    Report.errorAndGiveUp("Server seems to have died");
                }
            }

        }
        catch (IOException e) {
            Report.errorAndGiveUp("Server seems to have died " + e.getMessage());
        }
    }
}

/*

 * The method readLine returns null at the end of the stream

 * It may throw IoException if an I/O error occurs

 * See https://docs.oracle.com/javase/8/docs/api/java/io/BufferedReader.html#readLine--


 */
