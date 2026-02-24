package Controller;

import Services.MeetingService;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.Alert;

import java.net.URL;
import java.util.ResourceBundle;

public class MeetingController implements Initializable {

    @FXML
    private Button closeBtn;

    @FXML
    private Button copyLinkBtn;

    @FXML
    private StackPane meetingContainer;

    @FXML
    private WebView show;

    @FXML
    private Button startMeetingBtn;

    @FXML
    private VBox startScreen;

    @FXML
    private ProgressIndicator loadingIndicator;

    private MeetingService meetingService;
    private String currentMeetingUrl;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        meetingService = new MeetingService();
        loadingIndicator.setVisible(false);

        // Initially hide meeting container
        meetingContainer.setVisible(false);
        startScreen.setVisible(true);
        copyLinkBtn.setDisable(true); // Disable until meeting starts

        // Configure WebView permissions if needed (JavaFX WebView has limited permission handling by default)
        // Note: For full camera/mic support, user permissions are handled by the OS/Browser context,
        // but JavaFX WebView might have limitations.
        // This implementation focuses on loading the URL.

        WebEngine engine = show.getEngine();
        engine.setJavaScriptEnabled(true);
        // Using a generic user agent can sometimes help bypass browser-specific blocking in Jitsi
        engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        // Add listener for loading state
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadingIndicator.setVisible(false);
            } else if (newState == Worker.State.RUNNING) {
                loadingIndicator.setVisible(true);
            } else if (newState == Worker.State.FAILED) {
                 loadingIndicator.setVisible(false);
                 // Optionally show error
            }
        });
    }

    @FXML
    public void handleStartMeeting() {
        startScreen.setVisible(false);
        meetingContainer.setVisible(true);
        loadingIndicator.setVisible(false); // No loading needed for external launch
        copyLinkBtn.setDisable(false);

        currentMeetingUrl = meetingService.generateMeetingUrl();

        // Show message in WebView instead of loading the meeting directly
        WebEngine engine = show.getEngine();
        engine.loadContent(
            "<html><body style='font-family: sans-serif; text-align: center; padding-top: 50px; color: #555;'>" +
            "<h2>Meeting Launched</h2>" +
            "<p>The meeting has been opened in a secure application window.</p>" +
            "<p>If it didn't open, <a href='" + currentMeetingUrl + "'>click here</a>.</p>" +
            "</body></html>"
        );

        launchMeetingInAppMode(currentMeetingUrl);
    }
    
    public void loadMeeting(String url) {
        // Method to allow joining existing meeting (if needed for non-admins)
        startScreen.setVisible(false);
        meetingContainer.setVisible(true);
        loadingIndicator.setVisible(false);
        copyLinkBtn.setDisable(false);

        currentMeetingUrl = url;
        // Ensure config is correct for app mode too, though Jitsi handles it
        if (!currentMeetingUrl.contains("#")) {
             currentMeetingUrl += "#config.prejoinPageEnabled=false";
        }

        WebEngine engine = show.getEngine();
         engine.loadContent(
            "<html><body style='font-family: sans-serif; text-align: center; padding-top: 50px; color: #555;'>" +
            "<h2>Meeting Launched</h2>" +
            "<p>The meeting has been opened in a secure application window.</p>" +
            "<p>If it didn't open, <a href='" + currentMeetingUrl + "'>click here</a>.</p>" +
            "</body></html>"
        );

        launchMeetingInAppMode(currentMeetingUrl);
    }

    private void launchMeetingInAppMode(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Try launching Edge in App Mode (Standard on Windows 10/11)
                // This creates a window without address bar/tabs, looking like a native app
                new ProcessBuilder("powershell", "/c", "start msedge --app='" + url + "'").start();
            } else if (os.contains("mac")) {
                // Chrome on Mac app mode
                new ProcessBuilder("open", "-n", "-a", "Google Chrome", "--args", "--app=" + url).start();
            } else {
                // Linux/Other - Default browser
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            }
        } catch (Exception e) {
            // Fallback to standard browser open if app mode fails
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Could not launch meeting: " + ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    public void handleClose() {
        // Stop the meeting/webview
        show.getEngine().load(null);

        // Return to dashboard or close window
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();

        // Ideally, you would switch back to the main dashboard scene here
        // But since I don't have the main dashboard controller reference or FXML, closing is a safe default.
        // Or we could navigate back using a navigation helper if one existed.
    }

    @FXML
    public void handleCopyLink() {
        if (currentMeetingUrl != null && !currentMeetingUrl.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(currentMeetingUrl);
            clipboard.setContent(content);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Link Copied");
            alert.setHeaderText(null);
            alert.setContentText("Meeting link copied to clipboard!");
            alert.showAndWait();
        }
    }
}
