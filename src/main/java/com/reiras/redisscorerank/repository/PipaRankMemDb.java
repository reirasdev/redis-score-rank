package com.reiras.redisscorerank.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.reiras.redisscorerank.domain.User;
import com.reiras.redisscorerank.repository.sorter.SorterFactory;
import com.reiras.redisscorerank.repository.sorter.SorterType;

@Component
public class PipaRankMemDb {

	/*
	 * This Map will allow us to fast find, retrieve and save the user.
	 * Key ==> userId
	 * Value ==> userScore
	 */
	private static final Map<Integer, Integer> USERS = new ConcurrentHashMap<>();

	/*
	 * This map we need for ranking and it must be updated for every write
	 * transactions on the USERS Map
	 * Key ==> userScore
	 * Value ==> List<userId>()
	 */
	private static final Map<Integer, List<Integer>> RANKING = new TreeMap<Integer, List<Integer>>(
			Collections.reverseOrder());

	// This seems to be the best way to keep our transactions thread safe once we
	// can have different locks for write and read transactions
	private ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

	@Autowired
	private SorterFactory sorterFactory;

	public void incrementScore(Integer id, Integer score) {
		if (id == null || id <= 0)
			return;
		if (score == null || score <= 0)
			return;

		// locking the next steps to avoid data integrity loss
		reentrantReadWriteLock.writeLock().lock();
		try {
			// We will need the oldScore to update the ranking
			Integer oldScore = USERS.containsKey(id) ? USERS.get(id) : 0;
			Integer newScore = oldScore + score;

			// Create/Update it in our Map
			USERS.put(id, newScore);

			/*
			 * oldScore > 0 means we have it at our USERS Map so, for sure the oldScore
			 * exists on the RANKING Map
			 */
			if (oldScore > 0) {
				if (RANKING.get(oldScore).size() == 1) {
					/*
					 * Now, we need to know if there are other users with the same score before
					 * handling that. Size == 1 means that it was the only user with that score, after
					 * incrementing the score, no one will be related to the old score. Lets remove
					 * it from the RANKING
					 */
					RANKING.remove(oldScore);

				} else {
					/*
					 * As we saw above, for empty lists the score is removed from the RANKING. So,
					 * size != 1 means that we have more than one user with the same score Lets
					 * remove our current user from that list once its score has changed
					 */
					RANKING.get(oldScore).remove(id);
				}
			}

			/*
			 * Now, we need to apply the new score to the RANKING. This part must be
			 * commonly done for both situations, new and already existing users.
			 */
			if (RANKING.containsKey(newScore)) {
				// The new score already exists in the RANKING, lets add it to the existent list
				RANKING.get(newScore).add(id);

			} else {
				// The new score is not associated to any user yet. Lets add it to the RANKING
				List<Integer> idsList = new ArrayList<Integer>();
				idsList.add(id);
				RANKING.put(newScore, idsList);
			}

		} finally {
			reentrantReadWriteLock.writeLock().unlock();
		}

	}

	public User getById(Integer id) {
		if (id == null || id <= 0)
			return null;

		Integer score;
		List<Integer> rankingList;

		/*
		 * We cannot take the risk of RANKING changes just after we get the score but
		 * before we get the RANKING. We need to guarantee that both operations will be
		 * performed in atomic transaction. Lets lock only while getting a safe
		 * copy of the values. This way we guarantee we have the last values for user's
		 * score at the moment of the request, even if it changes before finishing this
		 * method our variable will not be affected. This way also offers better
		 * performance once it does not block the structure for other read transactions.
		 */
		reentrantReadWriteLock.readLock().lock();
		try {
			score = USERS.get(id);

			// If we don't find the user, just return null
			if (score == null)
				return null;

			rankingList = new ArrayList<Integer>(RANKING.keySet());

		} finally {
			reentrantReadWriteLock.readLock().unlock();
		}

		/*
		 * Now we have all the data we need We just have to update user's position based
		 * on the RANKING list that is already ordered DESC. Once Lists indices starts
		 * from 0, to make our RANKING user friendly, we add 1 to the indexOf result
		 */
		int position = rankingList.indexOf(score) + 1;

		return new User(id, score, position);
	}

	public List<User> getHighScoreList(int startIndex, int stopIndex) {
		//for subList method from List, the lastIndex is exclusive. Adding +1 to include it.
		stopIndex++;
		
		if (startIndex < 0 || startIndex >= stopIndex)
			return new ArrayList<>();

		List<User> highScoreList = null;

		/*
		 * Locking to prevent data from changing before we finish creating the list.
		 * Read transactions are not locked.
		 */
		reentrantReadWriteLock.readLock().lock();
		try {
			highScoreList = (List<User>) sorterFactory.getSorter(SorterType.PIPA_RANK_SORTER).sort(RANKING.entrySet());

		} finally {
			reentrantReadWriteLock.readLock().unlock();
		}

		int listSize = highScoreList.size();
		// validation to prevent IndexOutOfBoundsException
		if (startIndex >= listSize)
			startIndex = listSize;
		if (stopIndex >= listSize)
			stopIndex = listSize;
		if (startIndex == stopIndex)
			return new ArrayList<>();

		return highScoreList.subList(startIndex, stopIndex);
	}

}
