package capstone.project.gaze_tracking_interest.repository;

import capstone.project.gaze_tracking_interest.entity.Store;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StoreRepository extends MongoRepository<Store, String> {
    Optional<Store> findByStoreCode(String storeCode);
}
