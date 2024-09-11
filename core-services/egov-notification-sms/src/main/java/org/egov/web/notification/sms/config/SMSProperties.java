package org.egov.web.notification.sms;

import lombok.Data;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.util.*;
import java.util.regex.Pattern;

@Configuration
@Data
@Service
public class SMSService {

    private final RestTemplate restTemplate;
    private final SMSProperties smsProperties;

    public SMSService(RestTemplate restTemplate, SMSProperties smsProperties) {
        this.restTemplate = restTemplate;
        this.smsProperties = smsProperties;
    }

    // Method to send SMS using RestTemplate
    public String sendSms(Map<String, String> requestParams) {
        String url = smsProperties.getUrl();
        return restTemplate.postForObject(url, requestParams, String.class);
    }

    // Configures RestTemplate based on SSL validation flag
    @Bean
    public RestTemplate restTemplate() throws Exception {
        if (!smsProperties.isVerifySSL()) {
            // Disable SSL validation
            SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial((chain, authType) -> true).build();
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            return new RestTemplate(factory);
        } else {
            // Default RestTemplate with SSL validation
            return new RestTemplate();
        }
    }

    @Configuration
    @Data
    public static class SMSProperties {

        @Value("${sms.provider.class}")
        public String gatewayToUse;

        @Value("${sms.provider.requestType}")
        public String requestType;

        @Value("${sms.provider.contentType:application/x-www-form-urlencoded}")
        public String contentType;

        @Value("${sms.mobile.prefix:}")
        private String mobileNumberPrefix;

        @Value("${sms.provider.url}")
        public String url;

        @Value("${sms.provider.username}")
        public String username;

        @Value("${sms.provider.password}")
        public String password;

        @Value("${sms.senderid}")
        public String senderid;

        @Value("${sms.sender.secure.key}")
        public String secureKey;

        @Value("#{${sms.config.map}}")
        Map<String, String> configMap;

        @Value("#{${sms.extra.config.map}}")
        Map<String, String> extraConfigMap;

        @Value("#{${sms.category.map}}")
        Map<String, Map<String, String>> categoryMap;

        @Value("#{'${sms.error.codes}'.split(',')}")
        protected List<String> smsErrorCodes;

        @Value("#{'${sms.success.codes}'.split(',')}")
        protected List<String> smsSuccessCodes;

        @Value("${sms.verify.response:false}")
        private boolean verifyResponse;

        @Value("${sms.verify.responseContains:}")
        private String verifyResponseContains;

        @Value("${sms.verify.ssl:true}")
        private boolean verifySSL;

        @Value("${sms.blacklist.numbers}")
        private List<String> blacklistNumbers;

        @Value("${sms.whitelist.numbers}")
        private List<String> whitelistNumbers;

        @Value("${sms.verify.certificate:false}")
        private boolean verifyCertificate;

        @Value("${sms.msg.append}")
        private String smsMsgAppend;

        @Value("${sms.provider.entityid}")
        public String smsEntityId;

        @Value("${sms.default.tmplid:1}")
        public String smsDefaultTmplid;

        @Value("${sms.debug.msggateway:false}")
        private boolean debugMsggateway;

        @Value("${sms.enabled:false}")
        private boolean smsEnabled;

        private List<Pattern> whitelistPatterns;
        private List<Pattern> blacklistPatterns;

        private List<Pattern> convertToPattern(List<String> data) {
            List<Pattern> patterns = new ArrayList<>(data.size());
            for (String datum : data) {
                patterns.add(Pattern.compile("^" + datum.replace("X", "[0-9]").replace("*", "[0-9]+") + "$"));
            }
            return patterns;
        }

        public boolean isNumberBlacklisted(String number) {
            if (this.blacklistPatterns == null) {
                this.blacklistPatterns = convertToPattern(blacklistNumbers);
            }
            for (Pattern p : blacklistPatterns) {
                if (p.matcher(number).find()) {
                    return true;
                }
            }
            return false;
        }

        public boolean isNumberWhitelisted(String number) {
            if (this.whitelistPatterns == null) {
                this.whitelistPatterns = convertToPattern(whitelistNumbers);
            }
            if (!whitelistPatterns.isEmpty()) {
                for (Pattern p : whitelistPatterns) {
                    if (p.matcher(number).find()) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
    }
}
