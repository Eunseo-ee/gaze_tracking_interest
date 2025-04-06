package capstone.project.gaze_tracking_interest.repository;

import capstone.project.gaze_tracking_interest.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, String> {
    Optional<Store> findByStoreCode(String storeCode);
}