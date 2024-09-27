package digit.repository.querybuilder;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.springframework.stereotype.Component;

import digit.web.models.GetPeRequest;

@Component
public class PeEnquiryQueryBuilder {

    private static final String BASE_QUERY = """

            with data as(
                    Select
                    *,
                    RANK() over (PARTITION BY businessid ORDER BY createdtime DESC) as rank1
                    from eg_wf_processinstance_v2
                    )
                           select  epe.newcode, epe.oldcode,to_timestamp(epe.orderdate/1000)::date as orderdate, epe.orderno, 
                           epe.cedesig as empDesig, epe.cedept as empDept, epe.ceempcode, epe.enqsubject
                           , CONCAT(eed.empfname, ' ', eed.empmname, ' ', eed.emplname) AS emplname,
                            ROW_NUMBER() OVER (PARTITION BY epe.newcode ORDER BY epe.newcode) AS rn 
            """;
    private static final String ECL_query = """
             ,ecl.cecode, ecl.cename, ecl.cedept, ecl.cedesig, ecl.cesuspended, ecl.cesuspensionorder, ecl.cestatus, ecl.enqordertype, 
                  ecl.casetype,ecl.mobileno, ecl.email
            """;         
    private static final String SUBMIT_QUERY = """
            ,epsr.reportnumber , to_timestamp(epsr.reportsubmissiomdate/1000)::date as submissiondate,epsr.comment
            ,epsr.casetype,epsr.ordertype
            """;
    private static final String FROM_TABLES = """
            FROM data b
            %s
            left join eg_ebinder_ce_list ecl on epe.id = ecl.peenquiryid
            left join eg_employee_data eed on epe.ceempcode = eed.empcode
            
         """;
         private static final String FROM_TABLES_0 = """   
            INNER JOIN eg_ebinder_pe_enquiry epe  ON b.businessid = epe.newcode
                 """;
         private static final String FROM_TABLES_1 = """        
            INNER JOIN eg_ebinder_pe_submission_report epsr ON b.businessid = epsr.penumber
             left join eg_ebinder_pe_enquiry epe ON  epsr.penumber = epe.newcode 
                    """;        

    public String getEnquirySearchQuery(GetPeRequest req, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);
        if (req.getType() == 0) {
            if (!ObjectUtils.isEmpty(req.getEnqId()) && req.getEnqId() != null) {
                query.append(ECL_query);
                query.append(String.format(FROM_TABLES,FROM_TABLES_0));
                addClauseIfRequired(query, preparedStmtList);
                query.append(" epe.newcode = ? ");
                preparedStmtList.add(req.getEnqId());
            } else {
                query.append(String.format(FROM_TABLES,FROM_TABLES_0));
            }
            addClauseIfRequired(query, preparedStmtList);
            query.append("b.action not in ('APPROVE') and b.businessservice = 'PE-Service' and b.rank1 = 1");
        }
        if (req.getType() == 1) {
         
                if (!ObjectUtils.isEmpty(req.getEnqId()) && req.getEnqId() != null) {
                    query.append(SUBMIT_QUERY);
                    query.append(ECL_query);
                    query.append(String.format(FROM_TABLES,FROM_TABLES_1));
                    addClauseIfRequired(query, preparedStmtList);
                    query.append(" epsr.penumber = ? ");
                    preparedStmtList.add(req.getEnqId());
                } else {
                    query.append(SUBMIT_QUERY);
                    query.append(String.format(FROM_TABLES,FROM_TABLES_1));
                }
                addClauseIfRequired(query, preparedStmtList);
                query.append("b.action  in ('APPROVE') and b.businessservice = 'PE-Service' and b.rank1 = 1");

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
