package com.auticare.controllers;

import com.auticare.entities.Event;
import com.auticare.entities.Sponsor;
import com.auticare.services.SponsorServices;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
  // ← AJOUTE AVEC LES AUTRES SERVICES


public class SponsorTableController {

    @FXML
    private TextField searchField;
    @FXML
    private Label countLabel;
    @FXML
    private TableView<Sponsor> sponsorTable;
    @FXML
    private TableColumn<Sponsor, String> colLogo;
    @FXML
    private TableColumn<Sponsor, String> colName;
    @FXML
    private TableColumn<Sponsor, String> colType;
    @FXML
    private TableColumn<Sponsor, String> colEmail;
    @FXML
    private TableColumn<Sponsor, Integer> colTelephone;
    @FXML
    private TableColumn<Sponsor, String> colDescription;
    @FXML
    private TableColumn<Sponsor, Void> colActions;
    @FXML
    private TableColumn<Sponsor, Void> colEvents;
    @FXML
    private StackPane floatingFlowerPane;
    @FXML
    private ImageView floatingFlowerImage;

    // Boutons de filtre
    @FXML
    private Button tousButton;
    @FXML
    private Button orButton;
    @FXML
    private TableView<Event> eventsSponsorTable;
    @FXML
    private TableColumn<Event, String> colEventTitre;
    @FXML
    private TableColumn<Event, Date> colEventDate;
    @FXML
    private TableColumn<Event, String> colEventLieu;
    @FXML
    private Button argentButton;
    @FXML
    private Button bronzeButton;

    // Bouton ajouter
    @FXML
    private Button ajouterSponsorButton;
    private com.auticare.controllers.EventHome eventHome;

    private SponsorServices sponsorServices = new SponsorServices();
    private ObservableList<Sponsor> sponsorList = FXCollections.observableArrayList();
    private FilteredList<Sponsor> filteredData;




    public void setEventHome(EventHome controller) {
        this.eventHome = controller;
        System.out.println("✅ EventHome lié au SponsorTableController");
    }


    @FXML
    public void initialize() {

        setupTableColumns();


        refreshSponsors();


        setupTypeFilters();



        if (floatingFlowerImage != null) {
            TranslateTransition flowerAnimation = new TranslateTransition(Duration.seconds(3), floatingFlowerImage);
            flowerAnimation.setByY(-20);
            flowerAnimation.setAutoReverse(true);
            flowerAnimation.setCycleCount(TranslateTransition.INDEFINITE);
            flowerAnimation.play();
        }

        // Search filter avec FilteredList
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filteredData.setPredicate(sponsor -> {
                if (newText == null || newText.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newText.toLowerCase();
                return sponsor.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        sponsor.getTypeSponsor().toLowerCase().contains(lowerCaseFilter) ||
                        sponsor.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(sponsor.getTelephone()).contains(lowerCaseFilter);
            });
            updateCountLabel();
        });

        // Lier le bouton ajouter
        if (ajouterSponsorButton != null) {
            ajouterSponsorButton.setOnAction(event -> openAddSponsorForm());
        }
    }

    // ===================== FILTRAGE PAR TYPE =====================

    private void setupTypeFilters() {
        // Initialiser filteredData avec tous les sponsors
        filteredData = new FilteredList<>(sponsorList, p -> true);
        sponsorTable.setItems(filteredData);

        // Action pour le bouton "Tous"
        if (tousButton != null) {
            tousButton.setOnAction(e -> {
                filterByType(null);
                updateFilterButtonStyle(tousButton);
            });
        }

        // Action pour le bouton "Or"
        if (orButton != null) {
            orButton.setOnAction(e -> {
                filterByType("Or");
                updateFilterButtonStyle(orButton);
            });
        }

        // Action pour le bouton "Argent"
        if (argentButton != null) {
            argentButton.setOnAction(e -> {
                filterByType("Argent");
                updateFilterButtonStyle(argentButton);
            });
        }

        // Action pour le bouton "Bronze"
        if (bronzeButton != null) {
            bronzeButton.setOnAction(e -> {
                filterByType("Bronze");
                updateFilterButtonStyle(bronzeButton);
            });
        }
    }

    private void filterByType(String type) {
        filteredData.setPredicate(sponsor -> {
            if (type == null) {
                return true; // Afficher tous
            }
            return type.equals(sponsor.getTypeSponsor());
        });
        updateCountLabel();
    }

    private void updateFilterButtonStyle(Button activeButton) {
        String activeStyle = "-fx-background-color:#8b5cf6; -fx-text-fill:white; -fx-background-radius:20; -fx-padding: 8 25; -fx-cursor: hand; -fx-font-weight: bold;";
        String inactiveStyle = "-fx-background-color:white; -fx-text-fill:#8b5cf6; -fx-background-radius:20; -fx-border-color:#8b5cf6; -fx-border-radius:20; -fx-padding: 8 25; -fx-cursor: hand; -fx-font-weight: bold;";

        if (tousButton != null) tousButton.setStyle(inactiveStyle);
        if (orButton != null) orButton.setStyle(inactiveStyle);
        if (argentButton != null) argentButton.setStyle(inactiveStyle);
        if (bronzeButton != null) bronzeButton.setStyle(inactiveStyle);

        activeButton.setStyle(activeStyle);
    }

    private void setupTableColumns() {
        // Colonne Logo
        colLogo.setCellValueFactory(new PropertyValueFactory<>("image"));
        colLogo.setCellFactory(column -> new TableCell<Sponsor, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(80);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);

                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        File imgFile = new File(imagePath);
                        if (imgFile.exists()) {
                            Image image = new Image("file:" + imagePath, 80, 60, true, true);
                            imageView.setImage(image);
                        } else {
                            // Image par défaut
                            imageView.setImage(new Image(getClass().getResource("/assets/logo.png").toExternalForm()));
                        }
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        // Colonne Nom
        colName.setCellValueFactory(new PropertyValueFactory<>("nom"));

        // Colonne Type
        colType.setCellValueFactory(new PropertyValueFactory<>("typeSponsor"));

        // Colonne Email
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setCellFactory(column -> new TableCell<Sponsor, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("📧 " + item);
                }
            }
        });

        // Colonne Téléphone
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colTelephone.setCellFactory(column -> new TableCell<Sponsor, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("📞 " + item);
                }
            }
        });

        // Colonne Description
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setCellFactory(column -> new TableCell<Sponsor, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String shortDesc = item.length() > 50 ? item.substring(0, 47) + "..." : item;
                    setText(shortDesc);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        // Colonne Événements
        colEvents.setCellFactory(param -> new TableCell<Sponsor, Void>() {
            private final Button eventsBtn = new Button("📋 Événements");

            {
                eventsBtn.setStyle(
                        "-fx-background-color: #8b5cf6;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 5 10;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 12px;"
                );

                Tooltip tp = new Tooltip("Voir les événements sponsorisés");
                tp.setStyle("-fx-font-size: 12px; -fx-background-color: #2D3748; -fx-text-fill: white;");
                Tooltip.install(eventsBtn, tp);

                eventsBtn.setOnAction(event -> {
                    Sponsor s = getTableView().getItems().get(getIndex());
                    showSponsorEvents(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : eventsBtn);
            }
        });

        // Colonne Actions
        colActions.setCellFactory(param -> new TableCell<Sponsor, Void>() {
            private final Button editBtn = createActionButton("✏️", "#8B5CF6", "#6D28D9");
            private final Button deleteBtn = createActionButton("🗑️", "#EF4444", "#B91C1C");
            private final HBox pane = new HBox(10, editBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER);

                editBtn.setOnAction(event -> {
                    Sponsor s = getTableView().getItems().get(getIndex());
                    openEditWindow(s);
                });

                deleteBtn.setOnAction(event -> {
                    Sponsor s = getTableView().getItems().get(getIndex());
                    deleteSponsor(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        // Lier les données
        sponsorTable.setItems(sponsorList);
    }

    // Méthode pour afficher les événements sponsorisés
    private void showSponsorEvents(Sponsor sponsor) {
        List<Event> events = sponsorServices.getEventsForSponsor(sponsor.getIdSponsor());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Événements sponsorisés");
        dialog.setHeaderText("Sponsor : " + sponsor.getNom());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #8b5cf6; -fx-border-width: 2;");
        dialogPane.setPrefWidth(550);
        dialogPane.setPrefHeight(450);

        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20; -fx-background-color: #f8fafc; -fx-background-radius: 15;");

        if (events.isEmpty()) {
            Label noEvents = new Label("❌ Aucun événement sponsorisé");
            noEvents.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 16px; -fx-padding: 30; -fx-font-style: italic;");
            content.getChildren().add(noEvents);
        } else {
            // Compteur stylisé
            HBox countBox = new HBox(10);
            countBox.setAlignment(Pos.CENTER_LEFT);
            countBox.setStyle("-fx-background-color: #ede9fe; -fx-background-radius: 30; -fx-padding: 10 20;");

            Label iconLabel = new Label("📋");
            iconLabel.setStyle("-fx-font-size: 20px;");

            Label countLabel = new Label(events.size() + " événement(s) sponsorisé(s)");
            countLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #8b5cf6; -fx-font-size: 16px;");

            countBox.getChildren().addAll(iconLabel, countLabel);
            content.getChildren().add(countBox);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(300);

            VBox eventsList = new VBox(12);

            for (Event e : events) {
                VBox eventCard = new VBox(8);
                eventCard.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 15;" +
                                "-fx-padding: 15;" +
                                "-fx-border-color: #e0d7ff;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 15;" +
                                "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.1), 8, 0, 0, 3);" +
                                "-fx-cursor: hand;"
                );

                // Titre avec icône
                HBox titleBox = new HBox(10);
                titleBox.setAlignment(Pos.CENTER_LEFT);

                Label titleIcon = new Label("📅");
                titleIcon.setStyle("-fx-font-size: 18px;");

                Label title = new Label(e.getTitre());
                title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-font-size: 15px;");

                titleBox.getChildren().addAll(titleIcon, title);

                // Date
                HBox dateBox = new HBox(8);
                dateBox.setAlignment(Pos.CENTER_LEFT);

                Label dateIcon = new Label("🗓️");
                dateIcon.setStyle("-fx-font-size: 14px;");

                Label date = new Label(formatDate(e.getDateDebut()));
                date.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

                dateBox.getChildren().addAll(dateIcon, date);

                // Lieu
                HBox lieuBox = new HBox(8);
                lieuBox.setAlignment(Pos.CENTER_LEFT);

                Label lieuIcon = new Label("📍");
                lieuIcon.setStyle("-fx-font-size: 14px;");

                Label lieu = new Label(e.getLieu());
                lieu.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

                lieuBox.getChildren().addAll(lieuIcon, lieu);

                eventCard.getChildren().addAll(titleBox, dateBox, lieuBox);

                // Effet hover
                eventCard.setOnMouseEntered(ev ->
                        eventCard.setStyle(
                                "-fx-background-color: #f5f0ff;" +
                                        "-fx-background-radius: 15;" +
                                        "-fx-padding: 15;" +
                                        "-fx-border-color: #8b5cf6;" +
                                        "-fx-border-width: 2;" +
                                        "-fx-border-radius: 15;" +
                                        "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.2), 10, 0, 0, 5);" +
                                        "-fx-cursor: hand;"
                        )
                );

                eventCard.setOnMouseExited(ev ->
                        eventCard.setStyle(
                                "-fx-background-color: white;" +
                                        "-fx-background-radius: 15;" +
                                        "-fx-padding: 15;" +
                                        "-fx-border-color: #e0d7ff;" +
                                        "-fx-border-width: 2;" +
                                        "-fx-border-radius: 15;" +
                                        "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.1), 8, 0, 0, 3);" +
                                        "-fx-cursor: hand;"
                        )
                );

                eventsList.getChildren().add(eventCard);
            }

            scrollPane.setContent(eventsList);
            content.getChildren().add(scrollPane);
        }

        dialogPane.setContent(content);

        // ✅ Plus de bouton violet ! Seulement la croix reste
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private Button createActionButton(String icon, String color, String hoverColor) {
        Button btn = new Button(icon);
        btn.setStyle(
                "-fx-background-color: " + color + "20;" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: transparent;"
        );

        Tooltip tp = new Tooltip(icon.equals("✏️") ? "Modifier" : "Supprimer");
        tp.setStyle("-fx-font-size: 12px; -fx-background-color: #2D3748; -fx-text-fill: white; -fx-background-radius: 6;");
        Tooltip.install(btn, tp);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                    "-fx-background-color: " + hoverColor + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8 12;" +
                            "-fx-cursor: hand;"
            );
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(
                    "-fx-background-color: " + color + "20;" +
                            "-fx-text-fill: " + color + ";" +
                            "-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8 12;" +
                            "-fx-cursor: hand;"
            );
        });

        return btn;
    }

    private String formatDate(java.sql.Date date) {
        if (date == null) return "?";
        String[] parts = date.toString().split("-");
        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
        int month = Integer.parseInt(parts[1]) - 1;
        int day = Integer.parseInt(parts[2]);
        return day + " " + months[month] + " " + parts[0];
    }

    private void updateCountLabel() {
        if (filteredData != null) {
            countLabel.setText(filteredData.size() + " Sponsors");
        } else {
            countLabel.setText(sponsorList.size() + " Sponsors");
        }
    }

    // =================== CRUD OPERATIONS ===================

    private void openEditWindow(Sponsor s) {
        try {
            System.out.println("✏️ Modification du sponsor: " + s.getNom());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddSponsorForm.fxml"));
            Parent form = loader.load();

            AddSponsorController controller = loader.getController();
            controller.setSponsorToEdit(s);
            controller.setEventHome(eventHome);  // ← AJOUTE ÇA

            if (eventHome != null) {
                eventHome.getContentArea().getChildren().setAll(form);
            } else {
                Stage stage = (Stage) sponsorTable.getScene().getWindow();
                stage.getScene().setRoot(form);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire de modification");
        }
    }

    private void deleteSponsor(Sponsor s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer le sponsor \"" + s.getNom() + "\" ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                sponsorServices.supprimerSponsor(s.getIdSponsor());
                refreshSponsors();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Sponsor supprimé avec succès!");
            }
        });
    }

    private void refreshSponsors() {
        List<Sponsor> sponsors = sponsorServices.afficherSponsor();
        sponsorList.setAll(sponsors);
        if (filteredData != null) {
            filteredData = new FilteredList<>(sponsorList, p -> true);
            sponsorTable.setItems(filteredData);
        }
        updateCountLabel();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }




    @FXML
    private void openAddSponsorForm() {
        try {
            System.out.println("➕ Ajout d'un nouveau sponsor");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddSponsorForm.fxml"));
            Parent form = loader.load();

            AddSponsorController controller = loader.getController();
            controller.setEventHome(eventHome);  // ← AJOUTE ÇA

            if (eventHome != null) {
                eventHome.getContentArea().getChildren().setAll(form);
            } else {
                Stage stage = (Stage) ajouterSponsorButton.getScene().getWindow();
                stage.getScene().setRoot(form);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }



}



