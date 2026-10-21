package org.example.booksharing.service;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.ai.*;
import org.example.booksharing.entities.*;
import org.example.booksharing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final AiCatalog catalog;
    private final EmbeddingIndex index;
    private final AiPreferencesService preferences;
    private final BookRepository books;
    private final FavoriteRepository favorites;
    private final RatingRepository ratings;
    private final ExchangeRepository exchanges;

    @Transactional(readOnly = true)
    public AiDtos.Results recommend(User user) {
        var prefs = preferences.get(user);
        var candidates = catalog.available();
        Map<Long, Book> publicBooks = new HashMap<>();
        books.findAll(AiCatalog.publicBooks()).forEach(b -> publicBooks.put(b.getId(), b));
        Set<Long> known = new HashSet<>();
        Map<Long, Book> seeds = new LinkedHashMap<>();
        if (prefs.useHistory()) {
            var liked = new ArrayList<>(books.findFavoritesByUsername(user.getUsername()));
            favorites.findByUserId(user.getId()).forEach(f -> liked.add(f.getBook()));
            for (Book b : liked) {
                known.add(b.getId());
                if (publicBooks.containsKey(b.getId())) seeds.put(b.getId(), publicBooks.get(b.getId()));
            }
            exchanges.findByUserId(user.getId()).stream()
                    .filter(e -> e.getStatus() == Exchange.ExchangeStatus.COMPLETED).forEach(e -> {
                        Book received = user.getId().equals(e.getRequester().getId()) ? e.getRequestedBook() : e.getOfferedBook();
                        if (received != null) {
                            known.add(received.getId());
                            if (publicBooks.containsKey(received.getId())) seeds.put(received.getId(), publicBooks.get(received.getId()));
                        }
                    });
            ratings.findByUserId(user.getId()).forEach(r -> {
                known.add(r.getBook().getId());
                if (r.getScore() != null && r.getScore() >= 4 && publicBooks.containsKey(r.getBook().getId()))
                    seeds.put(r.getBook().getId(), publicBooks.get(r.getBook().getId()));
                else if (r.getScore() != null && r.getScore() <= 2) seeds.remove(r.getBook().getId());
            });
        }
        var vectors = index.current(new ArrayList<>(publicBooks.values()));
        var hits = candidates.stream()
                .filter(b -> !user.getId().equals(b.getUser().getId()) && !known.contains(b.getId()))
                .map(b -> {
                    boolean genre = prefs.genres().stream().anyMatch(g -> g.equalsIgnoreCase(b.getCategory()));
                    double interest = BookText.lexical(prefs.interests(), BookText.of(b));
                    double content = seeds.values().stream().mapToDouble(s ->
                            .5 * BookText.lexical(BookText.of(s), BookText.of(b))
                                    + .5 * Math.max(0, BookText.cosine(vectors.get(s.getId()), vectors.get(b.getId()))))
                            .max().orElse(0);
                    double popularity = Math.min(1, Math.log1p(Math.max(0, b.getLikesCount() == null ? 0 : b.getLikesCount())) / 5);
                    double score = (genre ? .4 : 0) + .25 * interest + .3 * content + .05 * popularity;
                    String reason = genre ? "Вы выбрали жанр «" + b.getCategory() + "»."
                            : interest > 0 ? "Описание или теги совпадают с указанными вами интересами."
                            : content > .1 ? "Похожа по описанию или тематике на книги из вашего избранного, оценок и завершённых обменов."
                            : "Популярная книга для знакомства с каталогом. Укажите интересы для более точного подбора.";
                    return AiSearchService.hit(b, score, reason);
                }).sorted(Comparator.comparingDouble(AiDtos.Hit::relevance).reversed()).limit(4).toList();
        return new AiDtos.Results(hits, "personalized",
                "Подбор учитывает ваши настройки. Отсутствие активной передачи не гарантирует согласие владельца на обмен.");
    }
}
