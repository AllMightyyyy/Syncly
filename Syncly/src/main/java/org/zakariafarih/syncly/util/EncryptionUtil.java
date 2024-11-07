package org.zakariafarih.syncly.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility class for encrypting and decrypting text using AES algorithm.
 */
@Component
public class EncryptionUtil {

    @Value("${encryption.key}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES";

    /**
     * Encrypts the given plain text using AES algorithm.
     *
     * @param plainText the text to be encrypted
     * @return the encrypted text encoded in Base64
     * @throws RuntimeException if an error occurs during encryption
     */
    public String encrypt(String plainText) {
        try {
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during encryption", e);
        }
    }

    /**
     * Decrypts the given cipher text using AES algorithm.
     *
     * @param cipherText the text to be decrypted, encoded in Base64
     * @return the decrypted plain text
     * @throws RuntimeException if an error occurs during decryption
     */
    public String decrypt(String cipherText) {
        try {
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during decryption", e);
        }
    }
}