package com.example.aqeelp.abita;

import android.util.Log;

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

        Log.v("Creation", this.toString());
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

    public String toString() {
        return "User! ID: " + userId + " Email: " + email;
    }
}
