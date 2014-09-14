package org.ovirt.engine.ui.uicommonweb.models.pools;


import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SerialNumberPolicyModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.TimeZoneModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseVmListModelTest {
    protected static final Guid VM_ID = Guid.newGuid();
    protected static final VmType VM_TYPE = VmType.Desktop;
    protected static final Guid TEMPLATE_GUID = Guid.newGuid();
    protected static final String VM_NAME = "myVm1"; //$NON-NLS-1$
    protected static final int OS_TYPE = 42;
    protected static final int NUM_OF_MONITORS = 2;
    protected static final String DESCRIPTION = "enterprise VM"; //$NON-NLS-1$
    protected static final String COMMENT = "important!"; //$NON-NLS-1$
    protected static final int MEM_SIZE = 1024;
    protected static final int MIN_MEM = 100;
    protected static final Guid CLUSTER_ID = Guid.newGuid();
    protected static final String TIMEZONE = "Europe/Berlin"; //$NON-NLS-1$
    protected static final int NUM_OF_SOCKETS = 4;
    protected static final int TOTAL_CPU = 4;
    protected static final UsbPolicy USB_POLICY = UsbPolicy.ENABLED_NATIVE;
    protected static final SsoMethod SSO_METHOD = SsoMethod.GUEST_AGENT;
    protected static final BootSequence BOOT_SEQUENCE = BootSequence.CDN;
    protected static final String ISO_NAME = "foo.iso";  //$NON-NLS-1$
    protected static final String INITRD_PATH = "initrd_path1";  //$NON-NLS-1$
    protected static final String KERNEL_PATH = "kernel_path1";  //$NON-NLS-1$
    protected static final String KERNEL_PARAMS = "kernel_params1"; //$NON-NLS-1$
    protected static final String INITRD_PATH_2 = "initrd_path2";    //$NON-NLS-1$
    protected static final String KERNEL_PATH_2 = "kernel_path2";    //$NON-NLS-1$
    protected static final String KERNEL_PARAMS_2 = "kernel_params2";  //$NON-NLS-1$
    protected static final String CUSTOM_PROPERTIES = "custom_properties"; //$NON-NLS-1$
    protected static final Guid INSTANCE_TYPE_ID = Guid.newGuid();
    protected static final Guid QUOTA_ID = Guid.newGuid();
    protected static final String VNC_KEYBOARD_LAYOUT = "en-us"; //$NON-NLS-1$
    protected static final String VNC_KEYBOARD_LAYOUT_2 = "cz-cs"; //$NON-NLS-1$
    protected static final DisplayType DISPLAY_TYPE = DisplayType.qxl;
    protected static final int PRIORITY = 37;
    protected static final Guid HOST_ID = Guid.newGuid();
    protected static final Guid HOST_ID_2 = Guid.newGuid();
    protected static final MigrationSupport MIGRATION_SUPPORT = MigrationSupport.PINNED_TO_HOST;
    protected static final MigrationSupport MIGRATION_SUPPORT_2 = MigrationSupport.IMPLICITLY_NON_MIGRATABLE;
    protected static final Integer MIGRATION_DOWNTIME = 500;
    protected static final Integer MIGRATION_DOWNTIME_2 = 750;
    protected static final SerialNumberPolicy SERIAL_NUMBER_POLICY = SerialNumberPolicy.CUSTOM;
    protected static final String CUSTOM_SERIAL_NUMBER = "my custom number"; //$NON-NLS-1$
    protected static final Guid CPU_PROFILE_ID = Guid.newGuid();

    protected void setUpUnitVmModelExpectations(UnitVmModel model) {
        when(model.getVmType().getSelectedItem()).thenReturn(VM_TYPE);
        VmTemplate template = new VmTemplate();
        template.setId(TEMPLATE_GUID);
        when(model.getTemplate().getSelectedItem()).thenReturn(template);
        when(model.getName().getEntity()).thenReturn(VM_NAME);
        InstanceType instanceType = new VmTemplate();
        instanceType.setId(INSTANCE_TYPE_ID);
        when(model.getInstanceTypes().getSelectedItem()).thenReturn(instanceType);
        when(model.getOSType().getSelectedItem()).thenReturn(OS_TYPE);
        when(model.getNumOfMonitors().getSelectedItem()).thenReturn(NUM_OF_MONITORS);
        when(model.getDescription().getEntity()).thenReturn(DESCRIPTION);
        when(model.getComment().getEntity()).thenReturn(COMMENT);
        when(model.getMemSize().getEntity()).thenReturn(MEM_SIZE);
        when(model.getMinAllocatedMemory().getEntity()).thenReturn(MIN_MEM);
        when(model.getSelectedCluster().getId()).thenReturn(CLUSTER_ID);
        ListModel<TimeZoneModel> timeZoneModelListModel = mockTimeZoneListModel();
        when(model.getTimeZone()).thenReturn(timeZoneModelListModel);
        when(model.getNumOfSockets().getSelectedItem()).thenReturn(NUM_OF_SOCKETS);
        SerialNumberPolicyModel serialNumberPolicyModel = mockSerialNumberPolicyModel();
        when(model.getSerialNumberPolicy()).thenReturn(serialNumberPolicyModel);
        when(model.getAllowConsoleReconnect().getEntity()).thenReturn(true);
        when(model.getIsSingleQxlEnabled().getEntity()).thenReturn(true);
        when(model.getTotalCPUCores().getEntity()).thenReturn(Integer.toString(TOTAL_CPU));
        when(model.getUsbPolicy().getSelectedItem()).thenReturn(USB_POLICY);
        when(model.getIsStateless().getEntity()).thenReturn(true);
        when(model.getIsSmartcardEnabled().getEntity()).thenReturn(true);
        when(model.getIsDeleteProtected().getEntity()).thenReturn(true);
        when(model.extractSelectedSsoMethod()).thenReturn(SSO_METHOD);
        when(model.getBootSequence()).thenReturn(BOOT_SEQUENCE);
        ListModel<String> cdListModel = mockCdListModel();
        when(model.getCdImage()).thenReturn(cdListModel);
        when(model.getIsHighlyAvailable().getEntity()).thenReturn(true);
        when(model.getInitrd_path().getEntity()).thenReturn(INITRD_PATH);
        when(model.getKernel_path().getEntity()).thenReturn(KERNEL_PATH);
        when(model.getKernel_parameters().getEntity()).thenReturn(KERNEL_PARAMS);
        when(model.getCustomPropertySheet().serialize()).thenReturn(CUSTOM_PROPERTIES);
        ListModel<Quota> quotaListModel = mockQuotaListModel();
        when(model.getQuota()).thenReturn(quotaListModel);
        when(model.getVncKeyboardLayout().getSelectedItem()).thenReturn(VNC_KEYBOARD_LAYOUT);
        EntityModel<DisplayType> displayTypeEntityModel = mockEntityModel(DISPLAY_TYPE);
        when(model.getDisplayProtocol().getSelectedItem()).thenReturn(displayTypeEntityModel);
        EntityModel<Integer> priorityEntityModel = mockEntityModel(PRIORITY);
        when(model.getPriority().getSelectedItem()).thenReturn(priorityEntityModel);
        when(model.getIsRunAndPause().getEntity()).thenReturn(true);
        VDS defaultHost = new VDS();
        defaultHost.setId(HOST_ID);
        when(model.getDefaultHost().getSelectedItem()).thenReturn(defaultHost);
        when(model.getIsAutoAssign().getEntity()).thenReturn(false);
        when(model.getMigrationMode().getSelectedItem()).thenReturn(MIGRATION_SUPPORT);
        when(model.getSelectedMigrationDowntime()).thenReturn(MIGRATION_DOWNTIME);
        when(model.getBootMenuEnabled().getEntity()).thenReturn(true);
        when(model.getSpiceFileTransferEnabled().getEntity()).thenReturn(true);
        when(model.getSpiceCopyPasteEnabled().getEntity()).thenReturn(true);
        ListModel<CpuProfile> cpuProfiles = mockCpuProfiles();
        when(model.getCpuProfiles()).thenReturn(cpuProfiles);
        when(model.getNumaNodeCount().getEntity()).thenReturn(0);
        when(model.getNumaTuneMode().getSelectedItem()).thenReturn(NumaTuneMode.INTERLEAVE);
    }

    protected void setUpOrigVm(VM origVm) {
        origVm.setId(VM_ID);
        origVm.setInitrdUrl(INITRD_PATH_2);
        origVm.setKernelUrl(KERNEL_PATH_2);
        origVm.setKernelParams(KERNEL_PARAMS_2);
    }

    /**
     * Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.CoreUnitToVmBaseBuilder}
     */
    protected void verifyBuiltCoreVm(VmBase vm) {
        assertTrue(vm.isAllowConsoleReconnect());
        assertEquals(VM_TYPE, vm.getVmType());
        assertEquals(OS_TYPE, vm.getOsId());
        assertEquals(TIMEZONE, vm.getTimeZone());
        assertEquals(CLUSTER_ID, vm.getVdsGroupId());
        assertEquals(BOOT_SEQUENCE, vm.getDefaultBootSequence());
        assertTrue(vm.isBootMenuEnabled());
        assertEquals(ISO_NAME, vm.getIsoPath());
        assertEquals(MEM_SIZE, vm.getMemSizeMb());
        assertEquals(MIN_MEM, vm.getMinAllocatedMem());
        assertEquals(NUM_OF_MONITORS, vm.getNumOfMonitors());
        assertEquals(SERIAL_NUMBER_POLICY, vm.getSerialNumberPolicy());
        assertEquals(CUSTOM_SERIAL_NUMBER, vm.getCustomSerialNumber());
        assertTrue(vm.getSingleQxlPci());
        assertTrue(vm.isSmartcardEnabled());
        assertEquals(SSO_METHOD, vm.getSsoMethod());
        assertEquals(NUM_OF_SOCKETS, vm.getNumOfSockets());
        assertEquals(TOTAL_CPU / NUM_OF_SOCKETS, vm.getCpuPerSocket());
        assertTrue(vm.isDeleteProtected());
        assertEquals(VNC_KEYBOARD_LAYOUT, vm.getVncKeyboardLayout());
        assertEquals(DISPLAY_TYPE, vm.getDefaultDisplayType());
        assertTrue(vm.isSpiceFileTransferEnabled());
        assertTrue(vm.isSpiceCopyPasteEnabled());
    }

    /**
     * Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.CommonUnitToVmBaseBuilder}
     */
    protected void verifyBuiltCommonVm(VmBase vm) {
        verifyBuiltCoreVm(vm);

        assertTrue(vm.isStateless());
        assertTrue(vm.isRunAndPause());
        assertTrue(vm.isAutoStartup());
        assertEquals(QUOTA_ID, vm.getQuotaId());
        assertEquals(CPU_PROFILE_ID, vm.getCpuProfileId());
        assertEquals(PRIORITY, vm.getPriority());
        assertEquals(DESCRIPTION, vm.getDescription());
        assertEquals(COMMENT, vm.getComment());
    }

    /**
     * Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.KernelParamsUnitToVmBaseBuilder}
     */
    protected void verifyBuiltKernelOptions(VmBase vm) {
        assertEquals(INITRD_PATH, vm.getInitrdUrl());
        assertEquals(KERNEL_PATH, vm.getKernelUrl());
        assertEquals(KERNEL_PARAMS, vm.getKernelParams());
    }

    /**
     * Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.FullUnitToVmBaseBuilder}
     */
    protected void verifyBuiltVmBase(VmBase vm) {
        verifyBuiltCommonVm(vm);
        verifyBuiltKernelOptions(vm);
        verifyBuiltMigrationOptions(vm);

        assertEquals(HOST_ID, vm.getDedicatedVmForVds());
        assertEquals(VM_NAME, vm.getName());
        assertEquals(USB_POLICY, vm.getUsbPolicy());

    }

    protected void verifyBuiltVm(VM vm) {
        verifyBuiltVmBase(vm.getStaticData());
        verifyBuiltVmSpecific(vm);
    }

    /**
     * Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.MigrationOptionsUnitToVmBaseBuilder}
     */
    protected void verifyBuiltMigrationOptions(VmBase vm) {
        assertEquals(MIGRATION_SUPPORT, vm.getMigrationSupport());
        assertEquals(MIGRATION_DOWNTIME, vm.getMigrationDowntime());
    }

    /**
     * Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.VmSpecificUnitToVmBuilder}
     */
    protected void verifyBuiltVmSpecific(VM vm) {
        assertEquals(TEMPLATE_GUID, vm.getVmtGuid());
        assertEquals(CUSTOM_PROPERTIES, vm.getCustomProperties());
        assertEquals(INSTANCE_TYPE_ID, vm.getInstanceTypeId());
    }

    protected void verifyBuiltOrigVm(VM origVm, VM vm) {
        verifyOrigKernelParams(origVm, vm);

        assertEquals(VM_ID, vm.getId());
        assertEquals(origVm.getUsbPolicy(), vm.getUsbPolicy());
    }

    /**
     * Verifies {@link org.ovirt.engine.ui.uicommonweb.builders.vm.KernelParamsVmBaseToVmBaseBuilder}
     */
    protected void verifyOrigKernelParams(VM origVm, VM vm) {
        assertEquals(origVm.getInitrdUrl(), vm.getInitrdUrl());
        assertEquals(origVm.getKernelUrl(), vm.getKernelUrl());
        assertEquals(origVm.getKernelParams(), vm.getKernelParams());
    }

    @SuppressWarnings("unchecked")
    protected static <T> EntityModel<T> mockEntityModel(T entity) {
        EntityModel<T> model = mock(EntityModel.class);
        when(model.getEntity()).thenReturn(entity);

        return model;
    }

    @SuppressWarnings("unchecked")
    protected static <T> ListModel<T> mockListModel(T selectedItem) {
        ListModel<T> model = mock(ListModel.class);
        when(model.getSelectedItem()).thenReturn(selectedItem);

        return model;
    }

    protected ListModel<TimeZoneModel> mockTimeZoneListModel() {
        final TimeZoneModel timeZoneModel = mock(TimeZoneModel.class);
        when(timeZoneModel.getTimeZoneKey()).thenReturn(TIMEZONE);

        final ListModel<TimeZoneModel> model = mockListModel(timeZoneModel);
        when(model.getIsAvailable()).thenReturn(true);

        return model;
    }

    protected ListModel<String> mockCdListModel() {
        final ListModel<String> model = mockListModel(ISO_NAME);
        when(model.getIsChangable()).thenReturn(true);

        return model;
    }

    protected ListModel<Quota> mockQuotaListModel() {
        final Quota quota = new Quota();
        quota.setId(QUOTA_ID);
        final ListModel<Quota> model = mockListModel(quota);
        when(model.getIsAvailable()).thenReturn(true);

        return model;
    }

    protected SerialNumberPolicyModel mockSerialNumberPolicyModel() {
        final SerialNumberPolicyModel model = mock(SerialNumberPolicyModel.class);
        final EntityModel<String> customSerialNumber = mockEntityModel(CUSTOM_SERIAL_NUMBER);
        when(model.getSelectedSerialNumberPolicy()).thenReturn(SERIAL_NUMBER_POLICY);
        when(model.getCustomSerialNumber()).thenReturn(customSerialNumber);

        return model;
    }

    protected ListModel<CpuProfile> mockCpuProfiles() {
        CpuProfile cpuProfile = new CpuProfile();
        cpuProfile.setId(CPU_PROFILE_ID);
        final ListModel<CpuProfile> model = mockListModel(cpuProfile);
        when(model.getIsAvailable()).thenReturn(true);

        return model;
    }
}
