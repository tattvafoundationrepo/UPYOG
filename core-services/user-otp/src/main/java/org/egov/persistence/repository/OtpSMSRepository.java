import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class OtpSMSRepository {

    private static final String LOCALIZATION_KEY_REGISTER_SMS = "sms.register.otp.msg";
    private static final String LOCALIZATION_KEY_LOGIN_SMS = "sms.login.otp.msg";
    private static final String LOCALIZATION_KEY_PWD_RESET_SMS = "sms.pwd.reset.otp.msg";

    @Value("${expiry.time.for.otp: 4000}")
    private long maxExecutionTime = 2000L;

    @Value("${egov.localisation.tenantid.strip.suffix.count}")
    private int tenantIdStripSuffixCount;

    private CustomKafkaTemplate<String, SMSRequest> kafkaTemplate;
    private String smsTopic;

    @Autowired
    private LocalizationService localizationService;

    @Autowired
    public OtpSMSRepository(CustomKafkaTemplate<String, SMSRequest> kafkaTemplate,
                            @Value("${sms.topic}") String smsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.smsTopic = smsTopic;
    }

    public void send(OtpRequest otpRequest, String otpNumber) {
        Long currentTime = System.currentTimeMillis() + maxExecutionTime;
        final String message = getMessage(otpNumber, otpRequest);

        // Convert message to Unicode (UTF-8) bytes if necessary
        String unicodeMessage = new String(message.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        
        kafkaTemplate.send(smsTopic, new SMSRequest(otpRequest.getMobileNumber(), unicodeMessage, Category.OTP, currentTime));
    }

    private String getMessage(String otpNumber, OtpRequest otpRequest) {
        final String messageFormat = getMessageFormat(otpRequest);
        return String.format(messageFormat, otpNumber);
    }

    private String getMessageFormat(OtpRequest otpRequest) {
        String tenantId = getRequiredTenantId(otpRequest.getTenantId());
        Map<String, String> localisedMsgs = localizationService.getLocalisedMessages(tenantId, "en_IN", "egov-user");

        if (localisedMsgs.isEmpty()) {
            log.info("Localization Service didn't return any msgs so using default...");
            localisedMsgs.put(LOCALIZATION_KEY_REGISTER_SMS, "प्रिय नागरिकांनो, BMC सिटिझन सर्व्हिसेसमध्ये लॉग इन करण्यासाठी OTP %s आहे. हा OTP 5 मिनिटांसाठी वैध आहे. कृपया, कोणाशीही शेअर करू नका.आभार, BMC महाराष्ट्र");
            localisedMsgs.put(LOCALIZATION_KEY_LOGIN_SMS, "प्रिय नागरिकांनो, BMC सिटिझन सर्व्हिसेसमध्ये लॉग इन करण्यासाठी OTP %s आहे. हा OTP 5 मिनिटांसाठी वैध आहे. कृपया, कोणाशीही शेअर करू नका.आभार, BMC महाराष्ट्र");
            localisedMsgs.put(LOCALIZATION_KEY_PWD_RESET_SMS, "Dear Citizen, Your OTP for recovering password is %s.");
        }

        String message;
        if (otpRequest.isRegistrationRequestType()) {
            message = localisedMsgs.get(LOCALIZATION_KEY_REGISTER_SMS);
        } else if (otpRequest.isLoginRequestType()) {
            message = localisedMsgs.get(LOCALIZATION_KEY_LOGIN_SMS);
        } else {
            message = localisedMsgs.get(LOCALIZATION_KEY_PWD_RESET_SMS);
        }

        return message;
    }

    private String getRequiredTenantId(String tenantId) {
        String[] tenantList = tenantId.split("\\.");
        if (tenantIdStripSuffixCount > 0 && tenantIdStripSuffixCount < tenantList.length) {
            int cutIndex = tenantList.length - tenantIdStripSuffixCount;
            String requriedTenantId = tenantList[0];
            for (int idx = 1; idx < cutIndex; idx++) {
                requriedTenantId = requriedTenantId + "." + tenantList[idx];
            }

            return requriedTenantId;
        } else if (tenantIdStripSuffixCount >= tenantList.length) {
            return tenantList[0];
        } else {
            return tenantId;
        }
    }
}
