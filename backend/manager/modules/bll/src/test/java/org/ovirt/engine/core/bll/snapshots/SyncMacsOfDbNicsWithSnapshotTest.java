package org.ovirt.engine.core.bll.snapshots;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

@ExtendWith(MockitoExtension.class)
public class SyncMacsOfDbNicsWithSnapshotTest {

    @Mock
    private MacPool macPool;

    @Mock
    private AuditLogDirector auditLogDirector;

    @Test
    public void testSyncWhenNothingChanged() {
        String macAddress = "1";
        VmNic nic = createNic(macAddress);

        createSyncMacsOfDbNicsWithSnapshot(false).sync(Arrays.asList(nic), Arrays.asList(nic));
        verify(nic, times(2)).getMacAddress();
        verify(macPool).freeMacs(Collections.emptyList());
        verify(macPool).addMacs(Collections.emptyList());
        Mockito.verifyNoMoreInteractions(nic);
    }

    @Test
    public void testSyncWhenNeedToSyncMacsWithStatefulSnapshot() {
        testSyncWhenNeedToSyncMacs(false);
    }

    @Test
    public void testSyncWhenNeedToSyncMacsWithStatelessSnapshot() {
        testSyncWhenNeedToSyncMacs(true);
    }

    private void testSyncWhenNeedToSyncMacs(boolean macsInSnapshotAreExpectedToBeAlreadyAllocated) {
        String snapshottedMac = "1";
        String currentMac = "2";

        VmNic snapshottedNic = createNic(snapshottedMac);
        VmNic currentNic = createNic(currentMac);

        createSyncMacsOfDbNicsWithSnapshot(macsInSnapshotAreExpectedToBeAlreadyAllocated)
                .sync(Arrays.asList(currentNic), Arrays.asList(snapshottedNic));

        verifyMethodCallOn(VmNic::getMacAddress, 1, snapshottedNic, currentNic);
        verify(macPool).freeMacs(Collections.singletonList(currentMac));

        verify(macPool).addMacs(macsInSnapshotAreExpectedToBeAlreadyAllocated
                ? Collections.emptyList()
                : Collections.singletonList(snapshottedMac));

        verifyNoMoreInteractionsOn(snapshottedNic, currentNic);
    }

    private void verifyMethodCallOn(Consumer<VmNic> calledMethod, int times, VmNic... nics) {
        Arrays.stream(nics).forEach(e -> calledMethod.accept(verify(e, times(times))));
    }

    private void verifyNoMoreInteractionsOn(VmNic... nics) {
        Arrays.stream(nics).forEach(Mockito::verifyNoMoreInteractions);
    }

    @Test
    public void testSyncWhenNeedToSyncMacsWithStatefulSnapshotAndRelocate() {
        //note that reallocation is only possible, when macs from snapshots are not reserved.

        String currentMac1 = "1";
        String currentMac2 = "3";
        String snapshottedMac1 = "1";
        String snapshottedMac2 = "2";

        String reallocatingMac = "4";

        VmNic currentNic1 = createNic(currentMac1);
        VmNic currentNic2 = createNic(currentMac2);
        VmNic snapshottedNic1 = createNic(snapshottedMac1);
        VmNic snapshottedNic2 = createNic(snapshottedMac2);

        List<String> macsToBeAdded = Collections.singletonList(snapshottedMac2);
        when(macPool.addMacs(macsToBeAdded)).thenReturn(macsToBeAdded);
        when(macPool.allocateNewMac()).thenReturn(reallocatingMac);

        createSyncMacsOfDbNicsWithSnapshot(false)
                .sync(Arrays.asList(currentNic1, currentNic2), Arrays.asList(snapshottedNic1, snapshottedNic2));

        verify(snapshottedNic2).setMacAddress(reallocatingMac);

        verifyMethodCallOn(VmNic::getMacAddress, 1, currentNic1, currentNic2);

        //because in reallocation all(in this case) snapshotted nics will be queried again.
        verifyMethodCallOn(VmNic::getMacAddress, 2, snapshottedNic1, snapshottedNic2);
        verify(macPool).allocateNewMac();

        verify(macPool).freeMacs(Collections.singletonList(currentMac2));

        verify(macPool).addMacs(macsToBeAdded);
        verifyNoMoreInteractionsOn(snapshottedNic1, snapshottedNic2, currentNic1, currentNic2);
    }

    private VmNic createNic(String macAddress) {
        VmNic nic = Mockito.mock(VmNic.class);

        when(nic.getMacAddress()).thenReturn(macAddress);
        return nic;
    }

    private SyncMacsOfDbNicsWithSnapshot createSyncMacsOfDbNicsWithSnapshot(boolean macsInSnapshotAreExpectedToBeAlreadyAllocated) {
        return new SyncMacsOfDbNicsWithSnapshot(macPool,
                auditLogDirector,
                macsInSnapshotAreExpectedToBeAlreadyAllocated);
    }
}

