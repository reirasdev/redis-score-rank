package com.reiras.redisscorerank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.reiras.redisscorerank.repository.PipaRankMemDb;

@SpringBootApplication
public class RedisScoreRankApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(RedisScoreRankApplication.class, args);
	}

	@Autowired
	private PipaRankMemDb pipaRankMemDb;

	@Value("${load.initialdata:false}")
	private String loadInitialData;

	private void loadInitialData() {
		// Adding 30.000 records in PipaRankMemDb
		int mockId = 30_000;
		for (int i = 1; i <= 30_000; i++) {
			pipaRankMemDb.incrementScore(mockId, i);
			mockId--;
		}

	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (Boolean.parseBoolean(loadInitialData)) {
			System.out.println("Loading initial data...'");
			this.loadInitialData();
			System.out.println("Done'");
		}
	}

}
