package com.reiras.redisscorerank.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.reiras.redisscorerank.domain.User;

public class HighScoreDto {

	@JsonProperty("highscores")
	private List<User> highScores;

	public List<User> getHighScores() {
		return highScores;
	}

	public void setHighScores(List<User> highScores) {
		this.highScores = highScores;
	}

}
