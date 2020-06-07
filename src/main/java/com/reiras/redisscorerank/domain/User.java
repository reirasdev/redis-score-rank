package com.reiras.redisscorerank.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

	@JsonProperty("userId")
	private Integer id;
	private Integer score;
	private Integer position;

	public User() {

	}

	public User(Integer id, Integer score, Integer position) {
		this.id = id;
		this.score = score;
		this.position = position;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("User [id=");
		builder.append(id);
		builder.append(", score=");
		builder.append(score);
		builder.append(", position=");
		builder.append(position);
		builder.append("]");
		return builder.toString();
	}

}
