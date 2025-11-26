/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) 2016  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.demand.service;

import static org.egov.demand.util.Constants.BUSINESS_SERVICE_URL_PARAMETER;
import static org.egov.demand.util.Constants.CONSUMERCODES_REPLACE_TEXT;
import static org.egov.demand.util.Constants.TENANTID_REPLACE_TEXT;
import static org.egov.demand.util.Constants.URL_NOT_CONFIGURED_FOR_DEMAND_UPDATE_KEY;
import static org.egov.demand.util.Constants.URL_NOT_CONFIGURED_FOR_DEMAND_UPDATE_MSG;
import static org.egov.demand.util.Constants.URL_NOT_CONFIGURED_REPLACE_TEXT;
import static org.egov.demand.util.Constants.URL_PARAMS_FOR_SERVICE_BASED_DEMAND_APIS;
import static org.egov.demand.util.Constants.URL_PARAM_SEPERATOR;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.egov.common.contract.request.PlainAccessRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.demand.config.ApplicationProperties;
import org.egov.demand.model.BillAccountDetailV2;
import org.egov.demand.model.BillDetailV2;
import org.egov.demand.model.BillSearchCriteria;
import org.egov.demand.model.BillV2;
import org.egov.demand.model.BillV2.BillStatus;
import org.egov.demand.model.BusinessServiceDetail;
import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandCriteria;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.GenerateBillCriteria;
import org.egov.demand.model.GlCodeMaster;
import org.egov.demand.model.TaxHeadMaster;
import org.egov.demand.model.TaxHeadMasterCriteria;
import org.egov.demand.model.UpdateBillCriteria;
import org.egov.demand.model.UpdateBillRequest;
import org.egov.demand.producer.Producer;
import org.egov.demand.repository.BillRepositoryV2;
import org.egov.demand.repository.IdGenRepo;
import org.egov.demand.repository.ServiceRequestRepository;
import org.egov.demand.util.Util;
import org.egov.demand.web.contract.BillRequestV2;
import org.egov.demand.web.contract.BillResponseV2;
import org.egov.demand.web.contract.BusinessServiceDetailCriteria;
import org.egov.demand.web.contract.RequestInfoWrapper;
import org.egov.demand.web.contract.User;
import org.egov.demand.web.contract.UserResponse;
import org.egov.demand.web.contract.UserSearchRequest;
import org.egov.demand.web.contract.factory.ResponseFactory;
import org.egov.demand.web.validator.BillValidator;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BillServicev2 {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private ResponseFactory responseFactory;

	@Autowired
	private ApplicationProperties appProps;

	@Autowired
	private BillRepositoryV2 billRepository;

	@Autowired
	private DemandService demandService;

	@Autowired
	private BusinessServDetailService businessServDetailService;

	@Autowired
	private TaxHeadMasterService taxHeadService;
	
	@Autowired
	private Util util;
	
	@Autowired
	private ServiceRequestRepository restRepository;
	
	@Autowired
	private IdGenRepo idGenRepo;
	
	@Autowired
	private BillValidator billValidator;

	@Autowired
	private Producer producer;
	
	@Autowired
	private ObjectMapper mapper;

	@Value("${kafka.topics.cancel.bill.topic.name}")
	private String billCancelTopic;

	@Value("${kafka.topics.billgen.topic.name}")
	private String notifTopicName;

	private static List<String> ownerPlainRequestFieldsList;

	/**
	 * Cancell bill operation can be carried by this method, based on consumerCodes
	 * and businessService.
	 * 
	 * Only ACTIVE bills will be cancelled as of now
	 * 
	 * @param cancelBillCriteria
	 * @param requestInfoWrapper
	 */
	public Integer cancelBill(UpdateBillRequest updateBillRequest) {
		
		UpdateBillCriteria cancelBillCriteria = updateBillRequest.getUpdateBillCriteria();
		billValidator.validateBillSearchRequest(cancelBillCriteria);
		Set<String> consumerCodes = cancelBillCriteria.getConsumerCodes();
		cancelBillCriteria.setStatusToBeUpdated(BillStatus.CANCELLED);

		if (!CollectionUtils.isEmpty(consumerCodes) && consumerCodes.size() > 1) {
			
			throw new CustomException("EG_BS_CANCEL_BILL_ERROR", "Only one consumer code can be provided in the Cancel request");
		} else {
			int result = billRepository.updateBillStatus(cancelBillCriteria);
			sendNotificationForBillCancellation(updateBillRequest.getRequestInfo(), cancelBillCriteria);
			return result;
		}
	}

	private void sendNotificationForBillCancellation(RequestInfo requestInfo, UpdateBillCriteria cancelBillCriteria) {
		Set<String> consumerCodes = cancelBillCriteria.getConsumerCodes();
		if(CollectionUtils.isEmpty(consumerCodes))
			return;

		List<BillV2> bills =  billRepository.findBill(BillSearchCriteria.builder()
				.service(cancelBillCriteria.getBusinessService())
				.tenantId(cancelBillCriteria.getTenantId())
				.consumerCode(consumerCodes)
				.build());

		if (CollectionUtils.isEmpty(bills))
			return;

		BillRequestV2 req = BillRequestV2.builder().bills(bills).requestInfo(requestInfo).build();
		producer.push(billCancelTopic, req);

	}

	/**
	 * Fetches the bill for given parameters
	 * 
	 * Searches the respective bill
	 * if nothing found then generates bill for the same criteria
	 * if bill found then checks the validity of the bill
	 * 	return the bill if valid
	 * else update the demands belonging to the bill then generate a new bill
	 * 
	 * @param moduleCode
	 * @param consumerCodes
	 * @return
	 */
	public BillResponseV2 fetchBill(GenerateBillCriteria billCriteria, RequestInfoWrapper requestInfoWrapper) {

		RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
		billValidator.validateBillGenRequest(billCriteria, requestInfo);
		if (CollectionUtils.isEmpty(billCriteria.getConsumerCode()))
			billCriteria.setConsumerCode(new HashSet<>());
		BillResponseV2 res = searchBill(billCriteria.toBillSearchCriteria(), requestInfo);
		List<BillV2> bills = res.getBill();

		/*
		 * If no existing bills found then Generate new bill
		 */
		if (CollectionUtils.isEmpty(bills))
		{
			log.info( "If bills are empty" +bills.size());
			//if(!billCriteria.getBusinessService().equalsIgnoreCase("WS") && !billCriteria.getBusinessService().equalsIgnoreCase("SW"))
			//if(!billCriteria.getBusinessService().equalsIgnoreCase("SW"))
			// Support both single and multiple business services
			if (!CollectionUtils.isEmpty(billCriteria.getBusinessServices())) {
				// Update demands for each business service
				for (String businessService : billCriteria.getBusinessServices()) {
					updateDemandsForexpiredBillDetails(businessService, billCriteria.getConsumerCode(), billCriteria.getTenantId(), requestInfoWrapper);
				}
			} else if (billCriteria.getBusinessService() != null) {
				// Backward compatibility: single business service
				updateDemandsForexpiredBillDetails(billCriteria.getBusinessService(), billCriteria.getConsumerCode(), billCriteria.getTenantId(), requestInfoWrapper);
			}
			return generateBill(billCriteria, requestInfo);
		}
		
		
		/*
		 * Adding consumer-codes of unbilled demands to generate criteria
		 */
		if (!(StringUtils.isEmpty(billCriteria.getMobileNumber()) && StringUtils.isEmpty(billCriteria.getEmail()))) {

			List<Demand> demands = demandService.getDemands(billCriteria.toDemandCriteria(), requestInfo);
			billCriteria.getConsumerCode().addAll(
					demands.stream().map(Demand::getConsumerCode).collect(Collectors.toSet()));
		}

		log.debug("fetchBill--------going to generate new bill-------------------");
		Map<String, BillV2> consumerCodeAndBillMap = bills.stream().collect(Collectors.toMap(BillV2::getConsumerCode, Function.identity()));
		billCriteria.getConsumerCode().addAll(consumerCodeAndBillMap.keySet());
		/*
		 * Collecting the businessService code and the list of consumer codes for those service codes 
		 * whose demands needs to be updated.
		 * 
		 * grouping by service code and collecting the list of 
		 * consumerCodes against the service code
		 */
 		List<String> cosnumerCodesNotFoundInBill = new ArrayList<>(billCriteria.getConsumerCode());
		Set<String> cosnumerCodesToBeExpired = new HashSet<>();
		Map<String, Set<String>> businessServiceToConsumerCodesMap = new HashMap<>();
		List<BillV2> billsToBeReturned = new ArrayList<>();
		Boolean isBillExpired = false;

		for (Entry<String, BillV2> entry : consumerCodeAndBillMap.entrySet()) {
			BillV2 bill = entry.getValue();

			for (BillDetailV2 billDetail : bill.getBillDetails()) {
				if (billDetail.getExpiryDate().compareTo(System.currentTimeMillis()) < 0) {
					isBillExpired = true;
					break;
				}
			}
			if (!isBillExpired) {
				billsToBeReturned.add(bill);
			} else {
				cosnumerCodesToBeExpired.add(bill.getConsumerCode());
				// Group consumer codes by business service
				businessServiceToConsumerCodesMap
					.computeIfAbsent(bill.getBusinessService(), k -> new HashSet<>())
					.add(bill.getConsumerCode());
			}
			cosnumerCodesNotFoundInBill.remove(entry.getKey());
			isBillExpired = false;
		}

		log.info("Consumer Code to be expired " + cosnumerCodesToBeExpired);
		log.info("Consumer code not found in bill " + cosnumerCodesNotFoundInBill);
		/*
		 * If none of the billDetails in the bills needs to be updated then return the search result
		 */
		if(CollectionUtils.isEmpty(cosnumerCodesToBeExpired) && CollectionUtils.isEmpty(cosnumerCodesNotFoundInBill))
			return res;
		else {

			billCriteria.getConsumerCode().retainAll(cosnumerCodesToBeExpired);
			billCriteria.getConsumerCode().addAll(cosnumerCodesNotFoundInBill);

			if (!CollectionUtils.isEmpty(billCriteria.getBusinessServices())) {
				for (String businessService : billCriteria.getBusinessServices()) {
					// Get consumer codes specific to this business service
					Set<String> consumerCodesForService = businessServiceToConsumerCodesMap.getOrDefault(businessService, new HashSet<>());

					if (!consumerCodesForService.isEmpty()) {
						updateDemandsForexpiredBillDetails(businessService, consumerCodesForService, billCriteria.getTenantId(), requestInfoWrapper);

						billRepository.updateBillStatus(
								UpdateBillCriteria.builder()
								.statusToBeUpdated(BillStatus.EXPIRED)
								.businessService(businessService)
								.consumerCodes(consumerCodesForService)
								.tenantId(billCriteria.getTenantId())
								.build()
								);
					}
				}
			} else if (billCriteria.getBusinessService() != null) {
				updateDemandsForexpiredBillDetails(billCriteria.getBusinessService(), billCriteria.getConsumerCode(), billCriteria.getTenantId(), requestInfoWrapper);

				billRepository.updateBillStatus(
						UpdateBillCriteria.builder()
						.statusToBeUpdated(BillStatus.EXPIRED)
						.businessService(billCriteria.getBusinessService())
						.consumerCodes(cosnumerCodesToBeExpired)
						.tenantId(billCriteria.getTenantId())
						.build()
						);
			}

			BillResponseV2 finalResponse = generateBill(billCriteria, requestInfo);
			// gen bill returns immutable empty list incase of zero bills
			billsToBeReturned.addAll(finalResponse.getBill());
			finalResponse.setBill(billsToBeReturned);
			return finalResponse;
		}
	}

	/**
	 * To make calls to respective service which updates the demands belonging to
	 * the arguments passed
	 * 
	 * @param serviceAndConsumerCodeListMap
	 * @param tenantId
	 */
	private void updateDemandsForexpiredBillDetails(String businessService, Set<String> consumerCodesTobeUpdated, String tenantId, RequestInfoWrapper requestInfoWrapper) {

		Map<String, String> serviceUrlMap = appProps.getBusinessCodeAndDemandUpdateUrlMap();


			String url = serviceUrlMap.get(businessService);
			if (StringUtils.isEmpty(url)) {
				
				log.info(URL_NOT_CONFIGURED_FOR_DEMAND_UPDATE_KEY, URL_NOT_CONFIGURED_FOR_DEMAND_UPDATE_MSG
						.replace(URL_NOT_CONFIGURED_REPLACE_TEXT, businessService));
				return;
			}

			StringBuilder completeUrl = new StringBuilder(url)
					.append(URL_PARAMS_FOR_SERVICE_BASED_DEMAND_APIS.replace(TENANTID_REPLACE_TEXT, tenantId).replace(
							CONSUMERCODES_REPLACE_TEXT, consumerCodesTobeUpdated.toString().replace("[", "").replace("]", "")));

			completeUrl.append(URL_PARAM_SEPERATOR).append(BUSINESS_SERVICE_URL_PARAMETER).append(businessService);
			log.info("the url : " + completeUrl);
			restRepository.fetchResult(completeUrl.toString(), requestInfoWrapper);
	}


	/**
	 * Searches the bills from DB for given criteria and enriches them with TaxAndPayments array
	 * 
	 * @param billCriteria
	 * @param requestInfo
	 * @return
	 */
	public BillResponseV2 searchBill(BillSearchCriteria billCriteria, RequestInfo requestInfo) {

		List<BillV2> bills = billRepository.findBill(billCriteria);

		return BillResponseV2.builder().resposneInfo(responseFactory.getResponseInfo(requestInfo, HttpStatus.OK))
				.bill(bills).build();
	}
	
	/**
	 * Generate bill based on the given criteria
	 * 
	 * @param billCriteria
	 * @param requestInfo
	 * @return
	 */
	public BillResponseV2 generateBill(GenerateBillCriteria billCriteria, RequestInfo requestInfo) {

		Set<String> demandIds = new HashSet<>();
		Set<String> consumerCodes = new HashSet<>();

		if (billCriteria.getDemandId() != null)
			demandIds.add(billCriteria.getDemandId());

		if (billCriteria.getConsumerCode() != null)
			consumerCodes.addAll(billCriteria.getConsumerCode());


		Set<String> businessServiceSet = null;
		if (!CollectionUtils.isEmpty(billCriteria.getBusinessServices())) {
			businessServiceSet = billCriteria.getBusinessServices();
		} else if (billCriteria.getBusinessService() != null) {
			businessServiceSet = Collections.singleton(billCriteria.getBusinessService());
		}

		// Fetch business service details to check if advance is allowed
		// Replaces hardcoded WS/SW check to support all advance-enabled business services (e.g., TX.Emarket_Rental_Fees)
		Map<String, BusinessServiceDetail> businessServiceMap = null;
		boolean hasAdvanceAllowedServices = false;

		if (businessServiceSet != null && !businessServiceSet.isEmpty()) {
			// Fetch business service details from MDMS
			businessServiceMap = getBusinessService(businessServiceSet,
													billCriteria.getTenantId(),
													requestInfo);

			// Check if any business service has isAdvanceAllowed = true
			hasAdvanceAllowedServices = businessServiceMap.values().stream()
					.anyMatch(bs -> Boolean.TRUE.equals(bs.getIsAdvanceAllowed()));
		}

		// Build demand criteria based on advance-allowed status
		DemandCriteria demandCriteria;
		if (hasAdvanceAllowedServices) {
			// For advance-allowed services, don't filter by isPaymentCompleted
			// This allows zero-amount demands to be fetched
			demandCriteria = DemandCriteria.builder()
					.status(org.egov.demand.model.Demand.StatusEnum.ACTIVE.toString())
					.businessService(billCriteria.getBusinessService())
					.businessServices(businessServiceSet)
					.mobileNumber(billCriteria.getMobileNumber())
					.tenantId(billCriteria.getTenantId())
					.email(billCriteria.getEmail())
					.consumerCode(consumerCodes)
					// .isPaymentCompleted(false)  // Commented out for advance-allowed services
					.receiptRequired(false)
					.demandId(demandIds)
					.build();
		} else {
			// For non-advance services, filter by isPaymentCompleted
			demandCriteria = DemandCriteria.builder()
					.status(org.egov.demand.model.Demand.StatusEnum.ACTIVE.toString())
					.businessService(billCriteria.getBusinessService())
					.businessServices(businessServiceSet)
					.mobileNumber(billCriteria.getMobileNumber())
					.tenantId(billCriteria.getTenantId())
					.email(billCriteria.getEmail())
					.consumerCode(consumerCodes)
					.isPaymentCompleted(false)
					.receiptRequired(false)
					.demandId(demandIds)
					.build();
		}
		

		/* Fetching demands for the given bill search criteria */
		List<Demand> demands = demandService.getDemands(demandCriteria, requestInfo);

		// For advance-allowed services, filter out old zero-amount demands
		// Keep only: (1) non-zero demands, (2) recent zero-amount advance demands
		if (hasAdvanceAllowedServices && !CollectionUtils.isEmpty(demands)) {
			int originalSize = demands.size();
			demands = demands.stream()
					.filter(demand -> {
						// Always include non-zero demands
						if (!isZeroAmountDemand(demand)) {
							return true;
						}
						// For zero-amount demands, only include if they are recent advances
						return isRecentZeroAmountAdvanceDemand(demand);
					})
					.collect(Collectors.toList());

			if (originalSize != demands.size()) {
				log.info("Filtered out {} old zero-amount demands. Remaining demands: {}",
						originalSize - demands.size(), demands.size());
			}
		}

		List<BillV2> bills;

		if (!demands.isEmpty())
			bills = prepareBill(demands, requestInfo);
		else
			return getBillResponse(Collections.emptyList());

		BillRequestV2 billRequest = BillRequestV2.builder().bills(bills).requestInfo(requestInfo).build();
		//kafkaTemplate.send(notifTopicName, null, billRequest);
		return create(billRequest);
	}

	/**
	 * method to get user unmasked
	 * 
	 * @param requestInfo
	 * @param uuid
	 * @return user
	 */
	private User getUnmaskedUser(RequestInfo requestInfo, String uuid) {
		
		PlainAccessRequest apiPlainAccessRequest = requestInfo.getPlainAccessRequest();
		List<String> plainRequestFieldsList = getOwnerFieldsPlainAccessList();
		PlainAccessRequest plainAccessRequest = PlainAccessRequest.builder()
				.plainRequestFields(plainRequestFieldsList)
				.recordId(uuid)
				.build();
		requestInfo.setPlainAccessRequest(plainAccessRequest);
		
		UserSearchRequest  userSearchRequest= UserSearchRequest.builder()
				.uuid(Stream.of(uuid).collect(Collectors.toSet()))
				.requestInfo(requestInfo)
				.build();
		String userUri = appProps.getUserServiceHostName()
				.concat(appProps.getUserServiceSearchPath());
		List<User> payer = mapper.convertValue(restRepository.fetchResult(userUri, userSearchRequest),
				UserResponse.class).getUser();
		
		requestInfo.setPlainAccessRequest(apiPlainAccessRequest);
		
		return payer.get(0);
	}

	/**
	 * Prepares the bill object from the list of given demands
	 * 
	 * @param demands demands for which bill should be generated
	 * @param requestInfo 
	 * @return
	 */
	private List<BillV2> prepareBill(List<Demand> demands, RequestInfo requestInfo) {

		log.info("Demand received in prepare Bill are of size "+ demands.size() );
		log.info("Demands are::"+demands);
		List<BillV2> bills = new ArrayList<>();
		User payer = null != demands.get(0).getPayer() ? demands.get(0).getPayer() : new User();
		if (payer.getUuid() != null)
			payer = getUnmaskedUser(requestInfo, payer.getUuid());

		Map<String, List<Demand>> tenatIdDemandsList = demands.stream().collect(Collectors.groupingBy(Demand::getTenantId));
		for (Entry<String, List<Demand>> demandTenantEntry : tenatIdDemandsList.entrySet()) {

			/*
			 * Fetching Required master data
			 */
			String tenantId = demandTenantEntry.getKey();
			List<Demand> demandForOneTenant = demandTenantEntry.getValue();
			Set<String> businessCodes = new HashSet<>();
			Set<String> taxHeadCodes = new HashSet<>();

			for (Demand demand : demandForOneTenant) {

				businessCodes.add(demand.getBusinessService());
				demand.getDemandDetails().forEach(detail -> taxHeadCodes.add(detail.getTaxHeadMasterCode()));
			}
			
			Map<String, TaxHeadMaster> taxHeadMap = getTaxHeadMaster(taxHeadCodes, tenantId, requestInfo);
			Map<String, BusinessServiceDetail> businessMap = getBusinessService(businessCodes, tenantId, requestInfo);
			
			
			/*
			 * Grouping the demands by their consumer code and generating a bill for each consumer code
			 */
			Map<String, List<Demand>> consumerCodeAndDemandsMap = demandForOneTenant.stream().collect(Collectors.groupingBy(Demand::getConsumerCode));
			
			for (Entry<String, List<Demand>> consumerCodeAndDemands : consumerCodeAndDemandsMap.entrySet()) {
				
				BigDecimal billAmount = BigDecimal.ZERO;
				List<BillDetailV2> billDetails = new ArrayList<>();
				
				String consumerCode = consumerCodeAndDemands.getKey();
				BigDecimal minimumAmtPayableForBill = BigDecimal.ZERO;
				List<Demand> demandsForSingleCode = consumerCodeAndDemands.getValue();
				BusinessServiceDetail business = businessMap.get(demandsForSingleCode.get(0).getBusinessService());
				
				String billId = UUID.randomUUID().toString();
				String billNumber = getBillNumbers(requestInfo, tenantId, demandForOneTenant.get(0).getBusinessService(), 1).get(0);
				
				for (Demand demand : demandsForSingleCode) {

					minimumAmtPayableForBill = minimumAmtPayableForBill.add(demand.getMinimumAmountPayable());
					String billDetailId = UUID.randomUUID().toString();
					BillDetailV2 billDetail = getBillDetailForDemand(demand, taxHeadMap, billDetailId);
					billDetail.setBillId(billId);
					billDetail.setId(billDetailId);
					billDetails.add(billDetail);
					billAmount = billAmount.add(billDetail.getAmount());
				}
				
				// Create bill if: (1) non-negative amount OR (2) negative amount for advance-allowed services
				// Replaced hardcoded ADVANCE_ALLOWED_BUSINESS_SERVICES with dynamic isAdvanceAllowed check
				if ((billAmount.compareTo(BigDecimal.ZERO) >= 0) ||
						(billAmount.compareTo(BigDecimal.ZERO) < 0 && Boolean.TRUE.equals(business.getIsAdvanceAllowed()))) {

					BillV2 bill = BillV2.builder()
						.auditDetails(util.getAuditDetail(requestInfo))
						.payerAddress(payer.getPermanentAddress())
						.mobileNumber(payer.getMobileNumber())
						.billDate(System.currentTimeMillis())
						.businessService(business.getCode())
						.payerName(payer.getName())
						.consumerCode(consumerCode)
						.status(BillStatus.ACTIVE)
						.billDetails(billDetails)
						.totalAmount(billAmount)
						.userId(payer.getUuid())
						.billNumber(billNumber)
						.tenantId(tenantId)
						.id(billId)
						.build();
				
					bills.add(bill);
				}
			}

		}
		return bills;
	}

	private List<String> getBillNumbers(RequestInfo requestInfo, String tenantId, String module, int count) {

		String billNumberFormat = appProps.getBillNumberFormat();
		billNumberFormat = billNumberFormat.replace(appProps.getModuleReplaceStirng(), module);

		if (appProps.getIsTenantLevelBillNumberingEnabled())
			billNumberFormat = billNumberFormat.replace(appProps.getTenantIdReplaceString(), "_".concat(tenantId.split("\\.")[1]));
		else
			billNumberFormat = billNumberFormat.replace(appProps.getTenantIdReplaceString(), "");

		return idGenRepo.getId(requestInfo, tenantId, "billnumberid", billNumberFormat, count);
	}


	/**
	 * Method to create BillDetail object from demand
	 *  
	 * @param demand
	 * @param taxHeadMap
	 * @param businessDetailMap
	 * @return
	 */
	private BillDetailV2 getBillDetailForDemand(Demand demand, Map<String, TaxHeadMaster> taxHeadMap, String billDetailId) {
		
		Long startPeriod = demand.getTaxPeriodFrom();
		Long endPeriod = demand.getTaxPeriodTo();
		String tenantId = demand.getTenantId();

		BigDecimal totalAmountForDemand = BigDecimal.ZERO;
		

		/*
		 * Map to store the bill account detail object with TaxHead code
		 * To accommodate conversion of multiple DemandDetails with same tax head code to single BillAccountDetail
		 */
		Map<String, BillAccountDetailV2> taxCodeAccountdetailMap = new HashMap<>();
		
		for(DemandDetail demandDetail : demand.getDemandDetails()) {

			TaxHeadMaster taxHead = taxHeadMap.get(demandDetail.getTaxHeadMasterCode());
			BigDecimal amountForAccDeatil = demandDetail.getTaxAmount().subtract(demandDetail.getCollectionAmount());

			// Extract GLCode from MDMS TaxHeadMaster (backward compatible - can be null)
			String glCode = extractGlCodeFromTaxHead(taxHead);

			addOrUpdateBillAccDetailInTaxCodeAccDetailMap(taxCodeAccountdetailMap, demandDetail, taxHead, billDetailId, glCode);

			/* Total tax and collection for the whole demand/bill-detail */
			totalAmountForDemand = totalAmountForDemand.add(amountForAccDeatil);
		}

		
		Long billExpiryDate = getExpiryDateForDemand(demand);
		
		return BillDetailV2.builder()
				.billAccountDetails(new ArrayList<>(taxCodeAccountdetailMap.values()))
				.amount(totalAmountForDemand)
				.expiryDate(billExpiryDate)
				.demandId(demand.getId())
				.fromPeriod(startPeriod)
				.toPeriod(endPeriod)
				.tenantId(tenantId)
				.additionalDetails(demand.getAdditionalDetails())
				.build();
	}

	/**
	 * @param demand
	 * 
	 * @return expiryDate
	 */
	private Long getExpiryDateForDemand(Demand demand) {

		Long billExpiryPeriod = demand.getBillExpiryTime();
		Long fixedBillExpiryDate = demand.getFixedBillExpiryDate();
		Calendar cal = Calendar.getInstance();

		if (!ObjectUtils.isEmpty(fixedBillExpiryDate) && fixedBillExpiryDate > cal.getTimeInMillis()) {
			cal.setTimeInMillis(fixedBillExpiryDate);
		} else if (!ObjectUtils.isEmpty(billExpiryPeriod) && 0 < billExpiryPeriod) {
			cal.setTimeInMillis(cal.getTimeInMillis() + billExpiryPeriod);
		}

		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
		return cal.getTimeInMillis();
	}

	/**
	 * Checks if a demand has zero net amount (taxAmount - collectionAmount = 0)
	 *
	 * @param demand the demand to check
	 * @return true if demand has zero net amount, false otherwise
	 */
	private boolean isZeroAmountDemand(Demand demand) {
		BigDecimal totalTax = BigDecimal.ZERO;
		BigDecimal totalCollection = BigDecimal.ZERO;

		for (DemandDetail detail : demand.getDemandDetails()) {
			totalTax = totalTax.add(detail.getTaxAmount());
			totalCollection = totalCollection.add(detail.getCollectionAmount());
		}

		return totalTax.subtract(totalCollection).compareTo(BigDecimal.ZERO) == 0;
	}

	/**
	 * Checks if an advance demand with zero amount was created recently
	 * and should be allowed to generate a bill.
	 *
	 * This enables customers to make multiple advance payments within the same tax period
	 * by allowing bill generation for zero-amount advance demands created within a
	 * configurable time window (default: 24 hours).
	 *
	 * @param demand the demand to check
	 * @return true if demand is a recent zero-amount advance, false otherwise
	 */
	private boolean isRecentZeroAmountAdvanceDemand(Demand demand) {
		// Check 1: Must be an advance demand
		if (!Boolean.TRUE.equals(demand.getIsAdvance())) {
			return false;
		}

		// Check 2: Must have zero amount
		if (!isZeroAmountDemand(demand)) {
			return false;
		}

		// Check 3: Must be created within configured time window (default 24 hours)
		Long createdTime = demand.getAuditDetails().getCreatedTime();
		Long currentTime = System.currentTimeMillis();
		Long allowedWindowMillis = appProps.getAdvanceZeroAmountAllowHours() * 60 * 60 * 1000L;
		Long thresholdTime = currentTime - allowedWindowMillis;

		boolean isRecent = createdTime >= thresholdTime;

		if (isRecent) {
			log.info("Allowing bill generation for recent zero-amount advance demand: " +
					 "demandId={}, consumerCode={}, advanceIndex={}, createdTime={}, hoursAgo={}",
					 demand.getId(), demand.getConsumerCode(), demand.getAdvanceIndex(),
					 createdTime, (currentTime - createdTime) / (60 * 60 * 1000));
		}

		return isRecent;
	}

	/**
	 * creates/ updates bill-account details based on the tax-head code in
	 * taxCodeAccDetailMap
	 *
	 * @param startPeriod
	 * @param endPeriod
	 * @param tenantId
	 * @param taxCodeAccDetailMap
	 * @param demandDetail
	 * @param taxHead
	 * @param billDetailId
	 * @param glCode GL Code from MDMS (can be null for backward compatibility)
	 */
	private void addOrUpdateBillAccDetailInTaxCodeAccDetailMap(Map<String, BillAccountDetailV2> taxCodeAccDetailMap,
			DemandDetail demandDetail, TaxHeadMaster taxHead, String billDetailId, String glCode) {

		BigDecimal newAmountForAccDeatil = demandDetail.getTaxAmount().subtract(demandDetail.getCollectionAmount());
		/*
		 * BAD - BillAccountDetail
		 * 
		 * To handle repeating tax-head codes in demand
		 * 
		 * And merge them in to single BAD
		 * 
		 * if taxHeadCode found in map then add the amount to existing BAD
		 * 
		 * else create and add a new BAD
		 */
		if (taxCodeAccDetailMap.containsKey(taxHead.getCode())) {

			BillAccountDetailV2 existingAccDetail = taxCodeAccDetailMap.get(taxHead.getCode());
			BigDecimal existingAmtForAccDetail = existingAccDetail.getAmount();
			existingAccDetail.setAmount(existingAmtForAccDetail.add(newAmountForAccDeatil));

		} else {

			BillAccountDetailV2 accountDetail = BillAccountDetailV2.builder()
					.demandDetailId(demandDetail.getId())
					.tenantId(demandDetail.getTenantId())
					.id(UUID.randomUUID().toString())
					.adjustedAmount(BigDecimal.ZERO)
					.taxHeadCode(taxHead.getCode())
					.glCode(glCode)
					.amount(newAmountForAccDeatil)
					.order(taxHead.getOrder())
					.billDetailId(billDetailId)
					.build();

			taxCodeAccDetailMap.put(taxHead.getCode(), accountDetail);
		}
	}

	/**
	 * Fetches the tax-head master data for the given tax-head codes
	 * 
	 * @param demands  list of demands for which tax-heads needs to searched
	 * @param tenantId tenant-id of the request
	 * @param info     RequestInfo object
	 * @return returns a map of tax-head code as key and tax-head object as value
	 */
	private Map<String, TaxHeadMaster> getTaxHeadMaster(Set<String> taxHeadCodes, String tenantId, RequestInfo info) {

		TaxHeadMasterCriteria taxHeadCriteria = TaxHeadMasterCriteria.builder().tenantId(tenantId).code(taxHeadCodes)
				.build();
		List<TaxHeadMaster> taxHeads = taxHeadService.getTaxHeads(taxHeadCriteria, info).getTaxHeadMasters();

		if (taxHeads.isEmpty())
			throw new CustomException("EG_BS_TAXHEADCODE_EMPTY", "No taxhead masters found for the given codes");

		return taxHeads.stream().collect(Collectors.toMap(TaxHeadMaster::getCode, Function.identity()));
	}

	/**
	 * Extracts GLCode from TaxHeadMaster if available
	 * Returns null if no GLCode configured (backward compatible)
	 *
	 * @param taxHead TaxHeadMaster object containing glCodes
	 * @return GLCode string or null if not found
	 */
	private String extractGlCodeFromTaxHead(TaxHeadMaster taxHead) {

		// NULL CHECK: TaxHead might not have glCodes (backward compatible)
		if (taxHead == null || taxHead.getGlCodes() == null || taxHead.getGlCodes().isEmpty()) {
			log.debug("No GLCodes found for taxHead: {}",
				taxHead != null ? taxHead.getCode() : "null");
			return null;
		}

		Long currentTime = System.currentTimeMillis();

		// Filter by date validity
		Optional<GlCodeMaster> validGlCode = taxHead.getGlCodes().stream()
			.filter(gl -> gl != null && gl.getFromDate() != null && gl.getToDate() != null)
			.filter(gl -> gl.getFromDate() <= currentTime && gl.getToDate() >= currentTime)
			.findFirst();

		if (validGlCode.isPresent()) {
			String glCode = validGlCode.get().getGlCode();
			log.info("GLCode extracted for taxHead {}: {}", taxHead.getCode(), glCode);
			return glCode;
		}

		// Fallback: Use first GLCode if no date match
		GlCodeMaster firstGlCode = taxHead.getGlCodes().get(0);
		if (firstGlCode != null && firstGlCode.getGlCode() != null) {
			log.warn("No date-valid GLCode found for taxHead: {}, using first available", taxHead.getCode());
			return firstGlCode.getGlCode();
		}

		return null;
	}

	/**
	 * To Fetch the businessServiceDetail master based on the business codes
	 * 
	 * @param businessService
	 * @param tenantId
	 * @param requestInfo
	 * @return returns a map with business code and businessDetail object
	 */
	private Map<String, BusinessServiceDetail> getBusinessService(Set<String> businessService, String tenantId, RequestInfo requestInfo) {
		List<BusinessServiceDetail> businessServiceDetails = businessServDetailService.searchBusinessServiceDetails(BusinessServiceDetailCriteria.builder().businessService(businessService).tenantId(tenantId).build(), requestInfo)
				.getBusinessServiceDetails();
		return businessServiceDetails.stream().collect(Collectors.toMap(BusinessServiceDetail::getCode, Function.identity()));
	}
	
	public BillResponseV2 getBillResponse(List<BillV2> bills) {
		BillResponseV2 billResponse = new BillResponseV2();
		billResponse.setBill(bills);
		return billResponse;
	}

	/**
	 * Publishes the bill request to kafka topic and returns bill response
	 * 
	 * @param billRequest
	 * @return billResponse object containing bills from the request
	 */
	public BillResponseV2 sendBillToKafka(BillRequestV2 billRequest) {

		try {
			kafkaTemplate.send(appProps.getCreateBillTopic(), appProps.getCreateBillTopicKey(), billRequest);
		} catch (Exception e) {
			log.debug("BillService createAsync:" + e);
			throw new CustomException("EGBS_BILL_SAVE_ERROR", e.getMessage());

		}
		return getBillResponse(billRequest.getBills());
	}
	
	public BillResponseV2 create(BillRequestV2 billRequest) {

		if (!CollectionUtils.isEmpty(billRequest.getBills()))
			billRepository.saveBill(billRequest);
		return getBillResponse(billRequest.getBills());
	}
	
	public static List<String> getOwnerFieldsPlainAccessList() {

		if (ownerPlainRequestFieldsList == null) {
			
			ownerPlainRequestFieldsList = new ArrayList<>();
			ownerPlainRequestFieldsList.add("mobileNumber");
			ownerPlainRequestFieldsList.add("guardian");
			ownerPlainRequestFieldsList.add("fatherOrHusbandName");
			ownerPlainRequestFieldsList.add("correspondenceAddress");
			ownerPlainRequestFieldsList.add("userName");
			ownerPlainRequestFieldsList.add("name");
			ownerPlainRequestFieldsList.add("gender");
			ownerPlainRequestFieldsList.add("permanentAddress");
		}
		return ownerPlainRequestFieldsList;
	}
	
}
