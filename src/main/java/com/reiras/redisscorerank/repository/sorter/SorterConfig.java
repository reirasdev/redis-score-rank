package com.reiras.redisscorerank.repository.sorter;

import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SorterConfig {

	@Bean("sorterFactory")
	public ServiceLocatorFactoryBean serviceLocatorFactoryBean() {
		ServiceLocatorFactoryBean serviceLocatorFactoryBean = new ServiceLocatorFactoryBean();
		serviceLocatorFactoryBean.setServiceLocatorInterface(SorterFactory.class);
		return serviceLocatorFactoryBean;
	}
}
