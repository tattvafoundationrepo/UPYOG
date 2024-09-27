package digit.service;

import digit.config.EBinderConfiguration;
import digit.kafka.Producer;
import digit.repository.PeEnquiryRepository;
import digit.web.models.CeList;
import digit.web.models.EmployeeData;
import digit.web.models.GetPeRequest;
import digit.web.models.PeEnquiryResponse;
import digit.web.models.peprocess.PeEnquiryRecord;
import digit.web.models.peprocess.ProcessApplicationInfo;
import digit.web.models.report.PeSubmissionReport;
import digit.web.models.request.CeRequest;
import digit.web.models.request.PeEnquiryRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PeEnquiryService {
    @Autowired
    private Producer producer;

    @Autowired
    private EBinderConfiguration configuration;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PeEnquiryRepository repository;
     
    Long time = System.currentTimeMillis();
    public void savePeDetails(PeEnquiryRequest request) {

        if (request == null || request.getPeEnquiry() == null) {
            throw new CustomException("Invalid request", "PeEnquiry can not be null or empty");

        }
     
        request.getPeEnquiry().setCreatedAt(time);
        request.getPeEnquiry().setUpdatedAt(time);
        request.getPeEnquiry().setUpdatedBy(request.getRequestInfo().getUserInfo().getUserName());
      
        for(EmployeeData ed : request.getEmpData()){
            ed.setCreatedAt(time);
            ed.setUpdatedAt(time);
            ed.setCreatedBy(request.getRequestInfo().getUserInfo().getUserName());
            ed.setUpdatedBy(request.getRequestInfo().getUserInfo().getUserName());
            ed.setEnqordertype("");
            if(ed.getCeSuspended()!= null){
                if (ed.getCeSuspended().getLabel() == 1)
                   ed.setIsSuspended(true);
                else
                  ed.setIsSuspended(false);
            }
           

        }
 
        producer.push("save-pe-enquiry", request);

    }

    public List<PeEnquiryResponse> getPeDetails(GetPeRequest request) {
        List<PeEnquiryResponse> per =  repository.getPeEnqData(request) ;
        return per;
    }



    public void  saveCeList(CeRequest request){
        if (request == null || request.getCeList() == null) {
            throw new CustomException("Invalid request", "CeRequest can not be null or empty");

        }
        if(!request.isRemove()){
            CeList ceList=request.getCeList();
            ceList.setCeStatus(true);
            request.setCeList(ceList);
            producer.push("save-eBinder-ceList", request);
        }

        else{
            CeList ceList=request.getCeList();
            ceList.setCeStatus(false);
            request.setCeList(ceList);
            producer.push("save-eBinder-ceList", request);
        }

    }
 

    public String savePeEnqRecords(ProcessApplicationInfo application,List<PeEnquiryRecord> records,RequestInfo info){
    for(PeEnquiryRecord record : records ){

        record.setActions(application.getAction());
        record.setPeEnquiryId(application.getBusinessId());
        record.setCreatedAt(time);
        record.setCreatedBy(info.getUserInfo().getId().toString());
        record.setUpdatedAt(time);
        record.setUpdatedBy(info.getUserInfo().getId().toString());
         Map<String, Object> message = new HashMap<>();
         message.put("record", record);
         producer.push("upsert-pe-enq-records", message);

    }
        return "";
    }

    public void savePeSubmission(PeSubmissionReport report,RequestInfo info){
      
        report.setCreatedAt(time);
        report.setCreatedBy(info.getUserInfo().getId());
        report.setUpdatedAt(time);
        report.setUpdatedBy(info.getUserInfo().getId());
        
        Map<String, Object> message = new HashMap<>();
        message.put("report", report);
        producer.push("save-pe-submission-report", message);
    }

}
