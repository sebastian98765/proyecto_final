package com.hoteluq.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/** Hash de contraseñas con PBKDF2-HMAC-SHA256 y salt aleatorio. */
public class PasswordHasher {
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private final SecureRandom random = new SecureRandom();

    public String hash(char[] password) {
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(derived);
    }

    public boolean matches(char[] password, String encoded) {
        if (encoded == null) return false;
        try {
            String[] parts = encoded.split(":");
            if (parts.length != 3) return false;
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            byte[] actual = derive(password, salt, iterations);
            return java.security.MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No fue posible procesar la contraseña.", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
