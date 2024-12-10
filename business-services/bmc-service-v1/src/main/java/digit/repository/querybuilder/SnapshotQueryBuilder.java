package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;
import digit.web.models.SnapshotSearchcriteria;

@Component
public class SnapshotQueryBuilder {
   
    private static final String BASE_QUERY = """
               SELECT 
               eba.housenobldgapt, eba.subdistrict, eba.postoffice, 
               eba.landmark, eba.country, eba."type", eba.streetroadline, eba.citytownvillage, 
               eba.arealocalitysector, eba.district, eba.state, eba.pincode, 
               eba.aadharref,eba.aadharfathername, eba.aadharname, eba.aadharmobile, UPPER(eba.gender) as gender, to_timestamp(eba.aadhardob/1000)::date as aadhardob,
               eba.transgenderid, eba.zone, eba.block, eba.ward ,eba.occupation,eba.income, eba.divyangcardid, eba.divyangpercent,
               UPPER(ebc."name") as caste ,ebc.id as casteid, \
               UPPER(ebr."name") as religion ,ebr.id as religionid , \
               ebd2.id as divyangid, UPPER(ebd2."name") as divyangtype,eba.divyangpercent ,eba.divyangcardid, 

               eba.user_documents, 
               eba.user_banks,
               eba.user_qualifications, 

               eba.applicationnumber, 
               eba.machineid, eba.courseid, eba.optedid, eba.applicationstatus, eba.verificationstatus, 
               eba.firstapprovalstatus, eba.randomselection, eba.finalapproval, eba.submitted, 
               eba.agreetopay, eba."statement", eba.userid, eba.tenantid, eba.createdon, eba.createdby, 
               eba.modifiedon, eba.modifiedby
              
            """;

    private static final String FROM_TABLES = """
            FROM public.eg_bmc_application_snapshot eba
            left join eg_bmc_caste ebc on ebc.id = eba.casteid \
            left join eg_bmc_religion ebr on ebr.id =eba.religionid \
            left join eg_bmc_divyang ebd2 on ebd2.id = eba.divyangid \
            """;

    
    private void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
        ids.forEach(preparedStmtList::add);
    }

    public String getApplicationSearchQuery(SnapshotSearchcriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);
        query.append(FROM_TABLES);

        if (criteria.getUserId() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" eba.userid = ? ");
            preparedStmtList.add(criteria.getUserId());
        }

        if (criteria.getTenantId() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" eba.tenantid = ? ");
            preparedStmtList.add(criteria.getTenantId());
        }

        if (criteria.getApplicationNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" eba.applicationnumber = ? ");
            preparedStmtList.add(criteria.getApplicationNumber());
        }

        return query.toString();
    }

    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {
        if (preparedStmtList.isEmpty()) {
            query.append(" WHERE ");
        } else {
            query.append(" AND ");
        }
    }

    private String createQuery(List<String> ids) {
        StringBuilder builder = new StringBuilder();
        int length = ids.size();
        for (int i = 0; i < length; i++) {
            builder.append(" ?");
            if (i != length - 1)
                builder.append(",");
        }
        return builder.toString();
    }


}
