package Controller;

import Entites.Event;
import Services.EventServices;
import Utils.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class EventTableController {

    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> titreColumn;
    @FXML private TableColumn<Event, String> descriptionColumn;
    @FXML private TableColumn<Event, Date> dateDebutColumn;
    @FXML private TableColumn<Event, Date> dateFinColumn;
    @FXML private TableColumn<Event, Time> heureDebutColumn;
    @FXML private TableColumn<Event, Time> heureFinColumn;
    @FXML private TableColumn<Event, String> lieuColumn;
    @FXML private TableColumn<Event, Integer> maxParticipantColumn;
    @FXML private TableColumn<Event, String> typeEventColumn;
    @FXML private TableColumn<Event, String> imageColumn;
    @FXML private TableColumn<Event, Void> actionsColumn;

    @FXML private Button tousButton, actifButton, termineButton, annuleButton;
    @FXML private Button ajouterEventButton;

    private EventServices eventServices = new EventServices();
    private List<Event> allEvents;
    private ObservableList<Event> eventObservableList;
    private String currentFilter = "tous"; // Track current filter

    @FXML
    public void initialize() {
        // Load all events
        allEvents = eventServices.afficherEvent();
        eventObservableList = FXCollections.observableArrayList(allEvents);

        // Setup table columns
        setupTableColumns();
        

        // Set items to table
        eventTable.setItems(eventObservableList);

        // Update count label
        updateCountLabel();

        // Setup search and filters
        setupSearch();
        setupFilters();
        setupNavigation();

        // Add CSS class to table
        eventTable.getStyleClass().add("event-table");
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

        // Event type column - NORMAL TEXT, NO BUTTON
        typeEventColumn.setCellValueFactory(new PropertyValueFactory<>("typeEvent"));

        // Actions column - ICONS INSTEAD OF BUTTONS
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

        switch(filterType) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEvent.fxml"));
            Parent form = loader.load();
            AddEventController controller = loader.getController();

            // ✅ PASSE LA RÉFÉRENCE DU DASHBOARD
            MainDashboardController dashboardController = getDashboardController();
            controller.setDashboardController(dashboardController);

            controller.setEventToEdit(e);

            // Remplacer le contenu du dashboard
            dashboardController.getContentArea().getChildren().setAll(form);

        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification: " + ex.getMessage());
        }
    }



    // Méthode pour trouver le dashboard controller

    private void openAddEventForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEvent.fxml"));
            Parent form = loader.load();
            AddEventController controller = loader.getController();

            // ✅ PASSE LA RÉFÉRENCE DU DASHBOARD
            MainDashboardController dashboardController = getDashboardController();
            controller.setDashboardController(dashboardController);

            // ✅ REMPLACE LE CONTENU, PAS TOUTE LA SCÈNE
            dashboardController.getContentArea().getChildren().setAll(form);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }
    private MainDashboardController dashboardController;

    public void setDashboardController(MainDashboardController controller) {
        this.dashboardController = controller;
        System.out.println("✅ Dashboard controller set dans EventTableController: " + (controller != null));
    }

    // Et modifie la méthode getDashboardController()
    private MainDashboardController getDashboardController() {
        return dashboardController;
    }
    private void setupNavigation() {
        if (ajouterEventButton != null) {
            ajouterEventButton.setOnAction(ev -> {
                System.out.println("➕ Clic sur Ajouter - Ouverture du formulaire");
                openAddEventForm();
            });
        }
    }


}