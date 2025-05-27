import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.self_description_service.common.consul.client.ConsulCredential;
import org.eclipse.slm.self_description_service.common.consul.client.apis.ConsulKeyValueApiClient;
import org.eclipse.slm.self_description_service.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.self_description_service.service.rest.SelfDescriptionServiceApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;

@SpringBootTest(classes = SelfDescriptionServiceApplication.class)
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
public class TestingResourceSelfDescriptionServiceTests {


    public static HashMap<String, Object> keycloakKV = new HashMap<>() {
        {
            put("realm", "fabos");
            put("auth-server-url", "http://localhost:7080/auth");
            put("ssl-required", "external");
            put("resource", "services");
            put("credentials", "{'secret': '7cFBKWWctQ8hIUDw4y2GK7scrhFFOKV9'}");
            put("confidential-port", 0);
        }
    };

    public static ComposeContainer environment = new ComposeContainer(
            new File("src/test/resources/docker-compose.yml")
    ).withExposedService("keycloak", 8080, Wait.forHttp("/").withStartupTimeout(Duration.ofMinutes(5)))
            .withExposedService("consul", 8500, Wait.forHttp("/").withStartupTimeout(Duration.ofMinutes(5)));


    @BeforeAll
    static void startContainers() throws ConsulLoginFailedException {
        environment.start();
        var objectMapper = new ObjectMapper();
        var consulKeyValueApiClient = new ConsulKeyValueApiClient("http", "localhost", 7500, "acl-test", "fabos", objectMapper);
        consulKeyValueApiClient.createKey(new ConsulCredential("acl-test"), "config/services", keycloakKV);
    }


    @AfterAll
    static void stopContainers() {
        environment.stop();
    }

    @Test
    void contextLoads() {
    }

}
