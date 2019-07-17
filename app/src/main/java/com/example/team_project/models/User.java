package com.example.team_project.models;

public class User {
    String username;
    String email;
    String password;

    public User() {
        //Empty Constructor For Firebase
    }

    public User(String username, String email, String password)
    {
        this.username = username; //Parameterized for Program-Inhouse objects.
        this.email = email;
        this.password = password;
    }

    //Getters and Setters
    public String getUsername()
    {
        return username;
    }
    public void setUsername(String username)
    {
        this.username = username;
    }
    public String getEmail()
    {
        return email;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }
    public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }
}
