package com.offlineupi.offline_upi_server.security;

import java.security.KeyPair;

public class DigitalSignatureTest {

    public static void main(String[] args) throws Exception {

        // Generate public and private keys
        KeyPair keyPair = DigitalSignatureUtil.generateKeyPair();

        String paymentData = "sender=1,receiver=2,amount=500";

        // Create signature using private key
        String signature = DigitalSignatureUtil.signData(
                paymentData,
                keyPair.getPrivate()
        );

        System.out.println("Signature:");
        System.out.println(signature);


        // Verify signature using public key
        boolean result = DigitalSignatureUtil.verifySignature(
                paymentData,
                signature,
                keyPair.getPublic()
        );

        System.out.println("Signature Valid: " + result);
    }
}