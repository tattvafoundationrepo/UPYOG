package digit.service;

import digit.kafka.Producer;
import digit.web.models.request.PeSubmissionReportRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeSubmissionReportService {
    @Autowired
    private Producer producer;

    public void saveSubmissionReport(PeSubmissionReportRequest request) {
        if (request == null || request.getReport() == null || request.getOrderType() == null || request.getTypeCase() == null || request.getPeSubmissionReport() == null) {
            throw new CustomException("INVAID DATA","submission report data can not be null or empty");
        }
        producer.push("save-eBinder-report",request);
    }
}