package org.egov.user.security;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * App-layer credential protection (defense-in-depth ON TOP of TLS) using ASYMMETRIC RSA.
 *
 * The browser encrypts credentials with the PUBLIC key; this service decrypts with the PRIVATE key,
 * which never leaves the server. A public key in the browser is not a secret (it is meant to be
 * public), so this does NOT reintroduce CWE-798 the way the old static-AES-key-in-JS scheme did.
 *
 * Crypto: RSA/ECB/OAEP with SHA-256 for both the OAEP hash AND MGF1 (matches the browser's
 * WebCrypto RSA-OAEP, which uses MGF1 with the same hash). {@link #maybeDecrypt} is accept-both: if
 * the value is a valid RSA ciphertext it is decrypted; otherwise it is returned unchanged (plaintext
 * over TLS still works), so the frontend/back-end rollout can never lock users out.
 *
 * Key source: {@code auth.rsa.private-key} (base64 PKCS#8) via env/secret for stable, multi-replica
 * deployments. If unset, an EPHEMERAL pair is generated at startup (single-instance/dev only).
 *
 * NOTE: this used to live in bmc-service-v1 (digit.security.AuthCryptoService), which proxied
 * /user/authenticate to this service. It now runs inside egov-user itself, so credentials are
 * decrypted where they are consumed and there is no intermediate hop.
 */
@Service
@Slf4j
public class AuthCryptoService {

    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    @Value("${auth.rsa.private-key:}")
    private String configuredPrivateKeyB64;

    private PrivateKey privateKey;
    private String publicKeyBase64; // X.509/SPKI, base64

    @PostConstruct
    void init() throws Exception {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        if (configuredPrivateKeyB64 != null && !configuredPrivateKeyB64.trim().isEmpty()) {
            byte[] pkcs8 = Base64.getDecoder().decode(configuredPrivateKeyB64.trim());
            privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
            RSAPrivateCrtKey crt = (RSAPrivateCrtKey) privateKey; // CRT key carries the public exponent
            PublicKey pub = kf.generatePublic(new RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent()));
            publicKeyBase64 = Base64.getEncoder().encodeToString(pub.getEncoded());
            log.info("AuthCryptoService: loaded RSA key pair from auth.rsa.private-key.");
        } else {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            privateKey = kp.getPrivate();
            publicKeyBase64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
            log.warn("AuthCryptoService: no auth.rsa.private-key configured — generated an EPHEMERAL RSA key pair. "
                    + "Set AUTH_RSA_PRIVATE_KEY (base64 PKCS#8) so all replicas share one key; otherwise "
                    + "logins routed to a different instance than the one that served the public key will fail.");
        }
    }

    /** Base64 (X.509/SPKI) public key for the browser to import + encrypt with. */
    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    /**
     * Decrypts a base64 RSA-OAEP(SHA-256) ciphertext. If the value is not a valid RSA ciphertext
     * (e.g. it is already plaintext), it is returned unchanged — TLS still protects it on the wire.
     */
    public String maybeDecrypt(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            byte[] ciphertext = Base64.getDecoder().decode(value);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            OAEPParameterSpec oaep = new OAEPParameterSpec(
                    "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, privateKey, oaep);
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Not RSA ciphertext for this key → treat as plaintext (accept-both). Never log the value.
            return value;
        }
    }
}
