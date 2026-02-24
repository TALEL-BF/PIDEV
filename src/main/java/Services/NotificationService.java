package Services;

import Entites.Seance;
import javafx.application.Platform;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.Objects;

public class NotificationService {

    private final SeanceServices seanceServices;
    private final ScheduledExecutorService scheduler;
    private final Set<Integer> notifiedSeanceIds;

    public NotificationService() {
        this.seanceServices = new SeanceServices();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // Ensure thread stops when app exits
            return t;
        });
        this.notifiedSeanceIds = new HashSet<>();
    }

    public void startService() {
        // Check every minute
        scheduler.scheduleAtFixedRate(this::checkUpcomingSeances, 0, 1, TimeUnit.MINUTES);
    }

    public void stopService() {
        scheduler.shutdown();
    }

    private void checkUpcomingSeances() {
        try {
            List<Seance> seances = seanceServices.afficherSeances();
            LocalDateTime now = LocalDateTime.now();

            for (Seance seance : seances) {
                // Only check planned or confirmed sessions
                if (!"planifiee".equalsIgnoreCase(seance.getStatutSeance()) && 
                    !"confirme".equalsIgnoreCase(seance.getStatutSeance())) {
                    continue;
                }

                long minutesUntilStart = ChronoUnit.MINUTES.between(now, seance.getDateSeance());

                // Check if session starts within the next 15 minutes (0 to 15 min window)
                if (minutesUntilStart >= 0 && minutesUntilStart <= 15) {
                    if (!notifiedSeanceIds.contains(seance.getIdSeance())) {
                        showNotification(seance, minutesUntilStart);
                        notifiedSeanceIds.add(seance.getIdSeance());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking seance notifications: " + e.getMessage());
        }
    }

    private void showNotification(Seance seance, long minutes) {
        Platform.runLater(() -> {
            Node graphic = createNotificationNode(seance, minutes);

            Notifications.create()
                    .graphic(graphic)
                    .position(Pos.BOTTOM_RIGHT)
                    .hideAfter(Duration.seconds(10))
                    .owner(null) // Ensure it displays on the active screen
                    .show(); // show() without arguments uses the graphic fully
        });
    }

    private Node createNotificationNode(Seance seance, long minutes) {
        VBox root = new VBox(8);
        root.getStyleClass().add("modern-notification");
        // Load stylesheet explicitly if needed, but styling usually flows from parent if added to Scene.
        // Since Notification is a Popup, we might need to add stylesheet to it or rely on inline styles if scene is not parent.
        // For robustness, we'll rely on the class but also ensure the popup has the stylesheet if possible.
        // A simpler way for a popup is to apply styles inline or assume the popup scene gets styles from the application.
        // ControlsFX notifications usually inherit if created from a Stage owner, but here owner is null (desktop).
        // Let's force load the stylesheet into the root node's scene once attached, but standard JavaFX CSS needs a Scene.
        // A trick is to use inline styles for the container if external CSS doesn't apply easily to desktop popups.

        // Let's try adding the stylesheet URL to the root
        try {
            root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/app.css")).toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load notification CSS: " + e.getMessage());
        }

        // Header with Icon and Title
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("⏰"); // Simple Unicode icon
        iconLabel.getStyleClass().add("notification-icon");

        Label titleLabel = new Label("Rappel de Séance");
        titleLabel.getStyleClass().add("notification-title");

        header.getChildren().addAll(iconLabel, titleLabel);

        // Content
        Label messageLabel = new Label("La séance \"" + seance.getTitreSeance() + "\" commence dans " + minutes + " minutes.");
        messageLabel.getStyleClass().add("notification-message");
        messageLabel.setWrapText(true);

        root.getChildren().addAll(header, messageLabel);

        return root;
    }
}
