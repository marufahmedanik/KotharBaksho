package com.kotharbaksho;

public class msgModelclass {
    String message;
    String senderid;
    long timeStamp;
    String messageId;
    String type; // "text" or "image"

    public msgModelclass() {
    }

    public msgModelclass(String message, String senderid, long timeStamp) {
        this.message = message;
        this.senderid = senderid;
        this.timeStamp = timeStamp;
        this.type = "text";
    }

    public msgModelclass(String message, String senderid, long timeStamp, String messageId, String type) {
        this.message = message;
        this.senderid = senderid;
        this.timeStamp = timeStamp;
        this.messageId = messageId;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderid() {
        return senderid;
    }

    public void setSenderid(String senderid) {
        this.senderid = senderid;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getType() {
        return type != null ? type : "text";
    }

    public void setType(String type) {
        this.type = type;
    }
}
