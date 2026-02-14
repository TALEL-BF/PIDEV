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

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class PasserEvaluation implements Initializable {

    @FXML private Label titreCoursLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Label scoreLabel;
    @FXML private Label questionNumeroLabel;
    @FXML private Label questionScoreLabel;
    @FXML private Label questionTextLabel;
    @FXML private RadioButton choix1Radio;
    @FXML private RadioButton choix2Radio;
    @FXML private RadioButton choix3Radio;
    @FXML private Button precedentButton;
    @FXML private Button suivantButton;
    @FXML private Button terminerButton;
    @FXML private Button retourButton;
    @FXML private Label feedbackLabel;
    @FXML private VBox questionCard;
    @FXML private ToggleGroup reponsesGroup;

    private EvaluationServices evaluationServices;
    private CoursServices coursServices;
    private List<Evaluation> questions;
    private int currentQuestionIndex = 0;
    private int scoreTotal = 0;
    private String[] reponsesUtilisateur;
    private boolean[] reponsesCorrectes;
    private Cours cours;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        evaluationServices = new EvaluationServices();
        coursServices = new CoursServices();

        // Récupérer l'ID du cours depuis les paramètres
        Map<String, String> params = Navigation.getParameters();
        if (params.containsKey("coursId")) {
            try {
                int coursId = Integer.parseInt(params.get("coursId"));
                chargerEvaluation(coursId);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "ID de cours invalide");
                Navigation.goBack();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun cours sélectionné");
            Navigation.goBack();
        }

        setupListeners();
    }

    private void chargerEvaluation(int coursId) {
        cours = coursServices.getById(coursId);
        questions = evaluationServices.getByCoursId(coursId);

        if (cours != null) {
            titreCoursLabel.setText(cours.getTitre());
        }

        if (questions.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Aucune question disponible pour ce cours pour le moment.");
            Navigation.goBack();
            return;
        }

        // Initialiser les tableaux de suivi
        reponsesUtilisateur = new String[questions.size()];
        reponsesCorrectes = new boolean[questions.size()];

        // Afficher la première question
        currentQuestionIndex = 0;
        afficherQuestion(currentQuestionIndex);
        mettreAJourProgression();
    }

    private void setupListeners() {
        precedentButton.setOnAction(e -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                afficherQuestion(currentQuestionIndex);
            }
        });

        suivantButton.setOnAction(e -> {
            if (!validerReponse()) {
                return;
            }

            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                afficherQuestion(currentQuestionIndex);
            } else {
                // Dernière question, afficher le bouton terminer
                terminerButton.setVisible(true);
                terminerButton.setManaged(true);
                suivantButton.setVisible(false);
                suivantButton.setManaged(false);
            }
        });

        terminerButton.setOnAction(e -> {
            if (!validerReponse()) {
                return;
            }
            afficherResultats();
        });

        retourButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Quitter le quiz");
            alert.setContentText("Voulez-vous vraiment quitter ? Votre progression sera perdue.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Navigation.navigateTo("coursaffichage.fxml", "Liste des cours");
                }
            });
        });

        // Réinitialiser le feedback quand on change de réponse
        reponsesGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            feedbackLabel.setText("");
            feedbackLabel.setStyle("-fx-background-color: transparent;");
        });
    }

    private void afficherQuestion(int index) {
        Evaluation question = questions.get(index);

        questionNumeroLabel.setText("Question " + (index + 1) + "/" + questions.size());
        questionTextLabel.setText(question.getQuestion());
        questionScoreLabel.setText(question.getScore() + " point" + (question.getScore() > 1 ? "s" : ""));

        choix1Radio.setText(question.getChoix1());
        choix2Radio.setText(question.getChoix2());
        choix3Radio.setText(question.getChoix3());

        // Restaurer la réponse précédente si elle existe
        if (reponsesUtilisateur[index] != null) {
            String reponse = reponsesUtilisateur[index];
            if (reponse.equals(question.getChoix1())) {
                choix1Radio.setSelected(true);
            } else if (reponse.equals(question.getChoix2())) {
                choix2Radio.setSelected(true);
            } else if (reponse.equals(question.getChoix3())) {
                choix3Radio.setSelected(true);
            }
        } else {
            reponsesGroup.selectToggle(null);
        }

        // Mettre à jour les boutons de navigation
        precedentButton.setDisable(index == 0);

        if (index == questions.size() - 1) {
            suivantButton.setVisible(false);
            suivantButton.setManaged(false);
            terminerButton.setVisible(true);
            terminerButton.setManaged(true);
        } else {
            suivantButton.setVisible(true);
            suivantButton.setManaged(true);
            terminerButton.setVisible(false);
            terminerButton.setManaged(false);
        }

        // Réinitialiser le feedback
        feedbackLabel.setText("");
        feedbackLabel.setStyle("-fx-background-color: transparent;");

        mettreAJourProgression();
    }

    private boolean validerReponse() {
        RadioButton selected = (RadioButton) reponsesGroup.getSelectedToggle();

        if (selected == null) {
            showFeedback("❌ Veuillez sélectionner une réponse", "#FF4444");
            return false;
        }

        String reponse = selected.getText();
        Evaluation question = questions.get(currentQuestionIndex);

        // Sauvegarder la réponse
        reponsesUtilisateur[currentQuestionIndex] = reponse;

        // Vérifier si la réponse est correcte
        boolean correct = reponse.equals(question.getBonne_reponse());
        reponsesCorrectes[currentQuestionIndex] = correct;

        // Mettre à jour le score
        mettreAJourScore();

        // Afficher un feedback
        if (correct) {
            showFeedback("✅ Bonne réponse ! (+" + question.getScore() + " point" + (question.getScore() > 1 ? "s" : "") + ")", "#28A745");
        } else {
            showFeedback("❌ Mauvaise réponse. La bonne réponse était : " + question.getBonne_reponse(), "#FF4444");
        }

        return true;
    }

    private void mettreAJourScore() {
        scoreTotal = 0;
        for (int i = 0; i < reponsesCorrectes.length; i++) {
            if (reponsesCorrectes[i]) {
                scoreTotal += questions.get(i).getScore();
            }
        }
        scoreLabel.setText("Score: " + scoreTotal);
    }

    private void mettreAJourProgression() {
        double progress = (double) (currentQuestionIndex + 1) / questions.size();
        progressBar.setProgress(progress);
        progressLabel.setText((currentQuestionIndex + 1) + "/" + questions.size());
    }

    private void showFeedback(String message, String couleur) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-background-color: " + couleur + "20; -fx-text-fill: " + couleur + "; -fx-padding: 15; -fx-background-radius: 15;");
    }

    private void afficherResultats() {
        // Calculer le score final
        int scoreMax = questions.stream().mapToInt(Evaluation::getScore).sum();
        double pourcentage = (scoreTotal * 100.0) / scoreMax;

        String appreciation;
        String couleur;
        String emoji;

        if (pourcentage >= 90) {
            appreciation = "Excellent !";
            couleur = "#28A745";
            emoji = "🏆";
        } else if (pourcentage >= 75) {
            appreciation = "Très bien !";
            couleur = "#7B2FF7";
            emoji = "🎉";
        } else if (pourcentage >= 60) {
            appreciation = "Bien !";
            couleur = "#FFA500";
            emoji = "👍";
        } else if (pourcentage >= 40) {
            appreciation = "Passable";
            couleur = "#FFA500";
            emoji = "😊";
        } else {
            appreciation = "Peut mieux faire";
            couleur = "#FF4444";
            emoji = "📝";
        }

        int correctCount = 0;
        for (boolean b : reponsesCorrectes) {
            if (b) correctCount++;
        }
        int incorrectCount = questions.size() - correctCount;

        // Créer une boîte de dialogue personnalisée
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Résultats du quiz");

        // Style du header
        dialog.getDialogPane().setHeaderText(null);
        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Quiz terminé " + emoji);
        headerLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");

        Label coursLabel = new Label(cours.getTitre());
        coursLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");

        headerBox.getChildren().addAll(headerLabel, coursLabel);

        // Contenu des résultats
        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 15;");
        content.setAlignment(Pos.CENTER);

        // Score avec cercle de progression
        StackPane scoreCircle = new StackPane();
        scoreCircle.setPrefSize(150, 150);
        scoreCircle.setStyle("-fx-background-color: " + couleur + "20; -fx-background-radius: 75; -fx-border-color: " + couleur + "; -fx-border-width: 3; -fx-border-radius: 75;");

        VBox scoreBox = new VBox(5);
        scoreBox.setAlignment(Pos.CENTER);

        Label scoreValue = new Label(scoreTotal + "/" + scoreMax);
        scoreValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");

        Label pourcentageLabel = new Label(String.format("%.1f%%", pourcentage));
        pourcentageLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #666666;");

        scoreBox.getChildren().addAll(scoreValue, pourcentageLabel);
        scoreCircle.getChildren().add(scoreBox);

        // Statistiques en grille
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(30);
        statsGrid.setVgap(15);
        statsGrid.setAlignment(Pos.CENTER);

        // Ligne 1: Questions totales
        Label totalIcon = new Label("📊");
        totalIcon.setStyle("-fx-font-size: 24px;");
        Label totalLabel = new Label(questions.size() + " question" + (questions.size() > 1 ? "s" : ""));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #7B2FF7;");
        statsGrid.add(totalIcon, 0, 0);
        statsGrid.add(totalLabel, 1, 0);

        // Ligne 2: Réponses correctes
        Label correctIcon = new Label("✅");
        correctIcon.setStyle("-fx-font-size: 24px;");
        Label correctLabel = new Label(correctCount + " correcte" + (correctCount > 1 ? "s" : ""));
        correctLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #28A745;");
        statsGrid.add(correctIcon, 0, 1);
        statsGrid.add(correctLabel, 1, 1);

        // Ligne 3: Réponses incorrectes
        if (incorrectCount > 0) {
            Label incorrectIcon = new Label("❌");
            incorrectIcon.setStyle("-fx-font-size: 24px;");
            Label incorrectLabel = new Label(incorrectCount + " incorrecte" + (incorrectCount > 1 ? "s" : ""));
            incorrectLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FF4444;");
            statsGrid.add(incorrectIcon, 0, 2);
            statsGrid.add(incorrectLabel, 1, 2);
        }

        // Appréciation
        Label appreciationLabel = new Label("⭐ " + appreciation);
        appreciationLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + couleur + "; -fx-padding: 10 0 0 0;");

        // Boutons
        ButtonType detailsBtn = new ButtonType("📋 Voir détails", ButtonBar.ButtonData.LEFT);
        ButtonType recommencerBtn = new ButtonType("🔄 Recommencer", ButtonBar.ButtonData.OTHER);
        ButtonType fermerBtn = new ButtonType("✅ Terminer", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(detailsBtn, recommencerBtn, fermerBtn);

        // Assembler le contenu
        content.getChildren().addAll(scoreCircle, statsGrid, appreciationLabel);

        VBox mainContent = new VBox(15);
        mainContent.getChildren().addAll(headerBox, content);

        dialog.getDialogPane().setContent(mainContent);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setStyle("-fx-background-color: #F0F8FF; -fx-background-radius: 20; -fx-padding: 20;");

        // Gérer les actions
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (result.get() == recommencerBtn) {
                // Recommencer le quiz
                currentQuestionIndex = 0;
                scoreTotal = 0;
                reponsesUtilisateur = new String[questions.size()];
                reponsesCorrectes = new boolean[questions.size()];
                scoreLabel.setText("Score: 0");
                afficherQuestion(0);
                mettreAJourProgression();

                // Réafficher les boutons de navigation
                suivantButton.setVisible(true);
                suivantButton.setManaged(true);
                terminerButton.setVisible(false);
                terminerButton.setManaged(false);

            } else if (result.get() == detailsBtn) {
                // Voir les détails des réponses
                afficherDetailsReponses();
            } else {
                // Fermer et retourner à la liste des cours
                Navigation.navigateTo("coursaffichage.fxml", "Liste des cours");
            }
        }
    }

    private void afficherDetailsReponses() {
        // Créer une boîte de dialogue pour afficher le détail des réponses
        Dialog<Void> detailsDialog = new Dialog<>();
        detailsDialog.setTitle("Détail des réponses");
        detailsDialog.setHeaderText("Vos réponses en détail");

        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 10;");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-width: 0;");

        int scoreDetail = 0;

        for (int i = 0; i < questions.size(); i++) {
            Evaluation q = questions.get(i);
            boolean correct = reponsesCorrectes[i];
            String userReponse = reponsesUtilisateur[i] != null ? reponsesUtilisateur[i] : "Non répondue";

            if (correct) {
                scoreDetail += q.getScore();
            }

            VBox questionBox = new VBox(8);
            questionBox.setStyle("-fx-background-color: " + (correct ? "#28A74515" : "#FF444415") +
                    "; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: " +
                    (correct ? "#28A745" : "#FF4444") + "; -fx-border-width: 1; -fx-border-radius: 10;");

            // En-tête de la question
            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            Label numeroLabel = new Label("Question " + (i + 1));
            numeroLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #7B2FF7;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label pointsLabel = new Label(q.getScore() + " pt" + (q.getScore() > 1 ? "s" : ""));
            pointsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (correct ? "#28A745" : "#FF4444") + ";");

            headerBox.getChildren().addAll(numeroLabel, spacer, pointsLabel);

            // Question
            Label questionLabel = new Label(q.getQuestion());
            questionLabel.setWrapText(true);
            questionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            // Réponse de l'utilisateur
            HBox reponseBox = new HBox(10);
            reponseBox.setAlignment(Pos.CENTER_LEFT);

            Label reponseIcon = new Label(correct ? "✅" : "❌");
            reponseIcon.setStyle("-fx-font-size: 16px;");

            Label reponseLabel = new Label("Votre réponse: " + userReponse);
            reponseLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (correct ? "#28A745" : "#FF4444") + ";");
            reponseLabel.setWrapText(true);

            reponseBox.getChildren().addAll(reponseIcon, reponseLabel);

            questionBox.getChildren().addAll(headerBox, questionLabel, reponseBox);

            // Si la réponse est incorrecte, afficher la bonne réponse
            if (!correct && reponsesUtilisateur[i] != null) {
                HBox bonneReponseBox = new HBox(10);
                bonneReponseBox.setAlignment(Pos.CENTER_LEFT);

                Label bonneIcon = new Label("✓");
                bonneIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: #28A745; -fx-font-weight: bold;");

                Label bonneReponseLabel = new Label("Bonne réponse: " + q.getBonne_reponse());
                bonneReponseLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28A745;");

                bonneReponseBox.getChildren().addAll(bonneIcon, bonneReponseLabel);
                questionBox.getChildren().add(bonneReponseBox);
            }

            content.getChildren().add(questionBox);
        }

        // Résumé en bas
        HBox summaryBox = new HBox(20);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 10;");

        Label totalPoints = new Label("Score final: " + scoreTotal + "/" + questions.stream().mapToInt(Evaluation::getScore).sum());
        totalPoints.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #7B2FF7;");

        summaryBox.getChildren().add(totalPoints);

        VBox mainContent = new VBox(15);
        mainContent.getChildren().addAll(scrollPane, summaryBox);

        ButtonType fermerBtn = new ButtonType("Fermer", ButtonBar.ButtonData.OK_DONE);
        detailsDialog.getDialogPane().getButtonTypes().add(fermerBtn);
        detailsDialog.getDialogPane().setContent(mainContent);
        detailsDialog.getDialogPane().setPrefSize(600, 600);
        detailsDialog.getDialogPane().setStyle("-fx-background-color: #F0F8FF; -fx-background-radius: 20; -fx-padding: 20;");
        detailsDialog.showAndWait();
    }

    // Méthodes pour les effets de survol
    @FXML
    private void survolPrecedent() {
        precedentButton.setStyle("-fx-background-color: #F0E6FF; -fx-border-color: #7B2FF7; -fx-text-fill: #7B2FF7; -fx-border-radius: 25; -fx-padding: 12 30; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    @FXML
    private void finSurvolPrecedent() {
        precedentButton.setStyle("-fx-background-color: transparent; -fx-border-color: #7B2FF7; -fx-text-fill: #7B2FF7; -fx-border-radius: 25; -fx-padding: 12 30; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    @FXML
    private void survolSuivant() {
        suivantButton.setStyle("-fx-background-color: #6A1FF7; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12 30; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    @FXML
    private void finSurvolSuivant() {
        suivantButton.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12 30; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    @FXML
    private void survolTerminer() {
        terminerButton.setStyle("-fx-background-color: #218838; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12 30; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    @FXML
    private void finSurvolTerminer() {
        terminerButton.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12 30; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    @FXML
    private void survolChoix1() {
        if (!choix1Radio.isSelected()) {
            choix1Radio.setStyle("-fx-font-size: 18px; -fx-padding: 15; -fx-background-color: #F0E6FF; -fx-background-radius: 15; -fx-maxWidth: Infinity; -fx-cursor: hand;");
        }
    }

    @FXML
    private void finSurvolChoix1() {
        if (!choix1Radio.isSelected()) {
            choix1Radio.setStyle("-fx-font-size: 18px; -fx-padding: 15; -fx-background-color: #F5F5F5; -fx-background-radius: 15; -fx-maxWidth: Infinity; -fx-cursor: hand;");
        }
    }

    @FXML
    private void survolChoix2() {
        if (!choix2Radio.isSelected()) {
            choix2Radio.setStyle("-fx-font-size: 18px; -fx-padding: 15; -fx-background-color: #F0E6FF; -fx-background-radius: 15; -fx-maxWidth: Infinity; -fx-cursor: hand;");
        }
    }

    @FXML
    private void finSurvolChoix2() {
        if (!choix2Radio.isSelected()) {
            choix2Radio.setStyle("-fx-font-size: 18px; -fx-padding: 15; -fx-background-color: #F5F5F5; -fx-background-radius: 15; -fx-maxWidth: Infinity; -fx-cursor: hand;");
        }
    }

    @FXML
    private void survolChoix3() {
        if (!choix3Radio.isSelected()) {
            choix3Radio.setStyle("-fx-font-size: 18px; -fx-padding: 15; -fx-background-color: #F0E6FF; -fx-background-radius: 15; -fx-maxWidth: Infinity; -fx-cursor: hand;");
        }
    }

    @FXML
    private void finSurvolChoix3() {
        if (!choix3Radio.isSelected()) {
            choix3Radio.setStyle("-fx-font-size: 18px; -fx-padding: 15; -fx-background-color: #F5F5F5; -fx-background-radius: 15; -fx-maxWidth: Infinity; -fx-cursor: hand;");
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