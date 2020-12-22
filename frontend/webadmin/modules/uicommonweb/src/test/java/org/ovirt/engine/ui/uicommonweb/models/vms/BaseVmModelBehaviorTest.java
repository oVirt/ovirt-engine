package org.ovirt.engine.ui.uicommonweb.models.vms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ConsoleDisconnectAction;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmResumeBehavior;

public abstract class BaseVmModelBehaviorTest extends BaseVmTest {

    protected void setUpVm(VmBase vm) {
        vm.setName(VM_NAME);
        vm.setDescription(DESCRIPTION);
        vm.setComment(COMMENT);
        vm.setOsId(OS_TYPE);
        vm.setMemSizeMb(MEM_SIZE);
        vm.setMinAllocatedMem(MIN_MEM);
        vm.setUsbPolicy(USB_POLICY);
        vm.setNumOfMonitors(NUM_OF_MONITORS);
        vm.setNumOfSockets(NUM_OF_SOCKETS);
        vm.setCpuPerSocket(TOTAL_CPU / NUM_OF_SOCKETS);
        vm.setAllowConsoleReconnect(true);
        vm.setStateless(true);
        vm.setRunAndPause(true);
        vm.setBootMenuEnabled(true);
        vm.setVncKeyboardLayout(VNC_KEYBOARD_LAYOUT);
        vm.setDeleteProtected(true);
        vm.setSsoMethod(SsoMethod.GUEST_AGENT);
        vm.setKernelParams(KERNEL_PARAMS);
        vm.setKernelUrl(KERNEL_PATH);
        vm.setInitrdUrl(INITRD_PATH);
        vm.setSerialNumberPolicy(SerialNumberPolicy.CUSTOM);
        vm.setCustomSerialNumber(CUSTOM_SERIAL_NUMBER);
        vm.setSpiceCopyPasteEnabled(true);
        vm.setSpiceFileTransferEnabled(true);
        vm.setMigrationDowntime(MIGRATION_DOWNTIME);
        vm.setSmartcardEnabled(true);
        vm.setDefaultBootSequence(BOOT_SEQUENCE);
        vm.setAutoConverge(true);
        vm.setMigrateCompressed(true);
        vm.setMigrateEncrypted(true);
        vm.setLargeIconId(LARGE_ICON_ID);
        vm.setSmallIconId(SMALL_ICON_ID);
        vm.setNumOfIoThreads(NUM_OF_IO_THREADS);
        vm.setConsoleDisconnectAction(ConsoleDisconnectAction.LOCK_SCREEN);
        vm.setResumeBehavior(VmResumeBehavior.KILL);
    }

    @BeforeEach
    public void setUpVmBase() {
        setUpVm(getVm());
    }

    @BeforeEach
    public void setUpIconCache() {
        TWO_ICONS_ICON_CACHE.inject();
    }

    @AfterEach
    public void tearDownIconCache() {
        IconCacheBaseVmModelMock.removeMock();
    }

    @Test
    public void testBuildModel() {
        VmModelBehaviorBase behavior = getBehavior();
        final UnitVmModel model = initModel(behavior);
        behavior.buildModel(getVm(), (source, destination) -> verifyBuiltModel(model));
    }

    protected abstract VmBase getVm();

    protected abstract VmModelBehaviorBase getBehavior();

    protected abstract void verifyBuiltModel(UnitVmModel model);

    protected UnitVmModel initModel(VmModelBehaviorBase behavior) {
        final Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(CLUSTER_VERSION);
        cluster.setBiosType(BiosType.Q35_SEA_BIOS);

        UnitVmModel model = spy(createModel(behavior));
        doReturn(cluster).when(model).getSelectedCluster();
        model.initialize();
        model.getInstanceImages().setItems(new ArrayList<InstanceImageLineModel>());

        return model;
    }

    protected UnitVmModel createModel(VmModelBehaviorBase behavior) {
        return new UnitVmModel(behavior, null);
    }

    /** Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.NameAndDescriptionVmBaseToUnitBuilder} */
    protected void verifyBuiltNameAndDescription(UnitVmModel model) {
        assertEquals(VM_NAME, model.getName().getEntity());
        assertEquals(DESCRIPTION, model.getDescription().getEntity());
    }

    /** Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.CommentVmBaseToUnitBuilder} */
    protected void verifyBuiltComment(UnitVmModel model) {
        assertEquals(COMMENT, model.getComment().getEntity());
    }

    /** Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.CommonVmBaseToUnitBuilder} */
    protected void verifyBuiltCommon(UnitVmModel model) {
        verifyBuiltCore(model);
        verifyBuiltHardware(model);

        assertEquals(OS_TYPE, (int) model.getOSType().getSelectedItem());
        assertTrue(model.getAllowConsoleReconnect().getEntity());
        assertTrue(model.getIsStateless().getEntity());
        assertTrue(model.getIsRunAndPause().getEntity());
    }

    /** Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.CoreVmBaseToUnitBuilder} */
    protected void verifyBuiltCore(UnitVmModel model) {
        verifyBuiltKernelParams(model);
        verifyBuiltSerialNumber(model);

        assertTrue(model.getBootMenuEnabled().getEntity());
        assertEquals(VNC_KEYBOARD_LAYOUT, model.getVncKeyboardLayout().getSelectedItem());
        assertTrue(model.getIsDeleteProtected().getEntity());
        assertTrue(model.getSsoMethodGuestAgent().getEntity());
        assertTrue(model.getSpiceFileTransferEnabled().getEntity());
        assertTrue(model.getSpiceCopyPasteEnabled().getEntity());
        assertTrue(model.getAutoConverge().getSelectedItem());
        assertTrue(model.getMigrateCompressed().getSelectedItem());
        assertTrue(model.getMigrateEncrypted().getSelectedItem());
        assertEquals(LARGE_ICON_DATA, model.getIcon().getEntity().getIcon());
        assertEquals(LARGE_OS_DEFAULT_ICON_DATA, model.getIcon().getEntity().getOsDefaultIcon());
        assertEquals(ConsoleDisconnectAction.LOCK_SCREEN, model.getConsoleDisconnectAction().getSelectedItem());
        assertEquals(VmResumeBehavior.KILL, model.getResumeBehavior().getSelectedItem());
    }

    /** Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.SerialNumberPolicyVmBaseToUnitBuilder} */
    protected void verifyBuiltSerialNumber(UnitVmModel model) {
        assertEquals(SERIAL_NUMBER_POLICY, model.getSerialNumberPolicy().getSelectedItem());
        assertEquals(CUSTOM_SERIAL_NUMBER, model.getCustomSerialNumber().getEntity());
    }

    /** Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.KernelParamsVmBaseToUnitBuilder} */
    protected void verifyBuiltKernelParams(UnitVmModel model) {
        assertEquals(KERNEL_PARAMS, model.getKernel_parameters().getEntity());
        assertEquals(KERNEL_PATH, model.getKernel_path().getEntity());
        assertEquals(INITRD_PATH, model.getInitrd_path().getEntity());
    }

    /** Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.HwOnlyVmBaseToUnitBuilder} */
    protected void verifyBuiltHardware(UnitVmModel model) {
        assertEquals(MEM_SIZE, (int) model.getMemSize().getEntity());
        assertEquals(MIN_MEM, (int) model.getMinAllocatedMemory().getEntity());
        assertEquals(USB_ENABLED, model.getIsUsbEnabled().getEntity());
        assertEquals(NUM_OF_MONITORS, (int) model.getNumOfMonitors().getSelectedItem());
        assertEquals(BOOT_SEQUENCE, model.getBootSequence());
        assertEquals(Integer.toString(TOTAL_CPU), model.getTotalCPUCores().getEntity());
        assertEquals(NUM_OF_SOCKETS, (int) model.getNumOfSockets().getSelectedItem());
        assertTrue(model.getIsSmartcardEnabled().getEntity());
        assertEquals(MIGRATION_DOWNTIME, model.getSelectedMigrationDowntime());
        assertEquals(NUM_OF_IO_THREADS, Integer.valueOf(model.getNumOfIoThreads().getEntity()));

    }
}
