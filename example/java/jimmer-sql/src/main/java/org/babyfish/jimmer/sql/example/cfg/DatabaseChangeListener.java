package org.babyfish.jimmer.sql.example.cfg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.event.binlog.BinLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

// -----------------------------
// If you are a beginner, please ignore this class,
// for non-cache mode, this class will never be used.
// -----------------------------
@ConditionalOnProperty("spring.redis.host")
@Component
public class DatabaseChangeListener {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final BinLog binLog;

    public DatabaseChangeListener(JSqlClient sqlClient) {
        this.binLog = sqlClient.getBinLog();
    }

    @KafkaListener(topicPattern = "mysql.big_data.*")
    public void onHandle(
            @Payload(required = false) String json,
            Acknowledgment acknowledgment
    ) throws JsonProcessingException {
        if(json == null){
            return;
        }
        JsonNode node = MAPPER.readTree(json);
        JsonNode payload = node.get("payload");
        System.out.println(payload);
        JsonNode source = payload.get("source");
        String tableName = source.get("table").asText();
        String op = payload.get("op").asText();
        JsonNode old = payload.get("before");
        JsonNode data = payload.get("after");
        switch (op) {
            case "c":
                binLog.accept(tableName, null, data);
                break;
            case "u":
                binLog.accept(tableName, old, data);
                break;
            case "d":
                binLog.accept(tableName, data, null);
                break;
        }
        acknowledgment.acknowledge();
    }
}
