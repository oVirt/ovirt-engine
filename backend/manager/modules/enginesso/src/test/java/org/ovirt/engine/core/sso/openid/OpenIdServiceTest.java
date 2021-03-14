package org.ovirt.engine.core.sso.openid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.resteasy.jose.jws.JWSInput;
import org.jboss.resteasy.jose.jws.crypto.HMACProvider;
import org.jboss.resteasy.jose.jws.crypto.RSAProvider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.sso.api.SsoSession;
import org.ovirt.engine.core.sso.service.OpenIdService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class OpenIdServiceTest {

    private static final String SSO_CLIENT_ID = "AliceClientID";
    private static final Date NOW = new Date();
    private static final String TEST_SERVER_NAME = "test1.redhat.com";
    private static final String SCHEME = "http";
    private static final int TEST_SERVER_PORT = 8081;

    private static KeyPair keyPair;
    private OpenIdService openIdService;
    private HttpServletRequest testHttpRequest;
    private SsoSession testSsoSession;

    @BeforeAll
    public static void generateTestKeyPair() {
        keyPair = OpenIdService.DEFAULT_RSA_KEY_PAIR_GENERATOR.get();
    }

    @BeforeEach
    public void setup() {
        openIdService = new OpenIdService(() -> keyPair);
        testHttpRequest = Mockito.mock(HttpServletRequest.class);
        given(testHttpRequest.getServerName()).willReturn(TEST_SERVER_NAME);
        given(testHttpRequest.getScheme()).willReturn(SCHEME);
        given(testHttpRequest.getServerPort()).willReturn(TEST_SERVER_PORT);

        testSsoSession = getTestSsoSession();

    }

    @Test
    public void shouldCreateOpenIdJWTSignedWithClientSecret() throws IOException {
        String secret = RandomStringUtils.randomAlphanumeric(256);

        doShouldCreateOpenIdJWT(
                () -> openIdService.createJWT(testHttpRequest, testSsoSession, SSO_CLIENT_ID, secret),
                jwsInput -> HMACProvider.verify(jwsInput, secret.getBytes()));
    }

    @Test
    public void shouldCreateOpenIdJWTSignedByRSA() throws IOException {
        doShouldCreateOpenIdJWT(
                () -> openIdService.createJWT(testHttpRequest, testSsoSession, SSO_CLIENT_ID),
                jwsInput -> RSAProvider.verify(jwsInput, keyPair.getPublic()));
    }

    private void doShouldCreateOpenIdJWT(Supplier<String> jwsSupplier, Function<JWSInput, Boolean> signatureVerifier)
            throws IOException {

        // when
        String encoded = jwsSupplier.get();

        // then
        JWSInput input = new JWSInput(encoded, ResteasyProviderFactory.getInstance());
        String msg = input.readContent(String.class);
        JsonNode responseJson = new ObjectMapper().readTree(msg);
        assertThat(responseJson.get("name").textValue()).isEqualTo("Alice");
        assertThat(responseJson.get("family_name").textValue()).isEqualTo("Alice");
        assertThat(responseJson.get("given_name").textValue()).isEqualTo("Alice");
        assertThat(responseJson.get("nonce").textValue()).isEqualTo("testNonce");
        assertThat(responseJson.get("jti").textValue()).isEqualTo("AliceID");
        assertThat(responseJson.get("aud").textValue()).isEqualTo(SSO_CLIENT_ID);
        assertThat(responseJson.get("sub").textValue()).isEqualTo("testUserIdWithProfile");
        assertThat(responseJson.get("preferred_username").textValue()).isEqualTo("testUserIdWithProfile");
        assertThat(responseJson.get("email").textValue()).isEqualTo("testUser@some.org");
        assertThat(responseJson.get("acr").textValue()).isEqualTo("0");
        String expectedIss = SCHEME + "://" + TEST_SERVER_NAME + ":" + TEST_SERVER_PORT;
        assertThat(responseJson.get("iss").textValue()).isEqualTo(expectedIss);
        assertThat(responseJson.get("exp").longValue()).isEqualTo(NOW.getTime() + 30000 * 60);
        assertThat(responseJson.get("iat").longValue()).isEqualTo(NOW.getTime());
        assertThat(responseJson.get("auth_time").longValue()).isEqualTo(NOW.getTime());

        assertThat(signatureVerifier.apply(input)).isTrue();
    }

    @Test
    public void shouldCreateJWKbyHand() throws IOException {
        Map<String, Object> jwk = openIdService.getJWK();

        ObjectMapper mapper = new ObjectMapper().deactivateDefaultTyping();
        String jwkString =
                mapper.writeValueAsString(jwk);

        // at least this is proper json and no exception is thrown
        assertThat(jwkString).isNotEmpty();
    }

    private SsoSession getTestSsoSession() {
        SsoSession session = Mockito.mock(SsoSession.class);
        given(session.getAuthTime()).willReturn(NOW);
        given(session.getUserIdWithProfile()).willReturn("testUserIdWithProfile");

        ExtMap principalRecord = new ExtMap();
        principalRecord.put(Authz.PrincipalRecord.EMAIL, "testUser@some.org");
        principalRecord.put(Authz.PrincipalRecord.FIRST_NAME, "Alice");
        principalRecord.put(Authz.PrincipalRecord.ID, "AliceID");
        given(session.getPrincipalRecord()).willReturn(principalRecord);
        given(session.getOpenIdNonce()).willReturn("testNonce");
        return session;
    }

}
