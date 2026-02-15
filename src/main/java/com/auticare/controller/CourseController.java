package com.auticare.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CourseController {

    @FXML
    private TableView<?> courseTable;

    @FXML
    private TableColumn<?, ?> colTitle;

    @FXML
    private TableColumn<?, ?> colCategory;

    @FXML
    private TableColumn<?, ?> colAuthor;

    @FXML
    private TableColumn<?, ?> colActions;

    @FXML
    public void initialize() {
        // Initialization logic for courses list
    }
}
