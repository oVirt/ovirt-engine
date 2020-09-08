package org.ovirt.engine.ui.uicommonweb.models.vms;

import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.junit.UiCommonSetupExtension;

@ExtendWith(UiCommonSetupExtension.class)
public class BaseVmTest {
    protected static final Guid VM_ID = Guid.newGuid();
    protected static final VmType VM_TYPE = VmType.Desktop;
    protected static final Guid TEMPLATE_GUID = Guid.newGuid();
    protected static final String VM_NAME = "myVm1"; //$NON-NLS-1$
    protected static final int OS_TYPE = 42;
    protected static final int NUM_OF_MONITORS = 2;
    protected static final String DESCRIPTION = "enterprise VM"; //$NON-NLS-1$
    protected static final String COMMENT = "important!"; //$NON-NLS-1$
    protected static final String EMULATED_MACHINE = "rhel_version"; //$NON-NLS-1$
    protected static final String CUSTOM_CPU_NAME = "proc"; //$NON-NLS-1$
    protected static final int MEM_SIZE = 1024;
    protected static final int MAX_MEMORY_SIZE = 4096;
    protected static final int MIN_MEM = 100;
    protected static final Guid CLUSTER_ID = Guid.newGuid();
    protected static final String TIMEZONE = "Europe/Berlin"; //$NON-NLS-1$
    protected static final int NUM_OF_SOCKETS = 4;
    protected static final int TOTAL_CPU = 4;
    protected static final int CORES_PER_SOCKET = 1;
    protected static final int THREADS_PER_CORE = 1;
    protected static final UsbPolicy USB_POLICY = UsbPolicy.ENABLED_NATIVE;
    protected static final boolean USB_ENABLED = true;
    protected static final SsoMethod SSO_METHOD = SsoMethod.GUEST_AGENT;
    protected static final BootSequence BOOT_SEQUENCE = BootSequence.CD;
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
    protected static final Guid CPU_PROFILE_ID = Guid.newGuid();
    protected static final String VNC_KEYBOARD_LAYOUT = "en-us"; //$NON-NLS-1$
    protected static final String VNC_KEYBOARD_LAYOUT_2 = "cz-cs"; //$NON-NLS-1$
    protected static final DisplayType DISPLAY_TYPE = DisplayType.qxl;
    protected static final int PRIORITY = 37;
    protected static final Guid HOST_ID = Guid.newGuid();
    protected static final Guid HOST_ID_2 = Guid.newGuid();
    protected static final MigrationSupport MIGRATION_SUPPORT = MigrationSupport.PINNED_TO_HOST;
    protected static final MigrationSupport MIGRATION_SUPPORT_2 = MigrationSupport.IMPLICITLY_NON_MIGRATABLE;
    protected static final Integer MIGRATION_DOWNTIME = 500;
    protected static final Integer NUM_OF_IO_THREADS = 12;
    protected static final Integer MIGRATION_DOWNTIME_2 = 750;
    protected static final SerialNumberPolicy SERIAL_NUMBER_POLICY = SerialNumberPolicy.CUSTOM;
    protected static final String CUSTOM_SERIAL_NUMBER = "my custom number"; //$NON-NLS-1$

    protected static final String LARGE_ICON_DATA = "largeIcon"; //$NON-NLS-1$
    protected static final String LARGE_OS_DEFAULT_ICON_DATA = "largeOsDefaultIcon"; //$NON-NLS-1$

    protected static final Guid SMALL_ICON_ID = Guid.createGuidFromString("00000000-0000-0000-0000-00000000000a"); //$NON-NLS-1$
    protected static final Guid LARGE_ICON_ID = Guid.createGuidFromString("00000000-0000-0000-0000-00000000001a"); //$NON-NLS-1$
    protected static final Guid LARGE_OS_DEFAULT_ICON_ID = Guid.createGuidFromString("00000000-0000-0000-0000-00000000001e"); //$NON-NLS-1$

    protected static final IconCacheBaseVmModelMock TWO_ICONS_ICON_CACHE = new IconCacheBaseVmModelMock()
            .put(LARGE_ICON_ID, LARGE_ICON_DATA)
            .put(LARGE_OS_DEFAULT_ICON_ID, LARGE_OS_DEFAULT_ICON_DATA);
    protected static final IconCacheModelVmBaseMock REVERSE_ICON_CACHE = new IconCacheModelVmBaseMock()
            .put(LARGE_ICON_DATA, LARGE_ICON_ID);
    protected static final Version CLUSTER_VERSION = Version.getLast();
    protected static final BiosType BIOS_TYPE = BiosType.Q35_OVMF;
    protected AsyncDataProvider adp;

    @BeforeEach
    public void mockAsyncDataProvider() {
        adp = AsyncDataProvider.getInstance(); // Mocked by UiCommonSetupExtension

        when(adp.getConfigValuePreConverted(ConfigValues.VncKeyboardLayoutValidValues)).thenReturn(Collections.emptyList());
        when(adp.osNameExists(OS_TYPE)).thenReturn(true);
        when(adp.getMaxVmNameLength()).thenReturn(64);
        when(adp.getOsDefaultIconId(OS_TYPE, false)).thenReturn(LARGE_OS_DEFAULT_ICON_ID);
    }

}
