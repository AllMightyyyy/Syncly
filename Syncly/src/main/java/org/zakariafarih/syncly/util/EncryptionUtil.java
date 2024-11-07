package org.zakariafarih.syncly.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zakariafarih.syncly.config.EncryptionConfig;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Component
public class EncryptionUtil {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12; // 96 bits for GCM
    private static final int TAG_SIZE = 128; // 16 bytes authentication tag

    @Autowired
    private EncryptionConfig encryptionConfig;

    private SecureRandom secureRandom = new SecureRandom();

    /**
     * Encrypts the given plaintext using the active encryption key.
     *
     * @param plainText the plaintext to encrypt
     * @return the encrypted data encoded in Base64, including keyId and IV
     */
    public String encrypt(String plainText) {
        try {
            // Get the active key (first key in the map)
            Map.Entry<String, String> activeKeyEntry = encryptionConfig.getEncryptionKeys().entrySet().iterator().next();
            String keyId = activeKeyEntry.getKey();
            String base64Key = activeKeyEntry.getValue();

            byte[] key = Base64.getDecoder().decode(base64Key);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            // Generate a unique IV
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine keyId, IV, and cipherText
            ByteBuffer byteBuffer = ByteBuffer.allocate(2 + keyId.getBytes(StandardCharsets.UTF_8).length + IV_SIZE + cipherText.length);
            byteBuffer.putShort((short) keyId.getBytes(StandardCharsets.UTF_8).length);
            byteBuffer.put(keyId.getBytes(StandardCharsets.UTF_8));
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            String encryptedData = Base64.getEncoder().encodeToString(byteBuffer.array());
            logger.info("Data encrypted using keyId: {}", keyId);
            return encryptedData;
        } catch (Exception e) {
            logger.error("Encryption failed", e);
            throw new RuntimeException("Error occurred during encryption", e);
        }
    }

    /**
     * Decrypts the given ciphertext using the appropriate encryption key based on keyId.
     *
     * @param cipherText the encrypted data encoded in Base64
     * @return the decrypted plaintext
     */
    public String decrypt(String cipherText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            // Read keyId length and keyId
            short keyIdLength = byteBuffer.getShort();
            byte[] keyIdBytes = new byte[keyIdLength];
            byteBuffer.get(keyIdBytes);
            String keyId = new String(keyIdBytes, StandardCharsets.UTF_8);

            // Read IV
            byte[] iv = new byte[IV_SIZE];
            byteBuffer.get(iv);

            // Read cipherText
            byte[] cipherBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherBytes);

            // Retrieve the corresponding key
            String base64Key = encryptionConfig.getEncryptionKeys().get(keyId);
            if (base64Key == null) {
                logger.error("Invalid keyId: {}", keyId);
                throw new RuntimeException("Invalid keyId: " + keyId);
            }

            byte[] key = Base64.getDecoder().decode(base64Key);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherBytes);
            logger.info("Data decrypted using keyId: {}", keyId);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Decryption failed", e);
            throw new RuntimeException("Error occurred during decryption", e);
        }
    }
}
