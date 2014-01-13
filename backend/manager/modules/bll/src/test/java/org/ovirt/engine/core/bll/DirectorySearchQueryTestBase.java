package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.mockito.Mock;
import org.ovirt.engine.core.authentication.AuthenticationProfile;
import org.ovirt.engine.core.authentication.AuthenticationProfileManager;
import org.ovirt.engine.core.authentication.Authenticator;
import org.ovirt.engine.core.authentication.AuthenticatorManager;
import org.ovirt.engine.core.authentication.Directory;
import org.ovirt.engine.core.authentication.DirectoryManager;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

public abstract class DirectorySearchQueryTestBase {
    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(
        mockConfig(ConfigValues.LDAPSecurityAuthentication, "SIMPLE"),
        mockConfig(ConfigValues.SearchResultsLimit, 100),
        mockConfig(ConfigValues.AuthenticationMethod, "LDAP")
    );

    // The name of the authenticator, directory and authentication profile used in the test:
    public static final String NAME = RandomUtils.instance().nextString(10);

    // Mocks for the authentication subsystem:
    @Mock protected Directory directoryMock;
    @Mock protected Authenticator authenticatorMock;
    @Mock protected AuthenticationProfile profileMock;

    @Before
    public void setUp() {
        initMocks(this);

        AuthenticatorManager.getInstance().registerAuthenticator(NAME, authenticatorMock);

        doReturn(NAME).when(directoryMock).getName();
        DirectoryManager.getInstance().registerDirectory(NAME, directoryMock);

        doReturn(NAME).when(profileMock).getName();
        doReturn(authenticatorMock).when(profileMock).getAuthenticator();
        doReturn(directoryMock).when(profileMock).getDirectory();
        AuthenticationProfileManager.getInstance().registerProfile(NAME, profileMock);
    }

    @After
    public void tearDown() {
        AuthenticatorManager.getInstance().clear();
        DirectoryManager.getInstance().clear();
        AuthenticationProfileManager.getInstance().clear();
    }
}
