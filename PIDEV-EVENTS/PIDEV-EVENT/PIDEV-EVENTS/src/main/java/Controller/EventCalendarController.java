package Controller;

import Entites.Event;
import Services.EventServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EventCalendarController {

    @FXML private Button backButton;
    @FXML private Button prevMonthBtn;
    @FXML private Button nextMonthBtn;
    @FXML private Button prevWeekBtn;
    @FXML private Button nextWeekBtn;
    @FXML private ToggleButton monthViewBtn;
    @FXML private ToggleButton weekViewBtn;
    @FXML private ToggleGroup viewToggle;
    @FXML private Label monthYearLabel;
    @FXML private Label weekRangeLabel;
    @FXML private GridPane calendarGrid;
    @FXML private VBox eventsListContainer;
    @FXML private Label selectedDateLabel;
    @FXML private VBox eventsList;
    @FXML private HBox weekNavigation;

    private EventServices eventServices = new EventServices();
    private LocalDate currentDate = LocalDate.now();
    private LocalDate selectedDate = LocalDate.now();
    private LocalDate weekStart;
    private List<Event> allEvents;

    private boolean isMonthView = true;

    // Style constants - Palette violette
    private static final String COLOR_PRIMARY = "#8b5cf6";  // Violet principal
    private static final String COLOR_PRIMARY_LIGHT = "#ede9fe"; // Violet très clair
    private static final String COLOR_PRIMARY_MEDIUM = "#a78bfa"; // Violet moyen
    private static final String COLOR_BACKGROUND = "#f5f3ff"; // Fond violet pâle

    @FXML
    public void initialize() {
        allEvents = eventServices.afficherEvent();

        // Initialiser la semaine courante
        weekStart = currentDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1);

        styleMainContainer();
        setupViewToggle();

        updateCalendar();
        showEventsForDate(selectedDate);

        // Navigation mois
        styleNavButton(prevMonthBtn, "◀");
        styleNavButton(nextMonthBtn, "▶");

        prevMonthBtn.setOnAction(e -> {
            currentDate = currentDate.minusMonths(1);
            weekStart = currentDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1);
            updateCalendar();
            animateTransition();
        });

        nextMonthBtn.setOnAction(e -> {
            currentDate = currentDate.plusMonths(1);
            weekStart = currentDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1);
            updateCalendar();
            animateTransition();
        });

        // Navigation semaine
        styleNavButton(prevWeekBtn, "◀");
        styleNavButton(nextWeekBtn, "▶");

        prevWeekBtn.setOnAction(e -> {
            weekStart = weekStart.minusWeeks(1);
            if (isMonthView) {
                currentDate = weekStart;
            }
            updateCalendar();
            animateTransition();
        });

        nextWeekBtn.setOnAction(e -> {
            weekStart = weekStart.plusWeeks(1);
            if (isMonthView) {
                currentDate = weekStart;
            }
            updateCalendar();
            animateTransition();
        });

        styleBackButton();
    }

    private void setupViewToggle() {
        monthViewBtn.setToggleGroup(viewToggle);
        weekViewBtn.setToggleGroup(viewToggle);

        monthViewBtn.setSelected(true);

        monthViewBtn.setOnAction(e -> {
            isMonthView = true;
            monthViewBtn.setStyle(getActiveToggleStyle());
            weekViewBtn.setStyle(getInactiveToggleStyle());
            weekNavigation.setVisible(false);
            updateCalendar();
        });

        weekViewBtn.setOnAction(e -> {
            isMonthView = false;
            weekViewBtn.setStyle(getActiveToggleStyle());
            monthViewBtn.setStyle(getInactiveToggleStyle());
            weekNavigation.setVisible(true);
            updateCalendar();
        });
    }

    private String getActiveToggleStyle() {
        return "-fx-background-color: " + COLOR_PRIMARY + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 8 20;" +
                "-fx-cursor: hand;";
    }

    private String getInactiveToggleStyle() {
        return "-fx-background-color: white;" +
                "-fx-text-fill: " + COLOR_PRIMARY + ";" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: " + COLOR_PRIMARY + ";" +
                "-fx-border-radius: 20;" +
                "-fx-padding: 8 20;" +
                "-fx-cursor: hand;";
    }

    private void styleMainContainer() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(139, 92, 246, 0.3));
        shadow.setRadius(15);
        shadow.setOffsetY(5);

        calendarGrid.setEffect(shadow);
        eventsListContainer.setEffect(shadow);
    }

    private void styleNavButton(Button btn, String icon) {
        btn.setText(icon);
        btn.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY_MEDIUM + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 50;" +
                        "-fx-min-width: 40;" +
                        "-fx-min-height: 40;" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-background-color: " + COLOR_PRIMARY + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 18px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 50;" +
                                "-fx-min-width: 40;" +
                                "-fx-min-height: 40;" +
                                "-fx-cursor: hand;"
                )
        );

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-background-color: " + COLOR_PRIMARY_MEDIUM + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 18px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 50;" +
                                "-fx-min-width: 40;" +
                                "-fx-min-height: 40;" +
                                "-fx-cursor: hand;"
                )
        );
    }

    private void styleBackButton() {
        backButton.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY_MEDIUM + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 8 20;" +
                        "-fx-cursor: hand;"
        );
    }

    private void animateTransition() {
        calendarGrid.setOpacity(0.5);
        new Thread(() -> {
            try {
                Thread.sleep(100);
                javafx.application.Platform.runLater(() ->
                        calendarGrid.setOpacity(1.0)
                );
            } catch (InterruptedException e) {}
        }).start();
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        if (isMonthView) {
            updateMonthView();
        } else {
            updateWeekView();
        }
    }

    private void updateMonthView() {
        // Style du mois/année
        monthYearLabel.setText(
                currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                        .toUpperCase()
        );
        monthYearLabel.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + COLOR_PRIMARY + ";"
        );

        weekRangeLabel.setVisible(false);

        // Jours de la semaine
        String[] jours = {"LUN", "MAR", "MER", "JEU", "VEN", "SAM", "DIM"};
        for (int i = 0; i < 7; i++) {
            Label jourLabel = new Label(jours[i]);
            jourLabel.setStyle(
                    "-fx-font-weight: bold;" +
                            "-fx-text-fill: " + COLOR_PRIMARY + ";" +
                            "-fx-padding: 10 5;" +
                            "-fx-font-size: 14px;" +
                            "-fx-background-color: " + COLOR_PRIMARY_LIGHT + ";" +
                            "-fx-background-radius: 20 20 0 0;" +
                            "-fx-alignment: center;"
            );
            jourLabel.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(jourLabel, Priority.ALWAYS);
            calendarGrid.add(jourLabel, i, 0);
        }

        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = yearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            Button dayButton = createDayButton(day, date);
            calendarGrid.add(dayButton, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private void updateWeekView() {
        // Afficher la plage de la semaine
        LocalDate weekEnd = weekStart.plusDays(6);
        String weekRange = "Semaine du " +
                weekStart.format(DateTimeFormatter.ofPattern("dd MMM")) +
                " au " +
                weekEnd.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

        weekRangeLabel.setText(weekRange);
        weekRangeLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + COLOR_PRIMARY + ";"
        );
        weekRangeLabel.setVisible(true);

        monthYearLabel.setVisible(false);

        // En-têtes des jours
        String[] jours = {"LUN", "MAR", "MER", "JEU", "VEN", "SAM", "DIM"};
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);

            VBox headerBox = new VBox(5);
            headerBox.setAlignment(Pos.CENTER);
            headerBox.setStyle(
                    "-fx-background-color: " + COLOR_PRIMARY_LIGHT + ";" +
                            "-fx-background-radius: 20 20 0 0;" +
                            "-fx-padding: 10 5;"
            );

            Label jourLabel = new Label(jours[i]);
            jourLabel.setStyle(
                    "-fx-font-weight: bold;" +
                            "-fx-text-fill: " + COLOR_PRIMARY + ";" +
                            "-fx-font-size: 14px;"
            );

            Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
            dateLabel.setStyle(
                    "-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: " + COLOR_PRIMARY + ";"
            );

            headerBox.getChildren().addAll(jourLabel, dateLabel);

            headerBox.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(headerBox, Priority.ALWAYS);
            calendarGrid.add(headerBox, i, 0);
        }

        // Créer les cellules pour chaque jour de la semaine
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);

            VBox dayCell = new VBox(5);
            dayCell.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: " + COLOR_PRIMARY_LIGHT + ";" +
                            "-fx-border-width: 1;" +
                            "-fx-padding: 5;"
            );
            dayCell.setPrefHeight(150);
            dayCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // Ajouter les événements du jour
            List<Event> eventsOnDay = getEventsForDate(date);
            if (!eventsOnDay.isEmpty()) {
                dayCell.setStyle(
                        "-fx-background-color: " + COLOR_PRIMARY_LIGHT + ";" +
                                "-fx-border-color: " + COLOR_PRIMARY + ";" +
                                "-fx-border-width: 2;" +
                                "-fx-padding: 5;"
                );

                Label countLabel = new Label("📅 " + eventsOnDay.size() + " événement(s)");
                countLabel.setStyle(
                        "-fx-text-fill: " + COLOR_PRIMARY + ";" +
                                "-fx-font-size: 11px;" +
                                "-fx-font-weight: bold;"
                );
                dayCell.getChildren().add(countLabel);

                // Afficher tous les événements (pas de limite)
                for (Event e : eventsOnDay) {
                    Label eventLabel = new Label("• " + e.getTitre());
                    eventLabel.setStyle(
                            "-fx-text-fill: #4a5568;" +
                                    "-fx-font-size: 10px;" +
                                    "-fx-cursor: hand;" +
                                    "-fx-wrap-text: true;"
                    );

                    eventLabel.setOnMouseClicked(ev -> openEventDetails(e));
                    dayCell.getChildren().add(eventLabel);
                }
            }

            // Rendre la cellule cliquable pour voir tous les événements
            dayCell.setOnMouseClicked(e -> {
                selectedDate = date;
                showEventsForDate(date);
            });

            GridPane.setHgrow(dayCell, Priority.ALWAYS);
            GridPane.setVgrow(dayCell, Priority.ALWAYS);
            calendarGrid.add(dayCell, i, 1);
        }
    }

    private Button createDayButton(int day, LocalDate date) {
        Button dayButton = new Button(String.valueOf(day));
        dayButton.setPrefSize(80, 60);
        dayButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        String baseStyle =
                "-fx-background-color: " + (date.equals(LocalDate.now()) ? COLOR_PRIMARY_LIGHT : "white") + ";" +
                        "-fx-border-color: " + COLOR_PRIMARY_MEDIUM + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: " + (date.equals(LocalDate.now()) ? "bold" : "normal") + ";" +
                        "-fx-cursor: hand;";

        dayButton.setStyle(baseStyle);

        List<Event> eventsOnDay = getEventsForDate(date);
        if (!eventsOnDay.isEmpty()) {
            dayButton.setStyle(baseStyle +
                    "-fx-background-color: " + COLOR_PRIMARY_LIGHT + ";" +
                    "-fx-border-color: " + COLOR_PRIMARY + ";" +
                    "-fx-border-width: 2;" +
                    "-fx-text-fill: " + COLOR_PRIMARY + ";"
            );

            Tooltip tp = new Tooltip(eventsOnDay.size() + " événement(s)");
            tp.setStyle("-fx-font-size: 12px; -fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white;");
            Tooltip.install(dayButton, tp);
        }

        dayButton.setOnAction(e -> {
            selectedDate = date;
            showEventsForDate(date);
        });

        dayButton.setOnMouseEntered(e ->
                dayButton.setStyle(baseStyle +
                        "-fx-background-color: " + COLOR_PRIMARY_LIGHT + ";" +
                        "-fx-scale-x: 1.05;" +
                        "-fx-scale-y: 1.05;"
                )
        );

        dayButton.setOnMouseExited(e ->
                dayButton.setStyle(baseStyle)
        );

        return dayButton;
    }

    private List<Event> getEventsForDate(LocalDate date) {
        return allEvents.stream()
                .filter(e -> e.getDateDebut() != null)
                .filter(e -> e.getDateDebut().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    private void showEventsForDate(LocalDate date) {
        eventsList.getChildren().clear();

        selectedDateLabel.setText(
                date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        );
        selectedDateLabel.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + COLOR_PRIMARY + ";" +
                        "-fx-padding: 10 0;"
        );

        List<Event> eventsOnDay = getEventsForDate(date);

        if (eventsOnDay.isEmpty()) {
            Label noEvent = new Label("✨ Aucun événement programmé ce jour");
            noEvent.setStyle(
                    "-fx-text-fill: #999;" +
                            "-fx-font-style: italic;" +
                            "-fx-padding: 20;" +
                            "-fx-font-size: 14px;"
            );
            eventsList.getChildren().add(noEvent);
        } else {
            Label countLabel = new Label("📅 " + eventsOnDay.size() + " événement(s)");
            countLabel.setStyle(
                    "-fx-text-fill: " + COLOR_PRIMARY + ";" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 0 0 10 0;"
            );
            eventsList.getChildren().add(countLabel);

            // Afficher TOUS les événements sans ScrollPane
            for (Event e : eventsOnDay) {
                VBox eventCard = createEventCard(e);
                eventsList.getChildren().add(eventCard);
            }
        }
    }

    private VBox createEventCard(Event e) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: " + COLOR_PRIMARY_MEDIUM + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.1), 5, 0, 0, 2);"
        );

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label timeIcon = new Label("⏰");
        timeIcon.setStyle("-fx-font-size: 16px;");

        Label timeLabel = new Label(formatTime(e.getHeureDebut()));
        timeLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + COLOR_PRIMARY + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: " + COLOR_PRIMARY_LIGHT + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 4 10;"
        );

        headerBox.getChildren().addAll(timeIcon, timeLabel);

        Label titleLabel = new Label(e.getTitre());
        titleLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2d3748;" +
                        "-fx-wrap-text: true;"
        );
        titleLabel.setMaxWidth(250);

        HBox locationBox = new HBox(5);
        locationBox.setAlignment(Pos.CENTER_LEFT);

        Label locationIcon = new Label("📍");
        locationIcon.setStyle("-fx-font-size: 12px;");

        Label locationLabel = new Label(e.getLieu());
        locationLabel.setStyle(
                "-fx-text-fill: #666;" +
                        "-fx-font-size: 13px;"
        );

        locationBox.getChildren().addAll(locationIcon, locationLabel);

        card.getChildren().addAll(headerBox, titleLabel, locationBox);

        card.setOnMouseEntered(ev ->
                card.setStyle(
                        "-fx-background-color: " + COLOR_PRIMARY_LIGHT + ";" +
                                "-fx-background-radius: 15;" +
                                "-fx-padding: 15;" +
                                "-fx-border-color: " + COLOR_PRIMARY + ";" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 15;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.2), 10, 0, 0, 5);" +
                                "-fx-scale-x: 1.02;" +
                                "-fx-scale-y: 1.02;"
                )
        );

        card.setOnMouseExited(ev ->
                card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 15;" +
                                "-fx-padding: 15;" +
                                "-fx-border-color: " + COLOR_PRIMARY_MEDIUM + ";" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 15;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.1), 5, 0, 0, 2);"
                )
        );

        card.setOnMouseClicked(ev -> openEventDetails(e));

        return card;
    }

    private void openEventDetails(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetailsPage.fxml"));
            Parent root = loader.load();
            EventDetailsController controller = loader.getController();
            controller.setEvent(event);
            Stage stage = (Stage) calendarGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(event.getTitre());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails de l'événement");
        }
    }

    private String formatTime(java.sql.Time time) {
        if (time == null) return "?";
        return time.toString().substring(0, 5);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goBackToShowEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ShowEvent.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();

            root.setOpacity(0);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Événements");
            stage.centerOnScreen();

            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), root
            );
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}