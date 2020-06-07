package com.reiras.redisscorerank.repository;

import java.util.List;
import java.util.Optional;

public interface ScoreRankRepository<T> {

	/*
	 * Providing a generic interface to make possible and easy to change "in memory" technology if necessary in future.
	 */
	public void incrementScore(Integer id, Integer score);

	public Optional<T> findById(Integer id);

	public List<T> findAllOrderByScoreDesc(Long startIndex, Long stopIndex);
}
