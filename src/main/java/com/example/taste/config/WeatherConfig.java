package com.example.taste.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

@Configuration
@RequiredArgsConstructor
public class WeatherConfig {

	private final ConnectionProvider sharedConnectionProvider;

	@Bean
	public WebClient weatherWebClient() {

		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
		factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

		TcpClient tcpClient = TcpClient.create(sharedConnectionProvider)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
			.doOnConnected(conn ->
				conn.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
					.addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS))
			);

		HttpClient httpClient = HttpClient.from(tcpClient)
			.responseTimeout(Duration.ofSeconds(10));

		return WebClient.builder()
			.uriBuilderFactory(factory)
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.build();
	}

}
