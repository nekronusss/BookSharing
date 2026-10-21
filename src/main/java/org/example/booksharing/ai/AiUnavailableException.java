package org.example.booksharing.ai;

public class AiUnavailableException extends RuntimeException {
    public AiUnavailableException() { super("AI временно недоступен. Попробуйте позже."); }
}
