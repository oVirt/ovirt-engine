package org.ovirt.engine.ui.uicommonweb.models.vms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import java.util.ArrayList;

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
        vm.setSingleQxlPci(true);
        vm.setAutoConverge(true);
        vm.setMigrateCompressed(true);
    }

    @Before
    public void setUpVmBase() {
        setUpVm(getVm());
    }

    @Test
    public void testBuildModel() {
        VmModelBehaviorBase behavior = getBehavior();
        final UnitVmModel model = createModel(behavior);
        behavior.buildModel(getVm(), new BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel>() {
            @Override
            public void finished(VmBase source, UnitVmModel destination) {
                verifyBuiltModel(model);
            }
        });
    }

    protected abstract VmBase getVm();

    protected abstract VmModelBehaviorBase getBehavior();

    protected abstract void verifyBuiltModel(UnitVmModel model);

    protected UnitVmModel createModel(VmModelBehaviorBase behavior) {
        final VDSGroup cluster = new VDSGroup();
        cluster.setCompatibilityVersion(CLUSTER_VERSION);

        UnitVmModel model = new UnitVmModel(behavior, null) {
            @Override
            public EntityModel<Boolean> getIsSingleQxlEnabled() {
                return new EntityModel<Boolean>(true);
            }

            @Override
            public VDSGroup getSelectedCluster() {
                return cluster;
            }
        };
        model.initialize(null);
        model.getInstanceImages().setItems(new ArrayList<InstanceImageLineModel>());
        mockAsyncDataProvider(model);
        return model;
    }

    protected void mockAsyncDataProvider(UnitVmModel model) {
        when(adp.supportedForUnitVmModel(ConfigurationValues.BootMenuSupported, model)).thenReturn(true);
        when(adp.supportedForUnitVmModel(ConfigurationValues.SpiceFileTransferToggleSupported, model)).thenReturn(true);
        when(adp.supportedForUnitVmModel(ConfigurationValues.SpiceCopyPasteToggleSupported, model)).thenReturn(true);
        when(adp.supportedForUnitVmModel(ConfigurationValues.AutoConvergenceSupported, model)).thenReturn(true);
        when(adp.supportedForUnitVmModel(ConfigurationValues.MigrationCompressionSupported, model)).thenReturn(true);
        when(adp.supportedForUnitVmModel(ConfigurationValues.SerialNumberPolicySupported, model)).thenReturn(true);
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
    }

    /** Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.SerialNumberPolicyVmBaseToUnitBuilder} */
    protected void verifyBuiltSerialNumber(UnitVmModel model) {
        assertEquals(SERIAL_NUMBER_POLICY, model.getSerialNumberPolicy().getSelectedSerialNumberPolicy());
        assertEquals(CUSTOM_SERIAL_NUMBER, model.getSerialNumberPolicy().getCustomSerialNumber().getEntity());
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
        assertEquals(USB_POLICY, model.getUsbPolicy().getSelectedItem());
        assertEquals(NUM_OF_MONITORS, (int) model.getNumOfMonitors().getSelectedItem());
        assertTrue(model.getIsSingleQxlEnabled().getEntity());
        assertEquals(BOOT_SEQUENCE, model.getBootSequence());
        assertEquals(Integer.toString(TOTAL_CPU), model.getTotalCPUCores().getEntity());
        assertEquals(NUM_OF_SOCKETS, (int) model.getNumOfSockets().getSelectedItem());
        assertTrue(model.getIsSmartcardEnabled().getEntity());
        assertEquals(MIGRATION_DOWNTIME, model.getSelectedMigrationDowntime());
    }
}
