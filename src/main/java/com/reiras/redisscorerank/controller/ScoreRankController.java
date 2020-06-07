package com.reiras.redisscorerank.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reiras.redisscorerank.domain.User;
import com.reiras.redisscorerank.dto.HighScoreDto;
import com.reiras.redisscorerank.dto.ScoreSaveDto;
import com.reiras.redisscorerank.service.UserService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/")
public class ScoreRankController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScoreRankController.class);

	@Autowired
	private UserService userService;

	@GetMapping(value = "/{userId}/position")
	@ApiResponses(value = { @ApiResponse(code = 500, message = "Internal Server Error") })
	@ApiOperation(value = "This endpoint returns the user's current score and its position in the ranking. If there is no user with the given id, empty value is returned")
	public ResponseEntity<User> getPosition(@PathVariable int userId) {

		Instant start = Instant.now();
		LOGGER.info(new StringBuffer("[getPosition:Start]")
				.append(" Input=>{userId=").append(userId).append("}").toString());
		
		User user = userService.findById(userId);
		
		LOGGER.info(new StringBuffer("[getPosition:End:").append(start.until(Instant.now(), ChronoUnit.MILLIS)).append("ms]")
				.append(" Output=>{").append(user).append("}").toString());
		
		return ResponseEntity.ok().body(user);
	}

	@GetMapping(value = "/highscorelist")
	@ApiResponses(value = { @ApiResponse(code = 500, message = "Internal Server Error") })
	@ApiOperation(value = "This endpoint returns the 20000 highest scores. If there is no score submitted, an empty list is returned")
	public ResponseEntity<HighScoreDto> getHighScores() {
		
		Instant start = Instant.now();
		LOGGER.info("[getHighScores:Start] Input=>{}");
		
		HighScoreDto dto = new HighScoreDto();
		dto.setHighScores(userService.findAllOrderByScoreDesc(0L, 19999L));
		
		LOGGER.info(new StringBuffer("[getHighScores:End:").append(start.until(Instant.now(), ChronoUnit.MILLIS)).append("ms]")
				.append(" Output=>{").append(dto.getHighScores().getClass())
				.append(":").append(dto.getHighScores().size()).append("items}").toString());
		
		return ResponseEntity.ok().body(dto);
	}

	@PostMapping(value = "/score")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Field validation error"), @ApiResponse(code = 500, message = "Internal Server Error") })
	@ApiOperation(value = "This endpoint increment the user's score. If there is no user with the given id, a new user is created with the given points as its score")
	public ResponseEntity<Void> saveScore(@Valid @RequestBody ScoreSaveDto scoreSaveDto) {

		Instant start = Instant.now();
		LOGGER.info(new StringBuffer("[saveScore:Start]")
				.append(" Input=>{scoreSaveDto=").append(scoreSaveDto).append("}").toString());
		
		userService.incrementScore(scoreSaveDto.getUserId(), scoreSaveDto.getPoints());
		
		LOGGER.info(new StringBuffer("[saveScore:End:").append(start.until(Instant.now(), ChronoUnit.MILLIS)).append("ms]")
				.append(" Output=>{}").toString());
		
		return ResponseEntity.ok().build();
	}
}
