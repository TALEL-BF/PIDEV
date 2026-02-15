package com.auticare.controller;

import com.auticare.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Scope("prototype")
public class ParentDashboardController {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        // Redirect logic...
    }
}
