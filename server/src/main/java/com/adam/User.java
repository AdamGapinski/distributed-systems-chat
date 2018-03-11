package com.adam;

public class User {
    private String nick;

    public User(String nick) {
        this.nick = nick;
    }

    public User() {
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    @Override
    public String toString() {
        return "User{" +
                "nick='" + nick + '\'' +
                '}';
    }
}
