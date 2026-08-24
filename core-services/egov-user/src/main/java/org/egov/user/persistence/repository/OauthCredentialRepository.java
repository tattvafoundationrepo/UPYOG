package org.egov.user.persistence.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

/**
 * Reads the encrypted service-account credentials used to mint a privileged token for
 * server-to-server calls (currently the HRMS employee registration in
 * {@link org.egov.user.domain.service.EmployeeRegistrationService}).
 *
 * The {@code eg_bmc_oauth} table is created by employee-service's flyway migration
 * (V20260424172507__Creds_ddl.sql) in the same database this service points at, so no
 * migration is duplicated here — this is a read-only consumer of that table.
 *
 * NOTE: moved here from employee-service (digit.repository.EmployeeRepository#getOauthDetails)
 * together with the employee registration flow. Query text is unchanged.
 */
@Repository
@Slf4j
public class OauthCredentialRepository {

    private static final String SELECT_OAUTH_CREDENTIALS =
            "SELECT enc_username, enc_password, tenantid FROM eg_bmc_oauth";

    private final JdbcTemplate jdbcTemplate;

    public OauthCredentialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Rows of {@code enc_username / enc_password / tenantid}; the first row is used. */
    public List<Map<String, Object>> getOauthDetails() {
        log.debug("Query: {}", SELECT_OAUTH_CREDENTIALS);
        return jdbcTemplate.queryForList(SELECT_OAUTH_CREDENTIALS);
    }
}
