package com.reiras.redisscorerank.repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.reiras.redisscorerank.domain.User;
import com.reiras.redisscorerank.exception.RepositoryException;
import com.reiras.redisscorerank.repository.sorter.SorterFactory;
import com.reiras.redisscorerank.repository.sorter.SorterType;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

@Component
public class UserRedisRepository implements ScoreRankRepository<User> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserRedisRepository.class);

	@Autowired
	private JedisPool jedisPool;

	@Autowired
	private SorterFactory sorterFactory;

	/*
	 * Using Redis Sorted Set structure to save user scores.
	 * It is a reliable and fast solutions that fits project's requirements very well.
	 * Besides that, it makes the implementation easier once it is possible to retrieve all the scores ordered or get the position of a specific user from the set.
	 */
	private static final String ZSET_KEY = "piparank";

	private static final Long ZSET_FIRST_INDEX = 0L;

	private static final Long ZSET_LAST_INDEX = -1L;

	/*
	 * Redis offers method to increments a user score in a Sorted Set. 
	 * If the user does not exist, it is added with the points as its score.
	 * The operation above practically offers the requirement already implemented.
	 * As a bonus, ZINCRBY is an atomic operation and requires no need of external synchronization.
	 */	
	@Override
	public void incrementScore(Integer id, Integer score) {

		Instant start = Instant.now();
		LOGGER.info(new StringBuffer("[incrementScore:Start]")
				.append(" Input=>{id=").append(id).append("}")
				.append("{score=").append(score).append("}").toString());
		
		try (Jedis jedis = jedisPool.getResource();) {
			jedis.zincrby(ZSET_KEY, score.doubleValue(), id.toString());

		} catch (Exception e) {
			throw new RepositoryException("Error handling operation [incrementScore]", e);

		}
		
		LOGGER.info(new StringBuffer("[incrementScore:End:").append(start.until(Instant.now(), ChronoUnit.MILLIS)).append("ms]")
				.append(" Output=>{}").toString());

	}

	/*
	 * ZSet indices, for positioning, starts from 0.
	 * Although, we are considering our ranking to start from 1 to make it user friendly.
	 * Thats why we add 1 to the position returned from Redis.
	 */
	@Override
	public Optional<User> findById(Integer id) {
		
		Instant start = Instant.now();
		LOGGER.info(new StringBuffer("[findById:Start]")
				.append(" Input=>{id=").append(id).append("}").toString());
		
		Double score = null;
		Long position = null;		
		
		try (Jedis jedis = jedisPool.getResource();) {
			score = jedis.zscore(ZSET_KEY, id.toString());

			if (score == null)
				return Optional.of(new User());

			position = jedis.zcount(ZSET_KEY, "(" + score.intValue(), "+inf");
			position++; 

		} catch (Exception e) {
			throw new RepositoryException("Error handling operation [findById]", e);

		}
		
		User user = new User(id, score.intValue(), position.intValue());
		
		LOGGER.info(new StringBuffer("[findById:End:").append(start.until(Instant.now(), ChronoUnit.MILLIS)).append("ms]")
				.append(" Output=>{user=").append(user).append("}").toString());

		return Optional.of(user);
	}
	
	/*
	 * Redis offers a method to get all members of a sorted set ordered by the high scores.
	 * However, it returns only the score and the value, our userId, of Zset structure, positioning is not returned as a field.
	 * To fulfill the requirements, an extra sorting processing is necessary. More info on class RedisZSetSorter.
	 * The sorters were implemented using a Service Locator pattern, this way will be easier adding different algorithms for other "in memory" or persistence technologies.
	 */

	@Override
	public List<User> findAllOrderByScoreDesc(Long startIndex, Long stopIndex) {
		
		Instant startInstant = Instant.now();
		LOGGER.info(new StringBuffer("[findAllOrderByScoreDesc:Start]")
				.append(" Input=>{startIndex=").append(startIndex).append("}")
				.append("{stopIndex=").append(stopIndex).append("}").toString());
		
		List<User> usersList = null;
		Set<Tuple> zSet = null;
		Long start = ZSET_FIRST_INDEX;
		Long stop = ZSET_LAST_INDEX;

		if (startIndex != null)
			start = startIndex;

		if (stopIndex != null)
			stop = stopIndex;

		try (Jedis jedis = jedisPool.getResource();) {
			zSet = jedis.zrevrangeWithScores(ZSET_KEY, start, stop);

		} catch (Exception e) {
			throw new RepositoryException("Error handling operation [findAllOrderByPositionDesc]", e);

		}

		usersList = (List<User>) sorterFactory.getSorter(SorterType.REDIS_ZSET_USER).sort(zSet);
		
		LOGGER.info(new StringBuffer("[findAllOrderByScoreDesc:End:").append(startInstant.until(Instant.now(), ChronoUnit.MILLIS)).append("ms]")
				.append(" Output=>{").append(usersList.getClass())
				.append(":").append(usersList.size()).append("items}").toString());
		
		return usersList;
	}

}
