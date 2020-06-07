package com.reiras.redisscorerank.repository.sorter;

public interface Sorter<T> {

	public Iterable<T> sort(Iterable<?> it);
}
