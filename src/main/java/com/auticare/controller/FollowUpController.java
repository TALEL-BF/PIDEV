package com.auticare.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Scope("prototype")
public class FollowUpController {

    @FXML
    private FlowPane cardContainer;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @FXML
    public void initialize() {
        loadMockCards();
    }

    private void loadMockCards() {
        // In a real app, this would fetch from a service
        for (int i = 0; i < 7; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FollowUpCard.fxml"));
                // No specific controller for card in this mock, but we can set values manually
                VBox card = loader.load();

                // Customize card content if needed (via IDs)
                // Label patientLabel = (Label) card.lookup("#patientName");
                // if (i == 1) patientLabel.setText("Another Patient");

                cardContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
