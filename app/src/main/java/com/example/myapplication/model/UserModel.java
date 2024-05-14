package com.example.myapplication.model;

import com.google.firebase.Timestamp;

public class UserModel {
    private String userId;
    private String email;
    private String username;
    private String nowUser;  // New field
    private String fcmToken;
    private Timestamp createdTimestamp;

    public UserModel() {
        // Default constructor required for calls to DataSnapshot.getValue(UserModel.class)
    }

    public UserModel(String userId, String email, String username, Timestamp createdTimestamp) {
        this(userId, email, username, null, createdTimestamp);
    }

    public UserModel(String userId, String email, String username, String fcmToken, Timestamp createdTimestamp) {
        this(userId, email, username, null, fcmToken, createdTimestamp);
    }

    public UserModel(String userId, String email, String username, String nowUser, String fcmToken, Timestamp createdTimestamp) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.nowUser = nowUser;
        this.fcmToken = fcmToken;
        this.createdTimestamp = createdTimestamp;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNowUser() {
        return nowUser;
    }

    public void setNowUser(String nowUser) {
        this.nowUser = nowUser;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
}
