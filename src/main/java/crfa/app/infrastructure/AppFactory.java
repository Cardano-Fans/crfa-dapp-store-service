package crfa.app.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public class AppFactory {

    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

//    @Singleton
//    public RedissonClient redisClient() {
//        Config config = new Config();
//
//        config.useSingleServer().setAddress("redis://kajka.lan:6379");
//
//        return Redisson.create(config);
//    }

}
