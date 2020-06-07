package com.reiras.redisscorerank;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.reiras.redisscorerank.controller.ScoreRankController;
import com.reiras.redisscorerank.domain.User;
import com.reiras.redisscorerank.repository.ScoreRankRepository;
import com.reiras.redisscorerank.service.UserService;

@SpringBootTest
class RedisScoreRankApplicationTests {

	@Autowired
	private ScoreRankRepository<User> userRedisRepository;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ScoreRankController scoreRankController; 
	
	@Test
	void contextLoads() {
		assertNotNull(userRedisRepository);
		assertNotNull(userService);
		assertNotNull(scoreRankController);
	}

}
