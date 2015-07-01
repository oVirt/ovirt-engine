package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionInfo;
import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

public class GetGlusterVolumeOptionsInfoQueryTest extends AbstractQueryTest<GlusterParameters, GetGlusterVolumeOptionsInfoQuery<GlusterParameters>> {

    List<GlusterVolumeOptionInfo> expected;
    GlusterParameters parameters;
    private Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    VdsDao vdsDao;
    ClusterUtils clusterUtils;

    private List<VDS> mockGetAllVdsForwithStatus(VDSStatus status) {
        List<VDS> vdsList = new ArrayList<VDS>();
        vdsList.add(getVds(status));
        return vdsList;
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setVdsGroupId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    public void mockVdsDbFacadeAndDao() {
        vdsDao = mock(VdsDao.class);
        when(vdsDao.getAllForVdsGroupWithStatus(CLUSTER_ID, VDSStatus.Up)).thenReturn(mockGetAllVdsForwithStatus(VDSStatus.Up));

        clusterUtils = mock(ClusterUtils.class);
        doReturn(clusterUtils).when(getQuery()).getClusterUtils();

        when(clusterUtils.getUpServer(CLUSTER_ID)).thenReturn(getVds(VDSStatus.Up));
        doReturn(vdsDao).when(clusterUtils).getVdsDao();
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockVdsDbFacadeAndDao();
        setupExpectedGlusterVolumeOptionInfo();
        setupMock();
    }

    private void setupExpectedGlusterVolumeOptionInfo() {
        parameters = new GlusterParameters(CLUSTER_ID);
        expected = new ArrayList<GlusterVolumeOptionInfo>();
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
        doReturn(returnValue).when(getQuery()).runVdsCommand(eq(VDSCommandType.GetGlusterVolumeOptionsInfo),
                any(VDSParametersBase.class));
    }

    @Test
    public void testExecuteQueryCommnad() {
        when(getQueryParameters().getClusterId()).thenReturn(parameters.getClusterId());
        getQuery().executeQueryCommand();
        List<GlusterVolumeOptionInfo> options =
                (List<GlusterVolumeOptionInfo>) getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(options);
        assertEquals(expected, options);
    }
}
