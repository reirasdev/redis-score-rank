package com.reiras.redisscorerank.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ScoreSaveDto {

	@NotNull(message = "Field <userId> is mandatory") 
	@Min(value = 1, message = "Field <userId> must be greater than 0")
	private Integer userId;

	@NotNull(message = "Field <userId> is mandatory")
	@Min(value = 1, message = "Field <points> must be greater than 0")
	private Integer points;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScoreSaveDto [userId=");
		builder.append(userId);
		builder.append(", points=");
		builder.append(points);
		builder.append("]");
		return builder.toString();
	}

}
