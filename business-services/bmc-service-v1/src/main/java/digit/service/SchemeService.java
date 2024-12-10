package digit.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.egov.common.contract.request.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import digit.repository.SchemeSearchCriteria;
import digit.repository.SchemesRepository;
import digit.web.models.scheme.CourseDetails;
import digit.web.models.scheme.EventDetails;
import digit.web.models.scheme.MachineDetails;
import digit.web.models.scheme.SchemeDetails;
import digit.web.models.scheme.SchemeHeadDetails;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SchemeService {
    @Autowired
    private SchemesRepository schemesRepository;

    private static final Logger logger = LoggerFactory.getLogger(SchemeService.class);

    public List<EventDetails> getSchemes(RequestInfo requestInfo, SchemeSearchCriteria searchcriteria) {
        if(requestInfo.getUserInfo() != null){
            searchcriteria.setUuid(requestInfo.getUserInfo().getUuid());
        }
        List<EventDetails> schemeWiseApplicatiocounts = schemesRepository.getSchemeWiseCounts(searchcriteria);
        log.info("withCount" + schemeWiseApplicatiocounts.toString());
        List<EventDetails> schemes = schemesRepository.getSchemeDetails(searchcriteria);
        log.info("schemes" + schemes.toString());
        if (!ObjectUtils.isEmpty(schemeWiseApplicatiocounts)) {
            schemes = mapCounts(schemes, schemeWiseApplicatiocounts);
        }
        if (CollectionUtils.isEmpty(schemes))
            return new ArrayList<>();
        return schemes;
    }

    public static List<EventDetails> mapCounts(List<EventDetails> targetList, List<EventDetails> sourceEvents) {

        EventDetails sourceEvent = sourceEvents.getFirst();
        targetList.forEach(targetEvent -> {
            targetEvent.getSchemeshead().forEach(targetSchemeHead -> {
                Optional<SchemeHeadDetails> matchingSourceSchemeHead = sourceEvent.getSchemeshead().stream()
                        .filter(sourceSchemeHead -> sourceSchemeHead.getSchemeHead()
                                .equals(targetSchemeHead.getSchemeHead()))
                        .findFirst();

                if (matchingSourceSchemeHead.isPresent()) {

                    SchemeHeadDetails sourceSchemeHead = matchingSourceSchemeHead.get();
                    targetSchemeHead.setSchemeHeadApplicationCount(sourceSchemeHead.getSchemeHeadApplicationCount());

                    targetSchemeHead.getSchemeDetails().forEach(targetSchemeDetails -> {
                        Optional<SchemeDetails> matchingSourceSchemeDetails = sourceSchemeHead.getSchemeDetails()
                                .stream()
                                .filter(sourceSchemeDetails -> sourceSchemeDetails.getSchemeID() == targetSchemeDetails
                                        .getSchemeID())
                                .findFirst();

                        if (matchingSourceSchemeDetails.isPresent()) {

                            SchemeDetails sourceSchemeDetails = matchingSourceSchemeDetails.get();
                            targetSchemeDetails
                                    .setSchemeApplicationCount(sourceSchemeDetails.getSchemeApplicationCount());

                            targetSchemeDetails.getMachines().forEach(targetMachine -> {

                                Optional<MachineDetails> matchingSourceMachine = sourceSchemeDetails.getMachines()
                                        .stream()
                                        .filter(sourceMachine -> sourceMachine.getMachID() == targetMachine.getMachID())
                                        .findFirst();

                                if (matchingSourceMachine.isPresent()) {

                                    MachineDetails sourceMachine = matchingSourceMachine.get();
                                    targetMachine.setMachineWiseApplicationCount(
                                            sourceMachine.getMachineWiseApplicationCount());
                                } else {

                                    targetMachine.setMachineWiseApplicationCount(0L);
                                }
                            });

                            targetSchemeDetails.getCourses().forEach(targetCourse -> {

                                Optional<CourseDetails> matchingSourceCourse = sourceSchemeDetails.getCourses().stream()
                                        .filter(sourceCourse -> sourceCourse.getCourseID() == targetCourse
                                                .getCourseID())
                                        .findFirst();

                                if (matchingSourceCourse.isPresent()) {

                                    CourseDetails sourceCourse = matchingSourceCourse.get();
                                    targetCourse.setCourseWiseApplicationCount(
                                            sourceCourse.getCourseWiseApplicationCount());
                                } else {

                                    targetCourse.setCourseWiseApplicationCount(0L);
                                }
                            });
                        } else {

                            targetSchemeDetails.setSchemeApplicationCount(0L);
                        }
                    });
                } else {

                    targetSchemeHead.setSchemeHeadApplicationCount(0L);
                }
            });
        });

        return targetList;
    }

}
