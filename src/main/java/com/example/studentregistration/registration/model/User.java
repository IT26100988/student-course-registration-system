package com.example.studentregistration.registration.model;

public abstract class User {

    private final String id;
    private final String username;
    private final String password;
    private final String fullName;
    private final String email;

    public User(String id, String username, String password,
                String fullName, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
}
