package Controllers;

import Entites.Todo;
import Utils.Navigation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TodoListController implements Initializable {

    @FXML
    private Button backButton;
    @FXML
    private VBox todoListContainer;
    @FXML
    private TextField newTodoField;
    @FXML
    private ComboBox<String> statusComboBox; // NOUVEAU : pour choisir le statut à l'ajout
    @FXML
    private Button addTodoButton;
    @FXML
    private Label todoCountLabel;
    @FXML
    private Label inProgressCountLabel;
    @FXML
    private Label doneCountLabel;
    @FXML
    private Label totalCountLabel;
    @FXML
    private ComboBox<String> filterComboBox;
    @FXML
    private Button clearCompletedButton;

    private ObservableList<Todo> todos;
    private int nextId = 1;

    // Fichier de sauvegarde
    private static final String SAVE_FILE = "todolist.dat";

    private static final String STYLE_TODO_ITEM =
            "-fx-background-color: white;" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 2;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);" +
                    "-fx-cursor: hand;";

    private static final String STYLE_TODO_ITEM_HOVER =
            "-fx-background-color: #F8F9FA;" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 2;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.3), 10, 0, 0, 5);" +
                    "-fx-cursor: hand;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        todos = FXCollections.observableArrayList();

        // Charger les données sauvegardées
        loadTodos();

        // Configurer l'interface
        setupUI();

        // Afficher les todos
        displayTodos();
    }

    // Sauvegarder les todos
    private void saveTodos() {
        try {
            List<Todo> todoList = new ArrayList<>(todos);
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
                oos.writeObject(todoList);
                oos.writeInt(nextId);
                System.out.println("✅ Todos sauvegardés: " + todoList.size() + " tâches");
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    // Charger les todos
    @SuppressWarnings("unchecked")
    private void loadTodos() {
        File file = new File(SAVE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<Todo> loadedTodos = (List<Todo>) ois.readObject();
                nextId = ois.readInt();
                todos.setAll(loadedTodos);
                System.out.println("✅ Todos chargés: " + loadedTodos.size() + " tâches");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("❌ Erreur lors du chargement: " + e.getMessage());
                todos.clear();
                nextId = 1;
            }
        } else {
            System.out.println("📋 Aucune sauvegarde trouvée, démarrage avec liste vide");
            todos.clear();
            nextId = 1;
        }
    }

    private void setupUI() {
        // Configuration du bouton retour
        if (backButton != null) {
            backButton.setOnAction(e -> {
                System.out.println("🔙 Retour à l'ajout de cours");
                Navigation.navigateTo("coursajout.fxml", "Ajouter un cours");
            });

            backButton.setOnMouseEntered(e ->
                    backButton.setStyle("-fx-background-color: #6A1FF7; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 20; " +
                            "-fx-padding: 12 25; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand;")
            );

            backButton.setOnMouseExited(e ->
                    backButton.setStyle("-fx-background-color: #7B2FF7; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 20; " +
                            "-fx-padding: 12 25; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand;")
            );
        }

        // Configuration du ComboBox de statut pour l'ajout
        if (statusComboBox != null) {
            statusComboBox.setItems(FXCollections.observableArrayList("À faire", "En cours", "Terminé"));
            statusComboBox.setValue("À faire"); // Valeur par défaut
        }

        // Configuration du bouton d'ajout
        addTodoButton.setOnAction(e -> addTodo());
        newTodoField.setOnAction(e -> addTodo());

        // Effet hover pour le bouton d'ajout
        addTodoButton.setOnMouseEntered(e ->
                addTodoButton.setStyle("-fx-background-color: #6A1FF7; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 25; " +
                        "-fx-padding: 15 35; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 14px;")
        );

        addTodoButton.setOnMouseExited(e ->
                addTodoButton.setStyle("-fx-background-color: #7B2FF7; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 25; " +
                        "-fx-padding: 15 35; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 14px;")
        );

        // Configuration du filtre
        filterComboBox.setItems(FXCollections.observableArrayList(
                "Toutes les tâches", "À faire", "En cours", "Terminé"
        ));
        filterComboBox.setValue("Toutes les tâches");
        filterComboBox.setOnAction(e -> displayTodos());

        // Bouton pour effacer les tâches terminées
        clearCompletedButton.setOnAction(e -> clearCompleted());

        clearCompletedButton.setOnMouseEntered(e ->
                clearCompletedButton.setStyle("-fx-background-color: #FF4444; " +
                        "-fx-border-color: #FF4444; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 20; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 12 25; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-width: 2;")
        );

        clearCompletedButton.setOnMouseExited(e ->
                clearCompletedButton.setStyle("-fx-background-color: transparent; " +
                        "-fx-border-color: #FF4444; " +
                        "-fx-text-fill: #FF4444; " +
                        "-fx-border-radius: 20; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 12 25; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-width: 2;")
        );
    }

    private void displayTodos() {
        todoListContainer.getChildren().clear();

        String filter = filterComboBox.getValue();
        System.out.println("Filtre sélectionné: " + filter); // Debug

        // Si le filtre est "Toutes les tâches", on affiche tout
        if (filter.equals("Toutes les tâches")) {
            // Afficher les 3 colonnes avec toutes les tâches
            VBox aFaireBox = createStatutSection("À faire", "⭕");
            VBox enCoursBox = createStatutSection("En cours", "🔄");
            VBox termineBox = createStatutSection("Terminé", "✅");

            // Placer chaque todo dans la bonne colonne selon son statut
            for (Todo todo : todos) {
                HBox item = createTodoItem(todo);
                switch (todo.getStatut()) {
                    case "À faire":
                        aFaireBox.getChildren().add(item);
                        break;
                    case "En cours":
                        enCoursBox.getChildren().add(item);
                        break;
                    case "Terminé":
                        termineBox.getChildren().add(item);
                        break;
                    default:
                        aFaireBox.getChildren().add(item);
                }
            }

            HBox columnsBox = new HBox(20);
            columnsBox.setAlignment(Pos.TOP_LEFT);
            columnsBox.getChildren().addAll(aFaireBox, enCoursBox, termineBox);

            // Ajuster la largeur des colonnes
            aFaireBox.setPrefWidth(450);
            enCoursBox.setPrefWidth(450);
            termineBox.setPrefWidth(450);

            todoListContainer.getChildren().add(columnsBox);
        }
        // Sinon, on filtre par statut
        else {
            // Créer une seule colonne pour le statut filtré
            VBox filterBox = createStatutSection(filter, getIconeForStatut(filter));

            // Ajouter seulement les todos du statut sélectionné
            for (Todo todo : todos) {
                if (todo.getStatut().equals(filter)) {
                    HBox item = createTodoItem(todo);
                    filterBox.getChildren().add(item);
                }
            }

            filterBox.setPrefWidth(450);
            todoListContainer.getChildren().add(filterBox);
        }

        updateCounts();
    }

    private String getIconeForStatut(String statut) {
        switch (statut) {
            case "À faire": return "⭕";
            case "En cours": return "🔄";
            case "Terminé": return "✅";
            default: return "📌";
        }
    }

    private void showEmptyState() {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(50));
        emptyBox.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 20; -fx-border-color: #7B2FF7; -fx-border-width: 2; -fx-border-radius: 20;");

        Label emptyIcon = new Label("📋");
        emptyIcon.setStyle("-fx-font-size: 80px; -fx-opacity: 0.5;");

        Label emptyText = new Label("Aucune tâche à afficher");
        emptyText.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #666666;");

        Label emptySubText = new Label("Ajoutez votre première tâche ci-dessus ✨");
        emptySubText.setStyle("-fx-font-size: 14px; -fx-text-fill: #999999;");

        emptyBox.getChildren().addAll(emptyIcon, emptyText, emptySubText);
        todoListContainer.getChildren().add(emptyBox);
    }

    private VBox createStatutSection(String titre, String icone) {
        VBox section = new VBox(15);
        section.setStyle(
                "-fx-background-color: #F8F9FA;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;"
        );

        // En-tête de section
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 0 0 15 0; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 2 0;");

        Label iconLabel = new Label(icone);
        iconLabel.setStyle("-fx-font-size: 20px;");

        Label titleLabel = new Label(titre);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLabel = new Label("0");
        countLabel.setStyle(
                "-fx-background-color: #7B2FF7;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 3 10;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );
        countLabel.setId("count-" + titre);

        header.getChildren().addAll(iconLabel, titleLabel, spacer, countLabel);
        section.getChildren().add(header);

        return section;
    }

    private HBox createTodoItem(Todo todo) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(15));
        item.setStyle(STYLE_TODO_ITEM + "-fx-border-color: " + todo.getCouleur() + ";");

        // Effet hover
        item.setOnMouseEntered(e ->
                item.setStyle(STYLE_TODO_ITEM_HOVER + "-fx-border-color: " + todo.getCouleur() + ";")
        );
        item.setOnMouseExited(e ->
                item.setStyle(STYLE_TODO_ITEM + "-fx-border-color: " + todo.getCouleur() + ";")
        );

        // Checkbox personnalisée
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(todo.getStatut().equals("Terminé"));
        checkBox.setStyle(
                "-fx-scale: 1.3;" +
                        "-fx-cursor: hand;"
        );

        // Contenu principal
        VBox content = new VBox(8);

        // Titre
        Label titleLabel = new Label(todo.getTitre());
        titleLabel.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + (todo.getStatut().equals("Terminé") ? "#888888" : "#333333") + ";"
        );

        // Métadonnées (description + date)
        HBox metaBox = new HBox(15);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        if (todo.getDescription() != null && !todo.getDescription().isEmpty()) {
            Label descLabel = new Label("📝 " + todo.getDescription());
            descLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px; -fx-background-color: #F0F0F0; -fx-background-radius: 10; -fx-padding: 3 10;");
            descLabel.setMaxWidth(200);
            descLabel.setWrapText(true);
            metaBox.getChildren().add(descLabel);
        }

        Label dateLabel = new Label("📅 " + todo.getDateFormatted());
        dateLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 11px; -fx-background-color: #F5F5F5; -fx-background-radius: 8; -fx-padding: 3 8;");

        if (!metaBox.getChildren().isEmpty()) {
            Separator separator = new Separator(Orientation.VERTICAL);
            separator.setStyle("-fx-background-color: #E0E0E0;");
            metaBox.getChildren().add(separator);
        }
        metaBox.getChildren().add(dateLabel);

        content.getChildren().addAll(titleLabel, metaBox);

        // Appliquer HBox.setHgrow à content
        HBox.setHgrow(content, Priority.ALWAYS);

        // Boutons d'action
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = createIconButton("✏️", "Modifier", "#FFA500");
        Button deleteButton = createIconButton("🗑️", "Supprimer", "#FF4444");

        actions.getChildren().addAll(editButton, deleteButton);

        // Événements - Action de la checkbox
        checkBox.setOnAction(e -> {
            if (checkBox.isSelected()) {
                todo.setStatut("Terminé");
                showNotification("✅ Tâche terminée !");
            } else {
                // Quand on décoche, la tâche retourne dans "À faire"
                todo.setStatut("À faire");
                showNotification("📋 Tâche à faire");
            }
            saveTodos();
            displayTodos();
        });

        editButton.setOnAction(e -> editTodo(todo));
        deleteButton.setOnAction(e -> deleteTodo(todo));

        // Double-clic pour éditer
        item.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                editTodo(todo);
            }
        });

        item.getChildren().addAll(checkBox, content, actions);

        // Style spécial pour les tâches terminées
        if (todo.getStatut().equals("Terminé")) {
            titleLabel.setStyle(titleLabel.getStyle() + "-fx-strikethrough: true;");
        }

        return item;
    }

    private Button createIconButton(String icon, String tooltip, String color) {
        Button button = new Button(icon);
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-font-size: 16px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5;" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-background-radius: 8;"
        );
        button.setTooltip(new Tooltip(tooltip));

        // Effet hover
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: " + color + "20;" +
                                "-fx-font-size: 16px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 5;" +
                                "-fx-text-fill: " + color + ";" +
                                "-fx-background-radius: 8;" +
                                "-fx-effect: dropshadow(three-pass-box, " + color + "80, 5, 0, 0, 2);"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-font-size: 16px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 5;" +
                                "-fx-text-fill: " + color + ";" +
                                "-fx-background-radius: 8;"
                )
        );

        return button;
    }

    // MODIFICATION ICI - Ajout avec choix du statut
    private void addTodo() {
        String titre = newTodoField.getText().trim();
        if (titre.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez entrer un titre pour la tâche");
            return;
        }

        // Récupérer le statut choisi dans le ComboBox
        String statutChoisi = statusComboBox.getValue();
        if (statutChoisi == null) {
            statutChoisi = "À faire"; // Valeur par défaut
        }

        Todo newTodo = new Todo(nextId++, titre, "", statutChoisi, LocalDate.now());
        todos.add(newTodo);
        saveTodos();

        newTodoField.clear();
        // Remettre le statut par défaut pour le prochain ajout
        statusComboBox.setValue("À faire");

        // Garder le filtre actuel ou remettre sur "Toutes les tâches"
        // Si vous voulez voir la nouvelle tâche, décommentez la ligne suivante :
        // filterComboBox.setValue("Toutes les tâches");

        displayTodos();
        showNotification("✅ Tâche ajoutée avec succès");
    }

    private void editTodo(Todo todo) {
        Dialog<Todo> dialog = new Dialog<>();
        dialog.setTitle("Modifier la tâche");
        dialog.setHeaderText("Modifier : " + todo.getTitre());

        ButtonType saveButtonType = new ButtonType("💾 Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 30, 20, 20));
        grid.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 15;");

        Label titreLabel = new Label("Titre :");
        titreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label descLabel = new Label("Description :");
        descLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label statutLabel = new Label("Statut :");
        statutLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        TextField titreField = new TextField(todo.getTitre());
        titreField.setPromptText("Titre de la tâche");
        titreField.setStyle("-fx-background-radius: 10; -fx-padding: 10; -fx-border-color: #7B2FF7; -fx-border-width: 1; -fx-border-radius: 10;");

        TextArea descArea = new TextArea(todo.getDescription());
        descArea.setPromptText("Description détaillée (optionnelle)");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        descArea.setStyle("-fx-background-radius: 10; -fx-padding: 10; -fx-border-color: #7B2FF7; -fx-border-width: 1; -fx-border-radius: 10;");

        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.setItems(FXCollections.observableArrayList("À faire", "En cours", "Terminé"));
        statutCombo.setValue(todo.getStatut());
        statutCombo.setStyle("-fx-background-radius: 10; -fx-padding: 5; -fx-border-color: #7B2FF7; -fx-border-width: 1; -fx-border-radius: 10;");

        grid.add(titreLabel, 0, 0);
        grid.add(titreField, 1, 0);
        grid.add(descLabel, 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(statutLabel, 0, 2);
        grid.add(statutCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-border-color: #7B2FF7; -fx-border-width: 3; -fx-border-radius: 15; -fx-background-color: white;");

        titreField.requestFocus();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                todo.setTitre(titreField.getText());
                todo.setDescription(descArea.getText());
                todo.setStatut(statutCombo.getValue());
                return todo;
            }
            return null;
        });

        Optional<Todo> result = dialog.showAndWait();
        result.ifPresent(updatedTodo -> {
            saveTodos();
            displayTodos();
            showNotification("✏️ Tâche modifiée");
        });
    }

    private void deleteTodo(Todo todo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la tâche");
        alert.setContentText("Voulez-vous vraiment supprimer : \"" + todo.getTitre() + "\" ?");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-border-color: #7B2FF7; -fx-border-width: 2; -fx-border-radius: 10;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            todos.remove(todo);
            saveTodos();
            displayTodos();
            showNotification("🗑️ Tâche supprimée");
        }
    }

    private void clearCompleted() {
        boolean hasCompleted = todos.stream().anyMatch(t -> t.getStatut().equals("Terminé"));

        if (!hasCompleted) {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Aucune tâche terminée à effacer");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Effacer les tâches terminées");
        alert.setContentText("Voulez-vous vraiment supprimer toutes les tâches terminées ?");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-border-color: #7B2FF7; -fx-border-width: 2; -fx-border-radius: 10;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            todos.removeIf(t -> t.getStatut().equals("Terminé"));
            saveTodos();
            displayTodos();
            showNotification("✅ Tâches terminées effacées");
        }
    }

    private void updateCounts() {
        long aFaire = todos.stream().filter(t -> t.getStatut().equals("À faire")).count();
        long enCours = todos.stream().filter(t -> t.getStatut().equals("En cours")).count();
        long termine = todos.stream().filter(t -> t.getStatut().equals("Terminé")).count();
        long total = todos.size();

        todoCountLabel.setText(String.valueOf(aFaire));
        inProgressCountLabel.setText(String.valueOf(enCours));
        doneCountLabel.setText(String.valueOf(termine));

        if (totalCountLabel != null) {
            totalCountLabel.setText(String.valueOf(total));
        }

        updateSectionCount("À faire", aFaire);
        updateSectionCount("En cours", enCours);
        updateSectionCount("Terminé", termine);
    }

    private void updateSectionCount(String section, long count) {
        todoListContainer.lookupAll("#count-" + section).forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setText(String.valueOf(count));
            }
        });
    }

    private void showNotification(String message) {
        Label notification = new Label(message);
        notification.setStyle(
                "-fx-background-color: #7B2FF7;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12 25;" +
                        "-fx-background-radius: 30;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.5), 10, 0, 0, 5);"
        );

        StackPane root = (StackPane) todoListContainer.getScene().getRoot();
        root.getChildren().add(notification);
        StackPane.setAlignment(notification, Pos.BOTTOM_CENTER);
        StackPane.setMargin(notification, new Insets(0, 0, 30, 0));

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> root.getChildren().remove(notification));
        pause.play();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}