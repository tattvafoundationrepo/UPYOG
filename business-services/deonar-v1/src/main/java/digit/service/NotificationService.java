package digit.service;

import digit.web.models.DeonarRequest;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import digit.kafka.Producer;
import digit.web.models.SMSRequest;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class NotificationService {

    public void process(DeonarRequest request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
