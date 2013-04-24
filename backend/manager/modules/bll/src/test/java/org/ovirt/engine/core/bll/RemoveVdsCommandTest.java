package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsDynamicDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;

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

    /**
     * The command under test.
     */
    private RemoveVdsCommand<RemoveVdsParameters> command;

    private Guid clusterId;

    @Before
    public void setUp() {
        clusterId = Guid.NewGuid();
    }

    private void prepareMocks() {
        doReturn(vdsDAO).when(command).getVdsDAO();
        doReturn(vmStaticDAO).when(command).getVmStaticDAO();
        doReturn(storagePoolDAO).when(command).getStoragePoolDAO();
        doReturn(vdsDynamicDAO).when(command).getVdsDynamicDAO();
        doReturn(glusterBrickDao).when(command).getGlusterBrickDao();
        doReturn(vdsGroupDao).when(command).getVdsGroupDAO();
        doReturn(vdsGroup).when(vdsGroupDao).get(Mockito.any(Guid.class));
        doReturn(clusterUtils).when(command).getClusterUtils();
        when(clusterUtils.getUpServer(clusterId)).thenReturn(getVds(VDSStatus.Up));
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.NewGuid());
        vds.setVdsName("gfs1");
        vds.setVdsGroupId(clusterId);
        vds.setStatus(status);
        return vds;
    }


    @Test
    public void canDoActionSucceeds() throws Exception {
        command = spy(new RemoveVdsCommand<RemoveVdsParameters>(new RemoveVdsParameters(Guid.NewGuid(), false)));
        prepareMocks();
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockVmsPinnedToHost(Collections.<String> emptyList());

        mockIsGlusterEnabled(false);
        mockHasVolumeOnServer(false);
        runAndAssertCanDoActionSuccess();
    }

    @Test
    public void canDoActionFailsWhenGlusterHostHasVolumes() throws Exception {
        command = spy(new RemoveVdsCommand<RemoveVdsParameters>(new RemoveVdsParameters(Guid.NewGuid(), false)));
        prepareMocks();
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockVmsPinnedToHost(Collections.<String> emptyList());

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);

        boolean canDoAction = command.canDoAction();
        System.out.println(command.getReturnValue().getCanDoActionMessages());
        assertFalse(canDoAction);
    }

    @Test
    public void canDoActionSucceedsWithForceOption() throws Exception {
        command = spy(new RemoveVdsCommand<RemoveVdsParameters>(new RemoveVdsParameters(Guid.NewGuid(), true)));
        prepareMocks();
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockVmsPinnedToHost(Collections.<String> emptyList());

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);
        runAndAssertCanDoActionSuccess();
    }

    @Test
    public void canDoActionFailsWhenVMsPinnedToHost() throws Exception {
        command = spy(new RemoveVdsCommand<RemoveVdsParameters>(new RemoveVdsParameters(Guid.NewGuid(), false)));
        prepareMocks();
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);

        String vmName = "abc";
        mockVmsPinnedToHost(Arrays.asList(vmName));

        ArrayList<String> messages =
                runAndAssertCanDoActionFailure(VdcBllMessages.ACTION_TYPE_FAILED_DETECTED_PINNED_VMS);

        boolean foundMessage = false;
        for (String message : messages) {
            foundMessage |= message.contains(vmName);
        }

        assertTrue("Can't find VM name in can do action messages", foundMessage);
    }

    /**
     * Run the canDoAction and assert that it fails with the given message, while printing the messages (for easier
     * debug if test fails).
     *
     * @param message
     *            The message that should be in the failed messages.
     * @return The failure messages, so that they can be further examined if needed.
     */
    private ArrayList<String> runAndAssertCanDoActionFailure(VdcBllMessages message) {
        boolean canDoAction = command.canDoAction();
        ArrayList<String> canDoActionMessages = command.getReturnValue().getCanDoActionMessages();

        System.out.println(canDoActionMessages);
        assertFalse(canDoAction);
        assertTrue(canDoActionMessages.contains(message.name()));

        return canDoActionMessages;
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
            brick.setVolumeId(new Guid());
            brick.setServerId(command.getVdsId());
            bricks.add(brick);
        }
        when(glusterBrickDao.getGlusterVolumeBricksByServerId(command.getVdsId())).thenReturn(bricks);
    }

    /**
     * Run the canDoAction and assert that it succeeds, while printing the messages (for easier debug if test fails).
     */
    private void runAndAssertCanDoActionSuccess() {
        boolean canDoAction = command.canDoAction();
        System.out.println(command.getReturnValue().getCanDoActionMessages());
        assertTrue(canDoAction);
    }
}
