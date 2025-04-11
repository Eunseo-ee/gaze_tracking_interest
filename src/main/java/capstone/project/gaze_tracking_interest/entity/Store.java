package capstone.project.gaze_tracking_interest.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter

@Document(collection = "stores")
public class Store {

    @Id
    private String id;

    @Field("store_code")
    private String storeCode;

    @Field("store_name")
    private String storeName;

    @Field("business_number")
    private String businessNumber;

    @Field("password_hash")
    private String passwordHash;

    @Field("is_verified")
    private boolean isVerified;
}
