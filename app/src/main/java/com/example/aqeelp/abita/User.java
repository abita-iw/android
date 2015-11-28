package com.example.aqeelp.abita;

/**
 * Created by aqeelp on 11/10/15.
 */
public class User {
    final private int userId;
    final private String email;
    final private String displayName;

    // TODO: implement fully
    public User(int uId, String e, String n) {
        userId = uId;
        email = e;
        displayName = n;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }
}
