package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.sso.utils.SsoSession;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class OpenIdUtils {

    private static KeyPair keyPair;

    static {
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(1024);
            keyPair = keyGenerator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to generate KeyPair", e);
        }
    }

    public static String getJson(Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper().disableDefaultTyping();
        return mapper.writeValueAsString(obj);
    }

    /**
     * Get the Java Web Key used to sign userinfo jwt. HS256 used to sign token's jwt does not need to be included here
     * as HS256 used client secret to sign the jwt which the client already has.
     */
    public static JWK getJWK() {
        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID("oVirt") // Give the key some ID (optional)
                .build();
    }

    /**
     * Create a Java web token and sign with the RSA key. Used by the openid userinfo endpoint to send userinfo back.
     */
    public static String createJWT(HttpServletRequest request, SsoSession ssoSession, String clientId)
            throws JOSEException {
        // Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(keyPair.getPrivate());
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256),
                createJWTClaimSet(request, ssoSession, clientId));
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    /**
     * Create a Java web token and sign with the client secret. Used by openid token endpoint to get id_token along with
     * access_token.
     */
    public static String createJWT(HttpServletRequest request, SsoSession ssoSession, String clientId, String clientSecret)
            throws NoSuchAlgorithmException, JOSEException {
        JWSSigner signer = new MACSigner(clientSecret);
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256),
                createJWTClaimSet(request, ssoSession, clientId));
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    private static JWTClaimsSet createJWTClaimSet(HttpServletRequest request, SsoSession ssoSession, String clientId) {
        String serverName = request.getServerName();
        String issuer = String.format("%s://%s:%s",
                request.getScheme(),
                InetAddressUtils.isIPv6Address(serverName) ? String.format("[%s]", serverName) : serverName,
                request.getServerPort());

        Date expirationTime = new Date(ssoSession.getAuthTime().getTime() + 30000 * 60);
        // Compose the JWT claims set
        JWTClaimsSet.Builder jwtClaimsBuilder = new JWTClaimsSet.Builder()
                .jwtID(ssoSession.getPrincipalRecord().get(Authz.PrincipalRecord.ID))
                .issueTime(ssoSession.getAuthTime())
                .expirationTime(expirationTime)
                .issuer(issuer)
                .subject(ssoSession.getUserIdWithProfile())
                .audience(clientId)
                .claim("acr", "0")
                .claim("auth_time", ssoSession.getAuthTime())
                .claim("sub", ssoSession.getUserIdWithProfile())
                .claim("preferred_username", ssoSession.getUserIdWithProfile())
                .claim("email", ssoSession.getPrincipalRecord().<String>get(Authz.PrincipalRecord.EMAIL))
                .claim("name", ssoSession.getPrincipalRecord().<String>get(Authz.PrincipalRecord.FIRST_NAME))
                .claim("family_name", ssoSession.getPrincipalRecord().<String>get(Authz.PrincipalRecord.FIRST_NAME))
                .claim("given_name", ssoSession.getPrincipalRecord().<String>get(Authz.PrincipalRecord.FIRST_NAME));
        if (StringUtils.isNotEmpty(ssoSession.getOpenIdNonce())) {
            jwtClaimsBuilder.claim("nonce", ssoSession.getOpenIdNonce());
        }
        return jwtClaimsBuilder.build();
    }
}
