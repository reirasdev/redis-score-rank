package com.reiras.redisscorerank.repository.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisPoolConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(RedisPoolConfig.class);

	@Value("${redis.host:localhost}")
	private String redisHost;

	@Value("${redis.port:6379}")
	private int redisPort;

	@Bean
	public JedisPool jedisPool() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(10);
		poolConfig.setMaxIdle(5);
		poolConfig.setMinIdle(3);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
		poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
		poolConfig.setNumTestsPerEvictionRun(3);
		poolConfig.setBlockWhenExhausted(true);
		JedisPool jedisPool = new JedisPool(poolConfig, redisHost, redisPort);

		LOGGER.info("[jedisPool:Ready] Host [" + redisHost + "] Port [" + redisPort + "] " + poolConfig.toString());

		return jedisPool;
	}
}
