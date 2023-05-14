//package org.babyfish.jimmer.sql.example.cfg;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.babyfish.jimmer.sql.JSqlClient;
//import org.babyfish.jimmer.sql.event.binlog.BinLog;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.stereotype.Component;
//
//// -----------------------------
//// If you are a beginner, please ignore this class,
//// for non-cache mode, this class will never be used.
//// -----------------------------
//@ConditionalOnProperty("spring.redis.host")
//@Component
//public class MaxwellListener {
//
//    private static final ObjectMapper MAPPER = new ObjectMapper()
//            .registerModule(new JavaTimeModule());
//
//    private final BinLog binLog;
//
//    public MaxwellListener(JSqlClient sqlClient) {
//        this.binLog = sqlClient.getBinLog();
//    }
//
//    @KafkaListener(topics = "maxwell")
//    public void onHandle(
//            String json,
//            Acknowledgment acknowledgment
//    ) throws JsonProcessingException {
//        JsonNode node = MAPPER.readTree(json);
//        String tableName = node.get("table").asText();
//        String type = node.get("type").asText();
//        JsonNode data = node.get("data");
//        switch (type) {
//            case "insert":
//                binLog.accept(tableName, null, data);
//                break;
//            case "update":
//                binLog.accept(tableName, node.get("old"), data);
//                break;
//            case "delete":
//                binLog.accept(tableName, data, null);
//                break;
//        }
//        acknowledgment.acknowledge();
//    }
//}
