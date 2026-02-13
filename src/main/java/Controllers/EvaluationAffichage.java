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
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map;

public class EvaluationAffichage implements Initializable {

    @FXML
    private TableView<Evaluation> evaluationTable;

    @FXML
    private TableColumn<Evaluation, Integer> idColumn;

    @FXML
    private TableColumn<Evaluation, String> typeColumn;

    @FXML
    private TableColumn<Evaluation, Float> scoreColumn;

    @FXML
    private TableColumn<Evaluation, String> niveauColumn;

    @FXML
    private TableColumn<Evaluation, LocalDate> dateColumn;

    @FXML
    private TableColumn<Evaluation, String> coursColumn;

    @FXML
    private TableColumn<Evaluation, Void> actionsColumn;

    @FXML
    private ComboBox<String> filterCoursCombo;

    @FXML
    private Label totalEvaluationsLabel;

    @FXML
    private Button ajouterEvaluationButton;

    @FXML
    private Label coursTitreLabel;

    @FXML
    private Button retourButton;

    private EvaluationServices evaluationServices;
    private CoursServices coursServices;
    private ObservableList<Evaluation> evaluationList;
    private Integer coursFiltreId = null; // ID du cours à filtrer (null = tous)

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        evaluationServices = new EvaluationServices();
        coursServices = new CoursServices();

        // Récupérer les paramètres de l'URL (si présents)
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
            }
        }
    }

    private void initializeTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_evaluation"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type_evaluation"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        niveauColumn.setCellValueFactory(new PropertyValueFactory<>("niveau_comprehension"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_evaluation"));

        // Colonne spéciale pour afficher le titre du cours
        coursColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCours().getTitre()
                )
        );

        ajouterBoutonsActions();
    }

    private void ajouterBoutonsActions() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifierButton = new Button("Modifier");
            private final Button supprimerButton = new Button("Supprimer");
            private final Button voirButton = new Button("Voir cours");
            private final HBox pane = new HBox(10, voirButton, modifierButton, supprimerButton);

            {
                voirButton.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10;");
                modifierButton.setStyle("-fx-background-color: transparent; -fx-border-color: #FFA500; -fx-text-fill: #FFA500; -fx-border-radius: 15; -fx-padding: 5 10;");
                supprimerButton.setStyle("-fx-background-color: transparent; -fx-border-color: #FF4444; -fx-text-fill: #FF4444; -fx-border-radius: 15; -fx-padding: 5 10;");
                pane.setAlignment(Pos.CENTER);

                voirButton.setOnAction(event -> {
                    Evaluation evaluation = getTableView().getItems().get(getIndex());
                    voirCoursAssocie(evaluation);
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
        coursNoms.add("Tous les cours");
        for (Cours c : coursList) {
            coursNoms.add(c.getId_cours() + " - " + c.getTitre());
        }

        if (filterCoursCombo != null) {
            filterCoursCombo.setItems(coursNoms);
            filterCoursCombo.setValue("Tous les cours");

            filterCoursCombo.setOnAction(e -> {
                String selected = filterCoursCombo.getValue();
                if (selected != null && !selected.equals("Tous les cours")) {
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
        totalEvaluationsLabel.setText("Total: " + evaluationList.size() + " évaluations");
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
        totalEvaluationsLabel.setText("Total: " + evaluationList.size() + " évaluations");
    }

    private void setupNavigation() {
        if (ajouterEvaluationButton != null) {
            ajouterEvaluationButton.setOnAction(event -> {
                if (coursFiltreId != null) {
                    Navigation.navigateTo("evaluationajout.fxml?coursId=" + coursFiltreId,
                            "Ajouter une évaluation");
                } else {
                    Navigation.navigateTo("evaluationajout.fxml", "Ajouter une évaluation");
                }
            });
        }

        if (retourButton != null) {
            retourButton.setOnAction(event -> {
                Navigation.goBack();
            });
        }
    }

    private void voirCoursAssocie(Evaluation evaluation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du cours");
        alert.setHeaderText("Cours : " + evaluation.getCours().getTitre());
        alert.setContentText(
                "Description: " + evaluation.getCours().getDescription() + "\n" +
                        "Type: " + evaluation.getCours().getType_cours() + "\n" +
                        "Niveau: " + evaluation.getCours().getNiveau() + "\n" +
                        "Durée: " + evaluation.getCours().getDuree() + " minutes\n" +
                        "Mots: " + evaluation.getCours().getMots()
        );
        alert.showAndWait();
    }

    private void modifierEvaluation(Evaluation evaluation) {
        // TODO: Implémenter la modification
        System.out.println("Modifier évaluation ID: " + evaluation.getId_evaluation());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Modification");
        alert.setContentText("Fonctionnalité de modification en cours de développement");
        alert.showAndWait();
    }

    private void supprimerEvaluation(Evaluation evaluation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'évaluation");
        alert.setContentText("Voulez-vous vraiment supprimer cette évaluation ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = evaluationServices.supprimer(evaluation.getId_evaluation());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation supprimée !");
                    loadEvaluations();
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