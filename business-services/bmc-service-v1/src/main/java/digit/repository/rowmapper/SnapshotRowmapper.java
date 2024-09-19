package digit.repository.rowmapper;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import digit.bmc.model.AadharUser;
import digit.bmc.model.Caste;
import digit.bmc.model.Divyang;
import digit.bmc.model.UserOtherDetails;
import digit.bmc.model.VerificationDetails;

import digit.web.models.BankDetails;
import digit.web.models.Religion;
import digit.web.models.user.DivyangDetails;
import digit.web.models.user.DocumentDetails;
import digit.web.models.user.PinCodeDto;
import digit.web.models.user.QualificationDetails;
import digit.web.models.user.QualificationSave;
import digit.web.models.user.UpdatedDocument;
import digit.web.models.user.UserAddressDetails;
import digit.web.models.user.UserDetails;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Component
public class SnapshotRowmapper implements ResultSetExtractor<List<VerificationDetails>> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<VerificationDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, VerificationDetails> verificationDetailsMap = new LinkedHashMap<>();
        Map<String, UserDetails> userDetailsMap = new LinkedHashMap<>();
        List<UserDetails> userDetailList = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        Set<String> columns = new HashSet<>();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            columns.add(rsmd.getColumnName(i).toLowerCase());
        }

        while (rs.next()) {
            String applicationNumber = rs.getString("applicationnumber");
            VerificationDetails verificationDetails = verificationDetailsMap.get(applicationNumber);

                verificationDetails = new VerificationDetails();
                verificationDetails.setApplicationNumber(applicationNumber);
                verificationDetails.setAgreeToPay(rs.getBoolean("agreetopay"));
                verificationDetails.setStatement(rs.getBoolean("statement"));
                verificationDetails.setUserId(rs.getLong("userid"));
                verificationDetails.setTenantId(rs.getString("tenantid"));
               // verificationDetails.setScheme(rs.getString("scheme"));
                String userId = rs.getString("userid");
                UserDetails userDetails = userDetailsMap.get(userId);
            if (userDetails == null) {
                Caste caste = null;
                if (columns.contains("casteid") && columns.contains("caste") && rs.getObject("casteid") != null
                        && rs.getString("caste") != null) {
                    caste = Caste.builder()
                            .id(rs.getLong("casteid"))
                            .name(rs.getString("caste"))
                            .build();
                }

                Religion religion = null;
                if (columns.contains("religionid") && columns.contains("religion") && rs.getObject("religionid") != null
                        && rs.getString("religion") != null) {
                    religion = Religion.builder()
                            .id(rs.getLong("religionid"))
                            .name(rs.getString("religion"))
                            .build();
                }

                PinCodeDto pin = PinCodeDto.builder().value(rs.getString("pincode")).build();
                UserAddressDetails address = null;
                if (columns.contains("citytownvillage") && columns.contains("pincode")) {
                    address = UserAddressDetails.builder()
                          //  .id(rs.getLong("id"))
                            .areaLocalitySector(rs.getString("arealocalitysector"))
                            .cityTownVillage(rs.getString("citytownvillage"))
                            .district(rs.getString("district"))
                            .landmark(rs.getString("landmark"))
                            .houseNoBldgApt(rs.getString("housenobldgapt"))
                            .subDistrict(rs.getString("subdistrict"))
                            .postOffice(rs.getString("postoffice"))
                            .country(rs.getString("country"))
                            .streetRoadLine(rs.getString("streetroadline"))
                            .pincode(pin)
                            .state(rs.getString("state"))
                            .build();
                }

                DivyangDetails divyangDetails = null;
                if (columns.contains("divyangpercent") && columns.contains("divyangtype")
                        && columns.contains("divyangcardid")) {
                    Divyang divyang = Divyang.builder()
                            .id(rs.getLong("divyangid"))
                            .name(rs.getString("divyangtype"))
                            .build();
                    divyangDetails = DivyangDetails.builder()
                            .divyangpercent(rs.getDouble("divyangpercent"))
                            .divyangcardid(rs.getString("divyangcardid"))
                            .divyangtype(divyang).build();

                }
                String[] result = splitSalutation(rs.getString("aadharname"));
                AadharUser aadharUser = null;
                if (columns.contains("aadhardob") && columns.contains("aadharfathername")
                        && columns.contains("aadharmobile") && columns.contains("aadharname")
                        && columns.contains("gender")) {
                    aadharUser = AadharUser.builder()
                            .aadharRef(rs.getString("aadharref"))
                            .aadharName(result[1])
                            .aadharDob(rs.getDate("aadhardob"))
                            .aadharFatherName(rs.getString("aadharfathername"))
                            .aadharMobile(rs.getString("aadharmobile"))
                            .gender(rs.getString("gender"))
                            .build();
                }

                UserOtherDetails uod = UserOtherDetails.builder()
                        .caste(caste)
                        .religion(religion)
                        .transgenderId(rs.getString("transgenderid"))
                        .block(rs.getString("block"))
                        .zone(rs.getString("zone"))
                        .ward(rs.getString("ward"))
                        .occupation(rs.getString("occupation"))
                        .income(rs.getDouble("income"))
                        .createdBy(rs.getString("createdby"))
                        .modifiedBy(rs.getString("modifiedby"))
                        .divyangCardId(rs.getString("divyangcardid"))
                        .divyangPercent(rs.getDouble("divyangpercent"))
                        .createdOn(rs.getLong("createdon"))
                        .modifiedOn(rs.getLong("modifiedon"))
                        .build();
                 Gson gson = new Gson();
                 List<UpdatedDocument> doc = new ArrayList<>();
                 List<DocumentDetails> documents = new ArrayList<>();
                if (columns.contains("user_documents") && rs.getString("user_documents") != null) {
                    try {
                      //  doc = objectMapper.readValue(rs.getString("user_documents"), new TypeReference<List<UpdatedDocument>>() {});
                      Type listType = new TypeToken<List<UpdatedDocument>>() {}.getType();
                      doc = gson.fromJson(rs.getString("user_documents"), listType);
                        for(UpdatedDocument d : doc){
                            DocumentDetails details = new DocumentDetails();
                            if(d.getDocumentDetails()!=null){
                              details.setAvailable(d.getDocumentDetails().getAvailable());
                              details.setDocumentId(d.getDocumentDetails().getDocumentId());
                              details.setDocumentName(d.getDocumentDetails().getDocumentName());

                            }
                            details.setDocumentNo(d.getDocumentNumber());
                            documents.add(details);
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }

                List<BankDetails> banks = new ArrayList<>();
                if (columns.contains("user_banks") && rs.getString("user_banks") != null) {
                    try {
                        banks = objectMapper.readValue(rs.getString("user_banks"), new TypeReference<List<BankDetails>>() {});
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }

                List<QualificationSave> qualifications = new ArrayList<>();
                List<QualificationDetails> qd = new ArrayList<>();
                if (columns.contains("user_qualifications") && rs.getString("user_qualifications") != null) {
                    try {
                  //      qualifications = objectMapper.readValue(rs.getString("user_qualifications"), new TypeReference<List<QualificationSave>>() {});
                        Type listType = new TypeToken<List<QualificationSave>>() {}.getType();
                        qualifications = gson.fromJson(rs.getString("user_qualifications"), listType);
                        for(QualificationSave q : qualifications){
                            QualificationDetails details = new QualificationDetails();
                            if(q.getBoardValue() != null)
                               details.setBoard(q.getBoardValue().getValue());
                            if(q.getQualificationDetails()!= null){
                                details.setQualificationId(q.getQualificationDetails().getId());
                                details.setQualification(q.getQualificationDetails().getQualification());
                            }
                            if(q.getYearOfPassingValue()!=null)
                                details.setYearOfPassing(q.getYearOfPassingValue().getValue());
                            details.setPercentage(q.getPercentage());    
                            qd.add(details);
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }  
                
                userDetails = UserDetails.builder()
                .title(result[0])
                .aadharUser(aadharUser)
                .userOtherDetails(uod)
                .address(address)
                .userID(columns.contains("userid") ? rs.getLong("userid") : null)
                .bankDetail(banks)
                .documentDetails(documents)
                .divyang(divyangDetails)
                .qualificationDetails(qd)
                .build();
                userDetailList.add(userDetails);
                verificationDetails.setUserDetails(userDetailList);
                verificationDetailsMap.put(applicationNumber, verificationDetails);   
            }
        }   
        return new ArrayList<>(verificationDetailsMap.values());
    }
    public static String[] splitSalutation(String fullName) {
        if (fullName == null || !fullName.contains(".")) {
            return new String[]{"", fullName != null ? fullName.trim() : ""};
        }
        String[] parts = fullName.split("\\.", 2); 
        String salutation = parts[0].trim(); 
        String name = parts.length > 1 ? parts[1].trim() : ""; 
        return new String[]{salutation, name};
    }



}
