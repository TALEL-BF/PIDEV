package Services;

import IServices.IMeetingService;
import java.util.UUID;

public class MeetingService implements IMeetingService {
    private static final String JITSI_BASE_URL = "https://meet.jit.si/";

    @Override
    public String generateMeetingUrl() {
        // Generates a unique room name using UUID
        String roomName = "Meeting-" + UUID.randomUUID().toString();

        // Add config parameters to disable prejoin page and optimize for embedded view
        // config.prejoinPageEnabled=false : Skips the "Asking to join" screen
        // interfaceConfig.TOOLBAR_BUTTONS : Simplifies the toolbar
        return JITSI_BASE_URL + roomName + "#config.prejoinPageEnabled=false&config.startWithAudioMuted=true&config.startWithVideoMuted=true";
    }
}

