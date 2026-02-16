package com.auticare.controllers;

import com.auticare.entities.Event;
import com.auticare.services.EventAIPredictor;
import com.auticare.services.EventServices;
import com.auticare.services.FallbackService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class EventTableController {

    @FXML
    private TextField searchField;
    private com.auticare.controllers.EventHome eventHome;

    @FXML
    private Label countLabel;
    private FallbackService fallbackService;  // ← À ajouter
    @FXML
    private TableView<Event> eventTable;
    @FXML
    private TableColumn<Event, String> titreColumn;
    @FXML
    private TableColumn<Event, String> descriptionColumn;
    @FXML
    private TableColumn<Event, Date> dateDebutColumn;
    @FXML
    private TableColumn<Event, Date> dateFinColumn;
    @FXML
    private TableColumn<Event, Time> heureDebutColumn;
    @FXML
    private TableColumn<Event, Time> heureFinColumn;
    @FXML
    private TableColumn<Event, String> lieuColumn;
    @FXML
    private TableColumn<Event, Void> predictColumn;
    @FXML
    private TableColumn<Event, Integer> maxParticipantColumn;
    @FXML
    private TableColumn<Event, String> typeEventColumn;
    @FXML
    private TableColumn<Event, String> imageColumn;
    @FXML
    private TableColumn<Event, Void> actionsColumn;
    @FXML
    private TableColumn<Event, Void> planningColumn;

    @FXML
    private Button tousButton, actifButton, termineButton, annuleButton;
    @FXML
    private Button ajouterEventButton;
    private EventAIPredictor aiPredictor = new EventAIPredictor();

    private EventServices eventServices = new EventServices();
    private List<Event> allEvents;
    private ObservableList<Event> eventObservableList;
    private String currentFilter = "tous";

    private void initializeAI() {
        try {

            aiPredictor.train(allEvents);
            System.out.println("✅ AI initialized and trained");
        } catch (Exception e) {
            System.out.println("⚠️ AI training issue: " + e.getMessage());

        }
    }

    @FXML
    public void initialize() {



        allEvents = eventServices.afficherEvent();
        eventObservableList = FXCollections.observableArrayList(allEvents);

        // Setup table columns
        setupTableColumns();
        String groqKey = "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";
        fallbackService = new FallbackService(groqKey);


        // Set items to table
        eventTable.setItems(eventObservableList);

        // Update count label
        updateCountLabel();

        // Setup search and filters
        setupSearch();
        initializeAI();
        setupFilters();
        setupNavigation();

        // Add CSS class to table
        eventTable.getStyleClass().add("event-table");
    }




    public void setEventHome(EventHome controller) {
        this.eventHome = controller;
        System.out.println("✅ EventHome lié au EventTableController");
    }



    private EventHome getEventHome() {
        return eventHome;
    }



    private void showPredictionForEvent(Event e) {
        double prediction = aiPredictor.predictAutismScore(e);

        // Créer un dialogue personnalisé
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🧩 Analyse Autisme");
        dialog.setHeaderText(null);

        // Style de la boîte de dialogue - Dégradé violet clair
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #E9D8FD, #D6BCFA);" +  // Dégradé violet clair
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: #9F7AEA;" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 30;" +
                        "-fx-padding: 20;"
        );
        dialogPane.getButtonTypes().addAll(ButtonType.OK);

        // Contenu principal
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(15, 20, 15, 20));
        content.setStyle("-fx-background-color: transparent;");

        // Titre simple
        Label titleLabel = new Label("🎯 PRÉDICTION");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #553C9A;");  // Violet foncé pour le contraste

        // Nom de l'événement
        Label eventTitle = new Label(e.getTitre());
        eventTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #44337A; -fx-wrap-text: true; -fx-text-alignment: center;");  // Violet foncé
        eventTitle.setMaxWidth(350);

        // Score en grand
        Label scoreLabel = new Label(String.format("%.0f%%", prediction));
        scoreLabel.setStyle("-fx-font-size: 64px; -fx-font-weight: 900; -fx-text-fill: #6B46C1;");  // Violet moyen

        // Barre de progression simple
        ProgressBar progressBar = new ProgressBar(prediction / 100);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(15);

        // Couleur de la barre selon le score
        String barColor;
        if (prediction >= 75) {
            barColor = "#10B981"; // Vert
        } else if (prediction >= 50) {
            barColor = "#F59E0B"; // Orange
        } else {
            barColor = "#EF4444"; // Rouge
        }
        progressBar.setStyle("-fx-accent: " + barColor + "; -fx-background-radius: 10; -fx-control-inner-background: rgba(255,255,255,0.3);");

        // Message simple
        String message;
        if (prediction >= 75) {
            message = "🌟 Excellent pour les enfants autistes !";
        } else if (prediction >= 50) {
            message = "✨ Adapté avec quelques précautions";
        } else {
            message = "⚠️ À considérer avec attention";
        }

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #553C9A; -fx-text-alignment: center;");  // Violet foncé
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(350);

        // Petit conseil
        Label conseilLabel = new Label();
        conseilLabel.setWrapText(true);
        conseilLabel.setMaxWidth(350);
        conseilLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #44337A; -fx-font-style: italic; -fx-text-alignment: center; -fx-padding: 10; -fx-background-color: rgba(255,255,255,0.3); -fx-background-radius: 15;");

        if (prediction >= 75) {
            conseilLabel.setText("💡 N'oublie pas de prévenir de tes besoins spécifiques");
        } else if (prediction >= 50) {
            conseilLabel.setText("💡 Prépare un casque anti-bruit si besoin");
        } else {
            conseilLabel.setText("💡 Contacte l'organisateur pour plus d'infos");
        }

        // Assembler le tout
        content.getChildren().addAll(
                titleLabel,
                eventTitle,
                scoreLabel,
                progressBar,
                messageLabel,
                conseilLabel
        );

        dialogPane.setContent(content);

        // Style du bouton OK
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle(
                "-fx-background-color: #9F7AEA;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 10 40;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );

        // Animation simple
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dialogPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        dialog.showAndWait();
    }
    private void setupTableColumns() {
        // Image column
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        imageColumn.setCellFactory(column -> new TableCell<Event, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-border-radius: 8; -fx-background-radius: 8;");
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
                            Image image = new Image("file:" + imagePath, 40, 40, true, true);
                            imageView.setImage(image);
                        } else {
                            // Try to load default image
                            try {
                                Image defaultImage = new Image(getClass().getResourceAsStream("/assets/event-default.jpg"));
                                imageView.setImage(defaultImage);
                            } catch (Exception e) {
                                imageView.setImage(null);
                            }
                        }
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        // Titre column
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        titreColumn.setCellFactory(column -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2E2E3A;");
                }
            }
        });

        // Description column
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(column -> new TableCell<Event, String>() {
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

        // Date columns
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateDebutColumn.setCellFactory(column -> new TableCell<Event, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatDate(item));
                }
            }
        });

        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        dateFinColumn.setCellFactory(column -> new TableCell<Event, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatDate(item));
                }
            }
        });

        // Time columns
        heureDebutColumn.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        heureDebutColumn.setCellFactory(column -> new TableCell<Event, Time>() {
            @Override
            protected void updateItem(Time item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatTime(item));
                }
            }
        });

        heureFinColumn.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        heureFinColumn.setCellFactory(column -> new TableCell<Event, Time>() {
            @Override
            protected void updateItem(Time item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatTime(item));
                }
            }
        });

        // Location column
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));

        // Max participants column
        maxParticipantColumn.setCellValueFactory(new PropertyValueFactory<>("maxParticipant"));
        maxParticipantColumn.setCellFactory(column -> new TableCell<Event, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("👥 " + item);
                }
            }
        });

        // Event type column
        typeEventColumn.setCellValueFactory(new PropertyValueFactory<>("typeEvent"));

        // PREDICT COLUMN - NEW BUTTON FOR AI PREDICTION
        predictColumn.setCellFactory(param -> new TableCell<Event, Void>() {
            private final Button predictBtn = new Button("✨ Autisme");

            {
                predictBtn.setStyle(
                        "-fx-background-color: #198C19;" +  // Vert au lieu de violet
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12px;" +
                                "-fx-background-radius: 15;" +
                                "-fx-padding: 5 15;" +
                                "-fx-cursor: hand;"
                );

                predictBtn.setOnAction(event -> {
                    Event e = getTableView().getItems().get(getIndex());
                    showPredictionForEvent(e);
                });

                // Hover effect
                predictBtn.setOnMouseEntered(e ->
                        predictBtn.setStyle(
                                "-fx-background-color: #198C19;" +
                                        "-fx-text-fill: white;" +
                                        "-fx-font-size: 12px;" +
                                        "-fx-background-radius: 15;" +
                                        "-fx-padding: 5 15;" +
                                        "-fx-cursor: hand;"
                        )
                );

                predictBtn.setOnMouseExited(e ->
                        predictBtn.setStyle(
                                "-fx-background-color:#008000;" +
                                        "-fx-text-fill: white;" +
                                        "-fx-font-size: 12px;" +
                                        "-fx-background-radius: 15;" +
                                        "-fx-padding: 5 15;" +
                                        "-fx-cursor: hand;"
                        )
                );
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : predictBtn);
            }
        });

        // Planning column (your existing planning button)
        planningColumn.setCellFactory(param -> new TableCell<Event, Void>() {
            private final Button planningBtn = createPlanningButton();

            {
                planningBtn.setOnAction(event -> {
                    Event e = getTableView().getItems().get(getIndex());
                    openPlanningPage(e);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : planningBtn);
            }
        });

        // Actions column - ICONS FOR EDIT/DELETE
        actionsColumn.setCellFactory(param -> new TableCell<Event, Void>() {
            private final HBox pane = new HBox(15);
            private final Label editIcon = createIconLabel("✏️", "Modifier", "#8B5CF6", "#6D28D9");
            private final Label deleteIcon = createIconLabel("🗑️", "Supprimer", "#EF4444", "#B91C1C");

            {
                pane.setAlignment(Pos.CENTER);
                pane.getChildren().addAll(editIcon, deleteIcon);

                editIcon.setOnMouseClicked(event -> {
                    Event e = getTableView().getItems().get(getIndex());
                    openEditWindow(e);
                });

                deleteIcon.setOnMouseClicked(event -> {
                    Event e = getTableView().getItems().get(getIndex());
                    deleteEvent(e);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private Label createIconLabel(String icon, String tooltip, String color, String hoverColor) {
        Label label = new Label(icon);
        label.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5;" +
                        "-fx-background-color: " + color + "20;" + // 20 = 12% opacity
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: " + color + ";"
        );

        // Tooltip
        Tooltip tp = new Tooltip(tooltip);
        tp.setStyle("-fx-font-size: 12px; -fx-background-color: #2D3748; -fx-text-fill: white;");
        Tooltip.install(label, tp);

        // Hover effects
        label.setOnMouseEntered(e -> {
            label.setStyle(
                    "-fx-font-size: 20px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 5;" +
                            "-fx-background-color: " + hoverColor + ";" +
                            "-fx-background-radius: 8;" +
                            "-fx-text-fill: white;"
            );
        });

        label.setOnMouseExited(e -> {
            label.setStyle(
                    "-fx-font-size: 20px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 5;" +
                            "-fx-background-color: " + color + "20;" +
                            "-fx-background-radius: 8;" +
                            "-fx-text-fill: " + color + ";"
            );
        });

        return label;
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                applyFilter(currentFilter); // Re-apply current filter
            } else {
                List<Event> sourceList = getFilteredEventsByStatus(currentFilter);
                List<Event> filtered = sourceList.stream()
                        .filter(e ->
                                // Recherche dans le titre (lettres)
                                e.getTitre().toLowerCase().contains(newText.toLowerCase()) ||
                                        // Recherche dans la description (lettres)
                                        e.getDescription().toLowerCase().contains(newText.toLowerCase()) ||
                                        // Recherche dans le lieu (lettres)
                                        e.getLieu().toLowerCase().contains(newText.toLowerCase()) ||
                                        // RECHERCHE DANS LE MAX PARTICIPANTS (chiffres)
                                        String.valueOf(e.getMaxParticipant()).contains(newText) ||
                                        // RECHERCHE DANS L'HEURE DE DÉBUT (chiffres)
                                        (e.getHeureDebut() != null && e.getHeureDebut().toString().contains(newText)) ||
                                        // RECHERCHE DANS L'HEURE DE FIN (chiffres)
                                        (e.getHeureFin() != null && e.getHeureFin().toString().contains(newText)) ||
                                        // RECHERCHE DANS LA DATE DE DÉBUT (chiffres)
                                        (e.getDateDebut() != null && e.getDateDebut().toString().contains(newText)) ||
                                        // RECHERCHE DANS LA DATE DE FIN (chiffres)
                                        (e.getDateFin() != null && e.getDateFin().toString().contains(newText))
                        )
                        .collect(Collectors.toList());
                eventObservableList.setAll(filtered);
            }
            updateCountLabel();
        });
    }

    private void setupFilters() {
        if (tousButton != null) {
            tousButton.setOnAction(e -> {
                currentFilter = "tous";
                resetFilterButtonStyles();
                tousButton.setStyle("-fx-background-color:#7B2FF7; -fx-text-fill:white; -fx-background-radius:20; -fx-padding: 8 25; -fx-font-weight: bold;");
                applyFilter("tous");
            });
        }

        if (actifButton != null) {
            actifButton.setOnAction(e -> {
                currentFilter = "actif";
                resetFilterButtonStyles();
                actifButton.setStyle("-fx-background-color:#7B2FF7; -fx-text-fill:white; -fx-background-radius:20; -fx-padding: 8 25; -fx-font-weight: bold;");
                applyFilter("actif");
            });
        }

        if (termineButton != null) {
            termineButton.setOnAction(e -> {
                currentFilter = "termine";
                resetFilterButtonStyles();
                termineButton.setStyle("-fx-background-color:#7B2FF7; -fx-text-fill:white; -fx-background-radius:20; -fx-padding: 8 25; -fx-font-weight: bold;");
                applyFilter("termine");
            });
        }

        if (annuleButton != null) {
            annuleButton.setOnAction(e -> {
                currentFilter = "annule";
                resetFilterButtonStyles();
                annuleButton.setStyle("-fx-background-color:#7B2FF7; -fx-text-fill:white; -fx-background-radius:20; -fx-padding: 8 25; -fx-font-weight: bold;");
                applyFilter("annule");
            });
        }
    }

    private void applyFilter(String filterType) {
        List<Event> filtered = getFilteredEventsByStatus(filterType);

        // Apply search text if any
        String searchText = searchField.getText();
        if (searchText != null && !searchText.isEmpty()) {
            filtered = filtered.stream()
                    .filter(e -> e.getTitre().toLowerCase().contains(searchText.toLowerCase()) ||
                            e.getDescription().toLowerCase().contains(searchText.toLowerCase()) ||
                            e.getLieu().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
        }

        eventObservableList.setAll(filtered);
        updateCountLabel();
    }

    private List<Event> getFilteredEventsByStatus(String filterType) {
        LocalDate today = LocalDate.now();
        Date currentDate = Date.valueOf(today);

        switch (filterType) {
            case "actif":
                // Events that are currently happening or will happen in the future
                return allEvents.stream()
                        .filter(e -> e.getDateFin() != null && !e.getDateFin().before(currentDate))
                        .collect(Collectors.toList());

            case "termine":
                // Events that have already ended
                return allEvents.stream()
                        .filter(e -> e.getDateFin() != null && e.getDateFin().before(currentDate))
                        .collect(Collectors.toList());

            case "annule":
                // You would need a status field in your Event entity for this
                // For now, return empty list
                return List.of();

            case "tous":
            default:
                return allEvents;
        }
    }

    private void resetFilterButtonStyles() {
        Button[] filters = {tousButton, actifButton, termineButton, annuleButton};
        for (Button btn : filters) {
            if (btn != null) {
                btn.setStyle("-fx-background-color:white; -fx-text-fill:#7B2FF7; -fx-background-radius:20; -fx-border-color:#7B2FF7; -fx-border-radius:20; -fx-padding: 8 25; -fx-font-weight: bold;");
            }
        }
    }

    private void updateCountLabel() {
        countLabel.setText(eventObservableList.size() + " Événements");
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

    private String formatTime(java.sql.Time time) {
        if (time == null) return "?";
        String timeStr = time.toString();
        return timeStr.length() >= 5 ? timeStr.substring(0, 5) : timeStr;
    }


    private void deleteEvent(Event e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer l'événement \"" + e.getTitre() + "\" ?");

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15;");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                eventServices.supprimerEvent(e.getIdEvent());
                refreshEvents();
                showAlert("Succès", "L'événement a été supprimé avec succès!");
            }
        });
    }

    private void refreshEvents() {
        allEvents = eventServices.afficherEvent();
        applyFilter(currentFilter);
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15;");

        alert.showAndWait();
    }

    private void openEditWindow(Event e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjouterEvent.fxml"));
            Parent form = loader.load();
            AddEventController controller = loader.getController();

            // ✅ Passer eventHome ET l'événement à modifier
            controller.setEventHome(eventHome);
            controller.setEventToEdit(e);

            // Remplacer le contenu
            if (eventHome != null) {
                eventHome.getContentArea().getChildren().setAll(form);
            } else {
                Stage stage = (Stage) eventTable.getScene().getWindow();
                stage.getScene().setRoot(form);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la modification");
        }
    }

    private void setupNavigation() {
        if (ajouterEventButton != null) {
            ajouterEventButton.setOnAction(ev -> {
                System.out.println("➕ Clic sur Ajouter - Ouverture du formulaire");
                openAddEventForm();
            });
        }
    }


    // Méthode pour trouver le dashboard controller
    private void openAddEventForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjouterEvent.fxml"));
            Parent form = loader.load();
            AddEventController controller = loader.getController();

            // ✅ Passer eventHome
            controller.setEventHome(eventHome);

            // Remplacer le contenu
            if (eventHome != null) {
                eventHome.getContentArea().getChildren().setAll(form);
            } else {
                Stage stage = (Stage) ajouterEventButton.getScene().getWindow();
                stage.getScene().setRoot(form);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    private Button createPlanningButton() {
        Button btn = new Button("📅");
        btn.setStyle(
                "-fx-background-color: #8b5cf6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 12;" +
                        "-fx-cursor: hand;"
        );

        Tooltip tp = new Tooltip("Voir le planning généré par IA");
        tp.setStyle("-fx-font-size: 12px; -fx-background-color: #2D3748; -fx-text-fill: white;");
        Tooltip.install(btn, tp);

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #6d28d9;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 12;" +
                        "-fx-cursor: hand;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #8b5cf6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 12;" +
                        "-fx-cursor: hand;"
        ));

        return btn;
    }

    private void openPlanningPage(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EventPlanning.fxml"));
            Parent root = loader.load();

            EventPlanningController controller = loader.getController();
            controller.setEvent(event);
            controller.setEventHome(eventHome);  // ← À AJOUTER DANS EventPlanningController
            controller.setFallbackService(fallbackService);

            // Remplacer le contenu
            if (eventHome != null) {
                eventHome.getContentArea().getChildren().setAll(root);
            } else {
                Stage stage = (Stage) eventTable.getScene().getWindow();
                stage.getScene().setRoot(root);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le planning");
        }
    }
}








