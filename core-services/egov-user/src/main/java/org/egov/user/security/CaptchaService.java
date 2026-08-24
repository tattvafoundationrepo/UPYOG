package org.egov.user.security;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Server-side CAPTCHA (fixes CWE-804 / weak client-side CAPTCHA).
 *
 * The challenge text is generated HERE and rendered to a PNG — it is NEVER sent to the browser as
 * readable text, so it cannot be scraped from the DOM. Verification also happens HERE, so calling the
 * authenticate API directly cannot bypass the CAPTCHA.
 *
 * The challenge is STATELESS: {@code generate()} returns an opaque {@code captchaId} of the form
 * {@code <expiryEpochMs>.<base64url(HMAC-SHA256(secret, answerLowerCase|expiry))>}. The plaintext answer
 * is never stored server-side and is not present in the token. {@code verify()} recomputes the HMAC from
 * the user's submitted answer and constant-time compares. A short TTL plus an in-memory single-use set
 * prevents replay of a solved token.
 *
 * Multi-replica note: set {@code auth.captcha.hmac-secret} (a shared random secret) so any replica can
 * verify a token minted by another. The single-use set is per-instance; combined with the short TTL this
 * is acceptable (a solved token could at worst be replayed once per replica within the TTL window). For
 * strict single-use across replicas, back the consumed-set with Redis.
 *
 * NOTE: moved here from bmc-service-v1 (digit.security.CaptchaService) so the CAPTCHA gate lives in the
 * same service that issues the token.
 */
@Service
@Slf4j
public class CaptchaService {

    private static final String CHARS = "abcdefghjkmnpqrstuvwxyz23456789"; // no visually ambiguous chars
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Color[] PALETTE = {
            new Color(0x2f, 0x2a, 0x1c), new Color(0x4a, 0x46, 0x36),
            new Color(0x7a, 0x6a, 0x2c), new Color(0xb8, 0x96, 0x2f), new Color(0x5c, 0x58, 0x48)
    };

    // Self-contained 5x7 bitmap font for the captcha alphabet. Glyphs are drawn as solid pixel blocks
    // (Graphics2D#fillRect) so rendering NEVER touches the native AWT font stack / libfreetype — the
    // captcha renders on any base image (e.g. openjdk:8-jdk-alpine) with no fonts installed. Shapes are
    // uppercase-styled; verify() lowercases the user's answer, so case never matters.
    private static final int GLYPH_COLS = 5;
    private static final int GLYPH_ROWS = 7;
    private static final Map<Character, int[]> FONT = buildFont();

    static {
        // AWT image rendering must run headless inside the service container.
        System.setProperty("java.awt.headless", "true");
    }

    private static Map<Character, int[]> buildFont() {
        Map<Character, int[]> f = new HashMap<Character, int[]>();
        f.put('a', new int[]{0b01110, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001});
        f.put('b', new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10001, 0b10001, 0b11110});
        f.put('c', new int[]{0b01110, 0b10001, 0b10000, 0b10000, 0b10000, 0b10001, 0b01110});
        f.put('d', new int[]{0b11110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b11110});
        f.put('e', new int[]{0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b11111});
        f.put('f', new int[]{0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b10000});
        f.put('g', new int[]{0b01110, 0b10001, 0b10000, 0b10111, 0b10001, 0b10001, 0b01110});
        f.put('h', new int[]{0b10001, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001});
        f.put('j', new int[]{0b00011, 0b00001, 0b00001, 0b00001, 0b10001, 0b10001, 0b01110});
        f.put('k', new int[]{0b10001, 0b10010, 0b10100, 0b11000, 0b10100, 0b10010, 0b10001});
        f.put('m', new int[]{0b10001, 0b11011, 0b10101, 0b10101, 0b10001, 0b10001, 0b10001});
        f.put('n', new int[]{0b10001, 0b10001, 0b11001, 0b10101, 0b10011, 0b10001, 0b10001});
        f.put('p', new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10000, 0b10000, 0b10000});
        f.put('q', new int[]{0b01110, 0b10001, 0b10001, 0b10001, 0b10101, 0b10010, 0b01101});
        f.put('r', new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10100, 0b10010, 0b10001});
        f.put('s', new int[]{0b01110, 0b10001, 0b10000, 0b01110, 0b00001, 0b10001, 0b01110});
        f.put('t', new int[]{0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100});
        f.put('u', new int[]{0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110});
        f.put('v', new int[]{0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01010, 0b00100});
        f.put('w', new int[]{0b10001, 0b10001, 0b10001, 0b10101, 0b10101, 0b10101, 0b01010});
        f.put('x', new int[]{0b10001, 0b10001, 0b01010, 0b00100, 0b01010, 0b10001, 0b10001});
        f.put('y', new int[]{0b10001, 0b10001, 0b01010, 0b00100, 0b00100, 0b00100, 0b00100});
        f.put('z', new int[]{0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b10000, 0b11111});
        f.put('2', new int[]{0b01110, 0b10001, 0b00001, 0b00010, 0b00100, 0b01000, 0b11111});
        f.put('3', new int[]{0b01110, 0b10001, 0b00001, 0b00110, 0b00001, 0b10001, 0b01110});
        f.put('4', new int[]{0b00010, 0b00110, 0b01010, 0b10010, 0b11111, 0b00010, 0b00010});
        f.put('5', new int[]{0b11111, 0b10000, 0b11110, 0b00001, 0b00001, 0b10001, 0b01110});
        f.put('6', new int[]{0b01110, 0b10001, 0b10000, 0b11110, 0b10001, 0b10001, 0b01110});
        f.put('7', new int[]{0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b01000, 0b01000});
        f.put('8', new int[]{0b01110, 0b10001, 0b10001, 0b01110, 0b10001, 0b10001, 0b01110});
        f.put('9', new int[]{0b01110, 0b10001, 0b10001, 0b01111, 0b00001, 0b10001, 0b01110});
        return f;
    }

    @Value("${auth.captcha.hmac-secret:}")
    private String configuredSecret;

    @Value("${auth.captcha.length:6}")
    private int length;

    @Value("${auth.captcha.ttl-seconds:120}")
    private long ttlSeconds;

    private byte[] secret;

    /** token -> expiryEpochMs of tokens already redeemed, to block replay until they expire anyway. */
    private final Map<String, Long> consumed = new ConcurrentHashMap<String, Long>();

    @PostConstruct
    void init() {
        if (configuredSecret != null && !configuredSecret.trim().isEmpty()) {
            secret = configuredSecret.trim().getBytes(StandardCharsets.UTF_8);
            log.info("CaptchaService: using configured auth.captcha.hmac-secret.");
        } else {
            secret = new byte[32];
            RANDOM.nextBytes(secret);
            log.warn("CaptchaService: no auth.captcha.hmac-secret configured — generated an EPHEMERAL secret. "
                    + "Set AUTH_CAPTCHA_HMAC_SECRET so all replicas can verify each other's captcha tokens.");
        }
    }

    /**
     * Generates a fresh challenge.
     *
     * @return map with {@code captchaId} (opaque signed token) and {@code image} (a data:image/png;base64 URI).
     */
    public Map<String, String> generate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        String answer = sb.toString();

        long expiry = nowMillis() + ttlSeconds * 1000L;
        String token = expiry + "." + base64Url(hmac(answer + "|" + expiry));

        Map<String, String> challenge = new HashMap<String, String>();
        challenge.put("captchaId", token);
        challenge.put("image", renderPng(answer));
        return challenge;
    }

    /**
     * Verifies a submitted answer against its token. Rejects on: missing input, malformed token,
     * expiry, replay (already consumed), or answer mismatch. On success the token is marked consumed.
     */
    public boolean verify(String captchaId, String answer) {
        if (isBlank(captchaId) || isBlank(answer)) return false;

        int dot = captchaId.indexOf('.');
        if (dot <= 0) return false;

        long expiry;
        try {
            expiry = Long.parseLong(captchaId.substring(0, dot));
        } catch (NumberFormatException e) {
            return false;
        }
        String providedMac = captchaId.substring(dot + 1);

        purgeExpired();
        if (nowMillis() > expiry) return false;
        if (consumed.containsKey(captchaId)) return false; // replay of an already-solved token

        String expectedMac = base64Url(hmac(answer.trim().toLowerCase() + "|" + expiry));
        boolean ok = MessageDigest.isEqual(
                expectedMac.getBytes(StandardCharsets.UTF_8), providedMac.getBytes(StandardCharsets.UTF_8));
        if (ok) consumed.put(captchaId, expiry); // single-use
        return ok;
    }

    // ── internals ──────────────────────────────────────────────────────────

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private byte[] hmac(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret, HMAC_ALGO));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC computation failed", e);
        }
    }

    private static String base64Url(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private void purgeExpired() {
        final long now = nowMillis();
        consumed.entrySet().removeIf(e -> e.getValue() < now);
    }

    private long nowMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Renders the challenge to a PNG using the built-in bitmap font — only pixel/line fills, never AWT
     * text rendering, so it does not depend on native fonts (libfreetype) and works on a bare slim image.
     */
    private String renderPng(String text) {
        int w = 200, h = 60;
        int px = 5;                                // size of one glyph "pixel" block
        int glyphW = GLYPH_COLS * px;              // 25
        int glyphH = GLYPH_ROWS * px;              // 35
        int gap = 8;
        int n = text.length();
        int totalW = n * glyphW + (n - 1) * gap;
        int startX = Math.max(4, (w - totalW) / 2);
        int baseY = (h - glyphH) / 2;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setColor(new Color(0xf7, 0xf5, 0xec));
            g.fillRect(0, 0, w, h);

            // speckle noise (pure raster ops — no font stack)
            for (int i = 0; i < 55; i++) {
                g.setColor(PALETTE[RANDOM.nextInt(PALETTE.length)]);
                g.fillRect(RANDOM.nextInt(w), RANDOM.nextInt(h), 2, 2);
            }

            // glyphs, with per-character vertical jitter and per-row horizontal wobble to resist OCR
            int x = startX;
            for (int i = 0; i < n; i++) {
                int[] glyph = FONT.get(text.charAt(i));
                if (glyph == null) { x += glyphW + gap; continue; }
                g.setColor(PALETTE[RANDOM.nextInt(3)]);
                int yJitter = RANDOM.nextInt(7) - 3;
                for (int row = 0; row < GLYPH_ROWS; row++) {
                    int rowWobble = RANDOM.nextInt(3) - 1;
                    int bits = glyph[row];
                    for (int col = 0; col < GLYPH_COLS; col++) {
                        if ((bits & (1 << (GLYPH_COLS - 1 - col))) != 0) {
                            g.fillRect(x + col * px + rowWobble, baseY + yJitter + row * px, px, px);
                        }
                    }
                }
                x += glyphW + gap;
            }

            // a couple of light distortion lines over the glyphs (kept sparse so text stays readable)
            for (int i = 0; i < 2; i++) {
                g.setColor(PALETTE[2 + RANDOM.nextInt(3)]); // lighter half of the palette
                int y = 10 + RANDOM.nextInt(h - 20);
                g.drawLine(0, y, w, y + RANDOM.nextInt(14) - 7);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Captcha image rendering failed", e);
        } finally {
            g.dispose();
        }
    }
}
