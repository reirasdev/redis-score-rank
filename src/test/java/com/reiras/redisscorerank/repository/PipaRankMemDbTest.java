package com.reiras.redisscorerank.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.reiras.redisscorerank.domain.User;

@SpringBootTest
public class PipaRankMemDbTest {

	@Autowired
	private PipaRankMemDb pipaRankMemDb;

	/*
	 * Testing simple increment. It will also serve as input for next tests.
	 */
	@Test
	@Order(1)
	public void incrementScore_shouldInsertRecords() {
		// Adding 30.000 records for tests in PipaRankMemDbTest
		int mockId = 30_000;
		for (int i = 1; i <= 30_000; i++) {
			pipaRankMemDb.incrementScore(mockId, i);
			mockId--;
		}

	}

	/*
	 * Lets check for the basics first. We know he have a simple set of users, from
	 * 1 to 30.000. Lets check if it has the correct sorting and also if each user
	 * has its correct position, id and score
	 */
	@Test
	@Order(2)
	public void getHighScoreList_shouldReturn20000SortedItems() throws Exception {
		// check if we have correct size for the list
		List<User> usersList = pipaRankMemDb.getHighScoreList(0, 19_999);
		assertEquals(20000, usersList.size());

		User user;
		int pos = 1;
		int score = 30_000;
		for (int i = 0; i < 20000; i++) {
			user = usersList.get(i);
			assertEquals(pos, user.getId());
			assertEquals(pos, user.getPosition());
			assertEquals(score, user.getScore());
			pos++;
			score--;
		}

	}

	/*
	 * Now, lets check for random position in the list and if position and score are
	 * correctly sorted
	 */
	@Test
	@Order(3)
	public void getById_shouldReturnCorrectPosition() throws Exception {
		Random random = new Random();
		int pos;
		User user;

		for (int i = 1; i <= 30_000; i++) {
			pos = random.nextInt(29_999) + 1; // Avoid 0
			user = pipaRankMemDb.getById(pos);

			assertEquals(pos, user.getId());
			assertEquals(pos, user.getPosition());
			// As we have a linear growth for the score, we can get that with a easy calc
			assertEquals(30_001 - pos, user.getScore());
		}

	}

	/*
	 * Lets check if ties are being sorted properly
	 */
	@Test
	@Order(4)
	public void getById_shouldReturnCorrectPositionForTies() throws Exception {
		Random random = new Random();
		int randomUserId;
		User tieUserBefore;
		User nextUserBefore;
		User tieUserAfter;
		User nextUserAfter;
		User newUser;

		// Performing 1.000 random verification for ties
		for (int i = 1; i <= 1_000; i++) {
			// We have 30.000 scores, lets set our range to 29_000 to avoid searches
			// out of the range and also to avoid getting a new user created in this loop
			randomUserId = random.nextInt(29_000) + 1; // Avoid 0
			tieUserBefore = pipaRankMemDb.getById(randomUserId);

			// lets get the user just after tieUser, this user position have to remain the
			// same after we insert a tie for the previous user
			nextUserBefore = pipaRankMemDb.getById(tieUserBefore.getId() + 1);

			// lets create a user with the same score
			pipaRankMemDb.incrementScore(30_000 + i, tieUserBefore.getScore());

			// lets retrieve all users again
			tieUserAfter = pipaRankMemDb.getById(randomUserId);
			nextUserAfter = pipaRankMemDb.getById(tieUserBefore.getId() + 1);
			newUser = pipaRankMemDb.getById(30_000 + i);

			// lets confirm we have a tie
			assertEquals(newUser.getScore(), tieUserAfter.getScore());
			assertEquals(newUser.getPosition(), tieUserAfter.getPosition());

			// position for tieUser must be the same from the beginning
			assertEquals(tieUserBefore.getPosition(), tieUserAfter.getPosition());

			// position for nextUser must remain the same
			assertEquals(nextUserBefore.getId(), nextUserAfter.getId());
			assertEquals(nextUserBefore.getPosition(), nextUserAfter.getPosition());

		}

	}

	/*
	 * This tests intend to guarantee that we are not getting any locks due
	 * synchronization, neither for write or read operations.
	 */
	@Test
	@Order(5)
	public void readConcurrency_concurrentThreadsIncremental_shouldKeepCoerence() throws Exception {
		// We will have 1000 threads incrementing the score concurrently
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		List<Callable<Boolean>> tasksList = new ArrayList<Callable<Boolean>>();

		Random random = new Random();
		Callable<Boolean> incr = () -> {
			pipaRankMemDb.incrementScore(random.nextInt(29_999) + 1, 1);
			return true;
		};

		/*
		 * Intention here is to show that we can keep read operation and data integrity
		 * even with write operations to the same users running concurrently
		 */
		Callable<Boolean> readById = () -> {
			int id = random.nextInt(29_999) + 1;
			User user = pipaRankMemDb.getById(id);
			return user.getId() == id;
		};

		Callable<Boolean> readHighScore = () -> {
			List<User> highScoresList = pipaRankMemDb.getHighScoreList(0, 19_999);
			return highScoresList.size() == 20_000;
		};

		// Each task, for each operation, will be repeated 10 times
		for (int i = 0; i < 100; i++) {
			tasksList.add(incr);
			tasksList.add(readById);
			tasksList.add(readHighScore);
		}

		List<Future<Boolean>> futuresList = executorService.invokeAll(tasksList);
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.MINUTES);

		// check if all conditions were reached in each Thread
		for (Future<Boolean> future : futuresList) {
			try {
				assertTrue(future.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/*
	 * Intention here is to test concurrency by incrementing score for the same user
	 * by multiple threads at the same time. The final result must demonstrate that
	 * the user's score was consistently incremented. The tests are done for 3 users
	 * at the same time, in the end, all of them must have the right score to pass
	 * the test.
	 */
	@Test
	@Order(6)
	public void incrementScore_concurrentThreadsIncremental_shouldKeepCoerence() throws Exception {

		// We will have 1.000 threads incrementing the score concurrently
		ExecutorService executorService = Executors.newFixedThreadPool(1_000);
		List<Callable<Boolean>> tasksList = new ArrayList<Callable<Boolean>>();

		// Each user will be incremented by 1 for several concurrent threads
		Callable<Boolean> task1 = () -> {
			pipaRankMemDb.incrementScore(999999, 1);
			return true;
		};

		Callable<Boolean> task2 = () -> {
			pipaRankMemDb.incrementScore(999998, 1);
			return true;
		};

		Callable<Boolean> task3 = () -> {
			pipaRankMemDb.incrementScore(999997, 1);
			return true;
		};

		// Each task, for each user, will be repeated 31.000 times
		for (int i = 0; i < 31_000; i++) {
			tasksList.add(task1);
			tasksList.add(task2);
			tasksList.add(task3);
		}

		executorService.invokeAll(tasksList);
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.MINUTES);

		// Check identity of each one
		assertEquals(999999, pipaRankMemDb.getById(999999).getId());
		assertEquals(999998, pipaRankMemDb.getById(999998).getId());
		assertEquals(999997, pipaRankMemDb.getById(999997).getId());

		// Check if they have score equal to 31_000
		assertEquals(31_000, pipaRankMemDb.getById(999999).getScore());
		assertEquals(31_000, pipaRankMemDb.getById(999998).getScore());
		assertEquals(31_000, pipaRankMemDb.getById(999997).getScore());

		// Check if their position is equal to 1
		assertEquals(1, pipaRankMemDb.getById(999999).getPosition());
		assertEquals(1, pipaRankMemDb.getById(999998).getPosition());
		assertEquals(1, pipaRankMemDb.getById(999997).getPosition());

		/*
		 * The whole structure is affected by increment operation, so, lets also perform
		 * a quick check for high scores list
		 */
		List<User> usersList = pipaRankMemDb.getHighScoreList(0, 19_999);
		assertEquals(20000, usersList.size());

		/*
		 * Check users position in the high scores list. Top 3 users of our list must
		 * contains the users used in the test. We cannot guarantee the order of each
		 * one inside the top 3 sublist, thats why we test the ID for all of them.
		 */
		User user = usersList.get(0);
		assertTrue(user.getId() == 999999 || user.getId() == 999998 || user.getId() == 999997);
		assertEquals(31_000, user.getScore());
		assertEquals(1, user.getPosition());

		user = usersList.get(1);
		assertTrue(user.getId() == 999999 || user.getId() == 999998 || user.getId() == 999997);
		assertEquals(31_000, user.getScore());
		assertEquals(1, user.getPosition());

		user = usersList.get(2);
		assertTrue(user.getId() == 999999 || user.getId() == 999998 || user.getId() == 999997);
		assertEquals(31_000, user.getScore());
		assertEquals(1, user.getPosition());

		/*
		 * To make sure our sort the list is ok, the 4th element have to be different.
		 * It also have to be marked as 2nd position.
		 */
		user = usersList.get(3);
		assertFalse(user.getId() == 999999 || user.getId() == 999998 || user.getId() == 999997);
		assertTrue(user.getScore() < 31_000);
		assertEquals(2, user.getPosition());

	}

}
