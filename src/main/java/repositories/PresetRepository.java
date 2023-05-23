package repositories;

import models.Preset;

import java.util.List;
import java.util.Optional;

public interface PresetRepository {
    Optional<Preset> findById(Long id);
    List<Preset> findAllByUserId(Long userId);
    Preset save(Preset preset);
}
