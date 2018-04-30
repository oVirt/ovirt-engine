package org.ovirt.engine.core.bll.provider.network.openstack;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;

import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.api.TokensResource;
import com.woorea.openstack.keystone.api.TokensResource.Authenticate;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.Token;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword.PasswordCredentials;

@ExtendWith(MockitoExtension.class)
public class ExternalNetworkTokenProviderTest {

    private static final String AUTH_URL = "authUrl";
    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";
    private static final String TOKEN_ID = "token id";

    private static final Token token = new Token();

    @Mock
    private Keystone mockKeystone;

    @Mock
    private TokensResource mockTokensResource;

    @Mock
    private Authenticate mockAuthenticate;

    @Mock
    private Access mockAccess;

    @Captor
    private ArgumentCaptor<UsernamePassword> usernamePasswordCaptor;

    private ExternalNetworkTokenProvider tokenProvider;

    @BeforeEach
    public void setUp() {
        Provider<OpenstackNetworkProviderProperties> provider = new Provider<>();
        provider.setAuthUrl(AUTH_URL);
        provider.setUsername(USERNAME);
        provider.setPassword(PASSWORD);
        tokenProvider = spy(new ExternalNetworkTokenProvider(provider));

        doReturn(mockKeystone).when(tokenProvider).createKeystone(AUTH_URL);
        doReturn(TOKEN_ID).when(tokenProvider).getTokenId(same(token));

        when(mockKeystone.tokens()).thenReturn(mockTokensResource);
        when(mockTokensResource.authenticate(any())).thenReturn(mockAuthenticate);
        when(mockAuthenticate.execute()).thenReturn(mockAccess);
        when(mockAccess.getToken()).thenReturn(token);
    }

    @Test
    public void testGetToken() {

        assertEquals(TOKEN_ID, tokenProvider.getToken());

        verify(mockTokensResource).authenticate(usernamePasswordCaptor.capture());
        verifyUsernamePassword();
    }

    private void verifyUsernamePassword() {
        final PasswordCredentials passwordCredentials = usernamePasswordCaptor.getValue().getPasswordCredentials();
        assertThat(passwordCredentials, notNullValue());
        assertThat(passwordCredentials.getPassword(), is(PASSWORD));
        assertThat(passwordCredentials.getUsername(), is(USERNAME));
    }

    @Test
    public void testExpireTokenCachesToken() {
        String result1 = tokenProvider.getToken();
        String result2 = tokenProvider.getToken();

        verify(mockAuthenticate, times(1)).execute();

        assertThat(result1, is(TOKEN_ID));
        assertThat(result2, is(TOKEN_ID));
    }

    @Test
    public void testExpireToken() {
        String result1 = tokenProvider.getToken();
        verify(mockAuthenticate, times(1)).execute();
        tokenProvider.expireToken();
        String result2 = tokenProvider.getToken();
        verify(mockAuthenticate, times(2)).execute();

        assertThat(result1, is(TOKEN_ID));
        assertThat(result2, is(TOKEN_ID));
    }
}
