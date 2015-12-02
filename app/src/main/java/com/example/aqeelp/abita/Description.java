package com.example.aqeelp.abita;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by aqeelp on 11/10/15.
 */
public class Description {
    final private int descriptionId;
    final private int userId;
    final private int pinId;
    final private String text;

    private User user;
    private Date createdAt;
    private Date modifiedAt;

    public Description(int dId, int uId, int pId, String t, String dateCreated,
                       String dateModified) {
        descriptionId = dId;
        userId = uId;
        pinId = pId;
        text = t;
        createdAt = null;
        modifiedAt = null;
        user = null;

        setDates(dateCreated, dateModified);

        //fetchUser();

        Log.v("Creation", this.toString());
    }

    private void setDates(String dateCreated, String dateModified) {
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        try {
            createdAt = format.parse(dateCreated);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            modifiedAt= format.parse(dateModified);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void fetchUser() {
        UserRetrieval userGetter = new UserRetrieval(null, (Description) this);
        userGetter.execute("https://www.abitatech.net:5000/api/users/" + userId);
    }

    public void setPinUser(User user) {
        this.user = user;
    }

    public void setDescriptionUser(User u) {
        user = u;
    }

    public int getDescriptionId() {
        return descriptionId;
    }

    public int getPinId() {
        return pinId;
    }

    public int getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public User getUser() {
        return user;
    }

    public String toString() {
        return "Description! ID: " + descriptionId + " Message: " + text;
    }
}
