package digit.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.egov.common.contract.models.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.kafka.Producer;
import digit.repository.StakeholderRepository;
import digit.util.IdgenUtil;
import digit.web.models.AnimalAssignment;
import digit.web.models.AnimalAssignmentRequest;
import digit.web.models.Assignments;
import digit.web.models.GetListRequest;
import digit.web.models.SlaughterList;
import digit.web.models.security.SecurityCheckDetails;
import digit.web.models.shopkeeper.ShopkeeperDetails;
import digit.web.models.shopkeeper.ShopkeeperRequest;

@Service
public class AnimalAssignmentService {

    @Autowired
    private Producer producer;

    @Autowired
    private IdgenUtil idgenUtil;

    @Autowired
    private StakeholderRepository stakeholderRepository;

    public void assignAnimalsToStakeholder(AnimalAssignmentRequest request) {
        if(request.getAnimalAssignments().get(0).getDeonarRemovalType() != null)
            saveAnimalRemoval(request);
        List<Assignments> assignments = new ArrayList<>();

        setDateTime(request);
        List<Long> stakeHolderIdDistinctList = request.getAnimalAssignments().stream()
                .map(AnimalAssignment::getAssignedStakeholder).distinct().collect(Collectors.toList());
        List<String> ddReferences = idgenUtil.getIdList(request.getRequestInfo(),
                request.getRequestInfo().getUserInfo().getTenantId(), "deonar.ddrefid", "",
                stakeHolderIdDistinctList.size());

        Map<Long, String> idDdMap = IntStream.range(0, stakeHolderIdDistinctList.size())
                .boxed()
                .collect(Collectors.toMap(stakeHolderIdDistinctList::get, ddReferences::get));

        IntStream.range(0, stakeHolderIdDistinctList.size())
                .forEach(i -> assignments.add(
                        new Assignments(stakeHolderIdDistinctList.get(i), ddReferences.get(i),
                                request.getArrivalId())));
        request.setAssignments(assignments);
        request.getAnimalAssignments().forEach(
                assignment -> assignment.setDdReference(idDdMap.get(assignment.getAssignedStakeholder())));
        producer.push("save-animal-assignment", request);

    }

    public void saveAnimalRemoval(AnimalAssignmentRequest request) {
        setDateTime(request);
        producer.push("save-animal-removal", request);

    }

    public List<ShopkeeperDetails> getShopkeepers(ShopkeeperRequest request) {

        List<ShopkeeperDetails> details = stakeholderRepository.getShopKeeperDetails(request);
        return details;
    }

    public List<SlaughterList> getListForSlaughter(ShopkeeperRequest request) {

        List<SlaughterList> details = stakeholderRepository.getSlaughterListDetails(request);
        return details;
    }

    public List<SecurityCheckDetails> getListForTrading(GetListRequest request) {
        List<SecurityCheckDetails> tradingList = stakeholderRepository.getTradingListDetails(request);
        return tradingList;
    }

    public List<SecurityCheckDetails> getListForStabling(GetListRequest request) {
        List<SecurityCheckDetails> stablingList = stakeholderRepository.getStablingListDetails(request);
        return stablingList;
    }


    public List<SecurityCheckDetails> getListForDawanwala(GetListRequest request) {
        List<SecurityCheckDetails> dawanwalaAssignmentList = stakeholderRepository.getListForDawanwalaAssingnmet(request);
        return dawanwalaAssignmentList;
    }

    public void setDateTime(AnimalAssignmentRequest request) {
        Long time = System.currentTimeMillis();
        request.setCreatedAt(time);
        request.setCreatedBy(request.getRequestInfo().getUserInfo().getId());
        request.setUpdatedAt(time);
        request.setUpdatedBy(request.getRequestInfo().getUserInfo().getId());
    }

    public List<SecurityCheckDetails> getListForHelkari(GetListRequest request) {
        List<SecurityCheckDetails> helkariAssignmentList = stakeholderRepository.getListForHelkariAssingnmet(request);
        return helkariAssignmentList;
    }

    public List<SecurityCheckDetails> getListOfRemovedAnimals(GetListRequest request) {
        List<SecurityCheckDetails> removalList = stakeholderRepository.getRemovalListDetails(request);
        return removalList;
    }

   


}
