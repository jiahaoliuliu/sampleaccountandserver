package com.jiahaoliuliu.android.sampleaccountandserver.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

/**
 * The SecurityUtils class contains methods used for password encryption/decryption.
 */
public final class SecurityUtils {

    /**
     * The SecurityUtils class should not be instantiated, so its constructor.
     *  is private to prevent instantiation by other objects.
     */
    private SecurityUtils() { }

    /**
     * Digits used in hex format.
     */
    private static final String HEX = "0123456789ABCDEF";

    /**
     * Seed to encryption.
     */
    private static final String SEED = "com.jiahaoliuliu.android.sampleaccountandserver";

    /**
     * This method appends a byte to a string buffer with hex format.
     * @param sb String buffer to append in
     * @param b Byte to append to the buffer
     */
    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(SecurityUtils.HEX.charAt((b >> 4) & 0x0f)).append(SecurityUtils.HEX.charAt(b & 0x0f));
    }

    /**
     * This method gets a raw key from the given seed.
     *  Raw key is needed for encrypt/decrypt text
     * @param seed Seed used to get the raw key
     * @return Byte array with the raw key
     * @throws Exception
     */
    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    /**
     * This method encrypts text with the given raw byte array.
     *  The algorithm used is AES
     * @param raw
     * @param clear Byte representation of text to encrypt
     * @return Byte array with encrypted text
     * @throws Exception
     */
    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    /**
     * This method decrypts encrypted text with the given raw byte array.
     * @param raw Byte array
     * @param encrypted Text encrypted for decryption
     * @return Byte array with decrypted text
     * @throws Exception
     */
    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    /**
     * This method converts a byte value into a String with hexadecimal format.
     * @param value Value to convert
     * @return String with hexadecimal format
     */
    private static String toHexadecimal(byte[] value) {
        String result = "";
        for (byte aux : value) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) {
                result += "0";
            }
            result += Integer.toHexString(b);
        }

        return result;
    }

    /**
     * This method encrypts text.
     * @param cleartext Text to encrypt
     * @return String in hex format with |cleartext| encrypted
     * @throws Exception
     */
    public static String encryptToHex(String cleartext) throws Exception {
        byte[] rawKey = SecurityUtils.getRawKey(SecurityUtils.SEED.getBytes());
        byte[] result = SecurityUtils.encrypt(rawKey, cleartext.getBytes());
        return SecurityUtils.toHex(result);
    }

    /**
     * This method encrypts text.
     * @param cleartext Text to encrypt
     * @return |cleartext| encrypted into byte array
     * @throws Exception
     */
    public static byte[] encryptToBytes(String cleartext) throws Exception {
        byte[] rawKey = SecurityUtils.getRawKey(SecurityUtils.SEED.getBytes());
        byte[] result = SecurityUtils.encrypt(rawKey, cleartext.getBytes());
        return result;
    }

    /**
     * This method decrypts text.
     * @param encrypted Text to decrypt
     * @return String |encrypted| decrypted into a string
     * @throws Exception
     */
    public static String decrypt(String encrypted) throws Exception {
        byte[] rawKey = SecurityUtils.getRawKey(SecurityUtils.SEED.getBytes());
        byte[] enc = SecurityUtils.toByte(encrypted);
        byte[] result = SecurityUtils.decrypt(rawKey, enc);
        return new String(result);
    }

    /**
     * This method encodes a byte array into a string with base64 format.
     * @param bytes Byte array to encode
     * @return String with |bytes| encoded into base64
     */
    public static String base64Encode(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    /**
     * This method decodes a base64 encoded string.
     * @param base64 String encoded in base64 format
     * @return String with |base64| decoded
     */
    public static String base64Decode(String base64) {
        return SecurityUtils.toHexadecimal(Base64.decode(base64, Base64.NO_WRAP));
    }

    /**
     * This method converts a string to its hex representation.
     */
    public static String toHex(String txt) {
        return SecurityUtils.toHex(txt.getBytes());
    }

    /**
     * This method converts a string in hex format to a byte array.
     */
    public static String fromHex(String hex) {
        return new String(SecurityUtils.toByte(hex));
    }

    /**
     * This method converts a string in hex format to a byte array.
     * @param hexString String in hex format to convert
     * @return Byte array gets from |hexString|
     */
    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return result;
    }

    /**
     * This method gives a hex conversion of a byte array.
     * @param buf Byte array to get its hex representation
     * @return String with |buf| in hex format
     */
    public static String toHex(byte[] buf) {
        if (buf == null) {
            return "";
        }
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            SecurityUtils.appendHex(result, buf[i]);
        }
        return result.toString();
    }
}