package org.example.booksharing.ai;

import org.example.booksharing.entities.Book;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

public final class BookText {
    private BookText() {}
    public static String of(Book b) {
        return "Название: " + safe(b.getTitle()) + "\nАвтор: " + safe(b.getAuthor())
                + "\nЖанр: " + safe(b.getCategory()) + "\nТеги: "
                + String.join(", ", b.getTags() == null ? List.of() : b.getTags())
                + "\nОписание: " + safe(b.getDescription());
    }
    public static String safe(String s) { return s == null ? "" : s; }
    public static String hash(String s) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public static Set<String> tokens(String text) {
        return Arrays.stream(safe(text).toLowerCase(Locale.ROOT).replace('ё', 'е').split("[^\\p{L}\\p{N}]+"))
                .filter(s -> s.length() > 2)
                .filter(s -> !Set.of("хочу", "книгу", "книга", "почитать", "что", "мне", "для", "про",
                        "найди", "что-нибудь", "или", "как", "это", "the", "and", "with",
                        "название", "автор", "жанр", "теги", "описание").contains(s))
                .collect(Collectors.toSet());
    }
    public static double lexical(String query, String text) {
        var terms = tokens(query);
        var words = tokens(text);
        if (terms.isEmpty()) return 0;
        long matches = terms.stream().filter(t -> words.stream().anyMatch(w ->
                w.equals(t) || (t.length() >= 5 && w.startsWith(t.substring(0, t.length() - 2))))).count();
        return (double) matches / terms.size();
    }
    public static double cosine(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length || a.length == 0) return 0;
        double dot = 0, aa = 0, bb = 0;
        for (int i = 0; i < a.length; i++) {
            if (!Double.isFinite(a[i]) || !Double.isFinite(b[i])) return 0;
            dot += a[i] * b[i]; aa += a[i] * a[i]; bb += b[i] * b[i];
        }
        return aa == 0 || bb == 0 ? 0 : Math.max(-1, Math.min(1, dot / Math.sqrt(aa * bb)));
    }
}
