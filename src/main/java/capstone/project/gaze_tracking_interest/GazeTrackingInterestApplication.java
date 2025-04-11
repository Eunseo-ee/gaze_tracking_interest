package capstone.project.gaze_tracking_interest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "capstone.project.gaze_tracking_interest.repository")
public class GazeTrackingInterestApplication {

	public static void main(String[] args) {
		SpringApplication.run(GazeTrackingInterestApplication.class, args);
	}

}
