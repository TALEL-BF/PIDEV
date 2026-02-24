package Services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class TextlinkSmsClient {

    private static final String API_KEY = "521qwbcegytFGGSIQOUk1Cln2J3OYD3PEgJ8pKV7jEvEX7gPFD10i1jUcGeEJ8yp";
    private static final String SENDER = "+21698136192";
    private static final String RECEIVER = "+21623590370";
    private static final String BASE_URL = "https://textlinksms.com/api/send-sms";

    private final HttpClient httpClient;

    public TextlinkSmsClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void sendSms(String messageText) {
        try {
            // Documentation specifies phone_number with + prefix, e.g., +11234567890
            // Ensure RECEIVER has the + prefix. If it's already there (it is in the constant), keep it.
            // If there are spaces, remove them.
            String cleanReceiver = RECEIVER.replace(" ", "");
            if (!cleanReceiver.startsWith("+")) {
                cleanReceiver = "+" + cleanReceiver;
            }

            // Construct JSON body according to documentation
            // Fields: phone_number, text
            // Note: We need to escape double quotes in the messageText if any.
            String escapedMessage = messageText.replace("\"", "\\\"");

            String jsonBody = String.format(
                "{\"phone_number\":\"%s\", \"text\":\"%s\"}",
                cleanReceiver, escapedMessage
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> System.out.println("SMS Response: " + response))
                    .join();

        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

