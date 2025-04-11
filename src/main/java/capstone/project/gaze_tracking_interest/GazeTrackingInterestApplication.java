package capstone.project.gaze_tracking_interest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
//@EnableJpaRepositories("capstone.project.gaze_tracking_interest.repository")
@EnableMongoRepositories(basePackages = "capstone.project.gaze_tracking_interest.repository")
public class GazeTrackingInterestApplication {

	public static void main(String[] args) {
		SpringApplication.run(GazeTrackingInterestApplication.class, args);
	}

}
