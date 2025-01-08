package digit.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import digit.kafka.Producer;
import digit.repository.CollectionRepository;
import digit.repository.CollectionSearchCriteria;
import digit.util.IdgenUtil;
import digit.web.models.collection.FeeDetail;
import digit.web.models.collection.ParkingFee;
import digit.web.models.collection.RemovalFee;
import digit.web.models.collection.SlaughterFee;
import digit.web.models.collection.StableFee;
import digit.web.models.collection.WashFee;
import digit.web.models.collection.WeighingFee;

@Service
public class CollectionService {
    @Autowired
    private Producer producer;

    @Autowired
    private CollectionRepository commonRepository;

    @Autowired
    private IdgenUtil idgenUtil;

    private static List<Integer> collectionTypeList  = List.of(1,2,3);

    public List<StableFee> getEntryFee(RequestInfo requestInfo, CollectionSearchCriteria criteria) {
        // Fetch applications from database according to the given search criteria
        List<StableFee> common = commonRepository.getEntryFee(criteria);
        // If no applications are found matching the given criteria, return an empty
        // list
        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;
    }

    public List<StableFee> getStableFee(RequestInfo requestInfo, CollectionSearchCriteria criteria) {
        // Fetch applications from database according to the given search criteria
        List<StableFee> common = commonRepository.getStableFee(criteria);
        // If no applications are found matching the given criteria, return an empty
        // list
        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;
    }

    public List<RemovalFee> getRemovalFee(RequestInfo requestInfo, CollectionSearchCriteria criteria) {
        List<RemovalFee> common = commonRepository.getRemovalCollectionFee(criteria);

        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;
    }

    public List<ParkingFee> getParkingFee(RequestInfo requestInfo, CollectionSearchCriteria criteria) {
        // Fetch applications from database according to the given search criteria
        List<ParkingFee> common = commonRepository.getParkingFee(criteria);
        // If no applications are found matching the given criteria, return an empty
        // list
        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;
    }

    public List<WashFee> getWashingFee(RequestInfo requestInfo, CollectionSearchCriteria criteria) {
        // Fetch applications from database according to the given search criteria
        List<WashFee> common = commonRepository.getWashingFee(criteria);
        // If no applications are found matching the given criteria, return an empty
        // list
        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;
    }

    public List<StableFee> getSlaughterFee(RequestInfo requestInfo, CollectionSearchCriteria criteria) {
        // Fetch applications from database according to the given search criteria
        List<StableFee> common = commonRepository.getSlaughterFee(criteria);
        // If no applications are found matching the given criteria, return an empty
        // list
        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;
    }

    public FeeDetail saveFee(RequestInfo requestInfo, FeeDetail feedetail) {
        long epochTime = System.currentTimeMillis();
        List<String> recieptNo = idgenUtil.getIdList(requestInfo, requestInfo.getUserInfo().getTenantId(),
                "deonar.paymentid", "", 1);
        AuditDetails audit = AuditDetails.builder()
                .createdBy(requestInfo.getUserInfo().getUuid())
                .lastModifiedBy(requestInfo.getUserInfo().getUuid())
                .createdTime(epochTime)
                .lastModifiedTime(epochTime)
                .build();
        FeeDetail common = FeeDetail.builder()
                .uuid(feedetail.getUuid())
                .paidby(feedetail.getPaidby())
                .feetype(feedetail.getFeetype())
                .feevalue(feedetail.getFeevalue())
                .method(feedetail.getMethod())
                .recieptno(recieptNo.getFirst())
                .referenceno(feedetail.getReferenceno())
                .recieptno(recieptNo.get(0))
                .audit(audit)
                .build();
        producer.push("topic_deonar_savefee", common);
        CollectionSearchCriteria criteria = new CollectionSearchCriteria();

        String animalDetails = null;
        if (common.getFeevalue() > 0 && collectionTypeList.contains(common.getFeetype().intValue())) {
            if (feedetail.getLicenceNumber() != null) {
                common.setLicenceNumber(feedetail.getLicenceNumber());
                criteria.setLiceneceNumber(feedetail.getLicenceNumber());
            }
            if (feedetail.getMobileNumber() != null) {
                criteria.setMobileNumber(feedetail.getMobileNumber());
            }
            criteria.setSearch(feedetail.getUuid());
            common.setStakeholderId(feedetail.getStakeholderId());
            criteria.setCollectionType(common.getFeetype().intValue());
            animalDetails = commonRepository.getAnimalDetails(criteria);
            common.setAnimalDetail(animalDetails);
            producer.push("topic_deonar_savefeedetails", common);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(animalDetails);

            if (rootNode.isArray()) {
                rootNode.forEach(node -> {
                    int token = node.get("token").asInt();
                    int animalTypeId = node.get("animaltypeid").asInt();
                    commonRepository.saveEntryFee(String.valueOf(token), String.valueOf(animalTypeId),
                            common.getUuid());
                });
            }
        } catch (JsonProcessingException ex) {
            System.out.print("Exception");
        }
        if (common.getFeevalue() > 0 && common.getFeetype() == 7) {
            commonRepository.saveVehicleParking(System.currentTimeMillis(), common.getPaidby());
        }
        if (common.getFeevalue() > 0 && common.getFeetype() == 6) {
            commonRepository.saveVehicleWashing(System.currentTimeMillis(), common.getPaidby());
        }
        return common;
    }

    public List<StableFee> getTradingFee(RequestInfo requestInfo, CollectionSearchCriteria criteria) {

        List<StableFee> common = commonRepository.getTradingCollectionFee(criteria);

        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;

    }

    public List<WeighingFee> getWeighingFee(RequestInfo requestInfo, CollectionSearchCriteria criteria) {

        List<WeighingFee> common = commonRepository.getWeighingCollectionFee(criteria);

        if (CollectionUtils.isEmpty(common))
            return new ArrayList<>();
        return common;

    }

}
