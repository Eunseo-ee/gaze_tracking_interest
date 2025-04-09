package capstone.project.gaze_tracking_interest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("capstone.project.gaze_tracking_interest.repository")
public class GazeTrackingInterestApplication {

	public static void main(String[] args) {
		SpringApplication.run(GazeTrackingInterestApplication.class, args);
	}
	//연동 잘 됐는지 궁금해서 달아본 주석
}
