package org.egov.infra.indexer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.IndexerApplicationRunnerImpl;
import org.egov.infra.indexer.custom.pgr.PGRCustomDecorator;
import org.egov.infra.indexer.custom.pgr.PGRIndexObject;
import org.egov.infra.indexer.custom.pgr.ServiceResponse;
import org.egov.infra.indexer.custom.pt.PTCustomDecorator;
import org.egov.infra.indexer.custom.pt.PropertyResponse;
import org.egov.infra.indexer.models.IndexJob;
import org.egov.infra.indexer.models.IndexJob.StatusEnum;
import org.egov.infra.indexer.models.IndexJobWrapper;
import org.egov.infra.indexer.producer.IndexerProducer;
import org.egov.infra.indexer.util.IndexerUtils;
import org.egov.infra.indexer.util.ResponseInfoFactory;
import org.egov.infra.indexer.web.contract.Index;
import org.egov.infra.indexer.web.contract.LegacyIndexRequest;
import org.egov.infra.indexer.web.contract.LegacyIndexResponse;
import org.egov.infra.indexer.web.contract.Mapping;
import org.egov.infra.indexer.web.contract.Mapping.ConfigKeyEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LegacyIndexService {

    @Autowired
    private IndexerApplicationRunnerImpl runner;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IndexerUtils indexerUtils;

    @Autowired
    private ResponseInfoFactory factory;

    @Autowired
    private IndexerProducer indexerProducer;

    @Value("${egov.core.reindex.topic.name}")
    private String reindexTopic;

    @Value("${egov.core.legacyindex.topic.name}")
    private String legacyIndexTopic;

    @Value("${egov.indexer.persister.create.topic}")
    private String persisterCreate;

    @Value("${egov.indexer.persister.update.topic}")
    private String persisterUpdate;

    @Value("${reindex.pagination.size.default}")
    private Integer defaultPageSizeForReindex;

    @Value("${legacyindex.pagination.size.default}")
    private Integer defaultPageSizeForLegacyindex;

    @Value("${egov.service.host}")
    private String serviceHost;

    @Value("${egov.indexer.pgr.legacyindex.topic.name}")
    private String pgrLegacyTopic;

    @Value("${egov.indexer.pt.legacyindex.topic.name}")
    private String ptLegacyTopic;

    @Value("${egov.infra.indexer.host}")
    private String esHostUrl;

    @Autowired
    private PGRCustomDecorator pgrCustomDecorator;

    @Autowired
    private PTCustomDecorator ptCustomDecorator;

    @Value("${egov.core.no.of.index.threads}")
    private Integer noOfIndexThreads;

    @Value("${egov.core.index.thread.poll.ms}")
    private Long indexThreadPollInterval;

    @Value("${egov.infra.indexer.legacy.version}")
    private Boolean isLegacyVersionES;


    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final ScheduledExecutorService schedulerofChildThreads = Executors.newScheduledThreadPool(1);

    /**
     * Creates a legacy index job by making an entry into the eg_indexer_job and returns response with job identifiers.
     *
     * @param legacyindexRequest
     * @return
     */
    public LegacyIndexResponse createLegacyindexJob(LegacyIndexRequest legacyindexRequest) {
        log.info("=== LEGACY INDEX JOB CREATION STARTED ===");
        log.debug("Request Parameters - TenantId: {}, LegacyIndexTopic: {}",
                legacyindexRequest.getTenantId(), legacyindexRequest.getLegacyIndexTopic());
        log.debug("API Details - URI: {}, ResponseJsonPath: {}",
                legacyindexRequest.getApiDetails().getUri(),
                legacyindexRequest.getApiDetails().getResponseJsonPath());
        log.debug("Pagination Details - StartingOffset: {}, MaxPageSize: {}, MaxRecords: {}",
                legacyindexRequest.getApiDetails().getPaginationDetails().getStartingOffset(),
                legacyindexRequest.getApiDetails().getPaginationDetails().getMaxPageSize(),
                legacyindexRequest.getApiDetails().getPaginationDetails().getMaxRecords());

        Map<String, Mapping> mappingsMap = runner.getMappingMaps();
        log.debug("Retrieved mapping maps. Total mappings available: {}", mappingsMap.size());
        log.debug("Available mapping topics: {}", mappingsMap.keySet());

        LegacyIndexResponse legacyindexResponse = null;
        StringBuilder url = new StringBuilder();

        Mapping mapping = mappingsMap.get(legacyindexRequest.getLegacyIndexTopic());
        if (mapping == null) {
            log.error("CRITICAL: No mapping found for topic: {}. Available topics: {}",
                    legacyindexRequest.getLegacyIndexTopic(), mappingsMap.keySet());
            throw new IllegalArgumentException("No mapping configuration found for topic: " + legacyindexRequest.getLegacyIndexTopic());
        }
        log.debug("Successfully retrieved mapping for topic: {}", legacyindexRequest.getLegacyIndexTopic());

        Index index = mapping.getIndexes().get(0);
        log.debug("Index Configuration - Name: {}, Type: {}, JsonPath: {}, TimeStampField: {}",
                index.getName(), index.getType(), index.getJsonPath(), index.getTimeStampField());
        if(this.isLegacyVersionES) {
            url.append(esHostUrl).append(index.getName()).append("/").append(index.getType()).append("/_search");
            log.debug("Using legacy ES version format. URL: {}", url.toString());
        } else {
            url.append(esHostUrl).append(index.getName()).append("/_search");
            log.debug("Using modern ES version format. URL: {}", url.toString());
        }

        legacyindexResponse = LegacyIndexResponse.builder()
                .message("Please hit the 'url' after the legacy index job is complete.").url(url.toString())
                .responseInfo(factory.createResponseInfoFromRequestInfo(legacyindexRequest.getRequestInfo(), true))
                .build();

        String generatedJobId = UUID.randomUUID().toString();
        log.info("Generated JobId: {}", generatedJobId);

        IndexJob job = IndexJob.builder().jobId(generatedJobId).jobStatus(StatusEnum.INPROGRESS)
                .typeOfJob(ConfigKeyEnum.LEGACYINDEX)
                .requesterId(legacyindexRequest.getRequestInfo().getUserInfo().getUuid())
                .newIndex(index.getName() + "/" + index.getType()).tenantId(legacyindexRequest.getTenantId())
                .totalRecordsIndexed(0).totalTimeTakenInMS(0L)
                .auditDetails(
                        indexerUtils.getAuditDetails(legacyindexRequest.getRequestInfo().getUserInfo().getUuid(), true))
                .build();

        log.debug("Created IndexJob - JobId: {}, RequesterId: {}, NewIndex: {}, TenantId: {}",
                job.getJobId(), job.getRequesterId(), job.getNewIndex(), job.getTenantId());

        legacyindexRequest.setJobId(job.getJobId());
        legacyindexRequest.setStartTime(new Date().getTime());
        log.debug("Set JobId and StartTime on request. StartTime: {}", legacyindexRequest.getStartTime());

        IndexJobWrapper wrapper = IndexJobWrapper.builder().requestInfo(legacyindexRequest.getRequestInfo()).job(job)
                .build();

//		indexerProducer.producer(legacyIndexTopic, legacyindexRequest);
        log.info("Starting legacy index thread for JobId: {}", job.getJobId());
        beginLegacyIndex(legacyindexRequest);

        log.debug("Publishing job creation to Kafka topic: {}", persisterCreate);
        indexerProducer.producer(persisterCreate, wrapper);
        log.debug("Successfully published job creation to persister");

        legacyindexResponse.setJobId(job.getJobId());

        log.info("=== LEGACY INDEX JOB CREATION COMPLETED. JobId: {}, ES Query URL: {} ===",
                job.getJobId(), url.toString());

        return legacyindexResponse;
    }

    /**
     * Method to start the index thread for indexing activity
     *
     * @param reindexRequest
     * @return
     */
    public Boolean beginLegacyIndex(LegacyIndexRequest legacyIndexRequest) {
        log.info("BEGIN LEGACY INDEX - JobId: {}, Topic: {}",
                legacyIndexRequest.getJobId(), legacyIndexRequest.getLegacyIndexTopic());
        log.debug("Scheduling index thread with poll interval: {} ms", indexThreadPollInterval);
        indexThread(legacyIndexRequest);
        log.debug("Index thread scheduled successfully for JobId: {}", legacyIndexRequest.getJobId());
        return true;
    }

    /**
     * Index thread which performs the indexing job. It operates as follows: 1.
     * Based on the Request, it makes API calls in batches to the external service
     * 2. With every batch fetched, data is sent to child threads for processing 3.
     * Child threads perform primary data transformation if required and then hand
     * it over to another esIndexer method 4. The esIndexer method performs checks
     * and transformations pas per the config and then posts the data to es in bulk
     * 5. The process repeats until all the records are indexed.
     *
     * @param reindexRequest
     */
    private void indexThread(LegacyIndexRequest legacyIndexRequest) {
        final Runnable legacyIndexer = new Runnable() {
            boolean threadRun = true;

            public void run() {
                if (threadRun) {
                    log.info("========================================");
                    log.info("INDEX THREAD STARTED - JobId: {}", legacyIndexRequest.getJobId());
                    log.info("========================================");

                    ObjectMapper mapper = indexerUtils.getObjectMapper();

                    Integer offset = legacyIndexRequest.getApiDetails().getPaginationDetails().getStartingOffset();
                    offset = offset == null ? 0: offset;
                    Integer count = offset;
                    Integer presentCount = 0;
                    Integer maxRecords = legacyIndexRequest.getApiDetails().getPaginationDetails().getMaxRecords();
                    Integer size = null != legacyIndexRequest.getApiDetails().getPaginationDetails().getMaxPageSize()
                            ? legacyIndexRequest.getApiDetails().getPaginationDetails().getMaxPageSize()
                            : defaultPageSizeForLegacyindex;

                    log.info("Pagination Configuration:");
                    log.info("  - Initial Offset: {}", offset);
                    log.info("  - Page Size: {}", size);
                    log.info("  - Max Records Limit: {}", maxRecords != null ? maxRecords : "No limit");
                    log.info("  - Default Page Size: {}", defaultPageSizeForLegacyindex);
                    log.debug("API URI Template: {}", legacyIndexRequest.getApiDetails().getUri());
                    log.debug("Response JSONPath: {}", legacyIndexRequest.getApiDetails().getResponseJsonPath());

                    Boolean isProccessDone = false;

                    while (!isProccessDone) {
                        log.info("--- Batch Iteration Start ---");
                        log.info("Current State - Offset: {}, Count: {}, PresentCount: {}", offset, count, presentCount);

                        if (maxRecords > 0 && presentCount >= maxRecords ) {
                            isProccessDone = true;
                            log.info("STOPPING: MaxRecords limit reached. MaxRecords: {}, PresentCount: {}", maxRecords, presentCount);
                            break;
                        }

                        log.debug("Building paginated URI with offset: {} and size: {}", offset, size);
                        String uri = indexerUtils.buildPagedUriForLegacyIndex(legacyIndexRequest.getApiDetails(),
                                offset, size);
                        log.info("Calling API - URI: {}", uri);

                        Object request = null;
                        try {
                            request = legacyIndexRequest.getApiDetails().getRequest();
                            if (null == legacyIndexRequest.getApiDetails().getRequest()) {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("RequestInfo", legacyIndexRequest.getRequestInfo());
                                request = map;
                                log.debug("Request body is null, using default RequestInfo wrapper");
                            } else {
                                log.debug("Using custom request body from apiDetails");
                            }

                            log.debug("Making POST request to external service...");
                            Object response = restTemplate.postForObject(uri, request, Map.class);

                            if (null == response) {
                                log.error("FATAL: Received NULL response from API");
                                log.error("Request Details:");
                                log.error("  URI: {}", uri);
                                log.error("  Request: {}", request);
                                log.warn("Marking job as FAILED due to null response");
                                IndexJob job = IndexJob.builder().jobId(legacyIndexRequest.getJobId())
                                        .auditDetails(indexerUtils.getAuditDetails(
                                                legacyIndexRequest.getRequestInfo().getUserInfo().getUuid(), false))
                                        .totalRecordsIndexed(count)
                                        .totalTimeTakenInMS(new Date().getTime() - legacyIndexRequest.getStartTime())
                                        .jobStatus(StatusEnum.FAILED).build();
                                IndexJobWrapper wrapper = IndexJobWrapper.builder()
                                        .requestInfo(legacyIndexRequest.getRequestInfo()).job(job).build();
                                log.debug("Publishing FAILED job status to Kafka topic: {}", persisterUpdate);
                                indexerProducer.producer(persisterUpdate, wrapper);
                                threadRun = false;
                                break;
                            } else {
                                log.debug("API response received successfully");
                                log.debug("Extracting data using JSONPath: {}", legacyIndexRequest.getApiDetails().getResponseJsonPath());

                                List<Object> searchResponse = null;
                                try {
                                    searchResponse = JsonPath.read(response, legacyIndexRequest.getApiDetails().getResponseJsonPath());
                                    log.debug("JSONPath extraction successful. Records found: {}",
                                            searchResponse != null ? searchResponse.size() : 0);
                                } catch (Exception jsonPathEx) {
                                    log.error("ERROR: JSONPath extraction failed!");
                                    log.error("  JSONPath: {}", legacyIndexRequest.getApiDetails().getResponseJsonPath());
                                    log.error("  Exception: {}", jsonPathEx.getMessage());
                                    throw jsonPathEx;
                                }

                                if (!CollectionUtils.isEmpty(searchResponse)) {
                                    log.info("Processing batch with {} records", searchResponse.size());
                                    log.debug("Sending response to childThreadExecutor for transformation");

                                    childThreadExecutor(legacyIndexRequest, mapper, response);

                                    presentCount = searchResponse.size();
                                    count += size;
                                    log.info("Batch processed successfully:");
                                    log.info("  - Records in this batch: {}", searchResponse.size());
                                    log.info("  - Total processed so far: {}", count);
                                    log.info("  - Current offset: {}", offset);
                                } else {
                                    log.info("EMPTY RESPONSE: No more records found. Completing indexing.");
                                    if (count > size) {
                                        count = (count - size) + presentCount;
                                    }else if(count == size) {
                                        count = presentCount;
                                    }
                                    log.info("Final Record Count: {}", count);
                                    isProccessDone = true;
                                    threadRun = false;
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            log.error("========================================");
                            log.error("EXCEPTION OCCURRED - JOB FAILED!");
                            log.error("========================================");
                            log.error("Failure Details:");
                            log.error("  JobId: {}", legacyIndexRequest.getJobId());
                            log.error("  Offset: {}", offset);
                            log.error("  Size: {}", size);
                            log.error("  URI: {}", uri);
                            log.error("  Records processed before failure: {}", count);
                            log.error("Exception Type: {}", e.getClass().getName());
                            log.error("Exception Message: {}", e.getMessage());
                            log.error("Full Exception Stack Trace:", e);
                            if (request != null) {
                                log.error("Request Body: {}", request);
                            }

                            log.warn("Marking job as FAILED and sending to persister");
                            IndexJob job = IndexJob.builder().jobId(legacyIndexRequest.getJobId())
                                    .auditDetails(indexerUtils.getAuditDetails(
                                            legacyIndexRequest.getRequestInfo().getUserInfo().getUuid(), false))
                                    .totalRecordsIndexed(count)
                                    .totalTimeTakenInMS(new Date().getTime() - legacyIndexRequest.getStartTime())
                                    .jobStatus(StatusEnum.FAILED).build();
                            IndexJobWrapper wrapper = IndexJobWrapper.builder()
                                    .requestInfo(legacyIndexRequest.getRequestInfo()).job(job).build();
                            indexerProducer.producer(persisterUpdate, wrapper);
                            threadRun = false;
                            break;
                        }

                        log.debug("Updating job progress status to INPROGRESS");
                        IndexJob job = IndexJob.builder().jobId(legacyIndexRequest.getJobId())
                                .auditDetails(indexerUtils.getAuditDetails(
                                        legacyIndexRequest.getRequestInfo().getUserInfo().getUuid(), false))
                                .totalTimeTakenInMS(new Date().getTime() - legacyIndexRequest.getStartTime())
                                .jobStatus(StatusEnum.INPROGRESS).totalRecordsIndexed(count).build();
                        IndexJobWrapper wrapper = IndexJobWrapper.builder()
                                .requestInfo(legacyIndexRequest.getRequestInfo()).job(job).build();
                        log.debug("Publishing INPROGRESS status to Kafka topic: {}", persisterUpdate);
                        indexerProducer.producer(persisterUpdate, wrapper);
                        log.debug("Progress update sent successfully");

                        offset += size;
                        log.info("Moving to next batch. New offset: {}", offset);
                        log.info("--- Batch Iteration End ---");
                    }
                    if (isProccessDone) {
                        log.info("========================================");
                        log.info("INDEXING COMPLETED SUCCESSFULLY!");
                        log.info("========================================");
                        long totalTime = new Date().getTime() - legacyIndexRequest.getStartTime();
                        log.info("Job Summary:");
                        log.info("  JobId: {}", legacyIndexRequest.getJobId());
                        log.info("  Total Records Indexed: {}", count);
                        log.info("  Total Time Taken: {} ms ({} seconds)", totalTime, totalTime / 1000);
                        log.info("  Average Time per Record: {} ms", count > 0 ? totalTime / count : 0);

                        IndexJob job = IndexJob.builder().jobId(legacyIndexRequest.getJobId())
                                .auditDetails(indexerUtils.getAuditDetails(
                                        legacyIndexRequest.getRequestInfo().getUserInfo().getUuid(), false))
                                .totalRecordsIndexed(count)
                                .totalTimeTakenInMS(totalTime)
                                .jobStatus(StatusEnum.COMPLETED).build();
                        IndexJobWrapper wrapper = IndexJobWrapper.builder()
                                .requestInfo(legacyIndexRequest.getRequestInfo()).job(job).build();
                        log.debug("Publishing COMPLETED status to Kafka topic: {}", persisterUpdate);
                        indexerProducer.producer(persisterUpdate, wrapper);
                        log.info("Job completion status sent to persister successfully");
                    }

                }
                threadRun = false;
                log.info("Index thread terminated for JobId: {}", legacyIndexRequest.getJobId());
            }
        };
        log.debug("Scheduling legacy indexer runnable with {} ms delay", indexThreadPollInterval);
        scheduler.schedule(legacyIndexer, indexThreadPollInterval, TimeUnit.MILLISECONDS);
    }


    /**
     * Child threads which perform the primary data transformation and pass it on to
     * the esIndexer method
     *
     * @param reindexRequest
     * @param mapper
     * @param requestToReindex
     * @param resultSize
     */
    public void childThreadExecutor(LegacyIndexRequest legacyIndexRequest, ObjectMapper mapper, Object response) {
        log.debug(">>> CHILD THREAD EXECUTOR STARTED <<<");
        log.debug("Processing topic: {}", legacyIndexRequest.getLegacyIndexTopic());
        log.debug("JobId: {}", legacyIndexRequest.getJobId());

        try {
            log.debug("Response object type: {}", response != null ? response.getClass().getName() : "null");

            if (legacyIndexRequest.getLegacyIndexTopic().equals(pgrLegacyTopic)) {
                log.info("Processing PGR (Public Grievance Redressal) data");
                log.debug("PGR Legacy Topic: {}", pgrLegacyTopic);

                log.debug("Converting response to ServiceResponse object...");
                ServiceResponse serviceResponse = mapper.readValue(mapper.writeValueAsString(response),
                        ServiceResponse.class);
                log.debug("ServiceResponse created. Services count: {}",
                        serviceResponse.getServices() != null ? serviceResponse.getServices().size() : 0);

                //PGRIndexObject indexObject = pgrCustomDecorator.dataTransformationForPGR(serviceResponse);
                //log.info("childThreadExecutor + indexObject----"+mapper.writeValueAsString(indexObject));

                log.debug("Publishing ServiceResponse to Kafka topic: {}", legacyIndexRequest.getLegacyIndexTopic());
                indexerProducer.producer(legacyIndexRequest.getLegacyIndexTopic(), serviceResponse);
                log.info("PGR data successfully published to Kafka");

            } else {
                if (legacyIndexRequest.getLegacyIndexTopic().equals(ptLegacyTopic)) {
                    log.info("Processing PT (Property Tax) data");
                    log.debug("PT Legacy Topic: {}", ptLegacyTopic);

                    log.debug("Converting response to PropertyResponse object...");
                    PropertyResponse propertyResponse = mapper.readValue(mapper.writeValueAsString(response), PropertyResponse.class);
                    log.debug("PropertyResponse created. Properties count: {}",
                            propertyResponse.getProperties() != null ? propertyResponse.getProperties().size() : 0);

                    log.debug("Applying PT custom transformation via PTCustomDecorator...");
                    propertyResponse.setProperties(ptCustomDecorator.transformData(propertyResponse.getProperties()));
                    log.debug("PT transformation completed");

                    log.debug("Publishing PropertyResponse to Kafka topic: {}", legacyIndexRequest.getLegacyIndexTopic());
                    indexerProducer.producer(legacyIndexRequest.getLegacyIndexTopic(), propertyResponse);
                    log.info("PT data successfully published to Kafka");

                } else {
                    log.info("Processing GENERIC data (no special transformation)");
                    log.debug("Topic: {}", legacyIndexRequest.getLegacyIndexTopic());

                    log.debug("Publishing raw response to Kafka topic: {}", legacyIndexRequest.getLegacyIndexTopic());
                    indexerProducer.producer(legacyIndexRequest.getLegacyIndexTopic(), response);
                    log.info("Generic data successfully published to Kafka");
                }
            }

            log.debug(">>> CHILD THREAD EXECUTOR COMPLETED SUCCESSFULLY <<<");

        } catch (Exception e) {
            log.error("========================================");
            log.error("ERROR IN CHILD THREAD EXECUTOR!");
            log.error("========================================");
            log.error("Error Details:");
            log.error("  JobId: {}", legacyIndexRequest.getJobId());
            log.error("  Topic: {}", legacyIndexRequest.getLegacyIndexTopic());
            log.error("  PGR Topic Match: {}", legacyIndexRequest.getLegacyIndexTopic().equals(pgrLegacyTopic));
            log.error("  PT Topic Match: {}", legacyIndexRequest.getLegacyIndexTopic().equals(ptLegacyTopic));
            log.error("Exception Type: {}", e.getClass().getName());
            log.error("Exception Message: {}", e.getMessage());
            log.error("Full Stack Trace:", e);

            if (response != null) {
                try {
                    String responseSnippet = mapper.writeValueAsString(response);
                    // Log only first 500 characters to avoid log overflow
                    log.error("Response Snippet (first 500 chars): {}",
                            responseSnippet.length() > 500 ? responseSnippet.substring(0, 500) + "..." : responseSnippet);
                } catch (Exception jsonEx) {
                    log.error("Could not serialize response for logging: {}", jsonEx.getMessage());
                }
            }
        }
    }

}
