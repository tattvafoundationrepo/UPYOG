package digit.repository.querybuilder;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.springframework.stereotype.Component;

import digit.web.models.GetPeRequest;

@Component
public class PeEnquiryQueryBuilder {

    private static final String BASE_QUERY = """

            select  epe.newcode, epe.oldcode,to_timestamp(epe.orderdate/1000)::date as orderdate, epe.orderno, 
            epe.cedesig as empDesig, epe.cedept as empDept, epe.ceempcode, epe.enqsubject,eed.emplname
            
                 """;
    private static final String ECL_query = """
             ,ecl.cecode, ecl.cename, ecl.cedept, ecl.cedesig, ecl.cesuspended, ecl.cesuspensionorder, ecl.cestatus, ecl.enqordertype, 
                  ecl.casetype,ecl.mobileno, ecl.email
            
            """;         

    private static final String FROM_TABLES = """
            from eg_ebinder_pe_enquiry epe 
            left join eg_ebinder_ce_list ecl on epe.id = ecl.peenquiryid
            left join eg_employee_data eed on epe.ceempcode = eed.empcode

         """;


    public String getEnquirySearchQuery(GetPeRequest req, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);
        
         if(!ObjectUtils.isEmpty(req.getEnqId()) && req.getEnqId() != null){
            query.append(ECL_query);
            query.append(FROM_TABLES);
            addClauseIfRequired(query,preparedStmtList);
            query.append(" epe.newcode = ? ");
            preparedStmtList.add(req.getEnqId());
         }else{
            query.append(FROM_TABLES);
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

                 

}
