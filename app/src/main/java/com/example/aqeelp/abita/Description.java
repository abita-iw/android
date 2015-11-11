package com.example.aqeelp.abita;

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
        findUserFromId(userId);
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

    private void findUserFromId(int uId) {
        // TODO: actually implement with async call made as needed
        user = new User(uId);
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
}
