package org.ovirt.engine.core.common.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;

public class StorageConstants {
    public static final int OVF_MAX_ITEMS_PER_SQL_STATEMENT = 100;

    public static final String HOSTED_ENGINE_LUN_DISK_ALIAS = "hosted_engine";
    public static final String EXPORT = "Export";
    public static final String FILE = "File";
    public static final String SHARED = "Shared";
    public static final String ISO = "ISO";
    public static final String GLANCE_DISK_ALIAS_PREFIX = "GlanceDisk-";
    public static final short LOW_SPACE_THRESHOLD = 100; // low space threshold maximum value (%)
    public static final int ENTITY_FENCING_GENERATION_DIFF = 3;
    public static final String LSM_AUTO_GENERATED_SNAPSHOT_DESCRIPTION = "Auto-generated for Live Storage Migration";
    public static final String OVA_AUTO_GENERATED_SNAPSHOT_DESCRIPTION = "Auto-generated for Export To OVA";
    public static final String CLONE_VM_AUTO_GENERATED_SNAPSHOT_DESCRIPTION = "Auto-generated for Clone VM";
    public static final String GUID = "guid";
    public static final double QCOW_OVERHEAD_FACTOR = 1.1;
    public static final long QCOW_HEADER_OVERHEAD = 1048576;
    public static final String STEP_DEVICE_TYPE = "device";
    public static final String GLUSTER_BACKUP_SERVERS_MNT_OPTION = "backup-volfile-servers";
    public static final String GLUSTER_VOL_SEPARATOR = ":/";
    public static final Set<StorageDomainStatus> monitoredDomainStatuses =
            Collections.unmodifiableSet(EnumSet.of(StorageDomainStatus.Active, StorageDomainStatus.Inactive));
    public static final List<String> HOSTED_ENGINE_DISKS_ALIASES = Arrays.asList("he_virtio_disk", "he_sanlock",
            "HostedEngineConfigurationImage", "he_metadata");
}
