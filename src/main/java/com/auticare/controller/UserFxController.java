package com.auticare.controller;

import com.auticare.entity.User;
import com.auticare.service.UserService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Scope("prototype")
public class UserFxController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminDashboardController dashboardController;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> roleFilter;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, User> colUser;
    @FXML
    private TableColumn<User, User> colContact;
    @FXML
    private TableColumn<User, String> colRole;
    @FXML
    private TableColumn<User, String> colStatus;
    @FXML
    private TableColumn<User, User> colActions;
    @FXML
    private Label userCountLabel;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;

    @FXML
    public void initialize() {
        System.out.println("🔧 Initialisation de UserFxController...");

        if (userService == null) {
            System.err.println("❌ userService est NULL!");
        } else {
            System.out.println("✅ userService injecté avec succès");
        }

        roleFilter.setItems(FXCollections.observableArrayList("Tous les rôles", "Apprenant", "Parent", "Thérapeute",
                "Éducateur", "ADMIN"));
        statusFilter.setItems(FXCollections.observableArrayList("Tous les statuts", "Actif", "En attente", "Inactif"));

        setupColumns();
        loadUsers();

        filteredData = new FilteredList<>(userList, p -> true);
        searchField.textProperty().addListener((obs, old, newVal) -> updateFilter());
        roleFilter.valueProperty().addListener((obs, old, newVal) -> updateFilter());
        statusFilter.valueProperty().addListener((obs, old, newVal) -> updateFilter());

        userTable.setItems(filteredData);
        updateUserCount();
        System.out.println("✅ Initialisation terminée - " + userList.size() + " utilisateurs dans la liste");
    }

    private void setupColumns() {
        // Colonne UTILISATEUR avec photo
        colUser.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colUser.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(12);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    // Créer l'avatar (photo ou initiale)
                    StackPane avatar = createAvatar(user);

                    // Informations utilisateur
                    VBox text = new VBox(2);
                    Label name = new Label(user.getName());
                    name.getStyleClass().add("user-name");

                    String dateText = "Inscrit le ";
                    if (user.getRegistrationDate() != null) {
                        dateText += user.getRegistrationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } else {
                        dateText += "N/A";
                    }
                    Label date = new Label(dateText);
                    date.getStyleClass().add("user-date");

                    text.getChildren().addAll(name, date);
                    box.getChildren().addAll(avatar, text);
                    setGraphic(box);
                }
            }

            // Méthode pour créer l'avatar avec photo ou initiale
            private StackPane createAvatar(User user) {
                // Vérifier si l'utilisateur a une photo
                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                    try {
                        String photoPath = user.getPhotoUrl();
                        System.out.println("🔍 Tentative de chargement photo pour " + user.getName() + ": " + photoPath);

                        // Nettoyer le chemin
                        if (photoPath.startsWith("/")) {
                            photoPath = photoPath.substring(1);
                        }

                        // Remplacer les backslashes par des forward slashes
                        photoPath = photoPath.replace("\\", "/");

                        File imageFile = new File(photoPath);
                        System.out.println("📁 Chemin absolu: " + imageFile.getAbsolutePath());
                        System.out.println("📁 Fichier existe? " + imageFile.exists());

                        if (imageFile.exists()) {
                            Image image = new Image(imageFile.toURI().toString());
                            ImageView imageView = new ImageView(image);
                            imageView.setFitHeight(45);
                            imageView.setFitWidth(45);
                            imageView.setPreserveRatio(true);

                            // Découper en cercle
                            Circle clip = new Circle(22.5, 22.5, 22.5);
                            imageView.setClip(clip);

                            System.out.println("✅ Photo chargée avec succès pour: " + user.getName());
                            return new StackPane(imageView);
                        } else {
                            System.out.println("⚠️ Fichier photo non trouvé pour: " + user.getName());
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Erreur chargement photo pour " + user.getName() + ": " + e.getMessage());
                    }
                }

                // Pas de photo ou erreur de chargement, utiliser l'initiale
                Circle circle = new Circle(22.5);
                circle.getStyleClass().add("avatar-circle");

                Label initialLabel = new Label(user.getName().substring(0, 1).toUpperCase());
                initialLabel.getStyleClass().add("avatar-text");

                return new StackPane(circle, initialLabel);
            }
        });

        // Colonne CONTACT
        colContact.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colContact.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    VBox box = new VBox(2);
                    Label email = new Label(user.getEmail());
                    email.getStyleClass().add("contact-email");

                    Label phone = new Label(user.getPhoneNumber() != null ? user.getPhoneNumber() : "--");
                    phone.getStyleClass().add("contact-phone");

                    box.getChildren().addAll(email, phone);
                    setGraphic(box);
                }
            }
        });

        // Colonne RÔLE
        colRole.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String role = user.getRole() != null ? user.getRole() : "";
            return new SimpleStringProperty(role);
        });

        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null || role.isEmpty()) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(role);
                    badge.getStyleClass().add("badge-role");

                    String roleLower = role.toLowerCase();
                    if (roleLower.contains("admin")) {
                        badge.getStyleClass().add("role-admin");
                    } else if (roleLower.contains("parent")) {
                        badge.getStyleClass().add("role-parent");
                    } else if (roleLower.contains("educateur") || roleLower.contains("éducateur")) {
                        badge.getStyleClass().add("role-educator");
                    } else if (roleLower.contains("therapeute") || roleLower.contains("thérapeute")) {
                        badge.getStyleClass().add("role-therapist");
                    } else if (roleLower.contains("apprenant")) {
                        badge.getStyleClass().add("role-learner");
                    } else {
                        badge.getStyleClass().add("role-default");
                    }

                    setGraphic(badge);
                }
            }
        });

        // Colonne STATUT
        colStatus.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String status = user.getStatus() != null ? user.getStatus() : "";
            return new SimpleStringProperty(status);
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null || status.isEmpty()) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("badge-role");

                    String statusLower = status.toLowerCase();
                    if (statusLower.contains("actif")) {
                        badge.getStyleClass().add("status-active");
                    } else if (statusLower.contains("en attente")) {
                        badge.getStyleClass().add("status-pending");
                    } else if (statusLower.contains("inactif")) {
                        badge.getStyleClass().add("status-inactive");
                    } else {
                        badge.getStyleClass().add("status-default");
                    }

                    setGraphic(badge);
                }
            }
        });

        // Colonne ACTIONS
        colActions.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colActions.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    HBox actions = new HBox(8);
                    actions.setAlignment(javafx.geometry.Pos.CENTER);

                    Button btnView = new Button("👁");
                    btnView.getStyleClass().addAll("action-button", "btn-view");
                    btnView.setOnAction(e -> showProfileView(user));

                    Button btnEdit = new Button("✏");
                    btnEdit.getStyleClass().addAll("action-button", "btn-edit");
                    btnEdit.setOnAction(e -> showEditView(user));

                    Button btnDelete = new Button("🗑");
                    btnDelete.getStyleClass().addAll("action-button", "btn-delete");
                    btnDelete.setOnAction(e -> deleteUser(user));

                    actions.getChildren().addAll(btnView, btnEdit, btnDelete);
                    setGraphic(actions);
                }
            }
        });
    }

    private void updateFilter() {
        filteredData.setPredicate(user -> {
            String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
            String role = roleFilter.getValue();
            String status = statusFilter.getValue();

            boolean matchesSearch = user.getName().toLowerCase().contains(search)
                    || user.getEmail().toLowerCase().contains(search);

            boolean matchesRole = role == null || role.equals("Tous les rôles") ||
                    (user.getRole() != null && user.getRole().equalsIgnoreCase(role));

            boolean matchesStatus = status == null || status.equals("Tous les statuts") ||
                    (user.getStatus() != null && user.getStatus().equalsIgnoreCase(status));

            return matchesSearch && matchesRole && matchesStatus;
        });

        // Mettre à jour le compteur après filtrage
        updateUserCount();
    }

    private void loadUsers() {
        System.out.println("🔄 Chargement des utilisateurs...");
        try {
            List<User> users = userService.getAllUsers();
            System.out.println("📋 " + users.size() + " utilisateurs trouvés");

            // Debug: afficher les détails
            for (User user : users) {
                System.out.println("   - " + user.getName() +
                        " | Rôle: " + user.getRole() +
                        " | Statut: " + user.getStatus());
            }

            userList.setAll(users);
            updateUserCount();

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateUserCount() {
        if (userCountLabel != null) {
            int count = filteredData != null ? filteredData.size() : userList.size();
            userCountLabel.setText(String.valueOf(count));
        }
    }

    @FXML
    private void showAddUserParams() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddUser.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Node view = loader.load();

            UserFormController controller = loader.getController();
            controller.setUser(new User());

            dashboardController.setContent(view, "Nouvel Utilisateur", "Ajouter un membre");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showEditView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditUser.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Node view = loader.load();

            UserFormController controller = loader.getController();
            controller.setUser(user);
            controller.setEditMode(true);

            dashboardController.setContent(view, "Modifier l'Utilisateur", "Mise à jour des infos");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showProfileView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserProfile.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Node view = loader.load();

            UserProfileController controller = loader.getController();
            controller.setUser(user);

            dashboardController.setContent(view, "Profil", "Détails utilisateur");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + user.getName() + " ?");
        alert.setContentText("Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                userService.deleteUser(user.getId());
                refreshList();
            }
        });
    }

    public void refreshList() {
        System.out.println("🔄 Rafraîchissement de la liste...");
        loadUsers();
        userTable.setItems(filteredData);
        userTable.refresh();
    }
}