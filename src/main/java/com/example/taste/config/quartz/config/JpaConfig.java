// package com.example.taste.config.quartz.config;
//
// import java.util.HashMap;
// import java.util.Map;
//
// import javax.sql.DataSource;
//
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
// import org.springframework.orm.jpa.JpaTransactionManager;
// import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
// import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
// import org.springframework.transaction.PlatformTransactionManager;
// import org.springframework.transaction.annotation.EnableTransactionManagement;
//
// import jakarta.persistence.EntityManagerFactory;
//
// @Configuration
// @EnableTransactionManagement
// @EnableJpaRepositories(
// 	basePackages = "com.example.taste",
// 	entityManagerFactoryRef = "entityManagerFactory",
// 	transactionManagerRef = "transactionManager"
// )
// public class JpaConfig {
//
// 	@Bean
// 	@Primary
// 	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
// 		LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
// 		emf.setDataSource(dataSource);
// 		emf.setPackagesToScan("com.example.taste.domain");
// 		emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//
// 		Map<String, Object> props = new HashMap<>();
// 		props.put("hibernate.hbm2ddl.auto", "update");
// 		props.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
// 		props.put("hibernate.show_sql", true);
// 		emf.setJpaPropertyMap(props);
//
// 		return emf;
// 	}
//
// 	@Bean
// 	@Primary
// 	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
// 		return new JpaTransactionManager(emf);
// 	}
// }