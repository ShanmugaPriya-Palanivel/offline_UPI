package com.offlineupi.offline_upi_server.security;

import java.security.*;
import java.util.Base64;

public class DigitalSignatureUtil {

    public static KeyPair generateKeyPair() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        return generator.generateKeyPair();
    }


    public static String signData(String data, PrivateKey privateKey) throws Exception {

        Signature signature = Signature.getInstance("SHA256withRSA");

        signature.initSign(privateKey);

        signature.update(data.getBytes());

        byte[] signedBytes = signature.sign();

        return Base64.getEncoder()
                .encodeToString(signedBytes);
    }


    public static boolean verifySignature(
            String data,
            String signatureText,
            PublicKey publicKey) throws Exception {

        Signature signature = Signature.getInstance("SHA256withRSA");

        signature.initVerify(publicKey);

        signature.update(data.getBytes());

        byte[] decodedSignature =
                Base64.getDecoder().decode(signatureText);

        return signature.verify(decodedSignature);
    }

    public static PublicKey getPublicKeyFromBase64(String base64PublicKey) throws Exception {
        String cleanKey = base64PublicKey
                .replaceAll("-----\\BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----\\END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
        java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public static boolean verifySignature(
            String data,
            String signatureText,
            String base64PublicKey) throws Exception {
        if (base64PublicKey == null || base64PublicKey.isEmpty()) {
            return false;
        }
        PublicKey publicKey = getPublicKeyFromBase64(base64PublicKey);
        return verifySignature(data, signatureText, publicKey);
    }
}