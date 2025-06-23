package com.example.taste.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(
	basePackages = "com.example.taste.domain.embedding.repository", // PostgreSQL용 Repository 경로
	entityManagerFactoryRef = "postgresEntityManagerFactory",
	transactionManagerRef = "postgresTransactionManager"
)
public class PostgresDataSourceConfig {
	// PostgreSQL DataSource 설정
	@Bean(name = "postgresDataSource")
	@ConfigurationProperties("spring.datasource.postgresql") // 프로퍼티스에서 주입
	public DataSource postgresDataSource() {
		return DataSourceBuilder.create().build();
	}

	// PostgreSQL EntityManagerFactory 설정
	@Bean(name = "postgresEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean postgresEntityManagerFactory(
		EntityManagerFactoryBuilder builder,
		@Qualifier("postgresDataSource") DataSource dataSource) {

		return builder
			.dataSource(dataSource) // PostgreSQL용 데이터소스 주입
			.packages("com.example.taste.domain.embedding.entity") // PostgreSQL 전용 Entity 클래스 경로 지정
			.persistenceUnit("postgres") // persistence unit 이름 (JPA 내부 식별자)
			.properties(postgresJpaProperties()) // Hibernate 속성 직접 주입
			.build();
	}

	// PostgreSQL 전용 트랜잭션 매니저 설정
	@Bean(name = "postgresTransactionManager")
	public PlatformTransactionManager postgresTransactionManager(
		@Qualifier("postgresEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	// PostgreSQL용 JPAQueryFactory (QueryDSL)
	@Bean(name = "postgresJpaQueryFactory")
	public JPAQueryFactory postgresJpaQueryFactory(
		@Qualifier("postgresEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return new JPAQueryFactory(entityManagerFactory.createEntityManager());
	}

	//  PostgreSQL 전용 JPA 설정(프로퍼티스에는 mysql있어서 중복 설정됨, 따로 분리함)
	private Map<String, Object> postgresJpaProperties() {
		Map<String, Object> props = new HashMap<>();
		props.put("hibernate.hbm2ddl.auto", "none");
		props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		return props;
	}
}
