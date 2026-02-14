package Controllers;

import Entites.Cours;
import Entites.Evaluation;
import Services.CoursServices;
import Services.EvaluationServices;
import Utils.Navigation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.util.StringConverter;

import java.net.URL;
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
    @FXML private TableColumn<Evaluation, Integer> idColumn;
    @FXML private TableColumn<Evaluation, String> questionColumn;
    @FXML private TableColumn<Evaluation, String> choix1Column;
    @FXML private TableColumn<Evaluation, String> choix2Column;
    @FXML private TableColumn<Evaluation, String> choix3Column;
    @FXML private TableColumn<Evaluation, String> bonneReponseColumn;
    @FXML private TableColumn<Evaluation, Integer> scoreColumn;
    @FXML private TableColumn<Evaluation, Void> actionsColumn;

    private EvaluationServices evaluationServices;
    private CoursServices coursServices;
    private ObservableList<Evaluation> questionsList;
    private ObservableList<Cours> coursList;
    private Integer coursFiltreId = null;
    private Evaluation questionEnModification = null;

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
        setupListeners();
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

        // Mettre à jour la bonne réponse quand les choix changent
        choix1Field.textProperty().addListener((obs, old, newVal) -> updateBonneReponseCombo());
        choix2Field.textProperty().addListener((obs, old, newVal) -> updateBonneReponseCombo());
        choix3Field.textProperty().addListener((obs, old, newVal) -> updateBonneReponseCombo());
    }

    private void updateBonneReponseCombo() {
        ObservableList<String> choix = FXCollections.observableArrayList();
        if (!choix1Field.getText().trim().isEmpty()) choix.add(choix1Field.getText().trim());
        if (!choix2Field.getText().trim().isEmpty()) choix.add(choix2Field.getText().trim());
        if (!choix3Field.getText().trim().isEmpty()) choix.add(choix3Field.getText().trim());
        bonneReponseCombo.setItems(choix);
    }

    private void initializeTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_eval"));
        questionColumn.setCellValueFactory(new PropertyValueFactory<>("question"));
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
        if (!validateQuestionFields()) return;

        try {
            if (coursCombo.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un cours");
                return;
            }

            Evaluation question = new Evaluation();
            question.setId_cours(coursCombo.getValue().getId_cours());
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

        // Mettre à jour la combo des bonnes réponses
        updateBonneReponseCombo();
        bonneReponseCombo.setValue(evaluation.getBonne_reponse());

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
        if (!validateQuestionFields()) return;

        try {
            if (questionEnModification == null) return;

            questionEnModification.setId_cours(coursCombo.getValue().getId_cours());
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
        choix1Field.clear();
        choix2Field.clear();
        choix3Field.clear();
        bonneReponseCombo.setValue(null);
        scoreField.clear();

        if (coursFiltreId != null && coursList != null) {
            for (Cours c : coursList) {
                if (c.getId_cours() == coursFiltreId) {
                    coursCombo.setValue(c);
                    break;
                }
            }
        } else {
            coursCombo.setValue(null);
        }
    }

    private boolean validateQuestionFields() {
        if (questionArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir une question");
            return false;
        }
        if (choix1Field.getText().trim().isEmpty() ||
                choix2Field.getText().trim().isEmpty() ||
                choix3Field.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir les 3 choix");
            return false;
        }
        if (bonneReponseCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner la bonne réponse");
            return false;
        }
        if (scoreField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir un score");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}