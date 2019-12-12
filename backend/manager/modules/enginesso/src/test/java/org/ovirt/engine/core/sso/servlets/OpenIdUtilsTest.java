package org.ovirt.engine.core.sso.servlets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.RandomStringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.jose.jws.JWSInput;
import org.jboss.resteasy.jose.jws.crypto.HMACProvider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.sso.utils.SsoSession;

class OpenIdUtilsTest {

    @Test
    public void shouldCreateOpenIdJWT() throws IOException {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getServerName()).willReturn("test1.redhat.com");
        given(request.getScheme()).willReturn("http");
        given(request.getServerPort()).willReturn(8081);

        Date now = new Date();
        SsoSession session = mock(SsoSession.class);
        given(session.getAuthTime()).willReturn(now);
        given(session.getUserIdWithProfile()).willReturn("testUserIdWithProfile");

        ExtMap principalRecord = new ExtMap();
        principalRecord.put(Authz.PrincipalRecord.EMAIL, "testUser@some.org");
        principalRecord.put(Authz.PrincipalRecord.FIRST_NAME, "Alice");
        principalRecord.put(Authz.PrincipalRecord.ID, "AliceID");
        given(session.getPrincipalRecord()).willReturn(principalRecord);

        given(session.getOpenIdNonce()).willReturn("testNonce");
        String secret = RandomStringUtils.randomAlphanumeric(256);

        // when
        String encoded = OpenIdUtils.createJWT(request, session, "AliceClientID", secret);

        // then
        JWSInput input = new JWSInput(encoded, ResteasyProviderFactory.getInstance());
        String msg = input.readContent(String.class);
        JsonNode responseJson = new ObjectMapper().readTree(msg);
        assertThat(responseJson.get("name").getTextValue()).isEqualTo("Alice");
        assertThat(responseJson.get("family_name").getTextValue()).isEqualTo("Alice");
        assertThat(responseJson.get("given_name").getTextValue()).isEqualTo("Alice");
        assertThat(responseJson.get("nonce").getTextValue()).isEqualTo("testNonce");
        assertThat(responseJson.get("jti").getTextValue()).isEqualTo("AliceID");
        assertThat(responseJson.get("aud").getTextValue()).isEqualTo("AliceClientID");
        assertThat(responseJson.get("sub").getTextValue()).isEqualTo("testUserIdWithProfile");
        assertThat(responseJson.get("preferred_username").getTextValue()).isEqualTo("testUserIdWithProfile");
        assertThat(responseJson.get("email").getTextValue()).isEqualTo("testUser@some.org");
        assertThat(responseJson.get("acr").getTextValue()).isEqualTo("0");
        assertThat(responseJson.get("iss").getTextValue()).isEqualTo("http://test1.redhat.com:8081");
        assertThat(responseJson.get("exp").getLongValue()).isEqualTo(now.getTime() + 30000 * 60);
        assertThat(responseJson.get("iat").getLongValue()).isEqualTo(now.getTime());
        assertThat(responseJson.get("auth_time").getLongValue()).isEqualTo(now.getTime());

        assertThat(HMACProvider.verify(input, secret.getBytes())).isTrue();
    }

}
