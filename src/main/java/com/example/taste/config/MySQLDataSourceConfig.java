package com.example.taste.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManagerFactory;

/**
 * MySQL 전용 데이터소스 설정 클래스
 * - 다중 데이터소스 환경에서 MySQL을 기본(@Primary)으로 지정
 * - JPA, QueryDSL, 트랜잭션 매니저를 모두 구성
 */
@Configuration
@EnableTransactionManagement // 트랜잭션 관리 활성화
@EnableJpaRepositories(
	// MySQL사용하는 Repository
	basePackages = {
		"com.example.taste.domain.board.repository",
		"com.example.taste.domain.chat.repository",
		"com.example.taste.domain.comment.repository",
		"com.example.taste.domain.event.repository",
		"com.example.taste.domain.favor.repository",
		"com.example.taste.domain.image.repository",
		"com.example.taste.domain.match.repository",
		"com.example.taste.domain.notification.repository",
		"com.example.taste.domain.party.repository",
		"com.example.taste.domain.pk.repository",
		"com.example.taste.domain.review.repository",
		"com.example.taste.domain.store.repository",
		"com.example.taste.domain.user.repository"
	},
	entityManagerFactoryRef = "mysqlEntityManagerFactory",
	transactionManagerRef = "mysqlTransactionManager"
)
public class MySQLDataSourceConfig {
	// DataSource Bean 등록 (MySQL 연결 정보 사용)
	@Primary
	@Bean
	@ConfigurationProperties("spring.datasource")
	public DataSourceProperties mysqlDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Primary
	@Bean
	public DataSource mysqlDataSource() {
		return mysqlDataSourceProperties()
			.initializeDataSourceBuilder()
			.build();
	}

	// EntityManagerFactory Bean 등록 (JPA의 핵심 객체)
	@Primary
	@Bean(name = "mysqlEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
		EntityManagerFactoryBuilder builder) {
		return builder
			.dataSource(mysqlDataSource())
			.packages( // MySQL 전용 Entity 클래스 경로 지정
				"com.example.taste.domain.board.entity",
				"com.example.taste.domain.chat.entity",
				"com.example.taste.domain.comment.entity",
				"com.example.taste.domain.event.entity",
				"com.example.taste.domain.favor.entity",
				"com.example.taste.domain.image.entity",
				"com.example.taste.domain.match.entity",
				"com.example.taste.domain.notification.entity",
				"com.example.taste.domain.party.entity",
				"com.example.taste.domain.pk.entity",
				"com.example.taste.domain.review.entity",
				"com.example.taste.domain.store.entity",
				"com.example.taste.domain.user.entity"
			)
			.persistenceUnit("mysql")
			.build();
	}

	// 트랜잭션 매니저 Bean 등록
	@Primary
	@Bean
	public PlatformTransactionManager mysqlTransactionManager(
		@Qualifier("mysqlEntityManagerFactory") EntityManagerFactory emf) {
		return new JpaTransactionManager(emf);
	}

	// QueryDSL 사용 시 필요한 JPAQueryFactory Bean
	@Primary
	@Bean(name = "mysqlJpaQueryFactory")
	public JPAQueryFactory mysqlJpaQueryFactory(
		@Qualifier("mysqlEntityManagerFactory") EntityManagerFactory entityManagerFactory
	) {
		return new JPAQueryFactory(entityManagerFactory.createEntityManager());
	}

}
