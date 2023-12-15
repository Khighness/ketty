package top.parak.ketty.exception;

/**
 * @author Khighness
 * @since 2023-12-14
 */
public class CancellationException extends RuntimeException {

    public CancellationException() {
    }

    public CancellationException(String message) {
        super(message);
    }

}
