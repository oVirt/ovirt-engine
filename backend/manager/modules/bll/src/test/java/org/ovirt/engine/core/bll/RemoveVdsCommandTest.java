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
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsDynamicDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class RemoveVdsCommandTest {
    @Mock
    private VdsDynamicDAO vdsDynamicDAO;

    @Mock
    private StoragePoolDAO storagePoolDAO;

    @Mock
    private VmStaticDAO vmStaticDAO;

    @Mock
    private VdsDAO vdsDAO;

    /**
     * The command under test.
     */
    private RemoveVdsCommand<VdsActionParameters> command;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        VdsActionParameters parameters = createParameters();
        command = spy(new RemoveVdsCommand<VdsActionParameters>(parameters));

        doReturn(vdsDAO).when(command).getVdsDAO();
        doReturn(vmStaticDAO).when(command).getVmStaticDAO();
        doReturn(storagePoolDAO).when(command).getStoragePoolDAO();
        doReturn(vdsDynamicDAO).when(command).getVdsDynamicDAO();
    }

    @Test
    public void canDoActionSucceeds() throws Exception {
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockVmsPinnedToHost(Collections.<String> emptyList());

        runAndAssertCanDoActionSuccess();
    }

    @Test
    public void canDoActionFailsWhenVMsPinnedToHost() throws Exception {
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
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
        vds.setstatus(status);
        when(vdsDAO.get(command.getParameters().getVdsId())).thenReturn(vds);
    }

    /**
     * Run the canDoAction and assert that it succeeds, while printing the messages (for easier debug if test fails).
     */
    private void runAndAssertCanDoActionSuccess() {
        boolean canDoAction = command.canDoAction();
        System.out.println(command.getReturnValue().getCanDoActionMessages());
        assertTrue(canDoAction);
    }

    /**
     * @return Valid parameters for the command.
     */
    private VdsActionParameters createParameters() {
        VdsActionParameters parameters = new VdsActionParameters(Guid.NewGuid());
        return parameters;
    }
}
