package org.eclipse.slm.self_description_service.service.rest;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLException;
import java.util.concurrent.Executor;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {
        "org.eclipse.slm.self_description_service.datasource",
        "org.eclipse.slm.self_description_service.templating",
        "org.eclipse.slm.self_description_service.service",
        "org.eclipse.slm.self_description_service.common",
}
)
@EnableAsync
@EnableConfigurationProperties
public class SelfDescriptionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SelfDescriptionServiceApplication.class, args);
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("SpringAsync-");
        executor.initialize();
        return executor;
    }

    @Bean
    public WebClient.Builder webClientBuilder() throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        TcpClient tcpClient = TcpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        HttpClient httpClient = HttpClient.from(tcpClient);
        var webClientBuilder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));

        return webClientBuilder;
    }

}
