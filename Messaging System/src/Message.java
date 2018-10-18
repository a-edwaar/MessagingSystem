

public class Message {

    private final String sender;
    private final String text;

    Message(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public String toString() { return "From " + sender + ": " + text;
    }

    public static Message toMessage(String message){

        String sender = message.substring(message.indexOf("From ") + 5, message.indexOf(":"));
        String text = message.substring(message.indexOf(": ") + 2);

        return new Message(sender, text);
    }

}

