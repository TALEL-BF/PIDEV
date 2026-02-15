package com.auticare.session;

import com.auticare.entity.User;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Private constructor for singleton
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        this.currentUser = null;
    }
}
