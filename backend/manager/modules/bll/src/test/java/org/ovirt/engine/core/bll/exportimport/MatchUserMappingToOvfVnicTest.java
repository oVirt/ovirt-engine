package org.ovirt.engine.core.bll.exportimport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicsContext;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicsHandlers.MatchUserMappingToOvfVnic;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
public class MatchUserMappingToOvfVnicTest {

    private static final String NETWORK_NAME = "network name";
    private static final String VNIC_PROFILE1_NAME = "vnic profile1 name";
    private static final String VNIC_PROFILE2_NAME = "vnic profile2 name";
    private static final Guid TARGET_VNIC_PROFILE_ID = Guid.newGuid();

    private MatchUserMappingToOvfVnic underTest;

    private List<ExternalVnicProfileMapping> externalVnicProfileMappings;
    private ExternalVnicProfileMapping externalVnicProfileMapping1;
    private ExternalVnicProfileMapping externalVnicProfileMapping2;
    private ExternalVnicProfileMapping emptySourceMapping;

    @BeforeEach
    public void setUp() {
        underTest = new MatchUserMappingToOvfVnic();
        externalVnicProfileMapping1 =
                new ExternalVnicProfileMapping(NETWORK_NAME, VNIC_PROFILE1_NAME, TARGET_VNIC_PROFILE_ID);
        externalVnicProfileMapping2 =
                new ExternalVnicProfileMapping(NETWORK_NAME, VNIC_PROFILE2_NAME, null);
        externalVnicProfileMappings = asList(externalVnicProfileMapping1, externalVnicProfileMapping2);
        emptySourceMapping = new ExternalVnicProfileMapping(null, null, TARGET_VNIC_PROFILE_ID);
    }

    @Test
    public void testFindMappingEntryEmptyInput() {
        VmNetworkInterface vnic = new VmNetworkInterface();
        MapVnicsContext ctx = new MapVnicsContext();
        ctx.setUserMappings(emptyList());
        ctx.setOvfVnics(singletonList(vnic));
        underTest.handle(ctx);
        assertEquals(1, ctx.getMatched().size());
        assertNull(ctx.getMatched().get(vnic));
    }

    @Test
    public void testFindMappingEntry() {
        VmNetworkInterface vnic = new VmNetworkInterface();
        vnic.setNetworkName(NETWORK_NAME);
        vnic.setVnicProfileName(VNIC_PROFILE1_NAME);
        MapVnicsContext ctx = new MapVnicsContext();
        ctx.setUserMappings(externalVnicProfileMappings);
        ctx.setOvfVnics(singletonList(vnic));
        underTest.handle(ctx);
        assertThat(ctx.getMatched().get(vnic), sameInstance(externalVnicProfileMapping1));
    }

    @Test
    public void testFindMappingEntryEmptyExternal() {
        VmNetworkInterface vnic = new VmNetworkInterface();
        vnic.setNetworkName(NETWORK_NAME);
        vnic.setVnicProfileName(VNIC_PROFILE1_NAME);
        MapVnicsContext ctx = new MapVnicsContext();
        ctx.setUserMappings(singletonList(emptySourceMapping));
        ctx.setOvfVnics(singletonList(vnic));
        underTest.handle(ctx);
        assertNull(ctx.getMatched().get(vnic));
    }

    @Test
    public void testFindMappingEntryEmptySourceVnicProfile() {
        VmNetworkInterface vnic = new VmNetworkInterface();
        vnic.setNetworkName(null);
        vnic.setVnicProfileName(null);
        MapVnicsContext ctx = new MapVnicsContext();
        ctx.setUserMappings(singletonList(emptySourceMapping));
        ctx.setOvfVnics(singletonList(vnic));
        underTest.handle(ctx);
        assertThat(ctx.getMatched().get(vnic), sameInstance(emptySourceMapping));
    }

    @Test
    public void testFindMappingEntryNotFound() {
        VmNetworkInterface vnic = new VmNetworkInterface();
        vnic.setNetworkName("not" + NETWORK_NAME);
        vnic.setVnicProfileName(VNIC_PROFILE2_NAME);
        MapVnicsContext ctx = new MapVnicsContext();
        ctx.setUserMappings(externalVnicProfileMappings);
        ctx.setOvfVnics(singletonList(vnic));
        underTest.handle(ctx);
        assertNull(ctx.getMatched().get(vnic));
    }
}
