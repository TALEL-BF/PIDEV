package com.auticare;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        try {
            System.out.println("Initializing Spring Context...");
            applicationContext = new SpringApplicationBuilder(AutiCareApplication.class).run();
            System.out.println("Spring Context initialized.");
        } catch (Exception e) {
            System.err.println("Failed to initialize Spring Context:");
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        try {
            System.out.println("Starting JavaFX Application...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("AutiCare - Connexion");
            stage.show();
            System.out.println("JavaFX Application started successfully.");
        } catch (Exception e) {
            System.err.println("Failed to start JavaFX Application:");
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }
}
