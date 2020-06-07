package com.reiras.redisscorerank.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reiras.redisscorerank.domain.User;
import com.reiras.redisscorerank.repository.ScoreRankRepository;

@Service
public class UserService {

	@Autowired
	private ScoreRankRepository<User> userRedisRepository;

	public void incrementScore(Integer id, Integer score) {
		userRedisRepository.incrementScore(id, score);
	}

	public User findById(Integer id) {
		return userRedisRepository.findById(id).orElse(new User());
	}

	public List<User> findAllOrderByScoreDesc(Long startIndex, Long stopIndex) {
		return userRedisRepository.findAllOrderByScoreDesc(startIndex, stopIndex);
	}

}
