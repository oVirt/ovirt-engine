package org.ovirt.engine.core.bll.network.vm;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;

@ExtendWith(MockitoExtension.class)
public abstract class AllocateReleaseMacWhenBeingReservedForSnapshotTest {
    private static final CommandContext CMD_CONTEXT = new CommandContext(new EngineContext());
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final Guid UPDATED_NIC_ID = Guid.newGuid();
    private static final Guid OTHER_NIC_ID = Guid.newGuid();
    protected static final Guid VM_ID = Guid.newGuid();
    protected static final String OLD_MAC = "oldmac";
    protected static final String NEW_MAC = "newmac";
    protected static final String OTHER_MAC = "othermac";

    protected VmNetworkInterface nicBeingUpdated = createVnNetworkInterface("nicBeingUpdated", UPDATED_NIC_ID);
    protected VmNetworkInterface updatingNic = createVnNetworkInterface("updatingNic", UPDATED_NIC_ID);
    protected VmNetworkInterface otherNic = createVnNetworkInterface("otherNic", OTHER_NIC_ID);
    protected VM vmOwningNicsBeingExtracted;
    private AddVmInterfaceParameters parameters = new AddVmInterfaceParameters(VM_ID, updatingNic);
    private VmNicFilterParameter filterParameter = new VmNicFilterParameter(Guid.newGuid(), UPDATED_NIC_ID, "name", "value");

    @Mock
    private VmDeviceDao vmDeviceDao;

    @Mock
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Mock
    private VmNicDao vmNicDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private VmNicFilterParameterDao vmNicFilterParameterDao;

    @Mock
    protected SnapshotsManager snapshotsManager;

    @Mock
    protected MacPool macPool;


    @InjectMocks
    UpdateVmInterfaceCommand underTest = new UpdateVmInterfaceCommand<>(parameters, CMD_CONTEXT);

    @BeforeEach
    public void setUp() {
        underTest.setClusterId(CLUSTER_ID);

        //by default other nic has other mac, which does not interfere with nics being updated.
        otherNic.setMacAddress("otherMac");

        vmOwningNicsBeingExtracted = new VM();
        vmOwningNicsBeingExtracted.setInterfaces(Arrays.asList(nicBeingUpdated, otherNic));


        when(vmNicDao.getAllForVm(VM_ID)).thenReturn(Arrays.asList(nicBeingUpdated, otherNic));
        when(vmDao.get(VM_ID)).thenReturn(vmOwningNicsBeingExtracted);
        when(vmNetworkInterfaceDao.getAllForVm(VM_ID)).thenReturn(vmOwningNicsBeingExtracted.getInterfaces());
        when(vmNicFilterParameterDao.getAllForVmNic(UPDATED_NIC_ID)).thenReturn(Collections.singletonList(filterParameter));

        underTest.initVmData();
    }

    private VmNetworkInterface createVnNetworkInterface(String name, Guid id) {
        VmNetworkInterface vmNetworkInterface = new VmNetworkInterface();

        vmNetworkInterface.setName(name);
        vmNetworkInterface.setId(id);

        return vmNetworkInterface;
    }
}
