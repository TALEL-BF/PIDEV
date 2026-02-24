package Services;

import Entites.RDV;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RdvNotificationService {

    private final TextlinkSmsClient smsClient;
    private final Set<Integer> notifiedRdvIds = new HashSet<>();

    public RdvNotificationService() {
        this.smsClient = new TextlinkSmsClient();
    }

    public void checkAndSendNotifications(List<RDV> rdvs) {
        // Current time in Tunisia
        ZoneId tunisiaZone = ZoneId.of("Africa/Tunis");
        LocalDateTime now = LocalDateTime.now(tunisiaZone);

        for (RDV rdv : rdvs) {
            // We only care about scheduled appointments
            if (!"planifiee".equalsIgnoreCase(rdv.getStatutRdv()) && !"confirme".equalsIgnoreCase(rdv.getStatutRdv())) {
                continue;
            }

            LocalDateTime rdvTime = rdv.getDateHeureRdv();

            // Calculate time difference in minutes
            long minutesUntilRdv = ChronoUnit.MINUTES.between(now, rdvTime);

            // Check if RDV is within the next 15 minutes (and hasn't started yet or just started)
            // range: 0 <= minutes <= 15
            if (minutesUntilRdv >= 0 && minutesUntilRdv <= 15) {
                if (!notifiedRdvIds.contains(rdv.getIdRdv())) {
                    sendNotification(rdv, minutesUntilRdv);
                    notifiedRdvIds.add(rdv.getIdRdv());
                }
            }
        }
    }

    public void checkSingleRdv(RDV rdv) {
        ZoneId tunisiaZone = ZoneId.of("Africa/Tunis");
        LocalDateTime now = LocalDateTime.now(tunisiaZone);

        // We only care about scheduled appointments
        if (!"planifiee".equalsIgnoreCase(rdv.getStatutRdv()) && !"confirme".equalsIgnoreCase(rdv.getStatutRdv())) {
            return;
        }

        LocalDateTime rdvTime = rdv.getDateHeureRdv();
        long minutesUntilRdv = ChronoUnit.MINUTES.between(now, rdvTime);

        if (minutesUntilRdv >= 0 && minutesUntilRdv <= 15) {
            // Even if already in notified set (unlikely for new), we might want to force send if explicitly called
            // But for safety, check set.
            if (!notifiedRdvIds.contains(rdv.getIdRdv())) {
                sendNotification(rdv, minutesUntilRdv);
                notifiedRdvIds.add(rdv.getIdRdv());
            }
        }
    }

    private void sendNotification(RDV rdv, long minutesUntil) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String timeStr = rdv.getDateHeureRdv().format(formatter);

        String message = String.format("Rappel: Votre RDV de type %s commence dans %d minutes (%s).",
                rdv.getTypeConsultation(), minutesUntil, timeStr);

        System.out.println("Sending SMS Notification for RDV ID " + rdv.getIdRdv() + ": " + message);
        smsClient.sendSms(message);
    }
}
