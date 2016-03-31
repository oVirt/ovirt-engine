package org.ovirt.engine.core.bll.validator.network;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.common.config.ConfigValues.NetworkExclusivenessPermissiveValidation;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Version;

@RunWith(MockitoJUnitRunner.class)
public class NetworkExclusivenessValidatorResolverTest {

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule(
            mockConfig(NetworkExclusivenessPermissiveValidation, Version.v3_5.toString(), false),
            mockConfig(NetworkExclusivenessPermissiveValidation, Version.v3_6.toString(), true));

    private NetworkExclusivenessValidatorResolver underTest;

    @Mock
    private NetworkExclusivenessValidator legacyNetworkExclusivenessValidator;
    @Mock
    private NetworkExclusivenessValidator vlanUntaggedNetworkExclusivenessValidator;

    @Before
    public void setUp() throws Exception {
        underTest = new NetworkExclusivenessValidatorResolver(legacyNetworkExclusivenessValidator,
                vlanUntaggedNetworkExclusivenessValidator);
    }

    @Test
    public void testResolveNetworkExclusivenessValidatorNullInput() {
        final NetworkExclusivenessValidator actual = underTest.resolveNetworkExclusivenessValidator(null);

        assertThat(actual, sameInstance(legacyNetworkExclusivenessValidator));
    }

    @Test
    public void testResolveNetworkExclusivenessValidatorEmptyInput() {
        final NetworkExclusivenessValidator actual =
                underTest.resolveNetworkExclusivenessValidator(Collections.<Version>emptySet());

        assertThat(actual, sameInstance(legacyNetworkExclusivenessValidator));
    }

    @Test
    public void testResolveNetworkExclusivenessValidatorNew() {
        final HashSet<Version> supportedVersions = new HashSet<>(Arrays.asList(Version.v3_5, Version.v3_6));
        final NetworkExclusivenessValidator actual = underTest.resolveNetworkExclusivenessValidator(supportedVersions);

        assertThat(actual, sameInstance(vlanUntaggedNetworkExclusivenessValidator));
    }

    @Test
    public void testResolveNetworkExclusivenessValidatorEveryLegacyVersion() {

        final Version version = Version.v3_5;
        final NetworkExclusivenessValidator actual =
                underTest.resolveNetworkExclusivenessValidator(Collections.singleton(version));

        assertThat(version.getValue() + " supposed to return the legacy validator",
                actual,
                sameInstance(legacyNetworkExclusivenessValidator));
    }
}
