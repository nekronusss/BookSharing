package org.example.booksharing.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.booksharing.entities.Book;
import org.example.booksharing.repository.BookRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmbeddingIndex {
    private final BookEmbeddingRepository vectors;
    private final BookRepository books;
    private final AiProvider provider;
    private final AiProperties config;
    private final ObjectMapper json;
    private final PlatformTransactionManager transactions;
    private long cursor = 0;
    private record Snapshot(long id, String text, String hash) {}

    public Map<Long, double[]> current(List<Book> candidates) {
        var expected = new HashMap<Long, String>();
        candidates.forEach(b -> expected.put(b.getId(), BookText.hash(BookText.of(b))));
        Map<Long, double[]> result = new HashMap<>();
        for (var vector : vectors.findAllById(expected.keySet())) {
            if (!config.modelKey().equals(vector.getModelKey())
                    || !Objects.equals(expected.get(vector.getBookId()), vector.getContentHash())) continue;
            try { result.put(vector.getBookId(), json.readValue(vector.getVectorJson(), double[].class)); }
            catch (Exception ignored) { /* Corrupt entries are never used. */ }
        }
        return result;
    }

    @Scheduled(fixedDelayString = "${ai.index-delay-ms:30000}", initialDelayString = "${ai.index-initial-delay-ms:5000}")
    public void indexNextBatch() {
        if (!config.isEnabled()) return;
        var tx = new TransactionTemplate(transactions);
        try {
            List<Snapshot> batch = tx.execute(status -> books.findAll(AiCatalog.publicBooks()
                            .and((r,q,c) -> c.greaterThan(r.get("id"), cursor)),
                            PageRequest.of(0, config.getIndexBatchSize(), Sort.by("id"))).getContent().stream()
                    .map(b -> new Snapshot(b.getId(), BookText.of(b), BookText.hash(BookText.of(b)))).toList());
            if (batch == null || batch.isEmpty()) { cursor = 0; return; }
            for (var snapshot : batch) {
                var existing = vectors.findById(snapshot.id());
                if (existing.isEmpty() || !existing.get().getContentHash().equals(snapshot.hash())
                        || !existing.get().getModelKey().equals(config.modelKey())) {
                    // Network outside the database transaction; recheck content and privacy before storing.
                    String encoded;
                    try { encoded = json.writeValueAsString(provider.embed(snapshot.text())); }
                    catch (AiUnavailableException e) {
                        // A rejected document must not starve every later book. Retry it on the next full pass.
                        cursor = snapshot.id();
                        return;
                    }
                    tx.executeWithoutResult(status -> books.findById(snapshot.id()).ifPresent(book -> {
                        if (!Boolean.TRUE.equals(book.getPublic()) || book.getUser() == null
                                || book.getUser().isPrivateBooks()
                                || !snapshot.hash().equals(BookText.hash(BookText.of(book)))) return;
                        BookEmbedding vector = vectors.findById(snapshot.id()).orElseGet(BookEmbedding::new);
                        vector.setBookId(book.getId());
                        vector.setContentHash(snapshot.hash()); vector.setModelKey(config.modelKey());
                        vector.setVectorJson(encoded); vectors.save(vector);
                    }));
                }
                cursor = snapshot.id();
            }
        } catch (AiUnavailableException ignored) {
            // Keep the cursor for a bounded retry next tick, without disrupting CRUD.
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("AI index batch failed ({}); retry on next scheduled run",
                    e.getClass().getSimpleName());
        }
    }
}
