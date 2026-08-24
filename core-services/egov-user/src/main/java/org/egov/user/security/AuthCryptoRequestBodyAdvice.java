package org.egov.user.security;

import java.lang.reflect.Type;

import org.egov.user.web.contract.CreateUserRequest;
import org.egov.user.web.contract.LoggedInUserUpdatePasswordRequest;
import org.egov.user.web.contract.NonLoggedInUserUpdatePasswordRequest;
import org.egov.user.web.contract.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * Decrypts the RSA-protected credential fields of JSON request bodies (see {@link AuthCryptoService}).
 *
 * WHY AN ADVICE AND NOT PLAIN CONTROLLER CODE: {@code RequestBodyAdvice#afterBodyRead} runs after the
 * body is deserialized but BEFORE {@code @Valid} bean validation. That ordering matters — an RSA-OAEP
 * ciphertext is ~344 base64 chars, so a still-encrypted {@code userName} would be rejected by
 * {@code @Size(max = 64)} on {@link UserRequest} before any controller code could decrypt it.
 *
 * {@link AuthCryptoService#maybeDecrypt} is accept-both: values that are not ciphertext for this key are
 * returned untouched, so plaintext-over-TLS clients (and every other endpoint that happens to use these
 * contracts) keep working exactly as before.
 *
 * This replaces the decrypt step that bmc-service-v1's OAuthController used to perform before proxying
 * /citizen/_create, /forget/_password and /update/_password to this service.
 */
@ControllerAdvice
public class AuthCryptoRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Autowired
    private AuthCryptoService authCryptoService;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return isSupported(targetType);
    }

    private boolean isSupported(Type targetType) {
        if (!(targetType instanceof Class)) {
            return false;
        }
        Class<?> type = (Class<?>) targetType;
        return CreateUserRequest.class.isAssignableFrom(type)
                || LoggedInUserUpdatePasswordRequest.class.isAssignableFrom(type)
                || NonLoggedInUserUpdatePasswordRequest.class.isAssignableFrom(type);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (body instanceof CreateUserRequest) {
            UserRequest user = ((CreateUserRequest) body).getUser();
            if (user != null) {
                user.setUserName(authCryptoService.maybeDecrypt(user.getUserName()));
                user.setOtpReference(authCryptoService.maybeDecrypt(user.getOtpReference()));
            }
        } else if (body instanceof LoggedInUserUpdatePasswordRequest) {
            LoggedInUserUpdatePasswordRequest request = (LoggedInUserUpdatePasswordRequest) body;
            request.setExistingPassword(authCryptoService.maybeDecrypt(request.getExistingPassword()));
            request.setNewPassword(authCryptoService.maybeDecrypt(request.getNewPassword()));
        } else if (body instanceof NonLoggedInUserUpdatePasswordRequest) {
            NonLoggedInUserUpdatePasswordRequest request = (NonLoggedInUserUpdatePasswordRequest) body;
            request.setNewPassword(authCryptoService.maybeDecrypt(request.getNewPassword()));
            request.setOtpReference(authCryptoService.maybeDecrypt(request.getOtpReference()));
        }
        return body;
    }
}
