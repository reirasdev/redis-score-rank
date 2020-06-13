package com.reiras.redisscorerank.repository.sorter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.reiras.redisscorerank.domain.User;
import com.reiras.redisscorerank.repository.sorter.Sorter;

@Component("PIPA_RANK_SORTER")
public class PipaRankSorter implements Sorter<User> {

	/*
	 * TreeMap structure keeps our list of users ordered by SCORE once SCORE is used as the key
	 * Taking advantage of the fact it returns the ranking ordered by SCORE DESC, this method reads the Set and saves each element on a list respecting the same position.
	 * In this same process, the property <position> is filled for each user. 
	 * The algorithm considers that same score should be represented by the same position on the ranking. 
	 */
	@Override
	public List<User> sort(Iterable<?> it) {
		
		Set<Map.Entry<Integer, List<Integer>>> mapEntries = (Set<Map.Entry<Integer, List<Integer>>>) it;
		List<User> usersList = new ArrayList<User>();
		int position = 1; // Indices starts from 0 but we are going to start with 1 to make it user friendly

		for (Map.Entry<Integer, List<Integer>> entry : mapEntries) {

			/*
			 * We already have the Map ordered by Score DESC
			 * We just need to set position to each user
			 * When a tie happens the user are already grouped in the same score
			 * Lets iterate over the list of ids and create the User for each element of the list
			 */
			for (Integer id : entry.getValue()) {
				usersList.add(new User(id, entry.getKey(), position));
			}

			position++;
		}

		return usersList;
	}

}
