package digit.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import digit.repository.CollectionRepository;
import digit.repository.CollectionSearchCriteria;
import digit.web.models.collection.EntryFee;
import digit.web.models.collection.ParkingFee;
import digit.web.models.collection.SlaughterFee;
import digit.web.models.collection.StableFee;
import digit.web.models.collection.WashFee;


@Service
public class CollectionService {

@Autowired
    private CollectionRepository commonRepository;
    public List<EntryFee> getEntryFee(RequestInfo requestInfo,CollectionSearchCriteria criteria){
        // Fetch applications from database according to the given search criteria
        List<EntryFee> common = commonRepository.getEntryFee(criteria);
        // If no applications are found matching the given criteria, return an empty list
        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;
    }
    public List<StableFee> getStableFee(RequestInfo requestInfo, CollectionSearchCriteria criteria) {
        // Fetch applications from database according to the given search criteria
        List<StableFee> common = commonRepository.getStableFee(criteria);
        // If no applications are found matching the given criteria, return an empty list
        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;
    }
    public List<ParkingFee> getParkingFee(RequestInfo requestInfo, CollectionSearchCriteria criteria) {
         // Fetch applications from database according to the given search criteria
         List<ParkingFee> common = commonRepository.getParkingFee(criteria);
         // If no applications are found matching the given criteria, return an empty list
         if (CollectionUtils.isEmpty(common))
             return new ArrayList<>();
         return common;
    }
    public List<WashFee> getWashingFee(RequestInfo requestInfo, CollectionSearchCriteria criteria) {
        // Fetch applications from database according to the given search criteria
        List<WashFee> common = commonRepository.getWashingFee(criteria);
        // If no applications are found matching the given criteria, return an empty list
        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;
    }
    public List<SlaughterFee> getSlaughterFee(RequestInfo requestInfo, CollectionSearchCriteria criteria) {
        // Fetch applications from database according to the given search criteria
        List<SlaughterFee> common = commonRepository.getSlaughterFee(criteria);
        // If no applications are found matching the given criteria, return an empty list
        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;
    }
}
