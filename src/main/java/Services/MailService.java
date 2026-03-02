package Services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;

import java.io.File;
import java.util.Properties;

public class MailService {

    private final String username;
    private final String appPassword;

    public MailService(String username, String appPassword) {
        this.username = username;
        this.appPassword = appPassword;
    }

    private Session session() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        return Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });
    }

    public void sendWithAttachment(String to, String subject, String bodyText, File attachment) {
        if (to == null || to.isBlank()) throw new IllegalArgumentException("Email parent vide");
        if (attachment == null || !attachment.exists()) throw new IllegalArgumentException("PDF introuvable");

        try {
            Message message = new MimeMessage(session());
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Texte
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(bodyText);

            // Pièce jointe
            MimeBodyPart filePart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachment);
            filePart.setDataHandler(new DataHandler(source));
            filePart.setFileName(attachment.getName());

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(textPart);
            mp.addBodyPart(filePart);

            message.setContent(mp);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur envoi mail + PDF: " + e.getMessage(), e);
        }
    }
}