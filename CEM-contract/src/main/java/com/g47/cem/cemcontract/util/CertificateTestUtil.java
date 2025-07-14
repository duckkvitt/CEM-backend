package com.g47.cem.cemcontract.util;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Component;

import com.g47.cem.cemcontract.entity.DigitalCertificate;
import com.g47.cem.cemcontract.enums.CertificateType;
import com.g47.cem.cemcontract.enums.CertificateStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for generating test certificates for digital signature testing
 * DO NOT USE IN PRODUCTION - These are self-signed certificates for testing only
 */
@Component
@Slf4j
public class CertificateTestUtil {

    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEY_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    private static final int VALIDITY_YEARS = 1;

    static {
        // Register BouncyCastle provider
        if (java.security.Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            java.security.Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Generate a self-signed X.509 certificate for testing purposes.
     *
     * @param commonName      Common Name for the certificate subject
     * @param organization    Organization for the certificate subject
     * @param privateKeyAlias Alias for the private key in the keystore
     * @return A map containing the certificate and private key
     * @throws Exception if generation fails
     */
    public static Map<String, Object> generateSelfSignedCertificate(
            String commonName,
            String organization,
            String privateKeyAlias) throws Exception {

        log.info("Generating test certificate for: {}", commonName);

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        X500Name issuer = new X500Name("CN=Test CA, O=Test Certification Authority");
        X500Name subject = new X500Name("CN=" + commonName + ", O=" + organization);

        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

        Date notBefore = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(notBefore);
        cal.add(Calendar.YEAR, VALIDITY_YEARS);
        Date notAfter = cal.getTime();

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serial,
                notBefore,
                notAfter,
                subject,
                publicKey);

        // Add basic constraints (this is a leaf certificate)
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));

        // Add key usage
        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment);
        certBuilder.addExtension(Extension.keyUsage, true, keyUsage);

        // Add extended key usage
        ExtendedKeyUsage extendedKeyUsage = new ExtendedKeyUsage(new KeyPurposeId[] {
                KeyPurposeId.id_kp_serverAuth,
                KeyPurposeId.id_kp_clientAuth,
                KeyPurposeId.id_kp_codeSigning
        });
        certBuilder.addExtension(Extension.extendedKeyUsage, false, extendedKeyUsage);

        // Add subject key identifier
        SubjectKeyIdentifier subjectKeyIdentifier = new JcaX509ExtensionUtils().createSubjectKeyIdentifier(publicKey);
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false, subjectKeyIdentifier);

        // Build and sign the certificate
        ContentSigner contentSigner = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(privateKey);
        X509CertificateHolder holder = certBuilder.build(contentSigner);
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(holder);

        Map<String, Object> result = new HashMap<>();
        result.put("certificate", certificate);
        result.put("privateKey", privateKey);

        return result;
    }

    /**
     * Creates a DigitalCertificate entity from a generated X.509 certificate.
     */
    public static DigitalCertificate createDigitalCertificateEntity(X509Certificate certificate, PrivateKey privateKey,
            String createdBy) throws Exception {
        
        byte[] certificateData = certificate.getEncoded();
        
        // Extract and encode public key
        byte[] publicKeyData = certificate.getPublicKey().getEncoded();
        
        // Create PKCS12 keystore with certificate and private key
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("cert", privateKey, "password".toCharArray(), new Certificate[]{certificate});
        
        // Export keystore as byte array for storage
        ByteArrayOutputStream keystoreStream = new ByteArrayOutputStream();
        keyStore.store(keystoreStream, "password".toCharArray());
        byte[] keystoreData = keystoreStream.toByteArray();

        return DigitalCertificate.builder()
                .alias("cert_" + System.currentTimeMillis())  // Generate unique alias
                .subjectDN(certificate.getSubjectX500Principal().getName())
                .issuerDN(certificate.getIssuerX500Principal().getName())
                .serialNumber(certificate.getSerialNumber().toString())
                .certificateType(CertificateType.SELF_SIGNED)
                .status(CertificateStatus.ACTIVE)
                .validFrom(certificate.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .validTo(certificate.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .certificateData(certificateData)
                .privateKeyData(keystoreData)  // Store complete PKCS12 keystore
                .publicKeyData(publicKeyData) // Store public key data
                .keyAlgorithm(certificate.getPublicKey().getAlgorithm())
                .keySize(certificate.getPublicKey() instanceof java.security.interfaces.RSAPublicKey
                        ? ((java.security.interfaces.RSAPublicKey) certificate.getPublicKey()).getModulus().bitLength()
                        : null)
                .signatureAlgorithm(certificate.getSigAlgName())
                .fingerprintSha1(generateFingerprint(certificateData, "SHA-1"))
                .fingerprintSha256(generateFingerprint(certificateData, "SHA-256"))
                .createdBy(createdBy)
                .description("Test certificate for " + createdBy)
                .build();
    }

    /**
     * Generate a fingerprint for the certificate data.
     */
    private static String generateFingerprint(byte[] certData, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] digest = md.digest(certData);
        return bytesToHex(digest);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Generate multiple test certificates for different roles
     */
    public DigitalCertificate generateManagerCertificate() {
        try {
            Map<String, Object> certData = generateSelfSignedCertificate(
                "Test Manager",
                "CEM Test Organization",
                "manager"
            );
            return createDigitalCertificateEntity((X509Certificate) certData.get("certificate"), (PrivateKey) certData.get("privateKey"), "SYSTEM_TEST");
        } catch (Exception e) {
            log.error("Failed to generate manager certificate: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate manager certificate", e);
        }
    }

    /**
     * Generate test certificate for any user with custom parameters
     */
    public DigitalCertificate generateTestCertificateForUser(String commonName, String organization, String createdBy) {
        try {
            Map<String, Object> certData = generateSelfSignedCertificate(
                commonName,
                organization,
                "test_user"
            );
            return createDigitalCertificateEntity(
                (X509Certificate) certData.get("certificate"), 
                (PrivateKey) certData.get("privateKey"), 
                createdBy
            );
        } catch (Exception e) {
            log.error("Failed to generate test certificate for user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate test certificate for user", e);
        }
    }

    /**
     * Generate test certificate for staff
     */
    public DigitalCertificate generateStaffCertificate() {
        try {
            Map<String, Object> certData = generateSelfSignedCertificate(
                "Test Staff",
                "CEM Test Organization",
                "staff"
            );
            return createDigitalCertificateEntity((X509Certificate) certData.get("certificate"), (PrivateKey) certData.get("privateKey"), "SYSTEM_TEST");
        } catch (Exception e) {
            log.error("Failed to generate staff certificate: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate staff certificate", e);
        }
    }

    public DigitalCertificate generateCustomerCertificate() {
        try {
            Map<String, Object> certData = generateSelfSignedCertificate(
                "Test Customer",
                "Customer Test Organization",
                "customer"
            );
            return createDigitalCertificateEntity((X509Certificate) certData.get("certificate"), (PrivateKey) certData.get("privateKey"), "SYSTEM_TEST");
        } catch (Exception e) {
            log.error("Failed to generate customer certificate: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate customer certificate", e);
        }
    }
} 