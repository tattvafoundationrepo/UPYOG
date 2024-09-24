package digit.kafka;


import java.util.HashMap;

import digit.web.models.EBinderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import digit.service.NotificationService;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class EBinderNotificationConsumer {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(topics = {"${eBinder.kafka.enquiry.topic}","${eBinder.kafka.ceList.topic}"})
    public void listen(final HashMap<String, Object> record) {

    }



}

