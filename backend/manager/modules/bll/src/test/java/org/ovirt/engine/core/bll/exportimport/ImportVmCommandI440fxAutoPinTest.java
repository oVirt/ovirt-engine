package org.ovirt.engine.core.bll.exportimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * Tests for the wiring of the automatic emulated-machine pinning of imported i440fx
 * VMs (ImportVmCommandBase.autoPinImportedI440fxMachine): the pin value derived from
 * the machine types common to the cluster hosts is applied to the imported VM. The
 * selection logic itself (oldest/newest common type, floating 'pc' alias exclusion)
 * is covered by ClusterEmulatedMachinesTest.
 */
@ExtendWith(MockitoExtension.class)
public class ImportVmCommandI440fxAutoPinTest {
    private static final Guid CLUSTER_ID = new Guid(UUID.randomUUID());

    private final VM vm = createVm();

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private VdsDao vdsDao;

    @Spy
    @InjectMocks
    private ImportVmCommand<ImportVmParameters> cmd =
            new ImportVmCommand<>(createParameters(), null);

    private static VM createVm() {
        VM vm = new VM();
        vm.setName("imported-vm");
        return vm;
    }

    private ImportVmParameters createParameters() {
        return new ImportVmParameters(vm, Guid.Empty, Guid.Empty, Guid.Empty, CLUSTER_ID);
    }

    /**
     * These tests exercise the >= 4.8 behaviour (auto-pin on import); stub a 4.8 cluster.
     */
    private void stubI440fxDeprecatedCluster() {
        Cluster cluster = new Cluster();
        cluster.setClusterId(CLUSTER_ID);
        cluster.setCompatibilityVersion(Version.v4_8);
        when(clusterDao.get(CLUSTER_ID)).thenReturn(cluster);
    }

    // --- pin derivable: the imported VM carries it ---

    @Test
    public void pinDerivableBySelectorIsAppliedToImportedVm() {
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        stubI440fxDeprecatedCluster();
        when(vdsDao.getClusterI440fxCommonMachines(CLUSTER_ID))
                .thenReturn(List.of("pc-i440fx-rhel7.6.0", "pc-i440fx-rhel10.0.0"));

        cmd.autoPinImportedI440fxMachine();

        assertEquals("pc-i440fx-rhel7.6.0", vm.getStaticData().getCustomEmulatedMachine(),
                "the oldest common i440fx machine type must be auto-selected on import");
    }

    // --- no pin derivable: the VM is imported as-is ---

    @Test
    public void noCommonTypeImportsVmUnpinned() {
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        stubI440fxDeprecatedCluster();
        when(vdsDao.getClusterI440fxCommonMachines(CLUSTER_ID)).thenReturn(List.of());

        cmd.autoPinImportedI440fxMachine();

        assertNull(vm.getStaticData().getCustomEmulatedMachine(),
                "import must proceed without a pin when no common type exists");
    }

    // --- no pin needed ---

    @Test
    public void alreadyPinnedVmIsUntouched() {
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        vm.getStaticData().setCustomEmulatedMachine("pc-i440fx-rhel7.3.0");

        cmd.autoPinImportedI440fxMachine();

        assertEquals("pc-i440fx-rhel7.3.0", vm.getStaticData().getCustomEmulatedMachine());
    }

    @Test
    public void q35VmIsUntouched() {
        vm.setBiosType(BiosType.Q35_SEA_BIOS);

        cmd.autoPinImportedI440fxMachine();

        assertNull(vm.getStaticData().getCustomEmulatedMachine());
    }
}
