package edu.fordham.firebasedemo;

import java.util.Date;

public class Message {
    private String mText;
    private String mUid;
    private String mImageUrl;
    private long mTimestamp;

    // Needed for Firebase
    public Message() {
    }

    public Message(String message, String uid, String image) {
        mText = message;
        mUid = uid;
        mImageUrl = image;
        mTimestamp = new Date().getTime();
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

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }
}
