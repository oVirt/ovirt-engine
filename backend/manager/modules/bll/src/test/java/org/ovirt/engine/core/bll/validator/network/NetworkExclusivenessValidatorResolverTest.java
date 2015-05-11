package org.ovirt.engine.core.bll.validator.network;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.common.config.ConfigValues.NetworkExclusivenessPermissiveValidation;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class NetworkExclusivenessValidatorResolverTest {

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule(
            mockConfig(NetworkExclusivenessPermissiveValidation, Version.v3_0.toString(), false),
            mockConfig(NetworkExclusivenessPermissiveValidation, Version.v3_1.toString(), false),
            mockConfig(NetworkExclusivenessPermissiveValidation, Version.v3_2.toString(), false),
            mockConfig(NetworkExclusivenessPermissiveValidation, Version.v3_3.toString(), false),
            mockConfig(NetworkExclusivenessPermissiveValidation, Version.v3_4.toString(), false),
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
    public void testResolveNetworkExclusivenessValidatorAllLegacy() {

        final Set<Version> supportedVersions = new HashSet<>();
        for (int i = 0; i < 6; i++) {
            supportedVersions.add(new Version(3, i));
        }
        final NetworkExclusivenessValidator actual =
                underTest.resolveNetworkExclusivenessValidator(supportedVersions);

        assertThat(actual, sameInstance(legacyNetworkExclusivenessValidator));
    }

    @Test
    public void testResolveNetworkExclusivenessValidatorEveryLegacyVersion() {

        for (int i = 0; i < 6; i++) {
            final Version version = new Version(3, i);
            final NetworkExclusivenessValidator actual =
                    underTest.resolveNetworkExclusivenessValidator(Collections.singleton(version));

            assertThat(version.getValue() + " supposed to return the legacy validator",
                    actual,
                    sameInstance(legacyNetworkExclusivenessValidator));
        }
    }
}
