package edu.fordham.firebasedemo;

import java.util.Date;

public class Message {
    private String mText;
    private String mUid;
    private Date mTimestamp;

    // Needed for Firebase
    public Message() {
    }

    public Message(String message, String uid, String username) {
        mText = message;
        mUid = uid;
        mTimestamp = new Date();
    }

    public String getText() {
        return mText;
    }

    public void setText(String message) {
        mText = message;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String id) {
        mUid = id;
    }

    public Date getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(Date timestamp) {
        mTimestamp = timestamp;
    }
}
