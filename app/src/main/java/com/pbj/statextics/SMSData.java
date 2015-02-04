package com.pbj.statextics;

/**
 * This class represents SMS.
 */
public class SMSData {
    public static final String INBOX = "inbox";
    public static final String OUTBOX = "outbox";

    private final String number;
    private final String body;
    private final String id;
    private final String time;
    private String folderName;
    private final String name;

    public SMSData(String number, String body, String id, String time, String folderName, String name) {
        this.number = number;
        this.body = body;
        this.id = id;
        this.time = time;
        this.folderName = folderName;
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public String getBody() {
        return body;
    }

    public String getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getName(){return name;}

    public void switchFolder() {
        if (this.folderName.equals(SMSData.INBOX)) {
            this.folderName = SMSData.OUTBOX;
        }
        else {
            this.folderName = SMSData.INBOX;
        }
    }


}