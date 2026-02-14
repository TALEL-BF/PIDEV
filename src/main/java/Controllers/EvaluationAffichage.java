package Controllers;

import Entites.Cours;
import Entites.Evaluation;
import Services.CoursServices;
import Services.EvaluationServices;
import Utils.Navigation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map;

public class EvaluationAffichage implements Initializable {

    @FXML private TableView<Evaluation> evaluationTable;
    @FXML private TableColumn<Evaluation, Integer> idColumn;
    @FXML private TableColumn<Evaluation, String> questionColumn;
    @FXML private TableColumn<Evaluation, String> choix1Column;
    @FXML private TableColumn<Evaluation, String> choix2Column;
    @FXML private TableColumn<Evaluation, String> choix3Column;
    @FXML private TableColumn<Evaluation, String> bonneReponseColumn;
    @FXML private TableColumn<Evaluation, Integer> scoreColumn;
    @FXML private TableColumn<Evaluation, String> coursColumn;
    @FXML private TableColumn<Evaluation, Void> actionsColumn;
    @FXML private ComboBox<String> filterCoursCombo;
    @FXML private Label totalEvaluationsLabel;
    @FXML private Label totalScoreLabel;
    @FXML private Button ajouterEvaluationButton;
    @FXML private Label coursTitreLabel;
    @FXML private Button retourButton;
    @FXML private Label statsLabel;

    private EvaluationServices evaluationServices;
    private CoursServices coursServices;
    private ObservableList<Evaluation> evaluationList;
    private Integer coursFiltreId = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        evaluationServices = new EvaluationServices();
        coursServices = new CoursServices();

        // Récupérer les paramètres
        Map<String, String> params = Navigation.getParameters();
        if (params.containsKey("coursId")) {
            try {
                coursFiltreId = Integer.parseInt(params.get("coursId"));
            } catch (NumberFormatException e) {
                coursFiltreId = null;
            }
        }

        initializeTable();
        loadCoursCombo();
        loadEvaluations();
        setupNavigation();

        // Afficher le titre du cours si filtré
        if (coursFiltreId != null) {
            Cours cours = coursServices.getById(coursFiltreId);
            if (cours != null && coursTitreLabel != null) {
                coursTitreLabel.setText("Évaluations pour : " + cours.getTitre());
                coursTitreLabel.setVisible(true);
                updateStats(coursFiltreId);
            }
        }
    }

    private void initializeTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_eval"));
        questionColumn.setCellValueFactory(new PropertyValueFactory<>("question"));
        choix1Column.setCellValueFactory(new PropertyValueFactory<>("choix1"));
        choix2Column.setCellValueFactory(new PropertyValueFactory<>("choix2"));
        choix3Column.setCellValueFactory(new PropertyValueFactory<>("choix3"));
        bonneReponseColumn.setCellValueFactory(new PropertyValueFactory<>("bonne_reponse"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        // Colonne pour le cours
        coursColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCours().getTitre()
                )
        );

        ajouterBoutonsActions();
    }

    private void ajouterBoutonsActions() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifierButton = new Button("✏️");
            private final Button supprimerButton = new Button("🗑️");
            private final Button voirButton = new Button("👁️");
            private final HBox pane = new HBox(10, voirButton, modifierButton, supprimerButton);

            {
                voirButton.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10;");
                modifierButton.setStyle("-fx-background-color: transparent; -fx-border-color: #FFA500; -fx-text-fill: #FFA500; -fx-border-radius: 15; -fx-padding: 5 10;");
                supprimerButton.setStyle("-fx-background-color: transparent; -fx-border-color: #FF4444; -fx-text-fill: #FF4444; -fx-border-radius: 15; -fx-padding: 5 10;");
                pane.setAlignment(Pos.CENTER);

                voirButton.setOnAction(event -> {
                    Evaluation evaluation = getTableView().getItems().get(getIndex());
                    voirDetails(evaluation);
                });

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

    private void loadCoursCombo() {
        List<Cours> coursList = coursServices.getAll();
        ObservableList<String> coursNoms = FXCollections.observableArrayList();
        coursNoms.add("📚 Tous les cours");
        for (Cours c : coursList) {
            coursNoms.add(c.getId_cours() + " - " + c.getTitre());
        }

        if (filterCoursCombo != null) {
            filterCoursCombo.setItems(coursNoms);
            filterCoursCombo.setValue("📚 Tous les cours");

            filterCoursCombo.setOnAction(e -> {
                String selected = filterCoursCombo.getValue();
                if (selected != null && !selected.equals("📚 Tous les cours")) {
                    try {
                        int id = Integer.parseInt(selected.split(" - ")[0]);
                        filtrerParCours(id);
                    } catch (Exception ex) {
                        loadEvaluations();
                    }
                } else {
                    loadEvaluations();
                }
            });
        }
    }

    private void filtrerParCours(int coursId) {
        List<Evaluation> filtered = evaluationServices.getByCoursId(coursId);
        evaluationList = FXCollections.observableArrayList(filtered);
        evaluationTable.setItems(evaluationList);
        updateDisplayStats(filtered, coursId);
    }

    private void loadEvaluations() {
        List<Evaluation> evaluations;
        if (coursFiltreId != null) {
            evaluations = evaluationServices.getByCoursId(coursFiltreId);
        } else {
            evaluations = evaluationServices.getAll();
        }
        evaluationList = FXCollections.observableArrayList(evaluations);
        evaluationTable.setItems(evaluationList);
        updateDisplayStats(evaluations, coursFiltreId);
    }

    private void updateDisplayStats(List<Evaluation> evaluations, Integer coursId) {
        totalEvaluationsLabel.setText("📊 Total: " + evaluations.size() + " question(s)");

        if (coursId != null) {
            int totalScore = evaluationServices.getScoreTotalParCours(coursId);
            totalScoreLabel.setText("⭐ Score total: " + totalScore + " points");
            totalScoreLabel.setVisible(true);
        } else {
            totalScoreLabel.setVisible(false);
        }
    }

    private void updateStats(int coursId) {
        int count = evaluationServices.compterParCours(coursId);
        int totalScore = evaluationServices.getScoreTotalParCours(coursId);
        if (statsLabel != null) {
            statsLabel.setText(count + " questions | " + totalScore + " points");
        }
    }

    private void setupNavigation() {
        if (ajouterEvaluationButton != null) {
            ajouterEvaluationButton.setOnAction(event -> {
                if (coursFiltreId != null) {
                    Navigation.navigateTo("evaluationajout.fxml?coursId=" + coursFiltreId,
                            "Ajouter des questions");
                } else {
                    Navigation.navigateTo("evaluationajout.fxml", "Ajouter des questions");
                }
            });
        }

        if (retourButton != null) {
            retourButton.setOnAction(event -> {
                Navigation.goBack();
            });
        }
    }

    private void voirDetails(Evaluation evaluation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la question");
        alert.setHeaderText("Question #" + evaluation.getId_eval());

        String content = String.format(
                "📝 Question: %s\n\n" +
                        "🔹 Choix 1: %s\n" +
                        "🔸 Choix 2: %s\n" +
                        "🔹 Choix 3: %s\n\n" +
                        "✅ Bonne réponse: %s\n" +
                        "⭐ Points: %d\n\n" +
                        "📚 Cours: %s",
                evaluation.getQuestion(),
                evaluation.getChoix1(),
                evaluation.getChoix2(),
                evaluation.getChoix3(),
                evaluation.getBonne_reponse(),
                evaluation.getScore(),
                evaluation.getCours().getTitre()
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    private void modifierEvaluation(Evaluation evaluation) {
        // TODO: Implémenter la modification
        showAlert(Alert.AlertType.INFORMATION, "Information",
                "Fonctionnalité de modification en cours de développement");
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
                    loadEvaluations();
                    if (coursFiltreId != null) {
                        updateStats(coursFiltreId);
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression");
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}