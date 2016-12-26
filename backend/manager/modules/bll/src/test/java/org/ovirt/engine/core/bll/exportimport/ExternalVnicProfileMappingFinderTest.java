package org.ovirt.engine.core.bll.exportimport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class ExternalVnicProfileMappingFinderTest {

    private static final String NETWORK_NAME = "network name";
    private static final String VNIC_PROFILE1_NAME = "vnic profile1 name";
    private static final String VNIC_PROFILE2_NAME = "vnic profile2 name";
    private static final Guid TARGET_VNIC_PROFILE_ID = Guid.newGuid();

    private ExternalVnicProfileMappingFinder underTest;

    private List<ExternalVnicProfileMapping> externalVnicProfileMappings;
    private ExternalVnicProfileMapping externalVnicProfileMapping1;
    private ExternalVnicProfileMapping externalVnicProfileMapping2;
    private ExternalVnicProfileMapping emptySourceMapping;

    @Before
    public void setUp() {
        underTest = new ExternalVnicProfileMappingFinder();

        externalVnicProfileMapping1 =
                new ExternalVnicProfileMapping(NETWORK_NAME, VNIC_PROFILE1_NAME, TARGET_VNIC_PROFILE_ID);
        externalVnicProfileMapping2 =
                new ExternalVnicProfileMapping(NETWORK_NAME, VNIC_PROFILE2_NAME, null);
        externalVnicProfileMappings = asList(externalVnicProfileMapping1, externalVnicProfileMapping2);
        emptySourceMapping = new ExternalVnicProfileMapping(null, null, TARGET_VNIC_PROFILE_ID);
    }

    @Test
    public void testFindMappingEntryEmptyInput() {
        final Optional<ExternalVnicProfileMapping> actual =
                underTest.findMappingEntry(NETWORK_NAME, VNIC_PROFILE1_NAME, emptyList());

        assertThat(actual, is(Optional.empty()));
    }

    @Test
    public void testFindMappingEntry() {
        final Optional<ExternalVnicProfileMapping> actual =
                underTest.findMappingEntry(NETWORK_NAME, VNIC_PROFILE1_NAME, externalVnicProfileMappings);

        assertThat(actual.get(), sameInstance(externalVnicProfileMapping1));
    }

    @Test
    public void testFindMappingEntryEmptyExternal() {
        final Optional<ExternalVnicProfileMapping> actual =
                underTest.findMappingEntry(NETWORK_NAME, VNIC_PROFILE1_NAME, singletonList(emptySourceMapping));

        assertFalse(actual.isPresent());
    }

    @Test
    public void testFindMappingEntryEmptySourceVnicProfile() {
        final Optional<ExternalVnicProfileMapping> actual =
                underTest.findMappingEntry(null, null, singletonList(emptySourceMapping));

        assertThat(actual.get(), sameInstance(emptySourceMapping));
    }

    @Test
    public void testFindMappingEntryNotFound() {
        final Optional<ExternalVnicProfileMapping> actual =
                underTest.findMappingEntry("not" + NETWORK_NAME, VNIC_PROFILE2_NAME, externalVnicProfileMappings);

        assertThat(actual, is(Optional.empty()));
    }
}
