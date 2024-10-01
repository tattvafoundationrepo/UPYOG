package digit.kafka;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import digit.service.NotificationService;
import digit.web.models.DeonarRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DeonarNotificationConsumer {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(topics = { "${deonar.kafka.arrival.topic}","${deonar.kafka.inspection.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        try {
            DeonarRequest request = mapper.convertValue(record, DeonarRequest.class);
            notificationService.process(request);

        } catch (Exception e) {

            log.error("Error while listening to value: " + record + " on topic: " + topic + ": ", e);
        }
    }

}

