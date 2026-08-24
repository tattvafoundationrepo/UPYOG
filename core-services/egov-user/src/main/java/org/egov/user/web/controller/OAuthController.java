package org.egov.user.web.controller;

import static org.egov.user.config.UserServiceConstants.USER_CLIENT_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.egov.user.security.AuthCryptoService;
import org.egov.user.security.CaptchaService;
import org.egov.user.security.SessionRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * Login front-door for the DIGIT UIs, moved here from bmc-service-v1 (digit.web.controllers.OAuthController).
 *
 * It used to be a thin HTTP proxy in bmc-service-v1 that forwarded to this service's OAuth endpoint. Now
 * that it lives inside egov-user — the authorization server itself — there is no network hop: it calls the
 * framework {@link TokenEndpoint} in-process and revokes previous sessions straight against the
 * {@link TokenStore} that {@code /user/_logout} uses.
 *
 * Endpoints (all relative to the {@code /user} context path):
 * <ul>
 *   <li>{@code GET  /user/auth/publickey} — RSA public key the browser encrypts credentials with</li>
 *   <li>{@code GET  /user/auth/captcha}   — server-generated CAPTCHA challenge</li>
 *   <li>{@code POST /user/authenticate}   — CAPTCHA gate + credential decrypt + token issue + single-session</li>
 * </ul>
 *
 * SECURITY: TLS protects credentials in transit. As an app-layer defense-in-depth layer, the browser
 * MAY additionally RSA-encrypt credential fields with the server's PUBLIC key (served by
 * {@code GET /user/auth/publickey}); this controller decrypts them with the private key via
 * {@link AuthCryptoService#maybeDecrypt} before handing them to the OAuth token granter. The decrypt is
 * accept-both, so plaintext-over-TLS also works and rollout can't lock users out. (The former
 * static-AES-key scheme was removed — that key was extractable from the browser, i.e. CWE-798; a public
 * key is not a secret.)
 *
 * DEPLOYMENT: {@code /user/auth/publickey}, {@code /user/auth/captcha} and {@code /user/authenticate} are
 * pre-login calls, so they must be present in the API gateway's open-endpoints whitelist (alongside the
 * existing {@code /user/oauth/token}).
 */
@RestController
@Slf4j
@CrossOrigin("*")
public class OAuthController {

    @Autowired
    private AuthCryptoService authCryptoService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private SessionRegistryService sessionRegistryService;

    /** The framework OAuth token endpoint backing {@code /user/oauth/token}; invoked in-process. */
    @Autowired
    private TokenEndpoint tokenEndpoint;

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private TokenStore tokenStore;

    // Whether a valid server-side CAPTCHA is mandatory on /user/authenticate. Default true (strict).
    // Set AUTH_CAPTCHA_REQUIRED=false as a STOPGAP for clients that verify the captcha client-side and
    // send no captchaId — the gate is then skipped only when no token is supplied (see authenticateUser).
    // This weakens the control (CWE-804: a client-only captcha is bypassable by calling the API directly),
    // so keep it true in production once the frontend sends server-issued captcha tokens again.
    @Value("${auth.captcha.required:true}")
    private boolean captchaRequired;

    /** Public RSA key (base64 SPKI) the browser uses to encrypt credentials. Not a secret. */
    @GetMapping("/auth/publickey")
    public ResponseEntity<Map<String, String>> getAuthPublicKey() {
        Map<String, String> body = new HashMap<String, String>();
        body.put("publicKey", authCryptoService.getPublicKeyBase64());
        return ResponseEntity.ok(body);
    }

    /**
     * Server-generated CAPTCHA challenge: returns {@code {captchaId, image}} (image is a
     * data:image/png;base64 URI). Sits under the same {@code /user/auth/*} bootstrap path as the public
     * key so it is reachable pre-login. NOTE: this path must be in the API gateway's open-endpoints
     * whitelist (same as {@code /user/auth/publickey} and {@code /user/authenticate}).
     */
    @GetMapping("/auth/captcha")
    public ResponseEntity<Map<String, String>> getCaptcha() {
        return ResponseEntity.ok(captchaService.generate());
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Object> authenticateUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String userType,
            @RequestParam String tenantId,
            @RequestParam String scope,
            @RequestParam String grant_type,
            @RequestParam(required = false) String captchaId,
            @RequestParam(required = false) String captchaAnswer) {

        // Server-side CAPTCHA gate: verified here so calling this API directly cannot bypass it.
        // Strict mode (captchaRequired=true, the default) always requires a valid captcha. When relaxed
        // to false, the gate is skipped ONLY when no captchaId is supplied — any token that IS supplied
        // is still verified, so a present captcha can't be spoofed even in the relaxed mode.
        boolean captchaProvided = captchaId != null && !captchaId.trim().isEmpty();
        if ((captchaRequired || captchaProvided) && !captchaService.verify(captchaId, captchaAnswer)) {
            return new ResponseEntity<Object>(
                    error("invalid_captcha", "Invalid or expired captcha"), HttpStatus.BAD_REQUEST);
        }

        try {
            String plainUsername = authCryptoService.maybeDecrypt(username);

            // Same parameter set the form-encoded /user/oauth/token call carried, so the token granter and
            // CustomAuthenticationProvider (which reads tenantId/userType out of these parameters) behave
            // exactly as they did behind the bmc-service-v1 proxy.
            Map<String, String> parameters = new LinkedHashMap<String, String>();
            parameters.put("username", plainUsername);
            parameters.put("password", authCryptoService.maybeDecrypt(password));
            parameters.put("grant_type", grant_type);
            parameters.put("scope", scope);
            parameters.put("tenantId", tenantId);
            parameters.put("userType", userType);

            ResponseEntity<OAuth2AccessToken> response = tokenEndpoint.postAccessToken(clientPrincipal(), parameters);
            OAuth2AccessToken accessToken = response.getBody();

            // Single-session enforcement: on a fresh login, terminate the user's previous session.
            enforceSingleSession(plainUsername, tenantId, userType, accessToken);

            return new ResponseEntity<Object>(accessToken, response.getHeaders(), response.getStatusCode());

        } catch (Exception e) {
            log.error("Authentication failed", e);
            return new ResponseEntity<Object>(
                    error("Authentication failed", "Authentication failed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * The OAuth client this service issues tokens for. {@code /user/oauth/token} identifies it from the
     * {@code Basic egov-user-client:} header; called in-process we resolve the very same registered client
     * (see {@code AuthorizationServerConfiguration#configure(ClientDetailsServiceConfigurer)}) and hand
     * {@link TokenEndpoint} an authenticated client principal for it.
     */
    private Authentication clientPrincipal() {
        ClientDetails client = clientDetailsService.loadClientByClientId(USER_CLIENT_ID);
        return new UsernamePasswordAuthenticationToken(
                client.getClientId(), null, new ArrayList<>(client.getAuthorities()));
    }

    /**
     * Records the newly-issued token as the user's active session and, if a different token was already
     * active, revokes that previous token in the token store so the earlier session ends across all
     * services. Best-effort: any failure here must never block a successful login.
     */
    private void enforceSingleSession(String username, String tenantId, String userType, OAuth2AccessToken issued) {
        try {
            if (issued == null || issued.getValue() == null) return;

            String userKey = SessionRegistryService.userKey(username, tenantId, userType);
            String previousToken = sessionRegistryService.swap(userKey, issued.getValue());
            if (previousToken == null || previousToken.equals(issued.getValue())) return;

            // Same revocation the UI's own logout performs (see LogoutController): drop the old token from
            // the shared Redis token store, after which every service rejects it.
            OAuth2AccessToken previous = tokenStore.readAccessToken(previousToken);
            if (previous != null) {
                tokenStore.removeAccessToken(previous);
                log.info("Previous session invalidated for user on new login.");
            }
        } catch (Exception e) {
            // Never fail the login because the previous-session logout could not be completed.
            log.warn("Could not invalidate previous session: {}", e.getMessage());
        }
    }

    private static Map<String, String> error(String error, String message) {
        Map<String, String> body = new HashMap<String, String>();
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
