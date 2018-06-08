package org.ovirt.engine.core.bll.provider.network.openstack;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;

import com.woorea.openstack.base.client.OpenStackResponse;
import com.woorea.openstack.keystone.v3.model.Authentication;

@ExtendWith(MockitoExtension.class)
public class KeystoneV3TokenProviderTest {
    private static final String AUTH_URL = "auth url";
    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";
    private static final String USER_DOMAIN_NAME = "user domain name";
    private static final String PROJECT_NAME = "project name";
    private static final String PROJECT_DOMAIN_NAME = "project domain name";
    private static final String TOKEN_ID = "token id";

    @Captor
    private ArgumentCaptor<Authentication> authenticationCaptor;

    private KeystoneV3TokenProvider tokenProvider;

    private Provider<OpenstackNetworkProviderProperties> provider = new Provider<>();

    @BeforeEach
    public void setUp() {
        provider.setAuthUrl(AUTH_URL);
        provider.setUsername(USERNAME);
        provider.setPassword(PASSWORD);

        OpenstackNetworkProviderProperties providerProperties = new OpenstackNetworkProviderProperties();
        providerProperties.setUserDomainName(USER_DOMAIN_NAME);
        provider.setAdditionalProperties(providerProperties);

        tokenProvider = spy(new KeystoneV3TokenProvider<>(provider));

        doReturn(new FakeResponse(TOKEN_ID)).when(tokenProvider).requestToken(any());
    }

    @Test
    public void testGetToken() {

        assertEquals(TOKEN_ID, tokenProvider.getToken());
        verifyAuthentication(false);
    }

    @Test
    public void testGetTokenWithScope() {

        OpenstackNetworkProviderProperties providerProperties = provider.getAdditionalProperties();
        providerProperties.setProjectName(PROJECT_NAME);
        providerProperties.setProjectDomainName(PROJECT_DOMAIN_NAME);

        assertEquals(TOKEN_ID, tokenProvider.getToken());
        verifyAuthentication(true);
    }

    private void verifyAuthentication(boolean hasProject) {

        verify(tokenProvider).requestToken(authenticationCaptor.capture());

        Authentication authentication = authenticationCaptor.getValue();
        assertThat(authentication, notNullValue());

        Authentication.Identity.Password.User user = authentication.getIdentity().getPassword().getUser();
        assertThat(user, notNullValue());
        assertThat(user.getPassword(), is(PASSWORD));
        assertThat(user.getName(), is(USERNAME));
        Authentication.Identity.Password.User.Domain domain = user.getDomain();
        assertThat(domain, notNullValue());
        assertThat(domain.getName(), is(USER_DOMAIN_NAME));

        if (hasProject) {
            Authentication.Scope.Project project = authentication.getScope().getProject();
            assertThat(project, notNullValue());
            assertThat(project.getName(), is(PROJECT_NAME));
            Authentication.Scope.Project.Domain projectDomain = project.getDomain();
            assertThat(projectDomain, notNullValue());
            assertThat(projectDomain.getName(), is(PROJECT_DOMAIN_NAME));
        } else {
            assertNull(authentication.getScope());
        }
    }

    @Test
    public void testExpireTokenCachesToken() {
        String result1 = tokenProvider.getToken();
        String result2 = tokenProvider.getToken();

        verify(tokenProvider, times(1)).requestToken(any());

        assertThat(result1, is(TOKEN_ID));
        assertThat(result2, is(TOKEN_ID));
    }

    @Test
    public void testExpireToken() {
        String result1 = tokenProvider.getToken();
        verify(tokenProvider, times(1)).requestToken(any());
        tokenProvider.expireToken();
        String result2 = tokenProvider.getToken();
        verify(tokenProvider, times(2)).requestToken(any());

        assertThat(result1, is(TOKEN_ID));
        assertThat(result2, is(TOKEN_ID));
    }

    private class FakeResponse implements OpenStackResponse {
        private final String token_id;

        FakeResponse(String token_id) {
            this.token_id = token_id;
        }
        @Override
        public <T> T getEntity(Class<T> aClass) {
            return null;
        }

        @Override
        public InputStream getInputStream() {
            return null;
        }

        @Override
        public String header(String s) {
            assertEquals("X-Subject-Token", s);
            return token_id;
        }

        @Override
        public Map<String, String> headers() {
            return null;
        }
    }
}
