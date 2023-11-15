package com.team8.socialmedia.kone;

import com.google.firebase.database.PropertyName;

public class ModelChat {
    String message, receiver, sender, timestamp;
    boolean isSeen;

    public ModelChat(String message, String receiver, String sender, String timestamp, boolean isSeen) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
    }

    public ModelChat() {

    }
    @PropertyName("message")
    public String getMessage() {
        return message;
    }
    @PropertyName("message")
    public void setMessage(String message) {
        this.message = message;
    }
    @PropertyName("receiver")
    public String getReceiver() {
        return receiver;
    }
    @PropertyName("receiver")
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    @PropertyName("sender")
    public String getSender() {
        return sender;
    }
    @PropertyName("sender")
    public void setSender(String sender) {
        this.sender = sender;
    }
    @PropertyName("timestamp")
    public String getTimetamp() {
        return timestamp;
    }
    @PropertyName("timestamp")
    public void setTimeStamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("isSeen")
    public boolean isSeen() {
        return isSeen;
    }

    @PropertyName("isSeen")
    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
