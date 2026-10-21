package org.example.booksharing.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.booksharing.entities.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AiPreferencesService {
    private final AiPreferenceRepository preferences;
    private final ObjectMapper json;
    @Transactional(readOnly = true)
    public AiDtos.Preferences get(User user) {
        return preferences.findById(user.getId()).map(p -> {
            try {
                return new AiDtos.Preferences(Arrays.asList(json.readValue(p.getGenresJson(), String[].class)),
                        p.getInterests(), p.isUseHistory());
            } catch (Exception e) { throw new IllegalStateException("Invalid stored preferences"); }
        }).orElse(new AiDtos.Preferences(List.of(), "", false));
    }
    @Transactional
    public AiDtos.Preferences save(User user, AiDtos.Preferences input) {
        var p = preferences.findById(user.getId()).orElseGet(AiPreference::new);
        p.setUserId(user.getId());
        try { p.setGenresJson(json.writeValueAsString(input.genres().stream().map(String::trim).distinct().toList())); }
        catch (Exception e) { throw new IllegalArgumentException("Invalid genres"); }
        p.setInterests(input.interests().trim()); p.setUseHistory(input.useHistory());
        preferences.save(p);
        return get(user);
    }
}
