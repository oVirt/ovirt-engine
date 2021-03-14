package org.ovirt.engine.core.uutils.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class EnvelopePBE {

    private static final String ARTIFACT_KEY = "artifact";
    private static final String VERSION_KEY = "version";
    private static final String ALGORITHM_KEY = "algorithm";
    private static final String SALT_KEY = "salt";
    private static final String ITERATIONS_KEY = "iterations";
    private static final String SECRET_KEY = "secret";

    private static final String ARTIFACT = "EnvelopePBE";
    private static final String VERSION = "1";

    public static boolean check(String blob, String password) throws IOException, GeneralSecurityException {
        final Map<String, String> map = new ObjectMapper().readValue(
                Base64.decodeBase64(blob),
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class));

        if (!ARTIFACT.equals(map.get(ARTIFACT_KEY))) {
            throw new IllegalArgumentException(String.format("Invalid artifact '%s'", map.get(ARTIFACT_KEY)));
        }
        if (!VERSION.equals(map.get(VERSION_KEY))) {
            throw new IllegalArgumentException(String.format("Invalid version '%s'", map.get(VERSION_KEY)));
        }

        byte[] salt = Base64.decodeBase64(map.get(SALT_KEY));
        return Arrays.equals(
                Base64.decodeBase64(map.get(SECRET_KEY)),
                SecretKeyFactory.getInstance(map.get(ALGORITHM_KEY))
                        .generateSecret(
                                new PBEKeySpec(
                                        password.toCharArray(),
                                        salt,
                                        Integer.parseInt(map.get(ITERATIONS_KEY)),
                                        salt.length * 8))
                        .getEncoded());
    }

    public static String encode(String algorithm, int keySize, int iterations, String randomProvider, String password)
            throws IOException, GeneralSecurityException {
        final Base64 base64 = new Base64(0);
        final Map<String, String> map = new HashMap<>();

        byte[] salt = new byte[keySize / 8];
        SecureRandom.getInstance(randomProvider == null ? "NativePRNG" : randomProvider).nextBytes(salt);

        map.put(ARTIFACT_KEY, ARTIFACT);
        map.put(VERSION_KEY, VERSION);
        map.put(ALGORITHM_KEY, algorithm);
        map.put(SALT_KEY, base64.encodeToString(salt));
        map.put(ITERATIONS_KEY, Integer.toString(iterations));
        map.put(
                SECRET_KEY,
                base64.encodeToString(
                        SecretKeyFactory.getInstance(algorithm)
                                .generateSecret(
                                        new PBEKeySpec(
                                                password.toCharArray(),
                                                salt,
                                                iterations,
                                                salt.length * 8))
                                .getEncoded()));
        return base64.encodeToString(new ObjectMapper().writeValueAsString(map).getBytes(StandardCharsets.UTF_8));
    }
}
