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
import java.util.Optional;

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

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;

    @FXML
    public void initialize() {
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
    }

    private void setupColumns() {
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

                    javafx.scene.layout.StackPane avatar;
                    if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                        File imageFile = new File(user.getPhotoUrl().replaceFirst("/", ""));
                        if (imageFile.exists()) {
                            ImageView iv = new ImageView(new Image(imageFile.toURI().toString()));
                            iv.setFitHeight(40);
                            iv.setFitWidth(40);
                            Circle clip = new Circle(20, 20, 20);
                            iv.setClip(clip);
                            avatar = new javafx.scene.layout.StackPane(iv);
                        } else {
                            Circle circle = new Circle(20, Color.web("#f1f2f6"));
                            Label label = new Label(user.getName().substring(0, 1).toUpperCase());
                            label.setStyle("-fx-text-fill: #7b2ff7; -fx-font-weight: bold;");
                            avatar = new javafx.scene.layout.StackPane(circle, label);
                        }
                    } else {
                        Circle circle = new Circle(20, Color.web("#f1f2f6"));
                        Label label = new Label(user.getName().substring(0, 1).toUpperCase());
                        label.setStyle("-fx-text-fill: #7b2ff7; -fx-font-weight: bold;");
                        avatar = new javafx.scene.layout.StackPane(circle, label);
                    }

                    VBox text = new VBox(2);
                    Label name = new Label(user.getName());
                    name.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3436;");
                    Label date = new Label("Inscrit le " + (user.getRegistrationDate() != null
                            ? user.getRegistrationDate().format(DateTimeFormatter.ISO_DATE)
                            : "N/A"));
                    date.setStyle("-fx-text-fill: #636e72; -fx-font-size: 11px;");
                    text.getChildren().addAll(name, date);

                    box.getChildren().addAll(avatar, text);
                    setGraphic(box);
                }
            }
        });

        colContact.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colContact.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null)
                    setGraphic(null);
                else {
                    VBox box = new VBox(2);
                    Label email = new Label(user.getEmail());
                    email.setStyle("-fx-text-fill: #636e72; -fx-font-size: 12px;");
                    Label phone = new Label(user.getPhoneNumber() != null ? user.getPhoneNumber() : "--");
                    phone.setStyle("-fx-text-fill: #636e72; -fx-font-size: 12px;");
                    box.getChildren().addAll(email, phone);
                    setGraphic(box);
                }
            }
        });

        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null)
                    setGraphic(null);
                else {
                    Label badge = new Label(role.toUpperCase());
                    badge.getStyleClass().add("badge");
                    String css = "badge-" + role.toLowerCase().replace("é", "e");
                    badge.getStyleClass().add(css);
                    setGraphic(badge);
                }
            }
        });

        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null)
                    setGraphic(null);
                else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("badge");
                    String css = "badge-" + status.toLowerCase().replace(" ", "");
                    badge.getStyleClass().add(css);
                    setGraphic(badge);
                }
            }
        });

        colActions.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colActions.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null)
                    setGraphic(null);
                else {
                    HBox actions = new HBox(8);
                    actions.setAlignment(javafx.geometry.Pos.CENTER);

                    Button btnView = new Button("👁");
                    btnView.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                    btnView.setOnAction(e -> showProfileView(user));

                    Button btnEdit = new Button("✏");
                    btnEdit.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                    btnEdit.setOnAction(e -> showEditView(user));

                    Button btnDelete = new Button("🗑");
                    btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand;");
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
            boolean matchesRole = role == null || role.equals("Tous les rôles") || user.getRole().equals(role);
            boolean matchesStatus = status == null || status.equals("Tous les statuts")
                    || (user.getStatus() != null && user.getStatus().equals(status));

            return matchesSearch && matchesRole && matchesStatus;
        });
    }

    private void loadUsers() {
        userList.setAll(userService.getAllUsers());
    }

    @FXML
    private void showAddUserParams() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddUser.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Node view = loader.load();

            UserFormController controller = loader.getController();
            controller.setUser(new User()); // Fix: Initialize user for navigation mode

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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + user.getName() + " ?", ButtonType.YES,
                ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                userService.deleteUser(user.getId());
                loadUsers();
            }
        });
    }
}
