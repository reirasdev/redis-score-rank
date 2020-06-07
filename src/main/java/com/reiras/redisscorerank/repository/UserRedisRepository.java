package com.reiras.redisscorerank.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

		try (Jedis jedis = jedisPool.getResource();) {
			jedis.zincrby(ZSET_KEY, score.doubleValue(), id.toString());

		} catch (Exception e) {
			throw new RepositoryException("Error handling operation [incrementScore]", e);

		}

	}

	/*
	 * ZSet indices, for positioning, starts from 0.
	 * Although, we are considering our ranking to start from 1 to make it user friendly.
	 * Thats why we add 1 to the position returned from Redis.
	 */
	@Override
	public Optional<User> findById(Integer id) {
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

		return Optional.of(new User(id, score.intValue(), position.intValue()));
	}
	
	/*
	 * Redis offers a method to get all members of a sorted set ordered by the high scores.
	 * However, it returns only the score and the value, our userId, of Zset structure, positioning is not returned as a field.
	 * To fulfill the requirements, an extra sorting processing is necessary. More info on class RedisZSetSorter.
	 * The sorters were implemented using a Service Locator pattern, this way will be easier adding different algorithms for other "in memory" or persistence technologies.
	 */

	@Override
	public List<User> findAllOrderByScoreDesc(Long startIndex, Long stopIndex) {
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
		return usersList;
	}

}
