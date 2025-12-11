package ma.startup.platform.pitchservice.exception;

/**
 * Exception personnalisée pour les erreurs liées aux services d'IA
 */
public class AIException extends RuntimeException {

    public AIException(String message) {
        super(message);
    }

    public AIException(String message, Throwable cause) {
        super(message, cause);
    }
}