package org.ovirt.engine.core.sso.api.jwk;

import java.math.BigInteger;
import java.security.interfaces.RSAMultiPrimePrivateCrtKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A JWK is a JSON object that represents a cryptographic key. The members of the object represent properties of the
 * key, including its value.
 * <p/>
 * This simplistic RSA implementation was heavily influenced by
 * <a href="https://bitbucket.org/connect2id/nimbus-jose-jwt/wiki/Home">nimbus-jose-jwt</a> version 5.12
 *
 *
 * @see <a href="https://tools.ietf.org/html/rfc7517#section-4">RFC 7517 section 4</a>
 * @see <a href=
 *      "https://bitbucket.org/connect2id/nimbus-jose-jwt/src/5.12/src/main/java/com/nimbusds/jose/jwk/RSAKey.java">
 *      com.nimbusds.jose.jwk.RSAKey</a>
 */
public final class JWK {

    private final Map<String, Object> jsonMap;

    private JWK(String kid,
            String kty,
            String n,
            String e,
            String d,
            String p,
            String q,
            String dp,
            String dq,
            String qi,
            List<Builder.OtherPrimes> oth) {
        // private: force builder usage
        jsonMap = Collections.unmodifiableMap(new HashMap<>() {
            {
                if (kid != null) {
                    put("kid", kid);
                }
                if (kty != null) {
                    put("kty", kty);
                }
                if (n != null) {
                    put("n", n);
                }
                if (e != null) {
                    put("e", e);
                }
                if (d != null) {
                    put("d", d);
                }
                if (p != null) {
                    put("p", p);
                }
                if (q != null) {
                    put("q", q);
                }
                if (dp != null) {
                    put("dp", dp);
                }
                if (dq != null) {
                    put("dq", dq);
                }
                if (qi != null) {
                    put("qi", qi);
                }
                if (oth != null && !oth.isEmpty()) {
                    put("oth", oth);
                }
            }
        });
    }

    public static Builder builder(RSAPublicKey pub) {
        return new Builder(pub);
    }

    public Map<String, Object> asJsonMap() {
        return jsonMap;
    }

    public static class Builder {

        private final String kty;
        /**
         * The modulus value for the RSA key.
         */
        private final String n;
        /**
         * The public exponent of the RSA key.
         */
        private final String e;

        /**
         * key id
         */
        private String kid;

        // Private RSA params, 1st representation
        /**
         * the first prime factor
         */
        private String d;

        // Private RSA params, 2nd representation
        /**
         * the second prime factor
         */
        private String p;
        /**
         * the first factor CRT exponent
         */
        private String q;
        /**
         * the second factor CRT exponent
         */
        private String dp;
        /**
         * The first CRT coefficient
         */
        private String dq;
        /**
         * Must not be {@code null}
         */
        private String qi;

        /**
         * Must not be {@code null}.
         */
        private List<OtherPrimes> oth;

        private Builder(final RSAPublicKey pub) {
            kty = pub.getAlgorithm();
            n = encodeBase64(pub.getModulus());
            e = encodeBase64(pub.getPublicExponent());
        }

        private static String encodeBase64(BigInteger arg) {
            return Base64Codec.encodeToString(BigIntegerUtils.toBytesUnsigned(arg), true);
        }

        /**
         * @param priv
         *            The private RSA key {@link RSAPrivateKey}, used to obtain the private exponent (see RFC 3447,
         *            section 3.2).
         */
        public Builder withPrivateRsa(RSAPrivateKey priv) {
            if (priv instanceof RSAPrivateCrtKey) {
                RSAPrivateCrtKey privCertKey = (RSAPrivateCrtKey) priv;
                this.d = encodeBase64(privCertKey.getPrivateExponent());
                this.p = encodeBase64(privCertKey.getPrimeP());
                this.q = encodeBase64(privCertKey.getPrimeQ());
                this.dp = encodeBase64(privCertKey.getPrimeExponentP());
                this.dq = encodeBase64(privCertKey.getPrimeExponentQ());
                this.qi = encodeBase64(privCertKey.getCrtCoefficient());
            } else if (priv instanceof RSAMultiPrimePrivateCrtKey) {
                RSAMultiPrimePrivateCrtKey privMultiPrimeKey = (RSAMultiPrimePrivateCrtKey) priv;
                this.d = encodeBase64(privMultiPrimeKey.getPrivateExponent());
                this.p = encodeBase64(privMultiPrimeKey.getPrimeP());
                this.q = encodeBase64(privMultiPrimeKey.getPrimeQ());
                this.dp = encodeBase64(privMultiPrimeKey.getPrimeExponentP());
                this.dq = encodeBase64(privMultiPrimeKey.getPrimeExponentQ());
                this.qi = encodeBase64(privMultiPrimeKey.getCrtCoefficient());
                this.oth = toOtherPrimeInfoList(privMultiPrimeKey.getOtherPrimeInfo());
            } else {
                this.d = encodeBase64(priv.getPrivateExponent());
            }
            return this;
        }

        public Builder withKeyId(String keyId) {
            this.kid = keyId;
            return this;
        }

        public JWK build() {
            if (n == null) {
                throw new IllegalArgumentException("The modulus value must not be null");
            }

            if (e == null) {
                throw new IllegalArgumentException("The public exponent value must not be null");
            }

            if (p != null && q != null && dp != null && dq != null && qi != null) {
                return new JWK(kid,
                        kty,
                        n,
                        e,
                        d,
                        p,
                        q,
                        dp,
                        dq,
                        qi,
                        oth != null ? Collections.unmodifiableList(oth) : Collections.emptyList());
            } else if (p == null && q == null && dp == null && dq == null && qi == null && oth == null) {
                return new JWK(kid, kty, null, null, d, null, null, null, null, null, Collections.emptyList());
            } else if (p != null || q != null || dp != null || dq != null || qi != null) {
                if (p == null) {
                    throw new IllegalArgumentException(
                            "Incomplete second private (CRT) representation: The first prime factor must not be null");
                } else if (q == null) {
                    throw new IllegalArgumentException(
                            "Incomplete second private (CRT) representation: The second prime factor must not be null");
                } else if (dp == null) {
                    throw new IllegalArgumentException(
                            "Incomplete second private (CRT) representation: The first factor CRT exponent must not be null");
                } else if (dq == null) {
                    throw new IllegalArgumentException(
                            "Incomplete second private (CRT) representation: The second factor CRT exponent must not be null");
                } else {
                    throw new IllegalArgumentException(
                            "Incomplete second private (CRT) representation: The first CRT coefficient must not be null");
                }
            }

            // No CRT params
            return new JWK(kid, kty, null, null, d, null, null, null, null, null, Collections.emptyList());
        }

        private List<OtherPrimes> toOtherPrimeInfoList(RSAOtherPrimeInfo[] otherPrimeInfoArray) {
            List<OtherPrimes> list = new ArrayList<>();
            if (otherPrimeInfoArray == null) {
                // Return empty list
                return list;
            }

            for (RSAOtherPrimeInfo otherPrimeInfo : otherPrimeInfoArray) {
                list.add(new OtherPrimes(otherPrimeInfo));
            }
            return list;
        }

        private static final class OtherPrimes {
            private final Map<String, Object> jsonMap;

            private OtherPrimes(RSAOtherPrimeInfo otherPrimeInfo) {
                jsonMap = Map.of(
                        // The prime factor.
                        "r",
                        encodeBase64(otherPrimeInfo.getPrime()),
                        // The factor Chinese Remainder Theorem (CRT) exponent.
                        "d",
                        encodeBase64(otherPrimeInfo.getExponent()),
                        // The factor Chinese Remainder Theorem (CRT) coefficient.
                        "t",
                        encodeBase64(otherPrimeInfo.getCrtCoefficient()));
            }
        }
    }
}
