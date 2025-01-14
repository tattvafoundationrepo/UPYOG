package digit.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import digit.repository.CommonRepository;
import digit.repository.CommonSearchCriteria;
import digit.web.models.Boundary;
import digit.web.models.common.BaundrySearchRequest;
import digit.web.models.common.CommonDetails;


@Service
public class CommonService {
    @Autowired
    private CommonRepository commonRepository;
    public List<CommonDetails> getcommon(RequestInfo requestInfo,CommonSearchCriteria searchcriteria){
        // Fetch applications from database according to the given search criteria
        List<CommonDetails> common = commonRepository.getCommonDetails(searchcriteria);
        // If no applications are found matching the given criteria, return an empty list
        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;
    }

    public List<Boundary> getboundary(BaundrySearchRequest requestInfo){
        
        List<Boundary> boundary = commonRepository.getboundary(requestInfo);
        if (CollectionUtils.isEmpty(boundary))
            return new ArrayList<>();
        return boundary;
    }

}
