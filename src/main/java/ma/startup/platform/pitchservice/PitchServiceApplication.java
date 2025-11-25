package ma.startup.platform.pitchservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PitchServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PitchServiceApplication.class, args);
	}

}
