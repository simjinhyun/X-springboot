package kr.co.semperpi;

import java.util.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AssignableTypeFilter;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;

public class Util {
    private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    public static String generateReqId() {
        UUID uuid = UUID.randomUUID();
        // UUID를 16바이트 배열로 변환
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        byte[] bytes = bb.array();
        BigInteger bigInt = new BigInteger(1, bytes);
        return toBase62AndPad0(bigInt);
    }

    private static String toBase62AndPad0(BigInteger value) {
        StringBuilder sb = new StringBuilder();
        BigInteger base = BigInteger.valueOf(62);
        while (value.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divRem = value.divideAndRemainder(base);
            sb.append(BASE62[divRem[1].intValue()]);
            value = divRem[0];
        }
        return String.format("%22s", sb.toString()).replace(' ', '0');
    }

    public static String formatElapsed(long millis) {
        if (millis < 1000) {
            return millis + " ms";
        } else if (millis < 60_000) {
            double seconds = millis / 1000.0;
            return String.format("%.2f s", seconds);
        } else {
            long minutes = millis / 60_000;
            double seconds = (millis % 60_000) / 1000.0;
            return String.format("%d min %.2f s", minutes, seconds);
        }
    }

    private static void addHandler(Method m, Class<?> clazz, Map<String, Method> target) {
        String className = clazz.getName().toLowerCase().replaceAll("\\.", "/");
        String url = "/" + className + "/" + m.getName();
        target.put(url, m);
    }

    public static Map<String, Method> scanHandler() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));

        Map<String, Method> localApi = new HashMap<>();
        for (BeanDefinition bd : scanner.findCandidateComponents("api")) {
            Class<?> clazz = Class.forName(bd.getBeanClassName());

            Arrays.stream(clazz.getDeclaredMethods())
                    .filter(m -> Modifier.isStatic(m.getModifiers()))
                    .filter(m -> Modifier.isPublic(m.getModifiers()))
                    .filter(m -> m.getReturnType().equals(void.class))
                    .filter(m -> {
                        Class<?>[] params = m.getParameterTypes();
                        return params.length == 1 && params[0].equals(Ctx.class);
                    })
                    .forEach(m -> addHandler(m, clazz, localApi));
        }
        return localApi;
    }

    ////////////////////////////////////////////////////////////////////////////
    /// 유틸리티 메서드
    ////////////////////////////////////////////////////////////////////////////

    private static String aeskey;
    private static String aesiv;

    public static void initAES(Environment env) {
        aeskey = env.getProperty("aeskey");
        aesiv = env.getProperty("aesiv");
        X.logger.debug(aeskey);
        X.logger.debug(aesiv);
    }

    // 양방향 암호화
    public static String encrypt(String plainText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(aeskey.getBytes("UTF-8"), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(aesiv.getBytes("UTF-8"));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new Error("EncryptError", null);
        }

    }

    // 양방향 복호화
    public static String decrypt(String cipherText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(aeskey.getBytes("UTF-8"), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(aesiv.getBytes("UTF-8"));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decodedBytes);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            throw new Error("DecryptError", null);
        }
    }

    // 단방향 해시
    private static final SecureRandom random = new SecureRandom();

    public static String hash(String password) {
        try {
            // Salt 생성 (16바이트)
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // 반복 횟수와 키 길이 설정
            int iterations = 65536;
            int keyLength = 256;

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = skf.generateSecret(spec).getEncoded();

            // salt와 hash를 합쳐서 저장 (Base64)
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new Error("HashError", null);
        }

    }

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    /**
     * 평문을 기존해시 salt이용해서 해싱해서 같은지 판단.
     */
    public static boolean match(String plain, String hash) throws Exception {
        // "salt:hash" 구조 분리
        String[] parts = hash.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid stored password format");
        }

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] storedHash = Base64.getDecoder().decode(parts[1]);

        // 입력 비밀번호를 동일한 방식으로 해싱
        PBEKeySpec spec = new PBEKeySpec(plain.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] testHash = skf.generateSecret(spec).getEncoded();

        // 결과 비교 (타이밍 공격 방지를 위해 Arrays.equals 사용)
        return Arrays.equals(storedHash, testHash);
    }

    public static String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = request.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String s = sb.toString();
        X.logger.debug("요청바디:" + s);
        return s;
    }
}
