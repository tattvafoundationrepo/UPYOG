package digit.repository.querybuilder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.management.Query;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import digit.web.models.DashboardCriteria;
import digit.web.models.SchemeApplicationSearchCriteria;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SchemeApplicationQueryBuilder {


    // Base query to select fields from SchemeApplication table
    private static final String BASE_QUERY = """
            SELECT id as id, applicationNumber as applicationNumber, userid as userid, tenantid as tenantid, optedId as optedId, \
            ApplicationStatus as ApplicationStatus, VerificationStatus as VerificationStatus, FirstApprovalStatus as FirstApprovalStatus, \
            RandomSelection as RandomSelection, FinalApproval as FinalApproval, Submitted as Submitted, ModifiedOn as ModifiedOn, \
            CreatedBy as CreatedBy, ModifiedBy as ModifiedBy \
            """;

    private static final String INITAIL_QUERY = """
            SELECT ewpv.id as id, ebas.aadharname as Name, ebas.ward as Ward, ebs."name" as SchemeName, ebc.coursename as CourseName, ebm."name" as MachineName, \
            ewpv.modulename as ModuleName, ebas.applicationnumber as ApplicationNumber, ebas.citytownvillage as City, ebas.zone as Zone, \
            ewpv.lastmodifiedtime as lastmodifiedtime, ewsv.state as state, \
            """;

    // Query to select fields from Address table
    private static final String ADDRESS_SELECT_QUERY = """
            addr.ID as addr_id, addr.userid as addr_userid, addr.tenantid as addr_tenantid, \
            addr.Address1 as addr_Address1, addr.Address2 as addr_Address2, addr.Location as addr_Location, \
            addr.Ward as addr_Ward, addr.City as addr_City, addr.District as addr_District, addr.State as addr_State, \
            addr.Country as addr_Country, addr.Pincode as addr_Pincode, \
            """;

    // Query to select fields from User table
    private static final String USER_SELECT_QUERY = """
            u.username as user_username, u.name as user_name, u.emailid as user_email, \
            u.mobilenumber as user_mobile, u.gender as user_gender, u.aadhaarnumber as user_aadhar \
            """;

    // From clause with join between SchemeApplication, Address, and User tables
    private static final String FROM_TABLES = """
            FROM eg_bmc_UserSchemeApplication 
            """;

    private static final String FROM_MULTITABLES = """
            FROM eg_wf_processinstance_v2 ewpv \
            left join eg_wf_state_v2 ewsv on ewpv.status = ewsv.uuid \
            left join eg_bmc_application_snapshot ebas on ebas.applicationnumber = ewpv.businessid \
            left join eg_bmc_schemes ebs on ebs.id = ebas.optedid \
            left join eg_bmc_machines ebm on ebm.id = ebas.machineid \
            left join eg_bmc_courses ebc on ebc.id = ebas.courseid \
            """;

    private static final String BMCMODULE_QUERY = """
            WHERE ewpv.modulename = 'BMC' \
            """;

    private static final String RANK_STATUS = """
            RANK() OVER (PARTITION BY ewpv.businessid ORDER BY ewpv.createdtime DESC) AS rank1 \
            """;

    private static final String STATUS_QUERY = """
                                ewpv.businessid IN (SELECT businessid \
                                FROM (SELECT ewpv.businessid, \
                                RANK() OVER (PARTITION BY ewpv.businessid ORDER BY ewpv.createdtime DESC) AS rank1 \
                                FROM eg_wf_processinstance_v2 ewpv) ranked_subquery \
                                WHERE rank1 = 1) \
                                """;
            
    // Order by clause to order results by the ModifiedOn field
    private static final String ORDERBY_MODIFIEDTIME = "ORDER BY ModifiedOn DESC ";

    /**
     * Builds the SQL query for searching SchemeApplications based on the given search criteria.
     *
     * @param criteria The search criteria for SchemeApplications.
     * @param preparedStmtList The list to hold the parameters for the prepared statement.
     * @return The constructed SQL query.
     */
    public String getSchemeApplicationSearchQuery(SchemeApplicationSearchCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);
        query.append(ADDRESS_SELECT_QUERY);
        query.append(USER_SELECT_QUERY);
        query.append(FROM_TABLES);

        // Add where clause for tenant ID if it is not empty
        if (!ObjectUtils.isEmpty(criteria.getTenantId())) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" tenantid = ? ");
            preparedStmtList.add(criteria.getTenantId());
        }

        // Add where clause for IDs if they are not empty
        if (!CollectionUtils.isEmpty(criteria.getIds())) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" id IN ( ").append(createQuery(criteria.getIds())).append(" ) ");
            addToPreparedStatement(preparedStmtList, criteria.getIds());
        }

        // Add where clause for application status if it is not empty
        if (!ObjectUtils.isEmpty(criteria.getApplicationStatus())) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" ApplicationStatus = ? ");
            preparedStmtList.add(criteria.getApplicationStatus());
        }

        if (!ObjectUtils.isEmpty(criteria.getVerificationStatus())) {
            addClauseIfRequired(query, preparedStmtList);
            if(criteria.getVerificationStatus()){
                query.append(" VerificationStatus = " + String.valueOf(criteria.getVerificationStatus()) + " ");
            } else {
                query.append(" VerificationStatus = " + String.valueOf(criteria.getVerificationStatus()) + " ");
            }
        }

        // Add where clause for application number if it is not empty
        if (!ObjectUtils.isEmpty(criteria.getApplicationNumber())) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" applicationNumber = ? ");
            preparedStmtList.add(criteria.getApplicationNumber());
        }
        
        // Append the order by clause to the query
        //query.append(ORDERBY_MODIFIEDTIME);

        return query.toString();
    }
    /** 
    * @param criteria The search criteria for SchemeApplications.
     * @param preparedStmtList The list to hold the parameters for the prepared statement.
     * @return The constructed SQL query.
     */
    public String getDashboardApplicationSearchQuery(DashboardCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(INITAIL_QUERY);
        query.append(RANK_STATUS);
        query.append(FROM_MULTITABLES);
        query.append(BMCMODULE_QUERY);

        boolean flag = false;


        if (!ObjectUtils.isEmpty(criteria.getState())) {
            
                switch (String.valueOf(criteria.getState())) {
                    case "APPROVED":
                        query.append(" AND ewsv.state = '" + String.valueOf(criteria.getState()) + "' ");
                        break;
                    case "APPLIED":
                        query.append(" AND ewsv.state = '" + String.valueOf(criteria.getState()) + "' AND ");
                        query.append(STATUS_QUERY);
                    case "SELECTED":
                        query.append(" AND ewsv.state = '" + String.valueOf(criteria.getState()) + "' AND ");
                        query.append(STATUS_QUERY);
                    case "VERIFIED":
                        query.append(" AND ewsv.state = '" + String.valueOf(criteria.getState()) + "' AND ");
                        query.append(STATUS_QUERY);
                    case "REJECTED":
                        query.append(" AND ewsv.state = '" + String.valueOf(criteria.getState()) + "' AND ");
                        query.append(STATUS_QUERY);
                    default:
                        break;
                }
        }

        if (!ObjectUtils.isEmpty(criteria.getCity())) {
            
                query.append(" AND ");
                query.append(" ebas.citytownvillage = '" + String.valueOf(criteria.getCity()) + "' ");
            
        }

        if (!ObjectUtils.isEmpty(criteria.getZone())) {
            
                query.append(" AND ");
                query.append(" ebas.zone = '" + String.valueOf(criteria.getZone()) + "' ");
            
        }

       
        if (!ObjectUtils.isEmpty(criteria.getSchemeId())) {
            
                query.append(" AND ");
                query.append(" ebs.id = " + String.valueOf(criteria.getSchemeId()) + " ");
            
        }

        if (!ObjectUtils.isEmpty(criteria.getWard())) {
            
                query.append(" AND ");
                query.append(" ebas.ward = '" + String.valueOf(criteria.getWard()) + "' ");
            
        }

        if (!ObjectUtils.isEmpty(criteria.getCourseId())) {
            
                query.append(" AND ");
                query.append(" ebas.courseid = " + String.valueOf(criteria.getCourseId()) + " ");
            
        }

        if (!ObjectUtils.isEmpty(criteria.getMachineId())) {
           
                query.append(" AND ");
                query.append(" ebas.machineid = " + String.valueOf(criteria.getMachineId()) + " ");
            
        }

        if(!ObjectUtils.isEmpty(criteria.getCreatedDate()) && ObjectUtils.isEmpty(criteria.getEndDate())){
            
                query.append(" AND ");
                query.append(" ewpv.lastmodifiedtime BETWEEN " + getMillies(String.valueOf(criteria.getCreatedDate())) + " AND " + System.currentTimeMillis()+ " ");
            
        } else if(!ObjectUtils.isEmpty(criteria.getCreatedDate()) && !ObjectUtils.isEmpty(criteria.getEndDate())){
            
                query.append(" AND ");
                query.append(" ewpv.lastmodifiedtime BETWEEN " + getMillies(String.valueOf(criteria.getCreatedDate())) + " AND " +
                getMillies(String.valueOf(criteria.getEndDate()))+ " ");
            
        }
        
        // Append the order by clause to the query
        query.append(" ORDER BY ewpv.lastmodifiedtime DESC ");

        return query.toString();
    }

    private long getMillies(String lastmodifieddate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(lastmodifieddate, formatter);

        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        log.info(lastmodifieddate);
        long millis = date.getTime();
        log.info(String.valueOf(millis));
        return millis;
    }

    /**
     * Adds a clause to the query if required based on the state of the prepared statement list.
     *
     * @param query The query string builder.
     * @param preparedStmtList The list of parameters for the prepared statement.
     */
    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {
        if (preparedStmtList.isEmpty()) {
            query.append(" WHERE ");
        } else {
            query.append(" AND ");
        }
    }

    /**
     * Creates a query string with placeholders for the given list of IDs.
     *
     * @param ids The list of IDs.
     * @return The query string with placeholders.
     */
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

    /**
     * Adds the given list of IDs to the prepared statement list.
     *
     * @param preparedStmtList The list of parameters for the prepared statement.
     * @param ids The list of IDs to be added.
     */
    private void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
        ids.forEach(preparedStmtList::add);
    }
}
