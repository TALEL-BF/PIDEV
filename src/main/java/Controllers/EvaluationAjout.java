package Controllers;

import Entites.Cours;
import Entites.Evaluation;
import Services.CoursServices;
import Services.EvaluationServices;
import Utils.Navigation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Optional;

public class EvaluationAjout implements Initializable {

    @FXML private ComboBox<Cours> coursCombo;
    @FXML private ComboBox<String> filterCoursCombo;
    @FXML private Label totalQuestionsLabel;
    @FXML private Label formTitleLabel;
    @FXML private TextArea questionArea;
    @FXML private TextField choix1Field;
    @FXML private TextField choix2Field;
    @FXML private TextField choix3Field;
    @FXML private ComboBox<String> bonneReponseCombo;
    @FXML private TextField scoreField;
    @FXML private Button ajouterQuestionBtn;
    @FXML private Button modifierQuestionBtn;
    @FXML private Button annulerModificationBtn;
    @FXML private Button retourButton;

    @FXML private TableView<Evaluation> questionsTable;
    @FXML private TableColumn<Evaluation, String> questionColumn;
    @FXML private TableColumn<Evaluation, String> coursColumn;  // Nouvelle colonne pour le cours
    @FXML private TableColumn<Evaluation, String> choix1Column;
    @FXML private TableColumn<Evaluation, String> choix2Column;
    @FXML private TableColumn<Evaluation, String> choix3Column;
    @FXML private TableColumn<Evaluation, String> bonneReponseColumn;
    @FXML private TableColumn<Evaluation, Integer> scoreColumn;
    @FXML private TableColumn<Evaluation, Void> actionsColumn;

    // Map pour les labels d'erreur
    private Map<Control, Label> errorLabels = new HashMap<>();

    // Styles pour la validation
    private static final String STYLE_VALIDE = "-fx-border-color: #00C853; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;";
    private static final String STYLE_INVALIDE = "-fx-border-color: #D32F2F; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;";

    private EvaluationServices evaluationServices;
    private CoursServices coursServices;
    private ObservableList<Evaluation> questionsList;
    private ObservableList<Cours> coursList;
    private Integer coursFiltreId = null;
    private Evaluation questionEnModification = null;

    // Map pour stocker tous les cours et les retrouver facilement
    private Map<Integer, Cours> coursMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        evaluationServices = new EvaluationServices();
        coursServices = new CoursServices();

        // Récupérer l'ID du cours depuis les paramètres
        Map<String, String> params = Navigation.getParameters();
        if (params.containsKey("coursId")) {
            try {
                coursFiltreId = Integer.parseInt(params.get("coursId"));
            } catch (NumberFormatException e) {
                coursFiltreId = null;
            }
        }

        initializeComboBoxes();
        initializeTable();
        loadCours();
        loadQuestions();
        setupValidation();
        setupListeners();

        // Ajouter les labels d'erreur après l'initialisation
        javafx.application.Platform.runLater(this::addErrorLabels);
    }

    /**
     * Ajoute les labels d'erreur après chaque champ
     */
    private void addErrorLabels() {
        addErrorLabelAfter(questionArea, "Question");
        addErrorLabelAfter(choix1Field, "Choix 1");
        addErrorLabelAfter(choix2Field, "Choix 2");
        addErrorLabelAfter(choix3Field, "Choix 3");
        addErrorLabelAfter(bonneReponseCombo, "Bonne réponse");
        addErrorLabelAfter(scoreField, "Score");
        addErrorLabelAfter(coursCombo, "Cours");
    }

    /**
     * Ajoute un label d'erreur après un champ spécifique
     */
    private void addErrorLabelAfter(Control field, String fieldName) {
        // Créer le label d'erreur
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 11px; -fx-wrap-text: true;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Trouver le parent du champ
        Node parent = field.getParent();
        if (parent instanceof VBox) {
            VBox vbox = (VBox) parent;
            int index = vbox.getChildren().indexOf(field);
            if (index >= 0 && index + 1 < vbox.getChildren().size()) {
                vbox.getChildren().add(index + 1, errorLabel);
            } else {
                vbox.getChildren().add(errorLabel);
            }
        } else if (parent instanceof HBox) {
            // Si le champ est dans un HBox, remonter au VBox parent
            Node grandParent = parent.getParent();
            if (grandParent instanceof VBox) {
                VBox vbox = (VBox) grandParent;
                int index = vbox.getChildren().indexOf(parent);
                if (index >= 0 && index + 1 < vbox.getChildren().size()) {
                    vbox.getChildren().add(index + 1, errorLabel);
                } else {
                    vbox.getChildren().add(errorLabel);
                }
            }
        }

        // Stocker le label dans la map
        errorLabels.put(field, errorLabel);
    }

    /**
     * Affiche un message d'erreur pour un champ
     */
    private void showError(Control field, String message) {
        Label errorLabel = errorLabels.get(field);
        if (errorLabel != null) {
            errorLabel.setText("❌ " + message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    /**
     * Cache le message d'erreur pour un champ
     */
    private void hideError(Control field) {
        Label errorLabel = errorLabels.get(field);
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /**
     * Configure la validation en temps réel
     */
    private void setupValidation() {
        // Validation de la question
        questionArea.textProperty().addListener((obs, old, newValue) -> {
            validerQuestion();
        });
        questionArea.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerQuestion();
            }
        });

        // Validation du choix 1
        choix1Field.textProperty().addListener((obs, old, newValue) -> {
            validerChoix1();
            updateBonneReponseCombo();
        });
        choix1Field.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerChoix1();
            }
        });

        // Validation du choix 2
        choix2Field.textProperty().addListener((obs, old, newValue) -> {
            validerChoix2();
            updateBonneReponseCombo();
        });
        choix2Field.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerChoix2();
            }
        });

        // Validation du choix 3
        choix3Field.textProperty().addListener((obs, old, newValue) -> {
            validerChoix3();
            updateBonneReponseCombo();
        });
        choix3Field.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerChoix3();
            }
        });

        // Validation de la bonne réponse
        bonneReponseCombo.valueProperty().addListener((obs, old, newValue) -> {
            validerBonneReponse();
        });

        // Validation du score
        scoreField.textProperty().addListener((obs, old, newValue) -> {
            validerScore();
        });
        scoreField.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerScore();
            }
        });

        // Validation du cours
        coursCombo.valueProperty().addListener((obs, old, newValue) -> {
            validerCours();
        });
    }

    /**
     * Valide la question
     */
    private boolean validerQuestion() {
        String question = questionArea.getText();
        if (question == null || question.trim().isEmpty()) {
            questionArea.setStyle(STYLE_INVALIDE);
            showError(questionArea, "La question est obligatoire");
            return false;
        } else if (question.length() < 5) {
            questionArea.setStyle(STYLE_INVALIDE);
            showError(questionArea, "La question doit contenir au moins 5 caractères");
            return false;
        } else if (question.length() > 500) {
            questionArea.setStyle(STYLE_INVALIDE);
            showError(questionArea, "La question ne doit pas dépasser 500 caractères");
            return false;
        } else {
            questionArea.setStyle(STYLE_VALIDE);
            hideError(questionArea);
            return true;
        }
    }

    /**
     * Valide le choix 1
     */
    private boolean validerChoix1() {
        String choix = choix1Field.getText();
        if (choix == null || choix.trim().isEmpty()) {
            choix1Field.setStyle(STYLE_INVALIDE);
            showError(choix1Field, "Le choix 1 est obligatoire");
            return false;
        } else if (choix.length() < 1) {
            choix1Field.setStyle(STYLE_INVALIDE);
            showError(choix1Field, "Le choix doit contenir au moins 1 caractère");
            return false;
        } else if (choix.length() > 200) {
            choix1Field.setStyle(STYLE_INVALIDE);
            showError(choix1Field, "Le choix ne doit pas dépasser 200 caractères");
            return false;
        } else {
            choix1Field.setStyle(STYLE_VALIDE);
            hideError(choix1Field);
            return true;
        }
    }

    /**
     * Valide le choix 2
     */
    private boolean validerChoix2() {
        String choix = choix2Field.getText();
        if (choix == null || choix.trim().isEmpty()) {
            choix2Field.setStyle(STYLE_INVALIDE);
            showError(choix2Field, "Le choix 2 est obligatoire");
            return false;
        } else if (choix.length() < 1) {
            choix2Field.setStyle(STYLE_INVALIDE);
            showError(choix2Field, "Le choix doit contenir au moins 1 caractère");
            return false;
        } else if (choix.length() > 200) {
            choix2Field.setStyle(STYLE_INVALIDE);
            showError(choix2Field, "Le choix ne doit pas dépasser 200 caractères");
            return false;
        } else {
            choix2Field.setStyle(STYLE_VALIDE);
            hideError(choix2Field);
            return true;
        }
    }

    /**
     * Valide le choix 3
     */
    private boolean validerChoix3() {
        String choix = choix3Field.getText();
        if (choix == null || choix.trim().isEmpty()) {
            choix3Field.setStyle(STYLE_INVALIDE);
            showError(choix3Field, "Le choix 3 est obligatoire");
            return false;
        } else if (choix.length() < 1) {
            choix3Field.setStyle(STYLE_INVALIDE);
            showError(choix3Field, "Le choix doit contenir au moins 1 caractère");
            return false;
        } else if (choix.length() > 200) {
            choix3Field.setStyle(STYLE_INVALIDE);
            showError(choix3Field, "Le choix ne doit pas dépasser 200 caractères");
            return false;
        } else {
            choix3Field.setStyle(STYLE_VALIDE);
            hideError(choix3Field);
            return true;
        }
    }

    /**
     * Valide la bonne réponse
     */
    private boolean validerBonneReponse() {
        String bonneReponse = bonneReponseCombo.getValue();
        if (bonneReponse == null || bonneReponse.isEmpty()) {
            bonneReponseCombo.setStyle(STYLE_INVALIDE);
            showError(bonneReponseCombo, "Veuillez sélectionner la bonne réponse");
            return false;
        } else {
            bonneReponseCombo.setStyle(STYLE_VALIDE);
            hideError(bonneReponseCombo);
            return true;
        }
    }

    /**
     * Valide le score
     */
    private boolean validerScore() {
        String scoreText = scoreField.getText();
        if (scoreText == null || scoreText.trim().isEmpty()) {
            scoreField.setStyle(STYLE_INVALIDE);
            showError(scoreField, "Le score est obligatoire");
            return false;
        }

        try {
            int score = Integer.parseInt(scoreText.trim());
            if (score <= 0) {
                scoreField.setStyle(STYLE_INVALIDE);
                showError(scoreField, "Le score doit être supérieur à 0");
                return false;
            } else if (score > 100) {
                scoreField.setStyle(STYLE_INVALIDE);
                showError(scoreField, "Le score ne doit pas dépasser 100 points");
                return false;
            } else {
                scoreField.setStyle(STYLE_VALIDE);
                hideError(scoreField);
                return true;
            }
        } catch (NumberFormatException e) {
            scoreField.setStyle(STYLE_INVALIDE);
            showError(scoreField, "Le score doit être un nombre entier");
            return false;
        }
    }

    /**
     * Valide la sélection du cours
     */
    private boolean validerCours() {
        Cours cours = coursCombo.getValue();
        if (cours == null) {
            coursCombo.setStyle(STYLE_INVALIDE);
            showError(coursCombo, "Veuillez sélectionner un cours");
            return false;
        } else {
            coursCombo.setStyle(STYLE_VALIDE);
            hideError(coursCombo);
            return true;
        }
    }

    /**
     * Valide tous les champs du formulaire
     */
    private boolean validateAllFields() {
        boolean questionValide = validerQuestion();
        boolean choix1Valide = validerChoix1();
        boolean choix2Valide = validerChoix2();
        boolean choix3Valide = validerChoix3();
        boolean bonneReponseValide = validerBonneReponse();
        boolean scoreValide = validerScore();
        boolean coursValide = validerCours();

        return questionValide && choix1Valide && choix2Valide &&
                choix3Valide && bonneReponseValide && scoreValide && coursValide;
    }

    private void initializeComboBoxes() {
        // Configuration du ComboBox de cours pour le formulaire
        coursCombo.setConverter(new StringConverter<Cours>() {
            @Override
            public String toString(Cours cours) {
                return cours == null ? "" : cours.getTitre() + " (" + cours.getNiveau() + ")";
            }

            @Override
            public Cours fromString(String string) {
                return null;
            }
        });
    }

    private void updateBonneReponseCombo() {
        ObservableList<String> choix = FXCollections.observableArrayList();
        if (!choix1Field.getText().trim().isEmpty()) choix.add(choix1Field.getText().trim());
        if (!choix2Field.getText().trim().isEmpty()) choix.add(choix2Field.getText().trim());
        if (!choix3Field.getText().trim().isEmpty()) choix.add(choix3Field.getText().trim());
        bonneReponseCombo.setItems(choix);

        // Si la valeur sélectionnée n'est plus dans la liste, la réinitialiser
        if (bonneReponseCombo.getValue() != null && !choix.contains(bonneReponseCombo.getValue())) {
            bonneReponseCombo.setValue(null);
            validerBonneReponse();
        }
    }

    private void initializeTable() {
        // Configuration des colonnes
        questionColumn.setCellValueFactory(new PropertyValueFactory<>("question"));

        // Configuration spéciale pour la colonne Cours - utilise coursMap pour trouver le cours
        coursColumn.setCellValueFactory(cellData -> {
            Evaluation evaluation = cellData.getValue();
            Cours cours = evaluation.getCours();

            // Si le cours n'est pas directement associé, utiliser la map
            if (cours == null && evaluation.getId_cours() > 0) {
                cours = coursMap.get(evaluation.getId_cours());
                evaluation.setCours(cours);
            }

            // Vérifier si cours n'est pas null avant d'accéder à ses méthodes
            String coursInfo;
            if (cours != null) {
                coursInfo = cours.getTitre() + " (" + cours.getNiveau() + ")";
            } else {
                coursInfo = "Non assigné";
            }

            return new SimpleStringProperty(coursInfo);
        });

        choix1Column.setCellValueFactory(new PropertyValueFactory<>("choix1"));
        choix2Column.setCellValueFactory(new PropertyValueFactory<>("choix2"));
        choix3Column.setCellValueFactory(new PropertyValueFactory<>("choix3"));
        bonneReponseColumn.setCellValueFactory(new PropertyValueFactory<>("bonne_reponse"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        ajouterBoutonsActions();
    }

    private void ajouterBoutonsActions() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifierButton = new Button("✏️");
            private final Button supprimerButton = new Button("🗑️");
            private final HBox pane = new HBox(10, modifierButton, supprimerButton);

            {
                modifierButton.setStyle("-fx-background-color: transparent; -fx-border-color: #FFA500; -fx-text-fill: #FFA500; -fx-border-radius: 15; -fx-padding: 5 10; -fx-cursor: hand;");
                supprimerButton.setStyle("-fx-background-color: transparent; -fx-border-color: #FF4444; -fx-text-fill: #FF4444; -fx-border-radius: 15; -fx-padding: 5 10; -fx-cursor: hand;");
                pane.setAlignment(Pos.CENTER);

                modifierButton.setOnAction(event -> {
                    Evaluation evaluation = getTableView().getItems().get(getIndex());
                    modifierEvaluation(evaluation);
                });

                supprimerButton.setOnAction(event -> {
                    Evaluation evaluation = getTableView().getItems().get(getIndex());
                    supprimerEvaluation(evaluation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadCours() {
        List<Cours> cours = coursServices.getAll();
        coursList = FXCollections.observableArrayList(cours);
        coursCombo.setItems(coursList);

        // Remplir la map des cours pour un accès facile
        coursMap.clear();
        for (Cours c : cours) {
            coursMap.put(c.getId_cours(), c);
        }

        // Configuration du filtre
        ObservableList<String> coursNoms = FXCollections.observableArrayList();
        coursNoms.add("📚 Tous les cours");
        for (Cours c : cours) {
            coursNoms.add(c.getId_cours() + " - " + c.getTitre());
        }
        filterCoursCombo.setItems(coursNoms);

        if (coursFiltreId != null) {
            // Sélectionner le cours dans le filtre
            for (String item : coursNoms) {
                if (item.startsWith(coursFiltreId + " - ")) {
                    filterCoursCombo.setValue(item);
                    break;
                }
            }

            // Sélectionner le cours dans le formulaire
            for (Cours c : cours) {
                if (c.getId_cours() == coursFiltreId) {
                    coursCombo.setValue(c);
                    validerCours();
                    break;
                }
            }
        } else {
            filterCoursCombo.setValue("📚 Tous les cours");
        }
    }

    private void loadQuestions() {
        List<Evaluation> questions;
        if (coursFiltreId != null) {
            questions = evaluationServices.getByCoursId(coursFiltreId);
        } else {
            questions = evaluationServices.getAll();
        }

        // Charger les cours pour chaque question en utilisant la map
        for (Evaluation question : questions) {
            if (question.getCours() == null && question.getId_cours() > 0) {
                Cours cours = coursMap.get(question.getId_cours());
                question.setCours(cours);
            }
        }

        questionsList = FXCollections.observableArrayList(questions);
        questionsTable.setItems(questionsList);
        totalQuestionsLabel.setText(questions.size() + " question(s)");
    }

    private void setupListeners() {
        // Filtre par cours
        filterCoursCombo.setOnAction(e -> {
            String selected = filterCoursCombo.getValue();
            if (selected != null && !selected.equals("📚 Tous les cours")) {
                try {
                    coursFiltreId = Integer.parseInt(selected.split(" - ")[0]);
                } catch (Exception ex) {
                    coursFiltreId = null;
                }
            } else {
                coursFiltreId = null;
            }
            loadQuestions();
        });

        // Bouton ajouter
        ajouterQuestionBtn.setOnAction(e -> ajouterQuestion());

        // Bouton modifier
        modifierQuestionBtn.setOnAction(e -> modifierQuestion());

        // Bouton annuler modification
        annulerModificationBtn.setOnAction(e -> annulerModification());

        // Bouton retour
        retourButton.setOnAction(e -> {
            Navigation.navigateTo("coursaffichage.fxml", "Liste des cours");
        });
    }

    private void ajouterQuestion() {
        if (!validateAllFields()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez corriger les erreurs dans le formulaire (champs en rouge)");
            return;
        }

        try {
            Evaluation question = new Evaluation();
            question.setId_cours(coursCombo.getValue().getId_cours());
            question.setCours(coursCombo.getValue()); // Associer le cours directement
            question.setQuestion(questionArea.getText().trim());
            question.setChoix1(choix1Field.getText().trim());
            question.setChoix2(choix2Field.getText().trim());
            question.setChoix3(choix3Field.getText().trim());
            question.setBonne_reponse(bonneReponseCombo.getValue());
            question.setScore(Integer.parseInt(scoreField.getText().trim()));

            boolean success = evaluationServices.ajouter(question);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Question ajoutée avec succès !");
                clearForm();
                loadQuestions();

                // Mettre à jour le filtre si nécessaire
                if (coursCombo.getValue() != null) {
                    String coursItem = coursCombo.getValue().getId_cours() + " - " + coursCombo.getValue().getTitre();
                    if (!filterCoursCombo.getItems().contains(coursItem)) {
                        filterCoursCombo.getItems().add(coursItem);
                    }
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de la question");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le score doit être un nombre valide");
        }
    }

    private void modifierEvaluation(Evaluation evaluation) {
        questionEnModification = evaluation;

        // Remplir le formulaire
        coursCombo.setValue(evaluation.getCours());
        questionArea.setText(evaluation.getQuestion());
        choix1Field.setText(evaluation.getChoix1());
        choix2Field.setText(evaluation.getChoix2());
        choix3Field.setText(evaluation.getChoix3());
        scoreField.setText(String.valueOf(evaluation.getScore()));

        // Valider tous les champs remplis
        validerQuestion();
        validerChoix1();
        validerChoix2();
        validerChoix3();
        validerScore();
        validerCours();

        // Mettre à jour la combo des bonnes réponses
        updateBonneReponseCombo();
        bonneReponseCombo.setValue(evaluation.getBonne_reponse());
        validerBonneReponse();

        // Changer l'interface
        formTitleLabel.setText("✏️ Modifier la question");
        ajouterQuestionBtn.setVisible(false);
        ajouterQuestionBtn.setManaged(false);
        modifierQuestionBtn.setVisible(true);
        modifierQuestionBtn.setManaged(true);
        annulerModificationBtn.setVisible(true);
        annulerModificationBtn.setManaged(true);
    }

    private void modifierQuestion() {
        if (!validateAllFields()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez corriger les erreurs dans le formulaire (champs en rouge)");
            return;
        }

        try {
            if (questionEnModification == null) return;

            questionEnModification.setId_cours(coursCombo.getValue().getId_cours());
            questionEnModification.setCours(coursCombo.getValue()); // Mettre à jour le cours
            questionEnModification.setQuestion(questionArea.getText().trim());
            questionEnModification.setChoix1(choix1Field.getText().trim());
            questionEnModification.setChoix2(choix2Field.getText().trim());
            questionEnModification.setChoix3(choix3Field.getText().trim());
            questionEnModification.setBonne_reponse(bonneReponseCombo.getValue());
            questionEnModification.setScore(Integer.parseInt(scoreField.getText().trim()));

            boolean success = evaluationServices.modifier(questionEnModification);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Question modifiée avec succès !");
                annulerModification();
                loadQuestions();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le score doit être un nombre valide");
        }
    }

    private void supprimerEvaluation(Evaluation evaluation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la question");
        alert.setContentText("Voulez-vous vraiment supprimer cette question ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = evaluationServices.supprimer(evaluation.getId_eval());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Question supprimée !");
                    loadQuestions();

                    // Si on était en train de modifier cette question, annuler
                    if (questionEnModification != null && questionEnModification.getId_eval() == evaluation.getId_eval()) {
                        annulerModification();
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression");
                }
            }
        });
    }

    private void annulerModification() {
        questionEnModification = null;
        clearForm();
        formTitleLabel.setText("➕ Ajouter une nouvelle question");
        ajouterQuestionBtn.setVisible(true);
        ajouterQuestionBtn.setManaged(true);
        modifierQuestionBtn.setVisible(false);
        modifierQuestionBtn.setManaged(false);
        annulerModificationBtn.setVisible(false);
        annulerModificationBtn.setManaged(false);
    }

    private void clearForm() {
        questionArea.clear();
        questionArea.setStyle(STYLE_INVALIDE);
        hideError(questionArea);

        choix1Field.clear();
        choix1Field.setStyle(STYLE_INVALIDE);
        hideError(choix1Field);

        choix2Field.clear();
        choix2Field.setStyle(STYLE_INVALIDE);
        hideError(choix2Field);

        choix3Field.clear();
        choix3Field.setStyle(STYLE_INVALIDE);
        hideError(choix3Field);

        bonneReponseCombo.setValue(null);
        bonneReponseCombo.setStyle(STYLE_INVALIDE);
        hideError(bonneReponseCombo);

        scoreField.clear();
        scoreField.setStyle(STYLE_INVALIDE);
        hideError(scoreField);

        if (coursFiltreId != null && coursList != null) {
            for (Cours c : coursList) {
                if (c.getId_cours() == coursFiltreId) {
                    coursCombo.setValue(c);
                    validerCours();
                    break;
                }
            }
        } else {
            coursCombo.setValue(null);
            coursCombo.setStyle(STYLE_INVALIDE);
            hideError(coursCombo);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}