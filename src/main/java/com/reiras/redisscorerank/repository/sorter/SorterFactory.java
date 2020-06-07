package com.reiras.redisscorerank.repository.sorter;

public interface SorterFactory {

	public Sorter<?> getSorter(SorterType type);
}
