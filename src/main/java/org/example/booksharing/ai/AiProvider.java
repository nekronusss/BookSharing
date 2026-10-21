package org.example.booksharing.ai;

public interface AiProvider {
    double[] embed(String text);
    String complete(String system, String input);
}
