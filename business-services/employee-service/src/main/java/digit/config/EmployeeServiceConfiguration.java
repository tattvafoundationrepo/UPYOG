package digit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.TimeZone;

@Configuration
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeServiceConfiguration {
    @Value("${app.timezone}")
    private String timeZone;

    @PostConstruct
    public void initialize() {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
    }

    @Bean
    @Autowired
    public MappingJackson2HttpMessageConverter jacksonConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }



    //HRMS
    @Value("${egov.hrms.host}")
    private String hrmsHost;

    @Value("${egov.hrms.search.endpoint}")
    private String hrmsEndPoint;

    @Value("${egov.hrms.create.endpoint}")
    private String hrmsCreateEndpoint;

    @Value("${egov.SAP.UserName}")
    private String UserName;

    @Value("${egov.SAP.password}")
    private String password;

    @Value("${egov.SAP.search.employee.URL}")
    private String URL;


    // KAFKA
    @Value("${employee-service.default.offset}")
    private Integer defaultOffset;

    @Value("${employee-service.default.limit}")
    private Integer defaultLimit;

    @Value("${employee-service.search.max.limit}")
    private Integer maxLimit;

    @Value("${employee-service-create-topic}")
    private String createTopic;

}
