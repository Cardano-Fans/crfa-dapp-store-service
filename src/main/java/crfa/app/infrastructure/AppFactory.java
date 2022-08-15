package crfa.app.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

@Factory
public class AppFactory {

    @Value("${redis-host:localhost}")
    private String redisHost;

    @Value("${redis-port:6379}")
    private int redisPort;

    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Singleton
    public RedissonClient redisClient() {
        Config config = new Config();

        config.useSingleServer()
                .setAddress(String.format("redis://%s:%d", redisHost, redisPort));

        return Redisson.create(config);
    }

}
