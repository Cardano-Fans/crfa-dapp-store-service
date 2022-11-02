package crfa.app.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Singleton
@Slf4j
public class Json {

    @Inject
    private ObjectMapper objectMapper;

    public String write(Map<String, ?>  data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("json error", e);
            throw new RuntimeException(e);
        }
    }

    public Map<String, ?> read(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("json error", e);
            throw new RuntimeException(e);
        }
    }

}
