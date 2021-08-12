package org.ovirt.engine.core.sso.service;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.jboss.resteasy.jose.jws.JWSBuilder;
import org.jboss.resteasy.jwt.JsonSerialization;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.sso.api.SsoSession;
import org.ovirt.engine.core.sso.api.jwk.JWK;
import org.ovirt.engine.core.sso.api.jwt.JWT;
import org.ovirt.engine.core.sso.api.jwt.JWTException;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class OpenIdService {

    public static final String OVIRT = "oVirt";
    public static final Supplier<KeyPair> DEFAULT_RSA_KEY_PAIR_GENERATOR = () -> {
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(1024);
            return keyGenerator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to generate KeyPair", e);
        }
    };
    private final ObjectMapper mapper = new ObjectMapper().deactivateDefaultTyping();

    private final KeyPair keyPair;

    @SuppressWarnings("unused") // injectable
    public OpenIdService() {
        this(DEFAULT_RSA_KEY_PAIR_GENERATOR);
    }

    // for testing
    public OpenIdService(Supplier<KeyPair> keyPairGenerator) {
        keyPair = keyPairGenerator.get();
    }

    public String getJson(Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }

    /**
     * Get the Java Web Key used to sign userinfo jwt. HS256 used to sign token's jwt does not need to be included here
     * as HS256 used client secret to sign the jwt which the client already has.
     */
    public Map<String, Object> getJWK() {
        RSAPublicKey rsa = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey rsaPrivate = (RSAPrivateKey) keyPair.getPrivate();
        return JWK.builder(rsa).withPrivateRsa(rsaPrivate).withKeyId(OVIRT).build().asJsonMap();
    }

    /**
     * Create a Java web token and sign with the RSA key. Used by the openid userinfo endpoint to send userinfo back.
     *
     * @throws JWTException
     *             RuntimeException thrown when unable to build JWT
     */
    public String createJWT(HttpServletRequest request, SsoSession ssoSession, String clientId) throws JWTException {
        String plainToken = createUnencodedJWT(request, ssoSession, clientId);
        // Create RSA-signer with the private key
        return new JWSBuilder()
                .contentType(MediaType.APPLICATION_JSON)
                .content(plainToken, MediaType.APPLICATION_JSON_TYPE)
                .rsa256(keyPair.getPrivate());
    }

    /**
     * Create a Java web token and sign with the client secret. Used by openid token endpoint to get id_token along with
     * access_token.
     *
     * @throws JWTException
     *             RuntimeException thrown when unable to build JWT
     */
    public String createJWT(HttpServletRequest request, SsoSession ssoSession, String clientId, String clientSecret)
            throws JWTException {
        String plainToken = createUnencodedJWT(request, ssoSession, clientId);

        return new JWSBuilder()
                .contentType(MediaType.APPLICATION_JSON)
                .content(plainToken, MediaType.APPLICATION_JSON_TYPE)
                .hmac256(clientSecret.getBytes());

    }

    /**
     * @param request
     *            HTTP servlet request
     * @param ssoSession
     *            SSO session
     * @param clientId
     *            Client Id
     *
     * @return Encoded JWT token with OpenId extensions
     *
     * @throws JWTException
     *             RuntimeException thrown when unable to build JWT
     */
    public String createUnencodedJWT(HttpServletRequest request, SsoSession ssoSession, String clientId)
            throws JWTException {
        long expirationTime = ssoSession.getAuthTime().getTime() + 30000 * 60;
        String serverName = request.getServerName();
        String issuer = String.format("%s://%s:%s",
                request.getScheme(),
                InetAddressUtils.isIPv6Address(serverName) ? String.format("[%s]", serverName) : serverName,
                request.getServerPort());

        // Open ID JWT
        JWT token = new JWT()
                .acr("0")
                .authTime(ssoSession.getAuthTime())
                .sub(ssoSession.getUserIdWithProfile())
                .preferredUserName(ssoSession.getUserIdWithProfile())
                .email(ssoSession.getPrincipalRecord().get(Authz.PrincipalRecord.EMAIL))
                .familyName(ssoSession.getPrincipalRecord().get(Authz.PrincipalRecord.FIRST_NAME))
                .givenName(ssoSession.getPrincipalRecord().get(Authz.PrincipalRecord.FIRST_NAME))
                .name(ssoSession.getPrincipalRecord().get(Authz.PrincipalRecord.FIRST_NAME));
        if (StringUtils.isNotEmpty(ssoSession.getOpenIdNonce())) {
            token.nonce(ssoSession.getOpenIdNonce());
        }
        // regular JWT
        token.id(ssoSession.getPrincipalRecord().get(Authz.PrincipalRecord.ID))
                .issuedAt(ssoSession.getAuthTime().getTime())
                .expiration(expirationTime)
                .issuer(issuer)
                .audience(clientId);

        try {
            return JsonSerialization.toString(token, true);
        } catch (Exception e) {
            throw new JWTException(JWTException.ErrorCode.CANNOT_SERIALIZE_PLAIN_JWT, e);
        }
    }
}
