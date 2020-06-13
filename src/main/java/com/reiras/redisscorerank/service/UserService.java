package com.reiras.redisscorerank.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.reiras.redisscorerank.domain.User;
import com.reiras.redisscorerank.repository.ScoreRankRepository;

@Service
public class UserService {

	@Autowired
	@Qualifier("userPipaMemDbRepository")
	private ScoreRankRepository<User> scoreRankRepository;

	public void incrementScore(Integer id, Integer score) {
		scoreRankRepository.incrementScore(id, score);
	}

	public User findById(Integer id) {
		return scoreRankRepository.findById(id).orElse(new User());
	}

	public List<User> findAllOrderByScoreDesc(Long startIndex, Long stopIndex) {
		return scoreRankRepository.findAllOrderByScoreDesc(startIndex, stopIndex);
	}

}
