package org.egov.demand.producer;

import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Producer {

	@Autowired
	private CustomKafkaTemplate kafkaTemplate;
	
	public void push(String topic, Object value) {
		log.info("Publishing message to topic: " + topic + " with value: " + value);
		kafkaTemplate.send(topic, value);
		
	}
}
