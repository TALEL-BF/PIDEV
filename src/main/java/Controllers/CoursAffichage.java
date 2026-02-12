package Controllers;

import Entites.Cours;
import Services.CoursServices;
import Utils.Navigation;  // 🔴 IMPORTER LA CLASSE NAVIGATION
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CoursAffichage implements Initializable {

    @FXML private Label coursCountLabel;
    @FXML private FlowPane coursFlowPane;
    @FXML private TextField searchField;
    @FXML private Button academicButton, socialButton, autonomieButton, creativiteButton;

    // 🔴 NOUVEAUX BOUTONS pour la navigation
    @FXML private Button backToAdminButton;
    @FXML private Button ajouterCoursButton;

    private CoursServices coursServices;
    private List<Cours> allCours;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coursServices = new CoursServices();
        loadCours();
        setupSearch();
        setupFilters();

        // 🔴 AJOUTER LA NAVIGATION
        setupNavigation();
    }

    // 🔴 **NOUVELLE MÉTHODE : Navigation vers le back-office**
    private void setupNavigation() {
        // Retour à l'interface d'administration
        if (backToAdminButton != null) {
            backToAdminButton.setOnAction(event -> {
                System.out.println("🔄 Navigation vers l'administration...");
                Navigation.navigateTo("coursajout.fxml", "Administration des cours");
            });
        }

        // Ajouter un nouveau cours (navigation directe vers formulaire)
        if (ajouterCoursButton != null) {
            ajouterCoursButton.setOnAction(event -> {
                System.out.println("➕ Navigation vers l'ajout de cours...");
                Navigation.navigateTo("coursajout.fxml", "Ajouter un cours");
            });
        }
    }

    private void loadCours() {
        allCours = coursServices.getAll();
        coursCountLabel.setText(allCours.size() + " Cours disponibles");
        displayCours(allCours);
    }

    private void displayCours(List<Cours> coursList) {
        coursFlowPane.getChildren().clear();

        for (Cours cours : coursList) {
            VBox courseCard = createCourseCard(cours);
            coursFlowPane.getChildren().add(courseCard);
        }
    }

    // 🔴 **AMÉLIORATION : Ajouter un bouton "Voir plus" sur chaque carte**
    private VBox createCourseCard(Cours cours) {
        // Votre code existant pour créer la carte...
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setPrefHeight(250); // Augmenté pour le bouton
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 15; " +
                "-fx-border-radius: 15; " +
                "-fx-border-color: #EEEEEE; " +
                "-fx-border-width: 1; " +
                "-fx-padding: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        // Effet hover
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #7B2FF7; -fx-border-width: 2; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.1), 15, 0, 0, 5);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #EEEEEE; -fx-border-width: 1; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);"));

        // Emoji
        Label emojiLabel = new Label(getEmojiForCours(cours));
        emojiLabel.setStyle("-fx-font-size: 40px;");

        // Titre
        Label titleLabel = new Label(cours.getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");

        // Description
        Label descriptionLabel = new Label(cours.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
        descriptionLabel.setPrefHeight(40);

        // Footer
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);

        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        ratingBox.getChildren().addAll(new Label("⭐"), new Label("4.8"));

        HBox participantsBox = new HBox(5);
        participantsBox.setAlignment(Pos.CENTER_LEFT);
        participantsBox.getChildren().addAll(new Label("👥"), new Label("124"));

        footer.getChildren().addAll(ratingBox, participantsBox);

        // 🔴 **NOUVEAU : Bouton "Voir plus" pour naviguer vers les détails**
        Button voirPlusBtn = new Button("Voir plus");
        voirPlusBtn.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");

        voirPlusBtn.setOnAction(e -> {
            System.out.println("👁 Détails du cours: " + cours.getTitre());
            // Option: Ouvrir une fenêtre de détails
            showCourseDetails(cours);
        });

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(emojiLabel);

        card.getChildren().addAll(headerBox, titleLabel, descriptionLabel, footer, voirPlusBtn);
        return card;
    }

    // 🔴 **NOUVELLE MÉTHODE : Afficher les détails d'un cours**
    private void showCourseDetails(Cours cours) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du cours");
        alert.setHeaderText(cours.getTitre());

        String details = String.format(
                "📌 Type: %s\n" +
                        "📊 Niveau: %s\n" +
                        "⏱️ Durée: %d minutes\n" +
                        "📝 Description: %s\n" +
                        "🖼️ Image: %s\n" +
                        "📋 ID: %d",
                cours.getType_cours(),
                cours.getNiveau(),
                cours.getDuree(),
                cours.getDescription(),
                cours.getImage() != null ? cours.getImage() : "Aucune",
                cours.getId_cours()
        );

        alert.setContentText(details);

        // Ajouter un bouton pour modifier
        ButtonType modifierBtn = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        ButtonType fermerBtn = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(modifierBtn, fermerBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == modifierBtn) {
                // Navigation vers l'admin avec ce cours
                Navigation.navigateTo("coursajout.fxml", "Modifier: " + cours.getTitre());
            }
        });
    }

    // Vos méthodes existantes...
    private String getEmojiForCours(Cours cours) {
        String titre = cours.getTitre().toLowerCase();
        if (titre.contains("math") || titre.contains("chiffre")) return "🧮";
        else if (titre.contains("social") || titre.contains("interaction")) return "💬";
        else if (titre.contains("francais") || titre.contains("langue")) return "📚";
        else if (titre.contains("nature")) return "🌿";
        else if (titre.contains("animal")) return "🐶";
        else if (titre.contains("art") || titre.contains("dessin")) return "🎨";
        else return "📘";
    }

    private String getFlagForCours(Cours cours) {
        String niveau = cours.getNiveau();
        if (niveau != null) {
            switch (niveau) {
                case "Débutant": return "🇫🇷";
                case "Intermédiaire": return "🇬🇧";
                case "Avancé": return "🇨🇦";
                case "Expert": return "🇺🇸";
                default: return "🌍";
            }
        }
        return "🌍";
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCours(newValue, null);
        });
    }

    private void setupFilters() {
        academicButton.setOnAction(e -> filterCours(null, "Académique"));
        socialButton.setOnAction(e -> filterCours(null, "Social"));
        autonomieButton.setOnAction(e -> filterCours(null, "Autonomie"));
        creativiteButton.setOnAction(e -> filterCours(null, "Créativité"));
    }

    private void filterCours(String searchText, String category) {
        List<Cours> filtered = allCours;

        if (searchText != null && !searchText.isEmpty()) {
            filtered = filtered.stream()
                    .filter(c -> c.getTitre().toLowerCase().contains(searchText.toLowerCase()) ||
                            c.getDescription().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (category != null) {
            filtered = filtered.stream()
                    .filter(c -> c.getType_cours().equals(category) ||
                            c.getNiveau().contains(category))
                    .collect(java.util.stream.Collectors.toList());
        }

        coursCountLabel.setText(filtered.size() + " Cours disponibles");
        displayCours(filtered);
    }
}