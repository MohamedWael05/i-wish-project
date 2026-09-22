package com.iwish.client;

import com.iwish.common.User;

public class Session {
    private static User currentUser;

    public static User getUser() { return currentUser; }
    public static void setUser(User user) { currentUser = user; }
    public static boolean isLoggedIn() { return currentUser != null; }
}
