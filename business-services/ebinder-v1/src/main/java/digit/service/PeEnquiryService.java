package digit.service;

import digit.config.EBinderConfiguration;
import digit.kafka.Producer;
import digit.repository.PeEnquiryRepository;
import digit.web.models.CeList;
import digit.web.models.EmployeeData;
import digit.web.models.GetPeRequest;
import digit.web.models.PeEnquiryResponse;
import digit.web.models.request.CeRequest;
import digit.web.models.request.PeEnquiryRequest;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.models.RequestInfoWrapper;
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

    public void savePeDetails(PeEnquiryRequest request) {

        if (request == null || request.getPeEnquiry() == null) {
            throw new CustomException("Invalid request", "PeEnquiry can not be null or empty");

        }
        Long time = System.currentTimeMillis();
        request.getPeEnquiry().setCreatedAt(time);
        request.getPeEnquiry().setUpdatedAt(time);
        request.getPeEnquiry().setUpdatedBy(request.getRequestInfo().getUserInfo().getUserName());
       // request.getPeEnquiry().getDepartment().setI18key(getDeptAndDesigCodeFromName(request.getPeEnquiry().getDepartment().getI18key()));
       // request.getPeEnquiry().getDesignation().setI18key(getDeptAndDesigCodeFromName(request.getPeEnquiry().getDesignation().getI18key()));

        for(EmployeeData ed : request.getEmpData()){
            // if(ed.getDepartment()!= null && ed.getDesignation()!=null){
            //     String dept = ed.getDepartment().getI18key();
            //     String desig = ed.getDesignation().getI18key();
            //     ed.getDepartment().setI18key(getDeptAndDesigCodeFromName(dept));
            //     ed.getDesignation().setI18key(getDeptAndDesigCodeFromName(desig));
            // }
            ed.setCasetype("");
            ed.setCestatus(false);
            ed.setCesuspended(false);
            ed.setCesuspensionorder("");
            ed.setCreatedAt(time);
            ed.setUpdatedAt(time);
            ed.setCreatedBy(request.getRequestInfo().getUserInfo().getUserName());
            ed.setUpdatedBy(request.getRequestInfo().getUserInfo().getUserName());
            ed.setEnqordertype("");

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


    public String getDeptAndDesigCodeFromName(String input) {
           
        String[] parts = input.split("_");
        String code = parts[parts.length - 1];
        return code;

    }

}
