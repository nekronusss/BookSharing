package org.example.booksharing.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiResultVerifier {
    private final AiCatalog catalog;
    @Transactional(readOnly = true)
    public List<AiDtos.Hit> stillAvailable(List<AiDtos.Hit> hits) {
        var current = catalog.available().stream().collect(Collectors.toMap(b -> b.getId(), b -> b));
        return hits.stream().filter(h -> current.containsKey(h.book().getId()))
                // Metadata could change during generation. Discard a result rather than show stale evidence.
                .filter(h -> Objects.equals(current.get(h.book().getId()).getDescription(), h.book().getDescription())
                        && Objects.equals(current.get(h.book().getId()).getTitle(), h.book().getTitle())
                        && Objects.equals(current.get(h.book().getId()).getAuthor(), h.book().getAuthor()))
                .map(h -> AiSearchService.hit(current.get(h.book().getId()), h.relevance(), h.explanation())).toList();
    }
}
