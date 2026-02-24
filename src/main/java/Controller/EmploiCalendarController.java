package Controller;

import Entites.EmploiDuTemps;
import Entites.RDV;
import Entites.Seance;
import Services.EmploiDuTempsServices;
import Services.RDVServices;
import Services.SeanceServices;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmploiCalendarController {

    @FXML
    private StackPane calendarContainer;

    private final EmploiDuTempsServices emploiServices = new EmploiDuTempsServices();
    private final RDVServices rdvServices = new RDVServices();
    private final SeanceServices seanceServices = new SeanceServices();

    @FXML
    public void initialize() {
        CalendarView calendarView = new CalendarView();

        // Configuration
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSearchField(false);

        // Define calendars
        Calendar rdvCalendar = new Calendar("Rendez-vous");
        rdvCalendar.setStyle(Calendar.Style.STYLE1); // Red/Pink

        Calendar seanceCalendar = new Calendar("Séances");
        seanceCalendar.setStyle(Calendar.Style.STYLE2); // Blue

        Calendar otherCalendar = new Calendar("Autre");
        otherCalendar.setStyle(Calendar.Style.STYLE3); // Green

        CalendarSource myCalendarSource = new CalendarSource("Mon Emploi");
        myCalendarSource.getCalendars().addAll(rdvCalendar, seanceCalendar, otherCalendar);

        calendarView.getCalendarSources().setAll(myCalendarSource);
        calendarView.setRequestedTime(LocalTime.now());

        // Load data in background
        Thread updateThread = new Thread(() -> {
            List<EmploiDuTemps> emplois = emploiServices.afficherEmplois();

            Platform.runLater(() -> {
                for (EmploiDuTemps emploi : emplois) {
                    Entry<String> entry = createEntry(emploi);
                    if (entry != null) {
                        if (emploi.getIdRdv() != null) {
                            rdvCalendar.addEntry(entry);
                        } else if (emploi.getIdSeance() != null) {
                            seanceCalendar.addEntry(entry);
                        } else {
                            otherCalendar.addEntry(entry);
                        }
                    }
                }
            });
        });
        updateThread.setDaemon(true);
        updateThread.start();

        calendarContainer.getChildren().add(calendarView);
    }

    private Entry<String> createEntry(EmploiDuTemps emploi) {
        LocalDate entryDate = getDateForDay(emploi.getJourSemaine());
        if (entryDate == null) return null;

        String title = "Activité";
        String details = "";

        if (emploi.getIdSeance() != null) {
            try {
                Seance s = seanceServices.getSeanceById(emploi.getIdSeance());
                if (s != null) {
                    title = "Séance: " + s.getTitreSeance();
                    details = s.getDescription(); // Using available field
                }
            } catch (Exception e) {
                title = "Séance #" + emploi.getIdSeance();
            }
        } else if (emploi.getIdRdv() != null) {
            try {
                RDV r = rdvServices.getRDVById(emploi.getIdRdv());
                if (r != null) {
                    title = "RDV: " + r.getTypeConsultation();
                    details = "Statut: " + r.getStatutRdv();
                }
            } catch (Exception e) {
                title = "RDV #" + emploi.getIdRdv();
            }
        }

        Entry<String> entry = new Entry<>(title);
        // Set times based on slots
        switch (emploi.getTrancheHoraire().toLowerCase()) {
            case "matin":
                entry.setInterval(entryDate, LocalTime.of(8, 0), entryDate, LocalTime.of(12, 0));
                break;
            case "apres_midi":
                entry.setInterval(entryDate, LocalTime.of(14, 0), entryDate, LocalTime.of(18, 0));
                break;
            case "soir":
                entry.setInterval(entryDate, LocalTime.of(18, 0), entryDate, LocalTime.of(20, 0));
                break;
            case "journee":
                entry.setInterval(entryDate, LocalTime.of(8, 0), entryDate, LocalTime.of(18, 0));
                break;
            default:
                // Default 1 hour slot
                entry.setInterval(entryDate, LocalTime.of(9, 0), entryDate, LocalTime.of(10, 0));
        }

        // Add details if needed, Entry supports user object or location etc.
        entry.setLocation(details); // Hijacking location for details display

        return entry;
    }

    private LocalDate getDateForDay(String dayName) {
        if (dayName == null) return null;

        DayOfWeek targetDay;
        switch (dayName.toLowerCase()) {
            case "lundi": targetDay = DayOfWeek.MONDAY; break;
            case "mardi": targetDay = DayOfWeek.TUESDAY; break;
            case "mercredi": targetDay = DayOfWeek.WEDNESDAY; break;
            case "jeudi": targetDay = DayOfWeek.THURSDAY; break;
            case "vendredi": targetDay = DayOfWeek.FRIDAY; break;
            case "samedi": targetDay = DayOfWeek.SATURDAY; break;
            case "dimanche": targetDay = DayOfWeek.SUNDAY; break;
            default: return null;
        }

        // Calculate date for THIS week's occurrence of that day
        // Assuming current week logic
        LocalDate today = LocalDate.now();
        return today.with(DayOfWeek.MONDAY).with(targetDay); // Start at Monday of current week, then adjust to target
        // Wait, with(DayOfWeek) adjusts to the day within the current week structure relative to ISO-8601
        // (Monday-Sunday). So today.with(DayOfWeek.FRIDAY) gives this Friday.
        // This works for a weekly view based on "current week".
    }
}
