package implementations.httpserver.handlers.exceptions;

public class BadRequestParameters extends Throwable {
    public BadRequestParameters(String message) {
        super(message);
    }
}
