package com.g47.cem.cemcontract.enums;

/**
 * Enum for digital signature algorithms
 */
public enum SignatureAlgorithm {
    /**
     * SHA-256 with RSA
     */
    SHA256_WITH_RSA("SHA256withRSA"),
    
    /**
     * SHA-256 with ECDSA
     */
    SHA256_WITH_ECDSA("SHA256withECDSA"),
    
    /**
     * SHA-384 with RSA
     */
    SHA384_WITH_RSA("SHA384withRSA"),
    
    /**
     * SHA-384 with ECDSA
     */
    SHA384_WITH_ECDSA("SHA384withECDSA"),
    
    /**
     * SHA-512 with RSA
     */
    SHA512_WITH_RSA("SHA512withRSA"),
    
    /**
     * SHA-512 with ECDSA
     */
    SHA512_WITH_ECDSA("SHA512withECDSA"),
    
    /**
     * RSASSA-PSS with SHA-256
     */
    RSASSA_PSS_SHA256("RSASSA-PSS"),
    
    /**
     * Ed25519 (EdDSA)
     */
    ED25519("Ed25519"),
    
    /**
     * Ed448 (EdDSA)
     */
    ED448("Ed448");
    
    private final String javaAlgorithmName;
    
    SignatureAlgorithm(String javaAlgorithmName) {
        this.javaAlgorithmName = javaAlgorithmName;
    }
    
    public String getJavaAlgorithmName() {
        return javaAlgorithmName;
    }
    
    /**
     * Get SignatureAlgorithm from Java algorithm name
     */
    public static SignatureAlgorithm fromJavaAlgorithmName(String algorithmName) {
        for (SignatureAlgorithm alg : values()) {
            if (alg.javaAlgorithmName.equals(algorithmName)) {
                return alg;
            }
        }
        return SHA256_WITH_RSA; // default
    }
} 