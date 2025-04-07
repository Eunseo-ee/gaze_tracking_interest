package capstone.project.gaze_tracking_interest.entity;

import jakarta.persistence.Entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

@Document(collection = "stores")
public class Store {

    @Id
    private String id;

    private String storeCode;

    private String storeName;
    private String businessNumber;
    private String passwordHash;
    private boolean isVerified;
}
