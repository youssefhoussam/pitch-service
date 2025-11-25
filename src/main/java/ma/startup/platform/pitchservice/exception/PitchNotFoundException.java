package ma.startup.platform.pitchservice.exception;

import java.util.UUID;

public class PitchNotFoundException extends RuntimeException {
    public PitchNotFoundException(String message) {
        super(message);
    }

    public PitchNotFoundException(UUID pitchId) {
        super("Pitch non trouvé avec l'ID: " + pitchId);
    }
}
