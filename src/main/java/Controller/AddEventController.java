package com.auticare.controllers;

import com.auticare.entities.Event;
import com.auticare.entities.Sponsor;
import com.auticare.services.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.auticare.controllers.EventHome;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AddEventController implements Initializable {

    @FXML
    private TextField titreField;
    private EventHome eventHome;


    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<String> typeBox;
    @FXML
    private TextField lieuField;

    @FXML
    private Spinner<Integer> maxParticipantsSpinner;
    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;
    @FXML
    private TextField heureDebutField;
    @FXML
    private TextField heureFinField;
    @FXML
    private TextField imagePathField;
    private FallbackService fallbackService;
    @FXML
    private ImageView eventImageView;
    @FXML
    private VBox sponsorCheckboxContainer;
    @FXML
    private Button ajouterButton;
    @FXML
    private Button annulerButton;
    @FXML
    private Button backButton;
    @FXML
    private Label titleLabel;
    @FXML
    private Button generateDescBtn;

    private GeminiService geminiService = new GeminiService();
    private Map<Control, Label> errorLabels = new HashMap<>();

    private EventServices eventServices;

    private SponsorServices sponsorServices;
    private Event eventToEdit;
    private List<CheckBox> sponsorCheckboxes;
    private String imagePath;




    // Styles pour la validation (seulement le texte, pas les champs)
    private static final String STYLE_TEXTE_VALIDE = "-fx-text-fill: #00C853; -fx-font-size: 11px; -fx-font-weight: bold;";
    private static final String STYLE_TEXTE_INVALIDE = "-fx-text-fill: #D32F2F; -fx-font-size: 11px; -fx-font-weight: bold;";
    private static final String STYLE_TEXTE_NEUTRE = "-fx-text-fill: #6B7280; -fx-font-size: 11px;";




    @Override
    public void initialize(URL url, ResourceBundle rb) {
        eventServices = new EventServices();
        sponsorServices = new SponsorServices();
        sponsorCheckboxes = new ArrayList<>();

        // Initialiser FallbackService avec ta clé Groq
        String groqKey = "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";  // TA CLÉ
        fallbackService = new FallbackService(groqKey);

        initializeComboBoxes();
        initializeSpinner();
        loadSponsorCheckboxes();
        configureActions();
        setupValidation();

        javafx.application.Platform.runLater(this::addErrorLabels);
        if (generateDescBtn != null) {
            generateDescBtn.setOnAction(e -> genererDescription());
        }
    }


    public void setEventHome(EventHome controller) {
        this.eventHome = controller;
        System.out.println("✅ EventHome lié au AddEventController");
    }


    /**
     * Ajoute les labels d'erreur après chaque champ
     */
    private void addErrorLabels() {
        addErrorLabelAfter(titreField, "Titre");
        addErrorLabelAfter(descriptionField, "Description");
        addErrorLabelAfter(typeBox, "Type");
        addErrorLabelAfter(lieuField, "Lieu");
        addErrorLabelAfter(dateDebutPicker, "Date de début");
        addErrorLabelAfter(dateFinPicker, "Date de fin");
        addErrorLabelAfter(heureDebutField, "Heure de début");
        addErrorLabelAfter(heureFinField, "Heure de fin");
        addErrorLabelAfter(imagePathField, "Image");
    }


    /**
     * Ajoute un label d'erreur après un champ spécifique
     */
    private void addErrorLabelAfter(Control field, String fieldName) {
        // Créer le label d'erreur
        Label errorLabel = new Label();
        errorLabel.setStyle(STYLE_TEXTE_NEUTRE);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Trouver le parent du champ
        javafx.scene.Node parent = field.getParent();
        if (parent instanceof VBox) {
            VBox vbox = (VBox) parent;
            int index = vbox.getChildren().indexOf(field);
            if (index >= 0 && index + 1 < vbox.getChildren().size()) {
                vbox.getChildren().add(index + 1, errorLabel);
            } else {
                vbox.getChildren().add(errorLabel);
            }
        }

        // Stocker le label dans la map
        errorLabels.put(field, errorLabel);
    }

    /**
     * Met à jour le texte et la couleur du label de validation
     */
    private void updateValidationLabel(Control field, String message, boolean isValid) {
        Label errorLabel = errorLabels.get(field);
        if (errorLabel != null) {
            if (message == null || message.isEmpty()) {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            } else {
                errorLabel.setText(message);
                if (isValid) {
                    errorLabel.setStyle(STYLE_TEXTE_VALIDE);
                } else {
                    errorLabel.setStyle(STYLE_TEXTE_INVALIDE);
                }
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        }
    }

    /**
     * Affiche un message d'erreur pour un champ (invalide)
     */
    private void showError(Control field, String message) {
        updateValidationLabel(field, "❌ " + message, false);
    }

    /**
     * Affiche un message de succès pour un champ (valide)
     */
    private void showSuccess(Control field, String message) {
        updateValidationLabel(field, "✓ " + message, true);
    }

    /**
     * Cache le message de validation pour un champ
     */
    private void hideValidation(Control field) {
        updateValidationLabel(field, null, false);
    }

    private void initializeComboBoxes() {
        ObservableList<String> types = FXCollections.observableArrayList(
                "Workshop", "Training", "Conference", "Social Activity", "Therapy Session"
        );
        typeBox.setItems(types);
    }

    private void initializeSpinner() {
        maxParticipantsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10)
        );
    }

    private void configureActions() {
        ajouterButton.setOnAction(event -> {
            if (eventToEdit != null) {
                modifierEventExistant();
            } else {
                ajouterEvent();
            }
        });
        annulerButton.setOnAction(event -> annuler());
    }

    /**
     * Configure la validation en temps réel pour tous les champs
     */
    private void setupValidation() {
        // Validation du titre
        titreField.textProperty().addListener((obs, old, newValue) -> {
            validerTitre();
        });
        titreField.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerTitre();
            }
        });

        // Validation de la description
        descriptionField.textProperty().addListener((obs, old, newValue) -> {
            validerDescription();
        });
        descriptionField.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerDescription();
            }
        });

        // Validation du type
        typeBox.valueProperty().addListener((obs, old, newValue) -> {
            validerType();
        });

        // Validation du lieu
        lieuField.textProperty().addListener((obs, old, newValue) -> {
            validerLieu();
        });
        lieuField.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerLieu();
            }
        });

        // Validation des dates
        dateDebutPicker.valueProperty().addListener((obs, old, newValue) -> {
            validerDateDebut();
            validerDateFin();
        });
        dateFinPicker.valueProperty().addListener((obs, old, newValue) -> {
            validerDateFin();
        });

        // Validation des heures
        heureDebutField.textProperty().addListener((obs, old, newValue) -> {
            validerHeureDebut();
            validerHeureFin();
        });
        heureDebutField.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerHeureDebut();
            }
        });

        heureFinField.textProperty().addListener((obs, old, newValue) -> {
            validerHeureFin();
        });
        heureFinField.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerHeureFin();
            }
        });

        // Validation de l'image
        imagePathField.textProperty().addListener((obs, old, newValue) -> {
            validerImage();
        });
    }

    /**
     * Valide le champ titre - doit contenir au moins une lettre
     */
    private boolean validerTitre() {
        String titre = titreField.getText();

        if (titre == null || titre.trim().isEmpty()) {
            showError(titreField, "Le titre est obligatoire");
            return false;
        }

        String trimmed = titre.trim();

        if (trimmed.length() < 3) {
            showError(titreField, "Le titre doit contenir au moins 3 caractères");
            return false;
        }

        if (trimmed.length() > 50) {
            showError(titreField, "Le titre ne doit pas dépasser 50 caractères");
            return false;
        }

        // Vérifier qu'il y a au moins une lettre (pas seulement des chiffres)
        boolean hasLetter = false;
        for (char c : trimmed.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
                break;
            }
        }

        if (!hasLetter) {
            showError(titreField, "Le titre doit contenir au moins une lettre");
            return false;
        }

        showSuccess(titreField, "Titre valide");
        return true;
    }

    /**
     * Valide le champ description
     */

    private boolean validerDescription() {
        String description = descriptionField.getText();

        if (description == null || description.trim().isEmpty()) {
            showError(descriptionField, "La description est obligatoire");
            return false;
        }

        String trimmed = description.trim();

        if (trimmed.length() < 10) {
            showError(descriptionField, "La description doit contenir au moins 10 caractères");
            return false;
        }

        if (trimmed.length() > 500) {
            showError(descriptionField, "La description ne doit pas dépasser 500 caractères");
            return false;
        }

        showSuccess(descriptionField, "Description valide");
        return true;
    }

    /**
     * Valide le champ type
     */
    private boolean validerType() {
        String type = typeBox.getValue();

        if (type == null || type.isEmpty()) {
            showError(typeBox, "Veuillez sélectionner un type d'événement");
            return false;
        }

        showSuccess(typeBox, "Type valide");
        return true;
    }

    /**
     * Valide le champ lieu
     */
    private boolean validerLieu() {
        String lieu = lieuField.getText();

        if (lieu == null || lieu.trim().isEmpty()) {
            showError(lieuField, "Le lieu est obligatoire");
            return false;
        }

        String trimmed = lieu.trim();

        if (trimmed.length() < 3) {
            showError(lieuField, "Le lieu doit contenir au moins 3 caractères");
            return false;
        }

        if (trimmed.length() > 100) {
            showError(lieuField, "Le lieu ne doit pas dépasser 100 caractères");
            return false;
        }

        showSuccess(lieuField, "Lieu valide");
        return true;
    }

    /**
     * Valide la date de début
     */
    private boolean validerDateDebut() {
        LocalDate dateDebut = dateDebutPicker.getValue();

        if (dateDebut == null) {
            showError(dateDebutPicker, "La date de début est obligatoire");
            return false;
        }

        if (dateDebut.isBefore(LocalDate.now())) {
            showError(dateDebutPicker, "La date de début doit être future");
            return false;
        }

        showSuccess(dateDebutPicker, "Date de début valide");
        return true;
    }

    /**
     * Valide la date de fin
     */
    private boolean validerDateFin() {
        LocalDate dateFin = dateFinPicker.getValue();
        LocalDate dateDebut = dateDebutPicker.getValue();

        if (dateFin == null) {
            showError(dateFinPicker, "La date de fin est obligatoire");
            return false;
        }

        if (dateDebut != null && dateFin.isBefore(dateDebut)) {
            showError(dateFinPicker, "La date de fin doit être après la date de début");
            return false;
        }

        showSuccess(dateFinPicker, "Date de fin valide");
        return true;
    }

    /**
     * Valide l'heure de début
     */
    private boolean validerHeureDebut() {
        String heureDebut = heureDebutField.getText();

        if (heureDebut == null || heureDebut.trim().isEmpty()) {
            showError(heureDebutField, "L'heure de début est obligatoire");
            return false;
        }

        if (!heureDebut.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
            showError(heureDebutField, "Format HH:MM invalide (ex: 14:30)");
            return false;
        }

        showSuccess(heureDebutField, "Heure de début valide");
        return true;
    }

    /**
     * Valide l'heure de fin
     */
    private boolean validerHeureFin() {
        String heureFin = heureFinField.getText();
        String heureDebut = heureDebutField.getText();

        if (heureFin == null || heureFin.trim().isEmpty()) {
            showError(heureFinField, "L'heure de fin est obligatoire");
            return false;
        }

        if (!heureFin.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
            showError(heureFinField, "Format HH:MM invalide (ex: 16:30)");
            return false;
        }

        // Vérifier l'ordre si les deux heures sont valides
        if (validerHeureDebut() && !heureDebut.isEmpty() && !heureFin.isEmpty()) {
            try {
                LocalTime debut = LocalTime.parse(heureDebut + ":00");
                LocalTime fin = LocalTime.parse(heureFin + ":00");
                if (!fin.isAfter(debut)) {
                    showError(heureFinField, "L'heure de fin doit être après l'heure de début");
                    return false;
                }
            } catch (Exception e) {
                // Ignorer les erreurs de parsing
            }
        }

        showSuccess(heureFinField, "Heure de fin valide");
        return true;
    }


    /**
     * Valide tous les champs du formulaire
     */
    private boolean validateAllFields() {
        boolean titreValide = validerTitre();
        boolean descriptionValide = validerDescription();
        boolean typeValide = validerType();
        boolean lieuValide = validerLieu();
        boolean dateDebutValide = validerDateDebut();
        boolean dateFinValide = validerDateFin();
        boolean heureDebutValide = validerHeureDebut();
        boolean heureFinValide = validerHeureFin();
        boolean imageValide = validerImage();

        // DEBUG : Afficher les résultats de validation
        System.out.println("🔍 Validation résultats:");
        System.out.println("   Titre: " + titreValide);
        System.out.println("   Description: " + descriptionValide);
        System.out.println("   Type: " + typeValide);
        System.out.println("   Lieu: " + lieuValide);
        System.out.println("   Date début: " + dateDebutValide);
        System.out.println("   Date fin: " + dateFinValide);
        System.out.println("   Heure début: " + heureDebutValide);
        System.out.println("   Heure fin: " + heureFinValide);
        System.out.println("   Image: " + imageValide);

        return titreValide && descriptionValide && typeValide && lieuValide &&
                dateDebutValide && dateFinValide && heureDebutValide && heureFinValide && imageValide;
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(ajouterButton.getScene().getWindow());
        if (file != null) {
            imagePath = file.getAbsolutePath();
            imagePathField.setText(imagePath);
            Image image = new Image(file.toURI().toString());
            eventImageView.setImage(image);
            eventImageView.setFitWidth(200);
            eventImageView.setFitHeight(150);
            eventImageView.setPreserveRatio(false);
            validerImage();
        }
    }

    @FXML
    private Button generateImageBtn;
    @FXML
    private ProgressIndicator imageLoadingIndicator;
    @FXML
    private Label imageStatusLabel;

    private void loadSponsorCheckboxes() {
        if (sponsorCheckboxContainer == null) return;

        sponsorCheckboxContainer.getChildren().clear();
        sponsorCheckboxes.clear();

        List<Sponsor> allSponsors = sponsorServices.afficherSponsor();

        List<Integer> linkedSponsorIds = new ArrayList<>();
        if (eventToEdit != null) {
            List<Sponsor> linkedSponsors = eventServices.getSponsorsForEvent(eventToEdit.getIdEvent());
            for (Sponsor s : linkedSponsors) {
                linkedSponsorIds.add(s.getIdSponsor());
            }
        }

        for (Sponsor s : allSponsors) {
            CheckBox cb = new CheckBox(s.getNom() + " (" + s.getTypeSponsor() + ")");
            cb.setUserData(s.getIdSponsor());
            cb.setStyle("-fx-padding: 3 0; -fx-font-size: 14px;");

            if (linkedSponsorIds.contains(s.getIdSponsor())) {
                cb.setSelected(true);
            }

            sponsorCheckboxes.add(cb);
            sponsorCheckboxContainer.getChildren().add(cb);
        }

        if (allSponsors.isEmpty()) {
            Label emptyLabel = new Label("Aucun sponsor disponible");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 10;");
            sponsorCheckboxContainer.getChildren().add(emptyLabel);
        }
    }

    @FXML
    private void openAddSponsorForm() {
        try {
            // ✅ NOUVEAU CHEMIN: /views/AddSponsorForm.fxml (plus de sponsor/)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddSponsorForm.fxml"));
            Parent root = loader.load();
            AddSponsorController controller = loader.getController();
            controller.setEventHome(eventHome);
            controller.setOnSponsorAdded(() -> loadSponsorCheckboxes());

            Stage stage = new Stage();
            stage.setTitle("Ajouter un sponsor");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire sponsor");
        }
    }

    private boolean validerImage() {
        String image = imagePathField.getText();

        // Image téléchargée (fichier local)
        if (image == null || image.trim().isEmpty()) {
            showError(imagePathField, "L'image est obligatoire");
            return false;
        }

        File file = new File(image);
        if (!file.exists()) {
            showError(imagePathField, "Le fichier image n'existe pas");
            return false;
        }

        String lowercase = image.toLowerCase();
        if (lowercase.endsWith(".png") || lowercase.endsWith(".jpg") ||
                lowercase.endsWith(".jpeg") || lowercase.endsWith(".gif")) {
            showSuccess(imagePathField, "Image valide");
            return true;
        } else {
            showError(imagePathField, "Format non supporté (PNG, JPG, JPEG, GIF)");
            return false;
        }
    }

    @FXML
    private void ajouterEvent() {
        if (!validateAllFields()) {
            showAlert("Validation", "Veuillez corriger les erreurs dans le formulaire");
            return;
        }

        try {
            String titre = titreField.getText().trim();
            String description = descriptionField.getText().trim();
            String typeEvent = typeBox.getValue();
            String lieu = lieuField.getText().trim();
            int maxParticipant = maxParticipantsSpinner.getValue();
            String imagePath = imagePathField.getText().trim();

            Date dateDebut = Date.valueOf(dateDebutPicker.getValue());
            Date dateFin = Date.valueOf(dateFinPicker.getValue());
            Time heureDebut = Time.valueOf(heureDebutField.getText() + ":00");
            Time heureFin = Time.valueOf(heureFinField.getText() + ":00");

            addNewEvent(titre, description, typeEvent, lieu, maxParticipant,
                    dateDebut, dateFin, heureDebut, heureFin, imagePath);

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void modifierEventExistant() {
        if (!validateAllFields()) {
            showAlert("Validation", "Veuillez corriger les erreurs dans le formulaire");
            return;
        }

        try {
            String titre = titreField.getText().trim();
            String description = descriptionField.getText().trim();
            String typeEvent = typeBox.getValue();
            String lieu = lieuField.getText().trim();
            int maxParticipant = maxParticipantsSpinner.getValue();
            String imagePath = imagePathField.getText().trim();

            Date dateDebut = Date.valueOf(dateDebutPicker.getValue());
            Date dateFin = Date.valueOf(dateFinPicker.getValue());
            Time heureDebut = Time.valueOf(heureDebutField.getText() + ":00");
            Time heureFin = Time.valueOf(heureFinField.getText() + ":00");

            updateEvent(eventToEdit, titre, description, typeEvent, lieu, maxParticipant,
                    dateDebut, dateFin, heureDebut, heureFin, imagePath);

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addNewEvent(String titre, String description, String typeEvent, String lieu,
                             int maxParticipant, Date dateDebut, Date dateFin,
                             Time heureDebut, Time heureFin, String imagePath) {
        try {
            // ✅ Correction : Ajouter un planning par défaut (chaîne vide au lieu de null)
            Event newEvent = new Event(
                    titre, description, typeEvent, lieu, maxParticipant,
                    dateDebut, dateFin, heureDebut, heureFin, imagePath,
                    ""  // ← CHANGÉ : "" au lieu de null
            );

            eventServices.ajoutEvent(newEvent);

            int eventId = getLastInsertedEventId();

            for (CheckBox cb : sponsorCheckboxes) {
                if (cb.isSelected()) {
                    int sponsorId = (int) cb.getUserData();
                    eventServices.addSponsorToEvent(eventId, sponsorId);
                }
            }

            showAlert("Succès", "Événement ajouté avec succès!");
            returnToEventTable();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec de l'ajout de l'événement: " + e.getMessage());
        }
    }

    private void updateEvent(Event event, String titre, String description, String typeEvent, String lieu,
                             int maxParticipant, Date dateDebut, Date dateFin,
                             Time heureDebut, Time heureFin, String imagePath) {
        try {
            event.setTitre(titre);
            event.setDescription(description);
            event.setTypeEvent(typeEvent);
            event.setLieu(lieu);
            event.setMaxParticipant(maxParticipant);
            event.setDateDebut(dateDebut);
            event.setDateFin(dateFin);
            event.setHeureDebut(heureDebut);
            event.setHeureFin(heureFin);
            event.setImage(imagePath);
            // Garde le planning existant (ne pas l'écraser)
            // event.setPlanning(existingPlanning); // Si besoin

            eventServices.modifierEvent(event);

            eventServices.removeAllSponsorsFromEvent(event.getIdEvent());
            for (CheckBox cb : sponsorCheckboxes) {
                if (cb.isSelected()) {
                    int sponsorId = (int) cb.getUserData();
                    eventServices.addSponsorToEvent(event.getIdEvent(), sponsorId);
                }
            }

            showAlert("Succès", "Événement modifié avec succès!");
            returnToEventTable();

        } catch (Exception e) {
            showAlert("Erreur", "Échec de la modification de l'événement.");
            e.printStackTrace();
        }
    }

    private int getLastInsertedEventId() {
        List<Event> events = eventServices.afficherEvent();
        return events.isEmpty() ? -1 : events.get(events.size() - 1).getIdEvent();
    }
    //modificationnnnn

    public void setEventToEdit(Event event) {
        this.eventToEdit = event;

        if (event != null) {
            // Mode MODIFICATION
            titreField.setText(event.getTitre());
            descriptionField.setText(event.getDescription());
            typeBox.setValue(event.getTypeEvent());
            lieuField.setText(event.getLieu());
            maxParticipantsSpinner.getValueFactory().setValue(event.getMaxParticipant());
            dateDebutPicker.setValue(event.getDateDebut().toLocalDate());
            dateFinPicker.setValue(event.getDateFin().toLocalDate());
            heureDebutField.setText(event.getHeureDebut().toString().substring(0, 5));
            heureFinField.setText(event.getHeureFin().toString().substring(0, 5));

            // Changer le titre
            if (titleLabel != null) {
                titleLabel.setText("Modifier l'événement");
            }

            // Changer le bouton
            ajouterButton.setText("Modifier");

            if (event.getImage() != null && !event.getImage().isEmpty()) {
                imagePathField.setText(event.getImage());
                try {
                    File imgFile = new File(event.getImage());
                    if (imgFile.exists()) {
                        eventImageView.setImage(new Image(imgFile.toURI().toString()));
                        eventImageView.setFitWidth(200);
                        eventImageView.setFitHeight(150);
                        eventImageView.setPreserveRatio(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Valider tous les champs après chargement
            validateAllFields();
            loadSponsorCheckboxes();
        } else {
            // Mode AJOUT
            clearFields();
            if (titleLabel != null) {
                titleLabel.setText("Ajouter un événement");
            }
            ajouterButton.setText("Ajouter");
        }
    }

    private void clearFields() {
        titreField.clear();
        hideValidation(titreField);

        descriptionField.clear();
        hideValidation(descriptionField);

        typeBox.setValue(null);
        hideValidation(typeBox);

        lieuField.clear();
        hideValidation(lieuField);

        maxParticipantsSpinner.getValueFactory().setValue(10);

        dateDebutPicker.setValue(null);
        hideValidation(dateDebutPicker);

        dateFinPicker.setValue(null);
        hideValidation(dateFinPicker);

        heureDebutField.clear();
        hideValidation(heureDebutField);

        heureFinField.clear();
        hideValidation(heureFinField);

        imagePathField.clear();
        hideValidation(imagePathField);

        eventImageView.setImage(null);
    }

    @FXML
    private void annuler() {
        if (eventToEdit != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Annuler la modification");
            alert.setContentText("Voulez-vous vraiment annuler la modification en cours ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                clearFields();
                eventToEdit = null;
                ajouterButton.setText("Ajouter");
                returnToEventTable();
            }
        } else {
            clearFields();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goBackToEventTable() {
        returnToEventTable();
    }





    private void returnToEventTable() {
        if (eventHome != null) {
            eventHome.loadEventsTable();
        } else {
            try {
                // ✅ NOUVEAU CHEMIN: /views/EventTable.fxml (plus de event/)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EventTable.fxml"));
                Parent table = loader.load();
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.setScene(new Scene(table));
                stage.setTitle("Liste des événements");
                stage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de retourner à la liste");
            }
        }
    }



    private void genererDescription() {
        String titre = titreField.getText().trim();
        String type = typeBox.getValue();
        String lieu = lieuField.getText().trim();

        if (titre.isEmpty()) {
            showAlert("Attention", "Veuillez d'abord saisir un titre");
            return;
        }

        final Button btn = generateDescBtn;
        final TextArea descArea = descriptionField;

        btn.setDisable(true);
        btn.setText("⏳ Génération...");

        new Thread(() -> {
            try {
                // Styles variés pour des descriptions longues
                String[] themes = {
                        "découverte sensorielle", "aventure créative", "exploration bienveillante",
                        "moment de partage", "expérience immersive", "atelier découverte",
                        "parcours adapté", "rencontre magique", "voyage des sens"
                };

                String theme = themes[new java.util.Random().nextInt(themes.length)];

                String prompt = String.format(
                        "Rédige une description détaillée et chaleureuse pour l'événement '%s'.\n\n" +
                                "Informations:\n" +
                                "- Titre: %s\n" +
                                "- Type: %s\n" +
                                "- Lieu: %s\n\n" +
                                "IMPORTANT - Respecte ces consignes:\n" +
                                "1. ✅ Description de 100 caractères (ni trop courte, ni trop longue)\n" +
                                "2. ✅ Adaptée aux enfants autistes (langage simple, positif, rassurant)\n" +
                                "3. ✅ Mentionne l'ambiance calme et bienveillante\n" +
                                "4. ✅ Donne envie de participer\n" +
                                "5. ✅ UNIQUEMENT la description, sans guillemets ni introduction\n\n" +
                                "Thème suggéré: %s\n\n" +
                                "Rédige maintenant une description complète et attrayante:",
                        titre, titre,
                        type != null ? type : "activité",
                        lieu.isEmpty() ? "un espace adapté" : lieu,
                        theme
                );

                String description = fallbackService.genererDescription(prompt);

                javafx.application.Platform.runLater(() -> {
                    descArea.setText(description);
                    validerDescription();
                    btn.setDisable(false);
                    btn.setText("🤖 Générer");

                    // Afficher le nombre de caractères pour vérifier
                    System.out.println("📝 Description générée: " + description.length() + " caractères");
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showAlert("Erreur", "Impossible de générer la description");
                    btn.setDisable(false);
                    btn.setText("🤖 Générer");
                });
            }
        }).start();
    }
}
    // En haut de la classe, avec les autres @FXML


