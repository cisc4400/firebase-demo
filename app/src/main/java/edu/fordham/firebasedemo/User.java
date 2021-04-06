package edu.fordham.firebasedemo;

public class User {
    private String mName;
    private String mUid;

    public User() {
    }

    public User(String name, String uid) {
        mName = name;
        mUid = uid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

}
