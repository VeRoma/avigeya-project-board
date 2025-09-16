package com.avigeya.projectboard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TelegramValidationService {

    @Value("${telegram.bot.token}")
    private String botToken;

    public boolean isDataSafe(String initData) {
        if (initData == null || initData.isEmpty()) {
            return false;
        }
        try {
            Map<String, String> params = parseInitData(initData);
            String receivedHash = params.remove("hash");
            if (receivedHash == null) {
                return false;
            }

            String dataCheckString = params.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("\n"));

            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            byte[] secretKey = hmacSha256.doFinal(botToken.getBytes(StandardCharsets.UTF_8));

            secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            byte[] hashBytes = hmacSha256.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));

            String calculatedHash = toHexString(hashBytes);
            return calculatedHash.equals(receivedHash);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Парсит строку initData в Map<String, String>.
     * 
     * @param initData строка window.Telegram.WebApp.initData
     * @return Map с параметрами
     */
    public Map<String, String> parseInitData(String initData) {
        return Arrays.stream(initData.split("&"))
                .map(param -> param.split("=", 2))
                .collect(Collectors.toMap(
                        p -> decode(p[0]),
                        p -> decode(p.length > 1 ? p[1] : "")));
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }

    private String toHexString(byte[] bytes) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }
}