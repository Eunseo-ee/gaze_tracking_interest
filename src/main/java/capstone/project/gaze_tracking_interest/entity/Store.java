package capstone.project.gaze_tracking_interest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Store {

    @Id
    private String storeCode;

    private String storeName;
    private String businessNumber;
    private String passwordHash;
    private boolean isVerified;
}
