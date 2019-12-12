package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.jose.jws.JWSBuilder;
import org.jboss.resteasy.jwt.JsonSerialization;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.sso.utils.SsoSession;
import org.ovirt.engine.core.sso.utils.jwk.JWK;
import org.ovirt.engine.core.sso.utils.jwt.OpenIdJWT;

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
    static Map<String, Object> getJWK() {
        RSAPublicKey rsa = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey rsaPrivate = (RSAPrivateKey) keyPair.getPrivate();
        return JWK.builder(rsa).withPrivateRsa(rsaPrivate).withKeyId("oVirt").build().asJsonMap();
    }

    /**
     * Create a Java web token and sign with the RSA key. Used by the openid userinfo endpoint to send userinfo back.
     *
     * @throws JWTException RuntimeException thrown when unable to build JWT
     */
    static String createJWT(HttpServletRequest request, SsoSession ssoSession, String clientId) {
        String plainToken = buildUnencodedOpenIDJWT(request, ssoSession, clientId);
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
     * @throws JWTException RuntimeException thrown when unable to build JWT
     */
    static String createJWT(HttpServletRequest request, SsoSession ssoSession, String clientId, String clientSecret) {
        String plainToken = buildUnencodedOpenIDJWT(request, ssoSession, clientId);

        String encoded = new JWSBuilder()
                .contentType(MediaType.APPLICATION_JSON)
                .content(plainToken, MediaType.APPLICATION_JSON_TYPE)
                .hmac256(clientSecret.getBytes());
        return encoded;

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
     * @throws JWTException RuntimeException thrown when unable to build JWT
     */
    private static String buildUnencodedOpenIDJWT(HttpServletRequest request, SsoSession ssoSession, String clientId) {
        long expirationTime = ssoSession.getAuthTime().getTime() + 30000 * 60;
        String serverName = request.getServerName();
        String issuer = String.format("%s://%s:%s",
                request.getScheme(),
                InetAddressUtils.isIPv6Address(serverName) ? String.format("[%s]", serverName) : serverName,
                request.getServerPort());

        // Open ID JWT
        OpenIdJWT token = new OpenIdJWT()
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
