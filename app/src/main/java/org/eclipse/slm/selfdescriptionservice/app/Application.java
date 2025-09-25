package org.eclipse.slm.selfdescriptionservice.app;

import org.eclipse.slm.common.aas.clients.AasRepositoryClientFactory;
import org.eclipse.slm.common.aas.clients.ConceptDescriptionRepositoryClient;
import org.eclipse.slm.common.aas.clients.SubmodelRegistryClientFactory;
import org.eclipse.slm.common.aas.clients.SubmodelRepositoryClientFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {
        "org.eclipse.slm.selfdescriptionservice",
        "org.eclipse.slm.common.aas.clients",
    },
    excludeFilters = @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {
                    ConceptDescriptionRepositoryClient.class,
                    AasRepositoryClientFactory.class,
                    SubmodelRepositoryClientFactory.class
            }
    )
)
@EnableAsync
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
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

}
