package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsDynamicDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.dao.VdsStatisticsDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

@RunWith(MockitoJUnitRunner.class)
public class RemoveVdsCommandTest {
    @Mock
    private VdsDynamicDAO vdsDynamicDAO;

    @Mock
    private StoragePoolDAO storagePoolDAO;

    @Mock
    private VmStaticDAO vmStaticDAO;

    @Mock
    private VdsDAO vdsDAO;

    @Mock
    private VDSGroup vdsGroup;

    @Mock
    private GlusterBrickDao glusterBrickDao;

    @Mock
    private VdsGroupDAO vdsGroupDao;

    @Mock
    private ClusterUtils clusterUtils;

    @Mock
    private GlusterVolumeDao volumeDao;

    @Mock
    private GlusterHooksDao hooksDao;

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    @Mock
    private BackendInternal backend;

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private VdsStaticDAO vdsStaticDao;

    @Mock
    private VdsStatisticsDAO vdsStatisticsDao;

    /**
     * The command under test.
     */
    private RemoveVdsCommand<RemoveVdsParameters> command;

    private Guid clusterId;

    @Before
    public void setUp() {
        clusterId = Guid.newGuid();
    }

    private void prepareMocks() {
        doReturn(vdsDAO).when(command).getVdsDAO();
        doReturn(vmStaticDAO).when(command).getVmStaticDAO();
        doReturn(storagePoolDAO).when(command).getStoragePoolDAO();
        doReturn(vdsDynamicDAO).when(command).getVdsDynamicDAO();
        doReturn(glusterBrickDao).when(command).getGlusterBrickDao();
        doReturn(vdsGroupDao).when(command).getVdsGroupDAO();
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        doReturn(hooksDao).when(command).getGlusterHooksDao();
        doReturn(vdsGroup).when(vdsGroupDao).get(Mockito.any(Guid.class));
        doReturn(clusterUtils).when(command).getClusterUtils();
        doReturn(backend).when(command).getBackend();
        doReturn(vdsBrokerFrontend).when(backend).getResourceManager();
        doReturn(vdsStaticDao).when(command).getVdsStaticDAO();
        doReturn(vdsStatisticsDao).when(command).getVdsStatisticsDao();
        when(clusterUtils.getUpServer(clusterId)).thenReturn(getVds(VDSStatus.Up));
    }

    private void mockHasMultipleClusters(Boolean isMultiple) {
        when(command.getVdsGroupId()).thenReturn(clusterId);
        when(clusterUtils.hasMultipleServers(clusterId)).thenReturn(isMultiple);
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setVdsGroupId(clusterId);
        vds.setStatus(status);
        return vds;
    }


    @Test
    public void canDoActionSucceeds() throws Exception {
        command = spy(new RemoveVdsCommand<RemoveVdsParameters>(new RemoveVdsParameters(Guid.newGuid(), false)));
        prepareMocks();
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockVmsPinnedToHost(Collections.<String> emptyList());

        mockIsGlusterEnabled(false);
        mockHasVolumeOnServer(false);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void canDoActionFailsWhenGlusterHostHasVolumes() throws Exception {
        command = spy(new RemoveVdsCommand<RemoveVdsParameters>(new RemoveVdsParameters(Guid.newGuid(), false)));
        prepareMocks();
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockVmsPinnedToHost(Collections.<String> emptyList());

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
    }

    @Test
    public void canDoActionFailsWhenGlusterMultipleHostHasVolumesWithForce() throws Exception {
        command = spy(new RemoveVdsCommand<RemoveVdsParameters>(new RemoveVdsParameters(Guid.newGuid(), true)));
        prepareMocks();
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockHasMultipleClusters(true);
        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
    }

    @Test
    public void canDoActionSucceedsWithForceOption() throws Exception {
        command = spy(new RemoveVdsCommand<RemoveVdsParameters>(new RemoveVdsParameters(Guid.newGuid(), true)));
        prepareMocks();
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockVmsPinnedToHost(Collections.<String> emptyList());

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void canDoActionFailsWhenVMsPinnedToHost() throws Exception {
        command = spy(new RemoveVdsCommand<RemoveVdsParameters>(new RemoveVdsParameters(Guid.newGuid(), false)));
        prepareMocks();
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);

        String vmName = "abc";
        mockVmsPinnedToHost(Arrays.asList(vmName));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_DETECTED_PINNED_VMS);

        boolean foundMessage = false;
        for (String message : command.getReturnValue().getCanDoActionMessages()) {
            foundMessage |= message.contains(vmName);
        }

        assertTrue("Can't find VM name in can do action messages", foundMessage);
    }

    @Test
    public void removeWhenMultipleHosts() {
        command = spy(new RemoveVdsCommand<RemoveVdsParameters>(new RemoveVdsParameters(Guid.newGuid(), false)));
        prepareMocks();

        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockIsGlusterEnabled(true);
        mockHasMultipleClusters(true);
        mockForExecute();
        command.executeCommand();
        assertEquals(command.getAuditLogTypeValue(), AuditLogType.USER_REMOVE_VDS);
        Mockito.verify(vdsDynamicDAO, times(1)).remove(any(Guid.class));
        Mockito.verify(vdsStatisticsDao, times(1)).remove(any(Guid.class));
        Mockito.verify(volumeDao, never()).removeByClusterId(any(Guid.class));
        Mockito.verify(hooksDao, never()).removeAllInCluster(any(Guid.class));
    }

    @Test
    public void removeLastHost() {
        command = spy(new RemoveVdsCommand<RemoveVdsParameters>(new RemoveVdsParameters(Guid.newGuid(), false)));
        prepareMocks();

        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockIsGlusterEnabled(true);
        mockHasMultipleClusters(false);
        mockForExecute();
        command.executeCommand();
        assertEquals(command.getAuditLogTypeValue(), AuditLogType.USER_REMOVE_VDS);
        Mockito.verify(vdsDynamicDAO, times(1)).remove(any(Guid.class));
        Mockito.verify(vdsStatisticsDao, times(1)).remove(any(Guid.class));
        Mockito.verify(volumeDao, times(1)).removeByClusterId(any(Guid.class));
        Mockito.verify(hooksDao, times(1)).removeAllInCluster(any(Guid.class));
    }

    private void mockForExecute() {
        doReturn(null).when(vdsBrokerFrontend).RunVdsCommand(any(VDSCommandType.class), any(VDSParametersBase.class));
        doNothing().when(vdsStaticDao).remove(any(Guid.class));
        doNothing().when(vdsStatisticsDao).remove(any(Guid.class));
        doNothing().when(vdsDynamicDAO).remove(any(Guid.class));
        doNothing().when(volumeDao).removeByClusterId(any(Guid.class));
        doNothing().when(hooksDao).removeAllInCluster(any(Guid.class));
    }

    /**
     * Mocks that a valid {@link VdsDynamic} gets returned.
     */
    private void mockVdsDynamic() {
        when(vdsDynamicDAO.get(command.getParameters().getVdsId())).thenReturn(new VdsDynamic());
    }

    /**
     * Mocks that the given VMs are pinned to the host (List can be empty, but by the API contract can't be
     * <code>null</code>).
     *
     * @param emptyList
     *            The list of VM names.
     */
    private void mockVmsPinnedToHost(List<String> emptyList) {
        when(vmStaticDAO.getAllNamesPinnedToHost(command.getParameters().getVdsId())).thenReturn(emptyList);
    }

    /**
     * Mocks that a {@link VDS} with the given status is returned.
     *
     * @param status
     *            The status of the VDS.
     */
    private void mockVdsWithStatus(VDSStatus status) {
        VDS vds = new VDS();
        vds.setStatus(status);
        when(vdsDAO.get(command.getParameters().getVdsId())).thenReturn(vds);
    }

    /**
     * Mock that {@link VDSGroup} with the given glusterservice status is returned
     *
     * @param glusterService
     *              The VDSGroup with the given glusterservice status
     */
    private void mockIsGlusterEnabled(boolean glusterService) {
        when(vdsGroup.supportsGlusterService()).thenReturn(glusterService);
    }

    /**
     * Mock that whether the VDS configured with gluster volume. This will return the given volume count
     *
     * @param volumeCount
     *              The volume count on the VDS
     */
    private void mockHasVolumeOnServer(boolean isBricksRequired) {
        List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();
        if (isBricksRequired) {
            GlusterBrickEntity brick = new GlusterBrickEntity();
            brick.setVolumeId(Guid.newGuid());
            brick.setServerId(command.getVdsId());
            bricks.add(brick);
        }
        when(glusterBrickDao.getGlusterVolumeBricksByServerId(command.getVdsId())).thenReturn(bricks);
    }
}
