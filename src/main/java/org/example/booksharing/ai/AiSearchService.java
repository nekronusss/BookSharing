package org.example.booksharing.ai;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.dto.DtoMapper;
import org.example.booksharing.entities.Book;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AiSearchService {
    private final AiCatalog catalog;
    private final EmbeddingIndex index;
    private final AiProvider provider;
    private final AiProperties config;

    @Transactional(readOnly = true)
    public AiDtos.Results search(String query, String category) {
        List<Book> candidates = catalog.available().stream()
                .filter(b -> category == null || category.isBlank()
                        || category.equalsIgnoreCase(b.getCategory())).toList();
        if (candidates.isEmpty()) return new AiDtos.Results(List.of(), "keyword", "Подходящих книг в каталоге нет.");
        var vectors = index.current(candidates);
        // A known title supplies real context for "similar to ..." queries, including a busy source book.
        List<Book> references = query.toLowerCase(Locale.ROOT).contains("похож")
                ? catalog.publicEntries().stream().filter(b -> b.getTitle() != null && b.getTitle().length() >= 3
                    && query.toLowerCase(Locale.ROOT).contains(b.getTitle().toLowerCase(Locale.ROOT))).limit(2).toList()
                : List.of();
        String retrievalText = query + references.stream().map(b -> "\n" + BookText.of(b))
                .collect(java.util.stream.Collectors.joining());
        double[] queryVector = null;
        if (config.isEnabled() && !vectors.isEmpty()) {
            try { queryVector = provider.embed(retrievalText); } catch (AiUnavailableException ignored) {}
        }
        final double[] embedding = queryVector;
        var hits = candidates.stream().map(b -> {
            if (references.stream().anyMatch(r -> r.getId().equals(b.getId()))) return null;
            double lexical = BookText.lexical(query, BookText.of(b));
            double semantic = BookText.cosine(embedding, vectors.get(b.getId()));
            if (lexical == 0 && (embedding == null || semantic < config.getSimilarityThreshold())) return null;
            double score = embedding == null ? lexical : .7 * Math.max(0, semantic) + .3 * lexical;
            return hit(b, score, lexical > 0 ? "Есть совпадения с запросом в названии, описании или тегах."
                    : "Близко по смыслу к вашему запросу.");
        }).filter(Objects::nonNull).sorted(Comparator.comparingDouble(AiDtos.Hit::relevance).reversed())
                .limit(24).toList();
        return new AiDtos.Results(hits, embedding == null ? "keyword" : "hybrid", embedding == null
                ? "Семантический поиск недоступен или индекс ещё готовится. Показаны текстовые совпадения."
                : (vectors.size() < candidates.size() ? "Индекс обновляется: часть книг пока ищется только по тексту." : ""));
    }

    @Transactional(readOnly = true)
    public AiDtos.Results similar(long id) {
        Book source = catalog.publicBook(id);
        var candidates = catalog.available();
        var all = new ArrayList<>(candidates); all.add(source);
        var vectors = index.current(all);
        double[] vector = vectors.get(id);
        var hits = candidates.stream().filter(b -> !b.getId().equals(id)).map(b -> {
            double lexical = BookText.lexical(BookText.of(source), BookText.of(b));
            boolean sameCategory = source.getCategory() != null && source.getCategory().equalsIgnoreCase(b.getCategory());
            double semantic = BookText.cosine(vector, vectors.get(b.getId()));
            if (!sameCategory && lexical < .15 && semantic < config.getSimilarityThreshold()) return null;
            return hit(b, .65 * Math.max(0, semantic) + .2 * lexical + (sameCategory ? .15 : 0),
                    sameCategory ? "Тот же жанр: " + source.getCategory() + "."
                            : semantic >= config.getSimilarityThreshold()
                            ? "Близкое по смыслу описание." : "Общие слова в описании и тегах.");
        }).filter(Objects::nonNull).sorted(Comparator.comparingDouble(AiDtos.Hit::relevance).reversed()).limit(4).toList();
        return new AiDtos.Results(hits, vector == null ? "content" : "hybrid",
                "Показаны публичные книги без активной передачи. Обмен нужно согласовать с владельцем.");
    }
    public static AiDtos.Hit hit(Book b, double score, String reason) {
        return new AiDtos.Hit(DtoMapper.toBookDto(b), Math.round(score * 10000.0) / 10000.0, reason);
    }
}
