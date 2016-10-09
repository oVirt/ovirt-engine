package org.ovirt.engine.core.bll.exportimport;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VnicProfileDao;

@RunWith(MockitoJUnitRunner.class)
public class TargetVnicProfileFinderTest {

    private static final String NETWORK_NAME = "network name";
    private static final String VNIC_PROFILE1_NAME = "vnic profile1 name";
    private static final String VNIC_PROFILE2_NAME = "vnic profile2 name";
    private static final Guid VNIC_PROFILE1_ID = Guid.newGuid();
    private static final Guid VNIC_PROFILE2_ID = Guid.newGuid();

    @InjectMocks
    private TargetVnicProfileFinder underTest;

    @Mock
    private VnicProfileDao mockVnicProfileDao;

    private VnicProfile vnicProfile;
    private List<ExternalVnicProfileMapping> externalVnicProfileMappings;
    private ExternalVnicProfileMapping externalVnicProfileMapping1;
    private ExternalVnicProfileMapping externalVnicProfileMapping2;

    @Before
    public void setUp() {
        externalVnicProfileMappings = new ArrayList<>();
        externalVnicProfileMapping1 =
                new ExternalVnicProfileMapping(NETWORK_NAME, VNIC_PROFILE1_NAME, VNIC_PROFILE1_ID);
        externalVnicProfileMapping2 =
                new ExternalVnicProfileMapping(NETWORK_NAME, VNIC_PROFILE2_NAME, VNIC_PROFILE2_ID);
        vnicProfile = new VnicProfile();
        when(mockVnicProfileDao.get(VNIC_PROFILE1_ID)).thenReturn(vnicProfile);
    }

    @Test
    public void testFindTargetVnicProfileEmptyMapping() {
        final VnicProfile actual =
                underTest.findTargetVnicProfile(NETWORK_NAME, VNIC_PROFILE1_NAME, externalVnicProfileMappings);

        assertThat(actual, nullValue());
    }

    @Test
    public void testFindTargetVnicProfile() {
        externalVnicProfileMappings.add(externalVnicProfileMapping1);
        externalVnicProfileMappings.add(externalVnicProfileMapping2);

        final VnicProfile actual =
                underTest.findTargetVnicProfile(NETWORK_NAME, VNIC_PROFILE1_NAME, externalVnicProfileMappings);

        assertThat(actual, is(vnicProfile));
        verify(mockVnicProfileDao).get(VNIC_PROFILE1_ID);
    }

    @Test
    public void testFindTargetVnicProfileNotFound() {
        externalVnicProfileMappings.add(externalVnicProfileMapping1);
        externalVnicProfileMappings.add(externalVnicProfileMapping2);

        final VnicProfile actual =
                underTest.findTargetVnicProfile(NETWORK_NAME, VNIC_PROFILE2_NAME, externalVnicProfileMappings);

        assertThat(actual, nullValue());
        verify(mockVnicProfileDao).get(VNIC_PROFILE2_ID);
    }
}
