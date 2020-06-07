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

	private static final String ZSET_KEY = "piparank";

	private static final Long ZSET_FIRST_INDEX = 0L;

	private static final Long ZSET_LAST_INDEX = -1L;

	@Override
	public void incrementScore(Integer id, Integer score) {

		try (Jedis jedis = jedisPool.getResource();) {
			jedis.zincrby(ZSET_KEY, score.doubleValue(), id.toString());

		} catch (Exception e) {
			throw new RepositoryException("Error handling operation [incrementScore]", e);

		}

	}

	@Override
	public Optional<User> findById(Integer id) {
		Double score = null;
		Long position = null;

		try (Jedis jedis = jedisPool.getResource();) {
			score = jedis.zscore(ZSET_KEY, id.toString());

			if (score == null)
				return Optional.of(new User());

			position = jedis.zcount(ZSET_KEY, "(" + score.intValue(), "+inf");
			position++; // ZSet indices starts from 0, although, we are considering our ranking starts
						// from 1 to make it user friendly, so lets add 1 to the position

		} catch (Exception e) {
			throw new RepositoryException("Error handling operation [findById]", e);

		}

		return Optional.of(new User(id, score.intValue(), position.intValue()));
	}

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
