package com.reiras.redisscorerank.repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.reiras.redisscorerank.domain.User;

@Component
public class UserPipaMemDbRepository implements ScoreRankRepository<User> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserPipaMemDbRepository.class);

	@Autowired
	private PipaRankMemDb pipaRankMemDb;

	@Override
	public void incrementScore(Integer id, Integer score) {
		
		Instant start = Instant.now();
		LOGGER.info(new StringBuffer("[incrementScore:Start]")
				.append(" Input=>{id=").append(id).append("}")
				.append("{score=").append(score).append("}").toString());

		pipaRankMemDb.incrementScore(id, score);
		
		LOGGER.info(new StringBuffer("[incrementScore:End:").append(start.until(Instant.now(), ChronoUnit.MILLIS)).append("ms]")
				.append(" Output=>{}").toString());
	}

	@Override
	public Optional<User> findById(Integer id) {
		
		Instant start = Instant.now();
		LOGGER.info(new StringBuffer("[findById:Start]")
				.append(" Input=>{id=").append(id).append("}").toString());
		
		User user = pipaRankMemDb.getById(id);
		
		LOGGER.info(new StringBuffer("[findById:End:").append(start.until(Instant.now(), ChronoUnit.MILLIS)).append("ms]")
				.append(" Output=>{user=").append(user).append("}").toString());

		return Optional.ofNullable(user);
	}

	@Override
	public List<User> findAllOrderByScoreDesc(Long startIndex, Long stopIndex) {
		
		Instant startInstant = Instant.now();
		LOGGER.info(new StringBuffer("[findAllOrderByScoreDesc:Start]")
				.append(" Input=>{startIndex=").append(startIndex).append("}")
				.append("{stopIndex=").append(stopIndex).append("}").toString());
		
		List<User> usersList = pipaRankMemDb.getHighScoreList(startIndex.intValue(), stopIndex.intValue());
		
		LOGGER.info(new StringBuffer("[findAllOrderByScoreDesc:End:").append(startInstant.until(Instant.now(), ChronoUnit.MILLIS)).append("ms]")
				.append(" Output=>{").append(usersList.getClass())
				.append(":").append(usersList.size()).append("items}").toString());

		return usersList;
	}

}
