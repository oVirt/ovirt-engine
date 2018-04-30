package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionInfo;
import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

public class GetGlusterVolumeOptionsInfoQueryTest extends AbstractQueryTest<GlusterParameters, GetGlusterVolumeOptionsInfoQuery<GlusterParameters>> {

    List<GlusterVolumeOptionInfo> expected;
    GlusterParameters parameters;
    private Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    @Mock
    private GlusterUtil glusterUtils;

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupExpectedGlusterVolumeOptionInfo();
        setupMock();
    }

    private void setupExpectedGlusterVolumeOptionInfo() {
        parameters = new GlusterParameters(CLUSTER_ID);
        expected = new ArrayList<>();
        GlusterVolumeOptionInfo option = new GlusterVolumeOptionInfo();
        option.setKey("cluster.self-heal-window-size");
        option.setDefaultValue("1");
        option.setDescription("Maximum number blocks per file for which self-heal process would be applied simultaneously.");
        expected.add(option);
    }

    private void setupMock() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(expected);
        doReturn(returnValue).when(getQuery()).runVdsCommand(eq(VDSCommandType.GetGlusterVolumeOptionsInfo), any());
        when(glusterUtils.getUpServer(CLUSTER_ID)).thenReturn(getVds(VDSStatus.Up));
    }

    @Test
    public void testExecuteQueryCommnad() {
        when(getQueryParameters().getClusterId()).thenReturn(parameters.getClusterId());
        getQuery().executeQueryCommand();
        List<GlusterVolumeOptionInfo> options = getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(options);
        assertEquals(expected, options);
    }
}
