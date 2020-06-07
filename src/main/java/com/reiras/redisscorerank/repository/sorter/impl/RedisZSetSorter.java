package com.reiras.redisscorerank.repository.sorter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.reiras.redisscorerank.domain.User;
import com.reiras.redisscorerank.repository.sorter.Sorter;

import redis.clients.jedis.Tuple;

@Component("REDIS_ZSET_USER")
public class RedisZSetSorter implements Sorter<User> {

	/*
	 * Redis ZSet structure sorts our list of players based on each score but does not return position of each member of the ranking.
	 * Taking advantage of the fact it returns the ranking ordered by SCORE DESC, this method reads the Set and saves each element on a list respecting the same position.
	 * In this same process, the property <position> is filled for each user. 
	 * The algorithm considers that same score should be represented by the same position on the ranking. 
	 */
	@Override
	public List<User> sort(Iterable<?> it) {
		Set<Tuple> zSet = (Set<Tuple>) it;
		List<User> usersList = new ArrayList<User>();
		int flowingPosition = 1; //ZSet indices starts from 0 but we are going to start with 1 to make it user friendly
		int tiePosition = 1; //ZSet indices starts from 0 but we are going to start with 1 to make it user friendly
		Integer userId = 0;
		Double currentScore = 0.0;
		Double lastScore = 0.0;
		
		for(Tuple zTuple : zSet) {
			userId = Integer.valueOf(zTuple.getElement());
			currentScore = zTuple.getScore();
			
			if(!currentScore.equals(lastScore)) {
				usersList.add(new User(userId, currentScore.intValue(), flowingPosition)); //we got a new score in the ranking, lets set a new position to it
				tiePosition = flowingPosition; //we got a new score, it is not a tie, lets update tiePosition so it will be ready when next tie happens
				
			}else {
				usersList.add(new User(userId, currentScore.intValue(), tiePosition)); //we got a tie, lets set the same position of the last member
			}
			
			lastScore = currentScore; //always save the last score to check for a tie with next value
			flowingPosition++; //flowingPosition marks position on the ranking not considering a tie, so it always should be incremented
		}
		
		return usersList;
	}

}
