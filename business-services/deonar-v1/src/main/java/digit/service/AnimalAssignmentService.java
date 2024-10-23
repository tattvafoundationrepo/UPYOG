package digit.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.kafka.Producer;
import digit.repository.StakeholderRepository;
import digit.util.IdgenUtil;
import digit.web.models.AnimalAssignment;
import digit.web.models.AnimalAssignmentRequest;
import digit.web.models.Assignments;
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
        List<Assignments> assignments = new ArrayList<>();
        setDateTime(request);
        List<Long> stakeHolderIdDistinctList = request.getAnimalAssignments().stream()
                .map(AnimalAssignment::getAssignedStakeholder).distinct().collect(Collectors.toList());
        List<String> ddReferences = idgenUtil.getIdList(request.getRequestInfo(),
                request.getRequestInfo().getUserInfo().getTenantId(), "deonar.ddrefid", "",
                stakeHolderIdDistinctList.size());

        IntStream.range(0, stakeHolderIdDistinctList.size())
                .forEach(i -> assignments.add(
                        new Assignments(stakeHolderIdDistinctList.get(i), ddReferences.get(i),
                                request.getArrivalId())));
        request.setAssignments(assignments);
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

    public List<ShopkeeperDetails> getListForSlaughter(ShopkeeperRequest request) {

        List<ShopkeeperDetails> details = stakeholderRepository.getSlaughterListDetails(request);
        return details;
    }

    public void setDateTime(AnimalAssignmentRequest request){
        Long time = System.currentTimeMillis();
        request.setCreatedAt(time);
        request.setCreatedBy(request.getRequestInfo().getUserInfo().getId());
        request.setUpdatedAt(time);
        request.setUpdatedBy(request.getRequestInfo().getUserInfo().getId()); 
    }

}
