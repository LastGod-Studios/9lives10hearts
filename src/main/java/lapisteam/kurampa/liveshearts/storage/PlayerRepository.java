package lapisteam.kurampa.liveshearts.storage;

import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository {
    Optional<Integer> findHearts(UUID playerId);
    void saveHearts(UUID playerId, int hearts);
}
